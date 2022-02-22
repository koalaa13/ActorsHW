package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.japi.pf.ReceiveBuilder;
import config.AggregatorConfig;
import config.SearchConfig;
import http.AsyncHttpClientProvider;
import messages.AggregateSearchQuery;
import messages.SearchQuery;
import messages.SearchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AggregateActor extends AbstractActor {
    private final CompletableFuture<Map<String, List<String>>> resultConsumer;
    private final AggregatorConfig config;
    private final AsyncHttpClientProvider provider;
    private int awaitingAnswers;
    private final Map<String, List<String>> result;

    public static Props props(CompletableFuture<Map<String, List<String>>> resultConsumer, AggregatorConfig config,
                              AsyncHttpClientProvider clientProvider) {
        return Props.create(AggregateActor.class, () -> new AggregateActor(resultConsumer, config, clientProvider));
    }

    private AggregateActor(CompletableFuture<Map<String, List<String>>> resultConsumer, AggregatorConfig config,
                           AsyncHttpClientProvider provider) {
        this.resultConsumer = resultConsumer;
        this.config = config;
        this.provider = provider;
        this.awaitingAnswers = 0;
        this.result = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return new ReceiveBuilder()
                .match(AggregateSearchQuery.class, this::onAggregateSearchQuery)
                .match(ReceiveTimeout.class, this::onReceiveTimeout)
                .match(SearchResult.class, this::onSearchResult)
                .build();
    }

    private void onAggregateSearchQuery(AggregateSearchQuery query) {
        for (SearchConfig searchConfig : query.getSearchConfigs()) {
            ActorRef searchActor = context().actorOf(SearchActor.props(provider), searchConfig.getName() + "_search");
            searchActor.tell(new SearchQuery(query.getText(), searchConfig), self());
        }
        awaitingAnswers = query.getSearchConfigs().size();
        context().setReceiveTimeout(config.getTimeout());
    }

    private void onReceiveTimeout(ReceiveTimeout msg) {
        finish();
    }

    private void onSearchResult(SearchResult searchResult) {
        result.put(searchResult.getEngineName(), searchResult.getUrls());
        if (result.size() == awaitingAnswers) {
            finish();
        }
    }

    private void finish() {
        getContext().cancelReceiveTimeout();
        context().stop(self());
    }

    @Override
    public void postStop() {
        resultConsumer.complete(result);
    }
}
