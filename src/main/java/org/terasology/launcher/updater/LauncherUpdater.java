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

package org.terasology.launcher.updater;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.terasology.launcher.util.BundleUtils;

public final class LauncherUpdater {

    private String getChangeLog() {
        // TODO: read CHANGELOG file from jar 
        // Files.readAllLines(Paths.get("CHANGELOG.txt"), Charset.defaultCharset());
        return "";
    }

    public void showChangelog(Component parentComponent) {
        final JPanel msgPanel = new JPanel(new BorderLayout(0, 10));
        final JTextArea msgLabel = new JTextArea(BundleUtils.getLabel("message_update_launcher"));
        msgLabel.setBackground(msgPanel.getBackground());
        msgLabel.setEditable(false);

        final JTextArea changeLogArea = new JTextArea();
        changeLogArea.setText(getChangeLog());
        changeLogArea.setEditable(false);
        changeLogArea.setRows(15);
        changeLogArea.setBorder(BorderFactory.createEmptyBorder(1, 7, 1, 7));
        final JScrollPane changeLogPane = new JScrollPane(changeLogArea);
        changeLogPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        msgPanel.add(msgLabel, BorderLayout.NORTH);
        msgPanel.add(changeLogPane, BorderLayout.SOUTH);

        JOptionPane.showOptionDialog(parentComponent,
            msgPanel,
            BundleUtils.getLabel("message_update_launcher_title"),
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null, null, null);
    }
}
