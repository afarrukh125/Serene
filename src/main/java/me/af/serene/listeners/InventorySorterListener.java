package me.af.serene.listeners;

import me.af.serene.model.Coordinate;
import me.af.serene.model.MaterialItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Runtime.getRuntime;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;

public class InventorySorterListener implements Listener {

    public static final int ROW_SIZE = 9;
    public static final int LARGE_CHEST_COL_SIZE = 6;
    public static final int SMALL_CHEST_COL_SIZE = 3;
    public static final int SMALL_CHEST_SIZE = 27;
    private final Set<Location> seenChestLocations = new HashSet<>();

    @EventHandler
    public void onChestOpenEvent(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getClickedBlock() != null) {
            boolean wasChest = requireNonNull(playerInteractEvent.getClickedBlock()).getType().equals(Material.CHEST);
            if (wasChest) {
                boolean sneaking = playerInteractEvent.getPlayer().isSneaking();
                boolean rightClicked = playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK);
                boolean usedFeather = playerInteractEvent.hasItem() && requireNonNull(playerInteractEvent.getItem()).getType().equals(Material.FEATHER);
                if (rightClicked && usedFeather && sneaking) {
                    var chest = (Chest) playerInteractEvent.getClickedBlock().getState();
                    var organisedMaterialGroups = getOrganisedGroups(chest);
                    var inventory = chest.getInventory();
                    var location = inventory.getLocation();
                    int colSize = inventory.getContents().length == SMALL_CHEST_SIZE ? SMALL_CHEST_COL_SIZE : LARGE_CHEST_COL_SIZE;
                    var newItemStacks = generateFinalSortedItemStacks(organisedMaterialGroups,
                            colSize,
                            location);
                    inventory.setContents(newItemStacks);
                    playerInteractEvent.getPlayer().getWorld().playSound(requireNonNull(location),
                            Sound.BLOCK_CONDUIT_ACTIVATE,
                            1,
                            1);
                }
            }
        }
    }

    // Collates all unorganised items into groups
    private List<MaterialItemStack> getOrganisedGroups(Chest chest) {
        Inventory inventory = chest.getInventory();
        var itemsToStacks = Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .collect(groupingBy(ItemStack::getType));

        ExecutorService executorService = Executors.newFixedThreadPool(getRuntime().availableProcessors() * 2);
        List<MaterialItemStack> reorganisedStacks = Collections.synchronizedList(new ArrayList<>());
        for (var material : itemsToStacks.keySet()) {
            executorService.execute(() -> {
                LinkedList<ItemStack> allStacks = new LinkedList<>();
                var groupedByMeta = itemsToStacks.get(material)
                        .stream()
                        .collect(groupingBy(ItemStack::getItemMeta));
                for (ItemMeta itemMeta : groupedByMeta.keySet()) {
                    int currentCount = 0;
                    int maxSize = material.getMaxStackSize();
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
                        ItemStack finalStack = next.clone();
                        finalStack.setAmount(currentCount);
                        allStacks.add(finalStack);
                    }
                }
                reorganisedStacks.add(new MaterialItemStack(material, allStacks));
            });
        }

        executorService.shutdown();
        try {
            //noinspection ResultOfMethodCallIgnored
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Place the biggest stacks first
        reorganisedStacks.sort((o1, o2) -> o2.itemStacks().size() - o1.itemStacks().size());
        return reorganisedStacks;
    }

    // Places organised groups into final sorted items
    private ItemStack[] generateFinalSortedItemStacks(
            List<MaterialItemStack> materialItemStacks,
            int colSize,
            Location location) {
        var newStacks = new ItemStack[colSize][ROW_SIZE];

        List<MaterialItemStack> notPlaced = new ArrayList<>();

        if (seenChestLocations.contains(location)) {
            alternatePrioritisingHorizontal(materialItemStacks, newStacks, notPlaced);
            alternatePrioritisingVertical(materialItemStacks, newStacks, notPlaced);
            seenChestLocations.remove(location);
        } else {
            materialItemStacks.addAll(notPlaced);
            alternatePrioritisingVertical(materialItemStacks, newStacks, notPlaced);
            alternatePrioritisingHorizontal(materialItemStacks, newStacks, notPlaced);
            seenChestLocations.add(location);
        }
        if (!notPlaced.isEmpty()) {
            dumpRemaining(newStacks, notPlaced);
        }
        return flatten(newStacks);
    }

    private void alternatePrioritisingHorizontal(List<MaterialItemStack> materialItemStacks, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        int x = 0;
        for (var materialItemStack : materialItemStacks) {
            if (x % 2 == 0) {
                populateHorizontally(materialItemStack, newStacks, notPlaced);
            } else {
                populateVertically(materialItemStack, newStacks, notPlaced);
            }
            x++;
        }
    }

    private void alternatePrioritisingVertical(List<MaterialItemStack> materialItemStacks, ItemStack[][] newStacks, List<MaterialItemStack> notPlaced) {
        int x;
        x = 0;
        for (var materialItemStack : materialItemStacks) {
            if (x % 2 == 0) {
                populateVertically(materialItemStack, newStacks, notPlaced);
            } else {
                populateHorizontally(materialItemStack, newStacks, notPlaced);
            }
            x++;
        }
    }

    private void populateHorizontally(MaterialItemStack materialItemStack,
                                      ItemStack[][] newStacks,
                                      List<MaterialItemStack> notPlaced) {
        var itemStacks = materialItemStack.itemStacks();
        if (itemStacks.isEmpty())
            return;
        boolean done = false;
        for (int x = 0; x < newStacks.length; x++) {
            for (int y = 0; y < newStacks.length; y++) {
                if (newStacks[y][x] != null)
                    continue;
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

    private void populateVertically(MaterialItemStack materialItemStack,
                                    ItemStack[][] newStacks,
                                    List<MaterialItemStack> notPlaced) {
        var itemStacks = materialItemStack.itemStacks();
        if (itemStacks.isEmpty())
            return;
        boolean done = false;
        for (int x = 0; x < newStacks[0].length; x++) {
            for (int y = 0; y < newStacks.length; y++) {
                if (newStacks[y][x] != null)
                    continue;
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
        List<Coordinate> coordinates = new ArrayList<>();
        for (int y = startY; y < newStacks.length; y++) {
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
        List<Coordinate> coordinates = new ArrayList<>();
        for (int x = startX; x < newStacks[0].length; x++) {
            if (newStacks[startY][x] != null) {
                return emptyList();
            } else {
                coordinates.add(new Coordinate(x, startY));
            }
        }
        return coordinates;
    }

    private void populate(Queue<ItemStack> itemStacks, ItemStack[][] newStacks, List<Coordinate> coordinates) {
        for (int i = 0; i < coordinates.size(); i++) {
            var coordinate = coordinates.get(i);
            newStacks[coordinate.y()][coordinate.x()] = itemStacks.poll();
        }
    }

    private void dumpRemaining(ItemStack[][] newStacks,
                               List<MaterialItemStack> couldntBePlaced) {
        for (var materialItemStack : couldntBePlaced) {
            var stacks = materialItemStack.itemStacks();
            for (int i = newStacks.length - 1; i >= 0; i--) {
                for (int j = newStacks[i].length - 1; j >= 0 && !stacks.isEmpty(); j--) {
                    if (newStacks[i][j] == null) {
                        newStacks[i][j] = stacks.poll();
                    }
                }
            }
        }
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

