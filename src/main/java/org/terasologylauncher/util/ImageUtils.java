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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;

/**
 * ImageUtils hold various helper methods for image handling, such as scaling or blur filters.
 *
 * @author Tobias 'Skaldarnar' Nett
 */
public final class ImageUtils {

    private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);

    private ImageUtils() {
    }

    /**
     * Scales a BufferedImage to the preferred size.
     *
     * @param image  - the image to scale
     * @param width  - the new width
     * @param height - the new height
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

    /**
     * Apply blur filter to the given image. The blur intensity is defined by the given kernel.
     *
     * @param image  - the image to blur
     * @param kernel - the kernel used for bluring
     * @return a blured instance of the image
     */
    public static BufferedImage blur(final BufferedImage image, final Kernel kernel) {
        final int width = image.getWidth();
        final int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, image.getType());
        int[] in = new int[width * height];
        int[] out = new int[width * height];
        image.getRGB(0, 0, width, height, in, 0, width);

        convolve(kernel, in, out, width, height);
        convolve(kernel, out, in, height, width);

        result.setRGB(0, 0, width, height, in, 0, width);
        return result;
    }

    private static void convolve(final Kernel kernel, final int[] in, final int[] out, final int width,
                                       final int height) {
        final float[] matrix = kernel.getKernelData(null);
        final int cols = kernel.getWidth();
        final int halfCols = cols / 2;

        for (int y = 0; y < height; y++) {
            int index = y;
            int imageOffset = y * width;
            for (int x = 0; x < width; x++) {
                int matrixOffset = halfCols;
                float r = 0;
                float g = 0;
                float b = 0;
                for (int c = -halfCols; c <= halfCols; c++) {
                    float f = matrix[c + matrixOffset];
                    if (f != 0) {
                        int idx = x + c;
                        if (idx < 0) {
                            idx = (x + width) % width;
                        } else if (idx >= width) {
                            idx = (x + width) % width;
                        }
                        int rgb = in[idx + imageOffset];
                        r += f * ((rgb >> 16) & 0xff);
                        g += f * ((rgb >> 8) & 0xff);
                        b += f * (rgb & 0xff);
                    }
                }
                int ia = 0xff;
                int ir = clamp((int) (r + 0.5));
                int ig = clamp((int) (g + 0.5));
                int ib = clamp((int) (b + 0.5));
                out[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }
    }

    /**
     * Clamp a value to range 0..255
     *
     * @param c - value to clamp
     * @return clamped value in the range 0..255
     */
    public static int clamp(int c) {
        if (c < 0) {
            return 0;
        }
        if (c > 255) {
            return 255;
        }
        return c;
    }

    /**
     * Builds a gaussian kernel for blur filter. The range and influence can be fine-tuned via radius and sigma values.
     *
     * @param radius - the filter radius
     * @param sigma  - the filter 'intensity'
     * @return Gaussian kernel
     */
    public static Kernel buildKernel(final int radius, final float sigma) {
        final int entries = 2 * radius + 1;

        float[] kernelMatrix = new float[entries];
        final float sigmaSqrt2Pi = (float) (sigma * Math.sqrt(2 * Math.PI));
        final float sigmaSquare2 = 2 * sigma * sigma;

        float total = 0;
        // build up 1D kernel
        for (int x = -radius; x <= radius; x++) {
            int xSquare = x * x;

            kernelMatrix[x + radius] = (float) (Math.exp(-(xSquare / sigmaSquare2)) / sigmaSqrt2Pi);
            total += kernelMatrix[x + radius];
        }
        // Normalize
        for (int i = 0; i < entries; i++) {
            kernelMatrix[i] /= total;
        }
        logger.debug("Gaussian Kernel: {}", kernelMatrix);
        return new Kernel(entries, 1, kernelMatrix);
    }
}
