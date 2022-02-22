package server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpSearchServer implements AutoCloseable {
    private final HttpServer server;
    private final String host;
    private final int port;
    private final String name;

    public HttpSearchServer(String host, int port, String name) throws IOException {
        this.host = host;
        this.port = port;
        this.name = name;
        server = HttpServer.create();
        server.bind(new InetSocketAddress(host, port), 0);
        server.createContext("/search", new SearchQueryHandler(name));
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void start() {
        server.start();
    }

    @Override
    public void close() throws Exception {
        server.stop(0);
    }
}
