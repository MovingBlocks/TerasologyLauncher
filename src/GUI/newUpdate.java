package GUI;

import Download.Downloader;
import Download.Zip;
import Starter.Main;
import Starter.StarterCreater;
import Starter.Terasology;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class newUpdate extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea aNewBuildIsTextArea;
    private BufferedImage img;

    public newUpdate() {

        try {
            img = ImageIO.read(new File("menuBackground.png"));
        } catch (IOException er) {
            er.printStackTrace();
        }
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        contentPane.setBorder(javax.swing.BorderFactory.createMatteBorder(10, 10, 10, 10, new ImageIcon(img)));

        this.setName("New update!");

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });


        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        this.dispose();
            Thread download = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Downloader.downloadNightlyBuild();
                        Zip.extractArchive(new File("Terasology.zip"), new File("download/"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    StarterCreater.createStarterWindows();
                    Terasology.startTerasology();
                }
            });
           download.start();

    }

    private void onCancel() {
        StarterCreater.createStarterWindows();
        Terasology.startTerasology();
        this.dispose();
    }

    public static void openDialog() {
        newUpdate dialog = new newUpdate();
        dialog.pack();
        dialog.setTitle("New update!");
        dialog.setLocationRelativeTo(Main.launcher.getChangelogTextArea());
        dialog.setVisible(true);
    }
}
