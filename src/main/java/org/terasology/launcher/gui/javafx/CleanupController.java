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
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.settings.AbstractLauncherSettings;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.CleanupUtils;

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

    void initialize(final AbstractLauncherSettings newLauncherSettings, final Stage newStage) {
        this.launcherSettings = newLauncherSettings;
        setLabelStrings();
    }

    private void setLabelStrings() {
        descriptionLabel.setText(BundleUtils.getLabel("cleanup_description"));
        deleteGameBox.setText(BundleUtils.getLabel("cleanup_delete_game"));
        deleteGameDataBox.setText(BundleUtils.getLabel("cleanup_delete_gameData"));
        deleteLauncherBox.setText(BundleUtils.getLabel("cleanup_delete_launcher"));
        okButton.setText(BundleUtils.getLabel("cleanup_ok"));
        cancelButton.setText(BundleUtils.getLabel("cleanup_cancel"));
    }

    @FXML
    public void doCleanupAction(ActionEvent event) throws IOException {  //TODO: catch and show a message instead of throwing
        if (deleteGameBox.isSelected()) {
            CleanupUtils.deleteGameDirectory(launcherSettings);
        }
        if (deleteGameDataBox.isSelected()) {
            CleanupUtils.deleteGameDatairectory(launcherSettings);
        }
        if (deleteLauncherBox.isSelected()) {
            CleanupUtils.deleteLauncherDirectory(launcherSettings);
        }
        Platform.exit();
        System.exit(0);
    }

    @FXML
    public void cancelCleanupAction(ActionEvent event) {
        //TODO: is this the right way to close?
        ((Node) event.getSource()).getScene().getWindow().hide();
    }
}
