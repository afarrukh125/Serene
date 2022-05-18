package listeners;

import database.SereneDatabaseClient;
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
    public void handle(PlayerExpChangeEvent playerExpChangeEvent) {
        Player player = playerExpChangeEvent.getPlayer();
        int level = player.getLevel();
        if (level < 30) {
            int originalAmount = playerExpChangeEvent.getAmount();
            databaseClient.addExperienceForPlayer(player, originalAmount);
            long experienceForPlayer = databaseClient.getExperienceForPlayer(player);
            if (experienceForPlayer >= REWARD_THRESHOLD) {
                int finalAmount = originalAmount + BONUS + (2 * level);
                playerExpChangeEvent.setAmount(finalAmount);
                LOG.info("Applying bonus to player {} | Original amount: {} Final experience amount: {} Level before changes: {}",
                        player.getDisplayName(),
                        originalAmount,
                        finalAmount,
                        level);
                databaseClient.setExperienceForPlayer(player, Math.max(0, experienceForPlayer - REWARD_THRESHOLD));
            }
        }
    }
}
