import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class WebCrawlerTest {

    @Test
    public void testTranslateWithApertium() {
        String text = "hello world";
        String sourceLang = "en";
        String targetLang = "es";
        String translation = WebCrawler.translateWithApertium(text, sourceLang, targetLang);
        assertNotNull(translation);
        assertNotEquals(text, translation);
    }

    @Test
    void testCrawl() {
        String url = "https://www.example.com";
        int depth = 1;

        assertDoesNotThrow(() -> {
            WebCrawler.crawl(url, depth);
        });
    }

    @Test
    void testCrawlWithValidURL() {
        assertDoesNotThrow(() -> WebCrawler.crawl("https://en.wikipedia.org/wiki/Java_(programming_language)", 1));
    }

    @Test
    void testCrawlWithInvalidURL() {
        assertThrows(Exception.class,() -> WebCrawler.crawl("https://www.thisurlshouldnotwork.com", 0));
    }

    @Test
    void testCrawlWithNullURL() {
        assertThrows(Exception.class, () -> WebCrawler.crawl(null, 0));
    }

    @Test
    void testCrawlWithNegativeDepth() {
        assertThrows(Exception.class,() -> WebCrawler.crawl("https://en.wikipedia.org/wiki/Java_(programming_language)", -1));
    }

    @Test
    void testCrawlWithZeroDepth() {
        assertThrows(Exception.class, () -> WebCrawler.crawl("https://en.wikipedia.org/wiki/Java_(programming_language)", 0));
    }

    @Test
    void testCrawlWithPositiveDepth() {
        assertDoesNotThrow(() -> WebCrawler.crawl("https://en.wikipedia.org/wiki/Java_(programming_language)", 2));
    }

    @Test
    public void testGetBaseUrl() {
        String url = "https://www.example.com/page1";
        String baseUrl = WebCrawler.getBaseUrl(url);
        assertEquals("https://www.example.com", baseUrl);
    }

    @Test
    public void testGetIndent() {
        assertEquals("  ", WebCrawler.getIndent(1));
        assertEquals("    ", WebCrawler.getIndent(2));
        assertEquals("      ", WebCrawler.getIndent(3));
    }
}
