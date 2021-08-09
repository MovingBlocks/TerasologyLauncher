// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GameIdentifierTest {
    @Test
    void stableReleasesWithDifferentDisplayNamesAreEqual() {
        // Some repositories do not have reliable data for some fields
        //   https://github.com/MovingBlocks/TerasologyLauncher/issues/651
        GameIdentifier fromGithub = new GameIdentifier("alpha-21", "5.1.1", Build.STABLE, Profile.OMEGA);
        GameIdentifier fromInstall = new GameIdentifier("alpha-21+42", "5.1.1", Build.STABLE, Profile.OMEGA);

        assertEquals(fromInstall, fromGithub);
    }

    @Test
    void nightlyReleasesWithDifferentDisplayNamesAreNotEqual() {
        GameIdentifier fromGithub = new GameIdentifier("alpha-21", "5.1.1", Build.NIGHTLY, Profile.OMEGA);
        GameIdentifier fromInstall = new GameIdentifier("alpha-21+42", "5.1.1", Build.NIGHTLY, Profile.OMEGA);

        assertNotEquals(fromInstall, fromGithub);
    }
}
