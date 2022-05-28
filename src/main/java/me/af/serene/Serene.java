package me.af.serene;

import me.af.serene.database.SereneDatabaseClient;
import me.af.serene.listeners.InventorySorterListener;
import me.af.serene.listeners.SleepListener;
import me.af.serene.listeners.TreeBreakerListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Serene extends JavaPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(Serene.class);

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new SleepListener(), this);
        pluginManager.registerEvents(new TreeBreakerListener(), this);
        pluginManager.registerEvents(new InventorySorterListener(), this);
        LOG.info("Started Serene...");
    }

    private SereneDatabaseClient getDatabaseClient() {
        return SereneDatabaseClient.create(this.getConfig());
    }
}
