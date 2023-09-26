package me.plugin.serene.actions;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

public class SleepHandler {
    private static final int FULL_DAY_TIME = 24000;

    private AtomicReference<Player> atomicPlayerReference;

    public SleepHandler() {
        this.atomicPlayerReference = new AtomicReference<>();
    }

    public void handleEvent(PlayerBedEnterEvent playerBedEnterEvent) {
        if (playerBedEnterEvent.getBedEnterResult().equals(PlayerBedEnterEvent.BedEnterResult.OK)) {
            if (playerBedEnterEvent.getPlayer().getWorld().getPlayers().size() == 1) {
                return;
            }
            var sleepingPlayer = playerBedEnterEvent.getPlayer();
            playerBedEnterEvent.getBed().getWorld().getPlayers().stream()
                    .filter(player -> !player.equals(sleepingPlayer))
                    .forEach(player -> player.setSleepingIgnored(false));
            atomicPlayerReference.set(sleepingPlayer);
            notifyAllPlayersOfSleepEvent(playerBedEnterEvent);
        }
    }

    public void handleEvent(PlayerBedLeaveEvent playerBedLeaveEvent) {
        if (playerBedLeaveEvent.getPlayer().getWorld().getPlayers().size() == 1) {
            return;
        }
        var sleepingPlayer = playerBedLeaveEvent.getPlayer();
        if (playerBedLeaveEvent.isCancelled() && atomicPlayerReference.get().equals(sleepingPlayer)) {
            atomicPlayerReference = new AtomicReference<>();
        }
        var players = playerBedLeaveEvent.getBed().getWorld().getPlayers();
        for (var player : players) {
            player.setSleepingIgnored(false);
        }
    }

    private void notifyAllPlayersOfSleepEvent(PlayerBedEnterEvent playerBedEnterEvent) {
        var sleepingPlayer = playerBedEnterEvent.getPlayer();
        var sleepingPlayerName = sleepingPlayer.getName();
        var world = requireNonNull(sleepingPlayer.getWorld());
        var players = world.getPlayers();
        for (var p : players) {
            p.sendMessage(sleepingPlayerName + " has triggered sleep");
        }
    }

    public static long nextDayFullTime(long currentTime) {
        var daysElapsed = currentTime / FULL_DAY_TIME;
        return (daysElapsed + 1) * FULL_DAY_TIME;
    }
}
