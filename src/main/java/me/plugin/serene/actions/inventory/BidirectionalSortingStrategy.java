package me.plugin.serene.actions.inventory;

import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public abstract class BidirectionalSortingStrategy implements SortingStrategy {

    private final UnidirectionalSortingStrategy horizontalSortingStrategy;
    private final UnidirectionalSortingStrategy verticalSortingStrategy;

    BidirectionalSortingStrategy() {
        this.horizontalSortingStrategy = new HorizontalSortingStrategy();
        this.verticalSortingStrategy = new VerticalSortingStrategy();
    }

    @Override
    public ItemStack[][] sort(
            List<MaterialItemStack> materialItemStacks, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        return new ItemStack[0][];
    }

    protected void alternatePrioritisingHorizontal(
            List<MaterialItemStack> materialItemStacks, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        int x = 0;
        for (var materialItemStack : filterStacks(materialItemStacks, itemStacksFitInRow())) {
            if (x % 2 == 0) {
                horizontalSortingStrategy.sort(materialItemStack, newStacks, notPlaced);
            } else {
                verticalSortingStrategy.sort(materialItemStack, newStacks, notPlaced);
            }
            x++;
        }
        notPlaced.addAll(filterStacks(materialItemStacks, itemStacksFitInRow().negate()));
    }

    protected void alternatePrioritisingVertical(
            List<MaterialItemStack> materialItemStacks, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        int x = 0;
        for (var materialItemStack : filterStacks(materialItemStacks, itemStacksFitInColumn(newStacks.length))) {
            if (x % 2 == 0) {
                verticalSortingStrategy.sort(materialItemStack, newStacks, notPlaced);
            } else {
                horizontalSortingStrategy.sort(materialItemStack, newStacks, notPlaced);
            }
            x++;
        }
        notPlaced.addAll(materialItemStacks.stream()
                .filter(itemStacksFitInColumn(newStacks.length))
                .toList());
    }

    private Predicate<MaterialItemStack> itemStacksFitInColumn(int length) {
        return materialItemStack -> materialItemStack.itemStacks().size() <= length;
    }

    private static Predicate<MaterialItemStack> itemStacksFitInRow() {
        return materialItemStack -> materialItemStack.itemStacks().size() <= InventorySorter.ROW_SIZE;
    }

    private static List<MaterialItemStack> filterStacks(
            List<MaterialItemStack> materialItemStacks, Predicate<MaterialItemStack> materialItemStackPredicate) {
        return materialItemStacks.stream()
                .filter(materialItemStack -> !materialItemStack.itemStacks().isEmpty())
                .filter(materialItemStackPredicate)
                .toList();
    }
}
