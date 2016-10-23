/*
 * Copyright 2016 MovingBlocks
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public final class Languages {

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    public static final List<Locale> SUPPORTED_LOCALES;
    public static final Map<Locale, String> SETTINGS_LABEL_KEYS;

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

        final List<Locale> supportedLocales = new ArrayList<>();

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

        Comparator comparator = (Object o1, Object o2) -> o1.toString().compareTo(o2.toString());

        final Map<Locale, String> settingsLabelKey = new TreeMap<>(comparator);
        settingsLabelKey.put(Locale.GERMAN, "settings_language_de");
        settingsLabelKey.put(Locale.ENGLISH, "settings_language_en");
        settingsLabelKey.put(spanish, "settings_language_es");
        settingsLabelKey.put(french, "settings_language_fr");
        settingsLabelKey.put(galician, "settings_language_gl");
        settingsLabelKey.put(italian, "settings_language_it");
        settingsLabelKey.put(Locale.JAPANESE, "settings_language_ja");
        settingsLabelKey.put(lithuanian, "settings_language_lt");
        settingsLabelKey.put(polish, "settings_language_pl");
        settingsLabelKey.put(russian, "settings_language_ru");
        settingsLabelKey.put(turkish, "settings_language_tr");
        settingsLabelKey.put(ukrainian, "settings_language_uk");
        settingsLabelKey.put(czech, "settings_language_cs");

        Collections.sort(supportedLocales, (o1, o2) -> o1.toString().compareTo(o2.toString()));

        SUPPORTED_LOCALES = supportedLocales;
        SETTINGS_LABEL_KEYS = settingsLabelKey;
    }

    private static Locale currentLocale = DEFAULT_LOCALE;

    private static final Logger logger = LoggerFactory.getLogger(Languages.class);

    private Languages() {
    }

    public static void init(String localeString) {
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
                if (logger.isDebugEnabled() && !locale.equals(currentLocale)) {
                    logger.debug("An appropriate locale has been found '{}'. Change the current locale from '{}' to '{}'.", defaultLocale, currentLocale, locale);
                }
                currentLocale = locale;
                break;
            }
        }
    }

    public static void update(Locale newLocale) {
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
