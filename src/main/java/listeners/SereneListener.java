package listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SereneListener implements Listener {

    private final Logger logger;

    public SereneListener() {
        this.logger = LoggerFactory.getLogger(SereneListener.class);
    }

    @EventHandler
    public void onPlayerExperienceGain(PlayerExpChangeEvent playerExpChangeEvent) {
        logger.info("Original amount gained: " + playerExpChangeEvent.getAmount());
        Player player = playerExpChangeEvent.getPlayer();
        int initialAmount = playerExpChangeEvent.getAmount();
        int level = player.getLevel();
        double bonus = Math.ceil((double) level / 4);
        int finalAmount = initialAmount + (int) bonus;

        if (level < 30) {
            playerExpChangeEvent.setAmount(finalAmount);
            logger.info("Actual experience gained is " + finalAmount + " from bonus.");
        }
    }
}
