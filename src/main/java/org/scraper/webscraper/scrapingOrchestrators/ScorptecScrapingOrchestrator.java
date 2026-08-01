package org.scraper.webscraper.scrapingOrchestrators;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.scraper.webscraper.dtos.GenericPricePointDTO;
import org.scraper.webscraper.productServices.GenericScrapingService;
import org.scraper.webscraper.vendorTemplates.ScorptecProductScraper;
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
import static org.scraper.webscraper.constants.VendorCSSLocations.SCORPTEC_CSS_MODEL_LOCATION;
import static org.scraper.webscraper.constants.VendorCSSLocations.SCORPTEC_CSS_PRICE_LOCATION;
import static org.scraper.webscraper.constants.VendorNames.SCORPTEC;

@Log
@Component
@RequiredArgsConstructor
public class ScorptecScrapingOrchestrator implements GenericScrapingOrchestrator {

    private final RestClient restClient;
    private final ScorptecProductScraper scorptecScraper;
    private final GenericScrapingService genericScrapingService;

    // SCRAPING JOBS
    @Scheduled(cron = SCORPTEC_GPU_SCRAPING_TIME)
    public void runGPUJob() {
        List<String> urls = getProductURLs("/api/v1/scorptecproducts/gpu-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls, "GPU");
        postPricePointsAndLogResponse("/api/v1/gpu-pricepoints", "GPU", pricePoints);
    }

    @Scheduled(cron = SCORPTEC_RAM_SCRAPING_TIME)
    public void runRAMJob() {
        List<String> urls = getProductURLs("/api/v1/scorptecproducts/ram-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls, "RAM");
        postPricePointsAndLogResponse("/api/v1/ram-pricepoints", "RAM", pricePoints);
    }

    @Scheduled(cron = SCORPTEC_CPU_SCRAPING_TIME)
    public void runCPUJob() {
        List<String> urls = getProductURLs("/api/v1/scorptecproducts/cpu-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls, "CPU");
        postPricePointsAndLogResponse("/api/v1/cpu-pricepoints", "CPU", pricePoints);
    }

    @Scheduled(cron = SCORPTEC_GPU_WORKSTATION_SCRAPING_TIME)
    public void runGPUWorkstationJob() {
        List<String> urls = getProductURLs("/api/v1/scorptecproducts/workstation-gpu-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls, "GPU Workstation");
        postPricePointsAndLogResponse("/api/v1/workstation-gpu-pricepoints", "GPU Workstation", pricePoints);
    }

    @Scheduled(cron = SCORPTEC_HDD_SCRAPING_TIME)
    public void runHDDJob() {
        List<String> urls = getProductURLs("/api/v1/scorptecproducts/hdd-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls, "HDD");
        postPricePointsAndLogResponse("/api/v1/hdd-pricepoints", "HDD", pricePoints);
    }

    @Scheduled(cron = SCORPTEC_SSD_SCRAPING_TIME)
    public void runSSDJob() {
        List<String> urls = getProductURLs("/api/v1/scorptecproducts/ssd-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls, "SSD");
        postPricePointsAndLogResponse("/api/v1/ssd-pricepoints", "SSD", pricePoints);
    }

    @Scheduled(cron = SCORPTEC_NVME_SCRAPING_TIME)
    public void runNVMEJob() {
        List<String> urls = getProductURLs("/api/v1/scorptecproducts/nvme-page-links");
        List<GenericPricePointDTO> pricePoints = scrapeProductURLs(urls, "NVME");
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
    public List<GenericPricePointDTO> scrapeProductURLs(List<String> urls, String productType) {
        Instant start = Instant.now();

        List<GenericPricePointDTO> pricePoints = urls
                .stream()
                .map(url -> processPricePoint(scorptecScraper, genericScrapingService, SCORPTEC_SLEEPING_CONSTANT,
                        url, SCORPTEC_CSS_MODEL_LOCATION, SCORPTEC_CSS_PRICE_LOCATION, SCORPTEC, AUD))
                .flatMap(Optional::stream)
                .toList();

        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        log.info("%s %s scraping service took %d seconds to execute.".formatted(SCORPTEC, productType, timeElapsed.toSeconds()));

        return pricePoints;
    }

    /** Hits the monolith's price point POST endpoint and logs the result. */
    public void postPricePointsAndLogResponse(String uri, String productType, List<GenericPricePointDTO> pricePointDTOs) {
        try {
            ResponseEntity<Void> response = postPricePoints(uri, pricePointDTOs);
            log.info("%s %s price point POST returned status %s".formatted(SCORPTEC, productType, response.getStatusCode()));
        } catch (RestClientResponseException e) {
            log.severe("%s %s price point POST failed with status %s: %s"
                    .formatted(SCORPTEC, productType, e.getStatusCode(), e.getResponseBodyAsString()));
        }
    }
}