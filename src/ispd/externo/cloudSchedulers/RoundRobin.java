/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.externo.cloudSchedulers;

import ispd.escalonadorCloud.EscalonadorCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Implementação do algoritmo de escalonamento Round-Robin
 * Atribui a proxima tarefa da fila (FIFO)
 * para o proximo recurso de uma fila circular de recursos
 * @author denison_usuario
 */
public class RoundRobin extends EscalonadorCloud{
    private ListIterator<CS_Processamento> recursos;
    private LinkedList<CS_Processamento> EscravosUsuario;
    private String usuario;
    
    public RoundRobin(){
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new LinkedList<CS_Processamento>();
        
    }

    @Override
    public void iniciar() {
        this.EscravosUsuario = new LinkedList<CS_Processamento>();
        recursos = EscravosUsuario.listIterator(0);
        
    }

    @Override
    public Tarefa escalonarTarefa() {
        return tarefas.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso(String usuario) {
        
        this.EscravosUsuario = (LinkedList<CS_Processamento>) getEscravosUsuario(usuario, escravos);
       
        if (recursos.hasNext()) {
            return recursos.next();
        }else{
            recursos = EscravosUsuario.listIterator(0);
            return recursos.next();
        }
    }

    @Override
    public void escalonar() {
        Tarefa trf = escalonarTarefa();
        CS_Processamento rec = escalonarRecurso(trf.getProprietario());
        trf.setLocalProcessamento(rec);
        trf.setCaminho(escalonarRota(rec));
        mestre.enviarTarefa(trf);
    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = escravos.indexOf(destino);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoEscravo.get(index));
    }
}