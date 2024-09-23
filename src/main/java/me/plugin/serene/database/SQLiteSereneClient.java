package me.plugin.serene.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import org.bukkit.entity.Player;

public class SQLiteSereneClient implements SereneDatabaseClient {

    private static final String RELATIVE_PATH = "plugins/serene/Serene.sqlite";
    private final Connection connection;

    SQLiteSereneClient() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:%s".formatted(RELATIVE_PATH));
            createUserTableIfNotPresent();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createUserTableIfNotPresent() throws SQLException {
        if (!isUserTableCreated()) createUserTable();
    }

    private void createUserTable() {
        try {
            var queries = List.of(
                    "CREATE TABLE player(id VARCHAR(60), exp VARCHAR(60), seed VARCHAR(60), veinBreakerEnabled BOOLEAN)");
            for (var query : queries) {
                connection.createStatement().execute(query);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isUserTableCreated() throws SQLException {
        var playerTable = connection
                .createStatement()
                .executeQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name='player'");
        return playerTable.next();
    }

    @Override
    public void addExperienceForPlayer(Player player, long amount) {
        var currentAmount = getExperienceForPlayer(player);
        var newAmount = currentAmount + amount;
        setExperienceForPlayer(player, newAmount);
    }

    @Override
    public long getExperienceForPlayer(Player player) {
        try {
            var seed = player.getWorld().getSeed();
            var query = "SELECT exp FROM player WHERE id='%s' AND seed=%s"
                    .formatted(player.getUniqueId().toString(), seed);
            var resultSet = connection.createStatement().executeQuery(query);
            if (!resultSet.next()) {
                createEntryForPlayer(player, seed);
                return 0;
            }
            return resultSet.getLong("exp");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createEntryForPlayer(Player player, long seed) {
        try {
            var query = "INSERT INTO player VALUES (?, ?, ?, ?)";
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.setLong(2, 0);
            preparedStatement.setLong(3, seed);
            preparedStatement.setBoolean(4, true);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setExperienceForPlayer(Player player, long amount) {
        var seed = player.getWorld().getSeed();
        var query = "UPDATE player SET exp=? WHERE id='%s' AND seed=%d".formatted(player.getUniqueId(), seed);
        try {
            createPlayerIfNotExisting(player, seed);
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, amount);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createPlayerIfNotExisting(Player player, long seed) throws SQLException {
        var query = "SELECT exp FROM player WHERE id='%s' AND seed=%s"
                .formatted(player.getUniqueId().toString(), seed);
        var resultSet = connection.createStatement().executeQuery(query);
        if (!resultSet.next()) {
            createEntryForPlayer(player, seed);
        }
    }

    public void setVeinBreakerEnabled(Player player, boolean value) {
        long seed = player.getWorld().getSeed();
        try {
            createPlayerIfNotExisting(player, seed);
            var query = "UPDATE player SET veinBreakerEnabled=? WHERE id='%s' AND seed=%d"
                    .formatted(player.getUniqueId(), seed);
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setBoolean(1, value);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isVeinBreakerEnabled(Player player) {
        var seed = player.getWorld().getSeed();
        var query = "SELECT veinBreakerEnabled FROM player WHERE id='%s' AND seed=%s"
                .formatted(player.getUniqueId().toString(), seed);
        try {
            createPlayerIfNotExisting(player, seed);
            var resultSet = connection.createStatement().executeQuery(query);
            if (!resultSet.next()) {
                createEntryForPlayer(player, seed);
                return true;
            }
            return resultSet.getBoolean("veinBreakerEnabled");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
