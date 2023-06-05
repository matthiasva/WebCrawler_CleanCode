import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;


public class WebCrawlerTest {
    protected WebCrawler webCrawler;

    @BeforeEach
    public void setup() {
        webCrawler = new WebCrawler();
    }

    @Test
    public void testTranslateWithApertium() throws IOException {
        String translation = webCrawler.translateWithApertium("Hello", "en", "es");
        int startIndex = translation.indexOf("\"translatedText\": \"") + 18;
        int endIndex = translation.indexOf("\"}, \"");
        String translatedText = translation.substring(startIndex+1, endIndex);

        assertEquals("Hola", translatedText);
    }

    @Test
    public void testTranslateWithApertium_InvalidLanguage() {
        assertThrows(IOException.class, () -> {
            webCrawler.translateWithApertium("Hello", "en", "invalid");
        });
    }

    @Test
    public void testGetBaseUrl() {
        String url = "https://www.example.com/page.html";
        String baseUrl = webCrawler.getBaseUrl(url);
        assertEquals("https://www.example.com", baseUrl);
    }

    @Test
    public void testGetInputUrls_InvalidInput() {
        String input = "invalid,url,input";

        System.setIn(new java.io.ByteArrayInputStream(input.getBytes()));
        assertThrows(NoSuchElementException.class, () -> {
            webCrawler.getInputUrls();
        });
    }

    @Test
    public void testGetIndent() {
        int depth = 3;
        String indent = webCrawler.getIndent(depth);
        assertEquals("      ", indent);
    }

    @Test
    public void testProcessUrl() throws IOException {
        File tempFile = File.createTempFile("output", ".md");
        tempFile.deleteOnExit();
        FileWriter writer = new FileWriter(tempFile);

        WebCrawler.writer = writer;

        String url = "https://www.example.com";
        WebCrawler.processUrl(url);

        writer.close();

        assertTrue(tempFile.length() > 0);
    }

    @Test
    public void testProcessUrl_Exception() {
        assertThrows(NullPointerException.class, () -> {
            webCrawler.processUrl("invalid-url");
        });
    }

    @Test
    public void testWriteError() throws IOException {
        File tempFile = File.createTempFile("output", ".md");
        tempFile.deleteOnExit();
        FileWriter writer = new FileWriter(tempFile);

        WebCrawler.writer = writer;

        String url = "https://www.example.com";
        String error = "Error message";
        WebCrawler.writeError(url, error);

        writer.close();

        String expectedContent = "# Error\n" +
                "- URL: " + url + "\n" +
                "- Message: " + error + "\n";
        assertEquals(expectedContent, tempFileContent(tempFile));
    }

    @Test
    public void testCrawl_InvalidUrl()  {
        assertThrows(IllegalArgumentException.class, () -> {
            webCrawler.crawl("invalid-url", 0);
        });
    }

    @Test
    public void testCrawl_StatusCodeError() throws IOException {
        assertThrows(IOException.class, () -> {
            webCrawler.crawl("https://www.example.com/error-page", 0);
        });
    }
    @Test
    public void testTranslate_SuccessfulTranslation() throws IOException {
        String text = "Hello";
        String sourceLang = "en";
        String targetLang = "es";
        String translation = webCrawler.translate(text, sourceLang, targetLang);
        int startIndex = translation.indexOf("\"translatedText\": \"") + 18;
        int endIndex = translation.indexOf("\"}, \"");
        String translatedText = translation.substring(startIndex+1, endIndex);

        assertEquals("Hola", translatedText);
    }

    @Test
    public void testTranslate_TranslationError() {
        String text = "Hello";
        String sourceLang = "en";
        String targetLang = "invalid";
        assertThrows(IOException.class, () -> {
            webCrawler.translate(text, sourceLang, targetLang);
        });
    }

    // Helper method to read the contents of a file
    private String tempFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            content.append(scanner.nextLine()).append("\n");
        }
        scanner.close();
        return content.toString();
    }


}
