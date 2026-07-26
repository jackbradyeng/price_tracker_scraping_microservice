package org.scraper.webscraper.vendorTemplates;

import lombok.extern.java.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.scraper.webscraper.dtos.ScrapedDataDTO;
import org.scraper.webscraper.logging.PricePointObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.logging.Level;
import static org.scraper.webscraper.constants.VendorNames.UMART;

@Log
@Service
public class UmartProductScraper implements GenericVendorScraper {

    private final PricePointObserver pricePointObserver;

    @Autowired
    public UmartProductScraper(PricePointObserver pricePointObserver) { this.pricePointObserver = pricePointObserver; }

    public Optional<ScrapedDataDTO> scrapeProductData(String url, String modelNumberLocation, String priceLocation) {
        try {
            // connect to the url and extract raw HTML
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36")
                    .header("Accept-Language", "en-AU,en;q=0.9")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .referrer("https://www.umart.com.au/")
                    .timeout(15000)
                    .get();

            return parseProductData(document, url, modelNumberLocation, priceLocation);

        } catch (IOException e) {
            log.log(Level.SEVERE, "WARNING: Failed to scrape " + url, e);
            return Optional.empty();
        }
    }

    /** Parses an already-fetched Document, without performing any network I/O. */
    public Optional<ScrapedDataDTO> parseProductData(Document document, String url, String modelNumberLocation,
                                                     String priceLocation) {
        try {
            // select raw model number & price
            String rawModelNumber = document.select(modelNumberLocation).text();
            String rawPrice = document.select(priceLocation).text();

            // fail-fast if the document selection returns nothing
            if(rawModelNumber.isEmpty() || rawPrice.isEmpty()) {
                log.warning("WARNING: One or more fields returned no data.");
                pricePointObserver.logModelNumberAndPrice(UMART, rawModelNumber, rawPrice, url);
                return Optional.empty();
            }

            // refine scraped data
            String modelNumber = refineModelNumber(rawModelNumber);
            BigDecimal price = refinePrice(rawPrice);

            // log and return scraped data
            pricePointObserver.logModelNumberAndPrice(UMART, modelNumber, price.toString(), url);
            return Optional.ofNullable(ScrapedDataDTO.builder()
                    .modelNumber(modelNumber)
                    .price(price)
                    .build());

        } catch (NumberFormatException e) {
            log.log(Level.SEVERE, "WARNING: Failed to scrape " + url, e);
            return Optional.empty();
        }
    }

    /** Removes any header text and semicolons and returns the model number as a String. */
    public String refineModelNumber(String rawModelNumber) {
        return rawModelNumber.contains(":") ? rawModelNumber.split(":")[1].trim() : rawModelNumber;
    }

    /** Removes any trailing commas form the price tag and returns a BigDecimal object. */
    public BigDecimal refinePrice(String rawPrice) {
        return new BigDecimal(rawPrice.replace(",", ""));
    }
}