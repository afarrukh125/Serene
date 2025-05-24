package me.plugin.serene.actions.inventory.util;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryRenderer {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryRenderer.class);

    public static final int GAP = 64;
    public static final int ROW_SIZE = 9;
    public static final int BASE_X_OFFSET = 10;
    public static final int BASE_Y_OFFSET = 10;

    private final Font font;
    private final Display display;
    private final ItemImageProvider itemImageProvider;

    @Inject
    public InventoryRenderer(Font font, Display display, ItemImageProvider itemImageProvider) {
        this.font = font;
        this.display = display;
        this.itemImageProvider = itemImageProvider;
    }

    public static void main(String[] args) {
        var injector = Guice.createInjector(new InventoryRendererModule());
        var inventoryRenderer = injector.getInstance(InventoryRenderer.class);
        inventoryRenderer.render(List.of(
                ItemStack.of(Material.ACACIA_LEAVES, 64),
                ItemStack.of(Material.ACACIA_LEAVES, 64),
                ItemStack.of(Material.ACACIA_LEAVES, 64),
                ItemStack.of(Material.BLUE_ICE, 64),
                ItemStack.of(Material.BLUE_ICE, 64),
                ItemStack.of(Material.BLUE_ICE, 64),
                ItemStack.of(Material.BLUE_ICE, 64),
                ItemStack.of(Material.BLUE_ICE, 64),
                ItemStack.of(Material.BLUE_ICE, 64),
                ItemStack.of(Material.BLUE_ICE, 64),
                ItemStack.of(Material.BLUE_ICE, 64),
                ItemStack.of(Material.BLUE_ICE, 64),
                ItemStack.of(Material.BLUE_ICE, 64),
                ItemStack.of(Material.BLAZE_ROD, 64),
                ItemStack.of(Material.BLAZE_ROD, 64),
                ItemStack.of(Material.BLAZE_ROD, 64),
                ItemStack.of(Material.BLAZE_ROD, 64),
                ItemStack.of(Material.BLAZE_ROD, 64),
                ItemStack.of(Material.BLAZE_ROD, 64),
                ItemStack.of(Material.BLAZE_ROD, 64),
                ItemStack.of(Material.EGG, 16),
                ItemStack.of(Material.EGG, 16),
                ItemStack.of(Material.COBBLESTONE, 64)));
    }

    private void render(List<ItemStack> items) {
        int fps = 1;
        double timePerTick = (double) 1000000000 / fps;
        double delta = 0;
        long currentTime;
        long prevTime = System.nanoTime();

        while (true) {
            currentTime = System.nanoTime();
            delta += (currentTime - prevTime) / timePerTick;
            prevTime = currentTime;

            if (delta >= 1) {
                renderFrame(g -> {
                    g.setColor(Color.darkGray);
                    g.fillRect(0, 0, display.width(), display.height());
                    var partition = Lists.partition(items, ROW_SIZE);
                    int row = 0;
                    for (var itemList : partition) {
                        for (int i = 0; i < itemList.size(); i++) {
                            var currentItem = itemList.get(i);
                            BufferedImage image;
                            image = itemImageProvider.getImage(currentItem.getType());
                            var x = BASE_X_OFFSET + (GAP * i);
                            var y = BASE_Y_OFFSET + (GAP * row);
                            g.drawImage(image, x, y, null);
                            g.setColor(Color.WHITE);
                            g.setFont(font);
                            g.drawString(
                                    "" + currentItem.getAmount(), x + image.getWidth() - 20, y + image.getHeight());
                        }
                        row++;
                    }
                });
                delta--;
            }
        }
    }

    private void renderFrame(Consumer<Graphics2D> renderAction) {
        var buffStrat = display.getCanvas().getBufferStrategy();
        if (buffStrat == null) {
            display.getCanvas().createBufferStrategy(3);
            return;
        }

        var g = buffStrat.getDrawGraphics();

        g.clearRect(0, 0, display.width(), display.height());

        LOG.info("Drawing now");

        renderAction.accept((Graphics2D) g);

        buffStrat.show();
        g.dispose();
    }
}
