package org.scraper.unitTests;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.scraper.documentCapture.DocumentLoader;
import org.scraper.webscraper.dtos.ScrapedDataDTO;
import org.scraper.webscraper.logging.PricePointObserver;
import org.scraper.webscraper.vendorTemplates.UmartProductScraper;
import java.math.BigDecimal;
import java.util.Optional;
import static org.scraper.testingData.vendorData.VendorWebDomainNames.UMART_RTX_PRO_6000;
import static org.scraper.webscraper.constants.VendorCSSLocations.UMART_CSS_MODEL_LOCATION;
import static org.scraper.webscraper.constants.VendorCSSLocations.UMART_CSS_PRICE_LOCATION;

public class GPUWorkstationScraperUnitTests {

    private final UmartProductScraper umartProductScraper = new UmartProductScraper(new PricePointObserver());

    @Test
    public void testThatUmartWSGPUScraperReturnsExpectedModelNumber() {
        Document document = DocumentLoader.load("umart/rtx_pro_6000.html");
        Optional<ScrapedDataDTO> scrapedDataDTO = umartProductScraper
                .parseProductData(document, UMART_RTX_PRO_6000, UMART_CSS_MODEL_LOCATION, UMART_CSS_PRICE_LOCATION);
        assert scrapedDataDTO.isPresent() && scrapedDataDTO.get().modelNumber().equals("900-5G144-2500-000");
    }

    @Test
    public void testThatUmartWSGPUScraperRemovesSemicolon() {
        String refinedModelNumber = umartProductScraper.refineModelNumber("Model Number : " + "900-5G144-2500-000");
        assert refinedModelNumber.equals("900-5G144-2500-000");
    }

    @Test
    public void testThatUmartWSGPUScraperRemovesSingleComma() {
        BigDecimal refinedPrice = umartProductScraper.refinePrice("9,990.00");
        assert refinedPrice.equals(new BigDecimal("9990.00"));
    }

    @Test
    public void testThatUmartWSGPUScraperRemovesMultipleCommas() {
        BigDecimal refinedPrice = umartProductScraper.refinePrice("1,099,000.00");
        assert refinedPrice.equals(new BigDecimal("1099000.00"));
    }
}