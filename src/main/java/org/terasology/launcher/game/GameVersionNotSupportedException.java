// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import org.semver4j.Semver;

public class GameVersionNotSupportedException extends RuntimeException {
    private final Semver engineVersion;
    private final String message;

    public GameVersionNotSupportedException(Semver engineVersion) {
        this(engineVersion, null);
    }

    public GameVersionNotSupportedException(Semver engineVersion, String message) {
        this.engineVersion = engineVersion;
        this.message = message;
    }

    @Override
    public String getMessage() {
        String details = (message != null) ? " (" + message + ")" : "";
        return "Unsupported engine version: " + engineVersion.toString() + details;
    }
}
