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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
                    ItemStack[] newItemStack = generateFinalSortedItemStacks(contents.length, organisedMaterialGroups, rowSize, colSize);
                }
            }
        }
    }

    private ItemStack[] generateFinalSortedItemStacks(int numStacks,
                                                      Map<Material, List<ItemStack>> organisedMaterialGroups,
                                                      int rowSize,
                                                      int colSize) {
        ItemStack[] newStacks = new ItemStack[numStacks];
        return null;
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

