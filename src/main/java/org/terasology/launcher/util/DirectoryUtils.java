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
import org.terasology.launcher.util.windows.SavedGamesPathFinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

public final class DirectoryUtils {

    public static final String LAUNCHER_APPLICATION_DIR_NAME = "TerasologyLauncher";
    public static final String GAME_APPLICATION_DIR_NAME = "Terasology";
    public static final String GAME_DATA_DIR_NAME = "Terasology";
    public static final String DOWNLOAD_DIR_NAME = "download";
    public static final String TEMP_DIR_NAME = "temp";
    public static final String CACHE_DIR_NAME = "cache";

    private static final String PROPERTY_USER_HOME = "user.home";
    private static final String ENV_APPDATA = "APPDATA";
    private static final String MAC_PATH = "Library/Application Support/";
    private static final Logger logger = LoggerFactory.getLogger(DirectoryUtils.class);

    private DirectoryUtils() {
    }

    /**
     * Checks if the given path exists, is a directory and can be read and written by the program.
     *
     * @param directory Path to check
     * @throws IOException Reading the path fails in some way
     */
    public static void checkDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        if (!Files.isDirectory(directory)) {
            throw new IOException("Directory is not a directory! " + directory);
        }

        if (!Files.isReadable(directory) || !Files.isWritable(directory)) {
            throw new IOException("Can not read from or write into directory! " + directory);
        }
    }

    /**
     * Checks whether the given directory contains any game data, e.g., save games or screenshots.
     *
     * @param gameInstallationPath the game installation folder
     * @return true if game data is stored in the installation directory.
     */
    public static boolean containsGameData(final Path gameInstallationPath) {
        boolean containsGameData = false;
        if (gameInstallationPath != null && Files.exists(gameInstallationPath) &&
                Files.isDirectory(gameInstallationPath) && Files.isReadable(gameInstallationPath)) {
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
     * @param os              the operating system
     * @param applicationName the name of the application
     * @return the app. folder
     */
    public static Path getApplicationDirectory(OperatingSystem os, String applicationName) {
        final Path userHome = Paths.get(System.getProperty(PROPERTY_USER_HOME, "."));
        final Path applicationDirectory;

        if (os.isWindows()) {
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
        } else if (os.isUnix()) {
            applicationDirectory = userHome.resolve('.' + applicationName.toLowerCase(Locale.ENGLISH));
        } else if (os.isMac()) {
            applicationDirectory = userHome.resolve(MAC_PATH + applicationName);
        } else {
            applicationDirectory = userHome.resolve(applicationName);
        }

        return applicationDirectory;
    }

    /**
     * Should only be executed once at the start.
     *
     * @param os the operating system
     * @return the gama data directory
     */
    public static Path getGameDataDirectory(OperatingSystem os) {
        final Path userHome = Paths.get(System.getProperty(PROPERTY_USER_HOME, "."));
        final Path gameDataDirectory;

        if (os.isWindows()) {
            Path path = null;

            final String savedGamesPath = SavedGamesPathFinder.findSavedGamesPath();
            if (savedGamesPath != null) {
                path = Paths.get(savedGamesPath);
            }

            if (path == null) {
                final String documentsPath = SavedGamesPathFinder.findDocumentsPath();
                if (documentsPath != null) {
                    path = Paths.get(documentsPath);
                }
            }
            if (path == null) {
                path = new DirectoryChooser().getInitialDirectory().toPath();
            }

            gameDataDirectory = path.resolve(GAME_DATA_DIR_NAME);
        } else if (os.isUnix()) {
            gameDataDirectory = userHome.resolve('.' + GAME_DATA_DIR_NAME.toLowerCase(Locale.ENGLISH));
        } else if (os.isMac()) {
            gameDataDirectory = userHome.resolve(MAC_PATH + GAME_DATA_DIR_NAME);
        } else {
            gameDataDirectory = userHome.resolve(GAME_DATA_DIR_NAME);
        }

        return gameDataDirectory;
    }
}
