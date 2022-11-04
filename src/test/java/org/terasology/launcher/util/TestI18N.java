// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class TestI18N {

    TestI18N() {
    }

    @Test
    void testDefaultLanguage() {
        assertEquals(Locale.ENGLISH, I18N.getDefaultLocale());
    }

    @Test
    void testUpdateWithDefault() {
        I18N.setLocale(I18N.getDefaultLocale());
        assertSame(I18N.getDefaultLocale(), I18N.getCurrentLocale());
    }

    @Test
    void testUpdateWithEnglish() {
        I18N.setLocale(Locale.ENGLISH);
        assertSame(Locale.ENGLISH, I18N.getCurrentLocale());
    }

    @Test
    void testUpdateWithGerman() {
        I18N.setLocale(Locale.GERMAN);
        assertSame(Locale.GERMAN, I18N.getCurrentLocale());
    }

    @Test
    void testUpdateWithGermany() {
        I18N.setLocale(I18N.getDefaultLocale());
        I18N.setLocale(Locale.GERMANY);
        assertSame(I18N.getDefaultLocale(), I18N.getCurrentLocale());
    }

    @Test
    void testUpdateWithJapanese() {
        I18N.setLocale(Locale.JAPANESE);
        assertSame(Locale.JAPANESE, I18N.getCurrentLocale());
    }

    @Test
    void testUpdateWithJapan() {
        I18N.setLocale(I18N.getDefaultLocale());
        I18N.setLocale(Locale.JAPAN);
        assertSame(I18N.getDefaultLocale(), I18N.getCurrentLocale());
    }

}
