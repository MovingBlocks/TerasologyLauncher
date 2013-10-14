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
import org.terasology.launcher.version.TerasologyGameVersion;

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
        final boolean notSuccessful = (gameVersion.getSuccessful() == null) || !gameVersion.getSuccessful();
        if (notSuccessful) {
            b.append("[");
        }
        if (gameVersion.isLatest()) {
            b.append(BundleUtils.getLabel("settings_game_buildVersion_latest"));
        } else {
            b.append(gameVersion.getBuildNumber());
        }
        if (notSuccessful) {
            b.append("]");
        }
        if (!gameVersion.isLatest()) {
            if ((gameVersion.getGameVersionInfo() != null)
                && (gameVersion.getGameVersionInfo().getDisplayVersion() != null)
                && (gameVersion.getGameVersionInfo().getDisplayVersion().trim().length() > 0)) {
                b.append(" - ");
                b.append(gameVersion.getGameVersionInfo().getDisplayVersion());
            }
            if (gameVersion.isInstalled()) {
                b.append(" - ");
                b.append(BundleUtils.getLabel("settings_game_buildVersion_installed"));
            }
        }
        return b.toString();
    }
}
