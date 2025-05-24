package me.plugin.serene.actions.inventory.util;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

public class InventoryRendererModule extends AbstractModule {

    @Singleton
    @Provides
    public ItemImageProvider itemImageProvider() {
        return new OnlineItemImageProvider();
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
