// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import org.terasology.launcher.model.GameRelease;

import java.util.List;

/**
 * Common interface for sources of game releaes.
 *
 * <p>
 * Each release repository provides a list of game releases. This is a common API for potentially different sources,
 * such as GitHub, Jenkins build servers, or other similar. A new release repository has to be added in-code by
 * implementing this interface and registering it in the {@link CombinedRepository}.
 * </p>
 */
public interface ReleaseRepository {

    /**
     * Retrieve the list of available game releases from this release repository.
     *
     * @return a list of available game releases (an empty list if fetching was not successful)
     */
    //TODO: this should probably throw an IOException in case of connection errors so that the UI can decide whether to
    //      notify the user about that.
    List<GameRelease> fetchReleases();
}
