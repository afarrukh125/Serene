package me.plugin.serene.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Queue;

public record MaterialItemStack(Material material,
                                Queue<ItemStack> itemStacks) {
}
