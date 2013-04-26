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
import org.terasologylauncher.util.DownloadException;
import org.terasologylauncher.util.DownloadUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Skaldarnar
 */
public final class TerasologyGameVersions {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyGameVersions.class);

    private List<Integer> stableVersions;
    private List<Integer> nightlyVersions;
    private Integer stableVersion;
    private Integer nightlyVersion;

    public TerasologyGameVersions() {
    }

    public void loadVersions(final Settings settings) {
        loadStable(settings);
        loadNightly(settings);
    }

    public boolean isVersionsLoaded() {
        return (stableVersion != null) && (nightlyVersion != null)
            && (stableVersions != null) && (nightlyVersions != null)
            && !stableVersions.isEmpty() && !nightlyVersions.isEmpty();
    }

    public List<Integer> getVersions(final BuildType buildType) {
        if (BuildType.STABLE == buildType) {
            return stableVersions;
        }
        return nightlyVersions;
    }

    public Integer getVersion(final BuildType buildType) {
        if (BuildType.STABLE == buildType) {
            return stableVersion;
        }
        return nightlyVersion;
    }

    private void loadStable(final Settings settings) {
        try {
            stableVersion = DownloadUtils.loadLatestSuccessfulVersion(DownloadUtils.TERASOLOGY_STABLE_JOB_NAME);
            // for stable builds, go at least 4 versions back for the list
            final int buildVersionSetting;
            if (settings.isBuildVersionLatest(BuildType.STABLE)) {
                buildVersionSetting = stableVersion;
            } else {
                buildVersionSetting = settings.getBuildVersion(BuildType.STABLE);
            }
            final int minVersionNumber = Math.min(stableVersion - 4, buildVersionSetting);
            stableVersions = new ArrayList<Integer>();
            stableVersions.add(Settings.BUILD_VERSION_LATEST);
            for (int i = stableVersion; i >= minVersionNumber; i--) {
                stableVersions.add(i);
            }
        } catch (DownloadException e) {
            logger.error("Retrieving latest stable version build number failed.", e);
        }
    }

    private void loadNightly(final Settings settings) {
        try {
            nightlyVersion = DownloadUtils.loadLatestSuccessfulVersion(DownloadUtils.TERASOLOGY_NIGHTLY_JOB_NAME);
            // for nightly builds, go 8 versions back for the list
            final int buildVersionSetting;
            if (settings.isBuildVersionLatest(BuildType.NIGHTLY)) {
                buildVersionSetting = nightlyVersion;
            } else {
                buildVersionSetting = settings.getBuildVersion(BuildType.NIGHTLY);
            }
            final int minVersionNumber = Math.min(nightlyVersion - 8, buildVersionSetting);
            nightlyVersions = new ArrayList<Integer>();
            nightlyVersions.add(Settings.BUILD_VERSION_LATEST);
            for (int i = nightlyVersion; i >= minVersionNumber; i--) {
                nightlyVersions.add(i);
            }
        } catch (DownloadException e) {
            logger.error("Retrieving latest nightly version build number failed.", e);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[stableVersion=" + stableVersion + ", nightlyVersion="
            + nightlyVersion + "]";
    }
}
