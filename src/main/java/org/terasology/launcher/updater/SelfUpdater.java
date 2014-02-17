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

package org.terasology.launcher.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The SelfUpdater class is responsible for copying the updated files to the right location.
 * <p/>
 * The update method will prepare a new process to run that will copy the files and restart the launcher.
 */
public final class SelfUpdater {

    private static final Logger logger = LoggerFactory.getLogger(SelfUpdater.class);

    private static final String TERASOLOGY_LAUNCHER_JAR = "TerasologyLauncher.jar";

    private SelfUpdater() {
    }

    private static String getJavaProgramFile() {
        return System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    }

    private static void deleteLauncherContent(final File directory) {
        final File[] files = directory.listFiles();
        if ((files != null) && (files.length > 0)) {
            for (File child : files) {
                if (child.isDirectory()) {
                    SelfUpdater.deleteLauncherContent(child);
                } else if (!child.getName().contains(".log")) {
                    boolean deleted = child.delete();
                    if (!deleted) {
                        logger.error("Could not delete file! {}", child);
                    }
                }
            }
        }
    }

    /**
     * Starts the update process after downloading the needed files.
     */
    public static void runUpdate(final File tempLauncherDirectory, final File launcherInstallationDirectory) throws IOException {
        final List<String> arguments = new ArrayList<>();
        // Set 'java' executable as programme to run
        arguments.add(getJavaProgramFile());
        // Build and set the classpath
        arguments.add("-cp");
        arguments.add(TERASOLOGY_LAUNCHER_JAR);
        // Specify class with main method to run
        arguments.add(SelfUpdater.class.getCanonicalName());
        // Arguments for update locations
        arguments.add(launcherInstallationDirectory.getPath());
        arguments.add(tempLauncherDirectory.getPath());

        logger.info("Running launcher self update: {}", arguments);

        final ProcessBuilder pb = new ProcessBuilder();
        pb.command(arguments);
        pb.directory(new File(tempLauncherDirectory, "lib"));
        pb.start();
    }

    public static void main(final String[] args) {
        logger.info("Running self updater.");

        if ((args == null) || (args.length != 2)) {
            logger.error("Two arguments needed!");
            System.exit(1);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted!", e);
        }

        final String launcherInstallationDirectoryArg = args[0];
        final String tempLauncherDirectoryArg = args[1];
        final File launcherInstallationDirectory = new File(launcherInstallationDirectoryArg);
        final File tempLauncherDirectory = new File(tempLauncherDirectoryArg);

        try {
            logger.info("Current launcher path: {}", launcherInstallationDirectory.getPath());
            logger.info("New files temporarily located in: {}", tempLauncherDirectory.getPath());

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
            pb.directory(new File(launcherInstallationDirectory, "lib"));
            pb.start();
        } catch (IOException | RuntimeException e) {
            logger.error("Failed to restart launcher process after update! {}", arguments, e);
            System.exit(1);
        }
        System.exit(0);
    }
}
