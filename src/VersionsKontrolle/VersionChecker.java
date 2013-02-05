package VersionsKontrolle;

/*
 MrBarsack
 TerasologyLauncher
 Check the number of the latest build.
 */

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class VersionChecker {

    public static int checkVersionFromSite(URL url) throws IOException {
        Scanner scanner = new Scanner(url.openStream());
        String version = scanner.nextLine();
        return Integer.valueOf(version);
    }
}
