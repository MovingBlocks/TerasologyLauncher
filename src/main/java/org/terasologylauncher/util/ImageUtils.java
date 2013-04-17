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

package org.terasologylauncher.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class ImageUtils {

    private ImageUtils() {
    }

    /**
     * Scales a BufferedImage to the preferred size.
     *
     * @param image  - the image to scale
     * @param width  - the new width
     * @param height - the new height
     *
     * @return the scaled BufferedImage
     */
    public static BufferedImage getScaledInstance(final BufferedImage image, final int width, final int height) {
        BufferedImage scaled = new BufferedImage(width, height, image.getType());
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        return scaled;
    }

    public static BufferedImage blur(final BufferedImage image, final int radius) {
        // TODO: implement gaussian blur filter

        return null;
    }

    public static BufferedImage desaturate(final BufferedImage image) {
        // TODO: implement desaturation
        return null;
    }
}
