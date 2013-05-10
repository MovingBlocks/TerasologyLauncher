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

import org.terasologylauncher.Settings;
import org.terasologylauncher.launcher.TerasologyStarter;
import org.terasologylauncher.updater.GameDownloader;
import org.terasologylauncher.util.BundleUtils;
import org.terasologylauncher.util.DirectoryUtils;
import org.terasologylauncher.util.OperatingSystem;
import org.terasologylauncher.version.TerasologyGameVersion;
import org.terasologylauncher.version.TerasologyGameVersions;
import org.terasologylauncher.version.TerasologyLauncherVersionInfo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.Color;
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
public final class LauncherFrame extends JFrame implements ActionListener {

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

    private JTextPane infoTextPane;
    private LinkJLabel logo;
    private JLabel version;
    private LinkJLabel forums;
    private LinkJLabel issues;
    private LinkJLabel mods;

    private JProgressBar progressBar;

    private LinkJButton github;
    private LinkJButton twitter;
    private LinkJButton facebook;
    private LinkJButton gplus;
    private LinkJButton youtube;
    private LinkJButton reddit;

    private SettingsMenu settingsMenu;

    private final File terasologyDirectory;
    private final OperatingSystem os;
    private final Settings settings;
    private final TerasologyGameVersions gameVersions;

    public LauncherFrame(final File terasologyDirectory, final OperatingSystem os, final Settings settings,
                         final TerasologyGameVersions gameVersions) {
        this.terasologyDirectory = terasologyDirectory;
        this.os = os;
        this.settings = settings;
        this.gameVersions = gameVersions;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        updateGui();

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
        final TransparentPanel topPanel = new TransparentPanel(0.5f);
        topPanel.setBounds(0, 0, FRAME_WIDTH, 96);

        final TransparentPanel updatePanel = new TransparentPanel(0.5f);
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
        infoTextPane.setContentType("text/html");
        infoTextPane.setForeground(Color.WHITE);
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
        logo = new LinkJLabel();
        logo.setBounds(8, 0, 400, 96);

        // Launcher version info label
        version = new JLabel(TerasologyLauncherVersionInfo.getInstance().getDisplayVersion());
        version.setFont(version.getFont().deriveFont(12f));
        version.setForeground(Color.WHITE);
        version.setBounds(FRAME_WIDTH - 64 - 16 - xShift, 0, 32, 32);
        version.setText(TerasologyLauncherVersionInfo.getInstance().getDisplayVersion());

        // Forums link
        forums = new LinkJLabel();
        forums.setFont(forums.getFont().deriveFont(24f));
        forums.setBounds(480, 36, 96, 32);

        // Issues link
        issues = new LinkJLabel();
        issues.setFont(issues.getFont().deriveFont(24f));
        issues.setBounds(640, 36, 96, 32);

        // Mods
        mods = new LinkJLabel();
        mods.setFont(mods.getFont().deriveFont(24f));
        mods.setBounds(FRAME_WIDTH - 96 - 16 - xShift, 36, 96, 32);

        // Progress Bar
        progressBar = new JProgressBar();
        progressBar.setBounds((FRAME_WIDTH / 2) - 200, (FRAME_HEIGHT - 70) + yShift, 400, 23);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);

        // Social media
        github = new LinkJButton();
        github.setBounds(8 + xShift, (FRAME_HEIGHT - 70) + yShift, 32, 32);
        github.setBorder(null);

        twitter = new LinkJButton();
        twitter.setBounds(8 + (38 * 4) + xShift, (FRAME_HEIGHT - 70) + yShift, 32, 32);
        twitter.setBorder(null);

        facebook = new LinkJButton();
        facebook.setBounds(8 + (38 * 3) + xShift, (FRAME_HEIGHT - 70) + yShift, 32, 32);
        facebook.setBorder(null);

        gplus = new LinkJButton();
        gplus.setBounds(8 + (38 * 2) + xShift, (FRAME_HEIGHT - 70) + yShift, 32, 32);
        gplus.setBorder(null);

        youtube = new LinkJButton();
        youtube.setBounds(8 + 38 + xShift, (FRAME_HEIGHT - 70) + yShift, 32, 32);
        youtube.setBorder(null);

        reddit = new LinkJButton();
        reddit.setBounds(8 + (38 * 5) + xShift, (FRAME_HEIGHT - 70) + yShift, 32, 32);
        reddit.setBorder(null);

        final Container contentPane = getContentPane();
        contentPane.setLayout(null);

        contentPane.add(logo);
        contentPane.add(forums);
        contentPane.add(issues);
        contentPane.add(mods);

        contentPane.add(version);

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


    private TerasologyGameVersion getSelectedGameVersion() {
        return gameVersions.getGameVersionForBuildVersion(settings.getBuildType(),
            settings.getBuildVersion(settings.getBuildType()));
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() instanceof JComponent) {
            action(e.getActionCommand());
        }
    }

    private void action(final String command) {
        if (command.equals(SETTINGS_ACTION)) {
            if ((settingsMenu == null) || !settingsMenu.isVisible()) {
                settingsMenu = new SettingsMenu(this, terasologyDirectory, settings, gameVersions);
                settingsMenu.setVisible(true);
                settingsMenu.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(final WindowEvent e) {
                        updateGui();
                    }
                });
            }
        } else if (command.equals(CANCEL_ACTION)) {
            dispose();
            System.exit(0);
        } else if (command.equals(START_ACTION)) {
            final TerasologyGameVersion gameVersion = getSelectedGameVersion();
            final TerasologyStarter terasologyStarter = new TerasologyStarter(gameVersion, os,
                settings.getMaxHeapSize(), settings.getInitialHeapSize());
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
            final GameDownloader downloader = new GameDownloader(progressBar, this, terasologyDirectory,
                getSelectedGameVersion(), gameVersions);
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

    public void updateGui() {
        updateLocale();
        updateStartButton();
        updateInfoTextPane();
    }

    private void updateLocale() {
        setTitle(BundleUtils.getLabel("launcher_title"));
        setIconImage(BundleUtils.getImage("icon"));

        settingsButton.setText(BundleUtils.getLabel("launcher_settings"));
        settingsButton.setToolTipText(BundleUtils.getLabel("tooltip_settings"));
        cancelButton.setText(BundleUtils.getLabel("launcher_cancel"));
        cancelButton.setToolTipText(BundleUtils.getLabel("tooltip_cancel"));

        logo.setText(BundleUtils.getLabel("launcher_website"));
        logo.setToolTipText(BundleUtils.getLabel("tooltip_website"));
        logo.setIcon(BundleUtils.getImageIcon("logo"));
        logo.setUri(BundleUtils.getURI("terasology_website"));
        forums.setText(BundleUtils.getLabel("launcher_forum"));
        forums.setToolTipText(BundleUtils.getLabel("tooltip_forum"));
        forums.setUri(BundleUtils.getURI("terasology_forum"));
        issues.setText(BundleUtils.getLabel("launcher_issues"));
        issues.setToolTipText(BundleUtils.getLabel("tooltip_github_issues"));
        issues.setUri(BundleUtils.getURI("terasology_github_issues"));
        mods.setText(BundleUtils.getLabel("launcher_mods"));
        mods.setToolTipText(BundleUtils.getLabel("tooltip_mods"));
        mods.setUri(BundleUtils.getURI("terasology_mods"));

        github.setToolTipText(BundleUtils.getLabel("tooltip_github"));
        github.setUri(BundleUtils.getURI("terasology_github"));
        github.setIcon(BundleUtils.getImageIcon("github"));
        github.setRolloverIcon(BundleUtils.getImageIcon("github_hover"));

        twitter.setToolTipText(BundleUtils.getLabel("tooltip_twitter"));
        twitter.setUri(BundleUtils.getURI("terasology_twitter"));
        twitter.setIcon(BundleUtils.getImageIcon("twitter"));
        twitter.setRolloverIcon(BundleUtils.getImageIcon("twitter_hover"));

        facebook.setToolTipText(BundleUtils.getLabel("tooltip_facebook"));
        facebook.setUri(BundleUtils.getURI("terasology_facebook"));
        facebook.setIcon(BundleUtils.getImageIcon("facebook"));
        facebook.setRolloverIcon(BundleUtils.getImageIcon("facebook_hover"));

        gplus.setToolTipText(BundleUtils.getLabel("tooltip_gplus"));
        gplus.setUri(BundleUtils.getURI("terasology_gplus"));
        gplus.setIcon(BundleUtils.getImageIcon("gplus"));
        gplus.setRolloverIcon(BundleUtils.getImageIcon("gplus_hover"));

        youtube.setToolTipText(BundleUtils.getLabel("tooltip_youtube"));
        youtube.setUri(BundleUtils.getURI("terasology_youtube"));
        youtube.setIcon(BundleUtils.getImageIcon("youtube"));
        youtube.setRolloverIcon(BundleUtils.getImageIcon("youtube_hover"));

        reddit.setToolTipText(BundleUtils.getLabel("tooltip_reddit"));
        reddit.setUri(BundleUtils.getURI("terasology_reddit"));
        reddit.setIcon(BundleUtils.getImageIcon("reddit"));
        reddit.setRolloverIcon(BundleUtils.getImageIcon("reddit_hover"));
    }


    private void updateStartButton() {
        final TerasologyGameVersion gameVersion = getSelectedGameVersion();
        if (gameVersion.isInstalled()) {
            // installed game can be started
            startButton.setVisible(true);
            startButton.setEnabled(true);
            startButton.setText(BundleUtils.getLabel("launcher_start"));
            startButton.setToolTipText(BundleUtils.getLabel("tooltip_start"));
            startButton.setActionCommand(START_ACTION);
        } else if (gameVersion.isSuccessful() && (gameVersion.getBuildNumber() != null)) {
            // download is possible
            startButton.setVisible(true);
            startButton.setEnabled(true);
            startButton.setText(BundleUtils.getLabel("launcher_download"));
            startButton.setToolTipText(BundleUtils.getLabel("tooltip_download"));
            startButton.setActionCommand(DOWNLOAD_ACTION);
        } else {
            // no game installed, and no way to download it...
            startButton.setVisible(false);
            startButton.setEnabled(false);
            startButton.setText("");
            startButton.setToolTipText("");
            startButton.setActionCommand(null);
        }
    }

    private void updateInfoTextPane() {
        final TerasologyGameVersion gameVersion = getSelectedGameVersion();
        final StringBuilder b = new StringBuilder();
        // TODO add more information, i18n and tooltip
        if (gameVersion.getBuildType() != null) {
            b.append(gameVersion.getBuildType());
            b.append("<br/>");
        }
        if (gameVersion.getBuildNumber() != null) {
            b.append(gameVersion.getBuildNumber());
            b.append("<br/>");
        }
        if (gameVersion.getChangeLog() != null) {
            for (String msg : gameVersion.getChangeLog()) {
                b.append("-");
                // TODO escape HTML entities/special characters
                b.append(msg);
                b.append("<br/>");
            }
        }
        infoTextPane.setText(b.toString());
        infoTextPane.setCaretPosition(0);
    }

}
