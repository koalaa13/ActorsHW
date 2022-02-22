package http;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.Future;

public class ApacheAsyncHttpClient implements AsyncHttpClient {
    private static class RawResponseCallback implements FutureCallback<HttpResponse> {
        private final FutureCallback<String> responseCallback;

        private RawResponseCallback(FutureCallback<String> responseCallback) {
            this.responseCallback = responseCallback;
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                String stringResponse = EntityUtils.toString(result.getEntity());
                responseCallback.completed(stringResponse);
            } catch (IOException e) {
                responseCallback.failed(e);
            }
        }

        @Override
        public void failed(Exception ex) {
            responseCallback.failed(ex);
        }

        @Override
        public void cancelled() {
            responseCallback.cancelled();
        }
    }

    private final CloseableHttpAsyncClient client;

    public ApacheAsyncHttpClient(CloseableHttpAsyncClient client) {
        this.client = client;
    }

    @Override
    public void start() {
        client.start();
    }

    @Override
    public Future<HttpResponse> run(HttpUriRequest request, FutureCallback<String> callback) {
        return client.execute(request, new RawResponseCallback(callback));
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
