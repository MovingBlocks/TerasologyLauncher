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
import java.net.URL;

/**
 * @author Skaldarnar
 */
public class SettingsMenu extends JDialog implements ActionListener {

    public static final URL ICON = LauncherFrame.class.getResource("/org/terasologylauncher/images/icon.png");

    private static final Logger logger = LoggerFactory.getLogger(SettingsMenu.class);

    private static final String SAVE_ACTION = "save";
    private static final String CANCEL_ACTION = "cancel";
    private static final String RESET_ACTION = "reset";

    private static final String BUILD_TYPE_ACTION = "buildType";

    private static final String OPEN_LOG_DIR_ACTION = "openLogs";
    private static final String OPEN_MOD_DIR_ACTION = "openMods";
    private static final String OPEN_SAVED_DIR_ACTION = "openSaved";
    private static final String OPEN_SCREENS_DIR_ACTION = "openScreens";

    /* To save the selected index when switching between different build types. */
    private int stableVersionIdx;
    private int nightlyVersionIdx;

    private JTabbedPane mainSettings;

    private JPanel gameSettingsTab;
    private JLabel buildTypeLabel;      // build type: nightly or stable
    private JComboBox buildType;
    private JLabel buildVersionLabel;   // build version: version number (e.g. stable #22)
    private JComboBox buildVersion;
    private JLabel maxMemLabel;
    private JComboBox maxMem;
    private JLabel initialMemLabel;
    private JComboBox initialMem;

    private JPanel directoriesTab;
    private JButton openLogDir;
    private JButton openSavedWorldsDir;
    private JButton openModsDir;
    private JButton openScreenShotsDir;

    private JButton resetButton;
    private JButton cancelButton;
    private JButton saveButton;

    public SettingsMenu() {
        initComponents();

        setTitle("Settings");
        setResizable(false);
        setIconImage(Toolkit.getDefaultToolkit().getImage(ICON));

        Settings.setBuildType(populateBuildType(buildType, Settings.getBuildType().type()));
        buildType.setActionCommand(BUILD_TYPE_ACTION);
        buildType.addActionListener(this);

        populateVersions(buildVersion);
        populateMaxMemory(maxMem);
        populateInitialMemory(initialMem);

        resetButton.setActionCommand(RESET_ACTION);
        resetButton.addActionListener(this);

        cancelButton.setActionCommand(CANCEL_ACTION);
        cancelButton.addActionListener(this);

        saveButton.setActionCommand(SAVE_ACTION);
        saveButton.addActionListener(this);

    }

    private void initComponents() {
        mainSettings = new JTabbedPane();

        gameSettingsTab = new JPanel();
        buildTypeLabel = new JLabel();
        buildType = new JComboBox();
        buildVersionLabel = new JLabel();
        buildVersion = new JComboBox();
        maxMemLabel = new JLabel();
        maxMem = new JComboBox();
        initialMemLabel = new JLabel();
        initialMem = new JComboBox();

        directoriesTab = new JPanel();
        openLogDir = new JButton();
        openSavedWorldsDir = new JButton();
        openScreenShotsDir = new JButton();
        openModsDir = new JButton();

        saveButton = new JButton();
        resetButton = new JButton();
        cancelButton = new JButton();

        Container contentPane = getContentPane();
        Font settingsFont = new Font("Arial", Font.PLAIN, 12);

        /*================= Game Settings =================*/
        gameSettingsTab.setFont(settingsFont);

        buildTypeLabel.setText("Build Type");
        buildTypeLabel.setFont(settingsFont);
        buildType.setFont(settingsFont);

        buildVersionLabel.setText("Build version:");
        buildVersionLabel.setFont(settingsFont);
        buildVersion.setFont(settingsFont);

        maxMemLabel.setText("Max. Memory:");
        maxMemLabel.setFont(settingsFont);
        maxMem.setFont(settingsFont);
        maxMem.addActionListener(this);

        initialMemLabel.setText("Initial Memory:");
        initialMemLabel.setFont(settingsFont);
        initialMem.setFont(settingsFont);

        GroupLayout gameTabLayout = new GroupLayout(gameSettingsTab);
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

        mainSettings.addTab("Game", gameSettingsTab);

        /*================= Directory Settings =================*/
        directoriesTab.setFont(settingsFont);

        JLabel logDirLabel = new JLabel("Logs:");
        openLogDir.setFont(settingsFont);
        openLogDir.setText("Open");
        openLogDir.addActionListener(this);
        openLogDir.setActionCommand(OPEN_LOG_DIR_ACTION);

        JLabel savedWorldsDirLabel = new JLabel("Saved Worlds:");
        openSavedWorldsDir.setFont(settingsFont);
        openSavedWorldsDir.setText("Open");
        openSavedWorldsDir.addActionListener(this);
        openSavedWorldsDir.setActionCommand(OPEN_SAVED_DIR_ACTION);

        JLabel screenShotDirLabel = new JLabel("Screen Shots:");
        openScreenShotsDir.setFont(settingsFont);
        openScreenShotsDir.setText("Open");
        openScreenShotsDir.addActionListener(this);
        openScreenShotsDir.setActionCommand(OPEN_SCREENS_DIR_ACTION);

        JLabel modsDirLabel = new JLabel("Mods:");
        openModsDir.setFont(settingsFont);
        openModsDir.setText("Open");
        openModsDir.addActionListener(this);
        openModsDir.setActionCommand(OPEN_MOD_DIR_ACTION);

        GroupLayout directoriesTabLayout = new GroupLayout(directoriesTab);
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

        mainSettings.addTab("Directories", directoriesTab);

        /*================== OK, Cancel, Reset ==================*/
        resetButton.setText("Reset");
        resetButton.addActionListener(this);
        resetButton.setActionCommand(RESET_ACTION);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(CANCEL_ACTION);

        saveButton.setText("Save");
        saveButton.addActionListener(this);
        saveButton.setActionCommand(SAVE_ACTION);

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
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

    private BuildType populateBuildType(JComboBox buildType, int selection) {
        buildType.addItem("Stable");
        buildType.addItem("Nightly");
        if (selection > buildType.getItemCount() - 1 || selection < 0) {
            selection = 0;
        }
        buildType.setSelectedIndex(selection);
        return BuildType.getType(selection);
    }

    private void populateVersions(JComboBox buildVersion) {
        BuildType currentType = Settings.getBuildType();

        logger.debug(Settings.getBuildVersion(BuildType.STABLE));
        logger.debug(Settings.getBuildVersion(BuildType.NIGHTLY));

        // init versions
        Versions.getVersions(BuildType.STABLE);
        Versions.getVersions(BuildType.NIGHTLY);

        // load new version list
        for (String version : Versions.getVersions(currentType)) {
            buildVersion.addItem(version);
        }
        for (int i = 0; i < buildVersion.getItemCount(); i++) {
            if (String.valueOf(buildVersion.getItemAt(i)).equals(Settings.getBuildVersion(currentType))) {
                buildVersion.setSelectedIndex(i);
            }
        }
    }

    private void populateMaxMemory(JComboBox maxMem) {
        long max = 512;

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            max = ((com.sun.management.OperatingSystemMXBean) osBean).getTotalPhysicalMemorySize() / 1024 / 1024;
        }

        max = Math.max(max, 512);

        // detect 32 or 64 bit OS
        String arch = System.getProperty("os.arch");
        boolean bit64 = arch.contains("64");

        // limit max memory for 32bit JVM
        if (!bit64) {
            max = Math.min(Memory.MAX_32_BIT_MEMORY, max);
            logger.debug("Maximal usable memory for 32 bit JVM: {}", max);
        } else {
            logger.debug("Maximal usable memory for 64 bit JVM: {}", max);
        }

        // fill in the combo box entries
        for (Memory m : Memory.MEMORY_OPTIONS) {
            if (max >= m.getMemoryMB()) {
                maxMem.addItem(m.getLabel());
            }
        }

        int memoryOptionID = Settings.getMaximalMemory();
        try {
            maxMem.setSelectedIndex(Memory.getMemoryIndexFromId(memoryOptionID));
        } catch (IllegalArgumentException e) {
            maxMem.removeAllItems();
            maxMem.addItem(String.valueOf(Memory.MEMORY_OPTIONS[0]));
            Settings.setMaximalMemory(0); // 0 == 256 MB
            maxMem.setSelectedIndex(0); // 1st element
        }
    }

    private void populateInitialMemory(JComboBox initialMemory) {
        int currentMemSetting = Memory.MEMORY_OPTIONS[maxMem.getSelectedIndex()].getMemoryMB();

        initialMem.removeAllItems();
        initialMemory.addItem("None");
        for (Memory m : Memory.MEMORY_OPTIONS) {
            if (currentMemSetting >= m.getMemoryMB()) {
                initialMemory.addItem(m.getLabel());
            }
        }
        int memoryOptionID = Settings.getInitialMemory();
        if (memoryOptionID == -1) {
            initialMemory.setSelectedIndex(0);
            return;
        }
        try {
            initialMemory.setSelectedIndex(Memory.getMemoryIndexFromId(memoryOptionID) + 1);
        } catch (IllegalArgumentException e) {
            initialMemory.removeAllItems();
            initialMemory.addItem("None");
            Settings.setInitialMemory(-1);
            initialMemory.setSelectedIndex(0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComponent) {
            action(e.getActionCommand(), (JComponent) e.getSource());
        }
    }

    private void action(String actionCommand, JComponent source) {
        if (source == maxMem) {
            updateInitMemBox(initialMem);
        }
        if (actionCommand.equals(BUILD_TYPE_ACTION)) {
            updateVersionBox(buildVersion);
        } else if (actionCommand.equals(CANCEL_ACTION)) {
            this.dispose();
            this.setVisible(false);
            this.setAlwaysOnTop(false);
        } else if (actionCommand.equals(RESET_ACTION)) {
            //TODO: reload settings from saved file
        } else if (actionCommand.equals(SAVE_ACTION)) {
            // save build type and version
            BuildType selectedType = BuildType.getType(buildType.getSelectedIndex());
            Settings.setBuildType(selectedType);
            Settings.setBuildVersion(String.valueOf(buildVersion.getSelectedItem()), selectedType);

            // save ram settings
            Settings.setMaximalMemory(Memory.MEMORY_OPTIONS[maxMem.getSelectedIndex()].getSettingsId());
            int selectedInitMem = initialMem.getSelectedIndex();
            if (selectedInitMem > 0) {
                Settings.setInitialMemory(Memory.MEMORY_OPTIONS[initialMem.getSelectedIndex() - 1].getSettingsId());
            } else {
                Settings.setInitialMemory(-1);
            }

            // store changed settings
            Settings.storeSettings();
            this.dispose();
            this.setVisible(false);
            this.setAlwaysOnTop(false);
        }
    }

    private void updateInitMemBox(JComboBox initialMem) {
        int currentIdx = initialMem.getSelectedIndex();

        int currentMemSetting = Memory.MEMORY_OPTIONS[maxMem.getSelectedIndex()].getMemoryMB();
        initialMem.removeAllItems();
        initialMem.addItem("None");
        for (Memory m : Memory.MEMORY_OPTIONS) {
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

    private void updateVersionBox(JComboBox buildVersion) {
        BuildType currentType = BuildType.getType(buildType.getSelectedIndex());
        switch (currentType) {
            case STABLE:
                Settings.setBuildVersion(String.valueOf(buildVersion.getSelectedItem()), BuildType.NIGHTLY);
                break;
            case NIGHTLY:
                Settings.setBuildVersion(String.valueOf(buildVersion.getSelectedItem()), BuildType.STABLE);
                break;
        }

        buildVersion.removeAllItems();
        for (String v : Versions.getVersions(currentType)) {
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
