/*
 * Copyright 2016 MovingBlocks
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

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Contains general information about a single retrieved build for TERASOLOGY.
 */
public final class TerasologyGameVersion implements Serializable {

    public static final int BUILD_VERSION_LATEST = -1;

    private static final long serialVersionUID = 4L;

    /** Build number for the engine job in Jenkins (bare engine + Core). */
    private Integer buildNumber;

    /** Build number for the Omega distribution job in Jenkins (includes extra modules). */
    private Integer omegaNumber;

    /** Which job line in Jenkins this build is part of. */
    private GameJob job;

    /** Detailed version information for the engine. */
    private TerasologyGameVersionInfo gameVersionInfo;

    /** What path the game has been installed to locally. */
    private transient File installationPath;

    /** Direct reference to the TERASOLOGY game jar. */
    private transient File gameJar;

    /** Changes for this version. */
    private List<String> changeLog;

    /** Success status from Jenkins. */
    private Boolean successful;

    /** Whether or not this instance is the very latest in the job line or not. */
    private boolean latest;

    public TerasologyGameVersion() {
    }

    public void copyTo(TerasologyGameVersion gameVersion) {
        gameVersion.setBuildNumber(buildNumber);
        gameVersion.setOmegaNumber(omegaNumber);
        gameVersion.setJob(job);
        gameVersion.setGameVersionInfo(gameVersionInfo);
        gameVersion.setInstallationPath(installationPath);
        gameVersion.setGameJar(gameJar);
        gameVersion.setChangeLog(changeLog);
        gameVersion.setSuccessful(successful);
    }

    public boolean isInstalled() {
        return (installationPath != null) && (gameJar != null);
    }

    public Integer getBuildVersion() {
        if (latest) {
            return BUILD_VERSION_LATEST;
        }
        return buildNumber;
    }

    public Integer getBuildNumber() {
        return buildNumber;
    }

    void setBuildNumber(Integer buildNumber) {
        this.buildNumber = buildNumber;
    }

    public Integer getOmegaNumber() {
        return omegaNumber;
    }

    void setOmegaNumber(Integer omegaNumber) {
        this.omegaNumber = omegaNumber;
    }

    public GameJob getJob() {
        return job;
    }

    void setJob(GameJob job) {
        this.job = job;
    }

    public TerasologyGameVersionInfo getGameVersionInfo() {
        return gameVersionInfo;
    }

    void setGameVersionInfo(TerasologyGameVersionInfo gameVersionInfo) {
        this.gameVersionInfo = gameVersionInfo;
    }

    public File getInstallationPath() {
        return installationPath;
    }

    void setInstallationPath(File installationPath) {
        this.installationPath = installationPath;
    }

    public File getGameJar() {
        return gameJar;
    }

    void setGameJar(File gameJar) {
        this.gameJar = gameJar;
    }

    public List<String> getChangeLog() {
        return changeLog;
    }

    void setChangeLog(List<String> changeLog) {
        this.changeLog = changeLog;
    }

    public boolean isLatest() {
        return latest;
    }

    void setLatest(boolean latest) {
        this.latest = latest;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + job + ", engine=" + buildNumber + ", omega=" + omegaNumber + ", latest="
                + latest + ", successful=" + successful + ", installed=" + isInstalled() + "]";
    }
}
