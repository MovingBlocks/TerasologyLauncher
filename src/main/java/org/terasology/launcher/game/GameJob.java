/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.launcher.game;

/**
 * Enum for available job lines from Jenkins we track.
 */
public enum GameJob {

    TerasologyStable("master", "DistroOmegaRelease", 49, 5, true, false, "RELEASE", "infoHeader1_TerasologyStable", "settings_game_buildType_TerasologyStable"),

    Terasology("develop", "DistroOmega", 1355, 40, false, false, "DEVELOP", "infoHeader1_Terasology", "settings_game_buildType_Terasology");

    private final String gitBranch;
    private final String omegaJobName;
    private final int minBuildNumber;
    private final int prevBuildNumbers;
    private final boolean stable;
    private final boolean onlyInstalled;
    private final String installationDirectory;
    private final String infoMessageKey;
    private final String settingsKey;

    GameJob(String gitBranch, String omegaJob, int minBuildNumber, int prevBuildNumbers, boolean stable, boolean onlyInstalled, String installationDirectory, String infoMessageKey,
                    String settingsKey) {
        this.gitBranch = gitBranch;
        this.omegaJobName = omegaJob;
        this.minBuildNumber = minBuildNumber;
        this.prevBuildNumbers = prevBuildNumbers;
        this.stable = stable;
        this.onlyInstalled = onlyInstalled;
        this.installationDirectory = installationDirectory;
        this.infoMessageKey = infoMessageKey;
        this.settingsKey = settingsKey;
    }

    public final String getGitBranch() {
        return gitBranch;
    }

    public final String getOmegaJobName() {
        return omegaJobName;
    }

    public int getMinBuildNumber() {
        return minBuildNumber;
    }

    public int getPrevBuildNumbers() {
        return prevBuildNumbers;
    }

    public boolean isStable() {
        return stable;
    }

    public boolean isOnlyInstalled() {
        return onlyInstalled;
    }

    public String getInstallationDirectory() {
        return installationDirectory;
    }

    public String getInfoMessageKey() {
        return infoMessageKey;
    }

    public String getSettingsKey() {
        return settingsKey;
    }
}
