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

package org.terasology.launcher.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

final class LinkJLabel extends JLabel implements MouseListener {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(LinkJLabel.class);

    private static final long CLICK_DELAY = 200L;
    private static final Color HOVER_COLOR = new Color(0x696969);
    private static final Color STANDARD_COLOR = new Color(0xd1d1d1);

    private long lastClicked;
    private URI uri;
    private Color hoverColor;
    private Color standardColor;

    public LinkJLabel() {
        lastClicked = 0;
        standardColor = STANDARD_COLOR;
        hoverColor = HOVER_COLOR;

        setForeground(standardColor);
        addMouseListener(this);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    Color getHoverColor() {
        return hoverColor;
    }

    void setHoverColor(final Color hoverColor) {
        this.hoverColor = hoverColor;
    }

    Color getStandardColor() {
        return standardColor;
    }

    void setStandardColor(final Color standardColor) {
        this.standardColor = standardColor;
    }

    URI getUri() {
        return uri;
    }

    public void setUri(final URI uri) {
        this.uri = uri;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        if ((lastClicked + CLICK_DELAY) > System.currentTimeMillis()) {
            return;
        }
        lastClicked = System.currentTimeMillis();

        if (uri != null && Desktop.isDesktopSupported()) {
            final Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                } catch (Exception ex) {
                    logger.error("Can't browse URI! " + uri, ex);
                }
            }
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) {
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        setForeground(hoverColor);
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        setForeground(standardColor);
    }
}
