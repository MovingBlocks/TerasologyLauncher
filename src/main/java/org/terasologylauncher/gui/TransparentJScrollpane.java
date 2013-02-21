package org.terasologylauncher.gui;

import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Graphics;

public class TransparentJScrollpane extends JScrollPane {

    public TransparentJScrollpane(Component view) {
        super(view);
    }

    @Override
    protected void paintComponent(Graphics g) {
        paintChildren(g);
    }
}
