/*
 * Copyright 2020 MovingBlocks
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

package org.terasology.launcher.gui.javafx;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class PackageItem {
    private final ReadOnlyStringProperty name;
    private final ObservableList<VersionItem> versionItems;

    PackageItem(final String name, final List<VersionItem> versions) {
        this.name = new SimpleStringProperty(name);
        versionItems = FXCollections.observableList(versions);
    }

    @Override
    public String toString() {
        return name.get();
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public ObservableList<VersionItem> getVersionItems() {
        return versionItems;
    }
}
