// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.jre;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.launcher.platform.Platform;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ManagedJRETest {

    @ParameterizedTest(name = "JRE in version {1} available for platform {0}")
    @MethodSource("provideSupportedPlatforms")
    void testJresForAllSupportedPlatforms(Platform platform, Integer javaVersion) {
        assertDoesNotThrow(() -> ManagedJRE.getJreFor(platform, javaVersion));
    }

    private static Stream<Arguments> provideSupportedPlatforms() {
        return Platform.SUPPORTED_PLATFORMS.stream().flatMap(p -> ManagedJRE.SUPPORTED_JAVA_VERSIONS.stream().map(v -> Arguments.of(p, v)));
    }
}