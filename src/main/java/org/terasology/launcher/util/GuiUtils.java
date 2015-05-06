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

package org.terasology.launcher.util;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;
import java.util.concurrent.ExecutionException;

public final class GuiUtils {

    private GuiUtils() {
    }

    public static void showWarningMessageDialog(Component parentComponent, String message) {
        // TODO: Java8 -- Use ControlsFX dialog
        JOptionPane.showMessageDialog(parentComponent, message, BundleUtils.getLabel("message_error_title"), JOptionPane.WARNING_MESSAGE);
    }

    public static void showErrorMessageDialog(Component parentComponent, String message) {
        // TODO: Java8 -- Use ControlsFX dialog
        JOptionPane.showMessageDialog(parentComponent, message, BundleUtils.getLabel("message_error_title"), JOptionPane.ERROR_MESSAGE);
    }

    public static File chooseDirectoryDialog(Window parentWindow, final File directory, final String title) {
        if (!directory.isDirectory()) {
            directory.mkdir();
        }

        final Task<File> chooseDirectory = new Task<File>() {
            @Override
            protected File call() throws Exception {
                final DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(directory);
                directoryChooser.setTitle(title);
                final File selected = directoryChooser.showDialog(parentWindow);
                return selected;
            }
        };

        Platform.runLater(chooseDirectory);
        File selected = null;
        try {
            selected = chooseDirectory.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // directory proposal needs to be deleted if the user chose a different one
        if (!directory.equals(selected)) {
            directory.delete();
        }
        return selected;
    }
}
