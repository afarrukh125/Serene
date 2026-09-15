package me.plugin.serene.model;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Identifies the thing being sorted for the purposes of remembering its last layout. Holds only a UUID rather
 * than a {@link Location} so that remembering a chest never keeps its {@link org.bukkit.World} alive.
 */
public record SortTarget(UUID owner, int x, int y, int z) {

    public static SortTarget ofBlock(Location location) {
        return new SortTarget(
                location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /** Ender chests are per player, so two players at the same block must not share a layout. */
    public static SortTarget ofEnderChest(Player player) {
        return new SortTarget(player.getUniqueId(), 0, 0, 0);
    }
}
