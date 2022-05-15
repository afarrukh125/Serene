package listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.SleepTimerTask;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

public class SereneListener implements Listener {

    private static final Logger LOG = LoggerFactory.getLogger(SereneListener.class);
    private final Timer dayChangeTimer;
    private boolean shouldSkip;

    public SereneListener() {
        dayChangeTimer = new Timer("Sleep timer");
    }

    @EventHandler
    public void onPlayerExperienceGain(PlayerExpChangeEvent playerExpChangeEvent) {
        int originalAmount = playerExpChangeEvent.getAmount();
        Player player = playerExpChangeEvent.getPlayer();
        int level = player.getLevel();
        double bonus = Math.ceil((double) level / 4);
        int finalAmount = originalAmount + (int) bonus;

        if (level < 30) {
            playerExpChangeEvent.setAmount(finalAmount);
            LOG.info("Original amount: {} Bonus: {} Final experience amount: {} (Level {})",
                    originalAmount,
                    bonus,
                    finalAmount,
                    level);
        }
    }

    // fixme Can't trigger from separate thread
    public void onPlayerSleep(PlayerBedEnterEvent playerBedEnterEvent) {
        if (playerBedEnterEvent.getBedEnterResult().equals(PlayerBedEnterEvent.BedEnterResult.OK)) {
            TimerTask timerTask = new SleepTimerTask(playerBedEnterEvent);
            long delay = Duration.ofSeconds(2).toMillis();
            dayChangeTimer.schedule(timerTask, delay);
            shouldSkip = true;
        }
    }

    public void onPlayerLeaveBed(PlayerBedLeaveEvent bedLeaveEvent) {
        if (shouldSkip) {
            if (bedLeaveEvent.isCancelled()) {
                dayChangeTimer.cancel();
                shouldSkip = false;
            } else {
                shouldSkip = true;
            }
        }
    }
}
