// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import com.vdurmont.semver4j.Semver;
import org.terasology.launcher.model.GameIdentifier;

/**
 * Terasology versions which introduce specific features.
 * <p>
 * These features change the way Launcher must interact with Terasology.
 */
public enum VersionHistory {
    /**
     * The preview release of v4.1.0-rc.1 is the first release with LWJGL v3.
     * See https://github.com/MovingBlocks/Terasology/releases/tag/v4.1.0-rc.1
     */
    LWJGL3("4.1.0-rc.1"),
    PICOCLI("5.2.0-SNAPSHOT");

    public final Semver engineVersion;

    VersionHistory(String s) {
        engineVersion = new Semver(s, Semver.SemverType.IVY);
    }

    boolean isProvidedBy(Semver version) {
        return version.isGreaterThanOrEqualTo(engineVersion);
    }

    boolean isProvidedBy(GameIdentifier version) {
        return isProvidedBy(version.getVersion());  // FIXME ASAP: obsoleted by PR#654
    }
}
