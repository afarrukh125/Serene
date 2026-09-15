package me.plugin.serene.actions.inventory.rendering;

import static me.plugin.serene.actions.inventory.CompactingSortingStrategy.FillDirection.ROW_MAJOR;
import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.plugin.serene.actions.PlayerTest;
import me.plugin.serene.actions.inventory.CompactingSortingStrategy;
import me.plugin.serene.actions.inventory.InventorySorter;
import me.plugin.serene.actions.inventory.util.InventoryUtils;
import me.plugin.serene.actions.inventory.util.SampleInventories;
import me.plugin.serene.model.MaterialItemStack;
import me.plugin.serene.util.Utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/**
 * Exercises the renderer's drawing path against an offscreen image so it runs in a headless build, where the
 * {@link Display}-backed {@link InventoryRenderer#render} cannot.
 */
public class InventoryRendererTest extends PlayerTest {

    private static final int WIDTH = 513;
    private static final int HEIGHT = 380;

    private static final ItemImageProvider SOLID_RED = material -> {
        var image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, 16, 16);
        graphics.dispose();
        return image;
    };

    private static BufferedImage drawSorted(List<ItemStack> unsorted) {
        var renderer = new InventoryRenderer(new Font(Font.MONOSPACED, Font.PLAIN, 10), null, SOLID_RED);
        var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        renderer.draw(graphics, sort(unsorted), WIDTH, HEIGHT, g -> {});
        graphics.dispose();
        return image;
    }

    private static List<ItemStack> sort(List<ItemStack> unsorted) {
        var groups = new InventorySorter()
                .getOrganisedGroups(InventoryUtils.inventoryOf(unsorted.toArray(ItemStack[]::new)));
        var grid = new CompactingSortingStrategy(ROW_MAJOR)
                .sort(groups, new ItemStack[6][InventorySorter.ROW_SIZE], new ArrayList<MaterialItemStack>());
        return Arrays.asList(Utils.flatten(grid, ItemStack[]::new));
    }

    private static boolean hasRedNear(BufferedImage image, int x, int y) {
        for (var dx = 0; dx < InventoryRenderer.BLOCK_IMAGE_WIDTH; dx++) {
            for (var dy = 0; dy < InventoryRenderer.BLOCK_IMAGE_HEIGHT; dy++) {
                var colour = new Color(image.getRGB(x + dx, y + dy));
                if (colour.getRed() > 150 && colour.getGreen() < 80 && colour.getBlue() < 80) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int slotX(int column) {
        return InventoryRenderer.BASE_X_OFFSET + InventoryRenderer.X_GAP * column;
    }

    private static int slotY(int row) {
        return InventoryRenderer.BASE_Y_OFFSET + InventoryRenderer.Y_GAP * row;
    }

    @Test
    void drawsAnItemIntoEveryOccupiedSlotAndNothingIntoEmptyOnes() {
        var image = drawSorted(SampleInventories.STONE_HAUL);

        // The stone haul merges down to 34 stacks, so rows 0 to 2 are full and row 3 is partly filled.
        assertThat(hasRedNear(image, slotX(0), slotY(0))).isTrue();
        assertThat(hasRedNear(image, slotX(8), slotY(0))).isTrue();
        assertThat(hasRedNear(image, slotX(6), slotY(3))).isTrue();
        assertThat(hasRedNear(image, slotX(7), slotY(3))).isFalse();
        assertThat(hasRedNear(image, slotX(0), slotY(4))).isFalse();
    }

    @Test
    void drawsNothingForACompletelyEmptyInventory() {
        var image = drawSorted(List.of());

        for (var row = 0; row < 6; row++) {
            for (var column = 0; column < 9; column++) {
                assertThat(hasRedNear(image, slotX(column), slotY(row)))
                        .as("slot at row %d column %d is empty", row, column)
                        .isFalse();
            }
        }
    }

    @Test
    void drawsASingleItemIntoTheFirstSlotOnly() {
        var image = drawSorted(List.of(ItemStack.of(Material.DIAMOND, 1)));

        assertThat(hasRedNear(image, slotX(0), slotY(0))).isTrue();
        assertThat(hasRedNear(image, slotX(1), slotY(0))).isFalse();
    }
}
