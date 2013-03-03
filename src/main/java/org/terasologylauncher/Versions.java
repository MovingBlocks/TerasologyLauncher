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

package org.terasologylauncher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasologylauncher.updater.GameData;

import java.util.ArrayList;
import java.util.List;

/** @author Skaldarnar */
public class Versions {

    private static final Logger logger = LoggerFactory.getLogger(Versions.class);

    private static final String UPDATER_URL = "http://updater.movingblocks.net/";
    private static final String STABLE_VER = "stable.ver";
    private static final String UNSTABLE_VER = "unstable.ver";

    private static List<String> stableVersions = null;
    private static List<String> nightlyVersions = null;

    public static List<String> getVersions(BuildType buildType) {
        if (!GameData.checkInternetConnection()) {
            List<String> list = new ArrayList<String>();
            list.add("Latest");
            return list;
        }
        switch (buildType) {
            case STABLE:
                return getStableVersionsList();
            case NIGHTLY:
                return getNightlyVersionsList();
        }
        return null;    // TODO: do something useful here!
    }

    private static List<String> getNightlyVersionsList() {
        if (nightlyVersions == null) {
            nightlyVersions = new ArrayList<String>();
            nightlyVersions.add("Latest");
            // TODO: Check for internet connection before?
            try {
                int latestVersionNumber = GameData.getUpStreamNightlyVersion();
                // for nightly builds, go 8 versions back for the list
                String currentSetting = Settings.getBuildVersion(BuildType.NIGHTLY);
                int buildVersionSetting = currentSetting.equals("Latest") ? latestVersionNumber : Integer.parseInt(currentSetting);
                int minVersionNumber = Math.min(latestVersionNumber - 8, buildVersionSetting);
                for (int i = latestVersionNumber - 1; i >= minVersionNumber; i--) {
                    nightlyVersions.add(String.valueOf(i));
                }
            } catch (Exception e) {
                logger.error("Retrieving latest nightly version build number failed.", e);
            }
        }
        return nightlyVersions;
    }

    private static List<String> getStableVersionsList() {
        if (stableVersions == null) {
            stableVersions = new ArrayList<String>();
            stableVersions.add("Latest");
            // TODO: Check for internet connection before?
            try {
                int latestVersionNumber = GameData.getUpStreamStableVersion();
                // for stable builds, go at least 4 versions back for the list
                String currentSetting = Settings.getBuildVersion(BuildType.STABLE);
                int buildVersionSetting = currentSetting.equals("Latest") ? latestVersionNumber : Integer.parseInt(currentSetting);
                int minVersionNumber = Math.min(latestVersionNumber - 4, buildVersionSetting);
                for (int i = latestVersionNumber - 1; i >= minVersionNumber; i--) {
                    stableVersions.add(String.valueOf(i));
                }
            } catch (Exception e) {
                logger.error("Retrieving latest stable version build number failed.", e);
            }
        }
        return stableVersions;
    }
}
