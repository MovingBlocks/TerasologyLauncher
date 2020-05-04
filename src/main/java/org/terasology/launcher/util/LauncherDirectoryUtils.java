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

package org.terasology.launcher.util;

import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

public final class LauncherDirectoryUtils {

    public static final String LAUNCHER_APPLICATION_DIR_NAME = "TerasologyLauncher";
    public static final String GAME_APPLICATION_DIR_NAME = "Terasology";
    public static final String GAME_DATA_DIR_NAME = "Terasology";
    public static final String CACHE_DIR_NAME = "cache";

    private static final String PROPERTY_USER_HOME = "user.home";
    private static final String ENV_APPDATA = "APPDATA";
    private static final String MAC_PATH = "Library/Application Support/";
    private static final Logger logger = LoggerFactory.getLogger(LauncherDirectoryUtils.class);

    private LauncherDirectoryUtils() {
    }

    /**
     * Checks whether the given directory contains any game data, e.g., save games or screenshots.
     *
     * @param gameInstallationPath the game installation folder
     * @return true if game data is stored in the installation directory.
     */
    public static boolean containsGameData(final Path gameInstallationPath) {
        boolean containsGameData = false;
        if (FileUtils.isReadableDir(gameInstallationPath)) {
            try (Stream<Path> stream = Files.list(gameInstallationPath)) {
                containsGameData = stream.anyMatch(child -> Files.isDirectory(child)
                        && isGameDataDirectoryName(child.getFileName().toString()) && containsFiles(child));
            } catch (IOException e) {
                logger.error("Failed to check if folder contains game data", e);
            }
        }
        return containsGameData;
    }

    /**
     * Checks if the directory name is a Terasology game data directory.
     *
     * @param directoryName name to check
     * @return true if the directory name is a Terasology game data directory
     */
    private static boolean isGameDataDirectoryName(String directoryName) {
        return Arrays.stream(GameDataDirectoryNames.values())
                .anyMatch(gameDataDirectoryNames -> gameDataDirectoryNames.getName().equals(directoryName));
    }

    /**
     * Checks if a directory and all subdirectories are containing files.
     *
     * @param directory directory to check
     * @return true if the directory contains one or more files
     */
    public static boolean containsFiles(Path directory) {
        if (directory == null || !Files.exists(directory) || !Files.isDirectory(directory)) {
            return false;
        }

        try (Stream<Path> stream = Files.list(directory)) {
            return stream.anyMatch(file -> Files.isRegularFile(file) || Files.isDirectory(file) && containsFiles(file));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Should only be executed once at the start.
     *
     * @param platform              the operating system
     * @param applicationName the name of the application
     * @return the app. folder
     */
    public static Path getApplicationDirectory(Platform platform, String applicationName) {
        final Path userHome = Paths.get(System.getProperty(PROPERTY_USER_HOME, "."));
        final Path applicationDirectory;

        if (platform.isWindows()) {
            final String envAppData = System.getenv(ENV_APPDATA);
            if ((envAppData != null) && (envAppData.length() > 0)) {
                applicationDirectory = Paths.get(envAppData).resolve(applicationName);
            } else {
                applicationDirectory = userHome.resolve(applicationName);
                // Alternatives :
                //   System.getenv("HOME")
                //   System.getenv("USERPROFILE")
                //   System.getenv("HOMEDRIVE") + System.getenv("HOMEPATH")
            }
        } else if (platform.isLinux()) {
            applicationDirectory = userHome.resolve('.' + applicationName.toLowerCase(Locale.ENGLISH));
        } else if (platform.isMac()) {
            applicationDirectory = userHome.resolve(MAC_PATH + applicationName);
        } else {
            applicationDirectory = userHome.resolve(applicationName);
        }

        return applicationDirectory;
    }

    /**
     * Should only be executed once at the start.
     *
     * @param platform the operating system
     * @return the gama data directory
     */
    public static Path getGameDataDirectory(Platform platform) {
        final Path userHome = Paths.get(System.getProperty(PROPERTY_USER_HOME, "."));
        final Path gameDataDirectory;

        if (platform.isWindows()) {
            Path path = Paths.get(System.getenv("APPDATA"));
            gameDataDirectory = path.resolve(GAME_DATA_DIR_NAME);
        } else if (platform.isLinux()) {
            gameDataDirectory = userHome.resolve('.' + GAME_DATA_DIR_NAME.toLowerCase(Locale.ENGLISH));
        } else if (platform.isMac()) {
            gameDataDirectory = userHome.resolve(MAC_PATH + GAME_DATA_DIR_NAME);
        } else {
            gameDataDirectory = userHome.resolve(GAME_DATA_DIR_NAME);
        }

        return gameDataDirectory;
    }

    public static Path getInstallationDirectory() {
        final URL location = LauncherDirectoryUtils.class.getProtectionDomain().getCodeSource().getLocation();
        Path installationDirectory = null;
        try {
            final Path launcherLocation = Paths.get(location.toURI());
            logger.trace("Launcher location: {}", launcherLocation);
            installationDirectory = launcherLocation.getParent().getParent();
            logger.trace("Launcher installation directory: {}", installationDirectory);
        } catch (URISyntaxException e) {
            logger.error("Could not determine launcher installation directory.", e);
        }
        return installationDirectory;
    }
}
