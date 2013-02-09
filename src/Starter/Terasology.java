package Starter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class Terasology {
    public static void startTerasology()
    {
        try {
            String os = System.getProperty("os.name");
            os = os.toLowerCase();

            if(os.contains("win"))
            {
                new ProcessBuilder("download/startscript.bat").start();
            }
            else if (os.contains("mac"))
            {
                new ProcessBuilder("download/run_macosx.command");
            }
            else
            {
                new ProcessBuilder("download/run_linux.sh");
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
