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
import org.terasology.launcher.util.JavaHeapSize;
import org.terasology.launcher.util.Languages;
import org.terasology.launcher.version.GameJob;
import org.terasology.launcher.version.TerasologyGameVersion;
import org.terasology.launcher.version.TerasologyGameVersions;

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
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import java.util.Locale;

final class SettingsMenu extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SettingsMenu.class);

    private static final String SAVE_ACTION = "save";
    private static final String CANCEL_ACTION = "cancel";

    private static final String LAUNCHER_DIRECTORY_OPEN = "launcherDirectoryOpen";
    private static final String GAMES_DIRECTORY_OPEN = "gamesDirectoryOpen";
    private static final String GAMES_DIRECTORY_EDIT = "gamesDirectoryEdit";
    private static final String JOB_ACTION = "job";
    private static final String MAX_HEAP_SIZE_ACTION = "maxHeapSize";
    private static final String INITIAL_HEAP_SIZE_ACTION = "initialHeapSize";

    private JComboBox<JobItem> jobBox;
    private JComboBox<VersionItem> buildVersionBox;
    private JComboBox<JavaHeapSize> maxHeapSizeBox;
    private JComboBox<JavaHeapSize> initialHeapSizeBox;
    private final JComboBox<String> languageBox = new JComboBox<String>();
    private final JCheckBox searchForLauncherUpdatesBox = new JCheckBox();
    private final JCheckBox closeLauncherAfterGameStartBox = new JCheckBox();

    private final File launcherDirectory;
    private File gamesDirectory;

    private final JCheckBox proxyEnabledBox = new JCheckBox();
    private final JTextField proxyHostField = new JTextField();
    private final JTextField proxyPortField = new JTextField();

    private final LauncherSettings launcherSettings;
    private final TerasologyGameVersions gameVersions;

    public SettingsMenu(final JFrame parent, final File launcherDirectory, final LauncherSettings launcherSettings,
            final TerasologyGameVersions gameVersions) {

        super(parent, BundleUtils.getLabel("settings_title"), true);

        this.launcherDirectory = launcherDirectory;
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
        populateProxySettings();

        gamesDirectory = launcherSettings.getGamesDirectory();

        pack();
    }

    private void populateProxySettings() {
        proxyEnabledBox.setSelected(launcherSettings.isProxyEnabled());
        proxyHostField.setText(launcherSettings.getProxyHost());
        proxyPortField.setText(launcherSettings.getProxyPort());
    }

    private void initComponents() {
        final Font settingsFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

        final JTabbedPane mainSettings = new JTabbedPane();
        mainSettings.addTab(BundleUtils.getLabel("settings_game_title"), createGameSettingsTab(settingsFont));
        mainSettings.addTab(BundleUtils.getLabel("settings_launcher_title"), createLauncherSettingsTab(settingsFont));

        /* ================== OK, Cancel ================== */
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
        contentPaneLayout.setHorizontalGroup(contentPaneLayout
                .createParallelGroup()
                .addComponent(mainSettings, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 0,
                        Short.MAX_VALUE)
                .addGroup(
                        contentPaneLayout.createSequentialGroup().addContainerGap()
                                .addComponent(saveButton, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cancelButton, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                                .addContainerGap()));
        contentPaneLayout.setVerticalGroup(contentPaneLayout.createParallelGroup().addGroup(
                contentPaneLayout
                        .createSequentialGroup()
                        .addComponent(mainSettings, GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(saveButton).addComponent(cancelButton)).addContainerGap()));
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel createGameSettingsTab(final Font settingsFont) {
        final JPanel gameSettingsTab = new JPanel();
        gameSettingsTab.setFont(settingsFont);

        final JLabel jobLabel = jlabel(BundleUtils.getLabel("settings_game_job"), settingsFont);

        jobBox = new JComboBox<>();
        jobBox.setFont(settingsFont);
        jobBox.addActionListener(this);
        jobBox.setActionCommand(JOB_ACTION);

        final JLabel buildVersionLabel = jlabel(BundleUtils.getLabel("settings_game_buildVersion"), settingsFont);

        buildVersionBox = new JComboBox<>();
        buildVersionBox.setFont(settingsFont);

        final JPanel gamesDirectoryPanel = new JPanel();

        final JLabel gamesDirectoryLabel = jlabel(BundleUtils.getLabel("settings_game_gamesDirectory"), settingsFont);

        final JButton gamesDirectoryOpenButton = new JButton();
        gamesDirectoryOpenButton.setFont(settingsFont);
        gamesDirectoryOpenButton.setText(BundleUtils.getLabel("settings_game_gamesDirectory_open"));
        gamesDirectoryOpenButton.addActionListener(this);
        gamesDirectoryOpenButton.setActionCommand(GAMES_DIRECTORY_OPEN);
        if (!Desktop.isDesktopSupported()) {
            gamesDirectoryOpenButton.setEnabled(false);
        }

        final JButton gamesDirectoryEditButton = new JButton();
        gamesDirectoryEditButton.setFont(settingsFont);
        gamesDirectoryEditButton.setText(BundleUtils.getLabel("settings_game_gamesDirectory_edit"));
        gamesDirectoryEditButton.addActionListener(this);
        gamesDirectoryEditButton.setActionCommand(GAMES_DIRECTORY_EDIT);

        gamesDirectoryPanel.add(gamesDirectoryOpenButton);
        gamesDirectoryPanel.add(gamesDirectoryEditButton);

        final JLabel maxHeapSizeLabel = jlabel(BundleUtils.getLabel("settings_game_maxHeapSize"), settingsFont);

        maxHeapSizeBox = new JComboBox<>();
        maxHeapSizeBox.setFont(settingsFont);
        maxHeapSizeBox.addActionListener(this);
        maxHeapSizeBox.setActionCommand(MAX_HEAP_SIZE_ACTION);

        final JLabel initialHeapSizeLabel = jlabel(BundleUtils.getLabel("settings_game_initialHeapSize"), settingsFont);

        initialHeapSizeBox = new JComboBox<>();
        initialHeapSizeBox.setFont(settingsFont);
        initialHeapSizeBox.addActionListener(this);
        initialHeapSizeBox.setActionCommand(INITIAL_HEAP_SIZE_ACTION);

        final GroupLayout gameTabLayout = new GroupLayout(gameSettingsTab);
        gameSettingsTab.setLayout(gameTabLayout);

        gameTabLayout.setHorizontalGroup(gameTabLayout.createParallelGroup().addGroup(
                gameTabLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                gameTabLayout.createParallelGroup().addComponent(jobLabel)
                                        .addComponent(buildVersionLabel).addComponent(gamesDirectoryLabel)
                                        .addComponent(maxHeapSizeLabel).addComponent(initialHeapSizeLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                gameTabLayout.createParallelGroup().addComponent(jobBox).addComponent(buildVersionBox)
                                        .addComponent(gamesDirectoryPanel).addComponent(maxHeapSizeBox)
                                        .addComponent(initialHeapSizeBox)).addContainerGap()));

        gameTabLayout.setVerticalGroup(gameTabLayout.createParallelGroup().addGroup(
                gameTabLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                gameTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jobLabel)
                                        .addComponent(jobBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                gameTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(buildVersionLabel)
                                        .addComponent(buildVersionBox, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                gameTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(gamesDirectoryLabel)
                                        .addComponent(gamesDirectoryPanel, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                gameTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(maxHeapSizeLabel)
                                        .addComponent(maxHeapSizeBox, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                gameTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(initialHeapSizeLabel)
                                        .addComponent(initialHeapSizeBox, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap()));
        return gameSettingsTab;
    }

    private JPanel createLauncherSettingsTab(final Font settingsFont) {

        languageBox.setFont(settingsFont);
        searchForLauncherUpdatesBox.setFont(settingsFont);
        closeLauncherAfterGameStartBox.setFont(settingsFont);

        final JPanel launcherSettingsTab = new JPanel();
        launcherSettingsTab.setFont(settingsFont);

        final JLabel languageLabel = jlabel(BundleUtils.getLabel("settings_launcher_chooseLanguage"), settingsFont);
        final JLabel searchForLauncherUpdatesLabel = jlabel(
                BundleUtils.getLabel("settings_launcher_searchForLauncherUpdates"), settingsFont);
        final JLabel closeLauncherAfterGameStartLabel = jlabel(
                BundleUtils.getLabel("settings_launcher_closeLauncherAfterGameStart"), settingsFont);


        final JPanel launcherDirectoryPanel = new JPanel();

        final JLabel launcherDirectoryLabel = jlabel(BundleUtils.getLabel("settings_launcher_launcherDirectory"),
                settingsFont);

        final JButton launcherDirectoryOpenButton = new JButton();
        launcherDirectoryOpenButton.setFont(settingsFont);
        launcherDirectoryOpenButton.setText(BundleUtils.getLabel("settings_launcher_launcherDirectory_open"));
        launcherDirectoryOpenButton.addActionListener(this);
        launcherDirectoryOpenButton.setActionCommand(LAUNCHER_DIRECTORY_OPEN);
        if (!Desktop.isDesktopSupported()) {
            launcherDirectoryOpenButton.setEnabled(false);
        }
        launcherDirectoryPanel.add(launcherDirectoryOpenButton);

        final GroupLayout launcherTabLayout = new GroupLayout(launcherSettingsTab);
        launcherSettingsTab.setLayout(launcherTabLayout);

        final JLabel proxyEnabledLabel = jlabel(BundleUtils.getLabel("settings_launcher_proxyEnabled"), settingsFont);
        final JLabel proxyHostLabel = jlabel(BundleUtils.getLabel("settings_launcher_proxyHost"), settingsFont);
        final JLabel proxyPortLabel = jlabel(BundleUtils.getLabel("settings_launcher_proxyPort"), settingsFont);

        launcherTabLayout.setHorizontalGroup(launcherTabLayout.createParallelGroup().addGroup(
                launcherTabLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        // labels
                        .addGroup(
                                launcherTabLayout.createParallelGroup().addComponent(languageLabel)
                                        .addComponent(searchForLauncherUpdatesLabel)
                                        .addComponent(closeLauncherAfterGameStartLabel)
                                        .addComponent(launcherDirectoryLabel).addComponent(proxyEnabledLabel)
                                        .addComponent(proxyHostLabel).addComponent(proxyPortLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        // inputs
                        .addGroup(
                                launcherTabLayout.createParallelGroup().addComponent(languageBox)
                                        .addComponent(searchForLauncherUpdatesBox)
                                        .addComponent(closeLauncherAfterGameStartBox)
                                        .addComponent(launcherDirectoryPanel).addComponent(proxyEnabledBox)
                                        .addComponent(proxyHostField).addComponent(proxyPortField))
                        .addContainerGap()));

        launcherTabLayout.setVerticalGroup(launcherTabLayout.createParallelGroup().addGroup(
                launcherTabLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                launcherTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(languageLabel)
                                        .addComponent(languageBox, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                launcherTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(searchForLauncherUpdatesLabel)
                                        .addComponent(searchForLauncherUpdatesBox, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                launcherTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(closeLauncherAfterGameStartLabel)
                                        .addComponent(closeLauncherAfterGameStartBox, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                launcherTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(launcherDirectoryLabel)
                                        .addComponent(launcherDirectoryPanel, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                launcherTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(proxyEnabledLabel)
                                        .addComponent(proxyEnabledBox, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                launcherTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(proxyHostLabel)
                                        .addComponent(proxyHostField, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                launcherTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(proxyPortLabel)
                                        .addComponent(proxyPortField, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                launcherTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                launcherTabLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE))
                        .addContainerGap()));

        return launcherSettingsTab;
    }

    private JLabel jlabel(final String label, final Font font) {
        final JLabel result = new JLabel(label);
        result.setFont(font);
        return result;
    }

    private void populateJob() {
        for (final GameJob job : GameJob.values()) {
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

        for (final TerasologyGameVersion version : gameVersions.getGameVersionList(jobItem.getJob())) {
            final VersionItem versionItem = new VersionItem(version);
            buildVersionBox.addItem(versionItem);
            if (versionItem.getVersion() == buildVersion) {
                buildVersionBox.setSelectedItem(versionItem);
            }
        }
    }

    private void populateHeapSize() {
        long max = 512;

        final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            max = ((com.sun.management.OperatingSystemMXBean) osBean).getTotalPhysicalMemorySize() / 1024 / 1024;
        }

        max = Math.max(max, 512);

        // detect 32 or 64 bit OS
        final String arch = System.getProperty("os.arch");
        final boolean bit64 = arch.contains("64");

        final List<JavaHeapSize> heapSizes = JavaHeapSize.getHeapSizes(max, bit64);
        for (final JavaHeapSize heapSize : heapSizes) {
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
        for (final Locale locale : Languages.SUPPORTED_LOCALES) {
            final String item = BundleUtils.getLabel(Languages.SETTINGS_LABEL_KEYS.get(locale));
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

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() instanceof JComponent) {
            actionPerformed(e.getActionCommand());
        }
    }

    private void actionPerformed(final String actionCommand) {
        switch (actionCommand) {
        case LAUNCHER_DIRECTORY_OPEN:
            try {
                DirectoryUtils.checkDirectory(launcherDirectory);
                Desktop.getDesktop().open(launcherDirectory);
            } catch (final IOException e) {
                logger.error("The launcher directory can not be opened! '{}'", launcherDirectory, e);
                JOptionPane.showMessageDialog(this, BundleUtils.getLabel("message_error_launcherDirectory") + "\n"
                        + launcherDirectory, BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
            }
            break;
        case GAMES_DIRECTORY_OPEN:
            try {
                DirectoryUtils.checkDirectory(gamesDirectory);
                Desktop.getDesktop().open(gamesDirectory);
            } catch (final IOException e) {
                logger.error("The game installation directory can not be opened! '{}'", gamesDirectory, e);
                JOptionPane.showMessageDialog(this, BundleUtils.getLabel("message_error_gamesDirectory") + "\n"
                        + gamesDirectory, BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
            }
            break;
        case GAMES_DIRECTORY_EDIT:
            final JFileChooser fileChooser = new JFileChooser(gamesDirectory);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle(BundleUtils.getLabel("settings_game_gamesDirectory_edit_title"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    final File selectedFile = fileChooser.getSelectedFile();
                    DirectoryUtils.checkDirectory(selectedFile);
                    gamesDirectory = selectedFile;
                } catch (final IOException e) {
                    logger.error("The game installation directory can not be created or used! '{}'", gamesDirectory, e);
                    JOptionPane.showMessageDialog(this, BundleUtils.getLabel("message_error_gamesDirectory") + "\n"
                            + gamesDirectory, BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
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

            // save gamesDirectory
            launcherSettings.setGamesDirectory(gamesDirectory);

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

            // proxy settings
            launcherSettings.setProxyEnabled(proxyEnabledBox.isSelected());
            launcherSettings.setProxyHost(proxyHostField.getText());
            launcherSettings.setProxyPort(proxyPortField.getText());

            // store changed settings
            try {
                launcherSettings.store();
                // apply http proxy immediately
                if (launcherSettings.isProxyEnabled()) {
                    System.setProperty("http.proxyHost", launcherSettings.getProxyHost());
                    System.setProperty("http.proxyPort", launcherSettings.getProxyPort());
                    logger.info("Using proxy host: '{}', port: '{}'", launcherSettings.getProxyHost(),
                            launcherSettings.getProxyPort());
                }
            } catch (final IOException e) {
                logger.error("The launcher settings can not be stored! '{}'",
                        launcherSettings.getLauncherSettingsFilePath(), e);
                JOptionPane.showMessageDialog(this, BundleUtils.getLabel("message_error_storeSettings"),
                        BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
            }
            dispose();
            setVisible(false);
            setAlwaysOnTop(false);
            break;
        }
    }
}
