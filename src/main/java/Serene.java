import listeners.ExperienceListener;
import listeners.SleepListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Serene extends JavaPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(Serene.class);

    @Override
    public void onEnable() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new SleepListener(), this);
        pluginManager.registerEvents(new ExperienceListener(), this);
        LOG.info("Started Serene...");
    }
}
