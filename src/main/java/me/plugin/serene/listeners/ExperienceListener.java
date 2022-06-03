package me.plugin.serene.listeners;

import me.plugin.serene.database.SereneDatabaseClient;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class ExperienceListener implements Listener {

    public static final int BONUS = 100;
    public static final int REWARD_THRESHOLD = 100;
    private final SereneDatabaseClient databaseClient;

    public ExperienceListener(SereneDatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @EventHandler
    public void onExperienceChange(PlayerExpChangeEvent playerExpChangeEvent) {
        var player = playerExpChangeEvent.getPlayer();
        var level = player.getLevel();
        if (level > 20) {
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
