package me.plugin.serene.core;

import static java.util.Objects.requireNonNull;

import com.google.inject.Guice;
import com.google.inject.Injector;
import me.plugin.serene.core.command.SearchItemCommand;
import me.plugin.serene.core.command.ToggleVeinBreakerCommand;
import me.plugin.serene.database.DatabaseModule;
import me.plugin.serene.database.SereneDatabaseClient;
import me.plugin.serene.listeners.EventListener;
import me.plugin.serene.model.SereneConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        setupCommands(injector);
        LOG.info("Started Serene...");
    }

    private void setupCommands(Injector injector) {
        var veinBreakerCommand = injector.getInstance(ToggleVeinBreakerCommand.class);
        requireNonNull(this.getCommand("veinbreaker")).setExecutor(veinBreakerCommand);
        requireNonNull(this.getCommand("vb")).setExecutor(veinBreakerCommand);
        requireNonNull(this.getCommand("search")).setExecutor(injector.getInstance(SearchItemCommand.class));
    }
}
