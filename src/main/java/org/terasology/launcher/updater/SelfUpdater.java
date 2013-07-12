/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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

    private SelfUpdater() {
    }

    /**
     * Starts the update process after downloading the needed files.
     */
    public static void runUpdate(final File tempDirectory, final File launcherInstallationDirectory) {
        final String separator = File.separator;
        final String javaBin = System.getProperty("java.home") + separator + "bin" + separator + "java";
        final File tempLauncherDirectory = new File(tempDirectory, "TerasologyLauncher");
        final File classpath = new File(tempLauncherDirectory, "lib");

        final List<String> arguments = new ArrayList<>();
        // Set 'java' executable as programme to run
        arguments.add(javaBin);
        // Build and set the classpath
        arguments.add("-cp");
        arguments.add("TerasologyLauncher.jar");
        // Specify class with main method to run
        arguments.add(SelfUpdater.class.getCanonicalName());
        // Arguments for update locations
        arguments.add(launcherInstallationDirectory.getPath());
        arguments.add(tempLauncherDirectory.getPath());

        logger.info("Running launcher self update with: {}", arguments);
        logger.info("Current launcher path: {}", launcherInstallationDirectory.getPath());
        logger.info("New files temporarily located in: {}", tempLauncherDirectory.getPath());

        final ProcessBuilder pb = new ProcessBuilder();
        pb.command(arguments);
        pb.directory(classpath);

        try {
            pb.start();
        } catch (IOException e) {
            logger.error("Failed to run self update process!", e);
        }
        System.exit(0);
    }

    public static void main(final String[] args) {
        logger.info("Running self updater.");

        final String launcherInstallationDirectoryArg = args[0];
        final String tempLauncherDirectoryArg = args[1];
        final File launcherInstallationDirectory = new File(launcherInstallationDirectoryArg);
        final File tempLauncherDirectory = new File(tempLauncherDirectoryArg);

        try {
            // Check both directories
            DirectoryUtils.checkDirectory(launcherInstallationDirectory);
            DirectoryUtils.checkDirectory(tempLauncherDirectory);

            logger.info("Delete launcher installation directory: {}", launcherInstallationDirectory);
            FileUtils.delete(launcherInstallationDirectory);

            logger.info("Copy new files: {}", tempLauncherDirectory);
            FileUtils.copyFolder(tempLauncherDirectory, launcherInstallationDirectory);
        } catch (IOException e) {
            logger.error("Auto updating the launcher failed!", e);
            System.exit(1);
        }

        // Start new launcher
        final String separator = System.getProperty("file.separator");
        final String javaPath = System.getProperty("java.home") + separator + "bin" + separator + "java";

        final File classpath = new File(launcherInstallationDirectory, "lib");

        final List<String> arguments = new ArrayList<>();
        arguments.add(javaPath);
        arguments.add("-jar");
        arguments.add("TerasologyLauncher.jar");

        final ProcessBuilder pb = new ProcessBuilder();
        pb.command(arguments);
        pb.directory(classpath);

        logger.info("Start new launcher: {}", arguments);
        try {
            pb.start();
        } catch (IOException e) {
            logger.error("Failed to restart launcher process after update.", e);
            System.exit(1);
        }
        System.exit(0);
    }
}
