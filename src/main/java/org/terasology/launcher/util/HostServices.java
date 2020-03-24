package org.terasology.launcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
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
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                logger.warn("Unable to open URI with 'Browse' action", e);
            }
        }
    }
}
