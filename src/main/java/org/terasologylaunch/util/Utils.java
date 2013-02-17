package org.terasologylauncher.util;

import java.io.*;

/**
 * @author Skaldarnar
 */
public class Utils {
    private static File workDir = null;

    public static File getWorkingDirectory() {
        if (workDir == null) {
            workDir = getWorkingDirectory("terasology"); 
        }
        return workDir;
    }

    private static File getWorkingDirectory(String applicationName) {
        String userHome = System.getProperty("user.home",".");
        File workingDirectory;

        OperatingSystem os = OperatingSystem.getOS();
        if (os.isUnix()) {
            workingDirectory = new File(userHome, '.' + applicationName + '/');
        } else if (os.isWindows()) {
            String applicationData = System.getenv("APPDATA");
            if (applicationData != null) {
                workingDirectory = new File(applicationData, "." + applicationName + '/');
            } else {
                workingDirectory = new File(userHome, '.' + applicationName + '/');
            }
        } else if (os.isMac()) {
            workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
        } else {
            workingDirectory = new File(userHome, applicationName + '/');
        }
        if ((!workingDirectory.exists()) && (!workingDirectory.mkdirs())) {
            throw new RuntimeException("The working directory could not be created: " + workingDirectory);
        }
        return workingDirectory;
    }
}
