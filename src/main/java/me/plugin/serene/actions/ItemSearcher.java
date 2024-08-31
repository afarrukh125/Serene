package me.plugin.serene.actions;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class ItemSearcher {

    private static final double MAX_DISTANCE_TO_SEARCH = 10;
    public static final int EFFECT_DELAY_MILLIS = 250;

    public void searchItem(Player player, Location originalLocation, World world, String targetItemName) {
        parseTargetMaterialFromParam(targetItemName)
                .ifPresentOrElse(
                        targetMaterial -> performSearch(player, originalLocation, world, targetMaterial),
                        () -> player.sendMessage(
                                "Could not match %s to an item. Ensure you spelt the name of the item correctly."
                                        .formatted(targetItemName)));
    }

    private static void performSearch(Player player, Location originalLocation, World world, Material targetMaterial) {
        var enderChestHasItem = player.getEnderChest().contains(targetMaterial);
        var locationsToSearch = new LinkedList<Location>();
        var foundChestLocations = new HashSet<Location>();
        var seenChests = new HashSet<Chest>();
        var seenLocations = new HashSet<Location>();
        locationsToSearch.add(originalLocation);

        while (!locationsToSearch.isEmpty()) {
            var nextLocation = locationsToSearch.poll();
            if (seenLocations.contains(nextLocation)) {
                continue;
            }
            var block = nextLocation.getBlock();
            var blockType = requireNonNull(block.getType());
            var wasChest = blockType.equals(Material.CHEST);
            if (wasChest) {
                var chest = (Chest) block.getState();
                var inventory = chest.getInventory();
                if (!inventory.isEmpty() && inventory.contains(targetMaterial) && !seenChests.contains(chest)) {
                    foundChestLocations.add(chest.getLocation());
                    seenChests.add(chest);
                }
            }

            if (blockType.equals(Material.ENDER_CHEST) && enderChestHasItem) {
                foundChestLocations.add(block.getLocation());
            }

            seenLocations.add(nextLocation);

            if (isInBoundsOfOriginalLocation(originalLocation, nextLocation)) {
                queueSurroundingLocations(world, locationsToSearch, nextLocation);
            }
        }

        var sanitisedName = sanitiseName(targetMaterial);

        if (!foundChestLocations.isEmpty()) {
            if (foundChestLocations.stream()
                    .anyMatch(l -> (l.getY() > player.getLocation().getY())
                            || (l.getY() < player.getLocation().getY()))) {
                player.sendMessage(
                        "Found chests with item %s (Marked with particle effects, may be above/below you)"
                                .formatted(sanitisedName));
            } else {
                player.sendMessage(
                        "Found chests with item %s (Marked with particle effects)".formatted(sanitisedName));
            }
            for (var chestLocation : foundChestLocations) {
                playEffectsAtLocation(player, chestLocation);
            }
        } else {
            player.sendMessage("Found no chests nearby with item %s".formatted(sanitisedName));
        }
    }

    private static String sanitiseName(Material targetMaterial) {
        return Arrays.stream(targetMaterial.name().split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0))
                        + word.substring(1).toLowerCase())
                .collect(joining(" "));
    }

    private static boolean isInBoundsOfOriginalLocation(Location originalLocation, Location nextLocation) {
        return Math.abs(nextLocation.getX() - originalLocation.getX()) < MAX_DISTANCE_TO_SEARCH
                && Math.abs(nextLocation.getY() - originalLocation.getY()) < MAX_DISTANCE_TO_SEARCH
                && Math.abs(nextLocation.getZ() - originalLocation.getZ()) < MAX_DISTANCE_TO_SEARCH;
    }

    private static void queueSurroundingLocations(
            World world, Queue<Location> locationsToSearch, Location nextLocation) {
        locationsToSearch.add(new Location(world, nextLocation.getX() + 1, nextLocation.getY(), nextLocation.getZ()));
        locationsToSearch.add(new Location(world, nextLocation.getX() - 1, nextLocation.getY(), nextLocation.getZ()));
        locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY() + 1, nextLocation.getZ()));
        locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY() - 1, nextLocation.getZ()));
        locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY(), nextLocation.getZ() + 1));
        locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY(), nextLocation.getZ() - 1));
    }

    private static void playEffectsAtLocation(Player player, Location location) {
        player.playSound(location, Sound.ITEM_BUNDLE_DROP_CONTENTS, 0.45f, 0.25f);
        var timer = new Timer();
        timer.schedule(new EffectTimer(Effect.ELECTRIC_SPARK, player, location, timer), 0, EFFECT_DELAY_MILLIS);
    }

    private Optional<Material> parseTargetMaterialFromParam(String targetItemParam) {
        return Optional.ofNullable(Material.matchMaterial(targetItemParam));
    }

    private static class EffectTimer extends TimerTask {
        private final Player player;
        private final Location location;
        private final Timer timer;
        private int repeats;
        private static final int MAX_REPEATS = 8;
        private final Effect effect;

        public EffectTimer(Effect effect, Player player, Location location, Timer timer) {
            this.player = player;
            this.location = location;
            this.timer = timer;
            this.effect = effect;
        }

        @Override
        public void run() {
            player.playEffect(location, effect, null);
            repeats++;
            if (repeats >= MAX_REPEATS) {
                timer.cancel();
            }
        }
    }
}
