package me.plugin.serene.actions.inventory;

import me.plugin.serene.model.Coordinate;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static me.plugin.serene.actions.inventory.SortingStrategy.dumpNormally;

public class PrioritisingHorizontalSortingStrategy extends BidirectionalSortingStrategy {

    @Override
    public ItemStack[][] sort(List<MaterialItemStack> materialItemStacks, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        alternatePrioritisingHorizontal(materialItemStacks, newStacks, notPlaced);
        alternatePrioritisingVertical(materialItemStacks, newStacks, notPlaced);
        if (!notPlaced.isEmpty()) {
            dumpRemainingHorizontally(newStacks, notPlaced);
        }
        return newStacks;
    }

    private void dumpRemainingHorizontally(ItemStack[][] newStacks, List<MaterialItemStack> couldntBePlaced) {
        for (var materialItemStack : couldntBePlaced.stream()
                .filter(materialItemStack -> !materialItemStack.itemStacks().isEmpty())
                .toList()) {
            if (materialItemStack.itemStacks().isEmpty()) {
                continue;
            }
            var stacks = materialItemStack.itemStacks();
            var placed = false;
            int maxLineLength = newStacks[0].length;
            for (int i = 2; i < maxLineLength; i++) {
                var totalItems = stacks.size();
                var lineSize = totalItems / i;
                var lineSizeDouble = (double) totalItems / i;
                if (lineSizeDouble == lineSize && lineSize > 1 && lineSize <= maxLineLength) {
                    var coordinates = tryToPlaceInGridHorizontally(newStacks, lineSize, i);
                    if (!coordinates.isEmpty()) {
                        for (var coordinate : coordinates) {
                            newStacks[coordinate.y()][coordinate.x()] = stacks.poll();
                        }
                        placed = true;
                    }
                }
                if (placed) {
                    break;
                }
            }
            if (!placed) {
                dumpNormally(newStacks, stacks);
            }
        }
    }

    private List<Coordinate> tryToPlaceInGridHorizontally(ItemStack[][] newStacks, int targetLineSize, int numRows) {
        for (int y = 0; y < newStacks.length; y++) {
            for (int x = 0; x < newStacks[y].length; x++) {
                if (newStacks[y][x] == null) {
                    var coordinates = new ArrayList<Coordinate>();
                    var canPlace = true;
                    for (int heightPointer = y;
                         heightPointer < newStacks.length && heightPointer < y + numRows;
                         heightPointer++) {
                        for (int widthPointer = x;
                             widthPointer < newStacks[y].length && widthPointer < x + targetLineSize;
                             widthPointer++) {
                            coordinates.add(new Coordinate(widthPointer, heightPointer));
                            if (newStacks[heightPointer][widthPointer] != null) {
                                canPlace = false;
                                break;
                            }
                        }
                        if (!canPlace) {
                            break;
                        }
                    }
                    if (canPlace && coordinates.size() == (targetLineSize*numRows)) {
                        return coordinates;
                    }
                }
            }
        }
        return emptyList();
    }
}
