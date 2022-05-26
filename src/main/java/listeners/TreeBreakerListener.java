package listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static org.bukkit.Material.*;

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


    @EventHandler
    public void onTreeBreak(BlockBreakEvent blockBreakEvent) {
        Block brokenBlock = blockBreakEvent.getBlock();
        Material type = brokenBlock.getType();
        Location location = brokenBlock.getLocation();
        World world = blockBreakEvent.getPlayer().getWorld();
        Location locationBelowBlock = new Location(world, location.getX(), location.getY() - 1., location.getZ());
        if (LOG_MATERIALS.contains(type) && blockBreakEvent.getPlayer().isSneaking() && canTreeGrow(world, locationBelowBlock)) {
            handleBreaking(blockBreakEvent);
        }
    }

    private boolean canTreeGrow(World world, Location locationBelowBlock) {
        return GROWABLE_TREE_DIRT_BLOCKS.contains(world.getBlockAt(locationBelowBlock).getType());
    }

    private static final Logger LOG = LoggerFactory.getLogger(TreeBreakerListener.class);

    private void handleBreaking(BlockBreakEvent blockBreakEvent) {
        World world = blockBreakEvent.getBlock().getWorld();
        Queue<Location> locationsToCheck = new LinkedList<>();
        Set<Location> seenLogs = new HashSet<>();
        Set<Block> leaves = new HashSet<>();
        Location originalBlockLocation = blockBreakEvent.getBlock().getLocation();
        locationsToCheck.add(originalBlockLocation);

        while (!locationsToCheck.isEmpty()) {
            Location location = locationsToCheck.poll();

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
            seenLogs.stream().map(Location::getBlock).forEach(Block::breakNaturally);
        }
    }
}
