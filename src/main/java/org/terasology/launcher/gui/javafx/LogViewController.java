package org.terasology.launcher.gui.javafx;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class LogViewController extends AppenderBase<ILoggingEvent> {
    @FXML
    private TextArea logArea;

    @FXML
    public void initialize() {
        logArea.setEditable(false);
    }

    @Override
    protected void append(ILoggingEvent loggingEvent) {
        logArea.appendText(loggingEvent.getFormattedMessage());
    }
}
