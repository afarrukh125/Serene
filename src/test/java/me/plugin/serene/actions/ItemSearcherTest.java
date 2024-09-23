package me.plugin.serene.actions;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ItemSearcherTest extends PlayerTest {

    @Test
    public void testSimpleSearch() {
        var chest1 = (Chest)
                player.simulateBlockPlace(Material.CHEST, player.getLocation().add(5, 0, 2))
                        .getBlock();
        chest1.getInventory().addItem(ItemStack.of(Material.BONE_BLOCK, 42));

        var chest2 = (Chest)
                player.simulateBlockPlace(Material.CHEST, player.getLocation().add(5, 0, 3));
        var chest3 = (Chest)
                player.simulateBlockPlace(Material.CHEST, player.getLocation().add(5, 0, 4));

        var itemSearcher = new ItemSearcher();
        itemSearcher.searchItem(player, player.getLocation(), player.getWorld(), "bone block");
    }
}
