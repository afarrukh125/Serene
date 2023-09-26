package me.plugin.serene.database;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLiteSereneClient implements SereneDatabaseClient {

    private static final String RELATIVE_PATH = "plugins/serene/Serene.sqlite";
    private final Connection connection;

    SQLiteSereneClient() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:%s".formatted(RELATIVE_PATH));
            tableCheck();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void tableCheck() throws SQLException {
        if (!isUserTableCreated()) createUserTable();
    }

    private void createUserTable() {
        try {
            List<String> queries = List.of(
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
        long currentAmount = getExperienceForPlayer(player);
        long newAmount = currentAmount + amount;
        setExperienceForPlayer(player, newAmount);
    }

    @Override
    public long getExperienceForPlayer(Player player) {
        try {
            long seed = player.getWorld().getSeed();
            String query = "SELECT exp FROM player WHERE id='%s' AND seed=%s"
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
            String query = "INSERT INTO player VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
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
        long seed = player.getWorld().getSeed();
        String query = "UPDATE player SET exp=? WHERE id='%s' AND seed=%d".formatted(player.getUniqueId(), seed);
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
        String query = "SELECT exp FROM player WHERE id='%s' AND seed=%s"
                .formatted(player.getUniqueId().toString(), seed);
        var resultSet = connection.createStatement().executeQuery(query);
        if (!resultSet.next()) createEntryForPlayer(player, seed);
    }

    public void setVeinBreakerEnabled(Player player, boolean value) {
        long seed = player.getWorld().getSeed();
        try {
            createPlayerIfNotExisting(player, seed);
            String query = "UPDATE player SET veinBreakerEnabled=? WHERE id='%s' AND seed=%d"
                    .formatted(player.getUniqueId(), seed);
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setBoolean(1, value);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isVeinBreakerEnabled(Player player) {
        long seed = player.getWorld().getSeed();
        String query = "SELECT veinBreakerEnabled FROM player WHERE id='%s' AND seed=%s"
                .formatted(player.getUniqueId().toString(), seed);
        try {
            createPlayerIfNotExisting(player, seed);
            ResultSet resultSet = connection.createStatement().executeQuery(query);
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
