package me.plugin.serene.actions.inventory;

import java.util.Comparator;
import me.plugin.serene.model.MaterialItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public final class ItemOrder {

    // Material.values() follows Minecraft's own registry order, which is what the creative menu
    // shows. It is the closest thing the API gives us to the item tree InventoryTweaks shipped.
    private static final Comparator<Material> REGISTRY = Comparator.comparingInt(Material::ordinal);

    public static final Comparator<MaterialItemStack> GROUPS =
            Comparator.comparing(MaterialItemStack::material, REGISTRY);

    public static final Comparator<ItemStack> WITHIN_GROUP = Comparator.comparingInt(ItemStack::getAmount)
            .reversed()
            .thenComparingInt(ItemOrder::damage)
            .thenComparing(ItemStack::hasItemMeta);

    public static final Comparator<ItemStack> STACKS =
            Comparator.comparing(ItemStack::getType, REGISTRY).thenComparing(WITHIN_GROUP);

    private static int damage(ItemStack itemStack) {
        return itemStack.getItemMeta() instanceof Damageable damageable ? damageable.getDamage() : 0;
    }

    private ItemOrder() {}
}
