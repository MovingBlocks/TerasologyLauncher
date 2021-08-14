// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import com.vdurmont.semver4j.Semver;

import java.nio.file.Path;

/** An Installation that does not depend on filesystem interaction to determine engineVersion. */
class StubInstallation extends Installation {
    StubInstallation(Path installDirectory) {
        super(installDirectory);
    }

    @Override
    Semver getEngineVersion() {
        return new Semver("0.0.1");
    }
}
