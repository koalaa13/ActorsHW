package messages;

import config.SearchConfig;

public class SearchQuery {
    private final String text;
    private final SearchConfig config;

    public SearchQuery(String text, SearchConfig config) {
        this.text = text;
        this.config = config;
    }

    public String getText() {
        return text;
    }

    public SearchConfig getConfig() {
        return config;
    }
}
