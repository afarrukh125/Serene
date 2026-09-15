package me.plugin.serene.actions.inventory;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.LinkedHashMap;
import java.util.List;
import me.plugin.serene.model.Coordinate;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * Lays the whole inventory out as one uninterrupted run of items starting at the first slot, ordered by
 * {@link ItemOrder}. This is the InventoryTweaks layout: no gaps, related items adjacent, partial stacks
 * trailing the full ones.
 */
public class CompactingSortingStrategy implements SortingStrategy {

    public enum FillDirection {
        /** Reading order: left to right, then down a row. */
        ROW_MAJOR,
        /** Column order: top to bottom, then across a column. */
        COLUMN_MAJOR
    }

    private final FillDirection fillDirection;

    public CompactingSortingStrategy(FillDirection fillDirection) {
        this.fillDirection = fillDirection;
    }

    @Override
    public ItemStack[][] sort(
            List<MaterialItemStack> materialItemStacks, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        var numRows = newStacks.length;
        var rowSize = newStacks[0].length;
        var capacity = numRows * rowSize;
        var ordered = flattenInOrder(materialItemStacks);

        for (var index = 0; index < Math.min(ordered.size(), capacity); index++) {
            var coordinate = coordinateFor(index, numRows, rowSize);
            newStacks[coordinate.y()][coordinate.x()] = ordered.get(index);
        }
        if (ordered.size() > capacity) {
            notPlaced.addAll(regroup(ordered.subList(capacity, ordered.size())));
        }
        return newStacks;
    }

    private Coordinate coordinateFor(int index, int numRows, int rowSize) {
        return switch (fillDirection) {
            case ROW_MAJOR -> new Coordinate(index % rowSize, index / rowSize);
            case COLUMN_MAJOR -> new Coordinate(index / numRows, index % numRows);
        };
    }

    private static List<ItemStack> flattenInOrder(List<MaterialItemStack> materialItemStacks) {
        return materialItemStacks.stream()
                .filter(materialItemStack -> !materialItemStack.itemStacks().isEmpty())
                .sorted(ItemOrder.GROUPS)
                .flatMap(materialItemStack ->
                        materialItemStack.itemStacks().stream().sorted(ItemOrder.WITHIN_GROUP))
                .toList();
    }

    private static List<MaterialItemStack> regroup(List<ItemStack> overflow) {
        return overflow.stream()
                .collect(groupingBy(ItemStack::getType, LinkedHashMap::new, toList()))
                .entrySet()
                .stream()
                .map(entry -> new MaterialItemStack(entry.getKey(), entry.getValue()))
                .toList();
    }
}
