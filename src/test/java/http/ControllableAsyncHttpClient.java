package http;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class ControllableAsyncHttpClient implements AsyncHttpClient {
    private final Set<String> badHosts;

    public ControllableAsyncHttpClient(Set<String> badHosts) {
        this.badHosts = badHosts;
    }

    @Override
    public void start() {

    }

    @Override
    public Future<HttpResponse> run(HttpUriRequest request, FutureCallback<String> callback) {
        String host = request.getURI().getHost();
        if (badHosts.contains(host)) {
            callback.failed(new IllegalAccessException());
        } else {
            Gson gson = new Gson();
            String query = request.getURI().getQuery().substring(6);
            List<String> ans = List.of("Result for " + query + " from " + host);
            callback.completed(gson.toJson(ans));
        }
        return (Future<HttpResponse>) Mockito.mock(Future.class);
    }

    @Override
    public void close() {

    }
}
