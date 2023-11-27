// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.platform;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import java.util.Locale;
import java.util.Set;

/**
 * A simplified representation of a computer platform as `os` and `arch`
 */
public final class Platform {

    final private OS os;
    final private Arch arch;

    //TODO(skaldarnar): I'm not fully settled on how to model the Platform. I thought splitting this up into two enums
    //  for OS and Architecture would help with more type-safe construction of these values, simplify comparison to
    //  select the right JRE for a game, etc.
    //
    //  On the other hand, the set of supported platforms is so limited, and continuing on a non-supported platform
    //  does not make much sense. So, maybe it is better to have a rather restrictive and explicit enum in the form of
    //      WINDOWS_X64
    //      LINUX_X64
    //  I'm adding the architecture to this list, as I hope that we'll be able to support old Intel and new M1 Macs at
    //  some point in the future, adding the following to the list:
    //      MAC_X64
    //      MAC_AARCH64
    //  The biggest drawback of being super-strict here is that development on non-supported platforms becomes
    //  impossible where it was just "not ideal" before.
    public static final Set<Platform> SUPPORTED_PLATFORMS = Sets.newHashSet(
            new Platform(OS.WINDOWS, Arch.X64),
            new Platform(OS.LINUX, Arch.X64),
            new Platform(OS.MAC, Arch.X64)
    );

    public Platform(OS os, Arch arch) {
        this.os = os;
        this.arch = arch;
    }

    /**
     * @return the simplified operating system name as platform os
     */
    public String getOs() {
        //TODO: change return type to OS
        return os.name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * @return the simplified operating system architecture as platform arch
     */
    public Arch getArch() {
        return arch;
    }

    public boolean isLinux() {
        return os == OS.LINUX;
    }

    public boolean isMac() {
        return os == OS.MAC;
    }

    public boolean isWindows() {
        return os == OS.WINDOWS;
    }

    public String toString() {
        return "OS '" + os + "', arch '" + arch + "'";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Platform platform = (Platform) o;
        return os == platform.os && arch == platform.arch;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(os, arch);
    }

    /**
     * Get information on the host platform the launcher is currently running on.
     *
     * @return the platform
     */
    public static Platform getPlatform() throws UnsupportedPlatformException {
        final String platformOs = System.getProperty("os.name").toLowerCase();
        final OS os;
        if (platformOs.startsWith("linux")) {
            os = OS.LINUX;
        } else if (platformOs.startsWith("mac os")) {
            os = OS.MAC;
        } else if (platformOs.startsWith("windows")) {
            os = OS.WINDOWS;
        } else {
            throw new UnsupportedPlatformException("Unsupported OS: " + platformOs);
        }

        final String platformArch = System.getProperty("os.arch");
        final Arch arch;
        switch (platformArch) {
            case "x86_64":
            case "amd64":
                arch = Arch.X64;
                break;
            case "x86":
            case "i386":
                arch = Arch.X86;
                break;
            case "aarch64":
            case "arm64":
                arch = Arch.ARM64;
                break;
            default:
                throw new UnsupportedPlatformException("Architecture not supported: " + platformArch);
        }

        return new Platform(os, arch);
    }
}
