package me.plugin.serene.actions.inventory.rendering;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryRendererModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryRendererModule.class);
    public static final URI BASE_URI = URI.create("https://mc.nerothe.com/");

    @Singleton
    @Provides
    public ItemImageProvider itemImageProvider() {
        HttpResponse<String> response;
        try (var httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) {
            var req = HttpRequest.newBuilder().GET().uri(BASE_URI).build();
            response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        var online = response.statusCode() == 200;
        if (online) {
            LOG.info("Connected to the internet successfully, using live images from online");
            return new OnlineItemImageProvider();
        } else {
            LOG.info("Could not connect to the internet, using local file-based provider");
            return new FileBasedItemImageProvider();
        }
    }

    @Singleton
    @Provides
    public Font font() throws IOException, FontFormatException {
        return Font.createFont(Font.PLAIN, InventoryRenderer.class.getResourceAsStream("/minecraft_font.ttf"))
                .deriveFont(10f);
    }

    @Provides
    public Display display() {
        return Display.create("Inventory Renderer", 513, 380);
    }
}
