package messages;

import config.SearchConfig;

import java.util.List;

public class AggregateSearchQuery {
    private final String text;
    private final List<SearchConfig> searchConfigs;

    public AggregateSearchQuery(String text, List<SearchConfig> searchConfigs) {
        this.text = text;
        this.searchConfigs = searchConfigs;
    }

    public String getText() {
        return text;
    }

    public List<SearchConfig> getSearchConfigs() {
        return searchConfigs;
    }
}
