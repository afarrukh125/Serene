package me.plugin.serene.actions;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

public class SleepHandler {

    private AtomicReference<Player> atomicPlayerReference;

    public SleepHandler() {
        this.atomicPlayerReference = new AtomicReference<>();
    }

    public void handleEvent(PlayerBedEnterEvent playerBedEnterEvent) {
        var targetWorld = playerBedEnterEvent.getPlayer().getWorld();
        if (targetWorld.getPlayers().size() != 1
                && playerBedEnterEvent.getBedEnterResult().equals(PlayerBedEnterEvent.BedEnterResult.OK)
                && targetWorld.getEnvironment().equals(World.Environment.NORMAL)) {
            var sleepingPlayer = playerBedEnterEvent.getPlayer();
            targetWorld.getPlayers().stream()
                    .filter(player -> !player.equals(sleepingPlayer))
                    .forEach(player -> player.setSleepingIgnored(false));
            atomicPlayerReference.set(sleepingPlayer);
            notifyAllPlayersOfSleepEvent(playerBedEnterEvent);
        }
    }

    public void handleEvent(PlayerBedLeaveEvent playerBedLeaveEvent) {
        var targetWorld = playerBedLeaveEvent.getPlayer().getWorld();
        if (targetWorld.getPlayers().size() != 1 && targetWorld.getEnvironment().equals(World.Environment.NORMAL)) {
            var sleepingPlayer = playerBedLeaveEvent.getPlayer();
            if (playerBedLeaveEvent.isCancelled() && atomicPlayerReference.get().equals(sleepingPlayer)) {
                atomicPlayerReference = new AtomicReference<>();
            }
            var players = targetWorld.getPlayers();
            for (var player : players) {
                player.setSleepingIgnored(false);
            }
            Bukkit.getServer().getWorlds().stream()
                    .filter(world -> !world.getEnvironment().equals(targetWorld.getEnvironment()))
                    .forEach(world -> world.setFullTime(targetWorld.getFullTime()));
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
}
