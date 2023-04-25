import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class WebCrawler {
    private static int depth;
    private static String sourceLanguage;
    private static String targetLanguage;
    private static FileWriter writer;
    private static String baseUrl;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter URL: ");
        String url = scanner.nextLine();
        System.out.println("Enter depth: ");
        depth = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter source Language");
        sourceLanguage = scanner.nextLine();
        System.out.println("Enter target language: ");
        targetLanguage = scanner.nextLine();
        baseUrl = getBaseUrl(url);
        writer = new FileWriter("output.md");
        crawl(url, 0);
        writer.close();
    }

    protected static void crawl(String url, int currentDepth) throws Exception {
        if (currentDepth > depth) {
            return;
        }

        Document document = Jsoup.connect(url).get();
        String title = translate(document.title(), sourceLanguage,targetLanguage);
        writer.write(getIndent(currentDepth) + "# " + title + "\n");
        writer.write(getIndent(currentDepth) + "- " + url + "\n");

        Elements links = document.select("a[href]");
        for (Element link : links) {
            String href = link.attr("href");
            if (!href.startsWith(baseUrl)) {
                continue;
            }
            try {
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(href).openConnection();
                httpURLConnection.setRequestMethod("HEAD");
                int statusCode = httpURLConnection.getResponseCode();
                if (statusCode >= 400) {
                    writer.write(getIndent(currentDepth + 1) + "- **" + href + "**" + "\n");
                } else {
                    crawl(href, currentDepth + 1);
                }
            } catch (IOException e) {
                writer.write(getIndent(currentDepth + 1) + "- **" + href + "**" + "\n");
            }
        }
    }

    protected static String translate(String text, String sourceLang, String targetLang) throws IOException {
        return translateWithApertium(text,sourceLang, targetLang);
    }

    protected static String getBaseUrl(String url) {
        String[] parts = url.split("/");
        String baseUrl = parts[0] + "//" + parts[2];
        return baseUrl;
    }

    protected static String getIndent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    protected static String translateWithApertium(String text, String sourceLang, String targetLang) {
        try {
            String urlStr = "https://www.apertium.org/apy/translate?q=" + URLEncoder.encode(text, "UTF-8")
                    + "&langpair=" + sourceLang + "|" + targetLang;
            URL url = new URL(urlStr);
            System.out.println(url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder resultString = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                resultString.append(line);
            }

            bufferedReader.close();

            return resultString.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
