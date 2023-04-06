// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

import javafx.animation.Transition;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.model.LauncherVersion;
import org.terasology.launcher.util.HostServices;
import org.terasology.launcher.util.I18N;

import java.util.Optional;

public class FooterController {

    private static final Logger logger = LoggerFactory.getLogger(FooterController.class);

    @FXML
    private Button warningButton;
    @FXML
    private Label versionInfo;

    private HostServices hostServices;
    private final Property<Optional<Warning>> warningProperty;

    public FooterController() {
        warningProperty = new SimpleObjectProperty<>(Optional.empty());
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    public void initialize() {
        initLabels();
        initWarningButton();
    }

    private void initLabels() {
        Binding<String> experimental = I18N.labelBinding("launcher_versionInfo");
        StringBinding versionString = Bindings.createStringBinding(() -> {
            final String launcherVersion = LauncherVersion.getInstance().getDisplayName();
            if (launcherVersion.isEmpty()) {
                return experimental.getValue();
            } else {
                return launcherVersion;
            }
        }, experimental);

        versionInfo.textProperty().bind(versionString);
    }

    private void initWarningButton() {
        warningButton.visibleProperty().bind(
                Bindings.createBooleanBinding(() -> warningProperty.getValue().isPresent(), warningProperty));

        Binding<String> warningText = Bindings.createObjectBinding(
                () -> warningProperty.getValue().map(Warning::getMessageKey).map(I18N::getLabel).orElse(null),
                warningProperty);

        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(warningText);
        tooltip.setShowDelay(Duration.millis(50));
        warningButton.setTooltip(tooltip);

        warningText.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                logger.warn(newValue);
            }
        });
    }

    @FXML
    protected void handleSocialButtonMouseEntered(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = Effects.createScaleTransition(1.2, source);
        t.playFromStart();
    }

    @FXML
    protected void handleSocialButtonMouseExited(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = Effects.createScaleTransition(1, source);
        t.playFromStart();
    }

    @FXML
    protected void handleSocialButtonMousePressed(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = Effects.createScaleTransition(0.8, source);
        t.playFromStart();
    }

    @FXML
    protected void handleSocialButtonMouseReleased(MouseEvent event) {
        final Node source = (Node) event.getSource();
        final Transition t = Effects.createScaleTransition(1.2, source);
        t.playFromStart();
    }

    @FXML
    protected void openFacebook() {
        hostServices.tryOpenUri(I18N.getURI("terasology_facebook"));
    }

    @FXML
    protected void openGithub() {
        hostServices.tryOpenUri(I18N.getURI("terasology_github"));
    }

    @FXML
    protected void openDiscord() {
        hostServices.tryOpenUri(I18N.getURI("terasology_discord"));
    }

    @FXML
    protected void openReddit() {
        hostServices.tryOpenUri(I18N.getURI("terasology_reddit"));
    }

    @FXML
    protected void openTwitter() {
        hostServices.tryOpenUri(I18N.getURI("terasology_twitter"));
    }

    @FXML
    protected void openYoutube() {
        hostServices.tryOpenUri(I18N.getURI("terasology_youtube"));
    }

    @FXML
    protected void openLogs() {
        //TODO: how to control the main launcher view from here?
        //contentTabPane.getSelectionModel().select(2);
    }

    void bind(ReadOnlyProperty<Optional<Warning>> property) {
        this.warningProperty.bind(property);
    }
}
