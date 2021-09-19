package listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

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

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent playerBedEnterEvent) {
        if(playerBedEnterEvent.getBedEnterResult().equals(PlayerBedEnterEvent.BedEnterResult.OK)) {
            Location bedLocation = playerBedEnterEvent.getBed().getLocation();
            Objects.requireNonNull(bedLocation.getWorld()).getPlayers().forEach(p->p.sendMessage(playerBedEnterEvent.getPlayer().getName() + " is sleeping."));
            Objects.requireNonNull(bedLocation.getWorld()).setTime(0);
        }
    }
}
