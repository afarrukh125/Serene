package me.plugin.serene.statistics;

import org.bukkit.Statistic;

import java.util.Set;

public class TimePlayedStatistic implements CustomStatistic {

    private final Set<String> aliases;

    public TimePlayedStatistic() {
        this.aliases = Set.of("time played", "played", "playtime", "timeplayed");
    }

    @Override
    public Statistic getStatistic() {
        return Statistic.PLAY_ONE_MINUTE;
    }

    @Override
    public Set<String> getAliases() {
        return aliases;
    }

    @Override
    public String getMessageSubject() {
        return "hours played";
    }

    @Override
    public double translateToReadableValue(double original) {
        return original / 20 / 60 / 60;
    }
}
