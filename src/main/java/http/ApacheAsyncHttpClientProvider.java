package http;

import org.apache.http.impl.nio.client.HttpAsyncClients;

public class ApacheAsyncHttpClientProvider implements AsyncHttpClientProvider {
    @Override
    public AsyncHttpClient getInstance() {
        return new ApacheAsyncHttpClient(HttpAsyncClients.createDefault());
    }
}
