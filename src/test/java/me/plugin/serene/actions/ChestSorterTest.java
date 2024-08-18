package me.plugin.serene.actions;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class ChestSorterTest {
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        player = new PlayerMock(MockBukkit.mock(), "player1");
    }

    @AfterEach
    public void tearDown()
    {
        MockBukkit.unmock();
    }

    @Test
    void testSimpleOrganisedGroupGeneration() {
        // given
        var chestSorter = new ChestSorter();

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

    @Test
    void testComplexScenarioWithMultipleStacks() {
        // given
        var chestSorter = new ChestSorter();
        player.getInventory()
                .addItem(
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.ACACIA_LEAVES, 23),
                        ItemStack.of(Material.COBBLESTONE, 42));
        Supplier<List<MaterialItemStack>> groupSupplier = () -> chestSorter.getOrganisedGroups(player.getInventory());

        // then
        assertThat(groupSupplier.get())
                .containsExactlyInAnyOrder(
                        materialItemStacks(Material.ACACIA_LEAVES, 64, 5),
                        materialItemStacks(Material.COBBLESTONE, 42));

        // when
        var itemStacks = getItemStacks(chestSorter, groupSupplier.get());

        // then
        assertThat(itemStacks.get(0)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 64));
        assertThat(itemStacks.get(9)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 5));
        assertThat(itemStacks.get(8)).isEqualTo(ItemStack.of(Material.COBBLESTONE, 42));

        // when
        var itemStacksAgainAtSameLocation = getItemStacks(chestSorter, groupSupplier.get());

        // then
        assertThat(itemStacksAgainAtSameLocation.get(0)).isEqualTo(ItemStack.of(Material.COBBLESTONE, 42));
        assertThat(itemStacksAgainAtSameLocation.get(8)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 64));
        assertThat(itemStacksAgainAtSameLocation.get(7)).isEqualTo(ItemStack.of(Material.ACACIA_LEAVES, 5));
    }

    private List<ItemStack> getItemStacks(ChestSorter chestSorter, List<MaterialItemStack> groups) {
        return Arrays.asList(chestSorter.generateFinalSortedItemStacks(groups, 3, player.getLocation()));
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
