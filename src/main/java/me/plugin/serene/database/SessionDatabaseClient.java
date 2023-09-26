package me.plugin.serene.database;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores data for this session only using Java objects
 */
public class SessionDatabaseClient implements SereneDatabaseClient {

    private final Map<Player, Long> experienceMap = new HashMap<>();
    private final Map<Player, Boolean> veinBreaker = new HashMap<>();

    @Override
    public void addExperienceForPlayer(Player player, long amount) {
        experienceMap.merge(player, amount, Long::sum);
    }

    @Override
    public long getExperienceForPlayer(Player player) {
        Long currentExperience = experienceMap.get(player);
        if (currentExperience == null) return 0;
        return currentExperience;
    }

    @Override
    public void setExperienceForPlayer(Player player, long amount) {
        experienceMap.put(player, amount);
    }

    @Override
    public void setVeinBreakerEnabled(Player player, boolean enabled) {
        veinBreaker.put(player, enabled);
    }

    @Override
    public boolean isVeinBreakerEnabled(Player player) {
        veinBreaker.putIfAbsent(player, true);
        return veinBreaker.get(player);
    }
}
