/*
 * Copyright 2013 MovingBlocks
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

public enum GameJob {

    TerasologyStable("master", 15, 4, true, false, "STABLE", "infoHeader1_TerasologyStable", "settings_game_buildType_TerasologyStable"),

    TerasologyLegacy("legacy", 1, 0, true, false, "STABLE", "infoHeader1_TerasologyLegacy", "settings_game_buildType_TerasologyLegacy"),

    Terasology("develop", 245, 4, false, false, "NIGHTLY", "infoHeader1_Terasology", "settings_game_buildType_Terasology"),

    TerasologyMulti("multiplayer", 1, 0, false, true, "NIGHTLY", "infoHeader1_TerasologyMulti", "settings_game_buildType_TerasologyMulti");

    private final String gitBranch;
    private final int minBuildNumber;
    private final int prevBuildNumbers;
    private final boolean stable;
    private final boolean onlyInstalled;
    private final String installationDirectory;
    private final String infoMessageKey;
    private final String settingsKey;

    private GameJob(String gitBranch, int minBuildNumber, int prevBuildNumbers, boolean stable, boolean onlyInstalled, String installationDirectory, String infoMessageKey,
                    String settingsKey) {
        this.gitBranch = gitBranch;
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
