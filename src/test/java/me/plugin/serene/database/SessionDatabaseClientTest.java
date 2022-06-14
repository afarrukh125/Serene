package me.plugin.serene.database;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SessionDatabaseClientTest {

    static Player player;
    private static SessionDatabaseClient sessionDatabaseClient;

    @BeforeAll
    static void setup() {
        player = Mockito.mock(Player.class);
    }

    @BeforeEach
    void setupDatabase() {
        sessionDatabaseClient = new SessionDatabaseClient();
    }

    @Test
    public void testIfPlayerIsNotThere() {
        boolean veinBreakerStatus = sessionDatabaseClient.isVeinBreakerEnabled(player);
        assertThat(veinBreakerStatus).isEqualTo(true);
    }

    @Test
    public void testSetFalseAndCheck() {
        sessionDatabaseClient.setVeinBreakerEnabled(player, false);
        assertThat(sessionDatabaseClient.isVeinBreakerEnabled(player)).isEqualTo(false);
    }
}