/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.launcher.util.BundleUtils;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;

public final class GuiUtils {

    private GuiUtils() {
    }

    public static void showWarningMessageDialog(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(parentComponent, message, BundleUtils.getLabel("message_error_title"), JOptionPane.WARNING_MESSAGE);
    }

    public static void showErrorMessageDialog(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(parentComponent, message, BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
    }

    public static File chooseDirectory(Component parentComponent, File directory, String title) {
        final JFileChooser fileChooser = new JFileChooser(directory.getParentFile());
        // Cannot use mode DIRECTORIES_ONLY, because the preselected name doesn't work.
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setSelectedFile(directory);
        fileChooser.setDialogTitle(title);
        if (fileChooser.showSaveDialog(parentComponent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return fileChooser.getSelectedFile();
    }
}
