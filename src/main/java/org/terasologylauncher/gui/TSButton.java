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
import org.terasologylauncher.util.BundleUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

/**
 * Custom JButton using the style/layout from the Terasology main menu.
 *
 * @author Skaldarnar
 */
public class TSButton extends JButton implements MouseListener {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(TSButton.class);

    private BufferedImage normalImg;
    private BufferedImage hoveredImg;
    private BufferedImage pressedImg;

    private boolean pressed;
    private boolean hovered;

    public TSButton(final String text) {
        super(text);
        setBorder(BorderFactory.createEmptyBorder());
        addMouseListener(this);

        try {
            normalImg = BundleUtils.getBufferedImage("button");
            hoveredImg = BundleUtils.getBufferedImage("button_hovered");
            pressedImg = BundleUtils.getBufferedImage("button_pressed");
        } catch (Exception e) {
            logger.error("Read button images failed!", e);
            normalImg = new BufferedImage(256, 30, BufferedImage.TYPE_INT_RGB);
            hoveredImg = new BufferedImage(256, 30, BufferedImage.TYPE_INT_RGB);
            pressedImg = new BufferedImage(256, 30, BufferedImage.TYPE_INT_RGB);
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;
        final Color old = g2d.getColor();

        if (pressed) {
            pressedImg.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
            g2d.drawImage(pressedImg, 0, 0, this);
        } else if (hovered) {
            hoveredImg.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
            g2d.drawImage(hoveredImg, 0, 0, this);
        } else {
            normalImg.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
            g2d.drawImage(normalImg, 0, 0, this);
        }

        // Draw label
        g2d.setColor(Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        float fontSize = 16f;
        while (g2d.getFontMetrics().stringWidth(getText()) > getWidth() - 4) {
            g2d.setFont(getFont().deriveFont(--fontSize));
        }

        final int width = g2d.getFontMetrics().stringWidth(getText());
        g2d.drawString(getText(), (getWidth() - width) / 2, ((getHeight() / 2) + (getFont().getSize() / 2)) - 2);

        g2d.setColor(old);
    }

    @Override
    public void mouseClicked(final MouseEvent e) {

    }

    @Override
    public void mousePressed(final MouseEvent e) {
        pressed = true;
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        pressed = false;
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        hovered = true;
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        hovered = false;
    }
}
