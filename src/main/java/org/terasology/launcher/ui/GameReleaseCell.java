// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.util.I18N;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

/**
 * Custom {@link ListCell} used to display a {@link GameRelease} along with its installation status.
 */
final class GameReleaseCell extends ListCell<GameRelease> {
    private static final Image ICON_CHECK = I18N.getFxImage("icon_check");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    private final ImageView iconStatus;

    private final Set<GameIdentifier> installedGames;
    private final boolean isButtonCell;

    GameReleaseCell(Set<GameIdentifier> installedGames) {
        this(installedGames, false);
    }

    GameReleaseCell(Set<GameIdentifier> installedGames, boolean isButtonCell) {
        this.installedGames = installedGames;
        this.isButtonCell = isButtonCell;
        iconStatus = new ImageView(ICON_CHECK);
    }

    @Override
    protected void updateItem(GameRelease item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            final GameIdentifier id = item.getId();

            String displayVersion;
            if (id.getBuild().equals(Build.NIGHTLY)) {
                setStyle("-fx-font-weight: normal");
                displayVersion = "preview " + id.getDisplayVersion() + " (" + DATE_FORMAT.format(item.getTimestamp()) + ")";
            } else {
                setStyle("-fx-font-weight: bold");
                displayVersion = "release " + id.getDisplayVersion();
            }

            setText(displayVersion);
            iconStatus.setVisible(installedGames.contains(id));
            // the graphic is not shown on the button cell, so we only set it for list cells
            if (!isButtonCell) {
                setGraphic(iconStatus);
            }
        }
    }
}
