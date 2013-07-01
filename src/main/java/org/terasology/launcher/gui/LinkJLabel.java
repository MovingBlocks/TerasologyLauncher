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
    private static final Color HOVER_COLOR = Color.DARK_GRAY;
    private static final Color STANDARD_COLOR = Color.LIGHT_GRAY;

    private long lastClicked;

    private URI uri;

    public LinkJLabel() {
        lastClicked = 0;
        setForeground(STANDARD_COLOR);
        addMouseListener(this);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public URI getUri() {
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
        setForeground(HOVER_COLOR);
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        setForeground(STANDARD_COLOR);
    }
}
