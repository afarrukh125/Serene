package listeners;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public class InventorySorterListener implements Listener {

    private static final Logger LOG = LoggerFactory.getLogger(InventorySorterListener.class);

    @EventHandler
    public void onChestOpenEvent(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getClickedBlock() != null) {
            boolean wasChest = requireNonNull(playerInteractEvent.getClickedBlock()).getType().equals(Material.CHEST);
            if (wasChest) {
                boolean sneaking = playerInteractEvent.getPlayer().isSneaking();
                boolean rightClicked = playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK);
                boolean usedFeather = playerInteractEvent.hasItem() && requireNonNull(playerInteractEvent.getItem()).getType().equals(Material.FEATHER);
                if (rightClicked && usedFeather && sneaking) {
                    Chest chest = (Chest) playerInteractEvent.getClickedBlock().getState();
                    // TODO do something with inventory
                    BlockData blockData = chest.getBlockData();
                }
            }
        }
    }
}
