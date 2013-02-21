package org.terasologylauncher.gui;

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class TransparentPanel extends JPanel {

    private float transparency = 1f;

    public TransparentPanel(float transparency) {
        this(transparency, Color.BLACK);
    }

    public TransparentPanel(float transparency, Color color) {
        this.transparency = transparency;
        this.setBorder(null);
        this.setOpaque(true);
        this.setBackground(color);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D copy = (Graphics2D) g.create();
        copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        super.paint(copy);
        copy.dispose();
    }
}
