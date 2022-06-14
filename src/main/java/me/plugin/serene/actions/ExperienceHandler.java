package me.plugin.serene.actions;

import me.plugin.serene.database.SereneDatabaseClient;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class ExperienceHandler {


    private static final int BONUS = 100;
    private static final int REWARD_THRESHOLD = 100;
    public static final int MINIMUM_LEVEL_FOR_BONUS = 20;

    private final SereneDatabaseClient databaseClient;

    public ExperienceHandler(SereneDatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public void handleEvent(PlayerExpChangeEvent playerExpChangeEvent) {
        var player = playerExpChangeEvent.getPlayer();
        var level = player.getLevel();
        if (level > MINIMUM_LEVEL_FOR_BONUS) {
            var originalAmount = playerExpChangeEvent.getAmount();
            databaseClient.addExperienceForPlayer(player, originalAmount);
            var experienceForPlayer = databaseClient.getExperienceForPlayer(player);
            if (experienceForPlayer >= REWARD_THRESHOLD) {
                var additional = BONUS + (2 * level);
                var finalAmount = originalAmount + additional;
                playerExpChangeEvent.setAmount(finalAmount);
                databaseClient.setExperienceForPlayer(player, Math.max(0, experienceForPlayer - REWARD_THRESHOLD));
                player.sendTitle("Bonus experience!",
                        "%s experience received".formatted(additional),
                        10,
                        70,
                        20);
            }
        }
    }
}
