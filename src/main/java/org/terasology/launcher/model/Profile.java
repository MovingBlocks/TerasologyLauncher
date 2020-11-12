// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.model;

/**
 * The profile (or code line/release line) describing what the release contains.
 *
 * <p>
 *     We have (or are planning to have) different release profiles. The most important profile is "Omega" containing
 *     pre-bundled modules for the major game modes. Other profiles, like the bare "Engine", may have different content,
 *     e.g., containing no modules at all.
 * </p>
 * <p>
 *     Different profiles can have different versioning, i.e., releases with the same version but of different profiles
 *     are <b>not comparable</b>!
 * </p>
 */
public enum Profile {
    OMEGA,
    ENGINE
}
