/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.launcher;

import org.powermock.api.mockito.PowerMockito;
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.util.DownloadUtils;
import org.terasology.launcher.util.JobResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

public final class TestingUtils {

    private TestingUtils() {

    }

    public static void mockBuildVersion(GameJob job, int version) throws Exception {
        VersionInformation information = new VersionInformation();
        information.addMapping(job, version);
        mockBuildVersions(information);
    }

    public static void mockBuildVersion(String job, int version) throws Exception {
        VersionInformation information = new VersionInformation();
        information.addMapping(job, version, version);
        mockBuildVersions(information);
    }

    /**
     * Mocks out various methods in DownloadUtils to respond using the giving job map.
     *
     * @param buildValues A map from {@link GameJob} names to a map of omega build number to normal build numbers
     *
     * @throws Exception
     */
    public static void mockBuildVersions(VersionInformation buildValues) throws Exception {
        PowerMockito.spy(DownloadUtils.class);

        // Cache the greatest normal build number (the values of each sub-map) for each job name to use as the 'latest' value
        Map<String, Integer> latestBuilds = new HashMap<>();
        for (Map.Entry<String, Map<Integer, Integer>> entry : buildValues.getMap().entrySet()) {
            latestBuilds.put(entry.getKey(), Collections.max(entry.getValue().keySet()));
        }

        PowerMockito.doAnswer((i) -> latestBuilds.get(i.getArgumentAt(0, String.class))).
                when(DownloadUtils.class, "loadBuildNumberJenkins", anyString(), anyString());

        PowerMockito.doAnswer((i) ->
                buildValues.hasEntry(i.getArgumentAt(0, String.class), i.getArgumentAt(1, Integer.class)) ? JobResult.SUCCESS : JobResult.NOT_BUILT)
                .when(DownloadUtils.class, "loadJobResultJenkins", anyString(), anyInt());

        PowerMockito.doReturn(null).when(DownloadUtils.class, "loadChangeLogJenkins", anyString(), anyInt());

        // Omega trigger
        // This lambda simulates the behavior of an actual Jenkins server, mapping an omega build number to a 'linked' normal build
        PowerMockito.doAnswer((i) -> {
            GameJob job = i.getArgumentAt(0, GameJob.class);
            int omegaBuildNumber = i.getArgumentAt(1, int.class);
            return buildValues.getNormalFromOmega(job.name(), omegaBuildNumber);
        }).when(DownloadUtils.class, "loadEngineTriggerJenkins", anyObject(), anyInt());
    }

    /**
     * A utility class to hold information for mocking builds.
     */
    public static class VersionInformation {
        // A mapping from a job name (normal or omega) to a mapping of build numbers (of that job's type) to linked build numbers of the opposite type.
        // For example, 'GameJob.TerasologyStable.name()' would contain a map of 'normal' numbers to 'omega' numbers,
        // while 'GameJob.TerasologyStable.getOmegaJobName()' would contain a map of 'omega' numbers to 'normal' numbers.
        private Map<String, Map<Integer, Integer>> buildVersions = new HashMap<>();

        public void addMapping(GameJob job, int normalBuild, Integer omegaBuild) {
            this.get(job.name()).put(normalBuild, omegaBuild);
            if (omegaBuild != -1) {
                this.get(job.getOmegaJobName()).put(omegaBuild, normalBuild);
            }

        }

        public void addMapping(GameJob job, int normalBuild) {
            this.addMapping(job, normalBuild, normalBuild);
        }

        public void addMapping(String job, int normalBuild, Integer omegaBuild) {
            this.get(job).put(normalBuild, omegaBuild);
        }

        public int getNormalFromOmega(String job, int omegaBuild) {
            return this.get(job).getOrDefault(omegaBuild, -1);
        }

        public boolean hasEntry(String job, int build) {
            return this.get(job).containsKey(build);
        }

        public Map<String, Map<Integer, Integer>> getMap() {
            return this.buildVersions;
        }

        private Map<Integer, Integer> get(String job) {
            return this.buildVersions.computeIfAbsent(job, (k) -> new HashMap<>());
        }
    }

}
