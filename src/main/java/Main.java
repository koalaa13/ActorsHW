import actors.AggregateActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import config.AggregatorConfig;
import config.ConfigUtils;
import config.SearchConfig;
import http.ApacheAsyncHttpClientProvider;
import http.AsyncHttpClientProvider;
import messages.AggregateSearchQuery;
import scala.concurrent.duration.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) {
        File configFile = Paths.get("src/main/resources/application.conf").toFile();
        Config appConfig = ConfigFactory.parseFile(configFile);
        AggregatorConfig aggregatorConfig = new AggregatorConfig(Duration.fromNanos(appConfig.getConfig("aggregator").getDuration("timeout").toNanos()));
        List<SearchConfig> searchEnginesConfigs = ConfigUtils.getSearchEnginesConfigs(appConfig.getConfig("searchEngines"));
        AsyncHttpClientProvider clientProvider = new ApacheAsyncHttpClientProvider();
        ActorSystem system = ActorSystem.create("AggregateSystem");
        System.out.println("Type your requests");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String query = reader.readLine();
                CompletableFuture<Map<String, List<String>>> result = new CompletableFuture<>();
                ActorRef aggregator = system.actorOf(AggregateActor.props(result, aggregatorConfig, clientProvider));
                aggregator.tell(new AggregateSearchQuery(query, searchEnginesConfigs), ActorRef.noSender());
                Map<String, List<String>> readyResult = result.get();
                System.out.println(gson.toJson(readyResult));
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println(e.getMessage());
        } finally {
            system.terminate();
        }
    }
}
