package me.plugin.serene.statistics;

import org.bukkit.Statistic;

import java.util.Set;

public class DeathStatistic implements CustomStatistic {

    public static final String MAIN_NAME = "deaths";

    public DeathStatistic() {

    }

    @Override
    public Statistic getStatistic() {
        return Statistic.DEATHS;
    }

    @Override
    public Set<String> getAliases() {
        return Set.of(MAIN_NAME);
    }

    @Override
    public double translateToReadableValue(double original) {
        return original;
    }

    @Override
    public String getMessageSubject() {
        return "deaths";
    }
}
