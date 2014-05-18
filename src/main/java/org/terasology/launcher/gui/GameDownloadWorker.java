/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.launcher.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameDownloader;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DownloadException;
import org.terasology.launcher.util.ProgressListener;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

final class GameDownloadWorker extends SwingWorker<Void, Void> implements ProgressListener {

    private static final Logger logger = LoggerFactory.getLogger(GameDownloadWorker.class);

    private final LauncherFrame frame;

    private final GameDownloader gameDownloader;

    private boolean successfulDownloadAndExtract;
    private boolean successfulLoadVersion;

    public GameDownloadWorker(final JProgressBar progressBar, LauncherFrame frame, GameDownloader gameDownloader) {
        this.frame = frame;
        this.gameDownloader = gameDownloader;

        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setString(null);
                    progressBar.setValue((Integer) evt.getNewValue());
                }
            }
        });
        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progressString".equals(evt.getPropertyName())) {
                    progressBar.setString((String) evt.getNewValue());
                }
            }
        });
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString(BundleUtils.getLabel("update_game_startDownload"));
        progressBar.setVisible(true);
        progressBar.setToolTipText(gameDownloader.getDownloadURL().toString());
    }

    @Override
    protected Void doInBackground() {
        try {
            gameDownloader.download(this);
            if (!isCancelled()) {
                firePropertyChange("progressString", null, BundleUtils.getLabel("update_game_extractZip"));
                successfulDownloadAndExtract = gameDownloader.extractAfterDownload();
                if (successfulDownloadAndExtract) {
                    gameDownloader.deleteSilentAfterExtract();

                    firePropertyChange("progressString", null, BundleUtils.getLabel("update_game_gameInfo"));
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

    public void update() {
        // Not implemented!
    }

    public void update(int progress) {
        setProgress(progress);
    }

    @Override
    protected void done() {
        frame.finishedGameDownload(isCancelled(), successfulDownloadAndExtract, successfulLoadVersion, gameDownloader.getGameDirectory());
    }
}
