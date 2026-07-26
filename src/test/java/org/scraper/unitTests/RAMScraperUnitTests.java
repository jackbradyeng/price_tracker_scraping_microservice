package org.scraper.unitTests;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.scraper.documentCapture.DocumentLoader;
import org.scraper.webscraper.dtos.ScrapedDataDTO;
import org.scraper.webscraper.logging.PricePointObserver;
import org.scraper.webscraper.vendorTemplates.UmartProductScraper;
import java.math.BigDecimal;
import java.util.Optional;
import static org.scraper.testingData.vendorData.VendorWebDomainNames.UMART_KINGSTON_KINGSTON_F64G;
import static org.scraper.webscraper.constants.VendorCSSLocations.UMART_CSS_MODEL_LOCATION;
import static org.scraper.webscraper.constants.VendorCSSLocations.UMART_CSS_PRICE_LOCATION;

public class RAMScraperUnitTests {

    private final UmartProductScraper umartProductScraper = new UmartProductScraper(new PricePointObserver());

    @Test
    public void testThatUmartRAMScraperReturnsExpectedModelNumber() {
        Document document = DocumentLoader.load("umart/kingston_f64g.html");
        Optional<ScrapedDataDTO> scrapedDataDTO = umartProductScraper
                .parseProductData(document, UMART_KINGSTON_KINGSTON_F64G, UMART_CSS_MODEL_LOCATION, UMART_CSS_PRICE_LOCATION);
        assert scrapedDataDTO.isPresent() && scrapedDataDTO.get().modelNumber().equals("KF560C36BBE2K2-64");
    }

    @Test
    public void testThatUmartRAMScraperRemovesSemicolon() {
        String refinedModelNumber = umartProductScraper.refineModelNumber("Model Number : " + "KF560C36BBE2K2-64");
        assert refinedModelNumber.equals("KF560C36BBE2K2-64");
    }

    @Test
    public void testThatUmartRAMScraperRemovesSingleComma() {
        BigDecimal refinedPrice = umartProductScraper.refinePrice("1,890.00");
        assert refinedPrice.equals(new BigDecimal("1890.00"));
    }

    @Test
    public void testThatUmartRAMScraperRemovesMultipleCommas() {
        BigDecimal refinedPrice = umartProductScraper.refinePrice("1,240,000.00");
        assert refinedPrice.equals(new BigDecimal("1240000.00"));
    }
}