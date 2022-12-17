// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

/**
 * Data model for parsing build information from Jenkins.
 *
 * Instances of this class will be created by JSON parsers (e.g., GSON) and are usually not instantiated by hand.
 */
public final class Jenkins {
    public static class ApiResult {
        public Build[] builds;
    }

    public static class Build {
        public String number;
        public Result result;
        public Artifact[] artifacts;
        public String url;
        public ChangeSet changeSet;
        public long timestamp;

        public enum Result {
            ABORTED, FAILURE, NOT_BUILT, SUCCESS, UNSTABLE
        }
    }

    public static class Artifact {
        public String fileName;
        public String relativePath;
    }

    public static class ChangeSet {
        public Change[] items;
    }

    public static class Change {
        public String msg;
    }
}
