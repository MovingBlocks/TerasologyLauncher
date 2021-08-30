// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.settings;

import com.google.common.base.MoreObjects;
import org.slf4j.event.Level;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.util.JavaHeapSize;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class SettingsObject {

    Locale locale;

    JavaHeapSize maxHeapSize;

    JavaHeapSize minHeapSize;

    Level logLevel;

    Path gameDirectory;
    Path gameDataDirectory;

    boolean keepDownloadedFiles;
    boolean showPreReleases;
    boolean closeLauncherAfterGameStart;

    GameIdentifier lastPlayedGameVersion;

    List<String> baseJavaParameters;
    List<String> userJavaParameters;
    List<String> userGameParameters;

    public SettingsObject(Locale locale, JavaHeapSize maxHeapSize, JavaHeapSize minHeapSize, Level logLevel, Path gameDirectory, Path gameDataDirectory, boolean keepDownloadedFiles, boolean showPreRelease, boolean closeLauncherAfterGameStart, GameIdentifier lastPlayedGameVersion, List<String> baseJavaParameters, List<String> userJavaParameters, List<String> userGameParameters) {
        this.locale = locale;
        this.maxHeapSize = maxHeapSize;
        this.minHeapSize = minHeapSize;
        this.logLevel = logLevel;
        this.gameDirectory = gameDirectory;
        this.gameDataDirectory = gameDataDirectory;
        this.keepDownloadedFiles = keepDownloadedFiles;
        this.showPreReleases = showPreRelease;
        this.closeLauncherAfterGameStart = closeLauncherAfterGameStart;
        this.lastPlayedGameVersion = lastPlayedGameVersion;
        this.baseJavaParameters = baseJavaParameters;
        this.userJavaParameters = userJavaParameters;
        this.userGameParameters = userGameParameters;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("locale", locale)
                .add("maxHeapSize", maxHeapSize)
                .add("minHeapSize", minHeapSize)
                .add("logLevel", logLevel)
                .add("gameDirectory", gameDirectory.toString())
                .add("gameDataDirectory", gameDataDirectory.toString())
                .add("keepDownloadedFiles", keepDownloadedFiles)
                .add("showPreReleases", showPreReleases)
                .add("closeLauncherAfterGameStart", closeLauncherAfterGameStart)
                .add("lastPlayedGameVersion", lastPlayedGameVersion)
                .add("baseJavaParameters", baseJavaParameters)
                .add("userJavaParameters", userJavaParameters)
                .add("userGameParameters", userGameParameters)
                .toString();
    }
}
