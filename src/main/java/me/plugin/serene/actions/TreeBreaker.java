package me.plugin.serene.actions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static me.plugin.serene.util.Utils.isToolBrokenAfterApplyingDamage;
import static me.plugin.serene.util.Utils.shouldTakeDamage;
import static org.bukkit.Material.ACACIA_LEAVES;
import static org.bukkit.Material.ACACIA_LOG;
import static org.bukkit.Material.BIRCH_LEAVES;
import static org.bukkit.Material.BIRCH_LOG;
import static org.bukkit.Material.CHERRY_LEAVES;
import static org.bukkit.Material.CHERRY_LOG;
import static org.bukkit.Material.COARSE_DIRT;
import static org.bukkit.Material.DARK_OAK_LEAVES;
import static org.bukkit.Material.DARK_OAK_LOG;
import static org.bukkit.Material.DIAMOND_AXE;
import static org.bukkit.Material.DIRT;
import static org.bukkit.Material.DIRT_PATH;
import static org.bukkit.Material.FARMLAND;
import static org.bukkit.Material.GOLDEN_AXE;
import static org.bukkit.Material.GRASS_BLOCK;
import static org.bukkit.Material.IRON_AXE;
import static org.bukkit.Material.JUNGLE_LEAVES;
import static org.bukkit.Material.JUNGLE_LOG;
import static org.bukkit.Material.MYCELIUM;
import static org.bukkit.Material.NETHERITE_AXE;
import static org.bukkit.Material.OAK_LEAVES;
import static org.bukkit.Material.OAK_LOG;
import static org.bukkit.Material.PODZOL;
import static org.bukkit.Material.ROOTED_DIRT;
import static org.bukkit.Material.SPRUCE_LEAVES;
import static org.bukkit.Material.SPRUCE_LOG;
import static org.bukkit.Material.STONE_AXE;
import static org.bukkit.Material.WOODEN_AXE;

public class TreeBreaker {
    private static final Set<Material> LOG_MATERIALS =
            Set.of(ACACIA_LOG, BIRCH_LOG, DARK_OAK_LOG, JUNGLE_LOG, OAK_LOG, SPRUCE_LOG, CHERRY_LOG);

    private static final Set<Material> LEAVES = Set.of(
            ACACIA_LEAVES, BIRCH_LEAVES, DARK_OAK_LEAVES, JUNGLE_LEAVES, OAK_LEAVES, SPRUCE_LEAVES, CHERRY_LEAVES);
    private static final Set<Material> GROWABLE_TREE_DIRT_BLOCKS =
            Set.of(GRASS_BLOCK, DIRT, FARMLAND, PODZOL, DIRT_PATH, MYCELIUM, COARSE_DIRT, ROOTED_DIRT);

    private static final Set<Material> AXES =
            Set.of(WOODEN_AXE, STONE_AXE, IRON_AXE, GOLDEN_AXE, DIAMOND_AXE, NETHERITE_AXE);

    public void handleEvent(BlockBreakEvent blockBreakEvent) {
        var itemInMainHand = blockBreakEvent.getPlayer().getInventory().getItemInMainHand();
        var armedMaterial = itemInMainHand.getType();
        if (AXES.contains(armedMaterial)) {
            var brokenBlock = blockBreakEvent.getBlock();
            var type = brokenBlock.getType();
            var location = brokenBlock.getLocation();
            var world = blockBreakEvent.getPlayer().getWorld();
            var locationBelowBlock = new Location(world, location.getX(), location.getY() - 1., location.getZ());
            if (LOG_MATERIALS.contains(type)
                    && blockBreakEvent.getPlayer().isSneaking()
                    && canTreeGrow(world, locationBelowBlock)) {
                handleBreaking(blockBreakEvent, itemInMainHand);
            }
        }
    }

    private boolean canTreeGrow(World world, Location locationBelowBlock) {
        return GROWABLE_TREE_DIRT_BLOCKS.contains(
                world.getBlockAt(locationBelowBlock).getType());
    }

    private void handleBreaking(BlockBreakEvent blockBreakEvent, ItemStack item) {
        var world = blockBreakEvent.getBlock().getWorld();
        Queue<Location> locationsToCheck = new LinkedList<>();
        var blockState = blockBreakEvent.getBlock().getState();
        Set<Block> seenLogs = new LinkedHashSet<>();
        Set<Block> leaves = new HashSet<>();
        var originalBlockLocation = blockBreakEvent.getBlock().getLocation();
        locationsToCheck.add(originalBlockLocation);

        while (!locationsToCheck.isEmpty()) {
            var location = locationsToCheck.poll();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        var locationToCheck =
                                new Location(world, location.getX() + x, location.getY() + y, location.getZ() + z);
                        var blockToCheck = world.getBlockAt(locationToCheck);
                        var material = blockToCheck.getType();
                        if (LOG_MATERIALS.contains(material)
                                && !seenLogs.contains(blockToCheck)
                                && blockToCheck.getType().equals(blockState.getType())) {
                            locationsToCheck.add(locationToCheck);
                            seenLogs.add(blockToCheck);
                        }
                        if (LEAVES.contains(material)) {
                            leaves.add(blockToCheck);
                        }
                    }
                }
            }
        }
        // Do not break random stack of logs with no leaves (might be a building with a stack of logs somewhere)
        if (!leaves.isEmpty()) {
            breakWithDamageAwareness(blockBreakEvent, item, world, seenLogs, originalBlockLocation);
        }
    }

    public static void breakWithDamageAwareness(
            BlockBreakEvent blockBreakEvent,
            ItemStack item,
            World world,
            Set<Block> seenBlocks,
            Location originalBlockLocation) {
        var damageable = requireNonNull((Damageable) item.getItemMeta());
        for (var block : seenBlocks) {
            int currentDamage = damageable.getDamage();
            int unbreakingLevel = damageable.getEnchantLevel(Enchantment.DURABILITY);
            boolean takeDamage = shouldTakeDamage(unbreakingLevel);
            if (isToolBrokenAfterApplyingDamage(
                    blockBreakEvent, item, world, originalBlockLocation, damageable, currentDamage, takeDamage)) break;
            block.breakNaturally();
        }
        item.setItemMeta(damageable);
    }
}
