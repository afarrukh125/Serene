package me.plugin.serene.actions.inventory;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import com.google.common.annotations.VisibleForTesting;
import java.util.*;
import me.plugin.serene.model.MaterialItemStack;
import me.plugin.serene.model.SortTarget;
import me.plugin.serene.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventorySorter {

    private static final Logger LOG = LoggerFactory.getLogger(InventorySorter.class);
    public static final int ROW_SIZE = 9;
    public static final int LARGE_CHEST_NUM_ROWS = 6;
    public static final int SMALL_CHEST_NUM_ROW = 3;
    public static final int SMALL_CHEST_SIZE = 27;

    private static final Set<Material> SORTABLE_CONTAINERS = sortableContainers();

    private final SortModeMemory sortModeMemory = new SortModeMemory();

    public void handleEvent(PlayerInteractEvent playerInteractEvent) {
        var block = playerInteractEvent.getClickedBlock();
        if (block == null || !SORTABLE_CONTAINERS.contains(block.getType())) {
            return;
        }
        var player = playerInteractEvent.getPlayer();
        var rightClicked = playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK);
        if (!rightClicked || !player.isSneaking() || !didPlayerUseFeather(playerInteractEvent)) {
            return;
        }

        var inventory = translateInventoryFromBlockType(block, player);
        if (inventory.isEmpty()) {
            return;
        }
        var contentsSize = inventory.getContents().length;
        if (contentsSize % ROW_SIZE != 0) {
            LOG.warn("Refusing to sort a container whose size {} is not a multiple of {}", contentsSize, ROW_SIZE);
            return;
        }

        var organisedMaterialGroups = getOrganisedGroups(inventory);
        var newItemStacks = generateFinalSortedItemStacks(
                organisedMaterialGroups, contentsSize / ROW_SIZE, sortTarget(block, player));

        if (!isEverythingAccountedFor(organisedMaterialGroups, newItemStacks)) {
            LOG.error(
                    "Found an error when comparing sizes of organised stacks and placed stacks. See below and generate a test for this scenario.");
            LOG.error("Could not sort inventory: \n{}", generateFaultyInventoryString(inventory));
            player.sendMessage(ChatColor.RED + "Could not sort this container safely, so nothing was moved.");
            return;
        }

        inventory.setContents(newItemStacks);
        var location = block.getLocation();
        player.getWorld().playSound(requireNonNull(location), Sound.BLOCK_CONDUIT_ACTIVATE, 0.7f, 1);
    }

    private static SortTarget sortTarget(Block block, Player player) {
        return block.getType() == Material.ENDER_CHEST
                ? SortTarget.ofEnderChest(player)
                : SortTarget.ofBlock(block.getLocation());
    }

    private static boolean isEverythingAccountedFor(List<MaterialItemStack> groups, ItemStack[] newItemStacks) {
        var placed = Arrays.stream(newItemStacks).filter(Objects::nonNull).toList();
        var expected =
                groups.stream().flatMap(group -> group.itemStacks().stream()).toList();
        return placed.size() == expected.size() && totalAmount(placed) == totalAmount(expected);
    }

    private static long totalAmount(List<ItemStack> itemStacks) {
        return itemStacks.stream().mapToLong(ItemStack::getAmount).sum();
    }

    private String generateFaultyInventoryString(Inventory originalInventory) {
        return Arrays.stream(originalInventory.getContents())
                .filter(Objects::nonNull)
                .map(itemStack ->
                        "ItemStack.of(Material." + itemStack.getType().name() + ", " + itemStack.getAmount() + ")")
                .collect(joining(",\n"));
    }

    private static boolean didPlayerUseFeather(PlayerInteractEvent playerInteractEvent) {
        return playerInteractEvent.hasItem()
                && requireNonNull(playerInteractEvent.getItem()).getType().equals(Material.FEATHER);
    }

    private Inventory translateInventoryFromBlockType(Block block, Player player) {
        if (block.getType() == Material.ENDER_CHEST) {
            return player.getEnderChest();
        }
        if (block.getState() instanceof Container container) {
            return container.getInventory();
        }
        throw new IllegalArgumentException(
                "Unknown block type to translate to inventory %s".formatted(block.getType()));
    }

    // Collates all unorganised items into groups
    public List<MaterialItemStack> getOrganisedGroups(Inventory inventory) {
        var itemsToStacks = Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .filter(itemStack -> itemStack.getType() != Material.AIR)
                .collect(groupingBy(ItemStack::getType, LinkedHashMap::new, toList()));

        var reorganisedStacks = new ArrayList<MaterialItemStack>();
        itemsToStacks.forEach((material, stacksOfMaterial) -> {
            var allStacks = new ArrayList<ItemStack>();
            var maxSize = Math.max(1, material.getMaxStackSize());
            var groupedByMeta =
                    stacksOfMaterial.stream().collect(groupingBy(ItemStack::getItemMeta, LinkedHashMap::new, toList()));
            groupedByMeta.forEach((itemMeta, itemStacks) -> {
                var remaining =
                        itemStacks.stream().mapToInt(ItemStack::getAmount).sum();
                while (remaining > 0) {
                    var merged = itemStacks.get(0).clone();
                    merged.setAmount(Math.min(remaining, maxSize));
                    allStacks.add(merged);
                    remaining -= merged.getAmount();
                }
            });
            reorganisedStacks.add(new MaterialItemStack(material, allStacks));
        });

        reorganisedStacks.sort(ItemOrder.GROUPS);
        return reorganisedStacks;
    }

    @VisibleForTesting
    public ItemStack[] generateFinalSortedItemStacks(
            List<MaterialItemStack> materialItemStacks, int numRows, SortTarget sortTarget) {
        List<MaterialItemStack> notPlaced = new ArrayList<>();
        var sorted = new CompactingSortingStrategy(sortModeMemory.nextFor(sortTarget))
                .sort(materialItemStacks, new ItemStack[numRows][ROW_SIZE], notPlaced);
        return Utils.flatten(sorted, ItemStack[]::new);
    }

    private static Set<Material> sortableContainers() {
        var containers = EnumSet.of(
                Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.BARREL, Material.SHULKER_BOX);
        Arrays.stream(Material.values())
                .filter(material -> material.name().endsWith("_SHULKER_BOX"))
                .forEach(containers::add);
        return Collections.unmodifiableSet(containers);
    }
}
