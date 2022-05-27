package listeners;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import static java.util.Objects.requireNonNull;

public class InventorySorterListener implements Listener {

    @EventHandler
    public void onChestOpenEvent(PlayerInteractEvent playerInteractEvent) {
        boolean didClickChest = requireNonNull(playerInteractEvent.getClickedBlock()).getType().equals(Material.CHEST);
        if (didClickChest) {
            Chest chest = (Chest) playerInteractEvent.getClickedBlock();
            // TODO do something with inventory
        }
    }
}
