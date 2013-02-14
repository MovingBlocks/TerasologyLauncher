package org.terasologyLauncher.updater;

import org.terasologyLauncher.BuildType;
import org.terasologyLauncher.util.Utils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * The GameData class provides access to information on the installed game version and type, if an internet
 * connection is available and whether the game could be updated to a newer version.
 */
public class GameData {
    private static final String UPDATER_URL = "http://updater.movingblocks.net/";
    private static final String STABLE_VER = "stable.ver";
    private static final String UNSTABLE_VER = "unstable.ver";

    private static File gameJar;

    private static int upstreamVersionStable  = -1;
    private static int upstreamVersionNightly = -1;

    private static BuildType installedBuildType;
    private static int installedBuildVersion = -1;

    public static boolean isGameInstalled(){
        return getGameJar().exists();
    }

    public static File getGameJar(){
        if (gameJar == null) {
            gameJar = new File(Utils.getWorkingDirectory(), "Terasology.jar");
        }
        return gameJar;
    }

    public static int getUpStreamNightlyVersion() {
        if (upstreamVersionNightly == -1) {
            URL url = null;
            try {
                url = new URL(UPDATER_URL+UNSTABLE_VER);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                upstreamVersionNightly = Integer.parseInt(in.readLine());

                try {
                    in.close();
                } catch (Exception ignored) { }
            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return upstreamVersionNightly;
    }

    public static int getUpStreamStableVersion() {
        if (upstreamVersionStable == -1) {
            URL url = null;
            try {
                url = new URL(UPDATER_URL+STABLE_VER);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                upstreamVersionStable = Integer.parseInt(in.readLine());
                try {
                    in.close();
                } catch (Exception e){

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return upstreamVersionStable;
    }

    public static int getUpStreamVersion(BuildType type) {
        switch (type) {
            case STABLE:
                return getUpStreamStableVersion();
            case NIGHTLY:
                return getUpStreamNightlyVersion();
        }
        return -1;
    }

    public  static boolean checkInternetConnection(){
        //TODO: test jenkins and terasologymods.net
        try {
            final URL testURL = new URL("http://www.google.com");
            final URLConnection connection = testURL.openConnection();
            connection.setConnectTimeout(5000);
            connection.getInputStream();
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {

        }
        return false;
    }

    public static BuildType getInstalledBuildType() {
        if (installedBuildType == null) {
            readVersionFile();
        }
        return installedBuildType;
    }

    public static int getInstalledBuildVersion() {
        if (installedBuildVersion == -1) {
            readVersionFile();
        }
        return installedBuildVersion;
    }

    private static void readVersionFile() {
        try {
            File installedVersionFile = new File(Utils.getWorkingDirectory(), "VERSION");
            if (installedVersionFile.isFile()){
                Scanner scanner = new Scanner(installedVersionFile);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.contains("Build number:")) {
                        installedBuildVersion = Integer.parseInt(line.split(":")[1].trim());
                    } else if (line.contains("GIT branch:")) {
                        String branch = line.split(":")[1].trim();
                        if (branch.equals("develop")) {
                            installedBuildType = BuildType.NIGHTLY;
                        } else {
                         installedBuildType = BuildType.STABLE;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void forceReReadVersionFile(){
        readVersionFile();
    }
}
