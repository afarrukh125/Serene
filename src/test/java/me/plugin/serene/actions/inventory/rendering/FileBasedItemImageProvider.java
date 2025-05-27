package me.plugin.serene.actions.inventory.rendering;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.bukkit.Material;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBasedItemImageProvider implements ItemImageProvider {
    private static final Logger LOG = LoggerFactory.getLogger(FileBasedItemImageProvider.class);
    private final Map<Material, BufferedImage> cache;

    public FileBasedItemImageProvider() {
        this.cache = new HashMap<>();
    }

    @Override
    public BufferedImage getImage(Material material) {
        if (cache.containsKey(material)) {
            return cache.get(material);
        }
        var fileName = "minecraft_" + material.name().toLowerCase() + ".png";
        var path = "/items_1.21.5/%s".formatted(fileName);
        LOG.info("Resolving path {} for material {}", path, material);
        BufferedImage image = null;
        try {
            image = ImageIO.read(InventoryRenderer.class.getResourceAsStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cache.put(material, image);
        return image;
    }
}
