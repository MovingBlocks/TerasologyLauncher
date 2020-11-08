// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameIdentifierTest {

    @Test
    void fromString() {
        final GameIdentifier expected = new GameIdentifier("3.14-SNAPSHOT", Build.STABLE, Profile.ENGINE);

        assertEquals(expected, GameIdentifier.fromString(expected.toString()), "Invariant: 'GameIdentifier.fromString(id.toString)) == id'");
        assertEquals(expected, GameIdentifier.fromString("MINIMAL@3.14-SNAPSHOT+STABLE"), "Should match from correct string");
        assertNull(GameIdentifier.fromString("minimal@3.14-SNAPSHOT+stable"), "Should be case sensitive");
        assertNull(GameIdentifier.fromString("this_is_not_a_game_identifier"));
        assertNull(GameIdentifier.fromString("FULL@42+OMEGA"), "Build and profile info in wrong order");

    }
}