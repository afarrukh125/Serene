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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
                    Map<Material, List<ItemStack>> organisedMaterialGroups = getOrganisedGroups(chest);
                    ItemStack[] contents = chest.getInventory().getContents();
                    int rowSize = 9;
                    int colSize = contents.length == SMALL_CHEST_SIZE ? SMALL_CHEST_COL_SIZE : LARGE_CHEST_COL_SIZE;
                    ItemStack[] newItemStacks = generateFinalSortedItemStacks(contents.length, organisedMaterialGroups, rowSize, colSize);
                    chest.getInventory().setContents(newItemStacks);
                }
            }
        }
    }

    private ItemStack[] generateFinalSortedItemStacks(int numStacks,
                                                      Map<Material, List<ItemStack>> organisedMaterialGroups,
                                                      int rowSize,
                                                      int colSize) {
        ItemStack[] newStacks = new ItemStack[numStacks];

        List<Material> materials = new ArrayList<>(organisedMaterialGroups.keySet());
        materials.sort(comparingInt(m -> organisedMaterialGroups.get(m).stream().mapToInt(ItemStack::getAmount).sum()).reversed());
        Set<Material> dumpedMaterials = new HashSet<>();
        for (Material material : materials) {
            List<ItemStack> itemStacks = organisedMaterialGroups.get(material);
            boolean placed = false;
            for (int i = 0; i < newStacks.length; i++) {
                if (canFitVertically(itemStacks.size(), newStacks, i, rowSize)) {
                    populateVertically(itemStacks, newStacks, i, rowSize);
                    placed = true;
                    break;
                } else if (canFitHorizontally(itemStacks.size(), newStacks, i)) {
                    populateHorizontally(itemStacks, newStacks, i);
                    placed = true;
                    break;
                }
            }
            if (!placed)
                dumpedMaterials.add(material);
        }
        for (Material material : dumpedMaterials) {
            Iterator<ItemStack> iterator = organisedMaterialGroups.get(material).iterator();
            for (int i = 0; i < newStacks.length && iterator.hasNext(); i++) {
                if (newStacks[i] == null) {
                    newStacks[i] = iterator.next();
                }
            }
        }
        return newStacks;
    }

    private boolean canFitHorizontally(int numStacks, ItemStack[] itemStacks, int index) {
        return canFit(numStacks, itemStacks, index, 1);
    }

    private void populateHorizontally(List<ItemStack> itemStacks, ItemStack[] newStacks, int index) {
        populate(itemStacks, newStacks, index, 1);
    }

    private boolean canFitVertically(int numStacks,
                                     ItemStack[] itemStacks,
                                     int index,
                                     int rowSize) {
        return canFit(numStacks, itemStacks, index, rowSize);
    }

    private void populateVertically(List<ItemStack> itemStacks, ItemStack[] newStacks, int index, int rowSize) {
        populate(itemStacks, newStacks, index, rowSize);
    }

    private boolean canFit(int numStacks, ItemStack[] itemStacks, int index, int increment) {
        for (int i = index; i <= index + (numStacks * increment); i += increment) {
            if (i < 0 || i >= itemStacks.length)
                return false;
            if (itemStacks[i] != null)
                return false;
        }
        return true;
    }

    private void populate(List<ItemStack> itemStacks, ItemStack[] newStacks, int index, int increment) {
        int numStacks = itemStacks.size();
        Iterator<ItemStack> iterator = itemStacks.iterator();
        for (int i = index; i <= index + (numStacks * increment); i += increment) {
            if (!iterator.hasNext())
                break;
            newStacks[i] = iterator.next();
        }
    }

    private Map<Material, List<ItemStack>> getOrganisedGroups(Chest chest) {
        Inventory inventory = chest.getInventory();
        Map<Material, List<ItemStack>> itemsToStacks = Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(ItemStack::getType));

        Map<Material, List<ItemStack>> reorganisedStacks = new HashMap<>();
        for (Material material : itemsToStacks.keySet()) {
            List<ItemStack> allStacks = new ArrayList<>();
            Map<ItemMeta, List<ItemStack>> groupedByMeta = itemsToStacks.get(material).stream().collect(Collectors.groupingBy(ItemStack::getItemMeta));
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
}

