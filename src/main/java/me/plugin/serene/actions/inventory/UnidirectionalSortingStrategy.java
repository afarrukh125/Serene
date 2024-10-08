package me.plugin.serene.actions.inventory;

import java.util.List;
import java.util.Queue;
import me.plugin.serene.model.Coordinate;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.inventory.ItemStack;

public interface UnidirectionalSortingStrategy {

    void sort(MaterialItemStack materialItemStack, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced);

    static void populate(Queue<ItemStack> itemStacks, ItemStack[][] newStacks, List<Coordinate> coordinates) {
        for (var coordinate : coordinates) {
            newStacks[coordinate.y()][coordinate.x()] = itemStacks.poll();
        }
    }
}
