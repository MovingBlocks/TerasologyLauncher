package VersionControl;

import java.io.*;

public class Version {
    // First time == no build yet.

    public static void firstTime() throws IOException
    {
        File file = new File("Settings.txt");
        FileWriter writer = new FileWriter(file,false);

        FileReader fr = new FileReader(file);

        writer.write(0);
        writer.flush();
        writer.close();
    }
    public static void saveVersion(int version) throws IOException {
       File file = new File("Version.txt");
       FileWriter writer = new FileWriter(file,false);
       writer.write(String.valueOf(version));
       writer.flush();
       writer.close();
    }
    public static int checkVersionFromLocal() throws IOException {
        FileReader fr = new FileReader("Version.txt");
        BufferedReader br = new BufferedReader(fr);
        return Integer.valueOf(br.readLine());
    }
}
