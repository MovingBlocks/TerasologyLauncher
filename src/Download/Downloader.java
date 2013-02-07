package Download;

/*
 MrBarsack
 TerasologyLauncher
 Downloads the latest build.
 */

import Starter.Main;
import VersionControl.Version;
import VersionControl.VersionChecker;

import java.io.*;
import java.net.URL;


public class Downloader {
    public static void downloadNightlyBuild() throws IOException {
        byte[] buffer = new byte[2048];
        int version = VersionChecker.checkVersionNightly();
        float sizeOfUpdate = getSizeOfUpdate(version);

        URL url = new URL("http://jenkins.movingblocks.net/job/Terasology/"+version+"/artifact/build/distributions/Terasology.zip");

        InputStream in = url.openConnection().getInputStream();
        OutputStream out = new FileOutputStream("Terasology.zip");

        File file = new File("Terasology.zip");

        for (int n;(n = in.read(buffer)) != -1;out.write(buffer, 0, n))
        {
            float percent = file.length() / (sizeOfUpdate /(1024*1024) * 1000000) * 100;
            Main.launcher.getProgressBar1().setValue((int)percent);
        }
        in.close();
        out.close();

        Version.saveVersion(version);

    }
    private static float getSizeOfUpdate(int version)
    {
        try {
            URL url = new URL("http://jenkins.movingblocks.net/job/Terasology/"+version+"/artifact/build/distributions/Terasology.zip");
           return url.openConnection().getContentLengthLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
