package me.plugin.serene.actions.inventory;

import me.plugin.serene.model.Coordinate;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class VerticalSortingStrategy implements UnidirectionalSortingStrategy {

    @Override
    public void sort(MaterialItemStack materialItemStack, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        populateVertically(materialItemStack, newStacks, notPlaced);
    }

    private void populateVertically(
            MaterialItemStack materialItemStack, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        var itemStacks = materialItemStack.itemStacks();
        if (itemStacks.isEmpty()) {
            return;
        }
        var done = false;
        for (var x = 0; x < newStacks[0].length; x++) {
            for (var y = 0; y < newStacks.length; y++) {
                if (newStacks[y][x] != null) {
                    continue;
                }
                var verticalCoordinates = canFitVertically(itemStacks.size(), newStacks, x, y);
                if (!verticalCoordinates.isEmpty()) {
                    UnidirectionalSortingStrategy.populate(itemStacks, newStacks, verticalCoordinates);
                    done = true;
                    break;
                }
            }
            if (done) {
                break;
            }
        }
        if (!done && !materialItemStack.itemStacks().isEmpty()) {
            notPlaced.add(materialItemStack);
        }
    }

    private List<Coordinate> canFitVertically(int size, ItemStack[][] newStacks, int startX, int startY) {
        if (newStacks.length - startY < size) {
            return emptyList();
        }
        var coordinates = new ArrayList<Coordinate>();
        for (var y = startY; y < newStacks.length; y++) {
            if (newStacks[y][startX] != null) {
                return emptyList();
            } else {
                coordinates.add(new Coordinate(startX, y));
            }
        }
        return coordinates;
    }
}
