package utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TimerTask;

import static java.util.Objects.requireNonNull;

public class SleepTimerTask extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(SleepTimerTask.class);

    private static final int FULL_DAY_TIME = 24000;

    private final PlayerBedEnterEvent playerBedEnterEvent;

    public SleepTimerTask(PlayerBedEnterEvent playerBedEnterEvent) {
        this.playerBedEnterEvent = playerBedEnterEvent;
    }

    public static long nextDayFullTime(long currentTime) {
        long daysElapsed = currentTime / FULL_DAY_TIME;
        return (daysElapsed + 1) * FULL_DAY_TIME;
    }

    @Override
    public void run() {
        Location bedLocation = playerBedEnterEvent.getBed().getLocation();
        notifyAllPlayersOfSleepEvent(playerBedEnterEvent, bedLocation);
        World world = requireNonNull(bedLocation.getWorld());
        long currentFullTime = world.getFullTime();
        LOG.info("World time is {}", currentFullTime);
        long targetTime = nextDayFullTime(currentFullTime);
        world.setFullTime(targetTime);
    }

    private void notifyAllPlayersOfSleepEvent(PlayerBedEnterEvent playerBedEnterEvent, Location bedLocation) {
        String sleepingPlayerName = playerBedEnterEvent.getPlayer().getName();
        World world = requireNonNull(bedLocation.getWorld());
        List<Player> players = world.getPlayers();
        for (Player p : players) {
            p.sendMessage(sleepingPlayerName + " is sleeping.");
        }
    }
}
