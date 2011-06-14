/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd;

import ispd.janela.LogExceptions;
import ispd.janela.SplashWindow;
import ispd.janela.JPrincipal;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author denison_usuario
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Exibir e armazenar erros durante execução:
        LogExceptions logExceptions = new LogExceptions(null);
        Thread.setDefaultUncaughtExceptionHandler(logExceptions);
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(Main.class.getResourceAsStream("janela/imagens/Splash.png"));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        SplashWindow window = new SplashWindow(image);
        window.setVisible(true);
        JPrincipal gui = new JPrincipal();
        gui.setVisible(true);
        gui.setLocationRelativeTo(null);
        logExceptions.setParentComponent(gui);
        window.dispose();
    }
}
