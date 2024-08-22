package me.plugin.serene.actions.inventory;

import com.google.common.annotations.VisibleForTesting;
import me.plugin.serene.model.MaterialItemStack;
import me.plugin.serene.util.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;

public class InventorySorter {

    public static final int ROW_SIZE = 9;
    public static final int LARGE_CHEST_NUM_ROWS = 6;
    public static final int SMALL_CHEST_NUM_ROW = 3;
    public static final int SMALL_CHEST_SIZE = 27;
    private final Set<Location> seenLocations = new HashSet<>();
    private static final Set<Material> CHEST_MATERIALS = Set.of(Material.CHEST, Material.ENDER_CHEST);

    public void handleEvent(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getClickedBlock() != null) {
            var block = requireNonNull(playerInteractEvent.getClickedBlock());
            var player = playerInteractEvent.getPlayer();
            var wasChest = CHEST_MATERIALS.contains(block.getType());
            if (wasChest) {
                var sneaking = player.isSneaking();
                var rightClicked = playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK);
                var usedFeather = playerInteractEvent.hasItem()
                        && requireNonNull(playerInteractEvent.getItem())
                                .getType()
                                .equals(Material.FEATHER);
                if (rightClicked && usedFeather && sneaking) {
                    var inventory = translateInventoryFromBlockType(block, player);
                    if (!inventory.isEmpty()) {
                        var organisedMaterialGroups = getOrganisedGroups(inventory);
                        var location = block.getLocation();
                        var numRows = inventory.getContents().length == SMALL_CHEST_SIZE
                                ? SMALL_CHEST_NUM_ROW
                                : LARGE_CHEST_NUM_ROWS;
                        var newItemStacks = generateFinalSortedItemStacks(organisedMaterialGroups, numRows, location);
                        inventory.setContents(newItemStacks);
                        player.getWorld().playSound(requireNonNull(location), Sound.BLOCK_CONDUIT_ACTIVATE, 0.7f, 1);
                    }
                }
            }
        }
    }

    private Inventory translateInventoryFromBlockType(Block block, Player player) {
        var type = block.getType();
        return switch (type) {
            case CHEST -> ((Chest) block.getState()).getInventory();
            case ENDER_CHEST -> player.getEnderChest();
            default -> throw new IllegalArgumentException(
                    "Unknown block type to translate to inventory %s".formatted(block.getType()));
        };
    }

    // Collates all unorganised items into groups
    @VisibleForTesting
    List<MaterialItemStack> getOrganisedGroups(Inventory inventory) {
        var itemsToStacks =
                Arrays.stream(inventory.getContents()).filter(Objects::nonNull).collect(groupingBy(ItemStack::getType));

        var reorganisedStacks = new ArrayList<MaterialItemStack>();
        for (var material : itemsToStacks.keySet()) {
            var allStacks = new LinkedList<ItemStack>();
            var groupedByMeta = itemsToStacks.get(material).stream().collect(groupingBy(ItemStack::getItemMeta));
            for (var itemMeta : groupedByMeta.keySet()) {
                var currentCount = 0;
                var maxSize = material.getMaxStackSize();
                var itemStacks = new LinkedList<>(groupedByMeta.get(itemMeta));
                ItemStack next = null;
                while (!itemStacks.isEmpty()) {
                    next = itemStacks.poll();
                    if (next.getAmount() == maxSize) {
                        allStacks.add(next);
                    } else {
                        currentCount += next.getAmount();
                        if (currentCount >= maxSize) {
                            next.setAmount(maxSize);
                            allStacks.add(next);
                            currentCount = currentCount - maxSize;
                        }
                    }
                }
                if (currentCount > 0) {
                    var finalStack = next.clone();
                    finalStack.setAmount(currentCount);
                    allStacks.add(finalStack);
                }
            }
            reorganisedStacks.add(new MaterialItemStack(material, allStacks));
        }

        // Place the biggest stacks first
        sortBySizeThenName(reorganisedStacks);
        return reorganisedStacks;
    }

    @VisibleForTesting
    ItemStack[] generateFinalSortedItemStacks(
            List<MaterialItemStack> materialItemStacks, int numRows, Location location) {
        List<MaterialItemStack> notPlaced = new ArrayList<>();

        SortingStrategy sortingStrategy;
        if (seenLocations.contains(location)) {
            sortingStrategy = new PrioritisingVerticalSortingStrategy();
            seenLocations.remove(location);
        } else {
            sortingStrategy = new PrioritisingHorizontalSortingStrategy();
            seenLocations.add(location);
        }
        return flatten(sortingStrategy.sort(materialItemStacks, new ItemStack[numRows][ROW_SIZE], notPlaced));
    }

    private ItemStack[] flatten(ItemStack[][] itemStacks) {
        return Utils.flatten(itemStacks, ItemStack[]::new);
    }

    private static void sortBySizeThenName(List<MaterialItemStack> reorganisedStacks) {
        reorganisedStacks.sort(Comparator.<MaterialItemStack, Integer>comparing(
                        materialItemStack -> materialItemStack.itemStacks().size())
                .reversed()
                .thenComparing(materialItemStack -> materialItemStack.material().name()));
    }
}
