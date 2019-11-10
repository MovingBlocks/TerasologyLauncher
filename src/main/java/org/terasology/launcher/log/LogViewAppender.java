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

package org.terasology.launcher.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.text.DateFormat;
import java.util.Date;

/**
 * Appends to a log view.
 */
public class LogViewAppender extends AppenderBase<ILoggingEvent> {

    private final TableView<ILoggingEvent> view;
    private final ObservableList<ILoggingEvent> data;

    public LogViewAppender(TableView<ILoggingEvent> newView) {
        this.view = newView;

        view.setEditable(false);
        view.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        data = FXCollections.observableArrayList();
        view.setItems(data);

        TableColumn<ILoggingEvent, Date> timestampColumn = new TableColumn<>("Timestamp");
        timestampColumn.setCellValueFactory(item -> new ReadOnlyObjectWrapper<>(new Date(item.getValue().getTimeStamp())));
        // Fix the width such that `CONSTRAINED_RESIZE_POLICY` only applies to the last column
        // TODO: setting fixed width is bad and might conflict with system settings such as font size
        setFixedWidth(timestampColumn, 120);
        timestampColumn.setCellFactory(column ->
            new TableCell<ILoggingEvent, Date>() {
                private final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : formatter.format(item));
                }
            }
        );
        view.getColumns().add(timestampColumn);

        TableColumn<ILoggingEvent, Level> levelColumn = new TableColumn<>("Level");
        levelColumn.setCellValueFactory(item -> new ReadOnlyObjectWrapper<>(item.getValue().getLevel()));
        // Fix the width such that `CONSTRAINED_RESIZE_POLICY` only applies to the last column
        // TODO: setting fixed width is bad and might conflict with system settings such as font size
        setFixedWidth(levelColumn, 50);
        view.getColumns().add(levelColumn);

        TableColumn<ILoggingEvent, String> messageColumn = new TableColumn<>("Message");
        messageColumn.setCellValueFactory(item -> new ReadOnlyObjectWrapper<>(item.getValue().getFormattedMessage()));
        view.getColumns().add(messageColumn);
    }

    @Override
    public void append(ILoggingEvent event) {
        data.add(event);
    }

    private void setFixedWidth(TableColumn<?, ?> column, double width) {
        column.setMinWidth(width);
        column.setMaxWidth(width);
        column.setResizable(false);
    }

}
