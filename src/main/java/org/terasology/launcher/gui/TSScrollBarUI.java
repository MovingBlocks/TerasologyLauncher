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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Custom scroll bar style to fit into the launcher. The scroll bar is a custom design with just a dark grey thumb element with rounded corners and no background.
 */
@Deprecated
final class TSScrollBarUI extends BasicScrollBarUI {

    public TSScrollBarUI() {
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        final Graphics2D g2d = (Graphics2D) g;
        final Color old = g2d.getColor();
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRoundRect(thumbBounds.x + 8, thumbBounds.y, thumbBounds.width - 8, thumbBounds.height, 8, 8);
        g2d.setColor(old);
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        // paint nothing
    }

    private JButton createZeroButton() {
        final JButton button = new JButton("zero button");
        final Dimension zeroDim = new Dimension(0, 0);
        button.setPreferredSize(zeroDim);
        button.setMinimumSize(zeroDim);
        button.setMaximumSize(zeroDim);
        return button;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }
}
