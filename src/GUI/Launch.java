package GUI;

/*
 MrBarsack
 TerasologyLauncher
 GUI Class.
 */

import Download.Downloader;
import Download.Zip;
import Starter.Main;
import Starter.StarterCreater;
import Starter.Terasology;
import VersionControl.Version;
import VersionControl.VersionChecker;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Launch {
   private JPanel Panel;
   private JButton Starter;
   private JProgressBar progressBar1;
   private JTextArea changelogTextArea;
   private JList list1;

    public JTextArea getChangelogTextArea() {
        return changelogTextArea;
    }


    public JProgressBar getProgressBar1 ()
    {
        return progressBar1;
    }

    public Launch() {
        Starter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    Thread download = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                               if(Version.checkVersionFromLocal() != VersionChecker.checkVersionNightly())
                               {
                                if(Version.checkVersionFromLocal() != 0)
                                {
                                newUpdate.openDialog();
                                }
                                else
                                {
                                 Downloader.downloadNightlyBuild();
                                 Zip.extractArchive(new File("Terasology.zip"), new File("download/"));
                                    StarterCreater.createStarterWindows();
                                    Terasology.startTerasology();
                                }
                               }
                               else
                               {
                                   StarterCreater.createStarterWindows();
                                   Terasology.startTerasology();
                               }

                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }

                        }
                    });
                download.start();
            }
        });
    }

    public void createLauncher()
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    displayJFrame();
                }
            });
        }

        void displayJFrame()
        {
            BufferedImage img = null;
            Launch launch =  Main.launcher;
            JPanel panel = launch.Panel;

            try {
                img = ImageIO.read(new File("menuBackground.png"));
            } catch (IOException er) {
                er.printStackTrace();
            }

            JFrame frame = new JFrame("Terasology Launcher 0.1");

            frame.setContentPane(panel);

            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setPreferredSize(new Dimension(800, 400));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.setBackground(Color.GREEN);
            frame.setForeground(Color.DARK_GRAY);

            panel.setBorder(javax.swing.BorderFactory.createMatteBorder(10, 10, 10, 10, new ImageIcon(img)));

        }
    }