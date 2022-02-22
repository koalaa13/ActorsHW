package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import config.AggregatorConfig;
import config.SearchConfig;
import http.AsyncHttpClientProvider;
import http.ControllableAsyncHttpClientProvider;
import messages.AggregateSearchQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scala.concurrent.duration.Duration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregateActorTest {
    private ActorSystem system;

    @BeforeEach
    public void initSystem() {
        system = ActorSystem.create("TestSystem");
    }

    @AfterEach
    public void stopSystem() {
        system.terminate();
        system = null;
    }

    private Map<String, List<String>> getReal(List<String> names, String query, AsyncHttpClientProvider provider) {
        CompletableFuture<Map<String, List<String>>> res = new CompletableFuture<>();
        Duration timeout = Duration.create(2, TimeUnit.SECONDS);
        AggregatorConfig aggregatorConfig = new AggregatorConfig(timeout);
        ActorRef aggregator = system.actorOf(AggregateActor.props(res, aggregatorConfig, provider));
        List<SearchConfig> configs = names.stream()
                .map(name -> new SearchConfig(name, name + "Host", 1337))
                .collect(Collectors.toList());
        AggregateSearchQuery searchQuery = new AggregateSearchQuery(query, configs);
        aggregator.tell(searchQuery, ActorRef.noSender());
        try {
            return res.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, List<String>> getExpected(List<String> names, String query, Set<String> badHosts) {
        return names.stream()
                .collect(Collectors.toMap(
                        name -> name,
                        name -> {
                            if (badHosts.contains(name + "Host")) {
                                return List.of("Error happened while searching.");
                            } else {
                                return List.of("Result for " + query + " from " + name + "Host");
                            }
                        }
                ));
    }

    @Test
    public void AllSuccessTest() {
        final List<String> names = List.of("a", "b", "c");
        final String query = "TestQuery";
        AsyncHttpClientProvider provider = new ControllableAsyncHttpClientProvider();
        Map<String, List<String>> real = getReal(names, query, provider);
        Map<String, List<String>> expected = getExpected(names, query, Collections.emptySet());
        assertEquals(expected, real);
    }

    @Test
    public void PartiallySuccessTest() {
        final List<String> names = List.of("a", "b", "c");
        final Set<String> badHosts = Set.of("aHost");
        final String query = "TestQuery";
        ControllableAsyncHttpClientProvider provider = new ControllableAsyncHttpClientProvider(badHosts);
        Map<String, List<String>> real = getReal(names, query, provider);
        Map<String, List<String>> expected = getExpected(names, query, badHosts);
        assertEquals(expected, real);
    }

    @Test
    public void AllFailTest() {
        final List<String> names = List.of("a", "b", "c");
        final Set<String> badHosts = names.stream().map(name -> name + "Host").collect(Collectors.toSet());
        final String query = "TestQuery";
        ControllableAsyncHttpClientProvider provider = new ControllableAsyncHttpClientProvider(badHosts);
        Map<String, List<String>> real = getReal(names, query, provider);
        Map<String, List<String>> expected = getExpected(names, query, badHosts);
        assertEquals(expected, real);
    }
}
