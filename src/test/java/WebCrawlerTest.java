import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebCrawlerTest {
    private static final String TEST_URL = "http://example.com";
    private static final String SOURCE_LANGUAGE = "en";
    private static final String TARGET_LANGUAGE = "es";
    private static final int MAX_DEPTH = 2;

    private WebCrawler webCrawler;

    @Mock
    private FileWriter writer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        webCrawler = new WebCrawler();
        webCrawler.writer = writer;
    }

    @Test
    public void testProcessUrl_ValidUrl_Success() {
        String TEST_URL = "https://example.com";
        CrawlResult crawlResult = webCrawler.processUrl(TEST_URL);
        Assertions.assertNotNull(crawlResult);
        assertEquals(TEST_URL, crawlResult.getUrl());
        Assertions.assertFalse(crawlResult.getTitle().isEmpty());
        Assertions.assertNotNull(crawlResult.getChildResults());
    }

    @Test
    public void testProcessUrl_InvalidUrl_ReturnsErrorResult() {
        String invalidUrl = "http://invalidurl";
        CrawlResult crawlResult = webCrawler.processUrl(invalidUrl);
        Assertions.assertNotNull(crawlResult);
        assertEquals(invalidUrl, crawlResult.getUrl());
        assertEquals("invalidurl", crawlResult.getTitle());
        assertTrue(crawlResult.getChildResults().isEmpty());
    }


    @Test
    public void testTranslate_ValidText_Success() throws IOException {
        String text = "Hello";
        String translatedText = webCrawler.translate(text, SOURCE_LANGUAGE, TARGET_LANGUAGE);
        Assertions.assertNotNull(translatedText);
    }

    @Test
    public void testTranslate_InvalidText_ReturnsEmptyString() throws IOException {
        String invalidText = "";
        String translatedText = webCrawler.translate(invalidText, SOURCE_LANGUAGE, TARGET_LANGUAGE);
        assertEquals("", translatedText);
    }

    @Test
    public void testGetBaseUrl_ValidUrl_Success() {
        String baseUrl = webCrawler.getBaseUrl(TEST_URL);
        Assertions.assertNotNull(baseUrl);
    }

    @Test
    public void testGetIndent_PositiveDepth_Success() {
        int depth = 3;
        String indent = webCrawler.getIndent(depth);
        Assertions.assertNotNull(indent);
        assertEquals("      ", indent);
    }


    @Test
    public void testWriteError_Success() throws IOException {
        String url = "http://example.com";
        String error = "Connection error";

        webCrawler.writeError(url, error);

        Mockito.verify(writer).write("# Error\n");
        Mockito.verify(writer).write("- URL: " + url + "\n");
        Mockito.verify(writer).write("- Message: " + error + "\n");
    }

    @Test
    public void testWriteError_ExceptionThrown() throws IOException {
        String url = "http://example.com";
        String error = "Connection error";
        Mockito.doThrow(new IOException()).when(writer).write(Mockito.anyString());

        Assertions.assertThrows(IOException.class, () -> webCrawler.writeError(url, error));
    }

    @Test
    public void testCrawlResult_GetReport_NoChildResults() {
        String url = "http://example.com";
        String title = "Example Domain";
        List<CrawlResult> childResults = new ArrayList<>();

        CrawlResult crawlResult = new CrawlResult(url, title, childResults);
        String report = crawlResult.getReport();

        assertTrue(report.contains(url));
        assertTrue(report.contains(title));
        Assertions.assertFalse(report.contains("Child Results:"));
    }

    @Test
    public void testCrawlResult_GetReport_WithChildResults() {
        String url = "http://example.com";
        String title = "Example Domain";
        List<CrawlResult> childResults = new ArrayList<>();
        childResults.add(new CrawlResult("http://example.com/page1", "Page 1"));
        childResults.add(new CrawlResult("http://example.com/page2", "Page 2"));

        CrawlResult crawlResult = new CrawlResult(url, title, childResults);
        String report = crawlResult.getReport();

        assertTrue(report.contains(url));
        assertTrue(report.contains(title));
        assertTrue(report.contains("Child Results:"));
        assertTrue(report.contains("http://example.com/page1"));
        assertTrue(report.contains("Page 1"));
        assertTrue(report.contains("http://example.com/page2"));
        assertTrue(report.contains("Page 2"));
    }

    @Test
    public void testConcurrency() throws InterruptedException {
        WebCrawler webCrawler = new WebCrawler();

        String[] urls = {"https://example.com/url1", "https://example.com/url2", "https://example.com/url3"};

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (String url : urls) {
            executorService.execute(() -> {
                try {
                    webCrawler.processUrl(url);
                } catch (CrawlException e) {
                    webCrawler.writeError(e.getUrl(), e.getMessage());
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}




