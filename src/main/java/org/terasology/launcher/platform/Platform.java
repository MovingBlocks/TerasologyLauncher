// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.platform;

import java.util.Locale;

/**
 * A simplified representation of a computer platform as `os` and `arch`
 */
public enum Platform {

    WINDOWS_X64(OS.WINDOWS, Arch.X64),
    WINDOWS_ARM64(OS.WINDOWS, Arch.ARM64),
    LINUX_X64(OS.LINUX, Arch.X64),
    MACOS_X64(OS.MAC, Arch.X64),
    MACOS_ARM64(OS.MAC, Arch.ARM64);

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

    /**
     * Whether OpenJFX ships a working WebKit native library ({@code javafx.web}, backing
     * {@link javafx.scene.web.WebView}) for this platform.
     * <p>
     * {@code javafx.application.Platform.isSupported(ConditionalFeature.WEB)} can't be trusted for
     * this: it only reports whether the javafx.web module was compiled in, not whether its native
     * library actually works on this OS/arch. Vendor JREs for platforms upstream OpenJFX doesn't
     * support - like Windows/aarch64, see https://bugs.openjdk.org/browse/JDK-8314064, whose PR
     * explicitly excludes javafx.web - still bundle a javafx.web module (for API compatibility)
     * that reports itself as supported and then crashes with an UnsatisfiedLinkError the moment a
     * WebView is constructed.
     */
    public boolean supportsWebView() {
        return this != WINDOWS_ARM64;
    }

    /**
     * {@link #supportsWebView()} for the platform this JVM is currently running on.
     * <p>
     * Falls back to {@code false} when the platform can't be identified at all: the plain-text view
     * renders everywhere, so an unrecognized platform is better served by it than by a WebView that
     * may not load. In practice this is unreachable - startup (see {@code LauncherInitTask}) already
     * resolves the platform before any view is built.
     */
    public static boolean currentSupportsWebView() {
        try {
            return getPlatform().supportsWebView();
        } catch (UnsupportedPlatformException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "OS '" + os + "', arch '" + arch + "'";
    }

    /**
     * Get information on the host platform the launcher is currently running on.
     *
     * @return the platform
     */
    public static Platform getPlatform() throws UnsupportedPlatformException {
        final String platformOs = System.getProperty("os.name").toLowerCase(Locale.ROOT);
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
        final Arch arch = switch (platformArch) {
            case "x86_64", "amd64" -> Arch.X64;
            case "x86", "i386" -> Arch.X86;
            case "aarch64", "arm64" -> Arch.ARM64;
            default -> throw new UnsupportedPlatformException("Architecture not supported: " + platformArch);
        };

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
        } else if (os.equals(OS.WINDOWS) && arch.equals(Arch.ARM64)) {
            return WINDOWS_ARM64;
        } else if (os.equals(OS.LINUX) && arch.equals(Arch.X64)) {
            return LINUX_X64;
        } else if (os.equals(OS.MAC) && arch.equals(Arch.X64)) {
            return MACOS_X64;
        } else if (os.equals(OS.MAC) && arch.equals(Arch.ARM64)) {
            return MACOS_ARM64;
        } else {
            throw new UnsupportedPlatformException("Unsupported platform: " + os + " " + arch);
        }
    }
}
