// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

import ch.qos.logback.classic.pattern.RootCauseFirstThrowableProxyConverter;
import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class LogViewController extends AppenderBase<ILoggingEvent> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final StringBuilder buffer;
    private final ThrowableHandlingConverter throwableConverter;

    // Bumped by clearLogAction(). A flush task's captured snapshot can otherwise outlive a clear
    // that happens between it releasing the buffer lock and its (Platform.runLater'd) append
    // actually running - snapshotting the generation alongside the snapshot, and only appending
    // if it's still current by the time the append runs, closes that window. Both the increment
    // (clearLogAction) and the re-check (inside the runLater callback) happen on the FX
    // Application Thread, so there's no race between them specifically.
    private final AtomicLong generation = new AtomicLong();

    @FXML
    private TextArea logArea;

    public LogViewController() {
        buffer = new StringBuilder();
        throwableConverter = new RootCauseFirstThrowableProxyConverter();

        ScheduledService<Void> schedule = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        final long gen = generation.get();
                        final String drained;
                        synchronized (buffer) {
                            drained = buffer.toString();
                            buffer.setLength(0);
                        }
                        if (!drained.isEmpty()) {
                            // appendText() must run on the FX Application Thread - this task's
                            // call() runs on a background thread, not that one.
                            Platform.runLater(() -> {
                                if (gen == generation.get()) {
                                    logArea.appendText(drained);
                                }
                            });
                        }
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

    @FXML
    protected void clearLogAction() {
        generation.incrementAndGet();
        synchronized (buffer) {
            buffer.setLength(0);
        }
        logArea.clear();
    }

    private LocalDateTime timestampFromEvent(ILoggingEvent loggingEvent) {
        return Instant.ofEpochMilli(loggingEvent.getTimeStamp())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    @Override
    protected void append(ILoggingEvent loggingEvent) {
        final String message = loggingEvent.getFormattedMessage();
        final LocalDateTime timestamp = timestampFromEvent(loggingEvent);

        synchronized (buffer) {
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
}
