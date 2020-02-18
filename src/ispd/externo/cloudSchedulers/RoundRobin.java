/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.externo.cloudSchedulers;

import ispd.escalonadorCloud.EscalonadorCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.ArrayList;
import java.util.HashMap;
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
    private HashMap <String, List<CentroServico>> caminhoEscravo;
    
    public RoundRobin(){
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new LinkedList<CS_Processamento>();
        this.caminhoEscravo = new HashMap<String, List<CentroServico>>();
        recursos = null;
        
    }

    @Override
    public void iniciar() {
        this.EscravosUsuario = new LinkedList<CS_Processamento>();       
        
    }

    @Override
    public Tarefa escalonarTarefa() {
        return tarefas.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if(recursos == null)
            recursos = EscravosUsuario.listIterator(0);
        
        if (recursos.hasNext()) {
            return recursos.next();
        }else{
            recursos = EscravosUsuario.listIterator(0);
            return recursos.next();
        }
    }

    @Override
    public void escalonar() {
        if(tarefas.isEmpty())
            return;
        
        Tarefa trf = escalonarTarefa();
        usuario = trf.getProprietario();
        EscravosUsuario = (LinkedList<CS_Processamento>) getVMsAdequadas(usuario, escravos);
        if(!EscravosUsuario.isEmpty()){
        
        CS_Processamento rec = escalonarRecurso();
        trf.setLocalProcessamento(rec);
        trf.setCaminho(escalonarRota(rec));
        mestre.enviarTarefa(trf);
        }
        else{
        adicionarTarefa(trf);
        mestre.liberarEscalonador();
        }
    }
    
    public void setCaminhoEscravo(HashMap<String, List<CentroServico>> caminhoEscravo){
        this.caminhoEscravo = caminhoEscravo;
    }
    
    @Override
    public void addCaminhoEscravo(String id, List caminho){
        this.caminhoEscravo.put(id, caminho);
    }
}
