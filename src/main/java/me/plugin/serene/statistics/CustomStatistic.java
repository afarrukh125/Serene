package me.plugin.serene.statistics;

import org.bukkit.Statistic;

import java.util.Set;

public interface CustomStatistic {
    Statistic getStatistic();

    Set<String> getAliases();

    double translateToReadableValue(double original);

    String getMessageSubject();
}
