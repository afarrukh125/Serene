package me.plugin.serene.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public record MaterialItemStack(Material material, Queue<ItemStack> itemStacks) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (MaterialItemStack) o;
        return material == that.material && Objects.equals(Set.of(itemStacks), Set.of(that.itemStacks));
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, Set.of(itemStacks));
    }
}
