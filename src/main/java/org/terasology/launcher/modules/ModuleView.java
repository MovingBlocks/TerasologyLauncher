/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher.modules;

import java.text.DateFormat;
import java.util.Date;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class ModuleView {

    public static void configure(TableView<ModuleInfo> view, ModuleManager mgr) {
        view.setEditable(false);

        ObservableList<ModuleInfo> data = FXCollections.observableArrayList(mgr.getAll());
        view.setItems(data);

        // ------------------- DISPLAY NAME ----------------------------------

        TableColumn<ModuleInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(150);
        nameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ModuleInfo, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<ModuleInfo, String> item) {
                return new ReadOnlyObjectWrapper<>(item.getValue().getDisplayName());
            }
        });
        view.getColumns().add(nameCol);

        // ------------------- AUTHOR ----------------------------------

        TableColumn<ModuleInfo, String> authorCol = new TableColumn<>("Author");
        authorCol.setPrefWidth(150);
        authorCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ModuleInfo, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<ModuleInfo, String> item) {
                return new ReadOnlyObjectWrapper<>(item.getValue().getAuthor());
            }
        });
        view.getColumns().add(authorCol);

        // ------------------- VERSION ----------------------------------

        TableColumn<ModuleInfo, String> versionCol = new TableColumn<>("Version");
        versionCol.setPrefWidth(50);
        versionCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ModuleInfo, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<ModuleInfo, String> item) {
                return new ReadOnlyObjectWrapper<>(item.getValue().getVersion());
            }
        });
        view.getColumns().add(versionCol);

        // ------------------- TIMESTAMP ----------------------------------

        TableColumn<ModuleInfo, Date> timestampCol = new TableColumn<>("Last Update");
        timestampCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ModuleInfo, Date>, ObservableValue<Date>>() {

            @Override
            public ObservableValue<Date> call(CellDataFeatures<ModuleInfo, Date> item) {
                return new ReadOnlyObjectWrapper<>(item.getValue().getLastPush());
            }
        });
        view.getColumns().add(timestampCol);

        timestampCol.setCellFactory(new Callback<TableColumn<ModuleInfo, Date>, TableCell<ModuleInfo, Date>>() {
            @Override
            public TableCell<ModuleInfo, Date> call(TableColumn<ModuleInfo, Date> param) {
                return new TableCell<ModuleInfo, Date>() {

                    private DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

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

        // ------------------- STARS ----------------------------------

        TableColumn<ModuleInfo, Integer> levelCol = new TableColumn<>("Stars");
        levelCol.setPrefWidth(50);
        levelCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ModuleInfo, Integer>, ObservableValue<Integer>>() {

            @Override
            public ObservableValue<Integer> call(CellDataFeatures<ModuleInfo, Integer> item) {
                return new ReadOnlyObjectWrapper<>(item.getValue().getStars());
            }
        });
        view.getColumns().add(levelCol);

        // ------------------- VERSION ----------------------------------

        TableColumn<ModuleInfo, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ModuleInfo, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(CellDataFeatures<ModuleInfo, String> item) {
                return new ReadOnlyObjectWrapper<>(item.getValue().getDescription());
            }
        });
        view.getColumns().add(descCol);
    }
}
