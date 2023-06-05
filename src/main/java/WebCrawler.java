import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class WebCrawler {
    protected static int depth;
    protected static String sourceLanguage;
    protected static String targetLanguage;
    static FileWriter writer;
    protected static String baseUrl;

    public static void main(String[] args) throws Exception {
        // Get input from user
        String[] urls = getInputUrls();

        // Initialize writer
        writer = new FileWriter("output.md");

        // Process each URL sequentially
        for (String url : urls) {
            processUrl(url);
        }

        // Close writer
        writer.close();
    }

    // Process the given URL
    protected static void processUrl(String url) {
        try {
            System.out.println("Processing URL: " + url);
            writer.write("# " + url + "\n");

            crawl(url, 0);
        } catch (IOException e) {
            e.printStackTrace();
            writeError(url, e.getMessage());
        }
    }

    // Recursive method to crawl the web pages
    protected static void crawl(String url, int currentDepth) throws IOException {
        if (currentDepth > depth) {
            return;
        }

        // Fetch the web page content using Jsoup
        Document document = Jsoup.connect(url).get();
        String title = translate(document.title(), sourceLanguage, targetLanguage);

        // Write the title and URL to the output file
        writer.write(getIndent(currentDepth) + "# " + title + "\n");
        writer.write(getIndent(currentDepth) + "- " + url + "\n");

        Elements links = document.select("a[href]");

        for (Element link : links) {
            String href = link.attr("href");
            if (!href.startsWith(baseUrl)) {
                continue;
            }

            // Check the HTTP status code of each link
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(href).openConnection();
            httpURLConnection.setRequestMethod("HEAD");

            // Write the link to the output file or continue crawling
            int statusCode = httpURLConnection.getResponseCode();
            if (statusCode >= 400) {
                writer.write(getIndent(currentDepth + 1) + "- **" + href + "**" + "\n");
            } else {
                crawl(href, currentDepth + 1);
            }
        }
    }

    // Translate the given text using Apertium
    protected static String translate(String text, String sourceLang, String targetLang) throws IOException {
        return translateWithApertium(text, sourceLang, targetLang);
    }

    // Extract the base URL from the provided URL
    protected static String getBaseUrl(String url) {
        String[] parts = url.split("/");
        String baseUrl = parts[0] + "//" + parts[2];
        return baseUrl;
    }

    // Generate indentation string based on depth level
    protected static String getIndent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    // Translate the given text using Apertium web service
    protected static String translateWithApertium(String text, String sourceLang, String targetLang) throws IOException {
        String urlStr = "https://www.apertium.org/apy/translate?q=" + URLEncoder.encode(text, "UTF-8")
                + "&langpair=" + sourceLang + "|" + targetLang;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to retrieve translation: " + conn.getResponseCode());
        }

        // Read the response from the Apertium service
        Scanner scanner = new Scanner(conn.getInputStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNextLine()) {
            response.append(scanner.nextLine());
        }
        scanner.close();

        return response.toString();
    }

    // Get input URLs and other parameters from the user
    protected static String[] getInputUrls() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the URLs (separated by a comma): ");
        String urlInput = scanner.nextLine();
        String[] urls = urlInput.split(",");

        System.out.println("Enter the maximum depth: ");
        depth = Integer.parseInt(scanner.nextLine());

        System.out.println("Enter the source language code (e.g. 'en'): ");
        sourceLanguage = scanner.nextLine();

        System.out.println("Enter the target language code (e.g. 'es'): ");
        targetLanguage = scanner.nextLine();

        baseUrl = getBaseUrl(urls[0]);

        return urls;
    }

    // Write an error message to the output file
    protected static void writeError(String url, String error) {
        try {
            writer.write("# Error\n");
            writer.write("- URL: " + url + "\n");
            writer.write("- Message: " + error + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
