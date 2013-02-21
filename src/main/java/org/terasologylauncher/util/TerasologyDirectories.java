package org.terasologylauncher.util;

import java.io.File;

/**
 * @author Skaldarnar
 *         <p/>
 *         This class collects references to important working directories, like game backups and launcher directory.
 */
public class TerasologyDirectories {
    public static final String BACKUP_DIR_NAME = "backups";
    public static final String LAUNCHER_DIR_NAME = "launcher";
    public static final String SAVED_WORLDS_DIR_NAME = "SAVED_WORLDS";
    public static final String SCREENSHOTS_DIR_NAME = "screens";
    public static final String MODS_DIR_NAME = "mods";


    private static final File BACKUP_DIR = new File(Utils.getWorkingDirectory(), BACKUP_DIR_NAME);
    private static final File LAUNCHER_DIR = new File(Utils.getWorkingDirectory(), LAUNCHER_DIR_NAME);

    private static final File SAVED_WORLDS_DIR = new File(Utils.getWorkingDirectory(), SAVED_WORLDS_DIR_NAME);
    private static final File SCREENSHOTS_DIR = new File(Utils.getWorkingDirectory(), SCREENSHOTS_DIR_NAME);
    private static final File MODS_DIR = new File(Utils.getWorkingDirectory(), MODS_DIR_NAME);

    public static final File getBackupDir() {
        return BACKUP_DIR;
    }

    public static final File getLauncherDir() {
        return LAUNCHER_DIR;
    }

    public static final File getSavedWorldsDir() {
        return SAVED_WORLDS_DIR;
    }

    public static final File getScreenshotsDir() {
        return SCREENSHOTS_DIR;
    }

    public static final File getModsDir() {
        return MODS_DIR;
    }
}
