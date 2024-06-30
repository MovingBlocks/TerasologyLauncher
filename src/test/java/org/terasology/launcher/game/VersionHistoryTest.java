// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import org.semver4j.Semver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** test edge cases in Semver comparison */
class VersionHistoryTest {

    @ParameterizedTest
    @ValueSource(strings = {"5.2.0-SNAPSHOT", "5.2.0", "5.2.1", "6.0.0"})
    void hasPicocli(String version) {
        assertTrue(VersionHistory.PICOCLI.isProvidedBy(new Semver(version)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"5.1.0", "5.1.0-SNAPSHOT", "5.1.1"})
    void lacksPicocli(String version) {
        assertFalse(VersionHistory.PICOCLI.isProvidedBy(new Semver(version)));
    }

}
