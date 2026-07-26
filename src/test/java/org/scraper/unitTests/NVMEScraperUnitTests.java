package org.scraper.unitTests;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.scraper.documentCapture.DocumentLoader;
import org.scraper.webscraper.dtos.ScrapedDataDTO;
import org.scraper.webscraper.logging.PricePointObserver;
import org.scraper.webscraper.vendorTemplates.UmartProductScraper;
import java.math.BigDecimal;
import java.util.Optional;
import static org.scraper.testingData.vendorData.VendorWebDomainNames.UMART_CRUCIAL_P510_1TB;
import static org.scraper.webscraper.constants.VendorCSSLocations.UMART_CSS_MODEL_LOCATION;
import static org.scraper.webscraper.constants.VendorCSSLocations.UMART_CSS_PRICE_LOCATION;

public class NVMEScraperUnitTests {

    private final UmartProductScraper umartProductScraper = new UmartProductScraper(new PricePointObserver());

    @Test
    public void testThatUmartNVMEScraperReturnsExpectedModelNumber() {
        Document document = DocumentLoader.load("umart/crucial_p510_1tb.html");
        Optional<ScrapedDataDTO> scrapedDataDTO = umartProductScraper
                .parseProductData(document, UMART_CRUCIAL_P510_1TB, UMART_CSS_MODEL_LOCATION, UMART_CSS_PRICE_LOCATION);
        assert scrapedDataDTO.isPresent() && scrapedDataDTO.get().modelNumber().equals("CT1000P510SSD8");
    }

    @Test
    public void testThatUmartNVMEScraperRemovesSemicolon() {
        String refinedModelNumber = umartProductScraper.refineModelNumber("Model Number : " + "CT1000P510SSD8");
        assert refinedModelNumber.equals("CT1000P510SSD8");
    }

    @Test
    public void testThatUmartNVMEScraperRemovesSingleComma() {
        BigDecimal refinedPrice = umartProductScraper.refinePrice("2,999.00");
        assert refinedPrice.equals(new BigDecimal("2999.00"));
    }

    @Test
    public void testThatUmartNVMEScraperRemovesMultipleCommas() {
        BigDecimal refinedPrice = umartProductScraper.refinePrice("1,999,000.00");
        assert refinedPrice.equals(new BigDecimal("1999000.00"));
    }
}