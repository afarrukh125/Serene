package me.plugin.serene.actions.inventory;

import static me.plugin.serene.actions.inventory.CompactingSortingStrategy.FillDirection.COLUMN_MAJOR;
import static me.plugin.serene.actions.inventory.CompactingSortingStrategy.FillDirection.ROW_MAJOR;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import me.plugin.serene.actions.PlayerTest;
import me.plugin.serene.actions.inventory.util.GridRenderer;
import me.plugin.serene.actions.inventory.util.InventoryUtils;
import me.plugin.serene.actions.inventory.util.SampleInventories;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

public class CompactingSortingStrategyTest extends PlayerTest {

    private static List<ItemStack> layOut(
            CompactingSortingStrategy.FillDirection direction, int numRows, ItemStack... items) {
        var sorter = new InventorySorter();
        var groups = sorter.getOrganisedGroups(InventoryUtils.inventoryOf(items));
        var grid = new CompactingSortingStrategy(direction)
                .sort(groups, new ItemStack[numRows][InventorySorter.ROW_SIZE], new ArrayList<>());
        return Arrays.asList(me.plugin.serene.util.Utils.flatten(grid, ItemStack[]::new));
    }

    @Test
    void rowMajorFillsInReadingOrderWithNoGaps() {
        var slots = layOut(ROW_MAJOR, 6, SampleInventories.stoneHaul());

        assertThat(GridRenderer.render(slots)).isEqualTo("""
                        A A A A A A A A A
                        A A A A A B C C C
                        C C C D D D D D E
                        E F G H I J J . .
                        . . . . . . . . .
                        . . . . . . . . .
                        A = STONE
                        B = STONE_STAIRS
                        C = STONE_SLAB
                        D = SMOOTH_STONE
                        E = STONE_BRICKS
                        F = STONE_BRICK_SLAB
                        G = STONE_BRICK_WALL
                        H = MOSSY_COBBLESTONE
                        I = MOSSY_STONE_BRICKS
                        J = CHISELED_STONE_BRICKS
                        """);
    }

    @Test
    void columnMajorFillsDownColumnsWithNoGaps() {
        var slots = layOut(COLUMN_MAJOR, 6, SampleInventories.stoneHaul());

        assertThat(GridRenderer.render(slots)).isEqualTo("""
                        A A A B C D . . .
                        A A A B C E . . .
                        A A F B G H . . .
                        A A B C G H . . .
                        A A B C I . . . .
                        A A B C J . . . .
                        A = STONE
                        B = STONE_SLAB
                        C = SMOOTH_STONE
                        D = MOSSY_COBBLESTONE
                        E = MOSSY_STONE_BRICKS
                        F = STONE_STAIRS
                        G = STONE_BRICKS
                        H = CHISELED_STONE_BRICKS
                        I = STONE_BRICK_SLAB
                        J = STONE_BRICK_WALL
                        """);
    }

    @Test
    void partialStacksTrailTheFullStacksOfTheSameItem() {
        var slots = layOut(
                ROW_MAJOR,
                3,
                ItemStack.of(Material.STONE, 12),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64));

        assertThat(GridRenderer.renderWithAmounts(slots).lines().findFirst())
                .hasValue("STONE:64 STONE:64 STONE:12 - - - - - -");
    }

    @Test
    void woolComesOutInVanillaDyeOrder() {
        var slots = layOut(
                ROW_MAJOR,
                3,
                ItemStack.of(Material.RED_WOOL, 64),
                ItemStack.of(Material.ORANGE_WOOL, 64),
                ItemStack.of(Material.YELLOW_WOOL, 64),
                ItemStack.of(Material.LIME_WOOL, 64),
                ItemStack.of(Material.LIGHT_BLUE_WOOL, 64),
                ItemStack.of(Material.BLUE_WOOL, 64),
                ItemStack.of(Material.PURPLE_WOOL, 64),
                ItemStack.of(Material.MAGENTA_WOOL, 64),
                ItemStack.of(Material.WHITE_WOOL, 64),
                ItemStack.of(Material.BLACK_WOOL, 64));

        assertThat(slots.stream().filter(Objects::nonNull).map(ItemStack::getType))
                .containsExactly(
                        Material.WHITE_WOOL,
                        Material.ORANGE_WOOL,
                        Material.MAGENTA_WOOL,
                        Material.LIGHT_BLUE_WOOL,
                        Material.YELLOW_WOOL,
                        Material.LIME_WOOL,
                        Material.PURPLE_WOOL,
                        Material.BLUE_WOOL,
                        Material.RED_WOOL,
                        Material.BLACK_WOOL);
    }

    @Test
    void everyLayoutIsGapFreeAlongItsOwnFillOrder() {
        var numRows = 6;
        for (var direction : CompactingSortingStrategy.FillDirection.values()) {
            var slots = layOut(direction, numRows, SampleInventories.stoneHaul());
            var filled = slots.stream().filter(Objects::nonNull).count();

            for (var position = 0; position < slots.size(); position++) {
                var slot = fillOrderToSlot(direction, position, numRows);
                assertThat(slots.get(slot) != null)
                        .as("%s: position %d in fill order (slot %d) occupied", direction, position, slot)
                        .isEqualTo(position < filled);
            }
        }
    }

    private static int fillOrderToSlot(CompactingSortingStrategy.FillDirection direction, int position, int numRows) {
        return switch (direction) {
            case ROW_MAJOR -> position;
            case COLUMN_MAJOR -> (position % numRows) * InventorySorter.ROW_SIZE + (position / numRows);
        };
    }

    @Test
    void mergingDoesNotMutateTheSourceInventoryAndIsRepeatable() {
        var items = new ItemStack[] {
            ItemStack.of(Material.ACACIA_LEAVES, 23),
            ItemStack.of(Material.ACACIA_LEAVES, 23),
            ItemStack.of(Material.ACACIA_LEAVES, 23)
        };
        var inventory = InventoryUtils.inventoryOf(items);
        var sorter = new InventorySorter();

        var first = totalItems(sorter.getOrganisedGroups(inventory));
        var second = totalItems(sorter.getOrganisedGroups(inventory));

        assertThat(first).isEqualTo(69);
        assertThat(second).isEqualTo(first);
        assertThat(Arrays.stream(items).map(ItemStack::getAmount)).containsExactly(23, 23, 23);
    }

    @Test
    void stacksLargerThanTheMaximumAreSplitProperly() {
        var groups = new InventorySorter()
                .getOrganisedGroups(InventoryUtils.inventoryOf(ItemStack.of(Material.COBBLESTONE, 200)));

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).itemStacks().stream().map(ItemStack::getAmount))
                .containsExactly(64, 64, 64, 8);
    }

    @Test
    void anEmptyInventoryProducesAnEmptyGrid() {
        var slots = layOut(ROW_MAJOR, 6);

        assertThat(slots).hasSize(54).containsOnlyNulls();
    }

    @Test
    void anExactlyFullChestUsesEverySlot() {
        var items = new ItemStack[54];
        Arrays.fill(items, ItemStack.of(Material.STONE, 64));

        var slots = layOut(ROW_MAJOR, 6, items);

        assertThat(slots).hasSize(54).doesNotContainNull();
        assertThat(slots.stream().mapToInt(ItemStack::getAmount).sum()).isEqualTo(54 * 64);
    }

    @Test
    void itemsThatCannotFitAreReportedRatherThanDropped() {
        var groups = new InventorySorter().getOrganisedGroups(InventoryUtils.inventoryOf(distinctUnstackables(40)));
        var notPlaced = new ArrayList<MaterialItemStack>();

        new CompactingSortingStrategy(ROW_MAJOR).sort(groups, new ItemStack[3][InventorySorter.ROW_SIZE], notPlaced);

        assertThat(notPlaced.stream()
                        .mapToInt(group -> group.itemStacks().size())
                        .sum())
                .isEqualTo(40 - 27);
    }

    @Test
    void repeatedSortsOfAnAlreadySortedInventoryAreStable() {
        var first = layOut(ROW_MAJOR, 6, SampleInventories.stoneHaul());
        var second =
                layOut(ROW_MAJOR, 6, first.stream().filter(Objects::nonNull).toArray(ItemStack[]::new));

        assertThat(GridRenderer.renderWithAmounts(second)).isEqualTo(GridRenderer.renderWithAmounts(first));
    }

    @Test
    void stacksDifferingOnlyByMetadataKeepAStableOrderAcrossRuns() {
        var renders = new HashSet<String>();
        for (var run = 0; run < 20; run++) {
            renders.add(GridRenderer.renderWithAmounts(layOut(ROW_MAJOR, 3, damagedPickaxes())));
        }

        assertThat(renders).hasSize(1);
    }

    private static ItemStack[] damagedPickaxes() {
        return new ItemStack[] {
            damaged(Material.DIAMOND_PICKAXE, 5),
            damaged(Material.DIAMOND_PICKAXE, 200),
            damaged(Material.DIAMOND_PICKAXE, 0),
            damaged(Material.DIAMOND_PICKAXE, 97),
            damaged(Material.DIAMOND_PICKAXE, 1531)
        };
    }

    private static ItemStack damaged(Material material, int damage) {
        var itemStack = ItemStack.of(material, 1);
        var meta = itemStack.getItemMeta();
        ((org.bukkit.inventory.meta.Damageable) meta).setDamage(damage);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private static ItemStack[] distinctUnstackables(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> damaged(Material.DIAMOND_PICKAXE, index + 1))
                .toArray(ItemStack[]::new);
    }

    @Test
    void oneMaterialsVariantsAreGroupedTogetherRatherThanByForm() {
        var slots = layOut(
                ROW_MAJOR,
                3,
                ItemStack.of(Material.BIRCH_PLANKS, 64),
                ItemStack.of(Material.OAK_STAIRS, 64),
                ItemStack.of(Material.BIRCH_LOG, 64),
                ItemStack.of(Material.OAK_PLANKS, 64),
                ItemStack.of(Material.OAK_LOG, 64),
                ItemStack.of(Material.BIRCH_SLAB, 64),
                ItemStack.of(Material.OAK_SLAB, 64),
                ItemStack.of(Material.BIRCH_STAIRS, 64));

        assertThat(slots.stream().filter(Objects::nonNull).map(ItemStack::getType))
                .containsExactly(
                        Material.OAK_LOG,
                        Material.OAK_PLANKS,
                        Material.OAK_STAIRS,
                        Material.OAK_SLAB,
                        Material.BIRCH_LOG,
                        Material.BIRCH_PLANKS,
                        Material.BIRCH_STAIRS,
                        Material.BIRCH_SLAB);
    }

    @Test
    void aStoneFamilyLeadsWithItsBaseBlock() {
        var slots = layOut(
                ROW_MAJOR,
                3,
                ItemStack.of(Material.STONE_BRICK_WALL, 64),
                ItemStack.of(Material.COBBLESTONE_STAIRS, 64),
                ItemStack.of(Material.STONE_BRICKS, 64),
                ItemStack.of(Material.STONE_SLAB, 64),
                ItemStack.of(Material.COBBLESTONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE_BRICK_STAIRS, 64));

        assertThat(slots.stream().filter(Objects::nonNull).map(ItemStack::getType))
                .containsExactly(
                        Material.STONE,
                        Material.STONE_SLAB,
                        Material.COBBLESTONE,
                        Material.COBBLESTONE_STAIRS,
                        Material.STONE_BRICKS,
                        Material.STONE_BRICK_STAIRS,
                        Material.STONE_BRICK_WALL);
    }

    @Test
    void colourVariantsStayGroupedByFormInDyeOrder() {
        var slots = layOut(
                ROW_MAJOR,
                3,
                ItemStack.of(Material.RED_CARPET, 64),
                ItemStack.of(Material.RED_WOOL, 64),
                ItemStack.of(Material.WHITE_CARPET, 64),
                ItemStack.of(Material.WHITE_WOOL, 64));

        assertThat(slots.stream().filter(Objects::nonNull).map(ItemStack::getType))
                .containsExactly(Material.WHITE_WOOL, Material.RED_WOOL, Material.WHITE_CARPET, Material.RED_CARPET);
    }

    @Test
    void familiesAreActuallyResolvedFromTags() {
        assertThat(ItemFamilies.anchor(Material.OAK_STAIRS)).isEqualTo(ItemFamilies.anchor(Material.OAK_PLANKS));
        assertThat(ItemFamilies.anchor(Material.OAK_STAIRS)).isNotEqualTo(ItemFamilies.anchor(Material.BIRCH_PLANKS));
        assertThat(ItemFamilies.anchor(Material.WHITE_WOOL)).isEqualTo(Material.WHITE_WOOL.ordinal());
    }

    private static long totalItems(List<MaterialItemStack> groups) {
        return groups.stream()
                .flatMap(group -> group.itemStacks().stream())
                .mapToLong(ItemStack::getAmount)
                .sum();
    }
}
