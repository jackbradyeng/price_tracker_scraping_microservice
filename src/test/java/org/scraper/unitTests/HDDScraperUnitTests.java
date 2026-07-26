package org.scraper.unitTests;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.scraper.documentCapture.DocumentLoader;
import org.scraper.webscraper.dtos.ScrapedDataDTO;
import org.scraper.webscraper.logging.PricePointObserver;
import org.scraper.webscraper.vendorTemplates.UmartProductScraper;
import java.math.BigDecimal;
import java.util.Optional;
import static org.scraper.testingData.vendorData.VendorWebDomainNames.UMART_SEAGATE_ST2000DM005;
import static org.scraper.webscraper.constants.VendorCSSLocations.UMART_CSS_MODEL_LOCATION;
import static org.scraper.webscraper.constants.VendorCSSLocations.UMART_CSS_PRICE_LOCATION;

public class HDDScraperUnitTests {

    private final UmartProductScraper umartProductScraper = new UmartProductScraper(new PricePointObserver());

    @Test
    public void testThatUmartHDDScraperReturnsExpectedModelNumber() {
        Document document = DocumentLoader.load("umart/seagate_st2000dm005.html");
        Optional<ScrapedDataDTO> scrapedDataDTO = umartProductScraper
                .parseProductData(document, UMART_SEAGATE_ST2000DM005, UMART_CSS_MODEL_LOCATION, UMART_CSS_PRICE_LOCATION);
        assert scrapedDataDTO.isPresent() && scrapedDataDTO.get().modelNumber().equals("ST2000DM005");
    }

    @Test
    public void testThatUmartHDDScraperRemovesSemicolon() {
        String refinedModelNumber = umartProductScraper.refineModelNumber("Model Number : " + "ST2000DM005");
        assert refinedModelNumber.equals("ST2000DM005");
    }

    @Test
    public void testThatUmartHDDScraperRemovesSingleComma() {
        BigDecimal refinedPrice = umartProductScraper.refinePrice("1,099.00");
        assert refinedPrice.equals(new BigDecimal("1099.00"));
    }

    @Test
    public void testThatUmartHDDScraperRemovesMultipleCommas() {
        BigDecimal refinedPrice = umartProductScraper.refinePrice("1,240,000.00");
        assert refinedPrice.equals(new BigDecimal("1240000.00"));
    }
}