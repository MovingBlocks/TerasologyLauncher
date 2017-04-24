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

package org.terasology.launcher.gui.javafx;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.settings.AbstractLauncherSettings;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.CleanupUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.GuiUtils;

import java.io.File;
import java.io.IOException;

public class CleanupController {

    private static final Logger logger = LoggerFactory.getLogger(CleanupController.class);

    private AbstractLauncherSettings launcherSettings;

    @FXML
    private Label descriptionLabel;
    @FXML
    private CheckBox deleteGameBox;
    @FXML
    private CheckBox deleteGameDataBox;
    @FXML
    private CheckBox deleteLauncherBox;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;

    public void initialize(final AbstractLauncherSettings newLauncherSettings) {
        this.launcherSettings = newLauncherSettings;
        setLabelStrings();
    }

    private String formatWithPathAndSize(String prefix, File file) {
        return String.format("%s (%s, %s)", prefix, file.getAbsolutePath(), FileUtils.formatByteCount(FileUtils.computeTotalSize(file)));
    }

    private void setLabelStrings() {
        descriptionLabel.setText(BundleUtils.getLabel("cleanup_description"));
        deleteGameBox.setText(formatWithPathAndSize(BundleUtils.getLabel("cleanup_delete_game"),
                CleanupUtils.getDirectory(launcherSettings, CleanupUtils.Directory.GAME)));
        deleteGameDataBox.setText(formatWithPathAndSize(BundleUtils.getLabel("cleanup_delete_gameData"),
                CleanupUtils.getDirectory(launcherSettings, CleanupUtils.Directory.GAME_DATA)));
        deleteLauncherBox.setText(formatWithPathAndSize(BundleUtils.getLabel("cleanup_delete_launcher"),
                CleanupUtils.getDirectory(launcherSettings, CleanupUtils.Directory.LAUNCHER)));
        okButton.setText(BundleUtils.getLabel("cleanup_ok"));
        cancelButton.setText(BundleUtils.getLabel("cleanup_cancel"));
    }

    private boolean deleteDirectory(CleanupUtils.Directory directory) {
        boolean retry;
        File f = CleanupUtils.getDirectory(launcherSettings, directory);
        do {
            try {
                if (f.exists()) {
                    logger.info("Deleting {} ({})", directory.toString(), f.getAbsolutePath());
                    FileUtils.delete(f);
                } else {
                    logger.info("Skipping {} ({}) because it doesn't exist", directory.toString(), f.getAbsolutePath());
                }
                return true;
            } catch (IOException e) {
                logger.warn("Failed to delete {}", f.getAbsolutePath());
                retry = GuiUtils.showBinaryChoiceDialog(BundleUtils.getMessage("cleanup_error"),
                        BundleUtils.getMessage("cleanup_delete_failed", f.getAbsolutePath(), e.getMessage()),
                        BundleUtils.getLabel("cleanup_retry"), BundleUtils.getLabel("cleanup_ignore"));
            }
        } while (retry);
        return false;
    }

    @FXML
    public void doCleanupAction() {
        boolean success = true;
        if (deleteGameBox.isSelected()) {
            success = deleteDirectory(CleanupUtils.Directory.GAME);
        }
        if (deleteGameDataBox.isSelected()) {
            success &= deleteDirectory(CleanupUtils.Directory.GAME_DATA);
        }
        if (deleteLauncherBox.isSelected()) {
            success &= deleteDirectory(CleanupUtils.Directory.LAUNCHER);
        }
        Platform.exit();
        System.exit(success ? 0 : 1);
    }

    @FXML
    public void cancelCleanupAction(ActionEvent event) {
        ((Node) event.getSource()).getScene().getWindow().hide();
    }
}
