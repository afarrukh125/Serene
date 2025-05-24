package me.plugin.serene.actions.inventory.util;

import org.bukkit.Material;

import java.awt.image.BufferedImage;

public interface ItemImageProvider {
    BufferedImage getImage(Material material);
}
