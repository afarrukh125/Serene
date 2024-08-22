package me.plugin.serene.listeners;

import jakarta.inject.Inject;
import me.plugin.serene.actions.ExperienceHandler;
import me.plugin.serene.actions.SleepHandler;
import me.plugin.serene.actions.TreeBreaker;
import me.plugin.serene.actions.VeinBreaker;
import me.plugin.serene.actions.inventory.InventorySorter;
import me.plugin.serene.model.SereneConfiguration;
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
    private final SereneConfiguration config;
    private final TreeBreaker treeBreaker;
    private final SleepHandler sleepHandler;
    private final InventorySorter inventorySorter;

    @Inject
    public EventListener(
            SereneConfiguration config,
            ExperienceHandler experienceHandler,
            VeinBreaker veinBreaker,
            TreeBreaker treeBreaker,
            SleepHandler sleepHandler,
            InventorySorter inventorySorter) {
        this.experienceHandler = experienceHandler;
        this.veinBreaker = veinBreaker;
        this.config = config;
        this.treeBreaker = treeBreaker;
        this.sleepHandler = sleepHandler;
        this.inventorySorter = inventorySorter;
    }

    @EventHandler
    public void onExperienceChange(PlayerExpChangeEvent playerExpChangeEvent) {
        if (config.isBonusExperienceEnabled()) {
            experienceHandler.handleEvent(playerExpChangeEvent);
        }
    }

    @EventHandler
    public void onChestOpenEvent(PlayerInteractEvent playerInteractEvent) {
        if (config.isInventorySortEnabled()) {
            inventorySorter.handleEvent(playerInteractEvent);
        }
    }

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent playerBedEnterEvent) {
        if (config.is1pSleepEnabled()) {
            sleepHandler.handleEvent(playerBedEnterEvent);
        }
    }

    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent playerBedLeaveEvent) {
        if (config.is1pSleepEnabled()) {
            sleepHandler.handleEvent(playerBedLeaveEvent.getPlayer(), playerBedLeaveEvent.isCancelled());
        }
    }

    @EventHandler
    public void onOreBreak(BlockBreakEvent blockBreakEvent) {
        if (config.isVeinBreakerEnabled()) {
            veinBreaker.handleEvent(blockBreakEvent);
        }
    }

    @EventHandler
    public void onTreeBreak(BlockBreakEvent blockBreakEvent) {
        if (config.isTreeBreakerEnabled()) {
            treeBreaker.handleEvent(blockBreakEvent);
        }
    }
}
