// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import java.util.Date;

/**
 * Data model for parsing build information from Jenkins.
 */
public final class Jenkins {

    private Jenkins() {

    }

    public static class ApiResult {
        public Build[] builds;
        public Project[] upstreamProjects;
    }

    public static class Build {
        public Action[] actions;
        public String  number;
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

    public static class Action {
        public Cause[] causes;
    }

    public  static class Cause {
        public String upstreamProject;
        public String upstreamBuild;
    }

    public  static class Project {
        public String name;
    }
}
