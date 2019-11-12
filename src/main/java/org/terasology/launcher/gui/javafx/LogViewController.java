package org.terasology.launcher.gui.javafx;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class LogViewController extends AppenderBase<ILoggingEvent> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS");

    @FXML
    private TextArea logArea;

    @FXML
    public void initialize() {
        logArea.setEditable(false);
        logArea.setFont(Font.font("monospaced"));
    }

    @Override
    protected void append(ILoggingEvent loggingEvent) {

        final LocalDateTime timestamp =
                new Date(loggingEvent.getTimeStamp())
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

        final String message =
                String.format("%s | %-5s | %s\n",
                        formatter.format(timestamp),
                        loggingEvent.getLevel(),
                        loggingEvent.getFormattedMessage());

        logArea.appendText(message);
    }
}
