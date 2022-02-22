package server;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import config.ConfigUtils;
import config.SearchConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ServersStarter {
    public static void main(String[] args) {
        File configFile = Paths.get("src/main/resources/search-engines.conf").toFile();
        Config config = ConfigFactory.parseFile(configFile);
        List<SearchConfig> searchConfigs = ConfigUtils.getSearchEnginesConfigs(config);
        List<HttpSearchServer> servers = new ArrayList<>();
        for (SearchConfig searchConfig : searchConfigs) {
            try {
                HttpSearchServer server = new HttpSearchServer(searchConfig.getHost(), searchConfig.getPort(), searchConfig.getName());
                servers.add(server);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                return;
            }
        }
        try {
            servers.forEach(server -> {
                server.start();
                System.out.println("Server " + server.getName() + " on host " + server.getHost() + " on port " + server.getPort() + " launched.");
            });
            System.out.println("All servers are ready");
            while (true) {
            }
        } finally {
            for (HttpSearchServer server : servers) {
                try {
                    server.close();
                    System.out.println("Server " + server.getName() + " on host " + server.getHost() + " on port " + server.getPort() + " shut down.");
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}
