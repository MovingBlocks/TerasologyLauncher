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

package org.terasology.launcher.util;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public final class TestLanguages {

    public TestLanguages() {
    }

    @Test
    public void testDefaultLanguage() {
        assertEquals(Locale.ENGLISH, Languages.DEFAULT_LOCALE);
    }

    @Test
    public void testUpdateWithDefault() {
        Languages.update(Languages.DEFAULT_LOCALE);
        assertSame(Languages.DEFAULT_LOCALE, Languages.getCurrentLocale());
    }

    @Test
    public void testUpdateWithEnglish() {
        Languages.update(Locale.ENGLISH);
        assertSame(Locale.ENGLISH, Languages.getCurrentLocale());
    }

    @Test
    public void testUpdateWithGerman() {
        Languages.update(Locale.GERMAN);
        assertSame(Locale.GERMAN, Languages.getCurrentLocale());
    }

    @Test
    public void testUpdateWithGermany() {
        Languages.update(Languages.DEFAULT_LOCALE);
        Languages.update(Locale.GERMANY);
        assertSame(Languages.DEFAULT_LOCALE, Languages.getCurrentLocale());
    }

    @Test
    public void testUpdateWithJapanese() {
        Languages.update(Languages.DEFAULT_LOCALE);
        Languages.update(Locale.JAPANESE);
        assertSame(Languages.DEFAULT_LOCALE, Languages.getCurrentLocale());
    }

    @Test
    public void testInitWithDefault() {
        Languages.init(Languages.DEFAULT_LOCALE.toString());
        assertSame(Languages.DEFAULT_LOCALE, Languages.getCurrentLocale());
    }

    @Test
    public void testInitWithEnglish() {
        Languages.init(Locale.ENGLISH.toString());
        assertSame(Locale.ENGLISH, Languages.getCurrentLocale());
    }

    @Test
    public void testInitWithGerman() {
        Languages.init(Locale.GERMAN.toString());
        assertSame(Locale.GERMAN, Languages.getCurrentLocale());
    }

    @Test
    public void testInitWithGermany() {
        Languages.init(Languages.DEFAULT_LOCALE.toString());
        Languages.init(Locale.GERMANY.toString());
        assertSame(Languages.DEFAULT_LOCALE, Languages.getCurrentLocale());
    }

    @Test
    public void testInitWithJapanese() {
        Languages.init(Languages.DEFAULT_LOCALE.toString());
        Languages.init(Locale.JAPANESE.toString());
        assertSame(Languages.DEFAULT_LOCALE, Languages.getCurrentLocale());
    }

    @Test
    public void testSettingsLabelKeysSize() {
        assertEquals(2, Languages.SETTINGS_LABEL_KEYS.size());
    }

    @Test
    public void testSupportedLocalesSize() {
        assertEquals(2, Languages.SUPPORTED_LOCALES.size());
    }
}
