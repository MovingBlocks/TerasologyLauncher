package VersionControl;


import Starter.Main;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

public class Changelog {

        private static Document getChangeLogDoc(int build) throws IOException, ParserConfigurationException, SAXException {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            return builder.parse( new URL("http://jenkins.movingblocks.net/job/Terasology/"+build+"/api/xml?xpath=//changeSet/item/msg[1]|//changeSet/item/author[1]/fullName&wrapper=msgs").openStream() );
        }

         private static String getChangelog(int build)
         {
             Document doc = null;
             try {
                 doc = getChangeLogDoc(build);
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (ParserConfigurationException e) {
                 e.printStackTrace();
             } catch (SAXException e) {
                 e.printStackTrace();
             }
             NodeList nodeList = null;
             if (doc != null) {
                 nodeList = doc.getElementsByTagName("msg");
             }

             StringBuilder str = new StringBuilder();

             str.append("Build: ").append(build).append(System.lineSeparator());
             for(int a = 0; a < nodeList.getLength(); a++)
             {
                str.append('-').append(nodeList.item(a).getLastChild().getTextContent()).append(System.lineSeparator());
             }
             str.append(System.lineSeparator());
              return str.toString();
         }

        public static void setChangelog()
        {
            try {
                if(Version.checkVersionFromLocal() == VersionChecker.checkVersionNightly())
                {
                    Main.launcher.getChangelogTextArea().setText("You already have the latest build!");
                }
                else if(Version.checkVersionFromLocal() != 0)
                {
                    StringBuilder str = new StringBuilder("Changes since last update:");

                    str.append(System.lineSeparator());

                    for(int a = Version.checkVersionFromLocal(); a < VersionChecker.checkVersionNightly(); a++)
                    {
                     str.append(getChangelog(VersionChecker.checkVersionNightly() - (a - Version.checkVersionFromLocal())));
                    }
                    Main.launcher.getChangelogTextArea().setText(str.toString());
                }
                else
                {
                    Main.launcher.getChangelogTextArea().setText("Press on 'start' to download the latest build!");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
}
