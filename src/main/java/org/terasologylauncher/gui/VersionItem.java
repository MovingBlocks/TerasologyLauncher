/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasologylauncher.gui;

import org.terasologylauncher.util.BundleUtils;
import org.terasologylauncher.version.TerasologyGameVersion;

/**
 * @author Mathias Kalb
 */
final class VersionItem {

    private final TerasologyGameVersion gameVersion;

    public VersionItem(final TerasologyGameVersion gameVersion) {
        this.gameVersion = gameVersion;
    }

    public Integer getVersion() {
        return gameVersion.getBuildVersion();
    }

    /**
     * Used at SettingsMenu.
     */
    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        if (gameVersion.isLatest()) {
            b.append(BundleUtils.getLabel("settings_game_buildVersion_latest"));
        } else {
            if (gameVersion.isSuccessful()) {
                b.append(gameVersion.getBuildNumber());
            } else {
                b.append("[");
                b.append(gameVersion.getBuildNumber());
                b.append("]");
            }
            if (gameVersion.isInstalled()) {
                b.append(" - ");
                b.append(BundleUtils.getLabel("settings_game_buildVersion_installed"));
            }
        }
        return b.toString();
    }
}
