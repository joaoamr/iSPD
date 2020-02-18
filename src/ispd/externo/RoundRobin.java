/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.externo;

import ispd.escalonador.Escalonador;
import ispd.motor.EventoFuturo;
import ispd.motor.Simulacao;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
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
public class RoundRobin extends Escalonador{
    private ListIterator<CS_Processamento> recursos;
    private HashMap<String, List> caminhos;
    private HashMap<String, List> tarefasexecutando;
    
    public RoundRobin(){
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new LinkedList<CS_Processamento>();
        this.caminhos = new HashMap<String,List>();
        this.tarefasexecutando = new HashMap<String, List>();
    }

    @Override
    public void iniciar() {        
        recursos = escravos.listIterator(0);
    }

    @Override
    public Tarefa escalonarTarefa() {
        return tarefas.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        CS_Processamento rec;
        do{
            if (recursos.hasNext()) {
                rec = recursos.next();
            }else{
                recursos = escravos.listIterator(0);
                rec = recursos.next();
            }
        } while(maquinasfalhadas.containsKey(rec.getId()));
        
        return rec;
    }

    @Override
    public void escalonar() {
        if(maquinasfalhadas.size() == escravos.size()){
            parado = true;
            return;
        }
        
        Tarefa trf = escalonarTarefa();
        CS_Processamento rec = escalonarRecurso();
        trf.setLocalProcessamento(rec);
        trf.setCaminho(escalonarRota(rec));
        mestre.enviarTarefa(trf);
    }

    @Override
    public void addEscravo(CS_Processamento maquina) {
        if(!indices.contains(maquina.getId())){
            this.indices.add(maquina.getId());
            this.escravos.add(maquina);
            ArrayList<Tarefa> tarefas = new ArrayList<Tarefa>();
            tarefasexecutando.put(maquina.getId(), tarefas);
        }
    }
    
    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        if(tarefa.getOrigem().equals(mestre)){
            List tarefas = tarefasexecutando.get(tarefa.getCSLProcessamento().getId());
            if(tarefas.remove(tarefa))
                this.metricaUsuarios.incTarefasConcluidas(tarefa);
        }
    }
    
    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        return new ArrayList<CentroServico>(caminhos.get(destino.getId()));
    }
    
    @Override
    public void recuperarTarefas(CS_Processamento maq, Simulacao sim){
        ArrayList<Tarefa>tarefasexec = (ArrayList<Tarefa>) tarefasexecutando.get(maq.getId());
        for(int i = 0; i < tarefasexec.size(); i++){            
            while(!tarefasexec.isEmpty()){
            tarefasexec.get(0).setEstado(Tarefa.CANCELADO); 
            Tarefa novocliente = new Tarefa(tarefasexec.remove(0));
            
            EventoFuturo evtFut = new EventoFuturo(
            sim.getTime(mestre),
            EventoFuturo.CHEGADA,
            (CentroServico)mestre, novocliente);
              
            sim.addEventoFuturo(evtFut);
            }
        }
    }
    
    @Override
    public void setCaminhoEscravo(List<List> caminhoEscravo) {
        for(int i = 0; i < caminhoEscravo.size(); i++){
            caminhos.put(escravos.get(i).getId(), caminhoEscravo.get(i));
        }
    }
}
