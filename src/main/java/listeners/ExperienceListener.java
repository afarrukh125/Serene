package listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperienceListener implements Listener {

    private static final Logger LOG = LoggerFactory.getLogger(ExperienceListener.class);

    @EventHandler
    public void handle(PlayerExpChangeEvent playerExpChangeEvent) {
        int originalAmount = playerExpChangeEvent.getAmount();
        Player player = playerExpChangeEvent.getPlayer();
        int level = player.getLevel();
        double bonus = Math.ceil((double) level / 4);
        int finalAmount = originalAmount + (int) bonus;

        if (level < 30) {
            playerExpChangeEvent.setAmount(finalAmount);
            LOG.info("Player {} | Original amount: {} Bonus: {} Final experience amount: {} (Level {})",
                    player.getDisplayName(),
                    originalAmount,
                    bonus,
                    finalAmount,
                    level);
        }
    }
}
