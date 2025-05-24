package me.plugin.serene.actions.inventory.util;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;

public class InventoryRendererModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryRendererModule.class);
    @Singleton
    @Provides
    public ItemImageProvider itemImageProvider() {
        try {
            boolean online = InetAddress.getByName("8.8.8.8").isReachable(3000);
            if(online) {
                LOG.info("Connected to the internet successfully, using live images from online");
                return new OnlineItemImageProvider();
            } else {
                LOG.info("Could not connect to the internet, using local file-based provider");
                return new FileBasedItemImageProvider();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Singleton
    @Provides
    public Font font() throws IOException, FontFormatException {
        return Font.createFont(Font.PLAIN, InventoryRenderer.class.getResourceAsStream("/minecraft_font.ttf"))
                .deriveFont(14f);
    }

    @Provides
    public Display display() {
        return Display.create("Inventory Renderer", 1024, 500);
    }
}
