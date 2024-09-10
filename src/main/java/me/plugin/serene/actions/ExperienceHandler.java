package me.plugin.serene.actions;

import jakarta.inject.Inject;
import me.plugin.serene.database.SereneDatabaseClient;
import me.plugin.serene.model.SereneConfiguration;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class ExperienceHandler {

    private static final int BONUS = 100;
    private static final int REWARD_THRESHOLD = 100;
    private static final int MINIMUM_LEVEL_FOR_BONUS = 20;
    private static final int MAXIMUM_LEVEL_FOR_BONUS = 30;

    private final SereneDatabaseClient databaseClient;
    private final SereneConfiguration config;

    @Inject
    public ExperienceHandler(SereneDatabaseClient databaseClient, SereneConfiguration config) {
        this.databaseClient = databaseClient;
        this.config = config;
    }

    public void handleEvent(PlayerExpChangeEvent playerExpChangeEvent) {
        var player = playerExpChangeEvent.getPlayer();
        var level = player.getLevel();
        if (level > MINIMUM_LEVEL_FOR_BONUS && level < MAXIMUM_LEVEL_FOR_BONUS) {
            var originalAmount = playerExpChangeEvent.getAmount();
            databaseClient.addExperienceForPlayer(player, originalAmount);
            var experienceForPlayer = databaseClient.getExperienceForPlayer(player);
            if (experienceForPlayer >= REWARD_THRESHOLD) {
                var additional = BONUS + (2 * level);
                var finalAmount = originalAmount + additional;
                playerExpChangeEvent.setAmount(finalAmount);
                databaseClient.setExperienceForPlayer(player, Math.max(0, experienceForPlayer - REWARD_THRESHOLD));
                if (config.shouldShowExperienceMessage()) {
                    player.sendMessage("A bonus of %s experience received!".formatted(additional));
                }
            }
        }
    }
}
