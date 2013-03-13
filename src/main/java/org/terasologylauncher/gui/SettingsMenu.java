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
import org.terasologylauncher.Settings;
import org.terasologylauncher.Versions;
import org.terasologylauncher.util.BundleUtil;
import org.terasologylauncher.util.Memory;

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
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * @author Skaldarnar
 */
public class SettingsMenu extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SettingsMenu.class);

    private static final String SAVE_ACTION = "save";
    private static final String CANCEL_ACTION = "cancel";
    private static final String RESET_ACTION = "reset";

    private static final String BUILD_TYPE_ACTION = "buildType";
    private static final String MAX_MEM_ACTION = "maxMem";

    private static final String OPEN_LOG_DIR_ACTION = "openLogs";
    private static final String OPEN_MOD_DIR_ACTION = "openMods";
    private static final String OPEN_SAVED_DIR_ACTION = "openSaved";
    private static final String OPEN_SCREENS_DIR_ACTION = "openScreens";

    private JComboBox buildType;
    private JComboBox buildVersion;
    private JComboBox maxMem;
    private JComboBox initialMem;

    public SettingsMenu() {
        setTitle(BundleUtil.getLabel("settings_title"));
        setResizable(false);
        setIconImage(Toolkit.getDefaultToolkit().getImage(LauncherFrame.class.getResource("/org/terasologylauncher/images/icon.png")));

        initComponents();

        // TODO Why "setBuildType"?
        Settings.setBuildType(populateBuildType(Settings.getBuildType().type()));

        populateVersions();
        populateMaxMemory();
        populateInitialMemory();
    }

    private void initComponents() {
        // TODO Check if font "Arial" is available on all OS
        final Font settingsFont = new Font("Arial", Font.PLAIN, 12);

        JTabbedPane mainSettings = new JTabbedPane();
        mainSettings.addTab(BundleUtil.getLabel("settings_game_title"), createGameSettingsTab(settingsFont));
        mainSettings.addTab(BundleUtil.getLabel("settings_directories_title"), createDirectoriesTab(settingsFont));

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

        buildType = new JComboBox();
        buildType.setFont(settingsFont);
        buildType.addActionListener(this);
        buildType.setActionCommand(BUILD_TYPE_ACTION);

        JLabel buildVersionLabel = new JLabel();
        buildVersionLabel.setText(BundleUtil.getLabel("settings_game_buildVersion"));
        buildVersionLabel.setFont(settingsFont);

        buildVersion = new JComboBox();
        buildVersion.setFont(settingsFont);

        JLabel maxMemLabel = new JLabel();
        maxMemLabel.setText(BundleUtil.getLabel("settings_game_maxMemory"));
        maxMemLabel.setFont(settingsFont);

        maxMem = new JComboBox();
        maxMem.setFont(settingsFont);
        maxMem.addActionListener(this);
        maxMem.setActionCommand(MAX_MEM_ACTION);

        JLabel initialMemLabel = new JLabel();
        initialMemLabel.setText(BundleUtil.getLabel("settings_game_initialMemory"));
        initialMemLabel.setFont(settingsFont);

        initialMem = new JComboBox();
        initialMem.setFont(settingsFont);

        final GroupLayout gameTabLayout = new GroupLayout(gameSettingsTab);
        gameSettingsTab.setLayout(gameTabLayout);

        gameTabLayout.setHorizontalGroup(
            gameTabLayout.createParallelGroup()
                .addGroup(gameTabLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gameTabLayout.createParallelGroup()
                        .addComponent(buildTypeLabel)
                        .addComponent(buildVersionLabel)
                        .addComponent(maxMemLabel)
                        .addComponent(initialMemLabel))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup()
                        .addComponent(buildType)
                        .addComponent(buildVersion)
                        .addComponent(maxMem)
                        .addComponent(initialMem))
                    .addContainerGap())
        );

        gameTabLayout.setVerticalGroup(
            gameTabLayout.createParallelGroup()
                .addGroup(gameTabLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(buildTypeLabel)
                        .addComponent(buildType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(buildVersionLabel)
                        .addComponent(buildVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(maxMemLabel)
                        .addComponent(maxMem, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(gameTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(initialMemLabel)
                        .addComponent(initialMem, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
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

    private BuildType populateBuildType(final int selection) {
        buildType.addItem(BundleUtil.getLabel("settings_game_buildType_stable"));
        buildType.addItem(BundleUtil.getLabel("settings_game_buildType_nightly"));
        int newSelection = selection;
        if ((newSelection > (buildType.getItemCount() - 1)) || (newSelection < 0)) {
            newSelection = 0;
        }
        buildType.setSelectedIndex(newSelection);
        return BuildType.getType(newSelection);
    }

    private void populateVersions() {
        final BuildType currentType = Settings.getBuildType();

        // init versions
        Versions.getVersions(BuildType.STABLE);
        Versions.getVersions(BuildType.NIGHTLY);

        // load new version list
        for (final String version : Versions.getVersions(currentType)) {
            buildVersion.addItem(version);
        }
        for (int i = 0; i < buildVersion.getItemCount(); i++) {
            if (String.valueOf(buildVersion.getItemAt(i)).equals(Settings.getBuildVersion(currentType))) {
                buildVersion.setSelectedIndex(i);
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
            if (max >= m.getMemoryMB()) {
                maxMem.addItem(m.getLabel());
            }
        }

        final int memoryOptionID = Settings.getMaximalMemory();
        try {
            maxMem.setSelectedIndex(Memory.getMemoryIndexFromId(memoryOptionID));
        } catch (IllegalArgumentException e) {
            maxMem.removeAllItems();
            maxMem.addItem(String.valueOf(Memory.MEMORY_OPTIONS[0]));
            Settings.setMaximalMemory(0); // 0 == 256 MB
            maxMem.setSelectedIndex(0); // 1st element
        }
    }

    private void populateInitialMemory() {
        final int currentMemSetting = Memory.MEMORY_OPTIONS[maxMem.getSelectedIndex()].getMemoryMB();

        initialMem.removeAllItems();
        initialMem.addItem(BundleUtil.getLabel("settings_game_initialMemory_none"));
        for (final Memory m : Memory.MEMORY_OPTIONS) {
            if (currentMemSetting >= m.getMemoryMB()) {
                initialMem.addItem(m.getLabel());
            }
        }
        final int memoryOptionID = Settings.getInitialMemory();
        if (memoryOptionID == -1) {
            initialMem.setSelectedIndex(0);
            return;
        }
        try {
            initialMem.setSelectedIndex(Memory.getMemoryIndexFromId(memoryOptionID) + 1);
        } catch (IllegalArgumentException e) {
            initialMem.removeAllItems();
            initialMem.addItem(BundleUtil.getLabel("settings_game_initialMemory_none"));
            Settings.setInitialMemory(-1);
            initialMem.setSelectedIndex(0);
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() instanceof JComponent) {
            action(e.getActionCommand(), (JComponent) e.getSource());
        }
    }

    private void action(final String actionCommand, final JComponent source) {
        if (actionCommand.equals(BUILD_TYPE_ACTION)) {
            updateVersionBox();
        } else if (actionCommand.equals(MAX_MEM_ACTION)) {
            updateInitMemBox();
        } else if (actionCommand.equals(CANCEL_ACTION)) {
            dispose();
            setVisible(false);
            setAlwaysOnTop(false);
        } else if (actionCommand.equals(RESET_ACTION)) {
            //TODO: reload settings from saved file
        } else if (actionCommand.equals(SAVE_ACTION)) {
            // save build type and version
            final BuildType selectedType = BuildType.getType(buildType.getSelectedIndex());
            Settings.setBuildType(selectedType);
            Settings.setBuildVersion(String.valueOf(buildVersion.getSelectedItem()), selectedType);

            // save ram settings
            Settings.setMaximalMemory(Memory.MEMORY_OPTIONS[maxMem.getSelectedIndex()].getSettingsId());
            final int selectedInitMem = initialMem.getSelectedIndex();
            if (selectedInitMem > 0) {
                Settings.setInitialMemory(Memory.MEMORY_OPTIONS[initialMem.getSelectedIndex() - 1].getSettingsId());
            } else {
                Settings.setInitialMemory(-1);
            }

            // store changed settings
            Settings.storeSettings();
            dispose();
            setVisible(false);
            setAlwaysOnTop(false);
        }
        // TODO Implement OPEN_*_DIR_ACTION
    }

    private void updateInitMemBox() {
        final int currentIdx = initialMem.getSelectedIndex();

        final int currentMemSetting = Memory.MEMORY_OPTIONS[maxMem.getSelectedIndex()].getMemoryMB();
        initialMem.removeAllItems();
        initialMem.addItem(BundleUtil.getLabel("settings_game_initialMemory_none"));
        for (final Memory m : Memory.MEMORY_OPTIONS) {
            if (currentMemSetting >= m.getMemoryMB()) {
                initialMem.addItem(m.getLabel());
            }
        }

        if (currentIdx >= initialMem.getItemCount()) {
            initialMem.setSelectedIndex(initialMem.getItemCount() - 1);
        } else {
            initialMem.setSelectedIndex(currentIdx);
        }
    }

    private void updateVersionBox() {
        final BuildType currentType = BuildType.getType(buildType.getSelectedIndex());
        switch (currentType) {
            case STABLE:
                Settings.setBuildVersion(String.valueOf(buildVersion.getSelectedItem()), BuildType.NIGHTLY);
                break;
            case NIGHTLY:
                Settings.setBuildVersion(String.valueOf(buildVersion.getSelectedItem()), BuildType.STABLE);
                break;
        }

        buildVersion.removeAllItems();
        for (final String v : Versions.getVersions(currentType)) {
            buildVersion.addItem(v);
        }
        for (int i = 0; i < buildVersion.getItemCount(); i++) {
            if (String.valueOf(buildVersion.getItemAt(i)).equals(Settings.getBuildVersion(currentType))) {
                buildVersion.setSelectedIndex(i);
                break;
            }
        }
    }
}
