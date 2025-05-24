package me.plugin.serene.actions.inventory.util;

import org.bukkit.Material;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class OnlineItemImageProvider implements ItemImageProvider{
    private static final Logger LOG = LoggerFactory.getLogger(OnlineItemImageProvider.class);
    private final Map<Material, BufferedImage> cache;

    public OnlineItemImageProvider() {
        this.cache = new HashMap<>();
    }

    @Override
    public BufferedImage getImage(Material material) {
        if(cache.containsKey(material)) {
            return cache.get(material);
        }
        try {
            var image = ImageIO.read(URI.create("https://mc.nerothe.com/img/1.21.5/minecraft_%s.png".formatted(material.name().toLowerCase())).toURL());
            LOG.info("Caching material {}", material);
            this.cache.put(material, image);
            return image;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
