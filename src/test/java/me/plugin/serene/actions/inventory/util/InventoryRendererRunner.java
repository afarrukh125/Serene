package me.plugin.serene.actions.inventory.util;

import com.google.inject.Guice;
import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import me.plugin.serene.actions.inventory.InventorySorter;
import me.plugin.serene.actions.inventory.rendering.InventoryRenderer;
import me.plugin.serene.actions.inventory.rendering.InventoryRendererModule;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryRendererRunner {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryRendererRunner.class);
    private static final Color SORT_COLOR = new Color(1, 100, 32);

    public static void main(String... args) {
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
}
