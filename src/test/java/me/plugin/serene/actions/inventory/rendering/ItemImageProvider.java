package me.plugin.serene.actions.inventory.rendering;

import java.awt.image.BufferedImage;
import org.bukkit.Material;

public interface ItemImageProvider {
    BufferedImage getImage(Material material);
}
