// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.gui.javafx;

import ch.qos.logback.classic.pattern.RootCauseFirstThrowableProxyConverter;
import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class LogViewController extends AppenderBase<ILoggingEvent> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final StringBuffer buffer;
    private final ThrowableHandlingConverter throwableConverter;

    @FXML
    private TextArea logArea;

    public LogViewController() {
        buffer = new StringBuffer();
        throwableConverter = new RootCauseFirstThrowableProxyConverter();

        ScheduledService<Void> schedule = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        logArea.appendText(buffer.toString());
                        //TODO figure out whether this is thread-safe (I suspect it's not)
                        buffer.delete(0, buffer.length());
                        return null;
                    }
                };
            }
        };
        schedule.setPeriod(Duration.seconds(2));
        schedule.start();
    }

    @FXML
    public void initialize() {
        logArea.setEditable(false);
        logArea.setFont(Font.font("monospaced"));
    }

    private LocalDateTime timestampFromEvent(ILoggingEvent loggingEvent) {
        return new Date(loggingEvent.getTimeStamp())
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    @Override
    protected void append(ILoggingEvent loggingEvent) {
        final String message = loggingEvent.getFormattedMessage();
        final LocalDateTime timestamp = timestampFromEvent(loggingEvent);

        buffer.append(String.format("%s | %-5s | ", DATE_FORMATTER.format(timestamp), loggingEvent.getLevel()));
        buffer.append(message);
        buffer.append("\n");

        var error = loggingEvent.getThrowableProxy();
        if (error != null) {
            var s = throwableConverter.convert(loggingEvent);
            buffer.append(s);
        }
    }
}
