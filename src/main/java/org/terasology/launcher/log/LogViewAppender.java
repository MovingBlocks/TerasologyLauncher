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

        data = FXCollections.observableArrayList();
        view.setItems(data);

        TableColumn<ILoggingEvent, Date> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ILoggingEvent, Date>, ObservableValue<Date>>() {

            @Override
            public ObservableValue<Date> call(CellDataFeatures<ILoggingEvent, Date> item) {
                return new ReadOnlyObjectWrapper<>(new Date(item.getValue().getTimeStamp()));
            }
        });
        view.getColumns().add(timestampCol);

        TableColumn<ILoggingEvent, Level> levelCol = new TableColumn<>("Level");
        levelCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ILoggingEvent, Level>, ObservableValue<Level>>() {

            @Override
            public ObservableValue<Level> call(CellDataFeatures<ILoggingEvent, Level> item) {
                return new ReadOnlyObjectWrapper<>(item.getValue().getLevel());
            }
        });
        view.getColumns().add(levelCol);

        timestampCol.setCellFactory(new Callback<TableColumn<ILoggingEvent, Date>, TableCell<ILoggingEvent, Date>>() {
            @Override
            public TableCell<ILoggingEvent, Date> call(TableColumn<ILoggingEvent, Date> param) {
                return new TableCell<ILoggingEvent, Date>() {

                    private DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

                    @Override
                    protected void updateItem(Date item, boolean empty) {
                        super.updateItem(item, empty);

                        if (!empty) {
                            setText(df.format(item));
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });

        TableColumn<ILoggingEvent, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ILoggingEvent, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<ILoggingEvent, String> item) {
                return new ReadOnlyObjectWrapper<>(item.getValue().getFormattedMessage());
            }
        });
        view.getColumns().add(messageCol);

    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void append(ILoggingEvent event) {
        data.add(event);
    }

}
