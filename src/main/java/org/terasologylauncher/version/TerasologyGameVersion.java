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

package org.terasologylauncher.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasologylauncher.BuildType;
import org.terasologylauncher.Settings;
import org.terasologylauncher.updater.GameData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Skaldarnar
 */
public final class TerasologyGameVersion {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyGameVersion.class);

    private static List<Integer> stableVersions;
    private static List<Integer> nightlyVersions;

    private TerasologyGameVersion() {
    }

    public static List<Integer> getVersions(final Settings settings, final BuildType buildType) {
        if (!GameData.checkInternetConnection()) {
            final List<Integer> list = new ArrayList<Integer>();
            list.add(Settings.BUILD_VERSION_LATEST);
            return list;
        }
        switch (buildType) {
            case STABLE:
                return getStableVersionsList(settings);
            case NIGHTLY:
                return getNightlyVersionsList(settings);
        }
        return null; // TODO: do something useful here!
    }

    private static List<Integer> getNightlyVersionsList(final Settings settings) {
        if (nightlyVersions == null) {
            nightlyVersions = new ArrayList<Integer>();
            nightlyVersions.add(Settings.BUILD_VERSION_LATEST);
            // TODO: Check for internet connection before?
            try {
                final int latestVersionNumber = GameData.getUpStreamNightlyVersion();
                // for nightly builds, go 8 versions back for the list
                final int buildVersionSetting;
                if (settings.isBuildVersionLatest(BuildType.NIGHTLY)) {
                    buildVersionSetting = latestVersionNumber;
                } else {
                    buildVersionSetting = settings.getBuildVersion(BuildType.NIGHTLY);
                }
                final int minVersionNumber = Math.min(latestVersionNumber - 8, buildVersionSetting);
                for (int i = latestVersionNumber - 1; i >= minVersionNumber; i--) {
                    nightlyVersions.add(i);
                }
            } catch (Exception e) {
                logger.error("Retrieving latest nightly version build number failed.", e);
            }
        }
        return nightlyVersions;
    }

    private static List<Integer> getStableVersionsList(final Settings settings) {
        if (stableVersions == null) {
            stableVersions = new ArrayList<Integer>();
            stableVersions.add(Settings.BUILD_VERSION_LATEST);
            // TODO: Check for internet connection before?
            try {
                final int latestVersionNumber = GameData.getUpStreamStableVersion();
                // for stable builds, go at least 4 versions back for the list
                final int buildVersionSetting;
                if (settings.isBuildVersionLatest(BuildType.STABLE)) {
                    buildVersionSetting = latestVersionNumber;
                } else {
                    buildVersionSetting = settings.getBuildVersion(BuildType.STABLE);
                }
                final int minVersionNumber = Math.min(latestVersionNumber - 4, buildVersionSetting);
                for (int i = latestVersionNumber - 1; i >= minVersionNumber; i--) {
                    stableVersions.add(i);
                }
            } catch (Exception e) {
                logger.error("Retrieving latest stable version build number failed.", e);
            }
        }
        return stableVersions;
    }
}
