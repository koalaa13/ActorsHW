package http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;

import java.util.concurrent.Future;

public interface AsyncHttpClient extends AutoCloseable {
    void start();

    Future<HttpResponse> run(HttpUriRequest request, FutureCallback<String> callback);
}
