package listeners;

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
        logger.info("Amount gained: " + playerExpChangeEvent.getAmount());
    }
}
