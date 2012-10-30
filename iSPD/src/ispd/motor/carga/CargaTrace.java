/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.carga;

import RedesDeFilas.RedesDeFilas;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import java.io.File;
import java.util.List;
import java.util.Vector;
import org.w3c.dom.Document;

/**
 *
 * @author Diogo Tavares
 */
public class CargaTrace extends GerarCarga{
    
    private String tipo;
    private File file;

    public CargaTrace(File file) {
        this.file = file;
    }

   
    

    @Override
    public Vector toVector() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Tarefa> toTarefaList(RedeDeFilas rdf) {
       return null;
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
                
    }

    @Override
    public int getTipo() {
        return GerarCarga.TRACE;
    }
    
}
