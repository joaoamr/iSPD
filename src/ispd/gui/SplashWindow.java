/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.gui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;
import javax.swing.ImageIcon;
import javax.swing.JWindow;

/**
 *
 * Janela de carregamento do iSPD, chamada durante a inicialização do programa
 *
 * @author denison_usuario
 */

public class SplashWindow extends JWindow {

    private static final int EXTRA = 7;
    private BufferedImage splash;
    private ImageIcon imagem;

    public SplashWindow(ImageIcon image) {
        int width = image.getIconWidth();
        int height = image.getIconHeight();

        setSize(new Dimension(
                width + EXTRA*2, height + EXTRA*2));
        setLocationRelativeTo(null);
        Rectangle windowRect = getBounds();
        imagem = image;
        splash = new BufferedImage(
                width + EXTRA*2, height + EXTRA*2,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) splash.getGraphics();

        try {
            Robot robot = new Robot(
                    getGraphicsConfiguration().getDevice());
            BufferedImage capture = robot.createScreenCapture(
                    new Rectangle(windowRect.x, windowRect.y,
                    windowRect.width + EXTRA*2,
                    windowRect.height + EXTRA*2));
            g2.drawImage(capture, null, 0, 0);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }

        BufferedImage shadow = new BufferedImage(
                width + EXTRA*2, height + EXTRA*2,
                BufferedImage.TYPE_INT_ARGB);
        Graphics shadowGraphics = shadow.getGraphics();
        shadowGraphics.setColor(
                new Color(0.0f, 0.0f, 0.0f, 0.3f));
        shadowGraphics.fillRoundRect(
                6, 6, width, height, 12, 12);
        shadowGraphics.dispose();

        float[] data = new float[49];
        Arrays.fill(data, 1 / (float) (49));
        g2.drawImage(shadow,
                new ConvolveOp(new Kernel(7, 7, data)),
                0, 0);
        g2.dispose();
    }

    @Override
    public void paint(Graphics g) {
        if (splash != null) {
            g.drawImage(splash, 0, 0, null);
            imagem.paintIcon(this, g, EXTRA, EXTRA);
        }
    }
}