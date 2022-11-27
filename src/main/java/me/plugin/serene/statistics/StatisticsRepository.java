package me.plugin.serene.statistics;

import java.util.HashMap;
import java.util.Map;

public class StatisticsRepository {
    private final Map<String, CustomStatistic> mappings;

    public StatisticsRepository() {
        mappings = new HashMap<>();
    }

    public StatisticsRepository registerStatistic(CustomStatistic customStatistic) {
        for (String alias : customStatistic.getAliases()) {
            mappings.put(alias, customStatistic);
        }
        return this;
    }

    public CustomStatistic getStatisticForString(String str) {
        return mappings.get(str);
    }
}
