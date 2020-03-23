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
import org.terasology.launcher.util.FileUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The SelfUpdater class is responsible for copying the updated files to the right location.
 * <br>
 * The update method will prepare a new process to run that will copy the files and restart the launcher.
 */
public final class SelfUpdater {

    private static final Logger logger = LoggerFactory.getLogger(SelfUpdater.class);

    private static final String PROJECT_NAME = "TerasologyLauncher";

    private SelfUpdater() {
    }

    private static Path getLauncherExecutable(final Path installationDirectory) {
        final String binDir =
                (System.getProperty("os.name").matches(".*(?i)mac.*")) ? "MacOS" : "bin";

        final String suffix = (System.getProperty("os.name").matches(".*(?i)windows.*")) ? ".bat" : "";

        return installationDirectory.resolve(binDir).resolve(PROJECT_NAME + suffix);
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

    public static void main(String[] args) {
        try {
            //TODO: properly wait for the old launcher application to shut down
            //      with Java9: https://docs.oracle.com/javase/9/docs/api/java/lang/ProcessHandle.html
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted!", e);
        }

        logger.info("Running self updater.");

        if (args == null || args.length != 2) {
            logger.error("Invalid number of arguments!");
            logger.error("Usage: {} <installationDir> <tmpDir>", SelfUpdater.class.getCanonicalName());
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
            FileUtils.ensureWritableDir(launcherInstallationDirectory);
            FileUtils.ensureWritableDir(tempLauncherDirectory);

            logger.info("Delete launcher installation directory: {}", launcherInstallationDirectory);
            SelfUpdater.deleteLauncherContent(launcherInstallationDirectory);

            logger.info("Copy new files: {}", tempLauncherDirectory);
            FileUtils.copyFolder(tempLauncherDirectory, launcherInstallationDirectory);
        } catch (IOException | RuntimeException e) {
            logger.error("Auto updating the launcher failed!", e);
            System.exit(1);
        }

        final Path cwd = launcherInstallationDirectory;
        final Path cmd = cwd.relativize(getLauncherExecutable(launcherInstallationDirectory));
        logger.info("Starting new launcher:\n\tcwd: {}\n\tcmd: {}", cwd.toString(), cmd.toString());
        try {
            new ProcessBuilder()
                    .command(cmd.toString())
                    .directory(cwd.toFile())
                    .start();
            logger.info("Closing self-update.");
            System.exit(0);
        } catch (IOException | RuntimeException e) {
            logger.error("Failed to restart launcher process after update!", e);
            System.exit(1);
        }
    }
}
