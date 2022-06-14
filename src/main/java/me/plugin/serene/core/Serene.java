package me.plugin.serene.core;

import me.plugin.serene.core.command.ToggleVeinBreakerCommand;
import me.plugin.serene.database.SereneDatabaseClient;
import me.plugin.serene.listeners.EventListener;
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
        var pluginManager = getServer().getPluginManager();
        var databaseClient = SereneDatabaseClient.create();
        pluginManager.registerEvents(new EventListener(databaseClient), this);
        setupCommands(databaseClient);
        LOG.info("Started Serene...");
    }

    private void setupCommands(SereneDatabaseClient databaseClient) {
        requireNonNull(this.getCommand("veinbreaker")).setExecutor(new ToggleVeinBreakerCommand(databaseClient));
        requireNonNull(this.getCommand("vb")).setExecutor(new ToggleVeinBreakerCommand(databaseClient));
    }
}
