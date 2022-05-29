package me.af.serene.listeners;

import me.af.serene.util.DropData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static me.af.serene.util.Utils.isToolBrokenAfterApplyingDamage;
import static me.af.serene.util.Utils.shouldTakeDamage;
import static org.bukkit.Material.COAL;
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
import static org.bukkit.Material.DIAMOND;
import static org.bukkit.Material.DIAMOND_ORE;
import static org.bukkit.Material.DIAMOND_PICKAXE;
import static org.bukkit.Material.EMERALD;
import static org.bukkit.Material.EMERALD_ORE;
import static org.bukkit.Material.GOLDEN_PICKAXE;
import static org.bukkit.Material.GOLD_ORE;
import static org.bukkit.Material.IRON_ORE;
import static org.bukkit.Material.IRON_PICKAXE;
import static org.bukkit.Material.LAPIS_LAZULI;
import static org.bukkit.Material.LAPIS_ORE;
import static org.bukkit.Material.NETHERITE_PICKAXE;
import static org.bukkit.Material.NETHER_GOLD_ORE;
import static org.bukkit.Material.NETHER_QUARTZ_ORE;
import static org.bukkit.Material.QUARTZ;
import static org.bukkit.Material.RAW_COPPER;
import static org.bukkit.Material.RAW_GOLD;
import static org.bukkit.Material.RAW_IRON;
import static org.bukkit.Material.REDSTONE;
import static org.bukkit.Material.REDSTONE_ORE;
import static org.bukkit.Material.STONE_PICKAXE;
import static org.bukkit.Material.WOODEN_PICKAXE;

public class VeinBreakerListener implements Listener {

    private static final Map<Material, DropData> ORE_TO_DROP_DATA = createMaterialExperienceMap();

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
        if (PICKAXES.contains(armedMaterial) && ORE_TO_DROP_DATA.containsKey(blockType) && sneaking) {
            handleBreaking(blockBreakEvent, itemInMainHand, blockType);
        }
    }

    private void handleBreaking(BlockBreakEvent blockBreakEvent, ItemStack item, Material originalMaterial) {
        var world = blockBreakEvent.getBlock().getWorld();
        Queue<Location> locationsToCheck = new LinkedList<>();
        var originalBlockLocation = blockBreakEvent.getBlock().getLocation();
        var originalBlockData = blockBreakEvent.getBlock().getState();
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
                        if (ORE_TO_DROP_DATA.containsKey(material) && material.equals(originalMaterial) && !seenOres.contains(blockToCheck)) {
                            locationsToCheck.add(locationToCheck);
                            seenOres.add(blockToCheck);
                        }
                    }
                }
            }
        }

        if (item.hasItemMeta() && !item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH))
            grantExperience(blockBreakEvent, originalMaterial, world, originalBlockLocation, seenOres);
        breakOresWithEnchantmentAwareness(blockBreakEvent, item, world, seenOres, originalBlockLocation, originalBlockData);
    }

    public static void breakOresWithEnchantmentAwareness(BlockBreakEvent blockBreakEvent,
                                                         ItemStack item,
                                                         World world,
                                                         Set<Block> seenBlocks,
                                                         Location originalBlockLocation,
                                                         BlockState blockState) {
        var damageable = requireNonNull((Damageable) item.getItemMeta());
        int numBrokenBlocks = 0;
        int bonus = 0;
        for (var block : seenBlocks) {
            int currentDamage = damageable.getDamage();
            int unbreakingLevel = damageable.getEnchantLevel(Enchantment.DURABILITY);
            boolean takeDamage = shouldTakeDamage(unbreakingLevel);
            if (isToolBrokenAfterApplyingDamage(blockBreakEvent, item, world, originalBlockLocation, damageable, currentDamage, takeDamage))
                break;
            block.setType(Material.AIR);
            numBrokenBlocks++;

            if (item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
                bonus += calculateFortuneBonus(block, item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS));
            }
        }
        ItemStack stackToDrop = new ItemStack(blockState.getType(), numBrokenBlocks);
        if (item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) {
            world.dropItem(originalBlockLocation, stackToDrop);
        } else {
            world.dropItem(originalBlockLocation, new ItemStack(getDropMaterial(blockState.getType()), numBrokenBlocks + bonus));
        }
        blockBreakEvent.setDropItems(false);
        item.setItemMeta(damageable);
    }

    private static int calculateFortuneBonus(Block block, int enchantLevel) {
        // TODO complete
        return 0;
    }

    private static Material getDropMaterial(Material type) {
        return ORE_TO_DROP_DATA.get(type).materialToDrop();
    }

    private void grantExperience(BlockBreakEvent blockBreakEvent, Material originalMaterial, World world, Location originalBlockLocation, Set<Block> seenOres) {
        int exp = 0;
        var experienceRange = ORE_TO_DROP_DATA.get(originalMaterial);
        for (int i = 0; i < seenOres.size(); i++) {
            exp += random.nextInt(experienceRange.minExp(), experienceRange.maxExp());
        }
        if (exp > 0) {
            world.playSound(originalBlockLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.25f);
        }
        blockBreakEvent.getPlayer().giveExp(exp);
    }

    private static Map<Material, DropData> createMaterialExperienceMap() {
        // Experience values taken from https://minecraft.fandom.com/wiki/Experience#Sources
        Map<Material, DropData> regularOres = new HashMap<>(Map.of(
                COAL_ORE, DropData.withMinMaxExp(0, 3, COAL),
                IRON_ORE, DropData.noExp(RAW_IRON),
                COPPER_ORE, DropData.noExp(RAW_COPPER),
                DIAMOND_ORE, DropData.withMinMaxExp(3, 8, DIAMOND),
                GOLD_ORE, DropData.noExp(RAW_GOLD),
                EMERALD_ORE, DropData.withMinMaxExp(3, 8, EMERALD),
                LAPIS_ORE, DropData.withMinMaxExp(2, 6, LAPIS_LAZULI),
                REDSTONE_ORE, DropData.withMinMaxExp(1, 6, REDSTONE)));

        Map<Material, DropData> remainingOres = Map.of(
                NETHER_GOLD_ORE, DropData.noExp(Material.GOLD_NUGGET),
                NETHER_QUARTZ_ORE, DropData.withMinMaxExp(3, 8, QUARTZ),
                DEEPSLATE_COAL_ORE, DropData.withMinMaxExp(0, 3, COAL),
                DEEPSLATE_IRON_ORE, DropData.noExp(RAW_IRON),
                DEEPSLATE_COPPER_ORE, DropData.noExp(RAW_COPPER),
                DEEPSLATE_DIAMOND_ORE, DropData.withMinMaxExp(3, 8, DIAMOND),
                DEEPSLATE_GOLD_ORE, DropData.noExp(RAW_GOLD),
                DEEPSLATE_EMERALD_ORE, DropData.withMinMaxExp(3, 8, EMERALD),
                DEEPSLATE_LAPIS_ORE, DropData.withMinMaxExp(2, 6, LAPIS_LAZULI),
                DEEPSLATE_REDSTONE_ORE, DropData.withMinMaxExp(1, 6, REDSTONE));
        regularOres.putAll(remainingOres);
        return regularOres;
    }
}
