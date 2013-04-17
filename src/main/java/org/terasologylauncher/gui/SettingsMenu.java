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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasologylauncher.BuildType;
import org.terasologylauncher.Languages;
import org.terasologylauncher.Settings;
import org.terasologylauncher.util.BundleUtils;
import org.terasologylauncher.util.DirectoryUtils;
import org.terasologylauncher.util.JavaHeapSize;
import org.terasologylauncher.version.TerasologyGameVersion;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import java.util.Locale;

/**
 * @author Skaldarnar
 */
public final class SettingsMenu extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SettingsMenu.class);

    private static final String SAVE_ACTION = "save";
    private static final String CANCEL_ACTION = "cancel";
    private static final String RESET_ACTION = "reset";

    private static final String MAX_HEAP_SIZE_ACTION = "maxHeapSize";
    private static final String INITIAL_HEAP_SIZE_ACTION = "initialHeapSize";

    private JComboBox buildTypeBox;
    private JComboBox buildVersionStableBox;
    private JComboBox buildVersionNightlyBox;
    private JComboBox maxHeapSizeBox;
    private JComboBox initialHeapSizeBox;
    private JComboBox languageBox;

    private final File terasologyDirectory;
    private final Settings settings;
    private final TerasologyGameVersion gameVersion;

    public SettingsMenu(final File terasologyDirectory, final Settings settings,
                        final TerasologyGameVersion gameVersion) {
        this.terasologyDirectory = terasologyDirectory;
        this.settings = settings;
        this.gameVersion = gameVersion;

        setTitle(BundleUtils.getLabel("settings_title"));
        setResizable(false);
        setIconImage(BundleUtils.getImage("icon"));

        initComponents();

        populateBuildType();
        populateVersions(buildVersionStableBox, BuildType.STABLE);
        populateVersions(buildVersionNightlyBox, BuildType.NIGHTLY);
        populateHeapSize();
        populateLanguage();

        pack();
    }

    private void initComponents() {
        final Font settingsFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

        JTabbedPane mainSettings = new JTabbedPane();
        mainSettings.addTab(BundleUtils.getLabel("settings_game_title"), createGameSettingsTab(settingsFont));
        mainSettings.addTab(BundleUtils.getLabel("settings_directories_title"), createDirectoriesTab(settingsFont));
        mainSettings.addTab(BundleUtils.getLabel("settings_language_title"), createLanguageTab(settingsFont));

        /*================== OK, Cancel, Reset ==================*/
        JButton resetButton = new JButton();
        resetButton.setActionCommand(RESET_ACTION);
        resetButton.addActionListener(this);
        resetButton.setText(BundleUtils.getLabel("settings_reset"));

        JButton cancelButton = new JButton();
        cancelButton.setActionCommand(CANCEL_ACTION);
        cancelButton.addActionListener(this);
        cancelButton.setText(BundleUtils.getLabel("settings_cancel"));

        JButton saveButton = new JButton();
        saveButton.setActionCommand(SAVE_ACTION);
        saveButton.addActionListener(this);
        saveButton.setText(BundleUtils.getLabel("settings_save"));

        final Container contentPane = getContentPane();
        final GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(mainSettings, GroupLayout.Alignment.TRAILING,
                    GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(resetButton)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(cancelButton)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(saveButton, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addComponent(mainSettings, GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(resetButton)
                        .addComponent(cancelButton)
                        .addComponent(saveButton))
                    .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel createGameSettingsTab(final Font settingsFont) {
        JPanel gameSettingsTab = new JPanel();
        gameSettingsTab.setFont(settingsFont);

        JLabel buildTypeLabel = new JLabel();
        buildTypeLabel.setText(BundleUtils.getLabel("settings_game_buildType"));
        buildTypeLabel.setFont(settingsFont);

        buildTypeBox = new JComboBox();
        buildTypeBox.setFont(settingsFont);

        JLabel buildVersionStableLabel = new JLabel();
        buildVersionStableLabel.setText(BundleUtils.getLabel("settings_game_buildVersion_stable"));
        buildVersionStableLabel.setFont(settingsFont);

        buildVersionStableBox = new JComboBox();
        buildVersionStableBox.setFont(settingsFont);

        JLabel buildVersionNightlyLabel = new JLabel();
        buildVersionNightlyLabel.setText(BundleUtils.getLabel("settings_game_buildVersion_nightly"));
        buildVersionNightlyLabel.setFont(settingsFont);

        buildVersionNightlyBox = new JComboBox();
        buildVersionNightlyBox.setFont(settingsFont);

        JLabel maxHeapSizeLabel = new JLabel();
        maxHeapSizeLabel.setText(BundleUtils.getLabel("settings_game_maxHeapSize"));
        maxHeapSizeLabel.setFont(settingsFont);

        maxHeapSizeBox = new JComboBox();
        maxHeapSizeBox.setFont(settingsFont);
        maxHeapSizeBox.addActionListener(this);
        maxHeapSizeBox.setActionCommand(MAX_HEAP_SIZE_ACTION);

        JLabel initialHeapSizeLabel = new JLabel();
        initialHeapSizeLabel.setText(BundleUtils.getLabel("settings_game_initialHeapSize"));
        initialHeapSizeLabel.setFont(settingsFont);

        initialHeapSizeBox = new JComboBox();
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
                        .addComponent(buildTypeLabel)
                        .addComponent(buildVersionStableLabel)
                        .addComponent(buildVersionNightlyLabel)
                        .addComponent(maxHeapSizeLabel)
                        .addComponent(initialHeapSizeLabel))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup()
                        .addComponent(buildTypeBox)
                        .addComponent(buildVersionStableBox)
                        .addComponent(buildVersionNightlyBox)
                        .addComponent(maxHeapSizeBox)
                        .addComponent(initialHeapSizeBox))
                    .addContainerGap())
        );

        gameTabLayout.setVerticalGroup(
            gameTabLayout.createParallelGroup()
                .addGroup(gameTabLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(buildTypeLabel)
                        .addComponent(buildTypeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(buildVersionStableLabel)
                        .addComponent(buildVersionStableBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(buildVersionNightlyLabel)
                        .addComponent(buildVersionNightlyBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
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

    private JPanel createDirectoriesTab(final Font settingsFont) {
        JPanel directoriesTab = new JPanel();
        directoriesTab.setFont(settingsFont);

        final JLabel logDirLabel = new JLabel(BundleUtils.getLabel("settings_directories_logs"));
        JButton openLogDir = new JButton();
        openLogDir.setFont(settingsFont);
        openLogDir.setText(BundleUtils.getLabel("settings_directories_open"));
        final File logDir = new File(terasologyDirectory, DirectoryUtils.LOGS_DIR_NAME);
        if (!logDir.exists()) {
            openLogDir.setEnabled(false);
        }
        openLogDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DirectoryUtils.showInFileManager(logDir);
            }
        });

        final JLabel savedWorldsDirLabel = new JLabel(BundleUtils.getLabel("settings_directories_savedWorlds"));
        JButton openSavedWorldsDir = new JButton();
        openSavedWorldsDir.setFont(settingsFont);
        openSavedWorldsDir.setText(BundleUtils.getLabel("settings_directories_open"));
        final File savesDir = new File(terasologyDirectory, DirectoryUtils.SAVED_WORLDS_DIR_NAME);
        if (!savesDir.exists()) {
            openSavedWorldsDir.setEnabled(false);
        }
        openSavedWorldsDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DirectoryUtils.showInFileManager(savesDir);
            }
        });

        final JLabel screenShotDirLabel = new JLabel(BundleUtils.getLabel("settings_directories_screenShots"));
        JButton openScreenShotsDir = new JButton();
        openScreenShotsDir.setFont(settingsFont);
        openScreenShotsDir.setText(BundleUtils.getLabel("settings_directories_open"));
        final File screensDir = new File(terasologyDirectory, DirectoryUtils.SCREENSHOTS_DIR_NAME);
        if (!screensDir.exists()) {
            openScreenShotsDir.setEnabled(false);
        }
        openScreenShotsDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DirectoryUtils.showInFileManager(screensDir);
            }
        });

        final JLabel modsDirLabel = new JLabel(BundleUtils.getLabel("settings_directories_mods"));
        JButton openModsDir = new JButton();
        openModsDir.setFont(settingsFont);
        openModsDir.setText(BundleUtils.getLabel("settings_directories_open"));
        final File modsDir = new File(terasologyDirectory, DirectoryUtils.MODS_DIR_NAME);
        if (!modsDir.exists()) {
            openModsDir.setEnabled(false);
        }
        openModsDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DirectoryUtils.showInFileManager(modsDir);
            }
        });

        final GroupLayout directoriesTabLayout = new GroupLayout(directoriesTab);
        directoriesTab.setLayout(directoriesTabLayout);

        directoriesTabLayout.setHorizontalGroup(
            directoriesTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(directoriesTabLayout.createParallelGroup()
                    .addComponent(logDirLabel)
                    .addComponent(modsDirLabel)
                    .addComponent(savedWorldsDirLabel)
                    .addComponent(screenShotDirLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(directoriesTabLayout.createParallelGroup()
                    .addComponent(openLogDir, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.DEFAULT_SIZE)
                    .addComponent(openModsDir, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.DEFAULT_SIZE)
                    .addComponent(openSavedWorldsDir, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.DEFAULT_SIZE)
                    .addComponent(openScreenShotsDir, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.DEFAULT_SIZE))
                .addContainerGap()
        );

        directoriesTabLayout.setVerticalGroup(
            directoriesTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(directoriesTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(logDirLabel)
                    .addComponent(openLogDir))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(directoriesTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(modsDirLabel)
                    .addComponent(openModsDir))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(directoriesTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(savedWorldsDirLabel)
                    .addComponent(openSavedWorldsDir))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(directoriesTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(screenShotDirLabel)
                    .addComponent(openScreenShotsDir))
                .addContainerGap()
        );
        return directoriesTab;
    }

    private JPanel createLanguageTab(final Font settingsFont) {
        JPanel languageTab = new JPanel();
        languageTab.setFont(settingsFont);

        JLabel languageLabel = new JLabel();
        languageLabel.setText(BundleUtils.getLabel("settings_language_chooseLanguage"));
        languageLabel.setFont(settingsFont);

        languageBox = new JComboBox();
        languageBox.setFont(settingsFont);

        final GroupLayout languageTabLayout = new GroupLayout(languageTab);
        languageTab.setLayout(languageTabLayout);

        languageTabLayout.setHorizontalGroup(
            languageTabLayout.createParallelGroup()
                .addGroup(languageTabLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(languageTabLayout.createParallelGroup()
                        .addComponent(languageLabel))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(languageTabLayout.createParallelGroup()
                        .addComponent(languageBox))
                    .addContainerGap())
        );

        languageTabLayout.setVerticalGroup(
            languageTabLayout.createParallelGroup()
                .addGroup(languageTabLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(languageTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(languageLabel)
                        .addComponent(languageBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
        );

        return languageTab;
    }

    private void populateBuildType() {
        buildTypeBox.removeAllItems();

        buildTypeBox.addItem(BundleUtils.getLabel("settings_game_buildType_stable"));
        buildTypeBox.addItem(BundleUtils.getLabel("settings_game_buildType_nightly"));

        updateBuildTypeSelection();
    }

    private void updateBuildTypeSelection() {
        if (settings.getBuildType() == BuildType.STABLE) {
            buildTypeBox.setSelectedIndex(0);
        } else {
            buildTypeBox.setSelectedIndex(1);
        }
    }

    private void populateVersions(final JComboBox buildVersionBox, final BuildType buildType) {
        if (gameVersion.isVersionsLoaded()) {
            final int buildVersion = settings.getBuildVersion(buildType);

            for (final Integer version : gameVersion.getVersions(buildType)) {
                String item;
                if (version == Settings.BUILD_VERSION_LATEST) {
                    item = BundleUtils.getLabel("settings_game_buildVersion_latest");
                } else {
                    item = String.valueOf(version);
                }
                buildVersionBox.addItem(item);
                if (version == buildVersion) {
                    buildVersionBox.setSelectedItem(item);
                }
            }
        } else {
            buildVersionBox.setEnabled(false);
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
        for (JavaHeapSize heapSize : heapSizes) {
            maxHeapSizeBox.addItem(heapSize);
            initialHeapSizeBox.addItem(heapSize);
        }
        updateHeapSizeSelection();
    }

    private void updateHeapSizeSelection() {
        maxHeapSizeBox.setSelectedItem(settings.getMaxHeapSize());
        initialHeapSizeBox.setSelectedItem(settings.getInitialHeapSize());
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
            final String item = BundleUtils.getLabel(Languages.SETTINGS_LABEL_KEYS.get(locale));
            languageBox.addItem(item);

            if (Languages.getCurrentLocale().equals(locale)) {
                languageBox.setSelectedItem(item);
            }
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() instanceof JComponent) {
            actionPerformed(e.getActionCommand());
        }
    }

    private void actionPerformed(final String actionCommand) {
        if (actionCommand.equals(MAX_HEAP_SIZE_ACTION)) {
            updateInitialHeapSizeBox();
        } else if (actionCommand.equals(INITIAL_HEAP_SIZE_ACTION)) {
            updateMaxHeapSizeBox();
        } else if (actionCommand.equals(CANCEL_ACTION)) {
            dispose();
            setVisible(false);
            setAlwaysOnTop(false);
        } else if (actionCommand.equals(RESET_ACTION)) {
            // reload the right selections
            updateBuildTypeSelection();
            populateVersions(buildVersionStableBox, BuildType.STABLE);
            populateVersions(buildVersionNightlyBox, BuildType.NIGHTLY);
            updateHeapSizeSelection();
            populateLanguage();
        } else if (actionCommand.equals(SAVE_ACTION)) {
            // save build type and version
            final BuildType selectedType;
            if (buildTypeBox.getSelectedIndex() == 0) {
                selectedType = BuildType.STABLE;
            } else {
                selectedType = BuildType.NIGHTLY;
            }
            settings.setBuildType(selectedType);
            if (buildVersionStableBox.isEnabled()) {
                if (buildVersionStableBox.getSelectedIndex() == 0) {
                    settings.setBuildVersion(Settings.BUILD_VERSION_LATEST, BuildType.STABLE);
                } else {
                    settings.setBuildVersion(Integer.parseInt((String) buildVersionStableBox.getSelectedItem()),
                        BuildType.STABLE);
                }
            }
            if (buildVersionNightlyBox.isEnabled()) {
                if (buildVersionNightlyBox.getSelectedIndex() == 0) {
                    settings.setBuildVersion(Settings.BUILD_VERSION_LATEST, BuildType.NIGHTLY);
                } else {
                    settings.setBuildVersion(Integer.parseInt((String) buildVersionNightlyBox.getSelectedItem()),
                        BuildType.NIGHTLY);
                }
            }

            // save heap size settings
            settings.setMaxHeapSize((JavaHeapSize) maxHeapSizeBox.getSelectedItem());
            settings.setInitialHeapSize((JavaHeapSize) initialHeapSizeBox.getSelectedItem());

            // save languageBox settings
            Languages.update(Languages.SUPPORTED_LOCALES.get(languageBox.getSelectedIndex()));
            settings.setLocale(Languages.getCurrentLocale());

            // store changed settings
            try {
                settings.store();
            } catch (IOException e) {
                logger.error("Could not store settings!", e);
                JOptionPane.showMessageDialog(null, BundleUtils.getLabel("message_error_storeSettings"),
                    BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
            }
            dispose();
            setVisible(false);
            setAlwaysOnTop(false);
        }
    }
}
