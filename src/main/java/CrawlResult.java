import java.util.ArrayList;
import java.util.List;

public class CrawlResult {
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
