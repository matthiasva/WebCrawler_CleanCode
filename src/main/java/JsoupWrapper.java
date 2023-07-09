import org.jsoup.nodes.Document;

import java.io.IOException;

public class JsoupWrapper {
    public Document connect(String url) throws IOException {
        return org.jsoup.Jsoup.connect(url).get();
    }
}
