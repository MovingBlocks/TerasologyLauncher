package org.terasologyLauncher.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 11.02.13
 * Time: 17:11
 * To change this template use File | Settings | File Templates.
 */
public class TSButton extends JButton implements MouseListener {

    private BufferedImage normalImg;
    private BufferedImage hoveredImg;
    private BufferedImage pressedImg;

    private boolean pressed = false;
    private boolean hovered = false;

    public TSButton(String text) {
        super(text);
        setBorder(BorderFactory.createEmptyBorder());
        addMouseListener(this);

        try {
            normalImg = ImageIO.read(TSButton.class.getResourceAsStream("/button.png"));
            hoveredImg = ImageIO.read(TSButton.class.getResourceAsStream("/button_hovered.png"));
            pressedImg = ImageIO.read(TSButton.class.getResourceAsStream("/button_pressed.png"));
        } catch (Exception e) {
            e.printStackTrace();
            normalImg = new BufferedImage(256,30,BufferedImage.TYPE_INT_RGB);
            hoveredImg = new BufferedImage(256,30,BufferedImage.TYPE_INT_RGB);
            pressedImg = new BufferedImage(256,30,BufferedImage.TYPE_INT_RGB);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        Color old = g2d.getColor();

        if (pressed) {
            pressedImg.getScaledInstance(getWidth(),getHeight(),Image.SCALE_SMOOTH);
            g2d.drawImage(pressedImg, 0, 0, this);
        } else if (hovered) {
            hoveredImg.getScaledInstance(getWidth(),getHeight(),Image.SCALE_SMOOTH);
            g2d.drawImage(hoveredImg, 0, 0, this);
        } else {
            normalImg.getScaledInstance(getWidth(),getHeight(),Image.SCALE_SMOOTH);
            g2d.drawImage(normalImg, 0, 0, this);
        }

        // Draw label
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
