import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebCrawler {
    private static final int THREAD_POOL_SIZE = 10;
    private static int depth;
    private static String sourceLanguage;
    private static String targetLanguage;
    protected static FileWriter writer;
    private static String baseUrl;
    protected static JsoupWrapper jsoupWrapper;

    public WebCrawler() {
        jsoupWrapper = new JsoupWrapper();
    }

    public static void main(String[] args) {
        WebCrawler webCrawler = new WebCrawler();
        try {
            webCrawler.start();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException, InterruptedException {
        // Get input from user
        String[] urls = getInputUrls();

        // Initialize writer
        writer = new FileWriter("output.md");

        // Create a list to store crawl results
        List<CrawlResult> crawlResults = new ArrayList<>();

        // Initialize executor service
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Process each URL concurrently
        for (String url : urls) {
            executorService.execute(() -> {
                try {
                    CrawlResult crawlResult = processUrl(url);
                    synchronized (crawlResults) {
                        crawlResults.add(crawlResult);
                    }
                } catch (CrawlException e) {
                    writeError(url, e.getMessage());
                }
            });
        }

        // Shutdown executor service
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        // Combine the results for each website in a single report
        synchronized (crawlResults) {
            for (CrawlResult crawlResult : crawlResults) {
                writer.write(crawlResult.getReport());
            }
        }

        // Close writer
        writer.close();
    }

    // Process the given URL
    protected CrawlResult processUrl(String url) throws CrawlException {
        try {
            System.out.println("Processing URL: " + url);
            writer.write("# " + url + "\n");

            CrawlResult crawlResult = crawl(url, 0);
            writer.write(crawlResult.getReport());
            return crawlResult;
        } catch (IOException e) {
            throw new CrawlException(url, e.getMessage());
        }
    }

    // Recursive method to crawl the web pages
    protected CrawlResult crawl(String url, int currentDepth) throws IOException, CrawlException {
        if (currentDepth > depth) {
            return new CrawlResult(url, "");
        }

        // Fetch the web page content using Jsoup
        Document document = jsoupWrapper.connect(url);
        String title = translate(document.title(), sourceLanguage, targetLanguage);

        // Write the title and URL to the output file
        writer.write(getIndent(currentDepth) + "# " + title + "\n");
        writer.write(getIndent(currentDepth) + "- " + url + "\n");

        Elements links = document.select("a[href]");

        List<CrawlResult> childResults = new ArrayList<>();

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
                CrawlResult childResult = crawl(href, currentDepth + 1);
                childResults.add(childResult);
            }
        }

        return new CrawlResult(url, title, childResults);
    }

    // Translate the given text using Apertium
    protected String translate(String text, String sourceLang, String targetLang) throws IOException {
        return ApertiumTranslator.translateWithApertium(text, sourceLang, targetLang);
    }

    // Extract the base URL from the provided URL
    protected String getBaseUrl(String url) {
        String[] parts = url.split("/");
        String baseUrl = parts[0] + "//" + parts[2];
        return baseUrl;
    }

    // Generate indentation string based on depth level
    protected String getIndent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    // Get input URLs and other parameters from the user
    protected String[] getInputUrls() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the URLs (separated by a comma): ");
        String urlInput = scanner.nextLine();
        String[] urls = urlInput.split(",");

        System.out.println("Enter the maximum depth: ");
        depth = Integer.parseInt(scanner.nextLine());

        System.out.println("Enter the source language code (e.g., 'en'): ");
        sourceLanguage = scanner.nextLine();

        System.out.println("Enter the target language code (e.g., 'es'): ");
        targetLanguage = scanner.nextLine();

        baseUrl = getBaseUrl(urls[0]);

        return urls;
    }

    // Write an error message to the output file
    protected void writeError(String url, String error) {
        try {
            writer.write("# Error\n");
            writer.write("- URL: " + url + "\n");
            writer.write("- Message: " + error + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class JsoupWrapper {
        public Document connect(String url) throws IOException {
            return org.jsoup.Jsoup.connect(url).get();
        }
    }

    static class ApertiumTranslator {
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

    static class CrawlResult {
        private String url;
        private String title;
        private List<CrawlResult> childResults;

        public CrawlResult(String url, String title) {
            this.url = url;
            this.title = title;
            this.childResults = new ArrayList<>();
        }

        public CrawlResult(String url, String title, List<CrawlResult> childResults) {
            this.url = url;
            this.title = title;
            this.childResults = childResults;
        }

        public String getUrl() {
            return url;
        }

        public String getTitle() {
            return title;
        }

        public List<CrawlResult> getChildResults() {
            return childResults;
        }

        public String getReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("# ").append(url).append("\n");

            if (!title.isEmpty()) {
                sb.append("- ").append(title).append("\n");
            }

            for (CrawlResult childResult : childResults) {
                sb.append(childResult.getReport());
            }

            return sb.toString();
        }
    }

    static class CrawlException extends RuntimeException {
        private String url;

        public CrawlException(String url, String message) {
            super(message);
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }
}
