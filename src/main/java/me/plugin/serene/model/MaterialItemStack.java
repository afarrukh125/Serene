package me.plugin.serene.model;

import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
