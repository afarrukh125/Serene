package me.plugin.serene.model;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public record SortTarget(UUID owner, int x, int y, int z) {

    public static SortTarget ofBlock(Location location) {
        return new SortTarget(
                location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static SortTarget ofEnderChest(Player player) {
        return new SortTarget(player.getUniqueId(), 0, 0, 0);
    }
}
