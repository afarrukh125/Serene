package me.plugin.serene.actions.inventory;

import java.util.List;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.inventory.ItemStack;

interface SortingStrategy {
    ItemStack[][] sort(
            List<MaterialItemStack> materialItemStacks, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced);
}
