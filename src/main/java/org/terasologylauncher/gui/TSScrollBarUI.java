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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Custom scroll bar style to fit into the launcher.
 * The scroll bar is a custom design with just a dark grey thumb element with rounded corners and no background.
 *
 * @author Skaldarnar
 */
public class TSScrollBarUI extends BasicScrollBarUI {

    @Override
    protected void paintThumb(final Graphics g, final JComponent c, final Rectangle thumbBounds) {
        final Graphics2D g2d = (Graphics2D) g;
        final Color old = g2d.getColor();
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRoundRect(thumbBounds.x + 8, thumbBounds.y, thumbBounds.width - 8, thumbBounds.height, 8, 8);
        g2d.setColor(old);
    }

    @Override
    protected void paintTrack(final Graphics g, final JComponent c, final Rectangle trackBounds) {
        // paint nothing
    }

    protected JButton createZeroButton() {
        final JButton button = new JButton("zero button");
        final Dimension zeroDim = new Dimension(0, 0);
        button.setPreferredSize(zeroDim);
        button.setMinimumSize(zeroDim);
        button.setMaximumSize(zeroDim);
        return button;
    }

    @Override
    protected JButton createDecreaseButton(final int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(final int orientation) {
        return createZeroButton();
    }
}
