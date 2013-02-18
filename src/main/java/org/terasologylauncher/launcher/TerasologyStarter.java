package org.terasologylauncher.launcher;

import org.terasologylauncher.Settings;
import org.terasologylauncher.updater.GameData;
import org.terasologylauncher.util.OperatingSystem;
import org.terasologylauncher.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MrBarsack
 * @author Skaldarnar
 */
public class TerasologyStarter {

    public static boolean startGame(){
        OperatingSystem os = OperatingSystem.getOS();
        if (os.isWindows()) {
            return startWindows();
        } else if (os.isMac()) {
            return startMac();
        } else if (os.isUnix()) {
            return startLinux();
        } else {
            System.out.println("Unknown operationg system - cannot start game!");
        }
        return false;
    }

    private static boolean startLinux(){
        List<String> parameters = new ArrayList<String>();
        parameters.add("java");
        parameters.addAll(Settings.createParameters());
        parameters.add("-jar");
        parameters.add(GameData.getGameJar().getName());
        ProcessBuilder pb = new ProcessBuilder(parameters);
        pb.directory(Utils.getWorkingDirectory());
        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean startMac(){
        List<String> parameters = new ArrayList<String>();
        parameters.add("java");
        parameters.addAll(Settings.createParameters());
        parameters.add("-jar");
        parameters.add(GameData.getGameJar().getName());
        ProcessBuilder pb = new ProcessBuilder(parameters);
        pb.directory(Utils.getWorkingDirectory());
        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean startWindows(){
        List<String> parameters = new ArrayList<String>();
        parameters.add("java");
        parameters.addAll(Settings.createParameters());
        parameters.add("-jar");
        parameters.add(GameData.getGameJar().getName());
        ProcessBuilder pb = new ProcessBuilder(parameters);
        pb.directory(Utils.getWorkingDirectory());
        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
