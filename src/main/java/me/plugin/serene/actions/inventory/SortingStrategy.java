package me.plugin.serene.actions.inventory;

import java.util.List;
import java.util.Queue;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.inventory.ItemStack;

interface SortingStrategy {
    ItemStack[][] sort(
            List<MaterialItemStack> materialItemStacks, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced);

    static void dumpNormally(ItemStack[][] newStacks, Queue<ItemStack> stacks) {
        for (var i = newStacks.length - 1; i >= 0; i--) {
            for (var j = newStacks[i].length - 1; j >= 0 && !stacks.isEmpty(); j--) {
                if (newStacks[i][j] == null) {
                    newStacks[i][j] = stacks.poll();
                }
            }
        }
    }
}
