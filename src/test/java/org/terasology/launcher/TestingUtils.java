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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.util.IBuildRepository;
import org.terasology.launcher.util.JobResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TestingUtils {

    private TestingUtils() {

    }

    /**
     * A utility class to hold information for mocking builds.
     */
    public static class VersionInformation {
        // A mapping from a job name (normal or omega) to a mapping of build numbers (of that job's type) to linked build numbers of the opposite type.
        // For example, 'GameJob.TerasologyStable.name()' would contain a map of 'normal' numbers to 'omega' numbers,
        // while 'GameJob.TerasologyStable.getOmegaJobName()' would contain a map of 'omega' numbers to 'normal' numbers.
        private final Map<String, Map<Integer, Integer>> buildVersions = new HashMap<>();

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

    public static class MockBuildRepository implements IBuildRepository {
        private static final Logger logger = LoggerFactory.getLogger(MockBuildRepository.class);

        private VersionInformation buildValues;
        private final Map<String, Integer> latestBuilds;

        /**
         * Initialized with no builds.
         *
         */
        public MockBuildRepository() {
            this(new VersionInformation());
        }

        /**
         * Mocks out various methods in DownloadUtils to respond using the giving job map.
         *
         * @param buildValues A map from {@link GameJob} names to a map of omega build number to normal build numbers
         */
        public MockBuildRepository(VersionInformation buildValues) {
            assert buildValues != null;
            latestBuilds = new HashMap<>();
            setBuildValues(buildValues);
        }

        public void setBuildValues(VersionInformation buildValues) {
            this.buildValues = buildValues;
            latestBuilds.clear();
            // Cache the greatest normal build number (the values of each sub-map) for each job name to use as the 'latest' value
            for (Map.Entry<String, Map<Integer, Integer>> entry : buildValues.getMap().entrySet()) {
                latestBuilds.put(entry.getKey(), Collections.max(entry.getValue().keySet()));
            }
        }

        @Override
        public boolean isJenkinsAvailable() {
            return true;
        }

        @Override
        public int loadLastStableBuildNumberJenkins(String jobName) {
            return loadLastSuccessfulBuildNumberJenkins(jobName);
        }

        @Override
        public int loadLastSuccessfulBuildNumberJenkins(String jobName) {
            if (!latestBuilds.containsKey(jobName)) {
                logger.error("I have no build number for the name \"{}\"", jobName);
                return -1;
            }
            return latestBuilds.get(jobName);
        }

        @Override
        public JobResult loadJobResultJenkins(String jobName, int buildNumber) {
            return buildValues.hasEntry(jobName, buildNumber) ? JobResult.SUCCESS : JobResult.NOT_BUILT;
        }

        @Override
        public List<String> loadChangeLogJenkins(String jobName, int buildNumber) {
            return Collections.emptyList();
        }

        @Override
        public int loadEngineTriggerJenkins(GameJob job, int omegaBuildNumber) {
            // Omega trigger
            // This simulates the behavior of an actual Jenkins server, mapping an omega build number to a 'linked' normal build
            return buildValues.getNormalFromOmega(job.name(), omegaBuildNumber);
        }

        @Override
        public URL createFileDownloadUrlJenkins(String jobName, int buildNumber, ArtifactType fileName) throws MalformedURLException {
            return new URL("file:///dev/null#" + this.getClass().getName());
        }
    }

    /**
     * temporary class to transition from PowerMock
     *
     * @deprecated delete before merging PR
     */
    @Deprecated
    public static class Whitebox {
        public static Object getInternalState(Object aClass, String attributeName) {
            return null;
        }
    }
}
