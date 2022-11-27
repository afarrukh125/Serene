package me.plugin.serene.statistics;

import org.bukkit.Statistic;

import java.util.Optional;
import java.util.Set;

public interface CustomStatistic {
    Statistic getStatistic();

    Set<String> getAliases();

    double readableValue(double original);

    String getMessageSubject();

    Optional<String> customMessage(double value);
}
