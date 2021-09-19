import listeners.SereneListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Serene extends JavaPlugin {

    private final Logger logger = LoggerFactory.getLogger(Serene.class);

    @Override
    public void onEnable() {
        Listener sereneListener = new SereneListener();
        getServer().getPluginManager().registerEvents(sereneListener, this);
        logger.info("Started Serene...");
    }
}
