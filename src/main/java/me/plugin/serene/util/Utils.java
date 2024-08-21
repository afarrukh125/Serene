package me.plugin.serene.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.stream.Collectors.joining;

public class Utils {
    public static boolean shouldTakeDamage(int unbreakingLevel) {
        // Uses formula from https://minecraft.fandom.com/wiki/Unbreaking#Usage
        if (unbreakingLevel == 0) {
            return true;
        }
        return ThreadLocalRandom.current().nextInt(unbreakingLevel + 1) == 0;
    }

    public static boolean isToolBrokenAfterApplyingDamage(
            PlayerInventory inventory,
            ItemStack item,
            World world,
            Location originalBlockLocation,
            Damageable damageable,
            int currentDamage,
            boolean takeDamage) {
        if (takeDamage) {
            damageable.setDamage(currentDamage + 1);
            currentDamage++;
        }
        var currentDurability = item.getType().getMaxDurability() - currentDamage;
        if (currentDurability <= 0) {
            inventory.remove(item);
            inventory.setItemInMainHand(new ItemStack(Material.AIR, 0));
            world.playSound(originalBlockLocation, Sound.ENTITY_ITEM_BREAK, 1, 2);
            return true;
        }
        return false;
    }

    public static String getGridString(ItemStack[][] newStacks) {
        return Arrays.stream(newStacks)
                .map(a -> Arrays.stream(a)
                        .map(itemStack -> itemStack == null ? "null " : itemStack.getType().name() + ":" + itemStack.getAmount())
                        .collect(joining(" | ")))
                .collect(joining("\n"));
    }
}
