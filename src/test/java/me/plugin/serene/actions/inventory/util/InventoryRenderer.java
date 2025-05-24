package me.plugin.serene.actions.inventory.util;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

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
                ItemStack.of(Material.STONE_SLAB, 64),
                ItemStack.of(Material.CHISELED_STONE_BRICKS, 64),
                ItemStack.of(Material.SMOOTH_STONE, 34),
                ItemStack.of(Material.SMOOTH_STONE, 64),
                ItemStack.of(Material.SMOOTH_STONE, 64),
                ItemStack.of(Material.SMOOTH_STONE, 64),
                ItemStack.of(Material.SMOOTH_STONE, 64),
                ItemStack.of(Material.STONE_SLAB, 64),
                ItemStack.of(Material.CHISELED_STONE_BRICKS, 24),
                ItemStack.of(Material.STONE_BRICKS, 64),
                ItemStack.of(Material.STONE_BRICKS, 64),
                ItemStack.of(Material.STONE_SLAB, 64),
                ItemStack.of(Material.MOSSY_COBBLESTONE, 8),
                ItemStack.of(Material.MOSSY_STONE_BRICKS, 4),
                ItemStack.of(Material.STONE_SLAB, 64),
                ItemStack.of(Material.STONE_BRICK_SLAB, 64),
                ItemStack.of(Material.STONE_BRICK_WALL, 5),
                ItemStack.of(Material.STONE_SLAB, 64),
                ItemStack.of(Material.STONE_STAIRS, 9),
                ItemStack.of(Material.STONE, 44),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE_SLAB, 16),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64),
                ItemStack.of(Material.STONE, 64)));
    }

    private void render(List<ItemStack> items) {
        if (display.getCanvas().getBufferStrategy() == null) {
            display.getCanvas().createBufferStrategy(3);
        }

        for (int attempts = 0; attempts < 3; attempts++) {
            if (renderFrame(g -> {
                g.setColor(Color.darkGray);
                g.fillRect(0, 0, display.width(), display.height());
                g.drawImage(chest(items.size()), BASE_X_OFFSET, BASE_Y_OFFSET, display.width(), display.height(), null);

                var partition = Lists.partition(items, ROW_SIZE);
                int row = 0;
                for (var itemList : partition) {
                    for (int i = 0; i < itemList.size(); i++) {
                        var currentItem = itemList.get(i);
                        var image = itemImageProvider.getImage(currentItem.getType());
                        var x = BASE_X_OFFSET + (GAP * i);
                        var y = BASE_Y_OFFSET + (GAP * row);
                        g.drawImage(image, x, y, null);
                        g.setColor(Color.WHITE);
                        g.setFont(font);
                        g.drawString(
                                String.valueOf(currentItem.getAmount()),
                                x + image.getWidth() - 20,
                                y + image.getHeight());
                    }
                    row++;
                }
            })) {
                break;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static BufferedImage chest(int size) {
        try {
            if (size >= 27) {
                return ImageIO.read(InventoryRenderer.class.getResource("/large_chest.png"));
            } else {
                return ImageIO.read(InventoryRenderer.class.getResource("/regular_chest.png"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean renderFrame(Consumer<Graphics2D> renderAction) {
        var buffStrat = display.getCanvas().getBufferStrategy();
        if (buffStrat == null) {
            display.getCanvas().createBufferStrategy(3);
            return false;
        }

        var g = buffStrat.getDrawGraphics();

        g.clearRect(0, 0, display.width(), display.height());

        LOG.info("Drawing now");

        renderAction.accept((Graphics2D) g);

        buffStrat.show();
        g.dispose();

        return true;
    }
}
