# Price Tracker Scraping Microservice

## Overview

This is the scraping module of the hardware price tracker project. Its purpose is to visit vendor product pages (i.e. **Umart** and **Scorptec**), extract the associated model number and price for each product, and turn that into a structured price point. The resulting stream of price points is then transmitted to the monolith, where it is validated and persisted.

The reason this codebase exists outside the main backend service, i.e. `hardware_price_tracker`, is two-fold:

1. **To simplify the problem of multiple price point insertions.** Since the monolith is deployed on a cloud instance (i.e. AWS EC2), integrating the scraping module into the main codebase makes persistence inefficient at scale. If the need to spawn multiple instances ever arises, then the scraping service would double up on the number of insertions to the DB. Extracting this logic into its own microservice ensures it remains a singleton service, at the cost of introducing network round-trips and additional validation.
2. **To avoid the firewall problem of web scraping from the cloud.** Web Application Firewalls (WAFs) like Cloudflare frequently classify cloud traffic as bot activity. In testing, this led to occasional 429 responses as well as silent failures. Coordinating product scraping locally from a residential IP partially solves this problem, although rate-limiting still needs to be considered.

Ultimately, it was decided that a microservice would be the cleanest and lowest-cost solution to these problems, with the added cross-service coupling being an acceptable trade-off. It's worth elaborating on the data flow in greater detail, which can be described as follows:

1. **GET** a list of product page URLs from the monolith (e.g. `/api/v1/umartproducts/gpu-page-links`).
2. Scrape each URL with Jsoup, throttling requests per vendor to avoid tripping rate limits / WAFs.
3. Map each successful scrape into a `GenericPricePointDTO`.
4. **POST** the batch of price points back to the monolith (e.g. `/api/v1/gpu-pricepoints`).

All communication with the monolith goes through a single authenticated `RestClient` (HTTP Basic Auth, base URL and credentials supplied via environment variables) — this microservice never talks to a database directly. It is purely: *fetch URLs from the monolith → scrape → post results back to the monolith*.

---

## Tech Stack

| Layer                   | Technology                                                        |
|-------------------------|-------------------------------------------------------------------|
| Language / Runtime      | Java 21                                                           |
| Application Framework   | Spring Boot 4.0.2 (`spring-boot-starter-web`, `@Scheduled` jobs)  |
| HTTP Client             | Spring `RestClient` (with Basic Auth interceptor)                 |
| HTML Scraping / Parsing | Jsoup 1.15.3                                                      |
| Build Tool              | Maven                                                             |
| Boilerplate Reduction   | Lombok (`@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Log`)  |
| Config / Secrets        | `application.properties` + `secrets.env` (java-dotenv dependency) |
| Testing                 | JUnit 5, with local HTML fixtures for offline scraper tests       |

---

## Architecture

### Core data flow

```
 Monolith                          Scraping Microservice                      Vendor sites
┌──────────┐  GET page links   ┌───────────────────────────┐   HTTP GET     ┌───────────────┐
│          │ <---------------- │  ScrapingOrchestrator     │ -------------> │    Vendor     │
│ Monolith │                   │  (per-vendor, @Scheduled) │ <------------- │  product page │
│          │                   └──────────────┬────────────┘   HTML         └───────────────┘
│          │                                  │
│          │                                  ▼
│          │                   VendorScraper (Jsoup, per-vendor)
│          │                                  │
│          │                                  ▼
│          │                       GenericScrapingService
│          │                                  │
│          │                                  ▼
│          │  POST price points ┌───────────────────────────┐
│          │ <----------------- │    ScrapingOrchestrator   │
└──────────┘                    └───────────────────────────┘
```

For every product type (GPU, CPU, RAM, HDD, SSD, NVME, GPU Workstation) and every vendor, a scheduled job repeats this same GET → scrape → POST cycle independently, staggered in time so vendors and product types don't compete for resources.

### Directory layout

```
src/main/java/org/scraper/
├── ScrapingMicroserviceApplication.java
└── webscraper/
    ├── constants/
    ├── dtos/
    ├── logging/
    ├── productServices/
    ├── restClient/
    ├── scrapingOrchestrators/
    └── vendorTemplates/
src/test/java/org/scraper/
├── documentCapture/
├── testingData/vendorData/
└── unitTests/
```

---

## Design Decisions

**Orchestrators are organized per-vendor, not per-product.** Each vendor (`ScorptecScrapingOrchestrator`, `UmartScrapingOrchestrator`) owns one `@Scheduled` method per product type, but shares its scraper, sleep constant, and `RestClient` across all of them. This mirrors the real constraint driving the design: throttling and scheduling decisions are a property of the **vendor** (its rate limits, its WAF sensitivity), not the product type. For example, Scorptec uses a much larger `SCORPTEC_SLEEPING_CONSTANT` than Umart because it is more aggressive about rate limiting. Keeping all of a vendor's jobs in one class makes that vendor-specific tuning (cron offsets, sleep delay) visible and adjustable in a single place, while the shared "sleep → scrape → map to DTO" flow is factored out into the default method on the `GenericScrapingOrchestrator` interface so it isn't duplicated per vendor.

**Adding a new vendor means implementing two small interfaces, not touching existing code.** `GenericVendorScraper` (scrape) and `GenericScrapingOrchestrator` (orchestrate) define the contract; a new vendor orchestrator only has to supply its own CSS selectors, cron times, sleep constant, and endpoints via the `constants` package. This keeps vendor-specific quirks isolated and stops the list of vendors from becoming a tangle of conditionals in shared code.

**Vendor scrapers separate network I/O from parsing deliberately.** `scrapeProductData` (does the Jsoup HTTP fetch) is a thin wrapper around `parseProductData` (pure HTML parsing, no network call). This split exists so the parsing logic — the part that actually breaks when a vendor changes their page markup — can be unit tested against static HTML fixtures (`src/test/resources/scraperDocuments/`) without hitting the live site on every test run. The `DocumentCaptureTool` is a manual (note: `@Disabled`) utility for refreshing those fixtures from the real vendor pages when markup drifts.

**Scraping failures degrade silently, one product at a time.** Both the scraper (`Optional<ScrapedDataDTO>`) and the orchestrator's per-URL mapping (`flatMap(Optional::stream)`) are designed so a single failed page (missing selector, network timeout, malformed price) drops that one product from the batch instead of failing the whole scheduled job. Every attempt is still logged in full via `PricePointObserver`, so failures are visible without being fatal.

---

## Testing

**Fixtures, not live pages, are the source of truth for tests.** Every unit test in `src/test/java/org/scraper/unitTests/` is organized by product type (`GPUScraperUnitTests`, `CPUScraperUnitTests`, etc.) and exercises both vendors' scrapers against static HTML files captured under `src/test/resources/scraperDocuments/` — never against `umart.com.au` or `scorptec.com.au` directly. This is what the `scrapeProductData` / `parseProductData` split in each vendor scraper is for: `scrapeProductData` does the Jsoup network fetch, while `parseProductData` takes an already-parsed `Document` and does the CSS selection and field refinement. Tests call `parseProductData` directly, so the network call never happens in the test suite at all.

**`DocumentLoader` is the bridge between fixtures and tests.** It's a small helper (`src/test/java/org/scraper/documentCapture/DocumentLoader.java`) with a single static method, `load(relativePath)`, that reads a file from `src/test/resources/scraperDocuments/` and parses it into a Jsoup `Document` with `Jsoup.parse(File, "UTF-8")`. Tests use it to turn a saved HTML file (e.g. `"umart/asus_5070ti.html"`) into the exact same `Document` type that `scrapeProductData` would have handed to `parseProductData` after a real network fetch — so the parsing code under test can't tell the difference between a fixture and a live page.

**Tests deliberately avoid touching live URLs, for a few reasons:**
- **Determinism.** A live vendor page can change its price, stock status, or layout between test runs; a fixture is frozen, so a test failure always means the *parsing logic* broke, not that the price changed.
- **Speed and reliability.** No network round trip means no flakiness from timeouts, DNS issues, or CI/sandbox environments that can't reach the public internet at all.
- **Avoiding vendor rate limits and WAFs.** The scraping orchestrators already throttle live requests deliberately (see `ScrapingConstants`) to avoid being blocked; a test suite that hit live pages on every run (potentially many times a day, across every contributor and CI run) would work directly against that same concern.
- **No secrets or environment required.** Because parsing is tested in isolation from `RestClient`/`secrets.env`, the test suite runs the same way on every machine with zero configuration.

**Sample data (the HTML fixtures) is a snapshot, and snapshots go stale.** The fixtures under `scraperDocuments/` capture what a vendor's product page markup looked like on the day they were saved. Vendors periodically redesign their sites or change class names, at which point the CSS selectors in `VendorCSSLocations` — which target the *live* markup — will stop matching reality even though the fixtures (frozen in the old markup) keep passing. Conversely, if a selector changes and someone updates the fixture without verifying against the real site, the test suite could pass while the production scraper silently fails. For that reason, fixtures need to be periodically refreshed and diffed against real vendor pages — not just left in place indefinitely — using `DocumentCaptureTool` (`src/test/java/org/scraper/documentCapture/DocumentCaptureTool.java`), a manual, `@Disabled` test class that is not part of the automated suite. It hits the live URLs listed in `VendorWebDomainNames` and writes the resulting HTML back into `scraperDocuments/`. It's run by hand (temporarily removing `@Disabled`) whenever a vendor's markup is suspected to have changed, or as a periodic sanity check that the fixtures still reflect what's actually live.

---

## Getting Started

### Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Network access to the monolith backend and to the vendor sites being scraped

### Setup

1. **Clone the repo**
   ```bash
   git clone <repository-url>
   cd price_tracker_scraping_microservice
   ```

2. **Create your own `secrets.env`.** This file is not checked into the repo (it's git-ignored) and holds credentials for the `hardware_price_tracker` monolith, so each developer must create it locally in the project root:
   ```
   BASE_URL=http://localhost:<monolith-port>
   ADMIN_USERNAME=<monolith-admin-username>
   ADMIN_PASSWORD=<monolith-admin-password>
   ```
   These map directly to `@Value("${BASE_URL}")` etc. in `RestClientConfig`, so however you run the service, these three variables must be present in its environment (export them in your shell, add them to your IDE run configuration's environment variables, or source `secrets.env` before launching).

3. The `hardware_price_tracker` monolith needs to be running and reachable at `BASE_URL` — this service has nothing to scrape into without it, since every job starts by fetching product URLs from the monolith's API.

4. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   The service starts on port `8081` (see `application.properties`) and the scheduled scraping jobs will fire at the cron times defined in `ScrapingConstants`.

### Running the tests

```bash
mvn test
```