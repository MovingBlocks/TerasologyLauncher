// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import org.semver4j.Semver;

public class GameVersionNotSupportedException extends RuntimeException {
    public GameVersionNotSupportedException(Semver engineVersion) {
        this(engineVersion, null);
    }

    public GameVersionNotSupportedException(Semver engineVersion, String message) {
        super(errorMessage(engineVersion, message));
    }

    private static String errorMessage(Semver engineVersion, String additionalInfo) {
        String message = "Unsupported engine version: " + engineVersion.toString();
        String details = ((additionalInfo != null) ? " (" + additionalInfo + ")" : "");
        return message + details;
    }
}
