package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SearchQueryHandler implements HttpHandler {
    private final String name;

    private static final byte[] ERROR_MESSAGE = "Bad query.".getBytes(StandardCharsets.UTF_8);
    private static final String GOOD_PATTERN = "query=";
    private static final Set<String> BAN = Set.of("bad", "Dmozze", "Nigger");
    private static final Set<String> SLOW = Set.of("slow", "nooshe");

    public SearchQueryHandler(String name) {
        this.name = name;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (!query.startsWith(GOOD_PATTERN)) {
            sendError(exchange);
        } else {
            String queryStr = query.substring(GOOD_PATTERN.length());
            if (BAN.contains(queryStr)) {
                sendError(exchange);
            } else {
                if (SLOW.contains(queryStr)) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {
                    }
                }
                Gson gson = new Gson();
                List<String> response = IntStream.range(1, 6)
                        .mapToObj(i -> "host" + i + ".ru/" + queryStr + "/from_" + name)
                        .collect(Collectors.toList());
                byte[] answer = gson.toJson(response).getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, answer.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(answer);
                }
            }
        }
    }

    private void sendError(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(400, ERROR_MESSAGE.length);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(ERROR_MESSAGE);
        }
    }
}
