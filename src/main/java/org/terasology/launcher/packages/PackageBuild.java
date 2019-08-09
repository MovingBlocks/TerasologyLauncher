/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher.packages;

/**
 * Enum for different builds of game package available now.
 */
public enum PackageBuild {
    STABLE("TerasologyStable"),
    UNSTABLE("Terasology"),
    OMEGA_STABLE("DistroOmegaRelease"),
    OMEGA_UNSTABLE("DistroOmega");

    // TODO: Remove this enum when 3rd-party package types are supported

    private final String jobName;

    PackageBuild(String jobName) {
        this.jobName = jobName;
    }

    public String getJobName() {
        return jobName;
    }

    public static PackageBuild byJobName(String jobName) {
        for (PackageBuild packageType : values()) {
            if (packageType.jobName.equals(jobName)) {
                return packageType;
            }
        }
        throw new IllegalArgumentException("Invalid job name: " + jobName);
    }
}
