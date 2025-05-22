package me.plugin.serene.actions.inventory.util;

import java.awt.Canvas;
import java.awt.Dimension;
import javax.swing.JFrame;

public class Display {
    private JFrame frame;
    private Canvas canvas;

    private final String title;
    private final int width;
    private final int height;

    public Display(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;

        createDisplay();
    }

    private void createDisplay() {
        frame = new JFrame(title);

        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(width, height));
        canvas.setMaximumSize(new Dimension(width, height));
        canvas.setMinimumSize(new Dimension(width, height));

        canvas.setFocusable(
                false); // Allows application to only focus on JFrame in order to allow keyboard inputs to work

        frame.add(canvas);
        frame.pack();
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
