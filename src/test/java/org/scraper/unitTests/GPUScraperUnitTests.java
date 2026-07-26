package org.scraper.unitTests;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.scraper.documentCapture.DocumentLoader;
import org.scraper.webscraper.dtos.ScrapedDataDTO;
import org.scraper.webscraper.logging.PricePointObserver;
import org.scraper.webscraper.vendorTemplates.ScorptecProductScraper;
import org.scraper.webscraper.vendorTemplates.UmartProductScraper;
import java.math.BigDecimal;
import java.util.Optional;
import static org.scraper.testingData.vendorData.VendorWebDomainNames.SCORPTEC_ASUS_5070TI;
import static org.scraper.testingData.vendorData.VendorWebDomainNames.UMART_ASUS_5070TI;
import static org.scraper.webscraper.constants.VendorCSSLocations.*;

public class GPUScraperUnitTests {

    private final PricePointObserver pricePointObserver = new PricePointObserver();
    private final UmartProductScraper umartProductScraper = new UmartProductScraper(pricePointObserver);
    private final ScorptecProductScraper scorptecProductScraper = new ScorptecProductScraper(pricePointObserver);

    @Test
    public void testThatUmartGPUScraperReturnsExpectedModelNumber() {
        Document document = DocumentLoader.load("umart/asus_5070ti.html");
        Optional<ScrapedDataDTO> scrapedDataDTO = umartProductScraper
                .parseProductData(document, UMART_ASUS_5070TI, UMART_CSS_MODEL_LOCATION, UMART_CSS_PRICE_LOCATION);
        assert scrapedDataDTO.isPresent() && scrapedDataDTO.get().modelNumber().equals("PRIME-RTX5070TI-O16G");
    }

    @Test
    public void testThatScorptecGPUScraperReturnsExpectedModelNumber() {
        Document document = DocumentLoader.load("scorptec/asus_5070ti.html");
        Optional<ScrapedDataDTO> scrapedDataDTO = scorptecProductScraper
                .parseProductData(document, SCORPTEC_ASUS_5070TI, SCORPTEC_CSS_MODEL_LOCATION, SCORPTEC_CSS_PRICE_LOCATION);
        assert scrapedDataDTO.isPresent() && scrapedDataDTO.get().modelNumber().equals("PRIME-RTX5070TI-O16G");
    }

    @Test
    public void testThatUmartGPUScraperRemovesSemicolon() {
        String refinedModelNumber = umartProductScraper
                .refineModelNumber("Model Number : " + "PRIME-RTX5070TI-O16G");
        assert refinedModelNumber.equals("PRIME-RTX5070TI-O16G");
    }

    @Test
    public void testThatUmartGPUScraperRemovesSingleComma() {
        BigDecimal refinedPrice = umartProductScraper.refinePrice("1,380.00");
        assert refinedPrice.equals(new BigDecimal("1380.00"));
    }

    @Test
    public void testThatUmartGPUScraperRemovesMultipleCommas() {
        BigDecimal refinedPrice = umartProductScraper.refinePrice("1,380,000.00");
        assert refinedPrice.equals(new BigDecimal("1380000.00"));
    }
}