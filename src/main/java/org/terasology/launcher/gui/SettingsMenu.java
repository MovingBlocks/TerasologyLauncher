/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.terasology.launcher.game.GameJob;
import org.terasology.launcher.game.JobItem;
import org.terasology.launcher.game.TerasologyGameVersion;
import org.terasology.launcher.game.TerasologyGameVersions;
import org.terasology.launcher.game.VersionItem;
import org.terasology.launcher.util.BundleUtils;
import org.terasology.launcher.util.DirectoryUtils;
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

final class SettingsMenu extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SettingsMenu.class);

    private static final String SAVE_ACTION = "save";
    private static final String CANCEL_ACTION = "cancel";

    private static final String LAUNCHER_DIRECTORY_OPEN = "launcherDirectoryOpen";
    private static final String DOWNLOAD_DIRECTORY_OPEN = "downloadDirectoryOpen";
    private static final String GAME_DIRECTORY_OPEN = "gameDirectoryOpen";
    private static final String GAME_DIRECTORY_EDIT = "gameDirectoryEdit";
    private static final String GAME_DATA_DIRECTORY_OPEN = "gameDataDirectoryOpen";
    private static final String GAME_DATA_DIRECTORY_EDIT = "gameDataDirectoryEdit";
    private static final String JOB_ACTION = "job";
    private static final String MAX_HEAP_SIZE_ACTION = "maxHeapSize";
    private static final String INITIAL_HEAP_SIZE_ACTION = "initialHeapSize";

    private JComboBox<JobItem> jobBox;
    private JComboBox<VersionItem> buildVersionBox;
    private JComboBox<JavaHeapSize> maxHeapSizeBox;
    private JComboBox<JavaHeapSize> initialHeapSizeBox;
    private JComboBox<String> languageBox;
    private JCheckBox searchForLauncherUpdatesBox;
    private JCheckBox closeLauncherAfterGameStartBox;
    private JCheckBox saveDownloadedFilesBox;

    private File gameDirectory;
    private File gameDataDirectory;
    private final File launcherDirectory;
    private final File downloadDirectory;
    private final LauncherSettings launcherSettings;
    private final TerasologyGameVersions gameVersions;

    public SettingsMenu(JFrame parent, File launcherDirectory, File downloadDirectory, LauncherSettings launcherSettings, TerasologyGameVersions gameVersions) {
        super(parent, BundleUtils.getLabel("settings_title"), true);

        this.launcherDirectory = launcherDirectory;
        this.downloadDirectory = downloadDirectory;
        this.launcherSettings = launcherSettings;
        this.gameVersions = gameVersions;

        setResizable(false);
        setIconImage(BundleUtils.getImage("icon"));

        initComponents();

        populateJob();
        populateHeapSize();
        populateLanguage();
        populateSearchForLauncherUpdates();
        populateCloseLauncherAfterGameStart();
        populateSaveDownloadedFiles();
        gameDirectory = launcherSettings.getGameDirectory();
        gameDataDirectory = launcherSettings.getGameDataDirectory();

        pack();
    }

    private void initComponents() {
        final Font settingsFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

        final JTabbedPane mainSettings = new JTabbedPane();
        mainSettings.addTab(BundleUtils.getLabel("settings_game_title"), createGameSettingsTab(settingsFont));
        mainSettings.addTab(BundleUtils.getLabel("settings_launcher_title"), createLauncherSettingsTab(settingsFont));

        /*================== OK, Cancel ==================*/
        final JButton saveButton = new JButton();
        saveButton.setActionCommand(SAVE_ACTION);
        saveButton.addActionListener(this);
        saveButton.setText(BundleUtils.getLabel("settings_save"));

        final JButton cancelButton = new JButton();
        cancelButton.setActionCommand(CANCEL_ACTION);
        cancelButton.addActionListener(this);
        cancelButton.setText(BundleUtils.getLabel("settings_cancel"));

        final Container contentPane = getContentPane();
        final GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(mainSettings, GroupLayout.Alignment.TRAILING,
                    GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(saveButton, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(cancelButton, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addComponent(mainSettings, GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(saveButton)
                        .addComponent(cancelButton))
                    .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel createGameSettingsTab(Font settingsFont) {
        final JPanel gameSettingsTab = new JPanel();
        gameSettingsTab.setFont(settingsFont);

        final JLabel jobLabel = new JLabel();
        jobLabel.setText(BundleUtils.getLabel("settings_game_job"));
        jobLabel.setFont(settingsFont);

        jobBox = new JComboBox<>();
        jobBox.setFont(settingsFont);
        jobBox.addActionListener(this);
        jobBox.setActionCommand(JOB_ACTION);

        final JLabel buildVersionLabel = new JLabel();
        buildVersionLabel.setText(BundleUtils.getLabel("settings_game_buildVersion"));
        buildVersionLabel.setFont(settingsFont);

        buildVersionBox = new JComboBox<>();
        buildVersionBox.setFont(settingsFont);

        final JPanel gameDirectoryPanel = new JPanel();

        final JLabel gameDirectoryLabel = new JLabel();
        gameDirectoryLabel.setText(BundleUtils.getLabel("settings_game_gameDirectory"));
        gameDirectoryLabel.setFont(settingsFont);

        final JButton gameDirectoryOpenButton = new JButton();
        gameDirectoryOpenButton.setFont(settingsFont);
        gameDirectoryOpenButton.setText(BundleUtils.getLabel("settings_game_gameDirectory_open"));
        gameDirectoryOpenButton.addActionListener(this);
        gameDirectoryOpenButton.setActionCommand(GAME_DIRECTORY_OPEN);
        if (!Desktop.isDesktopSupported()) {
            gameDirectoryOpenButton.setEnabled(false);
        }

        final JButton gameDirectoryEditButton = new JButton();
        gameDirectoryEditButton.setFont(settingsFont);
        gameDirectoryEditButton.setText(BundleUtils.getLabel("settings_game_gameDirectory_edit"));
        gameDirectoryEditButton.addActionListener(this);
        gameDirectoryEditButton.setActionCommand(GAME_DIRECTORY_EDIT);

        gameDirectoryPanel.add(gameDirectoryOpenButton);
        gameDirectoryPanel.add(gameDirectoryEditButton);

        final JPanel gameDataDirectoryPanel = new JPanel();

        final JLabel gameDataDirectoryLabel = new JLabel();
        gameDataDirectoryLabel.setText(BundleUtils.getLabel("settings_game_gameDataDirectory"));
        gameDataDirectoryLabel.setFont(settingsFont);

        final JButton gameDataDirectoryOpenButton = new JButton();
        gameDataDirectoryOpenButton.setFont(settingsFont);
        gameDataDirectoryOpenButton.setText(BundleUtils.getLabel("settings_game_gameDataDirectory_open"));
        gameDataDirectoryOpenButton.addActionListener(this);
        gameDataDirectoryOpenButton.setActionCommand(GAME_DATA_DIRECTORY_OPEN);
        if (!Desktop.isDesktopSupported()) {
            gameDataDirectoryOpenButton.setEnabled(false);
        }

        final JButton gameDataDirectoryEditButton = new JButton();
        gameDataDirectoryEditButton.setFont(settingsFont);
        gameDataDirectoryEditButton.setText(BundleUtils.getLabel("settings_game_gameDataDirectory_edit"));
        gameDataDirectoryEditButton.addActionListener(this);
        gameDataDirectoryEditButton.setActionCommand(GAME_DATA_DIRECTORY_EDIT);

        gameDataDirectoryPanel.add(gameDataDirectoryOpenButton);
        gameDataDirectoryPanel.add(gameDataDirectoryEditButton);

        final JLabel maxHeapSizeLabel = new JLabel();
        maxHeapSizeLabel.setText(BundleUtils.getLabel("settings_game_maxHeapSize"));
        maxHeapSizeLabel.setFont(settingsFont);

        maxHeapSizeBox = new JComboBox<>();
        maxHeapSizeBox.setFont(settingsFont);
        maxHeapSizeBox.addActionListener(this);
        maxHeapSizeBox.setActionCommand(MAX_HEAP_SIZE_ACTION);

        final JLabel initialHeapSizeLabel = new JLabel();
        initialHeapSizeLabel.setText(BundleUtils.getLabel("settings_game_initialHeapSize"));
        initialHeapSizeLabel.setFont(settingsFont);

        initialHeapSizeBox = new JComboBox<>();
        initialHeapSizeBox.setFont(settingsFont);
        initialHeapSizeBox.addActionListener(this);
        initialHeapSizeBox.setActionCommand(INITIAL_HEAP_SIZE_ACTION);

        final GroupLayout gameTabLayout = new GroupLayout(gameSettingsTab);
        gameSettingsTab.setLayout(gameTabLayout);

        gameTabLayout.setHorizontalGroup(
            gameTabLayout.createParallelGroup()
                .addGroup(gameTabLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gameTabLayout.createParallelGroup()
                        .addComponent(jobLabel)
                        .addComponent(buildVersionLabel)
                        .addComponent(gameDirectoryLabel)
                        .addComponent(gameDataDirectoryLabel)
                        .addComponent(maxHeapSizeLabel)
                        .addComponent(initialHeapSizeLabel))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup()
                        .addComponent(jobBox)
                        .addComponent(buildVersionBox)
                        .addComponent(gameDirectoryPanel)
                        .addComponent(gameDataDirectoryPanel)
                        .addComponent(maxHeapSizeBox)
                        .addComponent(initialHeapSizeBox))
                    .addContainerGap())
        );

        gameTabLayout.setVerticalGroup(
            gameTabLayout.createParallelGroup()
                .addGroup(gameTabLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jobLabel)
                        .addComponent(jobBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(buildVersionLabel)
                        .addComponent(buildVersionBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(gameDirectoryLabel)
                        .addComponent(gameDirectoryPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(gameDataDirectoryLabel)
                        .addComponent(gameDataDirectoryPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(maxHeapSizeLabel)
                        .addComponent(maxHeapSizeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(initialHeapSizeLabel)
                        .addComponent(initialHeapSizeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
        );
        return gameSettingsTab;
    }

    private JPanel createLauncherSettingsTab(Font settingsFont) {
        final JPanel launcherSettingsTab = new JPanel();
        launcherSettingsTab.setFont(settingsFont);

        final JLabel languageLabel = new JLabel();
        languageLabel.setText(BundleUtils.getLabel("settings_launcher_chooseLanguage"));
        languageLabel.setFont(settingsFont);

        languageBox = new JComboBox<>();
        languageBox.setFont(settingsFont);

        final JLabel searchForLauncherUpdatesLabel = new JLabel();
        searchForLauncherUpdatesLabel.setText(BundleUtils.getLabel("settings_launcher_searchForLauncherUpdates"));
        searchForLauncherUpdatesLabel.setFont(settingsFont);

        searchForLauncherUpdatesBox = new JCheckBox();
        searchForLauncherUpdatesBox.setFont(settingsFont);

        final JLabel closeLauncherAfterGameStartLabel = new JLabel();
        closeLauncherAfterGameStartLabel.setText(BundleUtils.getLabel("settings_launcher_closeLauncherAfterGameStart"));
        closeLauncherAfterGameStartLabel.setFont(settingsFont);

        closeLauncherAfterGameStartBox = new JCheckBox();
        closeLauncherAfterGameStartBox.setFont(settingsFont);

        final JLabel saveDownloadedFilesLabel = new JLabel();
        saveDownloadedFilesLabel.setText(BundleUtils.getLabel("settings_launcher_saveDownloadedFiles"));
        saveDownloadedFilesLabel.setFont(settingsFont);

        saveDownloadedFilesBox = new JCheckBox();
        saveDownloadedFilesBox.setFont(settingsFont);

        final JPanel launcherDirectoryPanel = new JPanel();

        final JLabel launcherDirectoryLabel = new JLabel();
        launcherDirectoryLabel.setText(BundleUtils.getLabel("settings_launcher_launcherDirectory"));
        launcherDirectoryLabel.setFont(settingsFont);

        final JButton launcherDirectoryOpenButton = new JButton();
        launcherDirectoryOpenButton.setFont(settingsFont);
        launcherDirectoryOpenButton.setText(BundleUtils.getLabel("settings_launcher_launcherDirectory_open"));
        launcherDirectoryOpenButton.addActionListener(this);
        launcherDirectoryOpenButton.setActionCommand(LAUNCHER_DIRECTORY_OPEN);
        if (!Desktop.isDesktopSupported()) {
            launcherDirectoryOpenButton.setEnabled(false);
        }
        launcherDirectoryPanel.add(launcherDirectoryOpenButton);

        final JPanel downloadDirectoryPanel = new JPanel();

        final JLabel downloadDirectoryLabel = new JLabel();
        downloadDirectoryLabel.setText(BundleUtils.getLabel("settings_launcher_downloadDirectory"));
        downloadDirectoryLabel.setFont(settingsFont);

        final JButton downloadDirectoryOpenButton = new JButton();
        downloadDirectoryOpenButton.setFont(settingsFont);
        downloadDirectoryOpenButton.setText(BundleUtils.getLabel("settings_launcher_downloadDirectory_open"));
        downloadDirectoryOpenButton.addActionListener(this);
        downloadDirectoryOpenButton.setActionCommand(DOWNLOAD_DIRECTORY_OPEN);
        if (!Desktop.isDesktopSupported()) {
            downloadDirectoryOpenButton.setEnabled(false);
        }
        downloadDirectoryPanel.add(downloadDirectoryOpenButton);

        final GroupLayout launcherTabLayout = new GroupLayout(launcherSettingsTab);
        launcherSettingsTab.setLayout(launcherTabLayout);

        launcherTabLayout.setHorizontalGroup(
            launcherTabLayout.createParallelGroup()
                .addGroup(launcherTabLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(launcherTabLayout.createParallelGroup()
                        .addComponent(languageLabel)
                        .addComponent(searchForLauncherUpdatesLabel)
                        .addComponent(closeLauncherAfterGameStartLabel)
                        .addComponent(launcherDirectoryLabel)
                        .addComponent(saveDownloadedFilesLabel)
                        .addComponent(downloadDirectoryLabel))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(launcherTabLayout.createParallelGroup()
                        .addComponent(languageBox)
                        .addComponent(searchForLauncherUpdatesBox)
                        .addComponent(closeLauncherAfterGameStartBox)
                        .addComponent(launcherDirectoryPanel)
                        .addComponent(saveDownloadedFilesBox)
                        .addComponent(downloadDirectoryPanel))
                    .addContainerGap())
        );

        launcherTabLayout.setVerticalGroup(
            launcherTabLayout.createParallelGroup()
                .addGroup(launcherTabLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(launcherTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(languageLabel)
                        .addComponent(languageBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(launcherTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(searchForLauncherUpdatesLabel)
                        .addComponent(searchForLauncherUpdatesBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(launcherTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(closeLauncherAfterGameStartLabel)
                        .addComponent(closeLauncherAfterGameStartBox, GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(launcherTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(launcherDirectoryLabel)
                        .addComponent(launcherDirectoryPanel, GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(launcherTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(saveDownloadedFilesLabel)
                        .addComponent(saveDownloadedFilesBox, GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(launcherTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(downloadDirectoryLabel)
                        .addComponent(downloadDirectoryPanel, GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
        );

        return launcherSettingsTab;
    }

    private void populateJob() {
        for (GameJob job : GameJob.values()) {
            if (job.isOnlyInstalled() && (launcherSettings.getJob() != job)) {
                boolean foundInstalled = false;
                final List<TerasologyGameVersion> gameVersionList = gameVersions.getGameVersionList(job);
                for (TerasologyGameVersion gameVersion : gameVersionList) {
                    if (gameVersion.isInstalled()) {
                        foundInstalled = true;
                        break;
                    }
                }
                if (!foundInstalled) {
                    continue;
                }
            }

            final JobItem jobItem = new JobItem(job);
            jobBox.addItem(jobItem);
            if (launcherSettings.getJob() == job) {
                jobBox.setSelectedItem(jobItem);
            }
        }
        updateBuildVersionBox();
    }

    private void updateBuildVersionBox() {
        buildVersionBox.removeAllItems();
        final JobItem jobItem = (JobItem) jobBox.getSelectedItem();
        final int buildVersion = launcherSettings.getBuildVersion(jobItem.getJob());

        for (TerasologyGameVersion version : gameVersions.getGameVersionList(jobItem.getJob())) {
            final VersionItem versionItem = new VersionItem(version);
            buildVersionBox.addItem(versionItem);
            if (versionItem.getVersion() == buildVersion) {
                buildVersionBox.setSelectedItem(versionItem);
            }
        }
    }

    private void populateHeapSize() {
        for (JavaHeapSize heapSize : JavaHeapSize.values()) {
            maxHeapSizeBox.addItem(heapSize);
            initialHeapSizeBox.addItem(heapSize);
        }
        updateHeapSizeSelection();
    }

    private void updateHeapSizeSelection() {
        maxHeapSizeBox.setSelectedItem(launcherSettings.getMaxHeapSize());
        initialHeapSizeBox.setSelectedItem(launcherSettings.getInitialHeapSize());
    }

    private void updateInitialHeapSizeBox() {
        final JavaHeapSize initialHeapSize = (JavaHeapSize) initialHeapSizeBox.getSelectedItem();
        final JavaHeapSize maxHeapSize = (JavaHeapSize) maxHeapSizeBox.getSelectedItem();

        if ((initialHeapSize != null) && (maxHeapSize != null) && (maxHeapSize.compareTo(initialHeapSize) < 0)) {
            initialHeapSizeBox.setSelectedItem(maxHeapSize);
        }
    }

    private void updateMaxHeapSizeBox() {
        final JavaHeapSize initialHeapSize = (JavaHeapSize) initialHeapSizeBox.getSelectedItem();
        final JavaHeapSize maxHeapSize = (JavaHeapSize) maxHeapSizeBox.getSelectedItem();

        if ((initialHeapSize != null) && (maxHeapSize != null) && (maxHeapSize.compareTo(initialHeapSize) < 0)) {
            maxHeapSizeBox.setSelectedItem(initialHeapSize);
        }
    }

    private void populateLanguage() {
        languageBox.removeAllItems();
        for (Locale locale : Languages.SUPPORTED_LOCALES) {
            String item = locale.toLanguageTag() + " : " + BundleUtils.getLabel(locale, Languages.SETTINGS_LABEL_KEYS.get(locale));
            if (!locale.equals(Languages.getCurrentLocale())) {
                item += " (" + BundleUtils.getLabel(Languages.SETTINGS_LABEL_KEYS.get(locale)) + ")";
            }
            languageBox.addItem(item);

            if (Languages.getCurrentLocale().equals(locale)) {
                languageBox.setSelectedItem(item);
            }
        }
    }

    private void populateSearchForLauncherUpdates() {
        searchForLauncherUpdatesBox.setSelected(launcherSettings.isSearchForLauncherUpdates());
    }

    private void populateCloseLauncherAfterGameStart() {
        closeLauncherAfterGameStartBox.setSelected(launcherSettings.isCloseLauncherAfterGameStart());
    }

    private void populateSaveDownloadedFiles() {
        saveDownloadedFilesBox.setSelected(launcherSettings.isSaveDownloadedFiles());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComponent) {
            actionPerformed(e.getActionCommand());
        }
    }

    private void actionPerformed(String actionCommand) {
        switch (actionCommand) {
            case LAUNCHER_DIRECTORY_OPEN:
                try {
                    DirectoryUtils.checkDirectory(launcherDirectory);
                    Desktop.getDesktop().open(launcherDirectory);
                } catch (IOException e) {
                    logger.error("The launcher directory can not be opened! '{}'", launcherDirectory, e);
                    JOptionPane.showMessageDialog(this,
                        BundleUtils.getLabel("message_error_launcherDirectory") + "\n" + launcherDirectory,
                        BundleUtils.getLabel("message_error_title"),
                        JOptionPane.ERROR_MESSAGE);
                }
                break;
            case DOWNLOAD_DIRECTORY_OPEN:
                try {
                    DirectoryUtils.checkDirectory(downloadDirectory);
                    Desktop.getDesktop().open(downloadDirectory);
                } catch (IOException e) {
                    logger.error("The download directory can not be opened! '{}'", downloadDirectory, e);
                    JOptionPane.showMessageDialog(this,
                        BundleUtils.getLabel("message_error_downloadDirectory") + "\n" + downloadDirectory,
                        BundleUtils.getLabel("message_error_title"),
                        JOptionPane.ERROR_MESSAGE);
                }
                break;
            case GAME_DIRECTORY_OPEN:
                try {
                    DirectoryUtils.checkDirectory(gameDirectory);
                    Desktop.getDesktop().open(gameDirectory);
                } catch (IOException e) {
                    logger.error("The game directory can not be opened! '{}'", gameDirectory, e);
                    JOptionPane.showMessageDialog(this,
                        BundleUtils.getLabel("message_error_gameDirectory") + "\n" + gameDirectory,
                        BundleUtils.getLabel("message_error_title"),
                        JOptionPane.ERROR_MESSAGE);
                }
                break;
            case GAME_DIRECTORY_EDIT:
                final JFileChooser fileChooserGameDir = new JFileChooser(gameDirectory);
                fileChooserGameDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooserGameDir.setDialogTitle(BundleUtils.getLabel("settings_game_gameDirectory_edit_title"));
                if (fileChooserGameDir.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        final File selectedFile = fileChooserGameDir.getSelectedFile();
                        DirectoryUtils.checkDirectory(selectedFile);
                        gameDirectory = selectedFile;
                    } catch (IOException e) {
                        logger.error("The game directory can not be created or used! '{}'", gameDirectory, e);
                        JOptionPane.showMessageDialog(this,
                            BundleUtils.getLabel("message_error_gameDirectory") + "\n" + gameDirectory,
                            BundleUtils.getLabel("message_error_title"),
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
            case GAME_DATA_DIRECTORY_OPEN:
                try {
                    DirectoryUtils.checkDirectory(gameDataDirectory);
                    Desktop.getDesktop().open(gameDataDirectory);
                } catch (IOException e) {
                    logger.error("The game data directory can not be opened! '{}'", gameDataDirectory, e);
                    JOptionPane.showMessageDialog(this,
                        BundleUtils.getLabel("message_error_gameDataDirectory") + "\n" + gameDataDirectory,
                        BundleUtils.getLabel("message_error_title"),
                        JOptionPane.ERROR_MESSAGE);
                }
                break;
            case GAME_DATA_DIRECTORY_EDIT:
                final JFileChooser fileChooserGameDataDir = new JFileChooser(gameDataDirectory);
                fileChooserGameDataDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooserGameDataDir.setDialogTitle(BundleUtils.getLabel("settings_game_gameDataDirectory_edit_title"));
                if (fileChooserGameDataDir.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        final File selectedFile = fileChooserGameDataDir.getSelectedFile();
                        DirectoryUtils.checkDirectory(selectedFile);
                        gameDataDirectory = selectedFile;
                    } catch (IOException e) {
                        logger.error("The game data directory can not be created or used! '{}'", gameDataDirectory, e);
                        JOptionPane.showMessageDialog(this,
                            BundleUtils.getLabel("message_error_gameDataDirectory") + "\n" + gameDataDirectory,
                            BundleUtils.getLabel("message_error_title"),
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
            case JOB_ACTION:
                updateBuildVersionBox();
                break;
            case MAX_HEAP_SIZE_ACTION:
                updateInitialHeapSizeBox();
                break;
            case INITIAL_HEAP_SIZE_ACTION:
                updateMaxHeapSizeBox();
                break;
            case CANCEL_ACTION:
                dispose();
                setVisible(false);
                setAlwaysOnTop(false);
                break;
            case SAVE_ACTION:
                // save job
                final JobItem jobItem = (JobItem) jobBox.getSelectedItem();
                launcherSettings.setJob(jobItem.getJob());

                // save build version
                final VersionItem versionItem = (VersionItem) buildVersionBox.getSelectedItem();
                launcherSettings.setBuildVersion(versionItem.getVersion(), jobItem.getJob());

                // save gameDirectory
                launcherSettings.setGameDirectory(gameDirectory);

                // save gameDataDirectory
                launcherSettings.setGameDataDirectory(gameDataDirectory);

                // save heap size settings
                launcherSettings.setMaxHeapSize((JavaHeapSize) maxHeapSizeBox.getSelectedItem());
                launcherSettings.setInitialHeapSize((JavaHeapSize) initialHeapSizeBox.getSelectedItem());

                // save languageBox settings
                Languages.update(Languages.SUPPORTED_LOCALES.get(languageBox.getSelectedIndex()));
                launcherSettings.setLocale(Languages.getCurrentLocale());

                // save searchForLauncherUpdates
                launcherSettings.setSearchForLauncherUpdates(searchForLauncherUpdatesBox.isSelected());

                // save closeLauncherAfterGameStart
                launcherSettings.setCloseLauncherAfterGameStart(closeLauncherAfterGameStartBox.isSelected());

                // save saveDownloadedFiles
                launcherSettings.setSaveDownloadedFiles(saveDownloadedFilesBox.isSelected());

                // store changed settings
                try {
                    launcherSettings.store();
                } catch (IOException e) {
                    logger.error("The launcher settings can not be stored! '{}'", launcherSettings.getLauncherSettingsFilePath(), e);
                    JOptionPane.showMessageDialog(this,
                        BundleUtils.getLabel("message_error_storeSettings"),
                        BundleUtils.getLabel("message_error_title"),
                        JOptionPane.ERROR_MESSAGE);
                }
                dispose();
                setVisible(false);
                setAlwaysOnTop(false);
                break;
            default:
                logger.warn("Unhandled action command '{}'!", actionCommand);
        }
    }
}
