package config;

import com.typesafe.config.Config;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigUtils {
    public static List<SearchConfig> getSearchEnginesConfigs(Config enginesConfig) {
        return enginesConfig.getStringList("engineList")
                .stream()
                .map(engine -> new SearchConfig(engine,
                        enginesConfig.getConfig(engine).getString("host"),
                        enginesConfig.getConfig(engine).getInt("port")))
                .collect(Collectors.toList());
    }
}
