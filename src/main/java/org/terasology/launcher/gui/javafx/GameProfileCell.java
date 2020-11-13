// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.gui.javafx;

import javafx.scene.control.ListCell;
import org.terasology.launcher.model.Profile;

/**
 * Custom {@link ListCell} to display a game {@link Profile} in human readable form.
 */
final class GameProfileCell extends ListCell<Profile> {
    @Override
    protected void updateItem(Profile profile, boolean empty) {
        super.updateItem(profile, empty);
        if (empty) {
            setText(null);
        } else {
            switch (profile) {
                case OMEGA:
                    setText("Terasology");
                    break;
                case ENGINE:
                    setText("Terasology Lite (engine-only)");
                    break;
            }
        }
    }
}
