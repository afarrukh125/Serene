package me.plugin.serene.core;

import com.google.inject.Guice;
import me.plugin.serene.core.command.SearchItemCommand;
import me.plugin.serene.core.command.ToggleVeinBreakerCommand;
import me.plugin.serene.database.DatabaseModule;
import me.plugin.serene.database.SereneDatabaseClient;
import me.plugin.serene.listeners.EventListener;
import me.plugin.serene.model.SereneConfiguration;
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
        var pluginManager = this.getServer().getPluginManager();
        var injector = Guice.createInjector(new DatabaseModule(), new ConfigModule(this.getConfig()));
        var databaseClient = injector.getInstance(SereneDatabaseClient.class);
        var config = injector.getInstance(SereneConfiguration.class);
        var eventListener = injector.getInstance(EventListener.class);
        pluginManager.registerEvents(eventListener, this);
        setupCommands(databaseClient, config);
        LOG.info("Started Serene...");
    }

    private void setupCommands(SereneDatabaseClient databaseClient, SereneConfiguration config) {
        requireNonNull(this.getCommand("veinbreaker"))
                .setExecutor(new ToggleVeinBreakerCommand(databaseClient, config));
        requireNonNull(this.getCommand("vb")).setExecutor(new ToggleVeinBreakerCommand(databaseClient, config));
        requireNonNull(this.getCommand("search")).setExecutor(new SearchItemCommand());
    }
}
