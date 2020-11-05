// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.releasefoo;

import org.terasology.launcher.model.GameRelease;

import java.util.List;

public interface ReleaseRepository {
    List<GameRelease> fetchReleases();
}
