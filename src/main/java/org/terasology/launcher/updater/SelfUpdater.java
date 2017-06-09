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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The SelfUpdater class is responsible for copying the updated files to the right location.
 * <br>
 * The update method will prepare a new process to run that will copy the files and restart the launcher.
 */
public final class SelfUpdater {

    private static final Logger logger = LoggerFactory.getLogger(SelfUpdater.class);

    private static final String TERASOLOGY_LAUNCHER_JAR = "lib/TerasologyLauncher.jar";

    private SelfUpdater() {
    }

    private static String getJavaProgramFile() {
        return System.getProperty("java.home") + "/bin/java";
    }

    private static void deleteLauncherContent(Path directory) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            directoryStream.forEach(p -> {
                if (Files.isDirectory(p)) {
                    String directoryName = p.toString();
                    if (directoryName.equals("bin") || directoryName.equals("lib") || directoryName.equals("licenses")) {
                        logger.info("Delete directory content: {}", p);
                        SelfUpdater.deleteLauncherContent(p);
                    } else {
                        logger.info("Skip directory: {}", p);
                    }
                } else if (!p.getFileName().toString().contains(".log")) {
                    FileUtils.deleteFileSilently(p);
                }
            });
        } catch (IOException e) {
            logger.error("Failed to delete launcher content", e);
        }
    }

    /**
     * Starts the update process after downloading the needed files.
     * @param tempLauncherDirectory the temp. launcher folder
     * @param launcherInstallationDirectory  the installation folder
     * @throws IOException if something goes wrong
     */
    public static void runUpdate(Path tempLauncherDirectory, Path launcherInstallationDirectory) throws IOException {
        final List<String> arguments = new ArrayList<>();
        // Set 'java' executable as programme to run
        arguments.add(getJavaProgramFile());
        // Build and set the classpath
        arguments.add("-cp");
        arguments.add(TERASOLOGY_LAUNCHER_JAR);
        // Specify class with main method to run
        arguments.add(SelfUpdater.class.getCanonicalName());
        // Arguments for update locations
        arguments.add(launcherInstallationDirectory.toString());
        arguments.add(tempLauncherDirectory.toString());

        logger.info("Running launcher self update: {}", arguments);

        final ProcessBuilder pb = new ProcessBuilder();
        pb.command(arguments);
        pb.directory(tempLauncherDirectory.toFile());
        pb.start();
    }

    public static void main(String[] args) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted!", e);
        }

        logger.info("Running self updater.");

        if (args == null || args.length != 2) {
            logger.error("Two arguments needed!");
            System.exit(1);
        }

        final String launcherInstallationDirectoryArg = args[0];
        final String tempLauncherDirectoryArg = args[1];
        final Path launcherInstallationDirectory = Paths.get(launcherInstallationDirectoryArg);
        final Path tempLauncherDirectory = Paths.get(tempLauncherDirectoryArg);

        try {
            logger.info("Current launcher path: {}", launcherInstallationDirectory);
            logger.info("New files temporarily located in: {}", tempLauncherDirectory);

            // Check both directories
            DirectoryUtils.checkDirectory(launcherInstallationDirectory);
            DirectoryUtils.checkDirectory(tempLauncherDirectory);

            logger.info("Delete launcher installation directory: {}", launcherInstallationDirectory);
            SelfUpdater.deleteLauncherContent(launcherInstallationDirectory);

            logger.info("Copy new files: {}", tempLauncherDirectory);
            FileUtils.copyFolder(tempLauncherDirectory, launcherInstallationDirectory);
        } catch (IOException | RuntimeException e) {
            logger.error("Auto updating the launcher failed!", e);
            System.exit(1);
        }

        // Start new launcher
        final List<String> arguments = new ArrayList<>();
        arguments.add(getJavaProgramFile());
        arguments.add("-jar");
        arguments.add(TERASOLOGY_LAUNCHER_JAR);

        logger.info("Start new launcher: {}", arguments);

        try {
            final ProcessBuilder pb = new ProcessBuilder();
            pb.command(arguments);
            pb.directory(launcherInstallationDirectory.toFile());
            pb.start();
            System.exit(0);
        } catch (IOException | RuntimeException e) {
            logger.error("Failed to restart launcher process after update! {}", arguments, e);
            System.exit(1);
        }
    }
}
