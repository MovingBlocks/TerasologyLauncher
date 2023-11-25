// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import org.semver4j.Semver;

/**
 * Terasology versions which introduce specific features.
 * <p>
 * These features change the way Launcher must interact with Terasology.
 */
public enum VersionHistory {
    /**
     * The preview release of v4.1.0-rc.1 is the first release with LWJGL v3.
     * See <a href="https://github.com/MovingBlocks/Terasology/releases/tag/v4.1.0-rc.1">v4.1.0-rc.1</a>
     */
    LWJGL3("4.1.0-rc.1"),

    /**
     * With the 5.2.0-rc.1 preview release Terasology switches to PICOCLI with POSIX-style command line options.
     * See <a href="https://github.com/MovingBlocks/Terasology/releases/tag/v5.2.0-rc.1">v5.2.0-rc.1</a>
     */
    PICOCLI("5.2.0-SNAPSHOT"),

    /** Since 3aa68c04f192243575f7f78de5b6ce268bb2da1a Terasology requires at least Java 17.
     * See <a href="https://github.com/MovingBlocks/Terasology/commit/3aa68c04f192243575f7f78de5b6ce268bb2da1a">3aa68c04</a>
     */
    JAVA17("6.0.0-SNAPSHOT");

    public final Semver engineVersion;

    VersionHistory(String s) {
        engineVersion = new Semver(s);
    }

    boolean isProvidedBy(Semver version) {
        return version.isGreaterThanOrEqualTo(engineVersion);
    }
}
