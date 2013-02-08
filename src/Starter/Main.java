package Starter;
/*
 MrBarsack
 TerasologyLauncher
 Main Class.
 */

import GUI.Launch;
import VersionControl.Changelog;
import VersionControl.Version;

import java.io.IOException;

public class Main {
    public static Launch launcher;
    public static void main(String[] args)
    {
        launcher = new Launch();
        Changelog.setChangelog();
        launcher.createLauncher();
        try {
            Version.firstTime();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
