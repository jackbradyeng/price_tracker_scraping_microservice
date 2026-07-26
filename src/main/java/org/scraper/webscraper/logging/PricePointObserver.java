package org.scraper.webscraper.logging;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

@Log
@Component
public class PricePointObserver {

    /** Logs the scraped model number and price given a particular URL. */
    public void logModelNumberAndPrice(String vendor, String modelNumber, String price, String url) {
        String logEntry = """
            
            ========================================
            %s - PRODUCT - FROM: %s
            Model: %s
            Price: %s
            ========================================
            """.formatted(vendor, url, modelNumber, price);

        log.info(logEntry);
    }
}