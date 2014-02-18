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

import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.ProgressListener;

public class SplashProgressIndicator implements ProgressListener {

    private final SplashScreenWindow splash;
    private final String[] downloadMsg;
    private long lastTime;
    private int counter;

    public SplashProgressIndicator(SplashScreenWindow splash, String labelKey) {
        this.splash = splash;
        downloadMsg = new String[3];
        downloadMsg[0] = BundleUtils.getLabel(labelKey) + ".";
        downloadMsg[1] = downloadMsg[0] + ".";
        downloadMsg[2] = downloadMsg[1] + ".";
        lastTime = 0;
        counter = 0;
    }

    @Override
    public void update() {
        final long currentTime = System.currentTimeMillis();
        if ((currentTime - lastTime) > 500) {
            splash.getInfoLabel().setText(downloadMsg[counter]);
            if (counter == 2) {
                counter = 0;
            } else {
                counter++;
            }
            lastTime = currentTime;
        }
    }

    public void update(int progress) {
        update();
    }

    public boolean isCancelled() {
        return false;
    }

}
