import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

public class ApertiumTranslatorTest {

    private WebCrawler.ApertiumTranslator apertiumTranslator;

    @BeforeEach
    public void setup() {
        apertiumTranslator = new WebCrawler.ApertiumTranslator();
    }

    @Test
    public void testTranslate_Success() throws IOException {
        String text = "Hello";
        String sourceLang = "en";
        String targetLang = "es";

        String translatedText = apertiumTranslator.translateWithApertium(text, sourceLang, targetLang);

        Assertions.assertNotNull(translatedText);
        Assertions.assertNotEquals(text, translatedText);
    }

    @Test
    public void testTranslate_IOException() {
        String text = "Hello";
        String sourceLang = "en";
        String targetLang = "es";

        Assertions.assertThrows(IOException.class, () -> apertiumTranslator.translateWithApertium(text, sourceLang, targetLang));
    }
}
