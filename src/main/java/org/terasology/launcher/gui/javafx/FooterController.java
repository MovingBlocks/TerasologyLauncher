package org.terasology.launcher.gui.javafx;

import javafx.animation.Transition;
import javafx.application.HostServices;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import java.net.URI;
import java.util.Optional;

public class FooterController {

    private static final Logger logger = LoggerFactory.getLogger(FooterController.class);

    @FXML
    private Button warningButton;
    @FXML
    private Label versionInfo;
    private Optional<HostServices> hostServices;
    private Property<Boolean> lowOnSpace;

    public FooterController() {
        lowOnSpace = new SimpleBooleanProperty(false);
    }

    private void updateLabels() {
        final String launcherVersion = TerasologyLauncherVersionInfo.getInstance().getDisplayVersion();
        if (launcherVersion.isEmpty()) {
            versionInfo.setText(BundleUtils.getLabel("launcher_versionInfo"));
        } else {
            versionInfo.setText(launcherVersion);
        }
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = Optional.ofNullable(hostServices);
    }

    @FXML
    public void initialize() {
        updateLabels();
        lowOnSpace.addListener((value, oldValue, newValue) -> updateWarningButton(newValue));
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
        openUri(BundleUtils.getURI("terasology_facebook"));
    }

    @FXML
    protected void openGithub() {
        openUri(BundleUtils.getURI("terasology_github"));
    }

    @FXML
    protected void openDiscord() {
        openUri(BundleUtils.getURI("terasology_discord"));
    }

    @FXML
    protected void openReddit() {
        openUri(BundleUtils.getURI("terasology_reddit"));
    }

    @FXML
    protected void openTwitter() {
        openUri(BundleUtils.getURI("terasology_twitter"));
    }

    @FXML
    protected void openYoutube() {
        openUri(BundleUtils.getURI("terasology_youtube"));
    }

    @FXML
    protected void openLogs() {
        //TODO: how to control the main launcher view from here?
        //contentTabPane.getSelectionModel().select(2);
    }

    private void openUri(URI uri) {
        if (uri != null) {
            hostServices.ifPresent(services -> services.showDocument(uri.toString()));
        }
    }

    private void updateWarningButton(boolean showWarning) {
        if (showWarning) {
            warningButton.setVisible(true);
            warningButton.setTooltip(new Tooltip(BundleUtils.getLabel("message_warning_lowOnSpace")));
            logger.warn(BundleUtils.getLabel("message_warning_lowOnSpace"));
        } else {
            warningButton.setVisible(false);
        }
    }

    public void bind(ReadOnlyProperty<Boolean> lowOnSpace) {
        this.lowOnSpace.bind(lowOnSpace);
    }
}
