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

package org.terasology.launcher.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Use this class to get the appropriate platform-specific updater for the launcher.
 */
public final class LauncherUpdaterFactory {

    private enum UpdateStrategy {
        SELF,
        WIN_INSTALLER
    }

    private static final Logger logger = LoggerFactory.getLogger(AbstractLauncherUpdater.class);

    private LauncherUpdaterFactory() {
    }

    /**
     * @return The right updater for the running instance of the launcher.
     * It is detected by reading the updateStrategy file in the launcher installation directory.
     * The returned launcher's {@link AbstractLauncherUpdater#update(File, File)} method will act differently depending
     * on how the launcher is installed; the default behaviour (used when no updateStrategy file is found, so when the
     * application has simply been downloaded in zip/tar format and extracted) will be to directly replace the jars; if
     * the application was installed via the NSIS-based installer for windows, the new installer will be downloaded and
     * started; if it was installed from a package manager, a message would ask the user to run the appropriate command
     * to update the application from the system package manager.
     */
    public static AbstractLauncherUpdater getUpdater(TerasologyLauncherVersionInfo currentVersionInfo) throws URISyntaxException, IOException {
        File launcherInstallationDirectory = getLauncherInstallationDirectory();
        UpdateStrategy updateStrategy;
        try {
            String fileContents = FileUtils.readSingleLine(new File(launcherInstallationDirectory, "updateStrategy"));
            updateStrategy = UpdateStrategy.valueOf(fileContents);
            logger.info("The updateStrategy for this launcher is: {}", updateStrategy.toString());
        } catch (IOException | IllegalArgumentException e) {
            logger.info("Failed to read updateStrategy file, assuming {} as default", UpdateStrategy.SELF.toString());
            updateStrategy = UpdateStrategy.SELF;
        }
        switch (updateStrategy) {
            case WIN_INSTALLER: return new WinInstallerLauncherUpdater(launcherInstallationDirectory, currentVersionInfo);
            case SELF:
            default:
                return new SelfLauncherUpdater(launcherInstallationDirectory, currentVersionInfo);
        }
    }

    private static File getLauncherInstallationDirectory() throws URISyntaxException, IOException {
        File launcherInstallationDirectory;
        final File launcherLocation = new File(LauncherUpdaterFactory.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        logger.info("Launcher location: {}", launcherLocation);
        launcherInstallationDirectory = launcherLocation.getParentFile().getParentFile();
        DirectoryUtils.checkDirectory(launcherInstallationDirectory);
        logger.info("Launcher installation directory: {}", launcherInstallationDirectory);
        return launcherInstallationDirectory;
    }

}
