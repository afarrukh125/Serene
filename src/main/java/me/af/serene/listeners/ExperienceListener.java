package me.af.serene.listeners;

import me.af.serene.database.SereneDatabaseClient;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperienceListener implements Listener {

    public static final int BONUS = 100;
    public static final int REWARD_THRESHOLD = 100;
    private static final Logger LOG = LoggerFactory.getLogger(ExperienceListener.class);
    private final SereneDatabaseClient databaseClient;

    public ExperienceListener(SereneDatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @EventHandler
    public void onExperienceChange(PlayerExpChangeEvent playerExpChangeEvent) {
        Player player = playerExpChangeEvent.getPlayer();
        int level = player.getLevel();
        if (level > 20) {
            int originalAmount = playerExpChangeEvent.getAmount();
            databaseClient.addExperienceForPlayer(player, originalAmount);
            long experienceForPlayer = databaseClient.getExperienceForPlayer(player);
            if (experienceForPlayer >= REWARD_THRESHOLD) {
                int additional = BONUS + (2 * level);
                int finalAmount = originalAmount + additional;
                playerExpChangeEvent.setAmount(finalAmount);
                databaseClient.setExperienceForPlayer(player, Math.max(0, experienceForPlayer - REWARD_THRESHOLD));
                player.sendTitle("Experience bonus received",
                        "Bonus experience of %s received".formatted(additional),
                        10,
                        70,
                        20);
            }
        }
    }
}
