/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import javax.swing.JButton;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

/**
 * Extends the standard JButton with linking capabilities, opening a browser with a specific URL (if supported by the
 * system).
 */
final class LinkJButton extends JButton {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(LinkJButton.class);

    private URI uri;

    public LinkJButton() {
        addActionListener(new ButtonClickHandler());
        setBorder(null);
        setOpaque(false);
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(final URI uri) {
        this.uri = uri;
    }

    private class ButtonClickHandler implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent event) {
            if ((uri != null) && Desktop.isDesktopSupported()) {
                final Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(uri);
                    } catch (IOException | RuntimeException e) {
                        logger.error("Can't browse URI! " + uri, e);
                    }
                }
            }
        }
    }
}
