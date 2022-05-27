package database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public interface SereneDatabaseClient {

    static SereneDatabaseClient create(FileConfiguration config) {
        Logger logger = LoggerFactory.getLogger(SereneDatabaseClient.class);
        try {
            String dbBasePath = "neo4j.db.";
            String uri = requireNonNull(config.get(dbBasePath + "uri")).toString();
            String username = requireNonNull(config.get(dbBasePath + "user")).toString();
            String password = requireNonNull(config.get(dbBasePath + "password")).toString();
            Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
            driver.verifyConnectivity();
            return new Neo4jSereneClient(driver);
        } catch (Exception e) {
            logger.info("Could not create Neo4j database client, using default no-op database client. {}", e.getLocalizedMessage());
            return new NoOpDatabaseClient();
        }
    }

    void addExperienceForPlayer(Player player, long amount);

    long getExperienceForPlayer(Player player);

    void setExperienceForPlayer(Player player, long max);
}
