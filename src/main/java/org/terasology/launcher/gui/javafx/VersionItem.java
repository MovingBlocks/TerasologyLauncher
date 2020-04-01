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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.terasology.launcher.packages.Package;

class VersionItem {
    private final ReadOnlyObjectProperty<Package> linkedPackage;
    private final ReadOnlyStringProperty version;
    private final BooleanProperty installed;

    VersionItem(final Package linkedPackage) {
        this.linkedPackage = new SimpleObjectProperty<>(linkedPackage);
        version = new SimpleStringProperty(linkedPackage.getVersion());
        installed = new SimpleBooleanProperty(linkedPackage.isInstalled());
    }

    @Override
    public String toString() {
        return version.get();
    }

    public Package getLinkedPackage() {
        return linkedPackage.get();
    }

    public ReadOnlyObjectProperty<Package> linkedPackageProperty() {
        return linkedPackage;
    }

    public boolean isInstalled() {
        return installed.get();
    }

    public BooleanProperty installedProperty() {
        return installed;
    }

    public String getVersion() {
        return version.get();
    }

    public ReadOnlyStringProperty versionProperty() {
        return version;
    }
}
