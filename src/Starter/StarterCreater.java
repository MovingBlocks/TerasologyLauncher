package Starter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StarterCreater {
    //ToDo: Min- and Maxram
    //ToDo: already a start file?
    public static void createStarterWindows()
    {
        File file = new File("download/startscript.bat");
        try {
            FileWriter writer = new FileWriter(file,false);
            writer.write("cd %cd%\\download\\" + System.lineSeparator() +"Terasology.jar -java -jar" + System.lineSeparator() +"exit");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
