/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher.util;

import org.terasology.launcher.game.GameJob;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public interface IBuildRepository {
    boolean isJenkinsAvailable();

    int loadLastStableBuildNumberJenkins(String jobName) throws DownloadException;

    int loadLastSuccessfulBuildNumberJenkins(String jobName) throws DownloadException;

    JobResult loadJobResultJenkins(String jobName, int buildNumber) throws DownloadException;

    List<String> loadChangeLogJenkins(String jobName, int buildNumber) throws DownloadException;

    int loadEngineTriggerJenkins(GameJob job, int omegaBuildNumber) throws DownloadException;

    URL createFileDownloadUrlJenkins(String jobName, int buildNumber, ArtifactType fileName) throws MalformedURLException;

    enum ArtifactType {
        FILE_TERASOLOGY_GAME_ZIP("/artifact/build/distributions/Terasology.zip"), FILE_TERASOLOGY_OMEGA_ZIP("/artifact/distros/omega/build/distributions/TerasologyOmega" +
                ".zip"), FILE_TERASOLOGY_GAME_VERSION_INFO("/artifact/build/resources/main/org/terasology/version/versionInfo.properties");
        private final String value;

        ArtifactType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
