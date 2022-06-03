package me.plugin.serene.core;

import me.plugin.serene.core.command.ToggleVeinBreakerCommand;
import me.plugin.serene.database.SQLiteSereneClient;
import me.plugin.serene.listeners.ExperienceListener;
import me.plugin.serene.listeners.InventorySorterListener;
import me.plugin.serene.listeners.SleepListener;
import me.plugin.serene.listeners.TreeBreakerListener;
import me.plugin.serene.listeners.VeinBreakerListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public class Serene extends JavaPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(Serene.class);

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        var databaseClient = new SQLiteSereneClient();
        var pluginManager = getServer().getPluginManager();
        registerEventListeners(databaseClient, pluginManager);
        setupCommands(databaseClient);
        LOG.info("Started Serene...");

    }

    private void registerEventListeners(SQLiteSereneClient databaseClient, PluginManager pluginManager) {
        pluginManager.registerEvents(new SleepListener(), this);
        pluginManager.registerEvents(new TreeBreakerListener(), this);
        pluginManager.registerEvents(new InventorySorterListener(), this);
        pluginManager.registerEvents(new VeinBreakerListener(databaseClient), this);
        pluginManager.registerEvents(new ExperienceListener(databaseClient), this);
    }

    private void setupCommands(SQLiteSereneClient databaseClient) {
        requireNonNull(this.getCommand("veinbreaker")).setExecutor(new ToggleVeinBreakerCommand(databaseClient));
        requireNonNull(this.getCommand("vb")).setExecutor(new ToggleVeinBreakerCommand(databaseClient));
    }
}
