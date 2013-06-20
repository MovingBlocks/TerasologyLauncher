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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SplashScreen;

public final class SplashScreenWindow extends JWindow {

    private static final int INFO_LABEL_WIDTH = 400;

    private final JLabel infoLabel;

    public SplashScreenWindow(final Image image) {
        final ImageIcon icon = new ImageIcon(image);
        final Container contentPane = getContentPane();
        contentPane.setLayout(null);

        // Init the info label
        infoLabel = new JLabel();
        infoLabel.setBounds((icon.getIconWidth() / 2) - (INFO_LABEL_WIDTH / 2), (icon.getIconHeight() / 2) + 64,
            INFO_LABEL_WIDTH, 32);
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setHorizontalTextPosition(JLabel.CENTER);
        infoLabel.setHorizontalAlignment(JLabel.CENTER);

        contentPane.add(infoLabel);

        // Draw the image
        final BackgroundImage background = new BackgroundImage(icon.getIconWidth(), icon.getIconHeight(), image);
        contentPane.add(background);
        this.setBackground(new Color(0, 0, 0, 0));

        setSize(background.getWidth(), background.getHeight() + 20);

        SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) {
            Rectangle splashBounds = splashScreen.getBounds();
            setLocation((int) splashBounds.getX(), (int) splashBounds.getY());
            splashScreen.close();
        } else {
            setLocationRelativeTo(null);
        }
    }

    public JLabel getInfoLabel() {
        return infoLabel;
    }
}
