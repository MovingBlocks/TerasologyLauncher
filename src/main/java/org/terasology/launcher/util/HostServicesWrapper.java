package org.terasology.launcher.util;

import javafx.application.HostServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/**
 * Provide capabilities to interact with the hosting system (OS).
 * <p>
 * For instance, this allows to open a URL in the default browser on the system.
 * <p>
 * This abstraction is necessary as {@link javafx.application.HostServices} may not be available and we want to use
 * {@link java.awt.Desktop} as a fallback solution.
 */
public class HostServicesWrapper {

    private static final Logger logger = LoggerFactory.getLogger(HostServicesWrapper.class);

    private final Desktop desktop;
    private final HostServices hostServices;

    /**
     * Create a new {@link javafx.application.HostServices} wrapper.
     * <p>
     * If the given host service instance is {@code null}
     *
     * @param hostServices a JavaFX host services instance or null
     */
    public HostServicesWrapper(HostServices hostServices) {
        this.hostServices = hostServices;

        if (hostServices == null && Desktop.isDesktopSupported()) {
            Desktop d = Desktop.getDesktop();
            if (d.isSupported(Desktop.Action.BROWSE)) {
                desktop = d;
            } else {
                desktop = null;
            }
        } else {
            desktop = null;
        }
    }

    /**
     * Attempt to open the given URI with the default browser.
     * <p>
     * The preferred way to open the URI is via the {@link javafx.application.HostServices}, as a fallback
     * {@link java.awt.Desktop} may be used internally.
     * <p>
     * In case the URI cannot be opened (e.g., because neither HostServices nor Desktop are available) this method will
     * fail silently.
     *
     * @param uri the URI to open
     */
    public void tryOpenUri(URI uri) {
        if (hostServices != null) {
            hostServices.showDocument(uri.toString());
        } else if (desktop != null) {
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                logger.warn("Unable to open URI with 'Browse' action", e);
            }
        }
    }
}
