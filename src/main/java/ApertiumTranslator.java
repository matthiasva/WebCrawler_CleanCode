import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class ApertiumTranslator {
    public static String translateWithApertium(String text, String sourceLang, String targetLang) throws IOException {
        String urlStr = "https://www.apertium.org/apy/translate?q=" + URLEncoder.encode(text, "UTF-8")
                + "&langpair=" + sourceLang + "|" + targetLang;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to retrieve translation: " + conn.getResponseCode());
        }

        // Read the response from the Apertium service
        try (Scanner scanner = new Scanner(conn.getInputStream())) {
            StringBuilder response = new StringBuilder();
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
            return response.toString();
        }
    }
}
