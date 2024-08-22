package me.plugin.serene.actions.inventory;

import me.plugin.serene.model.Coordinate;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class HorizontalSortingStrategy implements UnidirectionalSortingStrategy {
    @Override
    public void sort(MaterialItemStack materialItemStack, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        populateHorizontally(materialItemStack, newStacks, notPlaced);
    }

    private void populateHorizontally(
            MaterialItemStack materialItemStack, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        var itemStacks = materialItemStack.itemStacks();
        if (itemStacks.isEmpty()) {
            return;
        }
        var done = false;
        for (var x = newStacks[0].length - 1; x >= 0; x--) {
            for (var y = 0; y < newStacks.length; y++) {
                if (newStacks[y][x] != null) {
                    continue;
                }
                var horizontalCoordinates = canFitHorizontally(itemStacks.size(), newStacks, x, y);
                if (!horizontalCoordinates.isEmpty()) {
                    UnidirectionalSortingStrategy.populate(itemStacks, newStacks, horizontalCoordinates);
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

    private List<Coordinate> canFitHorizontally(int size, ItemStack[][] newStacks, int startX, int startY) {
        if (newStacks[startY].length - startX < size) {
            return emptyList();
        }
        var coordinates = new ArrayList<Coordinate>();
        for (var x = newStacks[0].length - 1; x >= startX; x--) {
            if (newStacks[startY][x] != null) {
                return emptyList();
            } else {
                coordinates.add(new Coordinate(x, startY));
            }
        }
        return coordinates;
    }
}
