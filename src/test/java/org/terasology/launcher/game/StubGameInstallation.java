// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import org.semver4j.Semver;

import java.nio.file.Path;

/** An Installation that does not depend on filesystem interaction to determine engineVersion or game jar. */
class StubGameInstallation extends GameInstallation {

    Path gameJar;

    StubGameInstallation(Path installDirectory, Path relativeGameJarPath) {
        super(installDirectory);
        gameJar = this.getPath().resolve(relativeGameJarPath);
    }

    @Override
    Semver getEngineVersion() {
        return new Semver("0.0.1");
    }

    @Override
    Path getGameJarPath() {
        return gameJar;
    }
}
