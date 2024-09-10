package me.plugin.serene.actions.inventory;

import me.plugin.serene.actions.PlayerTest;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class InventorySorterTest extends PlayerTest {

    @Test
    void testSimpleOrganisedGroupGeneration() {
        // given
        var chestSorter = new InventorySorter();

        // then
        assertThat(chestSorter.getOrganisedGroups(player.getInventory())).isEmpty();

        // when
        player.getInventory()
                .addItem(
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.COBBLESTONE, 42));
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
        assertThat(itemStacks.get(0)).isEqualTo(ItemStack.of(Material.COBBLESTONE, 42));

        assertThat(itemStacks.get(8)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 64));
        assertThat(itemStacks.get(7)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 5));


        // when
        var verticallySortedItemStacks = getItemStacks(inventorySorter, groupSupplier.get());

        // then
        assertThat(verticallySortedItemStacks.get(0)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 64));
        assertThat(verticallySortedItemStacks.get(9)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 5));

        assertThat(verticallySortedItemStacks.get(8)).isEqualTo(ItemStack.of(Material.COBBLESTONE, 42));

        assertThat(verticallySortedItemStacks).filteredOn(Objects::isNull).hasSize(24);
    }

    @Test
    public void testComplexScenarioInLargeInventory() {
        // when
        var itemStacks = setupFinalOrganisedInventory(
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
                ItemStack.of(Material.EGG, 16),
                ItemStack.of(Material.EGG, 16),
                ItemStack.of(Material.EGG, 16),
                ItemStack.of(Material.EGG, 16),
                ItemStack.of(Material.EGG, 16),
                ItemStack.of(Material.EGG, 16),
                ItemStack.of(Material.EGG, 16),
                ItemStack.of(Material.EGG, 16),
                ItemStack.of(Material.EGG, 16),
                ItemStack.of(Material.COBBLESTONE, 42));

        // then
        assertThat(itemStacks.subList(0, 9)).containsOnly(ItemStack.of(Material.EGG, 16));

        assertThat(itemStacks.get(9)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 64));
        assertThat(itemStacks.get(18)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 5));

        assertThat(itemStacks.get(17)).isEqualTo(ItemStack.of(Material.COBBLESTONE, 42));

        assertThat(itemStacks.subList(10, 15)).containsOnly(ItemStack.of(Material.GRAVEL, 64));
        assertThat(itemStacks.subList(19, 24)).containsOnly(ItemStack.of(Material.GRAVEL, 64));
    }

    @Test
    public void testReallyComplexScenarioInLargeInventoryWithMoreTypesOfItems() {
        // given
        var inventorySorter = new InventorySorter();
        // when
        var items = new ItemStack[]{ItemStack.of(Material.CRIMSON_ROOTS, 8),
                ItemStack.of(Material.LANTERN, 2),
                ItemStack.of(Material.NETHERRACK, 33),
                ItemStack.of(Material.NETHERRACK, 64),
                ItemStack.of(Material.NETHERRACK, 64),
                ItemStack.of(Material.NETHERRACK, 64),
                ItemStack.of(Material.NETHERRACK, 64),
                ItemStack.of(Material.NETHERRACK, 64),
                ItemStack.of(Material.NETHERRACK, 64),
                ItemStack.of(Material.CRIMSON_STEM, 45),
                ItemStack.of(Material.MAGMA_BLOCK, 10),
                ItemStack.of(Material.NETHER_GOLD_ORE, 7),
                ItemStack.of(Material.SHROOMLIGHT, 7),
                ItemStack.of(Material.CRIMSON_NYLIUM, 8),
                ItemStack.of(Material.CRIMSON_NYLIUM, 64),
                ItemStack.of(Material.CRIMSON_NYLIUM, 64),
                ItemStack.of(Material.CRYING_OBSIDIAN, 10),
                ItemStack.of(Material.MOSS_CARPET, 12),
                ItemStack.of(Material.NETHER_QUARTZ_ORE, 12),
                ItemStack.of(Material.SOUL_SOIL, 3),
                ItemStack.of(Material.SOUL_SAND, 49),
                ItemStack.of(Material.SOUL_SAND, 64),
                ItemStack.of(Material.GHAST_TEAR, 2),
                ItemStack.of(Material.NETHER_BRICK, 11),
                ItemStack.of(Material.NETHER_WART_BLOCK, 38),
                ItemStack.of(Material.WARPED_ROOTS, 1),
                ItemStack.of(Material.BLACKSTONE, 1),
                ItemStack.of(Material.GILDED_BLACKSTONE, 9),
                ItemStack.of(Material.NETHER_BRICK_FENCE, 1),
                ItemStack.of(Material.OAK_PLANKS, 2),
                ItemStack.of(Material.WARPED_STEM, 11),
                ItemStack.of(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, 8),
                ItemStack.of(Material.GLOW_INK_SAC, 8),
                ItemStack.of(Material.NETHER_BRICK_STAIRS, 34),
                ItemStack.of(Material.POLISHED_BLACKSTONE_BRICKS, 62),
                ItemStack.of(Material.WEEPING_VINES, 3),
                ItemStack.of(Material.CRIMSON_FUNGUS, 13)};

        var inventoryToSortHorizontally = spy(Inventory.class);
        var itemStacksHorizontal = setupFinalOrganisedInventory(inventorySorter, inventoryToSortHorizontally, items);
        assertSameContents(itemStacksHorizontal, inventorySorter, inventoryToSortHorizontally);

        var inventoryToSortVertically = spy(Inventory.class);
        var itemStacksVertical = setupFinalOrganisedInventory(inventorySorter, inventoryToSortVertically, items);
        assertSameContents(itemStacksVertical, inventorySorter, inventoryToSortVertically);
    }

    @Test
    public void testReallyComplexScenarioInLargeInventoryWithLessTypesOfItems() {
        // given
        var inventorySorter = new InventorySorter();
        // when
        var items = new ItemStack[]{
                ItemStack.of(Material.STONE_SLAB, 64),
                ItemStack.of(Material.CHISELED_STONE_BRICKS, 64),
                ItemStack.of(Material.SMOOTH_STONE, 34),
                ItemStack.of(Material.SMOOTH_STONE, 64),
                ItemStack.of(Material.SMOOTH_STONE, 64),
                ItemStack.of(Material.SMOOTH_STONE, 64),
                ItemStack.of(Material.SMOOTH_STONE, 64),
                ItemStack.of(Material.STONE_SLAB, 64),
                ItemStack.of(Material.CHISELED_STONE_BRICKS, 24),
                ItemStack.of(Material.STONE_BRICKS, 64),
                ItemStack.of(Material.STONE_BRICKS, 64),
                ItemStack.of(Material.STONE_SLAB, 64),
                ItemStack.of(Material.MOSSY_COBBLESTONE, 8),
                ItemStack.of(Material.MOSSY_STONE_BRICKS, 4),
                ItemStack.of(Material.STONE_SLAB, 64),
                ItemStack.of(Material.STONE_BRICK_SLAB, 64),
                ItemStack.of(Material.STONE_BRICK_WALL, 5),
                ItemStack.of(Material.STONE_SLAB, 64),
                ItemStack.of(Material.STONE_STAIRS, 9),
                ItemStack.of(Material.STONE, 44),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE_SLAB, 16),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64)
        };

        var inventoryToSortHorizontally = spy(Inventory.class);
        var itemStacksHorizontal = setupFinalOrganisedInventory(inventorySorter, inventoryToSortHorizontally, items);
        assertSameContents(itemStacksHorizontal, inventorySorter, inventoryToSortHorizontally);

        var inventoryToSortVertically = spy(Inventory.class);
        var itemStacksVertical = setupFinalOrganisedInventory(inventorySorter, inventoryToSortVertically, items);
        assertSameContents(itemStacksVertical, inventorySorter, inventoryToSortVertically);
    }

    @Test
    public void testMostlySingleItemStacks() {
        // given
        var inventorySorter = new InventorySorter();
        // when
        var items = new ItemStack[]{
                ItemStack.of(Material.DIAMOND_BOOTS, 1),
                ItemStack.of(Material.DIAMOND_BOOTS, 1),
                ItemStack.of(Material.DIAMOND_BOOTS, 1),
                ItemStack.of(Material.DIAMOND_BOOTS, 1),
                ItemStack.of(Material.DIAMOND_BOOTS, 1),
                ItemStack.of(Material.DIAMOND_BOOTS, 1),
                ItemStack.of(Material.DIAMOND_BOOTS, 1),
                ItemStack.of(Material.DIAMOND_BOOTS, 1),
                ItemStack.of(Material.DIAMOND_BOOTS, 1),
                ItemStack.of(Material.IRON_HELMET, 1),
                ItemStack.of(Material.IRON_HELMET, 1),
                ItemStack.of(Material.IRON_HELMET, 1),
                ItemStack.of(Material.IRON_HELMET, 1),
                ItemStack.of(Material.IRON_HELMET, 1),
                ItemStack.of(Material.IRON_HELMET, 1),
                ItemStack.of(Material.IRON_HELMET, 1),
                ItemStack.of(Material.IRON_HELMET, 1),
                ItemStack.of(Material.IRON_HELMET, 1),
                ItemStack.of(Material.BOW, 1),
                ItemStack.of(Material.IRON_AXE, 1),
                ItemStack.of(Material.SHIELD, 1),
                ItemStack.of(Material.SHIELD, 1),
                ItemStack.of(Material.SHIELD, 1),
                ItemStack.of(Material.SHIELD, 1),
                ItemStack.of(Material.SHIELD, 1),
                ItemStack.of(Material.SHIELD, 1),
                ItemStack.of(Material.BOW, 1),
                ItemStack.of(Material.IRON_AXE, 1),
                ItemStack.of(Material.DIAMOND_CHESTPLATE, 1),
                ItemStack.of(Material.DIAMOND_CHESTPLATE, 1),
                ItemStack.of(Material.DIAMOND_CHESTPLATE, 1),
                ItemStack.of(Material.DIAMOND_HELMET, 1),
                ItemStack.of(Material.DIAMOND_HELMET, 1),
                ItemStack.of(Material.DIAMOND_HELMET, 1),
                ItemStack.of(Material.DIAMOND_HELMET, 1),
                ItemStack.of(Material.BOW, 1),
                ItemStack.of(Material.IRON_AXE, 1),
                ItemStack.of(Material.DIAMOND_CHESTPLATE, 1),
                ItemStack.of(Material.DIAMOND_CHESTPLATE, 1),
                ItemStack.of(Material.DIAMOND_CHESTPLATE, 1),
                ItemStack.of(Material.DIAMOND_LEGGINGS, 1),
                ItemStack.of(Material.DIAMOND_LEGGINGS, 1),
                ItemStack.of(Material.BOW, 1),
                ItemStack.of(Material.DIAMOND_LEGGINGS, 1),
                ItemStack.of(Material.DIAMOND_CHESTPLATE, 1),
                ItemStack.of(Material.DIAMOND_CHESTPLATE, 1),
                ItemStack.of(Material.DIAMOND_CHESTPLATE, 1),
                ItemStack.of(Material.DIAMOND_LEGGINGS, 1),
                ItemStack.of(Material.DIAMOND_LEGGINGS, 1),
                ItemStack.of(Material.DIAMOND_LEGGINGS, 1),
                ItemStack.of(Material.DIAMOND_LEGGINGS, 1)
        };

        var inventoryToSortHorizontally = spy(Inventory.class);
        var itemStacksHorizontal = setupFinalOrganisedInventory(inventorySorter, inventoryToSortHorizontally, items);
        assertSameContents(itemStacksHorizontal, inventorySorter, inventoryToSortHorizontally);

        var inventoryToSortVertically = spy(Inventory.class);
        var itemStacksVertical = setupFinalOrganisedInventory(inventorySorter, inventoryToSortVertically, items);
        assertSameContents(itemStacksVertical, inventorySorter, inventoryToSortVertically);
    }

    private static void assertSameContents(List<ItemStack> stacks, InventorySorter inventorySorter, Inventory inventory) {
        assertThat(stacks.stream()
                .filter(Objects::nonNull))
                .containsExactlyInAnyOrder(inventorySorter.getOrganisedGroups(inventory)
                        .stream().map(MaterialItemStack::itemStacks)
                        .flatMap(Collection::stream)
                        .toArray(ItemStack[]::new));
    }

    private List<ItemStack> setupFinalOrganisedInventory(ItemStack... itemStacks) {
        return setupFinalOrganisedInventory(new InventorySorter(), itemStacks);
    }

    private List<ItemStack> setupFinalOrganisedInventory(InventorySorter inventorySorter, ItemStack... itemStacks) {
        return setupFinalOrganisedInventory(inventorySorter, spy(Inventory.class), itemStacks);
    }

    private List<ItemStack> setupFinalOrganisedInventory(InventorySorter inventorySorter, Inventory inventory, ItemStack... itemStacks) {

        var chest = spy(Chest.class);

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

        Supplier<List<MaterialItemStack>> groupSupplier = () -> inventorySorter.getOrganisedGroups(chest.getInventory());

        return getItemStacks(inventorySorter, groupSupplier.get(), InventorySorter.LARGE_CHEST_NUM_ROWS);
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
