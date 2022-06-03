package me.plugin.serene.database;

import org.bukkit.entity.Player;

public class NoOpDatabaseClient implements SereneDatabaseClient {
    @Override
    public void setExperienceForPlayer(Player player, long max) {

    }

    @Override
    public void addExperienceForPlayer(Player player, long amount) {

    }

    @Override
    public long getExperienceForPlayer(Player player) {
        return 0;
    }
}
