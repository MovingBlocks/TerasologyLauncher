// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.tasks;

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameManager;
import org.terasology.launcher.model.GameIdentifier;

import java.io.IOException;

public final class DeleteTask extends Task<Void> {
    private static final Logger logger = LoggerFactory.getLogger(DeleteTask.class);

    private final GameManager gameManager;
    private final GameIdentifier game;

    public DeleteTask(GameManager gameManager, GameIdentifier game) {
        this.gameManager = gameManager;
        this.game = game;
    }

    @Override
    protected Void call() {
        try {
            gameManager.remove(game);
        } catch (IOException e) {
            logger.error("Failed to remove package '{}'", game, e);
        }
        return null;
    }
}
