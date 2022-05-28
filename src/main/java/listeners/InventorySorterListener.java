package listeners;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;

public class InventorySorterListener implements Listener {

    private static final Logger LOG = LoggerFactory.getLogger(InventorySorterListener.class);
    public static final int LARGE_CHEST_COL_SIZE = 6;
    public static final int SMALL_CHEST_COL_SIZE = 3;
    public static final int SMALL_CHEST_SIZE = 27;

    @EventHandler
    public void onChestOpenEvent(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getClickedBlock() != null) {
            boolean wasChest = requireNonNull(playerInteractEvent.getClickedBlock()).getType().equals(Material.CHEST);
            if (wasChest) {
                boolean sneaking = playerInteractEvent.getPlayer().isSneaking();
                boolean rightClicked = playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK);
                boolean usedFeather = playerInteractEvent.hasItem() && requireNonNull(playerInteractEvent.getItem()).getType().equals(Material.FEATHER);
                if (rightClicked && usedFeather && sneaking) {
                    Chest chest = (Chest) playerInteractEvent.getClickedBlock().getState();
                    Map<Material, Queue<ItemStack>> organisedMaterialGroups = getOrganisedGroups(chest);
                    ItemStack[] contents = chest.getInventory().getContents();
                    int rowSize = 9;
                    int colSize = contents.length == SMALL_CHEST_SIZE ? SMALL_CHEST_COL_SIZE : LARGE_CHEST_COL_SIZE;
                    ItemStack[] newItemStacks = generateFinalSortedItemStacks(organisedMaterialGroups, rowSize, colSize);
                    chest.getInventory().setContents(newItemStacks);
                }
            }
        }
    }

    private Map<Material, Queue<ItemStack>> getOrganisedGroups(Chest chest) {
        Inventory inventory = chest.getInventory();
        var itemsToStacks = Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(ItemStack::getType));

        Map<Material, Queue<ItemStack>> reorganisedStacks = new HashMap<>();
        for (Material material : itemsToStacks.keySet()) {
            LinkedList<ItemStack> allStacks = new LinkedList<>();
            var groupedByMeta = itemsToStacks.get(material)
                    .stream()
                    .collect(Collectors.groupingBy(ItemStack::getItemMeta));
            for (ItemMeta itemMeta : groupedByMeta.keySet()) {
                int currentCount = 0;
                int maxSize = material.getMaxStackSize();
                LinkedList<ItemStack> itemStacks = new LinkedList<>(groupedByMeta.get(itemMeta));
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
                    ItemStack finalStack = next.clone();
                    finalStack.setAmount(currentCount);
                    allStacks.add(finalStack);
                }
            }
            reorganisedStacks.put(material, allStacks);
        }
        return reorganisedStacks;
    }

    private ItemStack[] generateFinalSortedItemStacks(
            Map<Material, Queue<ItemStack>> organisedMaterialGroups,
            int rowSize,
            int colSize) {
        ItemStack[][] newStacks = new ItemStack[colSize][rowSize];

        List<Material> materials = new ArrayList<>(organisedMaterialGroups.keySet());
        materials.sort(comparingInt(m -> organisedMaterialGroups.get(m).stream().mapToInt(ItemStack::getAmount).sum()).reversed());

        List<Material> dumpMaterials = new ArrayList<>();

        for (Material material : materials) {
            populateHorizontally(organisedMaterialGroups, newStacks, dumpMaterials, material);
        }

        for (Material material : materials) {
            populateVertically(organisedMaterialGroups, newStacks, dumpMaterials, material);
        }
        dumpRemaining(newStacks, dumpMaterials, organisedMaterialGroups);
        return flatten(newStacks);
    }

    private void populateHorizontally(Map<Material, Queue<ItemStack>> organisedMaterialGroups,
                                      ItemStack[][] newStacks,
                                      List<Material> dumpMaterials,
                                      Material material) {
        var itemStacks = organisedMaterialGroups.get(material);
        if (itemStacks.isEmpty())
            return;
        boolean done = false;
        for (int x = 0; x < newStacks[0].length; x++) {
            for (int y = 0; y < newStacks.length; y++) {
                var horizontalCoordinates = canFitHorizontally(itemStacks.size(), newStacks, x, y);
                if (!horizontalCoordinates.isEmpty()) {
                    populate(itemStacks, newStacks, horizontalCoordinates);
                    done = true;
                    break;
                }
            }
            if (done)
                break;
        }
        if (!done) {
            dumpMaterials.add(material);
        }
    }

    private void populateVertically(Map<Material, Queue<ItemStack>> organisedMaterialGroups,
                                    ItemStack[][] newStacks,
                                    List<Material> dumpMaterials,
                                    Material material) {
        var itemStacks = organisedMaterialGroups.get(material);
        if (itemStacks.isEmpty())
            return;
        boolean done = false;
        for (int x = 0; x < newStacks[0].length; x++) {
            for (int y = 0; y < newStacks.length; y++) {
                var verticalCoordinates = canFitVertically(itemStacks.size(), newStacks, x, y);
                if (!verticalCoordinates.isEmpty()) {
                    populate(itemStacks, newStacks, verticalCoordinates);
                    done = true;
                    break;
                }
            }
            if (done)
                break;
        }
        if (!done)
            dumpMaterials.add(material);
    }

    private void dumpRemaining(ItemStack[][] newStacks,
                               List<Material> couldntBePlaced,
                               Map<Material, Queue<ItemStack>> organisedMaterialGroups) {
        for (Material material : couldntBePlaced) {
            var stacks = organisedMaterialGroups.get(material);
            for (int i = 0; i < newStacks.length; i++) {
                for (int j = 0; j < newStacks[i].length && !stacks.isEmpty(); j++) {
                    if (newStacks[i][j] == null)
                        newStacks[i][j] = stacks.poll();
                }
            }
        }
    }

    private List<Coordinate> canFitVertically(int size, ItemStack[][] newStacks, int startX, int startY) {
        if (newStacks.length - startY < size) {
            return Collections.emptyList();
        }
        List<Coordinate> coordinates = new ArrayList<>();
        for (int y = startY; y < newStacks.length; y++) {
            if (newStacks[y][startX] != null) {
                return Collections.emptyList();
            } else {
                coordinates.add(new Coordinate(startX, y));
            }
        }
        return coordinates;
    }

    private void populate(Queue<ItemStack> itemStacks, ItemStack[][] newStacks, List<Coordinate> coordinates) {
        for (Coordinate coordinate : coordinates) {
            newStacks[coordinate.y()][coordinate.x()] = itemStacks.poll();
        }
    }

    private List<Coordinate> canFitHorizontally(int size, ItemStack[][] newStacks, int startX, int startY) {
        if (newStacks[startY].length - startX < size) {
            return Collections.emptyList();
        }
        List<Coordinate> coordinates = new ArrayList<>();
        for (int x = startX; x < newStacks[0].length; x++) {
            if (newStacks[startY][x] != null) {
                return Collections.emptyList();
            } else {
                coordinates.add(new Coordinate(x, startY));
            }
        }
        return coordinates;
    }

    private ItemStack[] flatten(ItemStack[][] newStacks) {
        List<ItemStack> allStacks = new ArrayList<>();
        for (int i = 0; i < newStacks.length; i++) {
            for (int j = 0; j < newStacks[0].length; j++) {
                allStacks.add(newStacks[i][j]);
            }
        }
        return allStacks.toArray(ItemStack[]::new);
    }
}

