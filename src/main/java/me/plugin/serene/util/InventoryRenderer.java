package me.plugin.serene.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InventoryRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryRenderer.class);
    public static final int GAP = 64;
    public static final int ROW_SIZE = 9;
    private final Display display;
    private final Map<String, BufferedImage> cache;

    private InventoryRenderer() {
        this.display = new Display("Inventory Renderer", 1024, 500);
        cache = new HashMap<>();
    }

    public BufferedImage imageFromItem(String material) throws IOException {
        if(cache.containsKey(material)) {
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
                "ACACIA_LEAVES",
                "ACACIA_LEAVES",
                "ACACIA_LEAVES",
                "GRAVEL",
                "GRAVEL",
                "GRAVEL",
                "GRAVEL",
                "GRAVEL",
                "GRAVEL",
                "GRAVEL",
                "GRAVEL",
                "GRAVEL",
                "GRAVEL",
                "EGG",
                "EGG",
                "EGG",
                "EGG",
                "EGG",
                "EGG",
                "EGG",
                "EGG",
                "EGG",
                "COBBLESTONE"));
    }

    private void render(List<String> items) {
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
                    g.setColor(Color.GRAY);
                    g.fillRect(0, 0, display.width(), display.height());
                    var row = 0;
                    for (int i = 0; i < items.size(); i++) {
                        BufferedImage image;
                        try {
                            image = imageFromItem(items.get(i));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        g.drawImage(image, 10 + (GAP * (i % ROW_SIZE)), 10 + (GAP * row), null);
                        if (i > 0 && i % ROW_SIZE == 0) {
                            row++;
                        }
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
