package org.terasologylauncher.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 11.02.13
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */
public class LinkJButton extends JButton {

    private final String url;
    private ImageIcon hoverIcon;

    public LinkJButton(String url) {
        this.url = url;
        this.addActionListener(new ButtonClickHandler());
        setBorder(null);
        setOpaque(false);
    }

    private class ButtonClickHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            java.net.URI uri = null;
            try {
                uri = new java.net.URI(url);
                browse(uri);
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }
        }

        private void browse(java.net.URI uri) {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
