package me.plugin.serene.actions;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

public class SleepHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SleepHandler.class);
    private AtomicReference<Player> atomicPlayerReference;

    public SleepHandler() {
        this.atomicPlayerReference = new AtomicReference<>();
    }

    public void handleEnterEvent(PlayerBedEnterEvent playerBedEnterEvent) {
        var sleepingPlayer = playerBedEnterEvent.getPlayer();
        var targetWorld = sleepingPlayer.getWorld();
        if (Boolean.TRUE.equals(targetWorld.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE))) {
            LOG.info("Game rule doDayLightCycle is set to false, which means sleep will have no effect");
        }
        if (targetWorld.getPlayers().size() > 1
                && playerBedEnterEvent.getBedEnterResult().equals(PlayerBedEnterEvent.BedEnterResult.OK)
                && targetWorld.getEnvironment().equals(World.Environment.NORMAL)) {
            targetWorld.getPlayers().stream()
                    .filter(player -> !player.equals(sleepingPlayer))
                    .forEach(player -> player.setSleepingIgnored(true));
            atomicPlayerReference.set(sleepingPlayer);
            notifyAllPlayersOfSleepEvent(sleepingPlayer);
        }
    }

    public void handleLeaveEvent(Player sleepingPlayer, boolean cancelled) {
        var targetWorld = sleepingPlayer.getWorld();
        if (targetWorld.getPlayers().size() > 1 && targetWorld.getEnvironment().equals(World.Environment.NORMAL)) {
            if (cancelled && atomicPlayerReference.get().equals(sleepingPlayer)) {
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

    private void notifyAllPlayersOfSleepEvent(Player sleepingPlayer) {
        var sleepingPlayerName = sleepingPlayer.getName();
        var world = requireNonNull(sleepingPlayer.getWorld());
        var players = world.getPlayers();
        for (var p : players) {
            p.sendMessage(sleepingPlayerName + " has triggered one-person sleep");
        }
    }
}
