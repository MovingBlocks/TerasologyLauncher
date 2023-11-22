// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import com.vdurmont.semver4j.Semver;

public class GameVersionNotSupportedException extends RuntimeException {
    private final Semver engineVersion;

    public GameVersionNotSupportedException(Semver engineVersion) {
        this.engineVersion = engineVersion;
    }

    @Override
    public String getMessage() {
        return "Unsupported engine version: " + engineVersion.toString();
    }
}
