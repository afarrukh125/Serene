import database.SereneDatabaseClient;
import listeners.ExperienceListener;
import listeners.SleepListener;
import listeners.TreeBreakerListener;
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
        SereneDatabaseClient databaseClient = getDatabaseClient();
        pluginManager.registerEvents(new ExperienceListener(databaseClient), this);
        pluginManager.registerEvents(new TreeBreakerListener(), this);
        LOG.info("Started Serene...");
    }

    private SereneDatabaseClient getDatabaseClient() {
        return SereneDatabaseClient.create(this.getConfig());
    }
}
