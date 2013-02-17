package org.terasologylauncher.util;

import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 11.02.13
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class ImageUtils {

    public static BufferedImage blur(final BufferedImage image, final int radius) {
        if (radius < 1) {
            throw new IllegalArgumentException("Filter radius has to be >= 1, but was " + radius);
        }

        return null;
    }
}
