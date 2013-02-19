/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.arquivo.interpretador.gridsim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author denison
 */
public class InterpretadorGridSim {

    private static String fname;

    private void setFileName(File f) {
        fname = f.getName();
    }

    public static String getFileName() {
        return fname;
    }

    public void interpreta(File file1) {
        FileInputStream fisfile = null;
        try {
            boolean error;
            fisfile = new FileInputStream(file1);
            JavaParser parser = JavaParser.main(fisfile);
            setFileName(file1);
            error = parser.resultadoParser();
            if (!error) {
                parser.writefile();
            }
            parser.reset();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InterpretadorGridSim.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fisfile.close();
            } catch (IOException ex) {
                Logger.getLogger(InterpretadorGridSim.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
