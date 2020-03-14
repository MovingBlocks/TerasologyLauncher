/*
 * Copyright 2019 MovingBlocks
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

import javafx.animation.Transition;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.HostServicesWrapper;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.util.Optional;

public class FooterController {

    private static final Logger logger = LoggerFactory.getLogger(FooterController.class);

    @FXML
    private Button warningButton;
    @FXML
    private Label versionInfo;
    private HostServicesWrapper hostServices;
    private Property<Optional<Warning>> warningProperty;

    public FooterController() {
        warningProperty = new SimpleObjectProperty<>(Optional.empty());
    }

    private void updateLabels() {
        final String launcherVersion = TerasologyLauncherVersionInfo.getInstance().getDisplayVersion();
        if (launcherVersion.isEmpty()) {
            versionInfo.setText(BundleUtils.getLabel("launcher_versionInfo"));
        } else {
            versionInfo.setText(launcherVersion);
        }
    }

    public void setHostServices(HostServicesWrapper hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    public void initialize() {
        updateLabels();
        warningProperty.addListener((value, oldValue, newValue) -> updateWarningButton(newValue));
    }

    @FXML
    protected void handleSocialButtonMouseEntered(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = FXUtils.createScaleTransition(1.2, source);
        t.playFromStart();
    }

    @FXML
    protected void handleSocialButtonMouseExited(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = FXUtils.createScaleTransition(1, source);
        t.playFromStart();
    }

    @FXML
    protected void handleSocialButtonMousePressed(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = FXUtils.createScaleTransition(0.8, source);
        t.playFromStart();
    }

    @FXML
    protected void handleSocialButtonMouseReleased(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = FXUtils.createScaleTransition(1.2, source);
        t.playFromStart();
    }

    @FXML
    protected void openFacebook() {
        hostServices.tryOpenUri(BundleUtils.getURI("terasology_facebook"));
    }

    @FXML
    protected void openGithub() {
        hostServices.tryOpenUri(BundleUtils.getURI("terasology_github"));
    }

    @FXML
    protected void openDiscord() {
        hostServices.tryOpenUri(BundleUtils.getURI("terasology_discord"));
    }

    @FXML
    protected void openReddit() {
        hostServices.tryOpenUri(BundleUtils.getURI("terasology_reddit"));
    }

    @FXML
    protected void openTwitter() {
        hostServices.tryOpenUri(BundleUtils.getURI("terasology_twitter"));
    }

    @FXML
    protected void openYoutube() {
        hostServices.tryOpenUri(BundleUtils.getURI("terasology_youtube"));
    }

    @FXML
    protected void openLogs() {
        //TODO: how to control the main launcher view from here?
        //contentTabPane.getSelectionModel().select(2);
    }

    private void updateWarningButton(Optional<Warning> warning) {
        warningButton.setVisible(warning.isPresent());
        warning.ifPresent(w -> {
            String msg = BundleUtils.getLabel(w.getMessageKey());
            warningButton.setTooltip(new Tooltip(msg));
            logger.warn(msg);
        });
    }

    void bind(ReadOnlyProperty<Optional<Warning>> property) {
        this.warningProperty.bind(property);
    }
}
