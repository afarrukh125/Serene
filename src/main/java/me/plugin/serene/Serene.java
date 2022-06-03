package me.plugin.serene;

import me.plugin.serene.database.SereneDatabaseClient;
import me.plugin.serene.listeners.ExperienceListener;
import me.plugin.serene.listeners.InventorySorterListener;
import me.plugin.serene.listeners.SleepListener;
import me.plugin.serene.listeners.TreeBreakerListener;
import me.plugin.serene.listeners.VeinBreakerListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class Serene extends JavaPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(Serene.class);

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new SleepListener(), this);
        pluginManager.registerEvents(new TreeBreakerListener(), this);
        pluginManager.registerEvents(new InventorySorterListener(), this);
        pluginManager.registerEvents(new VeinBreakerListener(), this);
        pluginManager.registerEvents(new ExperienceListener(getDatabaseClient()), this);
        LOG.info("Started Serene...");
    }

    private SereneDatabaseClient getDatabaseClient() {
        return SereneDatabaseClient.create(this.getConfig());
    }
}
