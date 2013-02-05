package Starter;

import java.awt.*;
import java.io.File;
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
                Desktop.getDesktop().open(new File("download/startscript.bat"));
            }
            else if (os.contains("nux"))
            {
                Desktop.getDesktop().open(new File("download/run_linux.sh"));
            }
            else if (os.contains("mac"))
            {
                Desktop.getDesktop().open(new File("download/run_macosx.command"));
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
