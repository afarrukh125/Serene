package me.plugin.serene.actions.inventory.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import be.seeseemelk.mockbukkit.WorldMock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import me.plugin.serene.actions.inventory.InventorySorter;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mockito.stubbing.Answer;

public class InventoryUtils {

    public static final WorldMock WORLD_MOCK = new WorldMock(Material.GRAVEL, 0);

    public static List<ItemStack> setupFinalOrganisedInventory(ItemStack... itemStacks) {
        return setupFinalOrganisedInventory(new InventorySorter(), itemStacks);
    }

    public static List<ItemStack> setupFinalOrganisedInventory(
            InventorySorter inventorySorter, ItemStack... itemStacks) {
        return setupFinalOrganisedInventory(inventorySorter, spy(Inventory.class), itemStacks);
    }

    public static List<ItemStack> setupFinalOrganisedInventory(
            InventorySorter inventorySorter, Inventory inventory, ItemStack... itemStacks) {

        var chest = spy(Chest.class);
        var location = WORLD_MOCK.getSpawnLocation();

        var backingList = new ArrayList<ItemStack>();
        when(chest.getInventory()).thenReturn(inventory);
        when(inventory.addItem(any(ItemStack.class))).thenAnswer((Answer<Void>) invocation -> {
            backingList.addAll(Arrays.stream(invocation.getArguments())
                    .map(object -> ((ItemStack) object))
                    .toList());
            return null;
        });

        chest.getInventory().addItem(itemStacks);

        when(inventory.getContents()).thenReturn(backingList.toArray(new ItemStack[54]));

        Supplier<List<MaterialItemStack>> groupSupplier =
                () -> inventorySorter.getOrganisedGroups(chest.getInventory());

        return getItemStacks(inventorySorter, groupSupplier.get(), InventorySorter.LARGE_CHEST_NUM_ROWS, location);
    }

    public static List<ItemStack> getItemStacks(
            InventorySorter inventorySorter, List<MaterialItemStack> groups, Location location) {
        return getItemStacks(inventorySorter, groups, 3, location);
    }

    private static List<ItemStack> getItemStacks(
            InventorySorter inventorySorter, List<MaterialItemStack> groups, int numRows, Location location) {
        return Arrays.asList(inventorySorter.generateFinalSortedItemStacks(groups, numRows, location));
    }
}
