package me.plugin.serene.actions;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class PlayerTest {
    protected PlayerMock player;

    @BeforeEach
    public void setUp() {
        player = new PlayerMock(MockBukkit.mock(), "player1");
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }
}
