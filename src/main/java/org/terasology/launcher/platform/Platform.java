// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.platform;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * A simplified representation of a computer platform as `os` and `arch`
 */
public final class Platform {
    public static final Set<Platform> SUPPORTED_PLATFORMS = Sets.newHashSet(
            new Platform(OS.WINDOWS, Arch.X64),
            new Platform(OS.LINUX, Arch.X64),
            new Platform(OS.MAC, Arch.X64)
    );
    
    private static final Platform PLATFORM = new Platform();

    private final OS os;
    private final Arch arch;

    /**
     * Constructs platform information for the current host system.
     * Simplifies operating system name to one of `linux`, `mac`, `windows` if applicable.
     * Simplifies operating system architecture to one of `32` and `64` if applicable.
     */
    private Platform() throws UnsupportedPlatformException{
        final String platformOs = System.getProperty("os.name").toLowerCase();
        // TODO: consider using regex
        if (platformOs.startsWith("linux")) {
            os = OS.LINUX;
        } else if (platformOs.startsWith("mac os")) {
            os = OS.MAC;
        } else if (platformOs.startsWith("windows")) {
            os = OS.WINDOWS;
        } else {
            throw new UnsupportedPlatformException();
        }

        final String platformArch = System.getProperty("os.arch");
        if (platformArch.equals("x86_64") || platformArch.equals("amd64")) {
            arch = Arch.X64;
        } else if (platformArch.equals("x86") || platformArch.equals("i386")) {
            arch = Arch.X86;
        } else {
            arch = Arch.X64;
            //TODO: throw new UnsupportedPlatformException();
        }
    }

    public Platform(OS os, Arch arch) {
        this.os = os;
        this.arch = arch;
    }

    /**
     * @return the simplified operating system name as platform os
     */
    public String getOs() {
        return os.name().toLowerCase();
    }

    /**
     * @return the simplified operating system architecture as platform arch
     */
    public String getArch() {
        return arch.name().toLowerCase();
    }

    public boolean isLinux() {
        return os.equals(OS.LINUX);
    }

    public boolean isMac() {
        return os.equals(OS.MAC);
    }

    public boolean isWindows() {
        return os.equals(OS.WINDOWS);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Platform platform = (Platform) o;

        if (os != platform.os) return false;
        return arch == platform.arch;
    }

    @Override
    public int hashCode() {
        int result = os != null ? os.hashCode() : 0;
        result = 31 * result + (arch != null ? arch.hashCode() : 0);
        return result;
    }
}
