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

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * JPanel with transparent background. Both, degree of transparency and color can be defined.
 *
 * @author Skaldarnar
 */
public final class TransparentPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private float transparency = 1f;

    public TransparentPanel(final float transparency) {
        this(transparency, Color.BLACK);
    }

    public TransparentPanel(final float transparency, final Color color) {
        this.transparency = transparency;
        setBorder(null);
        setOpaque(true);
        setBackground(color);
    }

    @Override
    public void paint(final Graphics g) {
        final Graphics2D copy = (Graphics2D) g.create();
        copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        super.paint(copy);
        copy.dispose();
    }
}
