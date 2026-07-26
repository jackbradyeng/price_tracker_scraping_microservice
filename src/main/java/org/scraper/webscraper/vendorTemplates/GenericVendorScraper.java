package org.scraper.webscraper.vendorTemplates;

import org.scraper.webscraper.dtos.ScrapedDataDTO;
import java.util.Optional;

public interface GenericVendorScraper {

    Optional<ScrapedDataDTO> scrapeProductData(String url, String modelNumberLocation, String priceLocation);
}