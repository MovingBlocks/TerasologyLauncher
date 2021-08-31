// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class BundleUtils {

    private static final Logger logger = LoggerFactory.getLogger(BundleUtils.class);

    private static final String LABELS_BUNDLE = "org.terasology.launcher.bundle.LabelsBundle";
    private static final String MESSAGE_BUNDLE = "org.terasology.launcher.bundle.MessageBundle";
    private static final String URI_BUNDLE = "org.terasology.launcher.bundle.URIBundle";
    private static final String IMAGE_BUNDLE = "org.terasology.launcher.bundle.ImageBundle";
    private static final String FXML_BUNDLE = "org.terasology.launcher.bundle.FXMLBundle";

    private BundleUtils() {
    }

    public static String getLabel(String key) {
        return getLabel(Languages.getCurrentLocale(), key);
    }

    public static String getLabel(Locale locale, String key) {
        try {
            String label = ResourceBundle.getBundle(LABELS_BUNDLE, locale).getString(key);
            if (label.length() == 0) {
                throw new IllegalArgumentException();
            }
            return label;
        } catch (MissingResourceException | IllegalArgumentException e) {
            logger.error("Missing label translation! key={}, locale={}", key, locale);
            return ResourceBundle.getBundle(LABELS_BUNDLE, Languages.DEFAULT_LOCALE).getString(key);
        }
    }

    public static Binding<String> labelBinding(Property<Locale> localeProperty, String key) {
        return Bindings.createStringBinding(()-> getLabel(localeProperty.getValue(), key), localeProperty);
    }

    public static String getMessage(String key, Object... arguments) {
        String pattern;
        try {
            pattern = ResourceBundle.getBundle(MESSAGE_BUNDLE, Languages.getCurrentLocale()).getString(key);
            if (pattern.length() == 0) {
                throw new IllegalArgumentException();
            }
        } catch (MissingResourceException | IllegalArgumentException e) {
            logger.error("Missing message translation! key={}, locale={}", key, Languages.getCurrentLocale());
            pattern = ResourceBundle.getBundle(MESSAGE_BUNDLE, Languages.DEFAULT_LOCALE).getString(key);
        }
        final MessageFormat messageFormat = new MessageFormat(pattern, Languages.getCurrentLocale());
        return messageFormat.format(arguments, new StringBuffer(), null).toString();
    }

    public static URI getURI(String key) {
        final String uriStr = ResourceBundle.getBundle(URI_BUNDLE, Languages.getCurrentLocale()).getString(key);
        try {
            return new URI(uriStr);
        } catch (URISyntaxException e) {
            logger.error("Could not create URI '{}' for key '{}'!", uriStr, key, e);
        }
        return null;
    }

    /**
     * Loads a JavaFX {@code Image} from the image path specified by the key in the image bundle file.
     *
     * @param key the key as specified in the image bundle file
     * @return the JavaFX image
     * @throws MissingResourceException if no resource for the specified key can be found
     */
    public static Image getFxImage(String key) throws MissingResourceException {
        final String imagePath = ResourceBundle.getBundle(IMAGE_BUNDLE, Languages.getCurrentLocale()).getString(key);
        return new Image(BundleUtils.class.getResource(imagePath).toExternalForm());
    }

    public static FXMLLoader getFXMLLoader(String key) {
        return new FXMLLoader(getFXMLUrl(key), ResourceBundle.getBundle("org.terasology.launcher.bundle.LabelsBundle", Languages.getCurrentLocale()));
    }

    public static URL getFXMLUrl(String key) {
        final String url = ResourceBundle.getBundle(FXML_BUNDLE, Languages.getCurrentLocale()).getString(key);
        return BundleUtils.class.getResource(url);
    }

    public static URL getFXMLUrl(String key, String relative) {
        final String url = ResourceBundle.getBundle(FXML_BUNDLE, Languages.getCurrentLocale()).getString(key);
        return BundleUtils.class.getResource(url + '/' + relative);
    }

    public static String getStylesheet(String key) {
        return ResourceBundle.getBundle(FXML_BUNDLE, Languages.getCurrentLocale()).getString(key);
    }
}
