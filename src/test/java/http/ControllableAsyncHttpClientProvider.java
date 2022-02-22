package http;

import java.util.Collections;
import java.util.Set;

public class ControllableAsyncHttpClientProvider implements AsyncHttpClientProvider {
    private final Set<String> badHosts;

    public ControllableAsyncHttpClientProvider() {
        this.badHosts = Collections.emptySet();
    }

    public ControllableAsyncHttpClientProvider(Set<String> badHosts) {
        this.badHosts = badHosts;
    }

    @Override
    public ControllableAsyncHttpClient getInstance() {
        return new ControllableAsyncHttpClient(badHosts);
    }
}
