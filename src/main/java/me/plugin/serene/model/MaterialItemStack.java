package me.plugin.serene.model;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public record MaterialItemStack(Material material, List<ItemStack> itemStacks) {}
