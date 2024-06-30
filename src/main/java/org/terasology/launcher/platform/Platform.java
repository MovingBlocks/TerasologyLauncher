// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.platform;

/**
 * A simplified representation of a computer platform as `os` and `arch`
 */
public enum Platform {

    // unsupported platforms commented out, but might be useful for local development
    // MACOS_X64(OS.MAC, Arch.X64),
    // supported platforms by both the game and the launcher
    WINDOWS_X64(OS.WINDOWS, Arch.X64),
    LINUX_X64(OS.LINUX, Arch.X64);

    /**
     * The simplified operating system identifier.
     */
    public final OS os;
    /**
     * The simplified architecture identifier.
     */
    public final Arch arch;

    Platform(OS os, Arch arch) {
        this.os = os;
        this.arch = arch;
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

        return fromOsAndArch(os, arch);
    }

    /**
     * Derive the {@link Platform} from the given {@link OS} and {@link Arch}
     *
     * @throws UnsupportedPlatformException if the given OS and Arch combination is not supported
     */
    public static Platform fromOsAndArch(OS os, Arch arch) throws UnsupportedPlatformException {
        if (os.equals(OS.WINDOWS) && arch.equals(Arch.X64)) {
            return WINDOWS_X64;
        } else if (os.equals(OS.LINUX) && arch.equals(Arch.X64)) {
            return LINUX_X64;
//        } else if (os.equals(OS.MAC) && arch.equals(Arch.X64)) {
//            return MACOS_X64;
        } else {
            throw new UnsupportedPlatformException("Unsupported platform: " + os + " " + arch);
        }
    }
}
