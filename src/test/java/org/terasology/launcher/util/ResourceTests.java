// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ApplicationExtension.class)
class ResourceTests {

    private static Stream<Arguments> provideSupportedLocales() {
        return I18N.getSupportedLocales().stream().map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("provideSupportedLocales")
    void ensureAllSupportedLocalesHaveFlagIcons(Locale locale) {
        assertNotNull(I18N.getFxImage("flag_" + locale.toLanguageTag()));
    }
}
