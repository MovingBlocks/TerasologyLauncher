// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@SuppressWarnings({"PMD.FieldNamingConventions", "checkstyle:ConstantName"})
public final class I18N {

    private static final Logger logger = LoggerFactory.getLogger(I18N.class);

    private static final String LABELS_BUNDLE = "org.terasology.launcher.bundle.LabelsBundle";
    private static final String MESSAGE_BUNDLE = "org.terasology.launcher.bundle.MessageBundle";
    private static final String URI_BUNDLE = "org.terasology.launcher.bundle.URIBundle";
    private static final String IMAGE_BUNDLE = "org.terasology.launcher.bundle.ImageBundle";
    private static final String FXML_BUNDLE = "org.terasology.launcher.bundle.FXMLBundle";

    /**
     * The currently selected locale.
     */
    private static final Locale systemLocale;
    private static final ObjectProperty<Locale> localeProperty;
    private static final List<Locale> supportedLocales;

    /**
     * Build up an index of missing translation to make sure we only log them once per launcher run.
     */
    private static final Multimap<String, Locale> missingTranslations = ArrayListMultimap.create();

    static {
        final Locale czech = new Locale("cs");
        final Locale spanish = new Locale("es");
        final Locale french = new Locale("fr");
        final Locale galician = new Locale("gl");
        final Locale italian = new Locale("it");
        final Locale lithuanian = new Locale("lt");
        final Locale polish = new Locale("pl");
        final Locale russian = new Locale("ru");
        final Locale turkish = new Locale("tr");
        final Locale ukrainian = new Locale("uk");

        supportedLocales = new ArrayList<>();

        supportedLocales.add(Locale.GERMAN);
        supportedLocales.add(Locale.ENGLISH);
        supportedLocales.add(spanish);
        supportedLocales.add(french);
        supportedLocales.add(galician);
        supportedLocales.add(italian);
        supportedLocales.add(Locale.JAPANESE);
        supportedLocales.add(lithuanian);
        supportedLocales.add(polish);
        supportedLocales.add(russian);
        supportedLocales.add(turkish);
        supportedLocales.add(ukrainian);
        supportedLocales.add(czech);

        systemLocale = getDefaultLocale();

        localeProperty = new SimpleObjectProperty<>(getDefaultLocale());
        localeProperty.addListener((observable, oldValue, newValue) -> Locale.setDefault(newValue));
    }

    private I18N() {
    }

    public static Locale getDefaultLocale() {
        return supportedLocales.stream().filter(l -> l.equals(systemLocale)).findAny().orElse(Locale.ENGLISH);
    }

    public static Locale getCurrentLocale() {
        return localeProperty.get();
    }

    public static void setLocale(Locale locale) {
        if (supportedLocales.contains(locale)) {
            localeProperty().setValue(locale);
            Locale.setDefault(locale);
        }
    }

    public static ObjectProperty<Locale> localeProperty() {
        return localeProperty;
    }

    public static List<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    public static String getLabel(String key) {
        return getLabel(getCurrentLocale(), key);
    }

    public static String getLabel(Locale locale, String key) {
        if (!missingTranslations.containsEntry(key, locale)) {
            try {
                String label = ResourceBundle.getBundle(LABELS_BUNDLE, locale).getString(key);
                if (label.length() == 0) {
                    throw new IllegalArgumentException();
                }
                return label;
            } catch (MissingResourceException | IllegalArgumentException e) {
                logger.warn("Missing label translation! locale={}, key={}", locale, key);
                missingTranslations.put(key, locale);
            }
        }
        return ResourceBundle.getBundle(LABELS_BUNDLE, getDefaultLocale()).getString(key);
    }

    public static Binding<String> labelBinding(String key) {
        return Bindings.createStringBinding(() -> getLabel(localeProperty.getValue(), key), localeProperty);
    }

    public static String getMessage(String key, Object... arguments) {
        String pattern;
        try {
            pattern = ResourceBundle.getBundle(MESSAGE_BUNDLE, getCurrentLocale()).getString(key);
            if (pattern.length() == 0) {
                throw new IllegalArgumentException();
            }
        } catch (MissingResourceException | IllegalArgumentException e) {
            logger.error("Missing message translation! key={}, locale={}", key, getCurrentLocale());
            pattern = ResourceBundle.getBundle(MESSAGE_BUNDLE, getDefaultLocale()).getString(key);
        }
        final MessageFormat messageFormat = new MessageFormat(pattern, getCurrentLocale());
        return messageFormat.format(arguments, new StringBuffer(), null).toString();
    }

    //TODO: move to 'Resources' helper class, unrelated to I18n
    public static URI getURI(String key) {
        final String uriStr = ResourceBundle.getBundle(URI_BUNDLE, getCurrentLocale()).getString(key);
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
     * @return the JavaFX image, or null if the image cannot be found or loaded
     */
    public static Image getFxImage(String key) throws MissingResourceException {
        final String imagePath = ResourceBundle.getBundle(IMAGE_BUNDLE, getCurrentLocale()).getString(key);
        URL resource = I18N.class.getResource(imagePath);
        if (resource != null) {
            return new Image(resource.toExternalForm());
        }
        return null;
    }

    public static FXMLLoader getFXMLLoader(String key) {
        return new FXMLLoader(getFXMLUrl(key), ResourceBundle.getBundle("org.terasology.launcher.bundle.LabelsBundle", getCurrentLocale()));
    }

    public static URL getFXMLUrl(String key) {
        final String url = ResourceBundle.getBundle(FXML_BUNDLE, getCurrentLocale()).getString(key);
        return I18N.class.getResource(url);
    }

    public static URL getFXMLUrl(String key, String relative) {
        final String url = ResourceBundle.getBundle(FXML_BUNDLE, getCurrentLocale()).getString(key);
        return I18N.class.getResource(url + '/' + relative);
    }

    public static String getStylesheet(String key) {
        return ResourceBundle.getBundle(FXML_BUNDLE, getCurrentLocale()).getString(key);
    }

    /**
     * Create a tooltip that updates its text automatically with the given string binding.
     *
     * @param text the string binding holding the tooltip text.
     */
    public static Tooltip createTooltip(Binding<String> text) {
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(text);
        return tooltip;
    }
}
