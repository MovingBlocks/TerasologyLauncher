/*
 * Copyright 2020 MovingBlocks
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

package org.terasology.launcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.URI;

/**
 * Provide capabilities to interact with the hosting system (OS).
 * <p>
 * For instance, this allows to open a URL in the default browser on the system.
 */
public class HostServices {

    private static final Logger logger = LoggerFactory.getLogger(HostServices.class);

    private final Desktop desktop;

    public HostServices() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            desktop = Desktop.getDesktop();
        } else {
            logger.info("java.awt.Desktop is not supported or does not support BROWSE action - cannot open links in browser");
            desktop = null;
        }
    }

    /**
     * Attempt to open the given URI with the default browser.
     * <p>
     * In case the URI cannot be opened this method will fail silently.
     *
     * @param uri the URI to open
     */
    public void tryOpenUri(URI uri) {
        if (desktop != null) {
            EventQueue.invokeLater(() -> {
                try {
                    desktop.browse(uri);
                } catch (IOException e) {
                    logger.warn("Unable to open URI '{}': {}", uri.toString(), e.getMessage());
                }
            });
        }
    }
}
