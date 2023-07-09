public class CrawlException extends RuntimeException {
    private String url;

    public CrawlException(String url, String message) {
        super(message);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
