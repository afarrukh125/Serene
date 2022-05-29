package me.af.serene.listeners;

import me.af.serene.util.ExperienceRange;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import static me.af.serene.util.Utils.breakWithEnchantmentAwareness;
import static org.bukkit.Material.COAL_ORE;
import static org.bukkit.Material.COPPER_ORE;
import static org.bukkit.Material.DEEPSLATE_COAL_ORE;
import static org.bukkit.Material.DEEPSLATE_COPPER_ORE;
import static org.bukkit.Material.DEEPSLATE_DIAMOND_ORE;
import static org.bukkit.Material.DEEPSLATE_EMERALD_ORE;
import static org.bukkit.Material.DEEPSLATE_GOLD_ORE;
import static org.bukkit.Material.DEEPSLATE_IRON_ORE;
import static org.bukkit.Material.DEEPSLATE_LAPIS_ORE;
import static org.bukkit.Material.DEEPSLATE_REDSTONE_ORE;
import static org.bukkit.Material.DIAMOND_ORE;
import static org.bukkit.Material.DIAMOND_PICKAXE;
import static org.bukkit.Material.EMERALD_ORE;
import static org.bukkit.Material.GOLDEN_PICKAXE;
import static org.bukkit.Material.GOLD_ORE;
import static org.bukkit.Material.IRON_ORE;
import static org.bukkit.Material.IRON_PICKAXE;
import static org.bukkit.Material.LAPIS_ORE;
import static org.bukkit.Material.NETHERITE_PICKAXE;
import static org.bukkit.Material.NETHER_GOLD_ORE;
import static org.bukkit.Material.NETHER_QUARTZ_ORE;
import static org.bukkit.Material.REDSTONE_ORE;
import static org.bukkit.Material.STONE_PICKAXE;
import static org.bukkit.Material.WOODEN_PICKAXE;

public class VeinBreakerListener implements Listener {

    private static final Map<Material, ExperienceRange> ORE_TO_EXPERIENCE = createMaterialExperienceMap();

    private static final Set<Material> PICKAXES = Set.of(
            WOODEN_PICKAXE,
            STONE_PICKAXE,
            IRON_PICKAXE,
            GOLDEN_PICKAXE,
            DIAMOND_PICKAXE,
            NETHERITE_PICKAXE);

    private static final Random random = new Random();

    @EventHandler
    public void onOreBreak(BlockBreakEvent blockBreakEvent) {
        var itemInMainHand = blockBreakEvent.getPlayer().getInventory().getItemInMainHand();
        var armedMaterial = itemInMainHand.getType();
        var brokenBlock = blockBreakEvent.getBlock();
        Material blockType = brokenBlock.getType();
        boolean sneaking = blockBreakEvent.getPlayer().isSneaking();
        if (PICKAXES.contains(armedMaterial) && ORE_TO_EXPERIENCE.containsKey(blockType) && sneaking) {
            handleBreaking(blockBreakEvent, itemInMainHand, blockType);
        }
    }

    private void handleBreaking(BlockBreakEvent blockBreakEvent, ItemStack item, Material originalMaterial) {
        var world = blockBreakEvent.getBlock().getWorld();
        Queue<Location> locationsToCheck = new LinkedList<>();
        var originalBlockLocation = blockBreakEvent.getBlock().getLocation();
        var originalBlockData = blockBreakEvent.getBlock().getBlockData();
        Set<Block> seenOres = new HashSet<>();
        locationsToCheck.add(originalBlockLocation);

        while (!locationsToCheck.isEmpty()) {
            var location = locationsToCheck.poll();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        var locationToCheck = new Location(world, location.getX() + x, location.getY() + y, location.getZ() + z);
                        var blockToCheck = world.getBlockAt(locationToCheck);
                        var material = blockToCheck.getType();
                        if (ORE_TO_EXPERIENCE.containsKey(material) && material.equals(originalMaterial) && !seenOres.contains(blockToCheck)) {
                            locationsToCheck.add(locationToCheck);
                            seenOres.add(blockToCheck);
                        }
                    }
                }
            }
        }

        grantExperience(blockBreakEvent, originalMaterial, world, originalBlockLocation, seenOres);
        breakWithEnchantmentAwareness(blockBreakEvent, item, world, seenOres, originalBlockLocation);
    }

    private void grantExperience(BlockBreakEvent blockBreakEvent, Material originalMaterial, World world, Location originalBlockLocation, Set<Block> seenOres) {
        int exp = 0;
        var experienceRange = ORE_TO_EXPERIENCE.get(originalMaterial);
        for (int i = 0; i < seenOres.size(); i++) {
            exp += random.nextInt(experienceRange.min(), experienceRange.max());
        }
        if (exp > 0) {
            world.playSound(originalBlockLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.25f);
        }
        blockBreakEvent.getPlayer().giveExp(exp);
    }

    private static Map<Material, ExperienceRange> createMaterialExperienceMap() {
        // Experience values taken from https://minecraft.fandom.com/wiki/Experience#Sources
        Map<Material, ExperienceRange> regularOres = new HashMap<>(Map.of(
                COAL_ORE, ExperienceRange.of(0, 3),
                IRON_ORE, ExperienceRange.none(),
                COPPER_ORE, ExperienceRange.none(),
                DIAMOND_ORE, ExperienceRange.of(3, 8),
                GOLD_ORE, ExperienceRange.none(),
                EMERALD_ORE, ExperienceRange.of(3, 8),
                LAPIS_ORE, ExperienceRange.of(2, 6),
                REDSTONE_ORE, ExperienceRange.of(1, 6)));

        Map<Material, ExperienceRange> remainingOres = Map.of(
                NETHER_GOLD_ORE, ExperienceRange.none(),
                NETHER_QUARTZ_ORE, ExperienceRange.of(3, 8),
                DEEPSLATE_COAL_ORE, ExperienceRange.of(0, 3),
                DEEPSLATE_IRON_ORE, ExperienceRange.none(),
                DEEPSLATE_COPPER_ORE, ExperienceRange.none(),
                DEEPSLATE_DIAMOND_ORE, ExperienceRange.of(3, 8),
                DEEPSLATE_GOLD_ORE, ExperienceRange.none(),
                DEEPSLATE_EMERALD_ORE, ExperienceRange.of(3, 8),
                DEEPSLATE_LAPIS_ORE, ExperienceRange.of(2, 6),
                DEEPSLATE_REDSTONE_ORE, ExperienceRange.of(1, 6));
        regularOres.putAll(remainingOres);
        return regularOres;
    }
}
