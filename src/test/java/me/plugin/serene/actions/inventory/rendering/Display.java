package me.plugin.serene.actions.inventory.rendering;

import com.google.common.io.Resources;
import java.awt.Canvas;
import java.awt.Dimension;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Display {
    private JFrame frame;
    private Canvas canvas;

    private final String title;
    private final int width;
    private final int height;

    private Display(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
    }

    public static Display create(String title, int width, int height) {
        var display = new Display(title, width, height);
        display.prepare();
        return display;
    }

    private void prepare() {
        frame = new JFrame(title);

        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(width, height));
        canvas.setMaximumSize(new Dimension(width, height));
        canvas.setMinimumSize(new Dimension(width, height));

        canvas.setFocusable(false);

        frame.add(canvas);
        frame.pack();

        try {
            frame.setIconImage(ImageIO.read(Resources.getResource("icon.png")));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Canvas getCanvas() { // Standard getter method to return our canvas
        return canvas;
    }

    public JFrame getFrame() {
        return frame;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
