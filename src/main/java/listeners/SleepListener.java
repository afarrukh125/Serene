package listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

public class SleepListener implements Listener {

    private static final Logger LOG = LoggerFactory.getLogger(SleepListener.class);
    private static final int FULL_DAY_TIME = 24000;

    private AtomicReference<Player> atomicPlayerReference;

    public SleepListener() {
        atomicPlayerReference = new AtomicReference<>();
    }

    public static long nextDayFullTime(long currentTime) {
        long daysElapsed = currentTime / FULL_DAY_TIME;
        return (daysElapsed + 1) * FULL_DAY_TIME;
    }

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent playerBedEnterEvent) {
        if (playerBedEnterEvent.getBedEnterResult().equals(PlayerBedEnterEvent.BedEnterResult.OK)) {
            if (playerBedEnterEvent.getPlayer().getWorld().getPlayers().size() == 1)
                return;
            Player sleepingPlayer = playerBedEnterEvent.getPlayer();
            List<Player> players = playerBedEnterEvent.getBed().getWorld().getPlayers();
            for (Player player : players) {
                if (!player.equals(sleepingPlayer))
                    player.setSleepingIgnored(true);
            }
            atomicPlayerReference.set(sleepingPlayer);
            notifyAllPlayersOfSleepEvent(playerBedEnterEvent);
        }
    }

    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent playerBedLeaveEvent) {
        Player sleepingPlayer = playerBedLeaveEvent.getPlayer();
        if (playerBedLeaveEvent.getPlayer().getWorld().getPlayers().size() == 1)
            return;
        if (playerBedLeaveEvent.isCancelled() && atomicPlayerReference.get().equals(sleepingPlayer)) {
            atomicPlayerReference = new AtomicReference<>();
            List<Player> players = playerBedLeaveEvent.getBed().getWorld().getPlayers();
            for (Player player : players)
                player.setSleepingIgnored(false);
        }
    }


    private void notifyAllPlayersOfSleepEvent(PlayerBedEnterEvent playerBedEnterEvent) {
        Player sleepingPlayer = playerBedEnterEvent.getPlayer();
        String sleepingPlayerName = sleepingPlayer.getName();
        World world = requireNonNull(sleepingPlayer.getWorld());
        List<Player> players = world.getPlayers();
        for (Player p : players)
            p.sendMessage(sleepingPlayerName + " has triggered sleep");
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
