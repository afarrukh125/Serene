package me.plugin.serene.actions.inventory.util;

import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InventoryRenderer {

    public static final Map<RenderingHints.Key, Object> RENDERING_HINTS_MAP = Map.of(
            RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
            RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY,
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    private static final Logger LOG = LoggerFactory.getLogger(InventoryRenderer.class);
    public static final int GAP = 64;
    public static final int ROW_SIZE = 9;
    public static final int BASE_X_OFFSET = 10;
    public static final int BASE_Y_OFFSET = 10;
    private final Display display;
    private final Map<String, BufferedImage> cache;
    private static final Font MINECRAFT_FONT;
    static {
        try {
            MINECRAFT_FONT = Font.createFont(Font.PLAIN, InventoryRenderer.class.getResourceAsStream("/minecraft_font.ttf")).deriveFont(14f);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InventoryRenderer() {
        this.display = new Display("Inventory Renderer", 1024, 500);
        cache = new HashMap<>();
    }

    public BufferedImage imageFromItem(String material) throws IOException {
        if (cache.containsKey(material)) {
            return cache.get(material);
        }
        var fileName = "minecraft_" + material.toLowerCase() + ".png";
        var path = "/items_1.21.5/%s".formatted(fileName);
        System.out.printf("Resolving path %s for material %s%n", path, material);
        var image = ImageIO.read(InventoryRenderer.class.getResourceAsStream(path));
        cache.put(material, image);
        return image;
    }

    public static void main(String[] args) {
        var inventoryRenderer = new InventoryRenderer();
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
                    // g.setRenderingHints(RENDERING_HINTS_MAP);
                    g.setColor(Color.darkGray);
                    g.fillRect(0, 0, display.width(), display.height());
                    var partition = Lists.partition(items, ROW_SIZE);
                    int row = 0;
                    for (var itemList : partition) {
                        for (int i = 0; i < itemList.size(); i++) {
                            var currentItem = itemList.get(i);
                            BufferedImage image;
                            try {
                                image = imageFromItem(currentItem.getType().name());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            var x = BASE_X_OFFSET + (GAP * i);
                            var y = BASE_Y_OFFSET + (GAP * row);
                            g.drawImage(image, x, y, null);
                            g.setColor(Color.WHITE);
                            g.setFont(MINECRAFT_FONT);
                            g.drawString("" + currentItem.getAmount(), x + image.getWidth() - 20,y + image.getHeight());
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

        System.out.println("Drawing now");

        renderAction.accept((Graphics2D) g);

        buffStrat.show();
        g.dispose();
    }
}
