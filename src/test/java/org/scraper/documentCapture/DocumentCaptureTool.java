package org.scraper.documentCapture;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static org.scraper.testingData.vendorData.VendorWebDomainNames.*;

/**
 * Not part of the automated test suite - hits live vendor URLs.
 * Run manually by removing the @Disabled tag to recapture the static HTML documents under
 * src/test/resources/scraperDocuments/ whenever a vendor changes their page markup.
 */
@Disabled("Manual fixture capture tool - runs on demand only, not part of the automated suite")
public class DocumentCaptureTool {

    private static final Path FIXTURE_ROOT = Path.of("src/test/resources/scraper_fixtures");
    private static final String CHROME_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36";

    @Test
    public void captureAllFixtures() throws IOException {
        Map<String, String> umartFixtures = Map.of(
                "ryzen_9_9600x.html", UMART_RYZEN_9_9600X,
                "asus_5070ti.html", UMART_ASUS_5070TI,
                "kingston_f64g.html", UMART_KINGSTON_KINGSTON_F64G,
                "rtx_pro_6000.html", UMART_RTX_PRO_6000,
                "seagate_st2000dm005.html", UMART_SEAGATE_ST2000DM005,
                "crucial_bx500_1tb.html", UMART_CRUCIAL_BX500_1TB,
                "crucial_p510_1tb.html", UMART_CRUCIAL_P510_1TB
        );
        for (var entry : umartFixtures.entrySet()) {
            captureFixture(entry.getValue(), "https://www.umart.com.au/", "scraperDocuments/umart", entry.getKey());
        }

        Map<String, String> scorptecFixtures = Map.of(
                "ryzen_5_9600x.html", SCORPTEC_RYZEN_5_9600X,
                "asus_5070ti.html", SCORPTEC_ASUS_5070TI
        );
        for (var entry : scorptecFixtures.entrySet()) {
            captureFixture(entry.getValue(), "https://www.scorptec.com.au/", "scraperDocuments/scorptec", entry.getKey());
        }
    }

    private void captureFixture(String url, String referrer, String vendorDir, String fileName) throws IOException {
        Document document = Jsoup.connect(url)
                .userAgent(CHROME_USER_AGENT)
                .header("Accept-Language", "en-AU,en;q=0.9")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .referrer(referrer)
                .timeout(15000)
                .get();

        Path targetDir = FIXTURE_ROOT.resolve(vendorDir);
        Files.createDirectories(targetDir);
        Files.writeString(targetDir.resolve(fileName), document.outerHtml());
    }
}