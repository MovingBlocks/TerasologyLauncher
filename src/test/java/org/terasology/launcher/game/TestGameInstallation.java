// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.launcher.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semver4j.Semver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestGameInstallation {

    private static Path writeEngineJar(Path libsDir, String jarFilename, String versionInfoContents) throws IOException {
        Files.createDirectories(libsDir);
        Path jar = libsDir.resolve(jarFilename);
        try (var out = new JarOutputStream(Files.newOutputStream(jar))) {
            if (versionInfoContents != null) {
                out.putNextEntry(new JarEntry("org/terasology/engine/version/versionInfo.properties"));
                out.write(versionInfoContents.getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return jar;
    }

    @Test
    void readsEngineVersionFromVersionInfoWhenPresent(@TempDir Path installDir) throws IOException {
        writeEngineJar(installDir.resolve("libs"), "engine-5.4.0-SNAPSHOT.jar", "engineVersion=5.4.1\n");

        Semver engineVersion = GameInstallation.getEngineVersion(installDir);

        assertEquals(new Semver("5.4.1"), engineVersion);
    }

    @Test
    void fallsBackToFilenameWhenVersionInfoMissing(@TempDir Path installDir) throws IOException {
        // Reproduces what a real GitHub release build looked like (v5.4.0-rc.1): a runnable engine
        // jar with no versionInfo.properties entry at all inside it.
        writeEngineJar(installDir.resolve("libs"), "engine-5.4.0-rc.1.jar", null);

        Semver engineVersion = GameInstallation.getEngineVersion(installDir);

        assertEquals(new Semver("5.4.0-rc.1"), engineVersion);
    }
}
