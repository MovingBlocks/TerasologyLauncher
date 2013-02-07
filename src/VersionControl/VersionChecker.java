package VersionControl;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

/*
 MrBarsack
 TerasologyLauncher
 Check the number of the latest build.
 */

public class VersionChecker {
    public static int checkVersionNightly()
    {
        try {
            return checkVersionFromSite(new URL("http://updater.movingblocks.net/unstable.ver"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int checkVersionStable()
    {
        try {
            return checkVersionFromSite(new URL("http://updater.movingblocks.net/stable.ver"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int checkVersionFromSite(URL url) throws IOException {
        Scanner scanner = new Scanner(url.openStream());
        String version = scanner.nextLine();
        return Integer.valueOf(version);
    }
}
