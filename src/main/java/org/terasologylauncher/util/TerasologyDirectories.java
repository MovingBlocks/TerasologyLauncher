package org.terasologylauncher.util;

import java.io.File;

/**
 * @author Skaldarnar
 *
 * This class collects references to important working directories, like game backups and launcherDir.
 */
public class TerasologyDirectories {
    private static final File backupDir = new File(Utils.getWorkingDirectory(), "backups");
    private static final File launcherDir = new File(Utils.getWorkingDirectory(), "launcher");

    private static final File savedWorldsDir = new File(Utils.getWorkingDirectory(), "SAVED_WORLDS");
    private static final File screenshotsDir = new File(Utils.getWorkingDirectory(), "screens");
    private static final File modsDir        = new File(Utils.getWorkingDirectory(), "mods");

    public static final File getBackupDir() {
        return backupDir;
    }

    public static final File getLauncherDir() {
        return launcherDir;
    }

    public static final File getSavedWorldsDir() {
        return savedWorldsDir;
    }

    public static final File getScreenshotsDir() {
        return screenshotsDir;
    }

    public static final File getModsDir() {
        return modsDir;
    }
}
