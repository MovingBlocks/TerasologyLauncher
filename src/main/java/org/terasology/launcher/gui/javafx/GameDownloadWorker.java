/*
 * Copyright 2014 MovingBlocks
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

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameDownloader;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DownloadException;

public class GameDownloadWorker extends Task<Void> {

    private static final Logger logger = LoggerFactory.getLogger(GameDownloadWorker.class);
    private final GameDownloader gameDownloader;
    private ApplicationController controller;

    private boolean successfulDownloadAndExtract;
    private boolean successfulLoadVersion;

    public GameDownloadWorker(final ApplicationController controller, final GameDownloader gameDownloader) {
        this.gameDownloader = gameDownloader;
        this.controller = controller;
    }

    @Override
    protected Void call() throws Exception {
        try {
            gameDownloader.downloadZipFile(this);
            if (!isCancelled()) {
                updateMessage(BundleUtils.getLabel("update_game_extractZip"));
                successfulDownloadAndExtract = gameDownloader.extractAfterDownload();
                if (successfulDownloadAndExtract) {
                    gameDownloader.deleteSilentAfterExtract();

                    updateMessage(BundleUtils.getLabel("update_game_gameInfo"));
                    successfulLoadVersion = gameDownloader.updateAfterDownload();
                }
            } else {
                gameDownloader.deleteSilentAfterCancel();
                logger.trace("GameDownloadWorker is cancelled");
            }
        } catch (DownloadException | RuntimeException e) {
            logger.error("There is an error occurred while downloading the game!", e);
        }
        return null;
    }

    @Override
    protected void done() {
        controller.finishedGameDownload(isCancelled(), successfulDownloadAndExtract, successfulLoadVersion, gameDownloader.getGameDirectory());
    }

    public void updateProgress(final long progress) {
        updateProgress(progress, 100);
    }
}
