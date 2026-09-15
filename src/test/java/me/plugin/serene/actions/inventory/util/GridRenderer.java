package me.plugin.serene.actions.inventory.util;

import static me.plugin.serene.actions.inventory.InventorySorter.ROW_SIZE;

import java.util.LinkedHashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Renders a sorted inventory as a symbol grid plus a legend, so layout tests read as a picture of the chest
 * and a regression shows up as a visual diff rather than a shifted slot index.
 */
public final class GridRenderer {

    private static final String SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String render(List<ItemStack> slots) {
        var legend = new LinkedHashMap<Material, Character>();
        var grid = new StringBuilder();
        for (var index = 0; index < slots.size(); index++) {
            var itemStack = slots.get(index);
            if (itemStack == null) {
                grid.append('.');
            } else {
                grid.append(legend.computeIfAbsent(itemStack.getType(), material -> SYMBOLS.charAt(legend.size())));
            }
            grid.append((index + 1) % ROW_SIZE == 0 ? "\n" : " ");
        }
        legend.forEach((material, symbol) ->
                grid.append(symbol).append(" = ").append(material.name()).append('\n'));
        return grid.toString();
    }

    public static String renderWithAmounts(List<ItemStack> slots) {
        var grid = new StringBuilder();
        for (var index = 0; index < slots.size(); index++) {
            var itemStack = slots.get(index);
            grid.append(itemStack == null ? "-" : itemStack.getType().name() + ":" + itemStack.getAmount());
            grid.append((index + 1) % ROW_SIZE == 0 ? "\n" : " ");
        }
        return grid.toString();
    }

    private GridRenderer() {}
}
