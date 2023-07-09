import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CrawlResultTest {
    @Test
    public void testGetReport() {
        String testUrl = "https://example.com";
        String testTitle = "Example";
        WebCrawler.CrawlResult testChildResult = new WebCrawler.CrawlResult("https://example.com/page1", "Child Page");
        List<WebCrawler.CrawlResult> childResults = List.of(testChildResult);
        WebCrawler.CrawlResult crawlResult = new WebCrawler.CrawlResult(testUrl, testTitle, childResults);
        String expectedReport = "# " + testUrl + "\n" +
                "- " + testTitle + "\n" +
                "# " + testChildResult.getUrl() + "\n" +
                "- " + testChildResult.getTitle() + "\n";

        String actualReport = crawlResult.getReport();

        assertEquals(expectedReport, actualReport);
    }

    @Test
    public void testGetters() {
        String url = "https://example.com";
        String title = "Example Domain";
        List<WebCrawler.CrawlResult> childResults = new ArrayList<>();

        WebCrawler.CrawlResult crawlResult = new WebCrawler.CrawlResult(url, title, childResults);

        Assertions.assertEquals(url, crawlResult.getUrl());
        Assertions.assertEquals(title, crawlResult.getTitle());
        Assertions.assertEquals(childResults, crawlResult.getChildResults());
    }

    @Test
    public void testGetReport_NoChildResults() {
        String url = "https://example.com";
        String title = "Example Domain";
        List<WebCrawler.CrawlResult> childResults = new ArrayList<>();

        WebCrawler.CrawlResult crawlResult = new WebCrawler.CrawlResult(url, title, childResults);
        String report = crawlResult.getReport();

        Assertions.assertTrue(report.contains(url));
        Assertions.assertTrue(report.contains(title));
        Assertions.assertFalse(report.contains("Child Results:"));
    }

    @Test
    public void testCrawlResult_GetReport_EmptyTitle() {
        String url = "http://example.com";
        String title = "";
        List<WebCrawler.CrawlResult> childResults = new ArrayList<>();

        WebCrawler.CrawlResult crawlResult = new WebCrawler.CrawlResult(url, title, childResults);
        String report = crawlResult.getReport();

        Assertions.assertTrue(report.contains(url));
        Assertions.assertFalse(report.contains(title));
        Assertions.assertFalse(report.contains("Child Results:"));
    }

    @Test
    public void testCrawlResult_GetReport_WithChildResultsRecursive() {
        String url = "http://example.com";
        String title = "Example Domain";
        List<WebCrawler.CrawlResult> childResults = new ArrayList<>();
        List<WebCrawler.CrawlResult> grandchildResults = new ArrayList<>();
        grandchildResults.add(new WebCrawler.CrawlResult("http://example.com/page3", "Page 3"));
        grandchildResults.add(new WebCrawler.CrawlResult("http://example.com/page4", "Page 4"));
        childResults.add(new WebCrawler.CrawlResult("http://example.com/page1", "Page 1", grandchildResults));
        childResults.add(new WebCrawler.CrawlResult("http://example.com/page2", "Page 2"));

        WebCrawler.CrawlResult crawlResult = new WebCrawler.CrawlResult(url, title, childResults);
        String report = crawlResult.getReport();

        Assertions.assertTrue(report.contains(url));
        Assertions.assertTrue(report.contains(title));
        Assertions.assertTrue(report.contains("Child Results:"));
        Assertions.assertTrue(report.contains("http://example.com/page1"));
        Assertions.assertTrue(report.contains("Page 1"));
        Assertions.assertTrue(report.contains("http://example.com/page2"));
        Assertions.assertTrue(report.contains("Page 2"));
        Assertions.assertTrue(report.contains("http://example.com/page3"));
        Assertions.assertTrue(report.contains("Page 3"));
        Assertions.assertTrue(report.contains("http://example.com/page4"));
        Assertions.assertTrue(report.contains("Page 4"));
    }
}
