package listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static org.bukkit.Material.ACACIA_LEAVES;
import static org.bukkit.Material.ACACIA_LOG;
import static org.bukkit.Material.BIRCH_LEAVES;
import static org.bukkit.Material.BIRCH_LOG;
import static org.bukkit.Material.COARSE_DIRT;
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

public class TreeBreakerListener implements Listener {

    private static final Set<Material> LOG_MATERIALS = Set.of(ACACIA_LOG,
            BIRCH_LOG,
            DARK_OAK_LOG,
            JUNGLE_LOG,
            OAK_LOG,
            SPRUCE_LOG);

    private static final Set<Material> LEAVES = Set.of(ACACIA_LEAVES,
            BIRCH_LEAVES,
            JUNGLE_LEAVES,
            OAK_LEAVES,
            SPRUCE_LEAVES);
    private static final Set<Material> GROWABLE_TREE_DIRT_BLOCKS = Set.of(GRASS_BLOCK,
            DIRT,
            FARMLAND,
            PODZOL,
            DIRT_PATH,
            MYCELIUM,
            COARSE_DIRT,
            ROOTED_DIRT);

    private static final Set<Material> AXES = Set.of(WOODEN_AXE,
            STONE_AXE,
            IRON_AXE,
            GOLDEN_AXE,
            DIAMOND_AXE,
            NETHERITE_AXE);

    private static final Logger LOG = LoggerFactory.getLogger(TreeBreakerListener.class);

    @EventHandler
    public void onTreeBreak(BlockBreakEvent blockBreakEvent) {
        Block brokenBlock = blockBreakEvent.getBlock();
        Material type = brokenBlock.getType();
        Location location = brokenBlock.getLocation();
        World world = blockBreakEvent.getPlayer().getWorld();
        Location locationBelowBlock = new Location(world, location.getX(), location.getY() - 1., location.getZ());
        ItemStack itemInMainHand = blockBreakEvent.getPlayer().getInventory().getItemInMainHand();
        Material armedMaterial = itemInMainHand.getType();
        LOG.info("Currently armed material is {}", armedMaterial);
        if (AXES.contains(armedMaterial)
                && LOG_MATERIALS.contains(type)
                && blockBreakEvent.getPlayer().isSneaking()
                && canTreeGrow(world, locationBelowBlock)) {
            handleBreaking(blockBreakEvent, itemInMainHand);
        }
    }

    private boolean canTreeGrow(World world, Location locationBelowBlock) {
        return GROWABLE_TREE_DIRT_BLOCKS.contains(world.getBlockAt(locationBelowBlock).getType());
    }

    private void handleBreaking(BlockBreakEvent blockBreakEvent, ItemStack item) {
        World world = blockBreakEvent.getBlock().getWorld();
        Stack<Location> locationsToCheck = new Stack<>();
        Set<Location> seenLogs = new HashSet<>();
        Set<Block> leaves = new HashSet<>();
        Location originalBlockLocation = blockBreakEvent.getBlock().getLocation();
        locationsToCheck.add(originalBlockLocation);

        while (!locationsToCheck.isEmpty()) {
            Location location = locationsToCheck.pop();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        Location locationToCheck = new Location(world, location.getX() + x, location.getY() + y, location.getZ() + z);
                        Block blockToCheck = world.getBlockAt(locationToCheck);
                        Material material = blockToCheck.getType();
                        if (LOG_MATERIALS.contains(material) && !seenLogs.contains(locationToCheck)) {
                            locationsToCheck.add(locationToCheck);
                            seenLogs.add(locationToCheck);
                        }
                        if (LEAVES.contains(material))
                            leaves.add(blockToCheck);
                    }
                }
            }
        }

        // Do not break random stack of logs with no leaves (might be a building with a stack of logs somewhere)
        if (leaves.size() > 0) {
            Damageable damageable = (Damageable) item.getItemMeta();
            for (Block block : seenLogs.stream().map(Location::getBlock).toList()) {
                LOG.info("Max durability is {}", item.getType().getMaxDurability());
                int currentDamage = damageable.getDamage();
                LOG.info("Item damage is {}", currentDamage);
                damageable.setDamage(currentDamage + 1);
                int currentDurability = item.getType().getMaxDurability() - currentDamage;
                if (currentDurability <= 0) {
                    PlayerInventory inventory = blockBreakEvent.getPlayer().getInventory();
                    inventory.remove(item);
                    inventory.setItemInMainHand(new ItemStack(Material.AIR, 0));
                    world.playSound(originalBlockLocation, Sound.ENTITY_ITEM_BREAK, 1, 1);
                    return;
                }
                block.breakNaturally();
            }
            item.setItemMeta(damageable);
        }
    }
}
