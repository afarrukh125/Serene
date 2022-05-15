package listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

public class SleepListener implements Listener {

    private static final Logger LOG = LoggerFactory.getLogger(SleepListener.class);
    AtomicBoolean hasSleepTriggered = new AtomicBoolean(false);
    private static final int FULL_DAY_TIME = 24000;
    public SleepListener() {
    }

    public static long nextDayFullTime(long currentTime) {
        long daysElapsed = currentTime / FULL_DAY_TIME;
        return (daysElapsed + 1) * FULL_DAY_TIME;
    }

    // fixme Can't trigger from separate thread
    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent playerBedEnterEvent) {
        if (playerBedEnterEvent.getBedEnterResult().equals(PlayerBedEnterEvent.BedEnterResult.OK) && !hasSleepTriggered.get()) {
            long startTime = System.nanoTime();
            long now = System.nanoTime();
            LOG.info("Starting count");
            Location location = playerBedEnterEvent.getBed().getLocation();
            notifyAllPlayersOfSleepEvent(playerBedEnterEvent, location);
            long targetTimeInNanos = Duration.ofSeconds(2).toNanos();
            while (now - startTime < targetTimeInNanos) {
                now = System.nanoTime();
            }
            LOG.info("Ready to apply");
            apply(playerBedEnterEvent);
            hasSleepTriggered.set(false);
        }
    }


    private void notifyAllPlayersOfSleepEvent(PlayerBedEnterEvent playerBedEnterEvent, Location bedLocation) {
        String sleepingPlayerName = playerBedEnterEvent.getPlayer().getName();
        World world = requireNonNull(bedLocation.getWorld());
        List<Player> players = world.getPlayers();
        for (Player p : players) {
            p.sendMessage(sleepingPlayerName + " has triggered sleep");
        }
    }

    private void apply(PlayerBedEnterEvent playerBedEnterEvent) {
        Location bedLocation = playerBedEnterEvent.getBed().getLocation();
        World world = requireNonNull(bedLocation.getWorld());
        long currentFullTime = world.getFullTime();
        LOG.info("World time is {}", currentFullTime);
        long targetTime = SleepListener.nextDayFullTime(currentFullTime);
        world.setFullTime(targetTime);
    }
}
