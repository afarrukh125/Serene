package me.plugin.serene.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

public class SleepListener implements Listener {

    private static final int FULL_DAY_TIME = 24000;

    private AtomicReference<Player> atomicPlayerReference;

    public SleepListener() {
        atomicPlayerReference = new AtomicReference<>();
    }

    public static long nextDayFullTime(long currentTime) {
        var daysElapsed = currentTime / FULL_DAY_TIME;
        return (daysElapsed + 1) * FULL_DAY_TIME;
    }

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent playerBedEnterEvent) {
        if (playerBedEnterEvent.getBedEnterResult().equals(PlayerBedEnterEvent.BedEnterResult.OK)) {
            if (playerBedEnterEvent.getPlayer().getWorld().getPlayers().size() == 1)
                return;
            var sleepingPlayer = playerBedEnterEvent.getPlayer();
            var players = playerBedEnterEvent.getBed().getWorld().getPlayers();
            for (var player : players) {
                if (!player.equals(sleepingPlayer))
                    player.setSleepingIgnored(true);
            }
            atomicPlayerReference.set(sleepingPlayer);
            notifyAllPlayersOfSleepEvent(playerBedEnterEvent);
        }
    }

    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent playerBedLeaveEvent) {
        if (playerBedLeaveEvent.getPlayer().getWorld().getPlayers().size() == 1)
            return;
        var sleepingPlayer = playerBedLeaveEvent.getPlayer();
        if (playerBedLeaveEvent.isCancelled() && atomicPlayerReference.get().equals(sleepingPlayer))
            atomicPlayerReference = new AtomicReference<>();
        var players = playerBedLeaveEvent.getBed().getWorld().getPlayers();
        for (var player : players)
            player.setSleepingIgnored(false);
    }


    private void notifyAllPlayersOfSleepEvent(PlayerBedEnterEvent playerBedEnterEvent) {
        var sleepingPlayer = playerBedEnterEvent.getPlayer();
        var sleepingPlayerName = sleepingPlayer.getName();
        var world = requireNonNull(sleepingPlayer.getWorld());
        var players = world.getPlayers();
        for (var p : players)
            p.sendMessage(Color.GREEN + sleepingPlayerName + " has triggered sleep");
    }

}
