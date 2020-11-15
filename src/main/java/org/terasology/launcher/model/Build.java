// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

/**
 * The build variant, i.e., whether a release is stable or a nightly build.
 *
 * <p>
 * <b>Note:</b> We need this differentiation for historical reasons. Ideally, this information should be encoded
 * in the semantic version (SemVer) as pre-release or build information.
 * </p>
 *
 * @see <a href="https://semver.org/">https://semver.org</a>
 */
public enum Build {
    STABLE,
    NIGHTLY
}
