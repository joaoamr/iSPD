/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas;

import ispd.motor.filas.servidores.CentroServico;
import java.util.List;

/**
 *
 * @author denison_usuario
 */
public abstract class Cliente {
    protected CentroServico ultimoCS = null;
    protected int id;
    private static int globalIndex = 0;
    
    public abstract double getTamComunicacao();
    public abstract double getTamProcessamento();
    public abstract double getTimeCriacao();
    public abstract CentroServico getOrigem();
    public abstract List<CentroServico> getCaminho();
    public abstract void setCaminho(List<CentroServico> caminho);
    
    public void setUltimoCS(CentroServico cs){
        ultimoCS = cs;
    }
    
    public CentroServico getUltimoCS(){
        return ultimoCS;
    }

    public final int getId() {
        return id;
    }
    
    public static final int atribuirIdGlobal(){
        int thisid = globalIndex;
        globalIndex++;
        return thisid;
    }
    
}
