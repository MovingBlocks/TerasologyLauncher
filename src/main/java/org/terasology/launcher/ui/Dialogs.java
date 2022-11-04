// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.I18N;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.LauncherDirectoryUtils;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public final class Dialogs {

    private static final Logger logger = LoggerFactory.getLogger(Dialogs.class);

    private Dialogs() {
    }

    private static <T> T runOnEventThread(Supplier<T> producer) {
        T result = null;
        if (Platform.isFxApplicationThread()) {
            result = producer.get();
        } else {
            final Task<T> task = new Task<T>() {
                @Override
                protected T call() throws Exception {
                    return producer.get();
                }
            };
            Platform.runLater(task);
            try {
                result = task.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("Uh oh, something went wrong running this on the event thread.", e);
            }
        }
        return result;
    }

    private static void showMessageDialog(Alert.AlertType type, String title, String message, Stage owner) {
        runOnEventThread(() -> {
            final Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.initOwner(owner);

            alert.showAndWait();
            return null;
        });
    }

    public static void showWarning(Stage owner, String message) {
        showMessageDialog(Alert.AlertType.WARNING, I18N.getLabel("message_error_title"), message, owner);
    }

    public static void showError(Stage owner, String message) {
        showMessageDialog(Alert.AlertType.ERROR, I18N.getLabel("message_error_title"), message, owner);
    }

    public static void showInfo(Stage owner, String message) {
        showMessageDialog(Alert.AlertType.INFORMATION, I18N.getLabel("message_information_title"), message, owner);
    }

    public static Path chooseDirectory(Stage owner, final Path defaultDirectory, final String title) {
        try {
            FileUtils.ensureWritableDir(defaultDirectory);
        } catch (IOException e) {
            logger.error("Could not use {} as default directory!", defaultDirectory, e);
            return null;
        }

        Path selected = runOnEventThread(() -> {
            final DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(defaultDirectory.toFile());
            directoryChooser.setTitle(title);
            return Optional.ofNullable(directoryChooser.showDialog(owner)).map(File::toPath).orElse(null);
        });

        // directory proposal needs to be deleted if the user chose a different one
        try {
            if (deleteProposedDirectoryIfUnused(defaultDirectory, selected)) {
                logger.warn("Could not delete unused default directory! {}", defaultDirectory);
            }
        } catch (IOException e) {
            logger.error("Failed to delete unused default directory! {}", defaultDirectory, e);
        }
        return selected;
    }

    private static boolean deleteProposedDirectoryIfUnused(Path proposed, Path selected) throws IOException {
        return selected != null
                && !Files.isSameFile(proposed, selected)
                && !LauncherDirectoryUtils.containsFiles(proposed)
                && !Files.deleteIfExists(proposed);
    }

    public static void openFileBrowser(Stage owner, final Path directory, final String errorMsg) {
        try {
            FileUtils.ensureWritableDir(directory);
            EventQueue.invokeLater(() -> {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(directory.toFile());
                    } catch (IOException e) {
                        logger.error("The directory could not be opened! {}", directory, e);
                        Dialogs.showError(owner, errorMsg + "\n" + directory);
                    }
                }
            });
        } catch (IOException e) {
            logger.error("The directory could not be opened! {}", directory, e);
            Dialogs.showError(owner, errorMsg + "\n" + directory);
        }
    }

}
