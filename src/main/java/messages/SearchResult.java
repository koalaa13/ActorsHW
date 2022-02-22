package messages;

import java.util.List;

public class SearchResult {
    private final String engineName;
    private final List<String> urls;

    public SearchResult(String engineName, List<String> urls) {
        this.engineName = engineName;
        this.urls = urls;
    }

    public String getEngineName() {
        return engineName;
    }

    public List<String> getUrls() {
        return urls;
    }
}
