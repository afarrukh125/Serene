package me.plugin.serene.actions.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import me.plugin.serene.actions.PlayerTest;
import me.plugin.serene.actions.inventory.util.GridRenderer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

public class InventorySorterEventTest extends PlayerTest {

    private InventorySorter inventorySorter;
    private Player sorter;
    private World world;
    private Inventory inventory;

    @BeforeEach
    void setUpSorter() {
        inventorySorter = new InventorySorter();
        world = mock(World.class);
        when(world.getUID()).thenReturn(UUID.randomUUID());
        sorter = mockPlayer();
        inventory = inventoryContaining(
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.COBBLESTONE, 12),
                ItemStack.of(Material.DIRT, 3));
    }

    private Player mockPlayer() {
        var mockPlayer = mock(Player.class);
        when(mockPlayer.getWorld()).thenReturn(world);
        when(mockPlayer.isSneaking()).thenReturn(true);
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        return mockPlayer;
    }

    private Inventory inventoryContaining(ItemStack... itemStacks) {
        var mockInventory = spy(Inventory.class);
        var contents = new ItemStack[27];
        System.arraycopy(itemStacks, 0, contents, 0, itemStacks.length);
        when(mockInventory.getContents()).thenReturn(contents);
        when(mockInventory.isEmpty()).thenReturn(itemStacks.length == 0);
        return mockInventory;
    }

    private Block containerBlock(Material material) {
        var block = mock(Block.class);
        when(block.getType()).thenReturn(material);
        when(block.getLocation()).thenReturn(new Location(world, 10, 64, -30));
        if (material == Material.BARREL) {
            var barrel = mock(Barrel.class);
            when(barrel.getInventory()).thenReturn(inventory);
            when(block.getState()).thenReturn(barrel);
        } else {
            var chest = mock(Chest.class);
            when(chest.getInventory()).thenReturn(inventory);
            when(block.getState()).thenReturn(chest);
        }
        return block;
    }

    private PlayerInteractEvent event(Player player, Block block, Action action, ItemStack heldItem) {
        return new PlayerInteractEvent(player, action, heldItem, block, null, EquipmentSlot.HAND);
    }

    private PlayerInteractEvent featherRightClickOn(Block block) {
        return featherRightClickOn(sorter, block);
    }

    private PlayerInteractEvent featherRightClickOn(Player player, Block block) {
        return event(player, block, Action.RIGHT_CLICK_BLOCK, ItemStack.of(Material.FEATHER, 1));
    }

    private List<ItemStack[]> captureSetContents() {
        var captured = new ArrayList<ItemStack[]>();
        doAnswer((Answer<Void>) invocation -> {
                    captured.add(((ItemStack[]) invocation.getArgument(0)).clone());
                    return null;
                })
                .when(inventory)
                .setContents(any());
        return captured;
    }

    @Test
    void sortsAChestWhenSneakingAndRightClickingWithAFeather() {
        inventorySorter.handleEvent(featherRightClickOn(containerBlock(Material.CHEST)));

        var contents = ArgumentCaptor.forClass(ItemStack[].class);
        verify(inventory).setContents(contents.capture());
        assertThat(GridRenderer.renderWithAmounts(Arrays.asList(contents.getValue()))
                        .lines()
                        .findFirst())
                .hasValue("STONE:64 DIRT:3 COBBLESTONE:12 - - - - - -");
        verify(world).playSound(any(Location.class), any(Sound.class), anyFloat(), anyFloat());
    }

    @Test
    void doesNothingWithoutAFeather() {
        inventorySorter.handleEvent(event(sorter, containerBlock(Material.CHEST), Action.RIGHT_CLICK_BLOCK, null));
        inventorySorter.handleEvent(event(
                sorter, containerBlock(Material.CHEST), Action.RIGHT_CLICK_BLOCK, ItemStack.of(Material.STICK, 1)));

        verify(inventory, never()).setContents(any());
    }

    @Test
    void doesNothingWhenNotSneaking() {
        when(sorter.isSneaking()).thenReturn(false);

        inventorySorter.handleEvent(featherRightClickOn(containerBlock(Material.CHEST)));

        verify(inventory, never()).setContents(any());
    }

    @Test
    void doesNothingOnLeftClick() {
        inventorySorter.handleEvent(event(
                sorter, containerBlock(Material.CHEST), Action.LEFT_CLICK_BLOCK, ItemStack.of(Material.FEATHER, 1)));

        verify(inventory, never()).setContents(any());
    }

    @Test
    void doesNothingForBlocksThatAreNotSortableContainers() {
        inventorySorter.handleEvent(featherRightClickOn(containerBlock(Material.FURNACE)));
        inventorySorter.handleEvent(featherRightClickOn(containerBlock(Material.STONE)));

        verify(inventory, never()).setContents(any());
    }

    @Test
    void doesNothingWhenThereIsNoClickedBlock() {
        inventorySorter.handleEvent(featherRightClickOn(sorter, null));

        verify(inventory, never()).setContents(any());
    }

    @Test
    void doesNothingForAnEmptyContainer() {
        inventory = inventoryContaining();

        inventorySorter.handleEvent(featherRightClickOn(containerBlock(Material.CHEST)));

        verify(inventory, never()).setContents(any());
    }

    @Test
    void sortsBarrelsTrappedChestsAndShulkerBoxes() {
        for (var material : new Material[] {Material.BARREL, Material.TRAPPED_CHEST, Material.RED_SHULKER_BOX}) {
            inventory = inventoryContaining(ItemStack.of(Material.STONE, 64), ItemStack.of(Material.DIRT, 3));

            inventorySorter.handleEvent(featherRightClickOn(containerBlock(material)));

            verify(inventory, times(1)).setContents(any());
        }
    }

    @Test
    void repeatedSortsOfTheSameChestAlternateBetweenRowsAndColumns() {
        inventory = inventoryContaining(ItemStack.of(Material.STONE, 64), ItemStack.of(Material.DIRT, 3));
        var block = containerBlock(Material.CHEST);
        var captured = captureSetContents();

        inventorySorter.handleEvent(featherRightClickOn(block));
        inventorySorter.handleEvent(featherRightClickOn(block));

        assertThat(captured).hasSize(2);
        assertThat(captured.get(0)[1])
                .as("row major puts the second stack beside the first")
                .isNotNull();
        assertThat(captured.get(1)[1]).as("column major leaves slot 1 empty").isNull();
        assertThat(captured.get(1)[9])
                .as("column major puts the second stack below the first")
                .isNotNull();
    }

    @Test
    void twoPlayersUsingTheSameEnderChestBlockDoNotShareALayout() {
        inventory = inventoryContaining(ItemStack.of(Material.STONE, 64), ItemStack.of(Material.DIRT, 3));
        var enderChest = containerBlock(Material.ENDER_CHEST);
        var otherPlayer = mockPlayer();
        when(sorter.getEnderChest()).thenReturn(inventory);
        when(otherPlayer.getEnderChest()).thenReturn(inventory);
        var captured = captureSetContents();

        inventorySorter.handleEvent(featherRightClickOn(sorter, enderChest));
        inventorySorter.handleEvent(featherRightClickOn(otherPlayer, enderChest));

        assertThat(captured).hasSize(2);
        assertThat(captured.get(0)[1])
                .as("first player's first sort is row major")
                .isNotNull();
        assertThat(captured.get(1)[1])
                .as("second player's first sort is also row major, not a flip of the first player's")
                .isNotNull();
    }

    @Test
    void remembersOnlyABoundedNumberOfContainers() {
        var sortModeMemory = new SortModeMemory();

        for (var i = 0; i < SortModeMemory.MAX_REMEMBERED * 2; i++) {
            sortModeMemory.nextFor(new me.plugin.serene.model.SortTarget(UUID.randomUUID(), i, 0, 0));
        }

        assertThat(sortModeMemory.size()).isEqualTo(SortModeMemory.MAX_REMEMBERED);
    }
}
