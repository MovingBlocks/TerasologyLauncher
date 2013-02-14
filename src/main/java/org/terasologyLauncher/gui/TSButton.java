package org.terasologyLauncher.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 11.02.13
 * Time: 17:11
 * To change this template use File | Settings | File Templates.
 */
public class TSButton extends JButton implements MouseListener {

    private static final URL NORMAL = TSButton.class.getResource("/button.png");
    private static final URL HOVERED = TSButton.class.getResource("/button_hovered.png");
    private static final URL PRESSED = TSButton.class.getResource("/button_pressed.png");

    private boolean pressed = false;
    private boolean hovered = false;

    public TSButton(String text) {
        super(text);
        setBorder(null);
        addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        Color old = g2d.getColor();

        URL icon;
        if (pressed) {
            icon = PRESSED;
        } else if (hovered) {
            icon = HOVERED;
        } else {
            icon = NORMAL;
        }

        //Draw background
        try {
            BufferedImage bg = ImageIO.read(new File(icon.toURI()));
            bg.getScaledInstance(getWidth(),getHeight(),Image.SCALE_SMOOTH);
            g2d.drawImage(bg, 0, 0, this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Draw label
        g2d.setColor(Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setFont(getFont().deriveFont(16f));
        int width = g2d.getFontMetrics().stringWidth(getText());
        g2d.drawString(getText(), (getWidth() - width) / 2, (getHeight() / 2) + (getFont().getSize()/2) - 2);

        g2d.setColor(old);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        pressed = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        hovered = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hovered = false;
    }
}
