// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import org.jspecify.annotations.Nullable;

/**
 * Data model for parsing build information from Jenkins.
 *
 * Populated by GSON via reflection, so a missing JSON key just leaves a field null - every
 * reference-type field here is genuinely @Nullable; consumers must null-check.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class Jenkins {
    private Jenkins() {
    }

    public static class ApiResult {
        public Build @Nullable [] builds;
    }

    public static class Build {
        public @Nullable String number;
        public @Nullable Result result;
        public Artifact @Nullable [] artifacts;
        public @Nullable String url;
        public @Nullable ChangeSet changeSet;
        public long timestamp;

        public enum Result {
            ABORTED, FAILURE, NOT_BUILT, SUCCESS, UNSTABLE
        }
    }

    public static class Artifact {
        public @Nullable String fileName;
        public @Nullable String relativePath;
    }

    public static class ChangeSet {
        public Change @Nullable [] items;
    }

    public static class Change {
        public @Nullable String msg;
    }
}
