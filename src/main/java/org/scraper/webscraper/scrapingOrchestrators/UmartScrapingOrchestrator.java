package org.scraper.webscraper.scrapingOrchestrators;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.scraper.webscraper.dtos.GenericPricePointDTO;
import org.scraper.webscraper.productServices.GenericScrapingService;
import org.scraper.webscraper.vendorTemplates.UmartProductScraper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.scraper.webscraper.constants.CurrencyConstants.AUD;
import static org.scraper.webscraper.constants.ScrapingConstants.*;
import static org.scraper.webscraper.constants.VendorCSSLocations.UMART_CSS_MODEL_LOCATION;
import static org.scraper.webscraper.constants.VendorCSSLocations.UMART_CSS_PRICE_LOCATION;
import static org.scraper.webscraper.constants.VendorNames.UMART;

@Log
@Component
@RequiredArgsConstructor
public class UmartScrapingOrchestrator implements GenericScrapingOrchestrator {

    private final RestClient restClient;
    private final UmartProductScraper umartScraper;
    private final GenericScrapingService genericScrapingService;

    // SCRAPING JOBS
    @Scheduled(cron = UMART_GPU_SCRAPING_TIME)
    public void runGPUJob() {
        List<String> urls = getProductURLs("/api/v1/umartproducts/gpu-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls);
        postPricePointsAndLogResponse("/api/v1/gpu-pricepoints", "GPU", pricePoints);
    }

    @Scheduled(cron = UMART_RAM_SCRAPING_TIME)
    public void runRAMJob() {
        List<String> urls = getProductURLs("/api/v1/umartproducts/ram-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls);
        postPricePointsAndLogResponse("/api/v1/ram-pricepoints", "RAM", pricePoints);
    }

    @Scheduled(cron = UMART_CPU_SCRAPING_TIME)
    public void runCPUJob() {
        List<String> urls = getProductURLs("/api/v1/umartproducts/cpu-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls);
        postPricePointsAndLogResponse("/api/v1/cpu-pricepoints", "CPU", pricePoints);
    }

    @Scheduled(cron = UMART_GPU_WORKSTATION_SCRAPING_TIME)
    public void runGPUWorkstationJob() {
        List<String> urls = getProductURLs("/api/v1/umartproducts/workstation-gpu-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls);
        postPricePointsAndLogResponse("/api/v1/workstation-gpu-pricepoints", "GPU Workstation", pricePoints);
    }

    @Scheduled(cron = UMART_HDD_SCRAPING_TIME)
    public void runHDDJob() {
        List<String> urls = getProductURLs("/api/v1/umartproducts/hdd-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls);
        postPricePointsAndLogResponse("/api/v1/hdd-pricepoints", "HDD", pricePoints);
    }

    @Scheduled(cron = UMART_SSD_SCRAPING_TIME)
    public void runSSDJob() {
        List<String> urls = getProductURLs("/api/v1/umartproducts/ssd-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls);
        postPricePointsAndLogResponse("/api/v1/ssd-pricepoints", "SSD", pricePoints);
    }

    @Scheduled(cron = UMART_NVME_SCRAPING_TIME)
    public void runNVMEJob() {
        List<String> urls = getProductURLs("/api/v1/umartproducts/nvme-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls);
        postPricePointsAndLogResponse("/api/v1/nvme-pricepoints", "NVME", pricePoints);
    }
    
    // COMMON METHODS
    /** Hits the monolith's product URL GET endpoint and returns a list of strings (URLs) to be parsed. The URI
     * parameter is a relative instead of absolute path. Correct input would look something like:
     * /api/v1/[insert product name]-pricepoints. */
    public List<String> getProductURLs(String uri) {
        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /** Hits the monolith's price point POST endpoint with a list of GenericPrictPointDTOs and returns the corresponding
     * response entity. */
    public ResponseEntity<Void> postPricePoints(String uri, List<GenericPricePointDTO> pricePointDTOs) {
        return restClient.post()
                .uri(uri)
                .body(pricePointDTOs)
                .retrieve()
                .toBodilessEntity();
    }

    /** Scrapes each of the URLs passed and returns a corresponding list of GenericPricePointDTOs. Failed URLs are
     * dropped from the return list. */
    public List<GenericPricePointDTO> scrapeProductURLs(List<String> urls) {
        Instant start = Instant.now();

        List<GenericPricePointDTO> pricePoints = urls
                .stream()
                .map(url -> processPricePoint(umartScraper, genericScrapingService, UMART_SLEEPING_CONSTANT,
                        url, UMART_CSS_MODEL_LOCATION, UMART_CSS_PRICE_LOCATION, UMART, AUD))
                .flatMap(Optional::stream)
                .toList();

        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        log.info("%s CPU scraping service took %d seconds to execute.".formatted(UMART, timeElapsed.toSeconds()));

        return pricePoints;
    }

    /** Hits the monolith's price point POST endpoint and logs the result. */
    public void postPricePointsAndLogResponse(String uri, String productType, List<GenericPricePointDTO> pricePointDTOs) {
        try {
            ResponseEntity<Void> response = postPricePoints(uri, pricePointDTOs);
            log.info("%s %s price point POST returned status %s".formatted(UMART, productType, response.getStatusCode()));
        } catch (RestClientResponseException e) {
            log.severe("%s %s price point POST failed with status %s: %s"
                    .formatted(UMART, productType, e.getStatusCode(), e.getResponseBodyAsString()));
        }
    }
}