package me.plugin.serene.database;

import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface SereneDatabaseClient {

    static SereneDatabaseClient create() {
        Logger logger = LoggerFactory.getLogger(SereneDatabaseClient.class);
        try {
            return new SQLiteSereneClient();
        } catch (Exception e) {
            logger.info("Could not create connected database client, using default SQL flat file. {}", e.getMessage());
            return new NoOpDatabaseClient();
        }
    }

    void addExperienceForPlayer(Player player, long amount);

    long getExperienceForPlayer(Player player);

    void setExperienceForPlayer(Player player, long amount);
}
