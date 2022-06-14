package me.plugin.serene.listeners;

import me.plugin.serene.actions.ExperienceHandler;
import me.plugin.serene.actions.InventorySorter;
import me.plugin.serene.actions.SleepHandler;
import me.plugin.serene.actions.TreeBreaker;
import me.plugin.serene.actions.VeinBreaker;
import me.plugin.serene.database.SereneDatabaseClient;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventListener implements Listener {

    private final ExperienceHandler experienceHandler;
    private final VeinBreaker veinBreaker;
    private final TreeBreaker treeBreaker;
    private final SleepHandler sleepHandler;
    private final InventorySorter inventorySorter;

    public EventListener(SereneDatabaseClient databaseClient) {
        experienceHandler = new ExperienceHandler(databaseClient);
        veinBreaker = new VeinBreaker(databaseClient);
        treeBreaker = new TreeBreaker();
        sleepHandler = new SleepHandler();
        inventorySorter = new InventorySorter();
    }

    @EventHandler
    public void onExperienceChange(PlayerExpChangeEvent playerExpChangeEvent) {
        experienceHandler.handleEvent(playerExpChangeEvent);
    }

    @EventHandler
    public void onChestOpenEvent(PlayerInteractEvent playerInteractEvent) {
        inventorySorter.handleEvent(playerInteractEvent);
    }

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent playerBedEnterEvent) {
        sleepHandler.handleEvent(playerBedEnterEvent);
    }

    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent playerBedLeaveEvent) {
        sleepHandler.handleEvent(playerBedLeaveEvent);
    }

    @EventHandler
    public void onOreBreak(BlockBreakEvent blockBreakEvent) {
        veinBreaker.handleEvent(blockBreakEvent);
    }

    @EventHandler
    public void onTreeBreak(BlockBreakEvent blockBreakEvent) {
        treeBreaker.handleEvent(blockBreakEvent);
    }
}
