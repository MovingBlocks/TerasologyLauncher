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

package org.terasology.launcher.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.LauncherSettings;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.FileUtils;
import org.terasology.launcher.util.GameStarter;
import org.terasology.launcher.version.GameBuildType;
import org.terasology.launcher.version.TerasologyGameVersion;
import org.terasology.launcher.version.TerasologyGameVersions;
import org.terasology.launcher.version.TerasologyLauncherVersionInfo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
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
import java.net.MalformedURLException;

public final class LauncherFrame extends JFrame implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(LauncherFrame.class);

    private static final long serialVersionUID = 1L;

    private static final int FRAME_WIDTH = 880;
    private static final int FRAME_HEIGHT = 520;

    private static final int INFO_PANEL_WIDTH = 600;
    private static final int INFO_PANEL_HEIGHT = 300;

    private static final String DOWNLOAD_ACTION = "download";
    private static final String START_ACTION = "start";
    private static final String DELETE_ACTION = "delete";
    private static final String SETTINGS_ACTION = "settings";
    private static final String EXIT_ACTION = "exit";

    private JButton downloadButton;
    private JButton startButton;
    private JButton deleteButton;
    private JButton settingsButton;
    private JButton exitButton;

    private JTextPane infoTextPane;
    private LinkJLabel logo;
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
    private final GameStarter gameStarter;

    private final File launcherDirectory;
    private final File downloadDirectory;
    private final LauncherSettings launcherSettings;
    private final TerasologyGameVersions gameVersions;

    public LauncherFrame(final File launcherDirectory, final File downloadDirectory,
                         final LauncherSettings launcherSettings, final TerasologyGameVersions gameVersions) {
        this.launcherDirectory = launcherDirectory;
        this.downloadDirectory = downloadDirectory;
        this.launcherSettings = launcherSettings;
        this.gameVersions = gameVersions;

        gameStarter = new GameStarter();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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

        // Download button
        downloadButton = new TSButton(BundleUtils.getLabel("launcher_download"));
        downloadButton.setBounds(FRAME_WIDTH - 96 - 16 - xShift, (FRAME_HEIGHT - 70 - (4 * 40)) + yShift, 96, 32);
        downloadButton.setActionCommand(DOWNLOAD_ACTION);
        downloadButton.addActionListener(this);

        // Start button
        startButton = new TSButton(BundleUtils.getLabel("launcher_start"));
        startButton.setBounds(FRAME_WIDTH - 96 - 16 - xShift, (FRAME_HEIGHT - 70 - (3 * 40)) + yShift, 96, 32);
        startButton.setActionCommand(START_ACTION);
        startButton.addActionListener(this);

        // Delete button
        deleteButton = new TSButton(BundleUtils.getLabel("launcher_delete"));
        deleteButton.setBounds(FRAME_WIDTH - 96 - 16 - xShift, (FRAME_HEIGHT - 70 - (2 * 40)) + yShift, 96, 32);
        deleteButton.setActionCommand(DELETE_ACTION);
        deleteButton.addActionListener(this);

        // Settings Button
        settingsButton = new TSButton(BundleUtils.getLabel("launcher_settings"));
        settingsButton.setBounds(FRAME_WIDTH - 96 - 16 - xShift, (FRAME_HEIGHT - 70 - (1 * 40)) + yShift, 96, 32);
        settingsButton.setActionCommand(SETTINGS_ACTION);
        settingsButton.addActionListener(this);

        // Exit button
        exitButton = new TSButton(BundleUtils.getLabel("launcher_exit"));
        exitButton.setBounds(FRAME_WIDTH - 96 - 16 - xShift, (FRAME_HEIGHT - 70 - (0 * 40)) + yShift, 96, 32);
        exitButton.setActionCommand(EXIT_ACTION);
        exitButton.addActionListener(this);

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
        final JLabel version = new JLabel(TerasologyLauncherVersionInfo.getInstance().getDisplayVersion());
        version.setFont(version.getFont().deriveFont(12f));
        version.setForeground(Color.WHITE);
        version.setBounds(FRAME_WIDTH - 400 - 16 - xShift, 0, 400, 32);
        version.setHorizontalAlignment(JLabel.RIGHT);

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

        contentPane.add(downloadButton);
        contentPane.add(startButton);
        contentPane.add(deleteButton);
        contentPane.add(settingsButton);
        contentPane.add(exitButton);

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
        return gameVersions.getGameVersionForBuildVersion(launcherSettings.getBuildType(),
            launcherSettings.getBuildVersion(launcherSettings.getBuildType()));
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
                settingsMenu = new SettingsMenu(this, launcherDirectory, launcherSettings, gameVersions);
                settingsMenu.setVisible(true);
                settingsMenu.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(final WindowEvent e) {
                        updateGui();
                    }
                });
            }
        } else if (command.equals(EXIT_ACTION)) {
            dispose();
            System.exit(0);
        } else if (command.equals(START_ACTION)) {
            final TerasologyGameVersion gameVersion = getSelectedGameVersion();
            if ((gameVersion == null) || !gameVersion.isInstalled()) {
                logger.warn("The selected game version can not be started! '{}'", gameVersion);
                JOptionPane.showMessageDialog(this, BundleUtils.getLabel("message_error_gameStart"),
                    BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
            } else if (gameStarter.isRunning()) {
                logger.debug("The game can not be started because another game is already running!");
                JOptionPane.showMessageDialog(this, BundleUtils.getLabel("message_information_gameRunning"),
                    BundleUtils.getLabel("message_information_title"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                final boolean gameStarted = gameStarter.startGame(gameVersion, launcherSettings.getMaxHeapSize(),
                    launcherSettings.getInitialHeapSize());
                if (!gameStarted) {
                    JOptionPane.showMessageDialog(this, BundleUtils.getLabel("message_error_gameStart"),
                        BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
                } else if (launcherSettings.isCloseLauncherAfterGameStart()) {
                    logger.info("Close launcher after game start.");
                    System.exit(0);
                }
            }
        } else if (command.equals(DOWNLOAD_ACTION)) {
            try {
                // start a thread with the download
                // TODO Check, if old GameDownloader is running
                final GameDownloader downloader = new GameDownloader(progressBar, this, downloadDirectory,
                    launcherSettings.getGamesDirectory(), getSelectedGameVersion(), gameVersions);
                downloader.execute();
                startButton.setEnabled(false);
            } catch (MalformedURLException e) {
                logger.error("The game could not be downloaded!", e);
            }
        } else if (command.equals(DELETE_ACTION)) {
            final TerasologyGameVersion gameVersion = getSelectedGameVersion();
            if ((gameVersion != null) && gameVersion.isInstalled()) {
                final boolean containsGameData = DirectoryUtils.containsGameData(gameVersion.getInstallationPath());
                final String msg;
                if (containsGameData) {
                    msg = BundleUtils.getMessage("confirmDeleteGame_withData", gameVersion.getInstallationPath());
                } else {
                    msg = BundleUtils.getMessage("confirmDeleteGame_withoutData", gameVersion.getInstallationPath());
                }
                final int option = JOptionPane.showConfirmDialog(this, msg,
                    BundleUtils.getLabel("message_deleteGame_title"),
                    JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    logger.info("Delete installed game! '{}' '{}'", gameVersion, gameVersion.getInstallationPath());
                    FileUtils.delete(gameVersion.getInstallationPath());
                    gameVersions.removeInstallationInfo(gameVersion);
                    updateGui();
                }
            } else {
                logger.warn("The selected game version can not be deleted! '{}'", gameVersion);
            }
        }
    }

    public void updateGui() {
        updateLocale();
        updateButtons();
        updateInfoTextPane();
    }

    private void updateLocale() {
        setTitle(BundleUtils.getLabel("launcher_title"));
        setIconImage(BundleUtils.getImage("icon"));

        downloadButton.setText(BundleUtils.getLabel("launcher_download"));
        downloadButton.setToolTipText(BundleUtils.getLabel("tooltip_download"));
        startButton.setText(BundleUtils.getLabel("launcher_start"));
        startButton.setToolTipText(BundleUtils.getLabel("tooltip_start"));
        deleteButton.setText(BundleUtils.getLabel("launcher_delete"));
        deleteButton.setToolTipText(BundleUtils.getLabel("tooltip_delete"));
        settingsButton.setText(BundleUtils.getLabel("launcher_settings"));
        settingsButton.setToolTipText(BundleUtils.getLabel("tooltip_settings"));
        exitButton.setText(BundleUtils.getLabel("launcher_exit"));
        exitButton.setToolTipText(BundleUtils.getLabel("tooltip_exit"));

        logo.setToolTipText(BundleUtils.getLabel("tooltip_website"));
        logo.setIcon(BundleUtils.getImageIcon("logo"));
        logo.setUri(BundleUtils.getURI("terasology_website"));
        forums.setText(BundleUtils.getLabel("launcher_forum"));
        forums.setToolTipText(BundleUtils.getLabel("tooltip_forum"));
        forums.setUri(BundleUtils.getURI("terasology_forum"));
        issues.setText(BundleUtils.getLabel("launcher_issues"));
        issues.setToolTipText(BundleUtils.getLabel("tooltip_githubIssues"));
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


    private void updateButtons() {
        final TerasologyGameVersion gameVersion = getSelectedGameVersion();
        if (gameVersion.isInstalled()) {
            downloadButton.setEnabled(false);
            startButton.setEnabled(true);
            deleteButton.setEnabled(true);
        } else if ((gameVersion.getSuccessful() != null) && gameVersion.getSuccessful()
            && (gameVersion.getBuildNumber() != null)) {
            downloadButton.setEnabled(true);
            startButton.setEnabled(false);
            deleteButton.setEnabled(false);
        } else {
            // no game installed, and no way to download it...
            downloadButton.setEnabled(false);
            startButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#x27;").replace("/", "&#x2F;");
    }

    private void updateInfoTextPane() {
        final TerasologyGameVersion gameVersion = getSelectedGameVersion();
        final StringBuilder b = new StringBuilder();
        final String infoHeader1;
        final String infoHeader2;
        final Object[] arguments = new Object[7];
        arguments[0] = gameVersion.getBuildType();
        arguments[1] = gameVersion.getBuildNumber();
        if (gameVersion.isLatest()) {
            arguments[2] = 1;
        } else {
            arguments[2] = 0;
        }
        if (gameVersion.isInstalled()) {
            arguments[3] = 1;
        } else {
            arguments[3] = 0;
        }
        if ((gameVersion.getSuccessful() != null) && gameVersion.getSuccessful()) {
            arguments[4] = 1;
        } else {
            arguments[4] = 0;
        }
        if ((gameVersion.getGameVersionInfo() != null)
            && (gameVersion.getGameVersionInfo().getDisplayVersion() != null)) {
            arguments[5] = gameVersion.getGameVersionInfo().getDisplayVersion();
        } else {
            arguments[5] = "";
        }
        if ((gameVersion.getGameVersionInfo() != null)
            && (gameVersion.getGameVersionInfo().getDateTime() != null)) {
            arguments[6] = gameVersion.getGameVersionInfo().getDateTime();
        } else {
            arguments[6] = "";
        }
        if (gameVersion.getBuildType() == GameBuildType.STABLE) {
            infoHeader1 = BundleUtils.getMessage("infoHeader1_stable", arguments);
            infoHeader2 = BundleUtils.getMessage("infoHeader2_stable", arguments);
        } else {
            infoHeader1 = BundleUtils.getMessage("infoHeader1_nightly", arguments);
            infoHeader2 = BundleUtils.getMessage("infoHeader2_nightly", arguments);
        }
        if ((infoHeader1 != null) && (infoHeader1.length() > 0)) {
            b.append("<h1>");
            b.append(escapeHtml(infoHeader1));
            b.append("</h1>\n");
        }
        if ((infoHeader2 != null) && (infoHeader2.length() > 0)) {
            b.append("<h2>");
            b.append(escapeHtml(infoHeader2));
            b.append("</h2>\n");
        }
        if ((gameVersion.getChangeLog() != null) && !gameVersion.getChangeLog().isEmpty()) {
            b.append("<ul>\n");
            for (String msg : gameVersion.getChangeLog()) {
                b.append("<li>");
                b.append(escapeHtml(msg));
                b.append("</li>\n");
            }
            b.append("</ul>\n");
        }
        infoTextPane.setText(b.toString());
        infoTextPane.setCaretPosition(0);
    }
}
