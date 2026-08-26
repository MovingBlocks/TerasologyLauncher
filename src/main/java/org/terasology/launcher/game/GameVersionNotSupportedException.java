// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

import java.util.Objects;

public class GameVersionNotSupportedException extends RuntimeException {
    public GameVersionNotSupportedException(Semver engineVersion) {
        this(engineVersion, null);
    }

    public GameVersionNotSupportedException(Semver engineVersion, @Nullable String message) {
        super(errorMessage(engineVersion, message));
    }

    // errorMessage() always builds a real string, so unlike Throwable.getMessage() this is never null -
    // callers (e.g. ApplicationController) show it directly.
    @Override
    public String getMessage() {
        return Objects.requireNonNull(super.getMessage());
    }

    private static String errorMessage(Semver engineVersion, @Nullable String additionalInfo) {
        String message = "Unsupported engine version: " + engineVersion.toString();
        String details = ((additionalInfo != null) ? " (" + additionalInfo + ")" : "");
        return message + details;
    }
}
