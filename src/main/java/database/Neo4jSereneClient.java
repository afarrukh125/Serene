package database;

import org.bukkit.entity.Player;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import java.util.Map;

public class Neo4jSereneClient implements SereneDatabaseClient {
    private static final String ADD_QUERY =
            """
                    MERGE (player:Player {uuid: $player_uuid})
                    ON CREATE
                      SET player.accumulated_exp = $accumulated_exp
                    ON MATCH
                      SET player.accumulated_exp = player.accumulated_exp + $accumulated_exp
                    RETURN player
                    """;
    private static final String GET_QUERY =
            """
                    MERGE (player:Player {uuid: $player_uuid})
                    ON CREATE
                      SET player.accumulated_exp = 0
                    RETURN player.accumulated_exp AS accumulated_exp
                    """;
    public static final String ACCUMULATED_EXP = "accumulated_exp";
    public static final String PLAYER_UUID = "player_uuid";
    private static final String SET_QUERY =
            """
                    MERGE (player:Player {uuid: $player_uuid})
                    ON CREATE
                      SET player.accumulated_exp = $accumulated_exp
                    ON MATCH
                      SET player.accumulated_exp = $accumulated_exp
                    RETURN player
                    """;

    private final Driver driver;

    public Neo4jSereneClient(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void addExperienceForPlayer(Player player, long amount) {
        Session session = driver.session();
        Map<String, Object> parameters = Map.of(PLAYER_UUID, player.getUniqueId().toString(), ACCUMULATED_EXP, amount);
        session.writeTransaction(tx -> tx.run(ADD_QUERY, parameters));
        session.close();
    }

    @Override
    public long getExperienceForPlayer(Player player) {
        Session session = driver.session();
        Map<String, Object> parameters = Map.of(PLAYER_UUID, player.getUniqueId().toString());
        long experience = session.writeTransaction(transaction -> transaction.run(GET_QUERY, parameters).list()
                .stream()
                .findFirst()
                .orElseThrow().get(ACCUMULATED_EXP).asLong());
        session.close();
        return experience;
    }

    @Override
    public void setExperienceForPlayer(Player player, long amount) {
        Session session = driver.session();
        Map<String, Object> parameters = Map.of(PLAYER_UUID, player.getUniqueId().toString(), ACCUMULATED_EXP, amount);
        session.writeTransaction(tx -> tx.run(SET_QUERY, parameters));
        session.close();
    }
}
