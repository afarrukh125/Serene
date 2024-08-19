package me.plugin.serene.actions;

import com.google.common.annotations.VisibleForTesting;
import me.plugin.serene.model.Coordinate;
import me.plugin.serene.model.MaterialItemStack;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
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
        reorganisedStacks.sort(
                (o1, o2) -> o2.itemStacks().size() - o1.itemStacks().size());
        return reorganisedStacks;
    }

    // Places organised groups into final sorted items
    @VisibleForTesting
    ItemStack[] generateFinalSortedItemStacks(
            List<MaterialItemStack> materialItemStacks, int numRows, Location location) {
        var newStacks = new ItemStack[numRows][ROW_SIZE];

        List<MaterialItemStack> notPlaced = new ArrayList<>();

        if (seenLocations.contains(location)) {
            alternatePrioritisingHorizontal(materialItemStacks, newStacks, notPlaced);
            alternatePrioritisingVertical(materialItemStacks, newStacks, notPlaced);
            seenLocations.remove(location);
        } else {
            materialItemStacks.addAll(notPlaced);
            alternatePrioritisingVertical(materialItemStacks, newStacks, notPlaced);
            alternatePrioritisingHorizontal(materialItemStacks, newStacks, notPlaced);
            seenLocations.add(location);
        }
        if (!notPlaced.isEmpty()) {
            dumpRemaining(newStacks, notPlaced);
        }
        return flatten(newStacks);
    }

    private void alternatePrioritisingHorizontal(
            List<MaterialItemStack> materialItemStacks, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        int x = 0;
        for (var materialItemStack : filterStacks(materialItemStacks, itemStacksFitInRow())) {
            if (x % 2 == 0) {
                populateHorizontally(materialItemStack, newStacks, notPlaced);
            } else {
                populateVertically(materialItemStack, newStacks, notPlaced);
            }
            x++;
        }
        notPlaced.addAll(filterStacks(materialItemStacks, itemStacksExceedRowSize()));
    }

    private void alternatePrioritisingVertical(
            List<MaterialItemStack> materialItemStacks, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        int x = 0;
        for (var materialItemStack : filterStacks(materialItemStacks, itemStacksFitInRow())) {
            if (x % 2 == 0) {
                populateVertically(materialItemStack, newStacks, notPlaced);
            } else {
                populateHorizontally(materialItemStack, newStacks, notPlaced);
            }
            x++;
        }
        notPlaced.addAll(materialItemStacks.stream().filter(itemStacksExceedRowSize()).toList());
    }

    private static Predicate<MaterialItemStack> itemStacksFitInRow() {
        return materialItemStack -> materialItemStack.itemStacks().size() <= ROW_SIZE;
    }

    private static Predicate<MaterialItemStack> itemStacksExceedRowSize() {
        return materialItemStack -> materialItemStack.itemStacks().size() > ROW_SIZE;
    }

    private static List<MaterialItemStack> filterStacks(List<MaterialItemStack> materialItemStacks, Predicate<MaterialItemStack> materialItemStackPredicate) {
        return materialItemStacks.stream().filter(materialItemStackPredicate).toList();
    }

    private void populateHorizontally(
            MaterialItemStack materialItemStack, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        var itemStacks = materialItemStack.itemStacks();
        if (itemStacks.isEmpty()) {
            return;
        }
        var done = false;
        for (var x = newStacks[0].length - 1; x >= 0; x--) {
            for (var y = 0; y < newStacks.length; y++) {
                if (newStacks[y][x] != null) {
                    continue;
                }
                var horizontalCoordinates = canFitHorizontally(itemStacks.size(), newStacks, x, y);
                if (!horizontalCoordinates.isEmpty()) {
                    populate(itemStacks, newStacks, horizontalCoordinates);
                    done = true;
                    break;
                }
            }
            if (done) {
                break;
            }
        }
        if (!done) {
            notPlaced.add(materialItemStack);
        }
    }

    private void populateVertically(
            MaterialItemStack materialItemStack, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        var itemStacks = materialItemStack.itemStacks();
        if (itemStacks.isEmpty()) {
            return;
        }
        var done = false;
        for (var x = 0; x < newStacks[0].length; x++) {
            for (var y = 0; y < newStacks.length; y++) {
                if (newStacks[y][x] != null) continue;
                var verticalCoordinates = canFitVertically(itemStacks.size(), newStacks, x, y);
                if (!verticalCoordinates.isEmpty()) {
                    populate(itemStacks, newStacks, verticalCoordinates);
                    done = true;
                    break;
                }
            }
            if (done) {
                break;
            }
        }
        if (!done) {
            notPlaced.add(materialItemStack);
        }
    }

    private List<Coordinate> canFitVertically(int size, ItemStack[][] newStacks, int startX, int startY) {
        if (newStacks.length - startY < size) {
            return emptyList();
        }
        var coordinates = new ArrayList<Coordinate>();
        for (var y = startY; y < newStacks.length; y++) {
            if (newStacks[y][startX] != null) {
                return emptyList();
            } else {
                coordinates.add(new Coordinate(startX, y));
            }
        }
        return coordinates;
    }

    private List<Coordinate> canFitHorizontally(int size, ItemStack[][] newStacks, int startX, int startY) {
        if (newStacks[startY].length - startX < size) {
            return emptyList();
        }
        var coordinates = new ArrayList<Coordinate>();
        for (var x = newStacks[0].length - 1; x >= startX; x--) {
            if (newStacks[startY][x] != null) {
                return emptyList();
            } else {
                coordinates.add(new Coordinate(x, startY));
            }
        }
        return coordinates;
    }

    private void populate(Queue<ItemStack> itemStacks, ItemStack[][] newStacks, List<Coordinate> coordinates) {
        for (var coordinate : coordinates) {
            newStacks[coordinate.y()][coordinate.x()] = itemStacks.poll();
        }
    }

    private void dumpRemaining(ItemStack[][] newStacks, List<MaterialItemStack> couldntBePlaced) {
        for (var materialItemStack : couldntBePlaced) {
            var stacks = materialItemStack.itemStacks();
            for (var i = newStacks.length - 1; i >= 0; i--) {
                for (var j = newStacks[i].length - 1; j >= 0 && !stacks.isEmpty(); j--) {
                    if (newStacks[i][j] == null) {
                        newStacks[i][j] = stacks.poll();
                    }
                }
            }
        }
    }

    private ItemStack[] flatten(ItemStack[][] newStacks) {
        var allStacks = new ArrayList<ItemStack>();
        for (var newStack : newStacks) {
            allStacks.addAll(Arrays.asList(newStack).subList(0, newStacks[0].length));
        }
        return allStacks.toArray(ItemStack[]::new);
    }
}
