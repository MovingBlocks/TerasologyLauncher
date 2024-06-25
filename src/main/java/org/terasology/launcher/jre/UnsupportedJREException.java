// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.jre;

import org.terasology.launcher.platform.Platform;

public class UnsupportedJREException extends Exception {
    private Platform platform;
    private int version;

    public UnsupportedJREException(Platform platform, int version) {
        this.platform = platform;
        this.version = version;
    }

    @Override
    public String getMessage() {
        return "UnsupportedJre: No managed JRE supported for Java version " + version + " and platform " + platform;
    }
}
