package org.terasologylauncher.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Skaldarnar
 */
public class LinkJLabel extends JLabel implements MouseListener {

    private static final long CLICK_DELAY = 200L;
    private long lastClicked = System.currentTimeMillis();

    private static Color HOVER_COLOR = Color.DARK_GRAY;
    private static Color STANDARD_COLOR = Color.LIGHT_GRAY;

    private String url;

    public LinkJLabel(String text, String url) {
        super(text);
        setForeground(STANDARD_COLOR);
        this.url = url;
        super.addMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (lastClicked + CLICK_DELAY > System.currentTimeMillis()){
            return;
        }
        lastClicked = System.currentTimeMillis();
        try {
            URI uri = new URI(url);
            browse(uri);
        } catch (URISyntaxException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void browse(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setForeground(HOVER_COLOR);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setForeground(STANDARD_COLOR);
    }
}
