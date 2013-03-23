/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasologylauncher.gui;

import org.terasologylauncher.BuildType;
import org.terasologylauncher.Settings;
import org.terasologylauncher.launcher.TerasologyStarter;
import org.terasologylauncher.updater.GameData;
import org.terasologylauncher.updater.GameDownloader;
import org.terasologylauncher.util.BundleUtils;
import org.terasologylauncher.util.DirectoryUtils;
import org.terasologylauncher.util.OperatingSystem;
import org.terasologylauncher.version.TerasologyGameVersion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * @author Skaldarnar
 */
public class LauncherFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final int FRAME_WIDTH = 880;
    private static final int FRAME_HEIGHT = 520;

    private static final int INFO_PANEL_WIDTH = 600;
    private static final int INFO_PANEL_HEIGHT = 300;

    private static final String SETTINGS_ACTION = "settings";
    private static final String CANCEL_ACTION = "cancel";

    private static final String START_ACTION = "start";
    private static final String DOWNLOAD_ACTION = "download";

    private JButton startButton;
    private JButton settingsButton;
    private JButton cancelButton;

    private JButton facebook;
    private JButton github;
    private JButton gplus;
    private JButton twitter;
    private JButton youtube;
    private JButton reddit;

    private JProgressBar progressBar;

    private JLabel forums;
    private JLabel issues;
    private JLabel mods;

    private JPanel topPanel;
    private JPanel updatePanel;

    private JTextPane infoTextPane;

    private SettingsMenu settingsMenu;

    private final File terasologyDirectory;
    private final OperatingSystem os;
    private final Settings settings;
    private final TerasologyGameVersion gameVersion;

    public LauncherFrame(final File terasologyDirectory, final OperatingSystem os, final Settings settings,
                         final TerasologyGameVersion gameVersion) {
        this.terasologyDirectory = terasologyDirectory;
        this.os = os;
        this.settings = settings;
        this.gameVersion = gameVersion;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(BundleUtils.getLabel("launcher_title"));
        setIconImage(BundleUtils.getImage("icon"));

        initComponents();

        updateStartButton();

        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((dim.width - FRAME_WIDTH) / 2, (dim.height - FRAME_HEIGHT) / 2, FRAME_WIDTH, FRAME_HEIGHT);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setResizable(false);
        getContentPane().add(new BackgroundImage(FRAME_WIDTH, FRAME_HEIGHT));
    }

    private void initComponents() {
        final int xShift = 0;
        int yShift = 0;
        if (isUndecorated()) {
            yShift += 30;
        }

        // Setup start button
        startButton = new TSButton(BundleUtils.getLabel("launcher_start"));
        startButton.setBounds(FRAME_WIDTH - 96 - 16 - xShift, (FRAME_HEIGHT - 70 - 40) + yShift, 96, 32);
        startButton.setActionCommand(START_ACTION);
        startButton.addActionListener(this);

        // Options Button
        settingsButton = new TSButton(BundleUtils.getLabel("launcher_settings"));
        settingsButton.setBounds(FRAME_WIDTH - 96 - 16 - xShift, (FRAME_HEIGHT - 70 - (2 * 40)) + yShift, 96, 32);
        settingsButton.setActionCommand(SETTINGS_ACTION);
        settingsButton.addActionListener(this);

        // Cancel button
        cancelButton = new TSButton(BundleUtils.getLabel("launcher_cancel"));
        cancelButton.setBounds(FRAME_WIDTH - 96 - 16 - xShift, (FRAME_HEIGHT - 70) + yShift, 96, 32);
        cancelButton.setActionCommand(CANCEL_ACTION);
        cancelButton.addActionListener(this);

        // Transparent top panel and content/update panel
        topPanel = new TransparentPanel(0.5f);
        topPanel.setBounds(0, 0, FRAME_WIDTH, 96);

        updatePanel = new TransparentPanel(0.5f);
        updatePanel.setBounds(
            (FRAME_WIDTH - INFO_PANEL_WIDTH) / 2,
            (FRAME_HEIGHT - INFO_PANEL_HEIGHT) / 2,
            INFO_PANEL_WIDTH,
            INFO_PANEL_HEIGHT);

        infoTextPane = new JTextPane();
        infoTextPane.setFont(new Font("Arial", Font.PLAIN, 14));
        infoTextPane.setEditable(false);
        infoTextPane.setEnabled(false);
        infoTextPane.setHighlighter(null);
        infoTextPane.setOpaque(false);

        infoTextPane.setForeground(Color.WHITE);

        // TODO BundleUtils
        infoTextPane.setText("Lorem ipsum dolor sit amet, \n " +
            "consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore " +
            "\n magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores " +
            "\n et ea rebum. " +
            "\n Stet clita kasd gubergren, " +
            "\n no sea takimata sanctus est Lorem ipsum dolor sit amet. " +
            "\n Lorem ipsum dolor " +
            "sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore " +
            "\n \n \n magna aliquyam erat, sed diam voluptua. " +
            "\n At vero eos et accusam et justo duo dolores et ea rebum. " +
            "\n Stet clita kasd gubergren, " +
            "\n no sea takimata sanctus est " +
            "\n Lorem ipsum dolor sit amet.");

        //infoTextPane.setBounds(updatePanel.getX() + 8, updatePanel.getY() + 8, updatePanelWidth - 16,
        // updatePanelHeight - 16);
        final JScrollPane sp = new JScrollPane();
        sp.getViewport().add(infoTextPane);
        sp.getVerticalScrollBar().setOpaque(false);
        sp.getVerticalScrollBar().setUI(new TSScrollBarUI());
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setBorder(BorderFactory.createEmptyBorder());
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.setPreferredSize(new Dimension(INFO_PANEL_WIDTH - 16, INFO_PANEL_HEIGHT - 16));
        sp.setBounds(updatePanel.getX() + 8, updatePanel.getY() + 8, INFO_PANEL_WIDTH - 16, INFO_PANEL_HEIGHT - 16);

        // Terasology logo
        final JLabel logo = new JLabel();
        logo.setBounds(8, 0, 400, 96);
        logo.setIcon(BundleUtils.getImageIcon("logo"));

        // Forums link
        forums = new LinkJLabel(BundleUtils.getLabel("launcher_forum"), BundleUtils.getURI("terasology_forum"));
        forums.setFont(forums.getFont().deriveFont(24f));
        forums.setBounds(480, 36, 96, 32);

        // Issues link
        issues = new LinkJLabel(BundleUtils.getLabel("launcher_issues"),
            BundleUtils.getURI("terasology_github_issues"));
        issues.setFont(issues.getFont().deriveFont(24f));
        issues.setBounds(640, 36, 96, 32);

        // Mods
        mods = new LinkJLabel(BundleUtils.getLabel("launcher_mods"), BundleUtils.getURI("terasology_mods"));
        mods.setFont(mods.getFont().deriveFont(24f));
        mods.setBounds(FRAME_WIDTH - 96 - 16 - xShift, 36, 96, 32);

        // Progress Bar
        progressBar = new JProgressBar();
        progressBar.setBounds((FRAME_WIDTH / 2) - 200, (FRAME_HEIGHT - 70) + yShift, 400, 23);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);

        // Social media
        github = new LinkJButton(BundleUtils.getURI("terasology_github"));
        github.setIcon(BundleUtils.getImageIcon("github"));
        github.setRolloverIcon(BundleUtils.getImageIcon("github_hover"));
        github.setBounds(8 + xShift, (FRAME_HEIGHT - 70) + yShift, 32, 32);
        github.setBorder(null);

        youtube = new LinkJButton(BundleUtils.getURI("terasology_youtube"));
        youtube.setIcon(BundleUtils.getImageIcon("youtube"));
        youtube.setRolloverIcon(BundleUtils.getImageIcon("youtube_hover"));
        youtube.setBounds(8 + 38 + xShift, (FRAME_HEIGHT - 70) + yShift, 32, 32);
        youtube.setBorder(null);

        gplus = new LinkJButton(BundleUtils.getURI("terasology_gplus"));
        gplus.setIcon(BundleUtils.getImageIcon("gplus"));
        gplus.setRolloverIcon(BundleUtils.getImageIcon("gplus_hover"));
        gplus.setBounds(8 + (38 * 2) + xShift, (FRAME_HEIGHT - 70) + yShift, 32, 32);
        gplus.setBorder(null);

        facebook = new LinkJButton(BundleUtils.getURI("terasology_facebook"));
        facebook.setIcon(BundleUtils.getImageIcon("facebook"));
        facebook.setRolloverIcon(BundleUtils.getImageIcon("facebook_hover"));
        facebook.setBounds(8 + (38 * 3) + xShift, (FRAME_HEIGHT - 70) + yShift, 32, 32);
        facebook.setBorder(null);

        twitter = new LinkJButton(BundleUtils.getURI("terasology_twitter"));
        twitter.setIcon(BundleUtils.getImageIcon("twitter"));
        twitter.setRolloverIcon(BundleUtils.getImageIcon("twitter_hover"));
        twitter.setBounds(8 + (38 * 4) + xShift, (FRAME_HEIGHT - 70) + yShift, 32, 32);
        twitter.setBorder(null);

        reddit = new LinkJButton(BundleUtils.getURI("terasology_reddit"));
        reddit.setIcon(BundleUtils.getImageIcon("reddit"));
        reddit.setRolloverIcon(BundleUtils.getImageIcon("reddit_hover"));
        reddit.setBounds(8 + (38 * 5) + xShift, (FRAME_HEIGHT - 70) + yShift, 32, 32);
        reddit.setBorder(null);

        final Container contentPane = getContentPane();
        contentPane.setLayout(null);

        contentPane.add(logo);
        contentPane.add(forums);
        contentPane.add(issues);
        contentPane.add(mods);

        contentPane.add(startButton);
        contentPane.add(settingsButton);
        contentPane.add(cancelButton);

        contentPane.add(github);
        contentPane.add(twitter);
        contentPane.add(facebook);
        contentPane.add(gplus);
        contentPane.add(youtube);
        contentPane.add(reddit);

        contentPane.add(progressBar);

        contentPane.add(sp);

        contentPane.add(topPanel);
        contentPane.add(updatePanel);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() instanceof JComponent) {
            action(e.getActionCommand(), (JComponent) e.getSource());
        }
    }

    private void action(final String command, final Component component) {
        if (command.equals(SETTINGS_ACTION)) {
            if ((settingsMenu == null) || !settingsMenu.isVisible()) {
                settingsMenu = new SettingsMenu(settings, gameVersion);
                settingsMenu.setModal(true);
                settingsMenu.setVisible(true);
                settingsMenu.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(final WindowEvent e) {
                        updateStartButton();
                        // TODO Update all labels (language change)
                    }
                });
            }
        } else if (command.equals(CANCEL_ACTION)) {
            dispose();
            System.exit(0);
        } else if (command.equals(START_ACTION)) {
            final TerasologyStarter terasologyStarter = new TerasologyStarter(terasologyDirectory, os,
                settings.getMaximalMemory(), settings.getInitialMemory());
            if (terasologyStarter.startGame()) {
                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(null, BundleUtils.getLabel("message_error_gameStart"),
                    BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
            }
        } else if (command.equals(DOWNLOAD_ACTION)) {
            // cleanup the directories (keep savedWorlds and screen shots)
            cleanUp();
            // start a thread with the download
            final GameDownloader downloader = new GameDownloader(progressBar, this, settings, terasologyDirectory,
                gameVersion);
            downloader.execute();
        }
    }

    /**
     * Clean up the installation directory, that means delete all files and folders except of the files kept by
     * <tt>canBeDeleted</tt> method.
     */
    private void cleanUp() {
        for (final File f : terasologyDirectory.listFiles()) {
            if (canBeDeleted(f)) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
    }

    /**
     * Check if the file can be deleted on clean up action. The only files/directories kept are "SAVED_WORLDS",
     * "screens" and "launcher".
     *
     * @param f the file to check
     *
     * @return true if the file can be deleted
     */
    private boolean canBeDeleted(final File f) {
        final String fileName = f.getName();
        if (fileName.equals(DirectoryUtils.LAUNCHER_DIR_NAME)) {
            return false;
        }
        if (fileName.equals(DirectoryUtils.SAVED_WORLDS_DIR_NAME)) {
            return false;
        }
        if (fileName.equals(DirectoryUtils.SCREENSHOTS_DIR_NAME)) {
            return false;
        }
        if (f.getAbsolutePath().equals(DirectoryUtils.BACKUP_DIR_NAME)) {
            return false;
        }
        if (f.getName().equals(DirectoryUtils.MODS_DIR_NAME)) {
            return false;
        }
        return true;
    }

    /**
     * recursively deletes the directory and all of its content.
     *
     * @param delDirectory directory
     */
    private void deleteDirectory(final File delDirectory) {
        for (final File sub : delDirectory.listFiles()) {
            if (sub.isFile()) {
                sub.delete();
            } else {
                deleteDirectory(sub);
            }
        }
        delDirectory.delete();
    }

    /**
     * Updates the start button with regard to the selected settings, the internet connection and the installed game.
     * Changes the button text and action command ("start" or "download").
     */
    public void updateStartButton() {
        if (GameData.checkInternetConnection()) {
            // get the selected build type
            final BuildType selectedType = settings.getBuildType();
            // get the installed build type
            final BuildType installedType = GameData.getInstalledBuildType(terasologyDirectory);
            if (selectedType == installedType) {
                // check if update is possible
                // therefore, get the installed version no. and the upstream version number
                final int installedVersion = GameData.getInstalledBuildVersion(terasologyDirectory);
                final int upstreamVersion = gameVersion.getVersion(installedType);
                final int selectedVersion;
                if (settings.isBuildVersionLatest(installedType)) {
                    selectedVersion = upstreamVersion;
                } else {
                    selectedVersion = settings.getBuildVersion(installedType);
                }
                if (installedVersion == selectedVersion) {
                    // game can be started
                    startButton.setText(BundleUtils.getLabel("launcher_start"));
                    startButton.setActionCommand(START_ACTION);
                } else {
                    // differentiate between up- and downgrade
                    if (installedVersion < selectedVersion) {
                        startButton.setText(BundleUtils.getLabel("launcher_update"));
                        startButton.setActionCommand(DOWNLOAD_ACTION);
                    } else {
                        startButton.setText(BundleUtils.getLabel("launcher_downgrade"));
                        startButton.setActionCommand(DOWNLOAD_ACTION);
                    }
                }
            } else {
                // download other build type
                startButton.setText(BundleUtils.getLabel("launcher_download"));
                startButton.setActionCommand(DOWNLOAD_ACTION);
            }
        } else {
            if (GameData.isGameInstalled(terasologyDirectory)) {
                // installed game can be started
                startButton.setText(BundleUtils.getLabel("launcher_start"));
                startButton.setActionCommand(START_ACTION);
            } else {
                // no game installed, and no way to download it...
                startButton.setEnabled(false);
            }
        }
    }
}
