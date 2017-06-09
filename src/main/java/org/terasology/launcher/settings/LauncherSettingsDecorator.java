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

package org.terasology.launcher.settings;

import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.LogLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * The {@link LauncherSettingsDecorator} allows to decorate a {@link BaseLauncherSettings} object with temporary settings.
 * <p>
 * The main launcher settings which are stored in a Java properties file are separated from additional (temporary) settings.
 * This enables "temporary settings", e.g., starting the launcher temporarily in offline mode or with a specific locale.
 */
class LauncherSettingsDecorator extends AbstractLauncherSettings {

    private AbstractLauncherSettings settings;

    private Locale locale;
    private GameJob gameJob;
    private Map<GameJob, Integer> buildVersionMap;
    private Map<GameJob, Integer> lastBuildNumberMap;
    private JavaHeapSize maxHeapSize;
    private JavaHeapSize initialHeapSize;
    private String userJavaParameters;
    private String userGameParameters;
    private LogLevel logLevel;
    private Path gameDirectory;
    private Path gameDataDirectory;

    private Boolean searchForLauncherUpdates;
    private Boolean keepDownloadedFiles;
    private Boolean closeLauncherAfterGameStart;

    LauncherSettingsDecorator(AbstractLauncherSettings settings) {
        this.settings = settings;

        this.buildVersionMap = Collections.emptyMap();
        this.lastBuildNumberMap = Collections.emptyMap();
    }

    @Override
    public void load() throws IOException {
        settings.load();
    }

    @Override
    public void store() throws IOException {
        settings.store();
    }

    // --------------------------------------------------------------------- //
    // INIT
    // --------------------------------------------------------------------- //

    @Override
    protected void initInitialHeapSize() {

    }

    @Override
    protected void initSearchForLauncherUpdates() {

    }

    @Override
    protected void initCloseLauncherAfterGameStart() {

    }

    @Override
    protected void initSaveDownloadedFiles() {

    }

    @Override
    protected void initGameDirectory() {

    }

    @Override
    protected void initGameDataDirectory() {

    }

    @Override
    protected void initUserJavaParameters() {

    }

    @Override
    protected void initUserGameParameters() {

    }

    @Override
    protected void initLogLevel() {

    }

    @Override
    protected void initMaxHeapSize() {

    }

    @Override
    protected void initLastBuildNumber() {

    }

    @Override
    protected void initLocale() {

    }

    @Override
    protected void initJob() {

    }

    @Override
    protected void initBuildVersion() {

    }

    // --------------------------------------------------------------------- //
    // GETTERS
    // --------------------------------------------------------------------- //

    @Override
    public Locale getLocale() {
        return (locale != null) ? locale : settings.getLocale();
    }

    @Override
    public GameJob getJob() {
        return (gameJob != null) ? gameJob : settings.getJob();
    }

    @Override
    public Integer getBuildVersion(GameJob job) {
        return buildVersionMap.containsKey(job) ? buildVersionMap.get(job) : settings.getBuildVersion(job);
    }

    @Override
    public Integer getLastBuildNumber(GameJob job) {
        return lastBuildNumberMap.containsKey(job) ? lastBuildNumberMap.get(job) : settings.getLastBuildNumber(job);
    }

    @Override
    public JavaHeapSize getMaxHeapSize() {
        return (maxHeapSize != null) ? maxHeapSize : settings.getMaxHeapSize();
    }

    @Override
    public JavaHeapSize getInitialHeapSize() {
        return (initialHeapSize != null) ? initialHeapSize : settings.getInitialHeapSize();
    }

    @Override
    public String getUserJavaParameters() {
        // TODO: merge/prepend the additional arguments
        return (userJavaParameters != null) ? userJavaParameters : settings.getUserJavaParameters();
    }

    @Override
    public String getUserGameParameters() {
        // TODO: merge/prepend the additional arguments
        return (userGameParameters != null) ? userGameParameters : settings.getUserGameParameters();
    }

    @Override
    public LogLevel getLogLevel() {
        return (logLevel != null) ? logLevel : settings.getLogLevel();
    }

    @Override
    public Path getGameDirectory() {
        return (gameDirectory != null) ? gameDirectory : settings.getGameDirectory();
    }

    @Override
    public Path getGameDataDirectory() {
        return (gameDataDirectory != null) ? gameDataDirectory : settings.getGameDataDirectory();
    }

    @Override
    public boolean isSearchForLauncherUpdates() {
        return (searchForLauncherUpdates != null) ? searchForLauncherUpdates : settings.isSearchForLauncherUpdates();
    }

    @Override
    public boolean isKeepDownloadedFiles() {
        return (keepDownloadedFiles != null) ? keepDownloadedFiles : settings.isKeepDownloadedFiles();
    }

    @Override
    public boolean isCloseLauncherAfterGameStart()  {
        return (closeLauncherAfterGameStart != null) ? closeLauncherAfterGameStart : settings.isCloseLauncherAfterGameStart();
    }

    // --------------------------------------------------------------------- //
    // SETTERS
    // --------------------------------------------------------------------- //

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void setJob(GameJob job) {
        this.gameJob = job;
    }

    @Override
    public void setBuildVersion(int version, GameJob job) {
        this.buildVersionMap.put(job, version);
    }

    @Override
    public void setLastBuildNumber(Integer lastBuildNumber, GameJob job) {
        this.lastBuildNumberMap.put(job, lastBuildNumber);
    }

    @Override
    public void setMaxHeapSize(JavaHeapSize maxHeapSize) {
        this.maxHeapSize = maxHeapSize;
    }

    @Override
    public void setInitialHeapSize(JavaHeapSize initialHeapSize) {
        this.initialHeapSize = initialHeapSize;
    }

    @Override
    public void setUserJavaParameters(String userJavaParameters) {
        this.userJavaParameters = userJavaParameters;
    }

    @Override
    public void setUserGameParameters(String userGameParameters) {
        this.userGameParameters = userGameParameters;
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public void setGameDirectory(Path gameDirectory) {
        this.gameDirectory = gameDirectory;
    }

    @Override
    public void setGameDataDirectory(Path gameDataDirectory) {
        this.gameDataDirectory = gameDataDirectory;
    }

    @Override
    public void setSearchForLauncherUpdates(boolean searchForLauncherUpdates) {
        this.searchForLauncherUpdates = searchForLauncherUpdates;
    }

    @Override
    public void setCloseLauncherAfterGameStart(boolean closeLauncherAfterGameStart) {
        this.closeLauncherAfterGameStart = closeLauncherAfterGameStart;
    }

    @Override
    public void setKeepDownloadedFiles(boolean keepDownloadedFiles) {
        this.keepDownloadedFiles = keepDownloadedFiles;
    }
}
