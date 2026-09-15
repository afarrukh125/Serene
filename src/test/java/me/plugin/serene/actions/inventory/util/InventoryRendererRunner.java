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
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryRendererRunner {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryRendererRunner.class);
    private static final Color SORT_COLOR = new Color(1, 100, 32);

    public static void main(String... args) {
        var injector = Guice.createInjector(new InventoryRendererModule());
        var inventoryRenderer = injector.getInstance(InventoryRenderer.class);
        var unsortedInventory = SampleInventories.STONE_HAUL;

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
