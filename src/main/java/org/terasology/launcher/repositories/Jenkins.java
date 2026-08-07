// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

/**
 * Data model for parsing build information from Jenkins.
 *
 * Instances of this class will be created by JSON parsers (e.g., GSON) and are usually not instantiated by hand.
 */
public final class Jenkins {
    // GSON populates these fields via reflection, bypassing any constructor - and leaves a field null
    // if its key is simply absent from the response, which is out of our control. Rather than mark
    // every field @Nullable and chase null-checks through every existing consumer (a bigger, separate
    // change), each nested class opts out of NullAway's "was this initialized" check the same way you
    // would for any other reflection-populated DTO.
    // (A shared constant, not five repeated literals, to keep PMD's AvoidDuplicateLiterals happy.)
    static final String SUPPRESS_NULLAWAY_INIT = "NullAway.Init";

    @SuppressWarnings(Jenkins.SUPPRESS_NULLAWAY_INIT)
    public static class ApiResult {
        public Build[] builds;
    }

    @SuppressWarnings(Jenkins.SUPPRESS_NULLAWAY_INIT)
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

    @SuppressWarnings(Jenkins.SUPPRESS_NULLAWAY_INIT)
    public static class Artifact {
        public String fileName;
        public String relativePath;
    }

    @SuppressWarnings(Jenkins.SUPPRESS_NULLAWAY_INIT)
    public static class ChangeSet {
        public Change[] items;
    }

    @SuppressWarnings(Jenkins.SUPPRESS_NULLAWAY_INIT)
    public static class Change {
        public String msg;
    }
}
