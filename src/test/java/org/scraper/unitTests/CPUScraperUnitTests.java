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
import static org.scraper.testingData.vendorData.VendorWebDomainNames.SCORPTEC_RYZEN_5_9600X;
import static org.scraper.testingData.vendorData.VendorWebDomainNames.UMART_RYZEN_9_9600X;
import static org.scraper.webscraper.constants.VendorCSSLocations.*;

public class CPUScraperUnitTests {

    private final PricePointObserver pricePointObserver = new PricePointObserver();
    private final UmartProductScraper umartProductScraper = new UmartProductScraper(pricePointObserver);
    private final ScorptecProductScraper scorptecProductScraper = new ScorptecProductScraper(pricePointObserver);

    @Test
    public void testThatUmartCPUScrapingServiceReturnsExpectedModelNumber() {
        Document document = DocumentLoader.load("umart/ryzen_9_9600x.html");
        Optional<ScrapedDataDTO> scrapedDataDTO = umartProductScraper
                .parseProductData(document, UMART_RYZEN_9_9600X, UMART_CSS_MODEL_LOCATION, UMART_CSS_PRICE_LOCATION);

        assert scrapedDataDTO.isPresent() && scrapedDataDTO.get().modelNumber().equals("100-100001405WOF");
    }

    @Test
    public void testThatScorptecCPUScrapingServiceReturnsExpectedModelNumber() {
        Document document = DocumentLoader.load("scorptec/ryzen_5_9600x.html");
        Optional<ScrapedDataDTO> scrapedDataDTO = scorptecProductScraper
                .parseProductData(document, SCORPTEC_RYZEN_5_9600X, SCORPTEC_CSS_MODEL_LOCATION, SCORPTEC_CSS_PRICE_LOCATION);

        assert scrapedDataDTO.isPresent() && scrapedDataDTO.get().modelNumber().equals("100-100001405WOF");
    }

    @Test
    public void testThatUmartCPUScrapingServiceRemovesSemicolon() {
        String refinedModelNumber = umartProductScraper
                .refineModelNumber("Model Number : 100-100001405WOF");
        assert refinedModelNumber.equals("100-100001405WOF");
    }

    @Test
    public void testThatUmartCPUScrapingServiceRemovesSingleComma() {
        BigDecimal refinedPrice = umartProductScraper.refinePrice("1,444.00");
        assert refinedPrice.equals(new BigDecimal("1444.00"));
    }

    @Test
    public void testThatUmartCPUScrapingServiceRemovesMultipleCommas() {
        BigDecimal refinedPrice = umartProductScraper.refinePrice("1,240,000.00");
        assert  refinedPrice.equals(new BigDecimal("1240000.00"));
    }
}