package me.plugin.serene.actions.inventory.rendering;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import me.plugin.serene.actions.inventory.InventorySorter;
import me.plugin.serene.actions.inventory.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryRenderer {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryRenderer.class);

    public static final int ROW_SIZE = 9;

    public static final int BASE_X_OFFSET = 22;
    public static final int BASE_Y_OFFSET = 50;

    public static final int X_GAP = 54;
    public static final int Y_GAP = 54;

    public static final int BLOCK_IMAGE_WIDTH = 42;
    public static final int BLOCK_IMAGE_HEIGHT = 42;
    public static final Color SORT_COLOR = new Color(1, 100, 32);

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
        var unsortedInventory = List.of(
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
                ItemStack.of(Material.STONE, 64));

        var timer = new Timer();
        var atomicInteger = new AtomicInteger();
        var inventorySorter = new InventorySorter();
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        List<ItemStack> items;
                        Consumer<Graphics> postRenderAction;
                        var iteration = atomicInteger.getAndIncrement();
                        if (iteration % 3 == 0) {
                            LOG.info("Re-rendering original inventory");
                            items = unsortedInventory;
                            postRenderAction = drawInfoText(Color.RED, "Original inventory");

                        } else {
                            LOG.info("Re-rendering organised inventory");
                            items = InventoryUtils.setupFinalOrganisedInventory(
                                    inventorySorter, unsortedInventory.toArray(ItemStack[]::new));
                            postRenderAction = drawInfoText(SORT_COLOR, "Sort %s".formatted(iteration % 3));
                        }
                        inventoryRenderer.render(items, postRenderAction);
                    }
                },
                0,
                2500);
    }

    private static Consumer<Graphics> drawInfoText(Color col, String message) {
        return g -> {
            g.setColor(col);
            g.drawString(message, 220, 30);
        };
    }

    private void render(List<ItemStack> items, Consumer<Graphics> postRenderAction) {
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

        g.clearRect(0, 0, display.width(), display.height());

        renderAction.accept(g);

        buffStrat.show();
        g.dispose();

        return true;
    }
}
