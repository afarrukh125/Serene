package me.plugin.serene.actions;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class InventorySorterTest {
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        player = new PlayerMock(MockBukkit.mock(), "player1");
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testSimpleOrganisedGroupGeneration() {
        // given
        var chestSorter = new InventorySorter();

        // then
        assertThat(chestSorter.getOrganisedGroups(player.getInventory())).isEmpty();

        // given
        player.getInventory()
                .addItem(
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.COBBLESTONE, 42));

        // when
        var updatedGroups = chestSorter.getOrganisedGroups(player.getInventory());

        // then
        assertThat(updatedGroups)
                .containsExactlyInAnyOrder(
                        materialItemStacks(Material.COBBLESTONE, 42), materialItemStacks(Material.ACACIA_LEAVES, 46));

        assertThat(updatedGroups).doesNotContain(materialItemStacks(Material.COBBLESTONE, 62));
    }

    /**
     * The grid for a chest, assuming 3 columns of 9 (27 slots total) looks something like
     * <p>
     * |  0   |  1   |  2   |   3  |   4  |  5  |  6  |  7  |  8  |
     * <p>
     * |  9   |  10  |  11  |  12  |  13  | 14  | 15  | 16  | 17  |
     * <p>
     * |  18  |  19  |  20  |  21  |  22  | 23  | 24  | 25  | 26  |
     * <p>
     * We assume an empty chest has all nulls and when the 2D array representing the chest contents is compressed,
     * we just get them all as if they were in one line
     */
    @Test
    void testComplexScenarioWithMultipleStacks() {
        // given
        var inventorySorter = new InventorySorter();
        player.getInventory()
                .addItem(
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.COBBLESTONE, 42));
        Supplier<List<MaterialItemStack>> groupSupplier =
                () -> inventorySorter.getOrganisedGroups(player.getInventory());

        // then
        assertThat(groupSupplier.get())
                .containsExactlyInAnyOrder(
                        materialItemStacks(Material.ACACIA_LEAVES, 64, 5),
                        materialItemStacks(Material.COBBLESTONE, 42));

        // when
        var itemStacks = getItemStacks(inventorySorter, groupSupplier.get());

        // then
        assertThat(itemStacks.get(0)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 64));
        assertThat(itemStacks.get(9)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 5));

        assertThat(itemStacks.get(8)).isEqualTo(ItemStack.of(Material.COBBLESTONE, 42));

        // when
        var itemStacksAgainAtSameLocation = getItemStacks(inventorySorter, groupSupplier.get());

        // then
        assertThat(itemStacksAgainAtSameLocation.get(0)).isEqualTo(ItemStack.of(Material.COBBLESTONE, 42));

        assertThat(itemStacksAgainAtSameLocation.get(8)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 64));
        assertThat(itemStacksAgainAtSameLocation.get(7)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 5));
        assertThat(itemStacksAgainAtSameLocation).filteredOn(Objects::isNull).hasSize(24);
    }

    @Test
    public void testComplexScenarioInLargeInventory() {
        // given
        var inventorySorter = new InventorySorter();

        var chest = spy(Chest.class);
        var inventory = spy(Inventory.class);

        var backingList = new ArrayList<ItemStack>();
        when(chest.getInventory()).thenReturn(inventory);
        when(inventory.addItem(any())).thenAnswer((Answer<Void>) invocation -> {
            backingList.addAll(Arrays.stream(invocation.getArguments())
                    .map(object -> ((ItemStack) object))
                    .toList());
            return null;
        });

        chest.getInventory()
                .addItem(
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.GRAVEL, 64),
                        ItemStack.of(Material.GRAVEL, 64),
                        ItemStack.of(Material.GRAVEL, 64),
                        ItemStack.of(Material.GRAVEL, 64),
                        ItemStack.of(Material.GRAVEL, 64),
                        ItemStack.of(Material.GRAVEL, 64),
                        ItemStack.of(Material.GRAVEL, 64),
                        ItemStack.of(Material.GRAVEL, 64),
                        ItemStack.of(Material.GRAVEL, 64),
                        ItemStack.of(Material.GRAVEL, 64),
                        ItemStack.of(Material.COBBLESTONE, 42));

        when(inventory.getContents()).thenReturn(backingList.toArray(new ItemStack[54]));

        Supplier<List<MaterialItemStack>> groupSupplier =
                () -> inventorySorter.getOrganisedGroups(chest.getInventory());

        // when
        var itemStacks = getItemStacks(inventorySorter, groupSupplier.get(), InventorySorter.LARGE_CHEST_NUM_ROWS);

        // then
        assertThat(itemStacks.get(0)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 64));
        assertThat(itemStacks.get(9)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 5));

        assertThat(itemStacks.get(8)).isEqualTo(ItemStack.of(Material.COBBLESTONE, 42));

        assertThat(itemStacks.subList(45, 54)).containsOnly(ItemStack.of(Material.GRAVEL, 64));
    }

    private List<ItemStack> getItemStacks(InventorySorter inventorySorter, List<MaterialItemStack> groups) {
        return getItemStacks(inventorySorter, groups, 3);
    }

    private List<ItemStack> getItemStacks(
            InventorySorter inventorySorter, List<MaterialItemStack> groups, int numRows) {
        return Arrays.asList(inventorySorter.generateFinalSortedItemStacks(groups, numRows, player.getLocation()));
    }

    private static MaterialItemStack materialItemStacks(Material material, int... amounts) {
        return new MaterialItemStack(
                material,
                new LinkedList<>(Arrays.stream(amounts)
                        .boxed()
                        .map(amount -> new ItemStack(material, amount))
                        .collect(toList())));
    }
}
