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

package org.terasology.launcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Languages {

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    public static final List<Locale> SUPPORTED_LOCALES;
    public static final Map<Locale, String> SETTINGS_LABEL_KEYS;

    static {
        final List<Locale> supportedLocales = new ArrayList<>(2);
        supportedLocales.add(Locale.ENGLISH);
        supportedLocales.add(Locale.GERMAN);

        final Map<Locale, String> settingsLabelKey = new HashMap<>();
        settingsLabelKey.put(Locale.ENGLISH, "settings_language_en");
        settingsLabelKey.put(Locale.GERMAN, "settings_language_de");

        SUPPORTED_LOCALES = Collections.unmodifiableList(supportedLocales);
        SETTINGS_LABEL_KEYS = Collections.unmodifiableMap(settingsLabelKey);
    }

    private static Locale currentLocale = DEFAULT_LOCALE;

    private static final Logger logger = LoggerFactory.getLogger(Languages.class);

    private Languages() {
    }

    public static void init(final String localeString) {
        for (Locale locale : SUPPORTED_LOCALES) {
            if (locale.toString().equals(localeString)) {
                currentLocale = locale;
                break;
            }
        }
    }

    public static void init() {
        final Locale defaultLocale = Locale.getDefault();
        for (Locale locale : SUPPORTED_LOCALES) {
            if (locale.getLanguage().equals(defaultLocale.getLanguage())) {
                if (!locale.equals(currentLocale)) {
                    logger.debug("An appropriate locale has been found '{}'. "
                        + "Change the current locale from '{}' to '{}'.", defaultLocale, currentLocale, locale);
                }
                currentLocale = locale;
                break;
            }
        }
    }

    public static void update(final Locale newLocale) {
        if (SUPPORTED_LOCALES.contains(newLocale)) {
            currentLocale = newLocale;
        } else {
            logger.warn("Unsupported locale '{}'.", newLocale);
        }
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }
}
