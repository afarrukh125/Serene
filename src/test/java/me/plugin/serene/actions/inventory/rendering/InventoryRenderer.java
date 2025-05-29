package me.plugin.serene.actions.inventory.rendering;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import org.bukkit.inventory.ItemStack;

public class InventoryRenderer {

    public static final int ROW_SIZE = 9;

    public static final int BASE_X_OFFSET = 22;
    public static final int BASE_Y_OFFSET = 50;

    public static final int X_GAP = 54;
    public static final int Y_GAP = 54;

    public static final int BLOCK_IMAGE_WIDTH = 42;
    public static final int BLOCK_IMAGE_HEIGHT = 42;
    public static final Map<RenderingHints.Key, Object> RENDERING_HINTS =
            Map.of(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    private final Font font;
    private final Display display;
    private final ItemImageProvider itemImageProvider;

    @Inject
    public InventoryRenderer(Font font, Display display, ItemImageProvider itemImageProvider) {
        this.font = font;
        this.display = display;
        this.itemImageProvider = itemImageProvider;
    }

    public void render(List<ItemStack> items, Consumer<Graphics> postRenderAction) {
        if (display.getCanvas().getBufferStrategy() == null) {
            display.getCanvas().createBufferStrategy(3);
        }

        for (int attempts = 0; attempts < 3; attempts++) {
            if (renderFrame(g -> {
                g.setColor(Color.darkGray);
                g.fillRect(0, 0, display.width(), display.height());
                g.drawImage(chest(items.size()), 0, 0, display.width(), display.height(), null);

                var partition = Lists.partition(items, ROW_SIZE);
                int row = 0;
                for (var itemList : partition) {
                    for (int i = 0; i < itemList.size(); i++) {
                        var currentItem = itemList.get(i);
                        if (currentItem != null) {
                            var image = itemImageProvider.getImage(currentItem.getType());
                            var x = BASE_X_OFFSET + (X_GAP * i);
                            var y = BASE_Y_OFFSET + (Y_GAP * row);
                            g.drawImage(image, x, y, BLOCK_IMAGE_WIDTH, BLOCK_IMAGE_HEIGHT, null);
                            g.setColor(Color.WHITE);
                            g.setFont(font);
                            g.drawString(
                                    String.valueOf(currentItem.getAmount()),
                                    x + BLOCK_IMAGE_WIDTH - 11,
                                    y + BLOCK_IMAGE_HEIGHT);
                            postRenderAction.accept(g);
                        }
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

    private boolean renderFrame(Consumer<Graphics> renderAction) {
        var buffStrat = display.getCanvas().getBufferStrategy();
        if (buffStrat == null) {
            display.getCanvas().createBufferStrategy(3);
            return false;
        }

        var g = buffStrat.getDrawGraphics();
        var g2d = (Graphics2D) g;
        g2d.addRenderingHints(RENDERING_HINTS);

        g.clearRect(0, 0, display.width(), display.height());

        renderAction.accept(g);

        buffStrat.show();
        g.dispose();

        return true;
    }
}
