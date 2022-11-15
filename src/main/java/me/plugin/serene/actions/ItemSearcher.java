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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
            Queue<Location> locationsToSearch = new LinkedList<>();
            List<Chest> foundChests = new ArrayList<>();
            Set<Chest> seenChests = new HashSet<>();
            Set<Location> seenLocations = new HashSet<>();
            locationsToSearch.add(originalLocation);

            while (!locationsToSearch.isEmpty()) {
                var nextLocation = locationsToSearch.poll();
                if (seenLocations.contains(nextLocation))
                    continue;
                Block block = nextLocation.getBlock();
                boolean wasChest = requireNonNull(block.getType()).equals(Material.CHEST);
                if (wasChest) {
                    var chest = (Chest) block.getState();
                    Inventory inventory = chest.getInventory();
                    if (!inventory.isEmpty() && inventory.contains(targetMaterial) && !seenChests.contains(chest)) {
                        foundChests.add(chest);
                        seenChests.add(chest);
                    }
                }

                seenLocations.add(nextLocation);

                if (Math.abs(nextLocation.getX() - originalLocation.getX()) < MAX_DISTANCE_TO_SEARCH &&
                    Math.abs(nextLocation.getY() - originalLocation.getY()) < MAX_DISTANCE_TO_SEARCH &&
                    Math.abs(nextLocation.getZ() - originalLocation.getZ()) < MAX_DISTANCE_TO_SEARCH) {
                    locationsToSearch.add(new Location(world, nextLocation.getX() + 1, nextLocation.getY(), nextLocation.getZ()));
                    locationsToSearch.add(new Location(world, nextLocation.getX() - 1, nextLocation.getY(), nextLocation.getZ()));
                    locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY() + 1, nextLocation.getZ()));
                    locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY() - 1, nextLocation.getZ()));
                    locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY(), nextLocation.getZ() + 1));
                    locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY(), nextLocation.getZ() - 1));
                }
            }

            if (!foundChests.isEmpty()) {
                if (foundChests.stream().anyMatch(c -> (c.getLocation().getY() > player.getLocation().getY()) || (c.getLocation().getY() < player.getLocation().getY()))) {
                    player.sendMessage("Found chests with item %s (Marked with green particle effect, may be above/below you)".formatted(targetMaterial));
                } else {
                    player.sendMessage("Found chests with item %s (Marked with green particle effect)".formatted(targetMaterial));
                }
                for (Chest chest : foundChests) {
                    player.playEffect(chest.getLocation(), Effect.BONE_MEAL_USE, 35);
                    player.playSound(chest.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.45f, 0.25f);
                }
            } else {
                player.sendMessage("Found no chests nearby with item %s".formatted(targetMaterial));
            }
        }, () -> player.sendMessage("Could not match %s to an item. Ensure you spelt the name of the item correctly."
                .formatted(targetItemParam)));
    }

    private Optional<Material> parseTargetMaterialFromParam(String targetItemParam) {
        Optional<Material> material = Optional.ofNullable(Material.matchMaterial(targetItemParam));
        if (material.isEmpty()) {
            return Optional.ofNullable(Material.matchMaterial(targetItemParam, true));
        }
        return material;
    }
}
