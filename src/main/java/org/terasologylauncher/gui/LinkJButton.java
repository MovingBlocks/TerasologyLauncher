/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasologylauncher.gui;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Extends the standard JButton with linking capabilities, opening a browser with a specific URL (if supported by the system).
 *
 * @author Skaldarnar
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
