package org.terasologyLauncher.gui;

import javax.swing.*;
import java.awt.*;

public class TransparentJScrollpane extends JScrollPane {

    public TransparentJScrollpane(Component view) {
        super(view);
    }

    @Override
    protected void paintComponent(Graphics g) {
        paintChildren(g);
    }
}
