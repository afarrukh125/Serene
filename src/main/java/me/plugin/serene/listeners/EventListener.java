package me.plugin.serene.listeners;

import me.plugin.serene.actions.ExperienceHandler;
import me.plugin.serene.actions.InventorySorter;
import me.plugin.serene.actions.SleepHandler;
import me.plugin.serene.actions.TreeBreaker;
import me.plugin.serene.actions.VeinBreaker;
import me.plugin.serene.database.SereneDatabaseClient;
import me.plugin.serene.util.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import static java.util.Objects.requireNonNull;
import static me.plugin.serene.util.Utils.isFeatureEnabledInConfig;

public class EventListener implements Listener {

    private final ExperienceHandler experienceHandler;
    private final VeinBreaker veinBreaker;
    private final FileConfiguration config;
    private final TreeBreaker treeBreaker;
    private final SleepHandler sleepHandler;
    private final InventorySorter inventorySorter;

    public EventListener(SereneDatabaseClient databaseClient, FileConfiguration config) {
        this.experienceHandler = new ExperienceHandler(databaseClient);
        this.veinBreaker = new VeinBreaker(databaseClient);
        this.treeBreaker = new TreeBreaker();
        this.sleepHandler = new SleepHandler();
        this.inventorySorter = new InventorySorter();
        this.config = config;
    }

    @EventHandler
    public void onExperienceChange(PlayerExpChangeEvent playerExpChangeEvent) {
        if(isFeatureEnabledInConfig(config, "experience")) {
            experienceHandler.handleEvent(playerExpChangeEvent);
        }
    }

    @EventHandler
    public void onChestOpenEvent(PlayerInteractEvent playerInteractEvent) {
        if(isFeatureEnabledInConfig(config, "inventorysort")) {
            inventorySorter.handleEvent(playerInteractEvent);
        }
    }

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent playerBedEnterEvent) {
        if(isFeatureEnabledInConfig(config, "1psleep")) {
            sleepHandler.handleEvent(playerBedEnterEvent);
        }
    }

    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent playerBedLeaveEvent) {
        if(isFeatureEnabledInConfig(config, "1psleep")) {
            sleepHandler.handleEvent(playerBedLeaveEvent);
        }
    }

    @EventHandler
    public void onOreBreak(BlockBreakEvent blockBreakEvent) {
        if(isFeatureEnabledInConfig(config, "veinbreaker")) {
            veinBreaker.handleEvent(blockBreakEvent);
        }
    }

    @EventHandler
    public void onTreeBreak(BlockBreakEvent blockBreakEvent) {
        if(isFeatureEnabledInConfig(config, "treebreaker")) {
            treeBreaker.handleEvent(blockBreakEvent);
        }
    }

}
