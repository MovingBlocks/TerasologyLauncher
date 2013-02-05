package VersionsKontrolle;


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

        private static Document getLatestChangeLog() throws IOException, ParserConfigurationException, SAXException {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            return builder.parse( new URL("http://jenkins.movingblocks.net/job/Terasology/lastSuccessfulBuild//api/xml?xpath=//changeSet/item/msg[1]|//changeSet/item/author[1]/fullName&wrapper=msgs").openStream() );
        }

         public static void setLatestChangelog()
         {
             Document doc = null;
             try {
                 doc = getLatestChangeLog();
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
             for(int a = 0; a < nodeList.getLength(); a++)
             {
                str.append("-").append(nodeList.item(a).getLastChild().getTextContent()).append(System.lineSeparator());
             }

             try {
                 Main.launcher.getChangelogTextArea().setText("Changelog Build " + VersionChecker.checkVersionFromSite(new URL("http://updater.movingblocks.net/unstable.ver")) + ":" + System.lineSeparator() + str.toString());
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }

}
