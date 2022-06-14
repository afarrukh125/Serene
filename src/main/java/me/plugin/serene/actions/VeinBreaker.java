package me.plugin.serene.actions;

import com.google.common.collect.ImmutableMap;
import me.plugin.serene.database.SereneDatabaseClient;
import me.plugin.serene.util.ExperienceData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static me.plugin.serene.util.Utils.isToolBrokenAfterApplyingDamage;
import static me.plugin.serene.util.Utils.shouldTakeDamage;
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

public class VeinBreaker {

    // Experience values taken from https://minecraft.fandom.com/wiki/Experience#Sources
    private static final Map<Material, ExperienceData> ORE_TO_EXPERIENCE_DATA = ImmutableMap.<Material, ExperienceData>builder()
            .put(COAL_ORE, ExperienceData.withMinMaxExp(0, 3))
            .put(IRON_ORE, ExperienceData.noExp())
            .put(COPPER_ORE, ExperienceData.noExp())
            .put(DIAMOND_ORE, ExperienceData.withMinMaxExp(3, 8))
            .put(GOLD_ORE, ExperienceData.noExp())
            .put(EMERALD_ORE, ExperienceData.withMinMaxExp(3, 8))
            .put(LAPIS_ORE, ExperienceData.withMinMaxExp(2, 6))
            .put(REDSTONE_ORE, ExperienceData.withMinMaxExp(1, 6))
            .put(NETHER_GOLD_ORE, ExperienceData.noExp())
            .put(NETHER_QUARTZ_ORE, ExperienceData.withMinMaxExp(3, 8))
            .put(DEEPSLATE_COAL_ORE, ExperienceData.withMinMaxExp(0, 3))
            .put(DEEPSLATE_IRON_ORE, ExperienceData.noExp())
            .put(DEEPSLATE_COPPER_ORE, ExperienceData.noExp())
            .put(DEEPSLATE_DIAMOND_ORE, ExperienceData.withMinMaxExp(3, 8))
            .put(DEEPSLATE_GOLD_ORE, ExperienceData.noExp())
            .put(DEEPSLATE_EMERALD_ORE, ExperienceData.withMinMaxExp(3, 8))
            .put(DEEPSLATE_LAPIS_ORE, ExperienceData.withMinMaxExp(2, 6))
            .put(DEEPSLATE_REDSTONE_ORE, ExperienceData.withMinMaxExp(1, 6))
            .build();

    private static final Set<Material> PICKAXES = Set.of(
            WOODEN_PICKAXE,
            STONE_PICKAXE,
            IRON_PICKAXE,
            GOLDEN_PICKAXE,
            DIAMOND_PICKAXE,
            NETHERITE_PICKAXE);

    private static final Random random = new Random();
    private final SereneDatabaseClient database;

    public VeinBreaker(SereneDatabaseClient database) {
        this.database = database;
    }

    public void handleEvent(BlockBreakEvent blockBreakEvent) {
        Player player = blockBreakEvent.getPlayer();
        if (database.isVeinBreakerEnabled(player)) {
            var itemInMainHand = player.getInventory().getItemInMainHand();
            var armedMaterial = itemInMainHand.getType();
            var brokenBlock = blockBreakEvent.getBlock();
            Material blockType = brokenBlock.getType();
            boolean sneaking = player.isSneaking();
            if (PICKAXES.contains(armedMaterial) && ORE_TO_EXPERIENCE_DATA.containsKey(blockType) && sneaking) {
                handleBreaking(blockBreakEvent, itemInMainHand, blockType);
            }
        }
    }


    private void handleBreaking(BlockBreakEvent blockBreakEvent, ItemStack item, Material originalMaterial) {
        var world = blockBreakEvent.getBlock().getWorld();
        Queue<Location> locationsToCheck = new LinkedList<>();
        var originalBlockLocation = blockBreakEvent.getBlock().getLocation();
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
                        if (ORE_TO_EXPERIENCE_DATA.containsKey(material) && material.equals(originalMaterial) && !seenOres.contains(blockToCheck)) {
                            locationsToCheck.add(locationToCheck);
                            seenOres.add(blockToCheck);
                        }
                    }
                }
            }
        }

        //noinspection ConstantConditions
        if (item.hasItemMeta() && !item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH))
            grantExperience(blockBreakEvent, originalMaterial, world, originalBlockLocation, seenOres);
        breakOresWithDamageAwareness(blockBreakEvent, item, world, seenOres, originalBlockLocation);
    }

    private static void breakOresWithDamageAwareness(BlockBreakEvent blockBreakEvent,
                                                     ItemStack item,
                                                     World world,
                                                     Set<Block> seenBlocks,
                                                     Location originalBlockLocation) {
        var damageable = requireNonNull((Damageable) item.getItemMeta());
        for (var block : seenBlocks) {
            int currentDamage = damageable.getDamage();
            int unbreakingLevel = damageable.getEnchantLevel(Enchantment.DURABILITY);
            boolean takeDamage = shouldTakeDamage(unbreakingLevel);
            if (isToolBrokenAfterApplyingDamage(blockBreakEvent,
                    item,
                    world,
                    originalBlockLocation,
                    damageable,
                    currentDamage,
                    takeDamage))
                break;
            block.breakNaturally(item);
        }
        item.setItemMeta(damageable);
    }

    private void grantExperience(BlockBreakEvent blockBreakEvent, Material originalMaterial, World world, Location originalBlockLocation, Set<Block> seenOres) {
        int exp = 0;
        var experienceRange = ORE_TO_EXPERIENCE_DATA.get(originalMaterial);
        for (int i = 0; i < seenOres.size(); i++) {
            exp += random.nextInt(experienceRange.minExp(), experienceRange.maxExp());
        }
        if (exp > 0) {
            world.playSound(originalBlockLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.25f);
        }
        blockBreakEvent.getPlayer().giveExp(exp);
    }

}
