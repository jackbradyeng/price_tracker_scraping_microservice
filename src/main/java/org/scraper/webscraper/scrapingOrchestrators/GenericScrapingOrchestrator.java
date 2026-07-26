package org.scraper.webscraper.scrapingOrchestrators;

import org.scraper.webscraper.dtos.GenericPricePointDTO;
import org.scraper.webscraper.productServices.GenericScrapingService;
import org.scraper.webscraper.vendorTemplates.GenericVendorScraper;
import java.util.Optional;

/**
 * A high level abstraction for generating GenericPricePointDTOs irrespective of vendor or product type. Takes in a
 * GenericVendorScraper and GenericScrapingService as parameters, along with the associated fields needed to generate a
 * ScrapedDataDTO and THEN a GenericPricePointDTO.
 */
public interface GenericScrapingOrchestrator {

    default Optional<GenericPricePointDTO> processPricePoint(GenericVendorScraper genericVendorScraper,
                                                             GenericScrapingService genericScrapingService,
                                                             Integer sleepingConstant,
                                                             String url,
                                                             String modelNumberLocation,
                                                             String priceLocation,
                                                             String vendor,
                                                             String currency) {
        try {
            Thread.sleep(sleepingConstant);
            return genericVendorScraper
                    .scrapeProductData(url, modelNumberLocation, priceLocation)
                    .map(scrapedData ->
                            genericScrapingService.createGenericPricePoint(scrapedData, vendor, currency));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
}