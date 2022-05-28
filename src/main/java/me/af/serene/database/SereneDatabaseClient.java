package me.af.serene.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public interface SereneDatabaseClient {

    static SereneDatabaseClient create(FileConfiguration config) {
        // TODO make a file-based one since we want easier compilation and no dependency on neo4j
        return new NoOpDatabaseClient();
    }

    void addExperienceForPlayer(Player player, long amount);

    long getExperienceForPlayer(Player player);

    void setExperienceForPlayer(Player player, long max);
}
