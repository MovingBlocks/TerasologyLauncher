package org.terasologylauncher.util;

import java.io.File;

/**
 * @author Skaldarnar
 *
 * This class collects references to important working directories, like game backups and launcherDir.
 */
public class TerasologyDirectories {
    public static final String BACKUP_DIR_NAME          = "backups";
    public static final String LAUNCHER_DIR_NAME        = "launcher";
    public static final String SAVED_WORLDS_DIR_NAME    = "SAVED_WORLDS";
    public static final String SCREENSHOTS_DIR_NAME     = "screens";
    public static final String MODS_DIR_NAME            = "mods";


    private static final File backupDir = new File(Utils.getWorkingDirectory(), BACKUP_DIR_NAME);
    private static final File launcherDir = new File(Utils.getWorkingDirectory(), LAUNCHER_DIR_NAME);

    private static final File savedWorldsDir = new File(Utils.getWorkingDirectory(), SAVED_WORLDS_DIR_NAME);
    private static final File screenshotsDir = new File(Utils.getWorkingDirectory(), SCREENSHOTS_DIR_NAME);
    private static final File modsDir        = new File(Utils.getWorkingDirectory(), MODS_DIR_NAME);

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
