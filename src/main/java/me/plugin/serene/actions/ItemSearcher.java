package me.plugin.serene.actions;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ItemSearcher {

    private static final Logger LOG = LoggerFactory.getLogger(ItemSearcher.class);
    private static final double MAX_DISTANCE_TO_SEARCH = 10;

    private final String targetItemParam;

    public ItemSearcher(String targetItemParam) {
        this.targetItemParam = targetItemParam;
    }

    public void searchItem(Player player, Location originalLocation, World world) {
        parseTargetMaterialFromParam(targetItemParam).ifPresentOrElse(targetMaterial -> {
            boolean enderChestHasItem = player.getEnderChest().contains(targetMaterial);
            Queue<Location> locationsToSearch = new LinkedList<>();
            Set<Location> foundChestLocations = new HashSet<>();
            Set<Chest> seenChests = new HashSet<>();
            Set<Location> seenLocations = new HashSet<>();
            locationsToSearch.add(originalLocation);

            while (!locationsToSearch.isEmpty()) {
                var nextLocation = locationsToSearch.poll();
                if (seenLocations.contains(nextLocation)) {
                    continue;
                }
                Block block = nextLocation.getBlock();
                Material blockType = requireNonNull(block.getType());
                boolean wasChest = blockType.equals(Material.CHEST);
                if (wasChest) {
                    var chest = (Chest) block.getState();
                    Inventory inventory = chest.getInventory();
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

            if (!foundChestLocations.isEmpty()) {
                if (foundChestLocations.stream().anyMatch(l -> (l.getY() > player.getLocation().getY()) || (l.getY() < player.getLocation().getY()))) {
                    player.sendMessage("Found chests with item %s (Marked with green particle effect, may be above/below you)".formatted(targetMaterial));
                } else {
                    player.sendMessage("Found chests with item %s (Marked with green particle effect)".formatted(targetMaterial));
                }
                for (Location chestLocation : foundChestLocations) {
                    playEffectsAtLocation(player, chestLocation);
                }
            } else {

                player.sendMessage("Found no chests nearby with item %s".formatted(targetMaterial));
            }
        }, () -> player.sendMessage("Could not match %s to an item. Ensure you spelt the name of the item correctly."
                .formatted(targetItemParam)));
    }

    private static boolean isInBoundsOfOriginalLocation(Location originalLocation, Location nextLocation) {
        return Math.abs(nextLocation.getX() - originalLocation.getX()) < MAX_DISTANCE_TO_SEARCH &&
               Math.abs(nextLocation.getY() - originalLocation.getY()) < MAX_DISTANCE_TO_SEARCH &&
               Math.abs(nextLocation.getZ() - originalLocation.getZ()) < MAX_DISTANCE_TO_SEARCH;
    }

    private static void queueSurroundingLocations(World world, Queue<Location> locationsToSearch, Location nextLocation) {
        locationsToSearch.add(new Location(world, nextLocation.getX() + 1, nextLocation.getY(), nextLocation.getZ()));
        locationsToSearch.add(new Location(world, nextLocation.getX() - 1, nextLocation.getY(), nextLocation.getZ()));
        locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY() + 1, nextLocation.getZ()));
        locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY() - 1, nextLocation.getZ()));
        locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY(), nextLocation.getZ() + 1));
        locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY(), nextLocation.getZ() - 1));
    }

    private static void playEffectsAtLocation(Player player, Location location) {
        player.playEffect(location, Effect.BONE_MEAL_USE, 35);
        player.playSound(location, Sound.ITEM_BUNDLE_DROP_CONTENTS, 0.45f, 0.25f);
    }

    private Optional<Material> parseTargetMaterialFromParam(String targetItemParam) {
        Optional<Material> material = Optional.ofNullable(Material.matchMaterial(targetItemParam));
        if (material.isEmpty()) {
            return Optional.ofNullable(Material.matchMaterial(targetItemParam, true));
        }
        return material;
    }
}
