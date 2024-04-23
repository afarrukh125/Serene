package me.plugin.serene.database;

import org.bukkit.entity.Player;

public interface SereneDatabaseClient {

    void addExperienceForPlayer(Player player, long amount);

    long getExperienceForPlayer(Player player);

    void setExperienceForPlayer(Player player, long amount);

    void setVeinBreakerEnabled(Player player, boolean enabled);

    boolean isVeinBreakerEnabled(Player player);
}
