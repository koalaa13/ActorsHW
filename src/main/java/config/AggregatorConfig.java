package config;


import scala.concurrent.duration.Duration;

public class AggregatorConfig {
    private final Duration timeout;

    public AggregatorConfig(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getTimeout() {
        return timeout;
    }
}
