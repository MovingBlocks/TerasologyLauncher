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

package org.terasologylauncher.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasologylauncher.util.FileUtils;
import org.terasologylauncher.util.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The SelfUpdater class is responsible for copying the updated files to the right location.
 * <p/>
 * The update method will prepare a new process to run that will copy the files and restart the launcher.
 *
 * @author Skaldarnar
 */
public final class SelfUpdater {

    private static final Logger logger = LoggerFactory.getLogger(SelfUpdater.class);

    private SelfUpdater() {
    }

    /**
     * Starts the update process after downloading the needed files.
     *
     * @param temporaryUpdateDir - where the downloaded files are located
     * @param launcherFile       - where the launcher is located
     */
    public static void runUpdate(final File temporaryUpdateDir, final File launcherFile) {
        List<String> arguments = new ArrayList<String>();

        final String separator = File.separator;
        final String javaBin = System.getProperty("java.home") + separator + "bin" + separator + "java";

        // Set 'java' executable as programme to run
        arguments.add(javaBin);
        // Build and set the classpath
        arguments.add("-cp");
        final File classpath = new File(new File(temporaryUpdateDir, "TerasologyLauncher"), "lib");

        final OperatingSystem os = OperatingSystem.getOS();
        if (os.isWindows()) {
            arguments.add("\"" + temporaryUpdateDir + separator + "TerasologyLauncher" + separator + "lib" + separator +
                "*" + "\"");
        } else {
            final StringBuilder classpathBuilder = new StringBuilder();
            for (File f : classpath.listFiles()) {
                classpathBuilder.append(f.toURI()).append(File.pathSeparator);
            }
            classpathBuilder.deleteCharAt(classpathBuilder.length() - 1);
            arguments.add(classpathBuilder.toString());
        }

        // Specify class with main method to run
        arguments.add(SelfUpdater.class.getCanonicalName());
        // Arguments for update locations
        arguments.add(launcherFile.getParentFile().getParent());
        arguments.add(temporaryUpdateDir + separator + "TerasologyLauncher");

        logger.info("Running launcher update with: \t  {}", arguments);
        logger.info("Current launcher path: \t \t {}", launcherFile);
        logger.info("New files located in: \t \t {}", temporaryUpdateDir + separator + "TerasologyLauncher");

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(arguments);
        try {
            pb.start();
        } catch (IOException e) {
            logger.error("Failed to run self update process!", e);
        }
        System.exit(0);
    }

    public static void main(final String[] args) {
        logger.info("Running self updater.");

        String launcherLocation = args[0];
        String temporaryUpdatePath = args[1];

        File launcher = new File(launcherLocation);
        File updateFiles = new File(temporaryUpdatePath);

        // Copy the new files
        logger.info("Copying updated files.");
        try {
            FileUtils.delete(launcher);
            FileUtils.copyFolder(updateFiles, launcher);
        } catch (IOException e) {
            logger.error("Auto updating the launcher failed!", e);
        }

        List<String> arguments = new ArrayList<String>();

        String separator = System.getProperty("file.separator");
        String javaPath = System.getProperty("java.home") + separator + "bin" + separator + "java";

        arguments.add(javaPath);
        arguments.add("-jar");
        arguments.add(launcherLocation + separator + "lib" + separator + "TerasologyLauncher.jar");

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(arguments);

        try {
            pb.start();
        } catch (IOException e) {
            logger.error("Failed to restart launcher process after update.", e);
        }

    }
}
