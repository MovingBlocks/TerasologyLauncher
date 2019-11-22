/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.launcher.game;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.terasology.launcher.TestingUtils;
import org.terasology.launcher.settings.BaseLauncherSettings;
import org.terasology.launcher.util.DownloadUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DownloadUtils.class, TerasologyGameVersions.class})
public class TestGameVersions {

    // These functions are common assertions, to be used with requireAssertions
    private static final BiConsumer<TerasologyGameVersion, Integer> REQUIRES_SUCCESSFUL =
        (version, i) -> Assert.assertEquals(
                String.format("Build should be successful: %s!", version),
                true,
                version.getSuccessful());
    private static final BiConsumer<TerasologyGameVersion, Integer> REQUIRES_MIN_BUILD_PLUS_ONE =
        (version, i) -> Assert.assertEquals(
                String.format("Build number mismatch!: %s", version),
                version.getJob().getMinBuildNumber() + 1,
                (long) version.getBuildNumber());
    private static final BiConsumer<TerasologyGameVersion, Integer> REQUIRES_OMEGA_SAME_NUMBER =
        (version, i) -> Assert.assertEquals(
                String.format("Omega build should have the same number as regular build: %s", version),
                version.getBuildNumber(),
                version.getOmegaNumber());

    // These functions can be used with
    private static final BiConsumer<TerasologyGameVersion, Integer> REQUIRES_NULL_OMEGA =
        (version, i) -> Assert.assertNull(String.format("Omega build should not be available: %s!", version), version.getOmegaNumber());

    @Test
    public void testGetSingleVersion() throws Exception {
        TerasologyGameVersions gameVersions = this.getGameVersions(true, false);
        this.loadGameVersions(gameVersions);
        this.runAssertions(gameVersions, 2, REQUIRES_NULL_OMEGA.andThen(REQUIRES_MIN_BUILD_PLUS_ONE).andThen(REQUIRES_SUCCESSFUL));
    }

    @Test
    public void testGetMultipleSequentialVersions() throws Exception {
        TerasologyGameVersions gameVersions = this.getGameVersions(false, false);

        int minStable = GameJob.TerasologyStable.getMinBuildNumber() + 1;
        int minUnstable = GameJob.Terasology.getMinBuildNumber() + 1;

        TestingUtils.VersionInformation buildVersions = new TestingUtils.VersionInformation();

        buildVersions.addMapping(GameJob.TerasologyStable, minStable, -1);
        buildVersions.addMapping(GameJob.TerasologyStable, minStable + 1, -1);
        buildVersions.addMapping(GameJob.TerasologyStable, minStable + 2, -1);

        buildVersions.addMapping(GameJob.Terasology, minUnstable, -1);
        buildVersions.addMapping(GameJob.Terasology, minUnstable + 1, -1);
        buildVersions.addMapping(GameJob.Terasology, minUnstable + 2, -1);

        // Three versions per job, plus the virtual 'latest' version
        int expectedVersionsPerJob = 4;

        TestingUtils.mockBuildVersions(buildVersions);
        this.loadGameVersions(gameVersions);

        int[] stableVers = this.getBuildArray(minStable, minStable + 2);
        int[] unstableVars = this.getBuildArray(minUnstable, minUnstable + 2);

        this.runAssertions(gameVersions, expectedVersionsPerJob, REQUIRES_NULL_OMEGA.andThen(REQUIRES_SUCCESSFUL).andThen((version, i) -> {
            int[] target = version.getJob() == GameJob.TerasologyStable ? stableVers : unstableVars;
            Assert.assertEquals(String.format("Build number mismatch: %s", version), target[i], (long) version.getBuildNumber());

        }));
    }

    @Test
    public void testGetMultipleNonSequentialVersions() throws Exception {
        TerasologyGameVersions gameVersions = this.getGameVersions(false, false);

        int minStable = GameJob.TerasologyStable.getMinBuildNumber() + 1;
        int minUnstable = GameJob.Terasology.getMinBuildNumber() + 1;

        TestingUtils.VersionInformation buildVersions = new TestingUtils.VersionInformation();

        buildVersions.addMapping(GameJob.TerasologyStable, minStable);
        buildVersions.addMapping(GameJob.TerasologyStable, minStable + 3);

        buildVersions.addMapping(GameJob.Terasology, minUnstable);
        buildVersions.addMapping(GameJob.Terasology, minUnstable + 3);

        int expectedBuildsPerJob = 5; // Four builds (including the 'filled in' ones) per job, plus the virtual 'latest' version

        Set<Integer> successfulStable = new HashSet<>();
        successfulStable.addAll(Arrays.asList(minStable, minStable + 3));

        Set<Integer> successfulUnstable = new HashSet<>();
        successfulUnstable.addAll(Arrays.asList(minUnstable, minUnstable + 3));

        TestingUtils.mockBuildVersions(buildVersions);
        this.loadGameVersions(gameVersions);

        int[] stableVers = this.getBuildArray(minStable, minStable + 3);
        int[] unstableVars = this.getBuildArray(minUnstable, minUnstable + 3);

        this.runAssertions(gameVersions, expectedBuildsPerJob, (version, i) -> {
            boolean isSuccessful =
                    (version.getJob() == GameJob.TerasologyStable ? successfulStable : successfulUnstable).contains(version.getBuildNumber());

            Assert.assertEquals(
                    String.format("Unexpected 'successful' state for version %s", version),
                    isSuccessful, version.getSuccessful());
            Assert.assertEquals(
                    String.format("Unexpected omega build for version %s", version),
                    isSuccessful ? version.getBuildNumber() : null, version.getOmegaNumber());
            Assert.assertEquals(
                    String.format("Build number mismatch! 'Gaps' between available builds should be filled %s", version),
                    (version.getJob() == GameJob.TerasologyStable ? stableVers : unstableVars)[i], (long) version.getBuildNumber());
        });
    }

    @Test
    public void testSingleOmegaBuildDetection() throws Exception {
        TerasologyGameVersions gameVersions = this.getGameVersions(true, true);
        this.loadGameVersions(gameVersions);
        this.runAssertions(gameVersions, 2, REQUIRES_OMEGA_SAME_NUMBER.andThen(REQUIRES_MIN_BUILD_PLUS_ONE).andThen(
                REQUIRES_SUCCESSFUL));
    }

    private int[] getBuildArray(int start, int stop) {
        return IntStream.concat(IntStream.of(stop), // Duplicate, to account for virtual 'latest' version
                IntStream.iterate(stop, i -> i - 1).limit(stop - start + 1)
        ).toArray();
    }

    private void runAssertions(TerasologyGameVersions gameVersions, int expected, BiConsumer<TerasologyGameVersion, Integer> additionalAssertions) {
        for (GameJob job : GameJob.values()) {
            List<TerasologyGameVersion> versions = gameVersions.getGameVersionList(job);
            Assert.assertEquals("Unexpected number of versions for " + job.name(), expected, versions.size());

            TerasologyGameVersion latest = versions.get(0);
            Assert.assertTrue(String.format(
                    "First version is not marked latest: %s", latest),
                    latest.isLatest());
            Assert.assertEquals(
                    "First ('latest') build and second ('real') build do not have the same build number!",
                    latest.getBuildNumber(), versions.get(1).getBuildNumber());
            for (int i = 0; i < versions.size(); i++) {
                TerasologyGameVersion version = versions.get(i);
                Assert.assertEquals(String.format(
                        "Build should not be installed: %s!", version),
                        false, version.isInstalled());
                additionalAssertions.accept(version, i);

                if (version != latest && version.isLatest()) {
                    Assert.assertNull(String.format("Found more than one latest build: %s %s", latest, version), latest);
                }
            }
        }
    }

    private void loadGameVersions(TerasologyGameVersions gameVersions) throws Exception {
        Path launcherDir = Files.createTempDirectory("terasology-launcher-dir").toAbsolutePath();
        Path gameDirectory = Files.createTempDirectory("terasology-game-dir").toAbsolutePath();

        BaseLauncherSettings settings = new BaseLauncherSettings(launcherDir);
        settings.init();

        gameVersions.loadGameVersions(settings, launcherDir, gameDirectory);
    }

    private TerasologyGameVersions getGameVersions(boolean doMapping, boolean omega) throws Exception {
        spy(TerasologyGameVersions.class);

        TerasologyGameVersions gameVersions = spy(new TerasologyGameVersions());

        if (doMapping) {
            this.setUpdatesAvailable(true, omega);
        }

        // We suppress fetching the game version info, so that the test doesn't depend on the network
        // and is deterministic
        PowerMockito.doAnswer((i) -> {
            TerasologyGameVersion version = i.getArgumentAt(0, TerasologyGameVersion.class);
            version.setGameVersionInfo(TerasologyGameVersionInfo.getEmptyGameVersionInfo());
            return null;
        }).when(gameVersions, "loadAndSetGameVersionInfo", any(), any(), any(), any());

        return gameVersions;
    }


    private void setUpdatesAvailable(boolean available, boolean omegaAvailable) throws Exception {
        int minStable = GameJob.TerasologyStable.getMinBuildNumber() + 1;
        int minUnstable = GameJob.Terasology.getMinBuildNumber() + 1;

        TestingUtils.VersionInformation buildVersions = new TestingUtils.VersionInformation();

        buildVersions.addMapping(GameJob.TerasologyStable, available ? minStable : 0, omegaAvailable ? minStable : -1);
        buildVersions.addMapping(GameJob.Terasology, available ? minUnstable : 0, omegaAvailable ? minUnstable : -1);

        TestingUtils.mockBuildVersions(buildVersions);
    }

}
