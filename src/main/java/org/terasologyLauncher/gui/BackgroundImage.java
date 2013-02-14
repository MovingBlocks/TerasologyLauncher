package org.terasologyLauncher.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Skaldarnar
 */
public class BackgroundImage extends JLabel {

    public BackgroundImage(int width, int height) {
        setVerticalAlignment(SwingConstants.CENTER);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBounds(0, 0, width, height);

        InputStream stream = null;
        BufferedImage bg;

        try {
            stream = BackgroundImage.class.getResourceAsStream("/background.png");
            bg = ImageIO.read(stream);
            //TODO: Apply blur filter
        } catch (IOException e) {
            e.printStackTrace();
            bg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        setIcon(new ImageIcon(bg.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        setVerticalAlignment(SwingConstants.TOP);
        setHorizontalAlignment(SwingConstants.LEFT);
    }
}
