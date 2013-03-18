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
import org.terasologylauncher.util.BundleUtil;
import org.terasologylauncher.util.Memory;
import org.terasologylauncher.version.TerasologyGameVersion;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Locale;

/**
 * @author Skaldarnar
 */
public class SettingsMenu extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SettingsMenu.class);

    private static final String SAVE_ACTION = "save";
    private static final String CANCEL_ACTION = "cancel";
    private static final String RESET_ACTION = "reset";

    private static final String MAX_MEM_ACTION = "maxMem";

    private static final String OPEN_LOG_DIR_ACTION = "openLogs";
    private static final String OPEN_MOD_DIR_ACTION = "openMods";
    private static final String OPEN_SAVED_DIR_ACTION = "openSaved";
    private static final String OPEN_SCREENS_DIR_ACTION = "openScreens";

    private JComboBox buildTypeBox;
    private JComboBox buildVersionStableBox;
    private JComboBox buildVersionNightlyBox;
    private JComboBox maxMemBox;
    private JComboBox initialMemBox;
    private JComboBox languageBox;

    private final Settings settings;

    public SettingsMenu(Settings settings) {
        this.settings = settings;
        setTitle(BundleUtil.getLabel("settings_title"));
        setResizable(false);
        setIconImage(Toolkit.getDefaultToolkit().getImage(LauncherFrame.class.getResource("/org/terasologylauncher/images/icon.png")));

        initComponents();

        populateBuildType();
        populateVersions(buildVersionStableBox, BuildType.STABLE);
        populateVersions(buildVersionNightlyBox, BuildType.NIGHTLY);
        populateMaxMemory();
        populateInitialMemory();
        populateLanguage();
    }

    private void initComponents() {
        // TODO Check if font "Arial" is available on all OS
        final Font settingsFont = new Font("Arial", Font.PLAIN, 12);

        JTabbedPane mainSettings = new JTabbedPane();
        mainSettings.addTab(BundleUtil.getLabel("settings_game_title"), createGameSettingsTab(settingsFont));
        mainSettings.addTab(BundleUtil.getLabel("settings_directories_title"), createDirectoriesTab(settingsFont));
        mainSettings.addTab(BundleUtil.getLabel("settings_language_title"), createLanguageTab(settingsFont));

        /*================== OK, Cancel, Reset ==================*/
        JButton resetButton = new JButton();
        resetButton.setActionCommand(RESET_ACTION);
        resetButton.addActionListener(this);
        resetButton.setText(BundleUtil.getLabel("settings_reset"));

        JButton cancelButton = new JButton();
        cancelButton.setActionCommand(CANCEL_ACTION);
        cancelButton.addActionListener(this);
        cancelButton.setText(BundleUtil.getLabel("settings_cancel"));

        JButton saveButton = new JButton();
        saveButton.setActionCommand(SAVE_ACTION);
        saveButton.addActionListener(this);
        saveButton.setText(BundleUtil.getLabel("settings_save"));

        final Container contentPane = getContentPane();
        final GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(mainSettings, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
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
        buildTypeLabel.setText(BundleUtil.getLabel("settings_game_buildType"));
        buildTypeLabel.setFont(settingsFont);

        buildTypeBox = new JComboBox();
        buildTypeBox.setFont(settingsFont);

        JLabel buildVersionStableLabel = new JLabel();
        buildVersionStableLabel.setText(BundleUtil.getLabel("settings_game_buildVersion_stable"));
        buildVersionStableLabel.setFont(settingsFont);

        buildVersionStableBox = new JComboBox();
        buildVersionStableBox.setFont(settingsFont);

        JLabel buildVersionNightlyLabel = new JLabel();
        buildVersionNightlyLabel.setText(BundleUtil.getLabel("settings_game_buildVersion_nightly"));
        buildVersionNightlyLabel.setFont(settingsFont);

        buildVersionNightlyBox = new JComboBox();
        buildVersionNightlyBox.setFont(settingsFont);

        JLabel maxMemLabel = new JLabel();
        maxMemLabel.setText(BundleUtil.getLabel("settings_game_maxMemory"));
        maxMemLabel.setFont(settingsFont);

        maxMemBox = new JComboBox();
        maxMemBox.setFont(settingsFont);
        maxMemBox.addActionListener(this);
        maxMemBox.setActionCommand(MAX_MEM_ACTION);

        JLabel initialMemLabel = new JLabel();
        initialMemLabel.setText(BundleUtil.getLabel("settings_game_initialMemory"));
        initialMemLabel.setFont(settingsFont);

        initialMemBox = new JComboBox();
        initialMemBox.setFont(settingsFont);

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
                        .addComponent(maxMemLabel)
                        .addComponent(initialMemLabel))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup()
                        .addComponent(buildTypeBox)
                        .addComponent(buildVersionStableBox)
                        .addComponent(buildVersionNightlyBox)
                        .addComponent(maxMemBox)
                        .addComponent(initialMemBox))
                    .addContainerGap())
        );

        gameTabLayout.setVerticalGroup(
            gameTabLayout.createParallelGroup()
                .addGroup(gameTabLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(buildTypeLabel)
                        .addComponent(buildTypeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(buildVersionStableLabel)
                        .addComponent(buildVersionStableBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(buildVersionNightlyLabel)
                        .addComponent(buildVersionNightlyBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(maxMemLabel)
                        .addComponent(maxMemBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(initialMemLabel)
                        .addComponent(initialMemBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
        );
        return gameSettingsTab;
    }

    private JPanel createDirectoriesTab(final Font settingsFont) {
        JPanel directoriesTab = new JPanel();
        directoriesTab.setFont(settingsFont);

        final JLabel logDirLabel = new JLabel(BundleUtil.getLabel("settings_directories_logs"));
        JButton openLogDir = new JButton();
        openLogDir.setFont(settingsFont);
        openLogDir.setText(BundleUtil.getLabel("settings_directories_open"));
        openLogDir.addActionListener(this);
        openLogDir.setActionCommand(OPEN_LOG_DIR_ACTION);

        final JLabel savedWorldsDirLabel = new JLabel(BundleUtil.getLabel("settings_directories_savedWorlds"));
        JButton openSavedWorldsDir = new JButton();
        openSavedWorldsDir.setFont(settingsFont);
        openSavedWorldsDir.setText(BundleUtil.getLabel("settings_directories_open"));
        openSavedWorldsDir.addActionListener(this);
        openSavedWorldsDir.setActionCommand(OPEN_SAVED_DIR_ACTION);

        final JLabel screenShotDirLabel = new JLabel(BundleUtil.getLabel("settings_directories_screenShots"));
        JButton openScreenShotsDir = new JButton();
        openScreenShotsDir.setFont(settingsFont);
        openScreenShotsDir.setText(BundleUtil.getLabel("settings_directories_open"));
        openScreenShotsDir.addActionListener(this);
        openScreenShotsDir.setActionCommand(OPEN_SCREENS_DIR_ACTION);

        final JLabel modsDirLabel = new JLabel(BundleUtil.getLabel("settings_directories_mods"));
        JButton openModsDir = new JButton();
        openModsDir.setFont(settingsFont);
        openModsDir.setText(BundleUtil.getLabel("settings_directories_open"));
        openModsDir.addActionListener(this);
        openModsDir.setActionCommand(OPEN_MOD_DIR_ACTION);

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
                    .addComponent(openLogDir, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                    .addComponent(openModsDir, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                    .addComponent(openSavedWorldsDir, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                    .addComponent(openScreenShotsDir, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
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
        languageLabel.setText(BundleUtil.getLabel("settings_language_chooseLanguage"));
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
                        .addComponent(languageBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
        );

        return languageTab;
    }

    private void populateBuildType() {
        buildTypeBox.addItem(BundleUtil.getLabel("settings_game_buildType_stable"));
        buildTypeBox.addItem(BundleUtil.getLabel("settings_game_buildType_nightly"));

        if (settings.getBuildType() == BuildType.STABLE) {
            buildTypeBox.setSelectedIndex(0);
        } else {
            buildTypeBox.setSelectedIndex(1);
        }
    }

    private void populateVersions(final JComboBox buildVersionBox, final BuildType buildType) {
        final int buildVersion = settings.getBuildVersion(buildType);

        for (final Integer version : TerasologyGameVersion.getVersions(settings, buildType)) {
            String item;
            if (version == Settings.BUILD_VERSION_LATEST) {
                item = BundleUtil.getLabel("settings_game_buildVersion_latest");
            } else {
                item = String.valueOf(version);
            }
            buildVersionBox.addItem(item);
            if (version == buildVersion) {
                buildVersionBox.setSelectedItem(item);
            }
        }
    }

    private void populateMaxMemory() {
        long max = 512;

        final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            max = ((com.sun.management.OperatingSystemMXBean) osBean).getTotalPhysicalMemorySize() / 1024 / 1024;
        }

        max = Math.max(max, 512);

        // detect 32 or 64 bit OS
        final String arch = System.getProperty("os.arch");
        final boolean bit64 = arch.contains("64");

        // limit max memory for 32bit JVM
        if (!bit64) {
            max = Math.min(Memory.MAX_32_BIT_MEMORY, max);
            logger.debug("Maximal usable memory for 32 bit JVM: {}", max);
        } else {
            logger.debug("Maximal usable memory for 64 bit JVM: {}", max);
        }

        // fill in the combo box entries
        for (final Memory m : Memory.MEMORY_OPTIONS) {
            if (m.getMemoryMB() <= max) {
                maxMemBox.addItem(m.getLabel());
            }
        }

        final int memoryOptionID = settings.getMaximalMemory();
        final int index = Memory.getMemoryIndexFromId(memoryOptionID);
        if (index < maxMemBox.getItemCount()) {
            maxMemBox.setSelectedIndex(index);
        } else {
            maxMemBox.setSelectedIndex(maxMemBox.getItemCount() - 1);
        }
    }

    private void populateInitialMemory() {
        final int currentMemSetting = Memory.MEMORY_OPTIONS[maxMemBox.getSelectedIndex()].getMemoryMB();

        initialMemBox.removeAllItems();
        initialMemBox.addItem(BundleUtil.getLabel("settings_game_initialMemory_none"));
        for (final Memory m : Memory.MEMORY_OPTIONS) {
            if (m.getMemoryMB() <= currentMemSetting) {
                initialMemBox.addItem(m.getLabel());
            }
        }
        final int memoryOptionID = settings.getInitialMemory();
        if (memoryOptionID == -1) {
            initialMemBox.setSelectedIndex(0);
        } else {
            final int index = Memory.getMemoryIndexFromId(memoryOptionID);
            if (index + 1 < initialMemBox.getItemCount()) {
                initialMemBox.setSelectedIndex(index + 1);
            } else {
                initialMemBox.setSelectedIndex(initialMemBox.getItemCount() - 1);
            }
        }
    }

    private void populateLanguage() {
        for (Locale locale : Languages.SUPPORTED_LOCALES) {
            final String item = BundleUtil.getLabel(Languages.SETTINGS_LABEL_KEYS.get(locale));
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
        if (actionCommand.equals(MAX_MEM_ACTION)) {
            updateInitMemBox();
        } else if (actionCommand.equals(CANCEL_ACTION)) {
            dispose();
            setVisible(false);
            setAlwaysOnTop(false);
        } else if (actionCommand.equals(RESET_ACTION)) {
            //TODO: reload settings from saved file
        } else if (actionCommand.equals(SAVE_ACTION)) {
            // save build type and version
            final BuildType selectedType;
            if (buildTypeBox.getSelectedIndex() == 0) {
                selectedType = BuildType.STABLE;
            } else {
                selectedType = BuildType.NIGHTLY;
            }
            settings.setBuildType(selectedType);
            if (buildVersionStableBox.getSelectedIndex() == 0) {
                settings.setBuildVersion(Settings.BUILD_VERSION_LATEST, BuildType.STABLE);
            } else {
                settings.setBuildVersion(Integer.parseInt((String) buildVersionStableBox.getSelectedItem()), BuildType.STABLE);
            }
            if (buildVersionNightlyBox.getSelectedIndex() == 0) {
                settings.setBuildVersion(Settings.BUILD_VERSION_LATEST, BuildType.NIGHTLY);
            } else {
                settings.setBuildVersion(Integer.parseInt((String) buildVersionNightlyBox.getSelectedItem()), BuildType.NIGHTLY);
            }

            // save ram settings
            settings.setMaximalMemory(Memory.MEMORY_OPTIONS[maxMemBox.getSelectedIndex()].getSettingsId());
            final int selectedInitMem = initialMemBox.getSelectedIndex();
            if (selectedInitMem > 0) {
                settings.setInitialMemory(Memory.MEMORY_OPTIONS[initialMemBox.getSelectedIndex() - 1].getSettingsId());
            } else {
                settings.setInitialMemory(Settings.INITIAL_MEMORY_NONE);
            }

            // save languageBox settings
            Languages.update(Languages.SUPPORTED_LOCALES.get(languageBox.getSelectedIndex()));
            settings.setLocale(Languages.getCurrentLocale());

            // store changed settings
            try {
                settings.store();
            } catch (IOException e) {
                logger.error("Could not store settings!", e);
                // TODO Show error message dialog
            }
            dispose();
            setVisible(false);
            setAlwaysOnTop(false);
        }
        // TODO Implement OPEN_*_DIR_ACTION
    }

    private void updateInitMemBox() {
        final int currentIdx = initialMemBox.getSelectedIndex();

        final int currentMemSetting = Memory.MEMORY_OPTIONS[maxMemBox.getSelectedIndex()].getMemoryMB();
        initialMemBox.removeAllItems();
        initialMemBox.addItem(BundleUtil.getLabel("settings_game_initialMemory_none"));
        for (final Memory m : Memory.MEMORY_OPTIONS) {
            if (m.getMemoryMB() <= currentMemSetting) {
                initialMemBox.addItem(m.getLabel());
            }
        }

        if (currentIdx >= initialMemBox.getItemCount()) {
            initialMemBox.setSelectedIndex(initialMemBox.getItemCount() - 1);
        } else {
            initialMemBox.setSelectedIndex(currentIdx);
        }
    }
}
