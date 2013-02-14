package org.terasologyLauncher;

import org.terasologyLauncher.updater.GameData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Skaldarnar
 */
public class Versions {
    private static final String UPDATER_URL = "http://updater.movingblocks.net/";
    private static final String STABLE_VER = "stable.ver";
    private static final String UNSTABLE_VER = "unstable.ver";

    private static List<String> stableVersions = null;
    private static List<String> nightlyVersions = null;

    public static List<String> getVersions(BuildType buildType) {
        if (!GameData.checkInternetConnection()) {
            List<String> list = new ArrayList<String>();
            list.add("Latest");
            return list;
        }
        switch (buildType) {
            case STABLE:
                return getStableVersionsList();
            case NIGHTLY:
                return getNightlyVersionsList();
        }
        return null;    // TODO: do something useful here!
    }

    private static List<String> getNightlyVersionsList() {
        if (nightlyVersions == null) {
            nightlyVersions = new ArrayList<String>();
            nightlyVersions.add("Latest");

            try {
                int latestVersionNumber = GameData.getUpStreamNightlyVersion();
                // for nightly builds, go 8 versions back for the list
                String currentSetting = Settings.getBuildVersion(BuildType.NIGHTLY);
                int buildVersionSetting = currentSetting.equals("Latest") ? latestVersionNumber : Integer.parseInt(currentSetting);
                int minVersionNumber = Math.min(latestVersionNumber - 8, buildVersionSetting);
                for (int i = latestVersionNumber-1; i >= minVersionNumber; i--){
                    nightlyVersions.add(String.valueOf(i));
                }
            } catch (Exception ignored) { }
        }
        return nightlyVersions;
    }

    private static List<String> getStableVersionsList() {
        if (stableVersions == null) {
            stableVersions = new ArrayList<String>();
            stableVersions.add("Latest");

            try {
                URL url = new URL(UPDATER_URL+STABLE_VER);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                int latestVersionNumber = Integer.parseInt(in.readLine());

                try {
                    in.close();
                } catch (Exception ignored) { }

                // for stable builds, go at least 4 versions back for the list
                String currentSetting = Settings.getBuildVersion(BuildType.STABLE);
                int buildVersionSetting = currentSetting.equals("Latest") ? latestVersionNumber : Integer.parseInt(currentSetting);
                int minVersionNumber = Math.min(latestVersionNumber - 4, buildVersionSetting);
                for (int i = latestVersionNumber-1; i >= minVersionNumber; i--){
                    stableVersions.add(String.valueOf(i));
                }
            } catch (Exception ignored) { }
        }
        return stableVersions;
    }
}
