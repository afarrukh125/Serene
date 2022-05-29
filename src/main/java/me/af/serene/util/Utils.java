package me.af.serene.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Objects.requireNonNull;

public class Utils {
    public static boolean shouldTakeDamage(int unbreakingLevel) {
        // Uses formula from https://minecraft.fandom.com/wiki/Unbreaking#Usage
        if (unbreakingLevel == 0)
            return true;
        return ThreadLocalRandom.current().nextInt(unbreakingLevel + 1) == 0;
    }

    public static void breakWithEnchantmentAwareness(BlockBreakEvent blockBreakEvent,
                                                     ItemStack item,
                                                     World world,
                                                     Set<Block> seenBlocks,
                                                     Location originalBlockLocation) {
        var damageable = requireNonNull((Damageable) item.getItemMeta());
        for (var block : seenBlocks) {
            int currentDamage = damageable.getDamage();
            int unbreakingLevel = damageable.getEnchantLevel(Enchantment.DURABILITY);
            boolean takeDamage = shouldTakeDamage(unbreakingLevel);
            if (takeDamage) {
                damageable.setDamage(currentDamage + 1);
                currentDamage++;
            }
            int currentDurability = item.getType().getMaxDurability() - currentDamage;
            if (currentDurability <= 0) {
                var inventory = blockBreakEvent.getPlayer().getInventory();
                inventory.remove(item);
                inventory.setItemInMainHand(new ItemStack(Material.AIR, 0));
                world.playSound(originalBlockLocation, Sound.ENTITY_ITEM_BREAK, 1, 2);
                return;
            }
            block.breakNaturally();
        }
        item.setItemMeta(damageable);
    }
}
