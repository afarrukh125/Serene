package me.plugin.serene.actions.inventory.util;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** Realistic unsorted inventories shared by the layout tests and the renderer. */
public final class SampleInventories {

    public static final List<ItemStack> STONE_HAUL = List.of(
            ItemStack.of(Material.STONE_SLAB, 64),
            ItemStack.of(Material.CHISELED_STONE_BRICKS, 64),
            ItemStack.of(Material.SMOOTH_STONE, 34),
            ItemStack.of(Material.SMOOTH_STONE, 64),
            ItemStack.of(Material.SMOOTH_STONE, 64),
            ItemStack.of(Material.SMOOTH_STONE, 64),
            ItemStack.of(Material.SMOOTH_STONE, 64),
            ItemStack.of(Material.STONE_SLAB, 64),
            ItemStack.of(Material.CHISELED_STONE_BRICKS, 24),
            ItemStack.of(Material.STONE_BRICKS, 64),
            ItemStack.of(Material.STONE_BRICKS, 64),
            ItemStack.of(Material.STONE_SLAB, 64),
            ItemStack.of(Material.MOSSY_COBBLESTONE, 8),
            ItemStack.of(Material.MOSSY_STONE_BRICKS, 4),
            ItemStack.of(Material.STONE_SLAB, 64),
            ItemStack.of(Material.STONE_BRICK_SLAB, 64),
            ItemStack.of(Material.STONE_BRICK_WALL, 5),
            ItemStack.of(Material.STONE_SLAB, 64),
            ItemStack.of(Material.STONE_STAIRS, 9),
            ItemStack.of(Material.STONE, 44),
            ItemStack.of(Material.STONE, 64),
            ItemStack.of(Material.STONE, 64),
            ItemStack.of(Material.STONE, 64),
            ItemStack.of(Material.STONE, 64),
            ItemStack.of(Material.STONE, 64),
            ItemStack.of(Material.STONE_SLAB, 16),
            ItemStack.of(Material.STONE, 64),
            ItemStack.of(Material.STONE, 64),
            ItemStack.of(Material.STONE, 64),
            ItemStack.of(Material.STONE, 64),
            ItemStack.of(Material.STONE, 64),
            ItemStack.of(Material.STONE, 64),
            ItemStack.of(Material.STONE, 64),
            ItemStack.of(Material.STONE, 64));

    public static ItemStack[] stoneHaul() {
        return STONE_HAUL.toArray(ItemStack[]::new);
    }

    private SampleInventories() {}
}
