// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

/**
 * A simplified representation of a computer platform as `os` and `arch`
 */
public final class Platform {
    
    private static final Platform PLATFORM = new Platform();

    private String os;
    private String arch;

    /**
     * Constructs platform information for the current host system.
     * Simplifies operating system name to one of `linux`, `mac`, `windows` if applicable.
     * Simplifies operating system architecture to one of `32` and `64` if applicable.
     */
    private Platform() {
        final String platformOs = System.getProperty("os.name").toLowerCase();
        // TODO: consider using regex
        if (platformOs.startsWith("linux")) {
            os = "linux";
        } else if (platformOs.startsWith("mac os")) {
            os = "mac";
        } else if (platformOs.startsWith("windows")) {
            os = "windows";
        } else {
            os = platformOs;
        }

        final String platformArch = System.getProperty("os.arch");
        if (platformArch.equals("x86_64") || platformArch.equals("amd64")) {
            arch = "64";
        } else if (platformArch.equals("x86") || platformArch.equals("i386")) {
            arch = "32";
        } else {
            arch = platformArch;
        }
    }

    /**
     * @return the simplified operating system name as platform os
     */
    public String getOs() {
        return os;
    }

    /**
     * @return the simplified operating system architecture as platform arch
     */
    public String getArch() {
        return arch;
    }

    public boolean isLinux() {
        return os.equals("linux");
    }

    public boolean isMac() {
        return os.equals("mac");
    }

    public boolean isWindows() {
        return os.equals("windows");
    }

    public String toString() {
        return "OS '" + os + "', arch '" + arch + "'";
    }

    /**
     * Get information on the host platform the launcher is currently running on.
     *
     * @return the platform
     */
    public static Platform getPlatform() {
        return PLATFORM;
    }
}
