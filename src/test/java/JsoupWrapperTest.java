import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class JsoupWrapperTest {

    private WebCrawler.JsoupWrapper jsoupWrapper;

    @BeforeEach
    public void setup() {
        jsoupWrapper = new WebCrawler.JsoupWrapper();
    }

    @Test
    public void testConnect_Success() throws IOException {
        String url = "https://example.com";

        Document document = jsoupWrapper.connect(url);

        Assertions.assertNotNull(document);
        Assertions.assertTrue(document.baseUri().contains(url));
    }

    @Test
    public void testConnect_IOException() {
        String url = "https://nonexistent-url.com";

        Assertions.assertThrows(IOException.class, () -> jsoupWrapper.connect(url));
    }
}
