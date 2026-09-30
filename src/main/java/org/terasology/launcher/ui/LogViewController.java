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
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.terasology.launcher.util.I18N;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

// logArea is injected by FXMLLoader after construction, before initialize() runs.
@SuppressWarnings("NullAway.Init")
public class LogViewController extends AppenderBase<ILoggingEvent> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final StringBuilder buffer;
    private final ThrowableHandlingConverter throwableConverter;

    // Bumped by clearLogAction(). A flush task's captured snapshot can otherwise outlive a clear
    // that happens between it releasing the buffer lock and its (Platform.runLater'd) append
    // actually running - tagging the snapshot with the generation, and only appending if it's
    // still current by the time the append runs, closes that window.
    //
    // Both the read (flush) and the bump (clear) happen under the buffer lock, so the tag and the
    // text it describes always agree. Reading the generation outside that lock would leave a
    // window where a clear lands after the read but before the drain: the flush would then tag
    // text drained *after* the clear with the pre-clear generation, and the append would discard
    // log lines that belong on screen.
    private final AtomicLong generation = new AtomicLong();

    @FXML
    private TextArea logArea;

    @FXML
    private Button clearLogButton;

    public LogViewController() {
        buffer = new StringBuilder();
        throwableConverter = new RootCauseFirstThrowableProxyConverter();
    }

    @FXML
    public void initialize() {
        logArea.setEditable(false);
        logArea.setFont(Font.font("monospaced"));
        // Bound, not set: labelBinding tracks the locale property, so the button re-translates
        // when the language is changed in Settings - same as the surrounding tabs.
        clearLogButton.textProperty().bind(I18N.labelBinding("tab_log_clear"));

        // Started here rather than the constructor: logArea isn't injected until after
        // construction, and this is the first point that's guaranteed to run after that.
        ScheduledService<Void> schedule = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        final long gen;
                        final String drained;
                        synchronized (buffer) {
                            gen = generation.get();
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
    protected void clearLogAction() {
        synchronized (buffer) {
            generation.incrementAndGet();
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
