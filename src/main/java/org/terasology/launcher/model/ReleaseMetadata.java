// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

import java.util.Date;
import java.util.List;

/**
 *
 */
public class ReleaseMetadata {
    final List<String> changelog;
    final Date timestamp;
    final boolean isLwjgl3;

    public ReleaseMetadata(List<String> changelog, Date timestamp, boolean isLwjgl3) {
        this.changelog = changelog;
        this.timestamp = timestamp;
        this.isLwjgl3 = isLwjgl3;
    }

    public List<String> getChangelog() {
        return changelog;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
