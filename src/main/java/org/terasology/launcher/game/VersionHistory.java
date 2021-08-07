// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import com.vdurmont.semver4j.Requirement;
import com.vdurmont.semver4j.Semver;
import org.terasology.launcher.model.GameIdentifier;

/**
 * Terasology versions which introduce specific features.
 * <p>
 * These features change the way Launcher must interact with Terasology.
 */
public enum VersionHistory {
    LWJGL3("[4.1.0,)"),
    PICOCLI("[5.1.0,)");

    public final Requirement engineVersion;

    VersionHistory(String s) {
        engineVersion = Requirement.buildIvy(s);
    }

    boolean isProvidedBy(Semver version) {
        return engineVersion.isSatisfiedBy(version);
    }

    boolean isProvidedBy(GameIdentifier version) {
        return isProvidedBy(version.getEngineVersion());
    }
}
