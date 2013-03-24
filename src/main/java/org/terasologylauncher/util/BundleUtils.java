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
import org.terasologylauncher.Languages;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

public final class BundleUtils {

    private static final Logger logger = LoggerFactory.getLogger(BundleUtils.class);

    private static final String LABELS_BUNDLE = "org.terasologylauncher.bundle.LabelsBundle";
    private static final String URI_BUNDLE = "org.terasologylauncher.bundle.URIBundle";
    private static final String IMAGE_BUNDLE = "org.terasologylauncher.bundle.ImageBundle";
    private static final String AUDIO_BUNDLE = "org.terasologylauncher.bundle.AudioBundle";

    private BundleUtils() {
    }

    public static String getLabel(final String key) {
        return ResourceBundle.getBundle(LABELS_BUNDLE, Languages.getCurrentLocale()).getString(key);
    }

    public static URI getURI(final String key) {
        final String uriStr = ResourceBundle.getBundle(URI_BUNDLE, Languages.getCurrentLocale()).getString(key);
        try {
            return new URI(uriStr);
        } catch (URISyntaxException e) {
            logger.error("Can not create the URI! " + uriStr, e);
        }
        return null;
    }

    public static ImageIcon getImageIcon(final String key) {
        final String imagePath = ResourceBundle.getBundle(IMAGE_BUNDLE, Languages.getCurrentLocale()).getString(key);
        return new ImageIcon(BundleUtils.class.getResource(imagePath));
    }

    public static Image getImage(final String key) {
        final String imagePath = ResourceBundle.getBundle(IMAGE_BUNDLE, Languages.getCurrentLocale()).getString(key);
        return Toolkit.getDefaultToolkit().getImage(BundleUtils.class.getResource(imagePath));
    }

    public static BufferedImage getBufferedImage(final String key) throws IOException {
        final String imagePath = ResourceBundle.getBundle(IMAGE_BUNDLE, Languages.getCurrentLocale()).getString(key);
        return ImageIO.read(BundleUtils.class.getResourceAsStream(imagePath));
    }

    public static void playSound(final String key) {
        final String audioPath = ResourceBundle.getBundle(AUDIO_BUNDLE, Languages.getCurrentLocale()).getString(key);

        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(BundleUtils.class.getResourceAsStream(audioPath));
            AudioFormat format = ais.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            logger.error("Failed to play sound {}", key, e);
        }
    }
}
