// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.tasks;

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameManager;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.remote.DownloadException;

import java.io.IOException;

public final class DownloadTask extends Task<Void> implements ProgressListener {
    private static final Logger logger = LoggerFactory.getLogger(DownloadTask.class);

    private final GameManager gameManager;
    private final GameRelease release;

    public DownloadTask(GameManager gameManager, GameRelease release) {
        this.gameManager = gameManager;
        this.release = release;
    }

    @Override
    protected Void call() throws InterruptedException {
        try {
            gameManager.install(release, this);
        } catch (IOException | DownloadException e) {
            logger.error("Failed to download package '{}' from '{}'",
                    release.getId(), release.getUrl(), e);
        }
        return null;
    }

    @Override
    public void update() {
    }

    @Override
    public void update(int progress) {
        updateProgress(progress, 100);
    }
}
