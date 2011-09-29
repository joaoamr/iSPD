/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo;

import ispd.escalonador.Escalonador;
import ispd.escalonador.Mestre;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do algoritmo de escalonamento Workqueue
 * Atribui a proxima tarefa da fila (FIFO)
 * para um recurso que está livre
 * @author denison_usuario
 */
public class Workqueue extends Escalonador{
    private Tarefa ultimaTarefaConcluida;
    private List<Tarefa> tarefaEnviada;
    private int servidoresOcupados;
    
    public Workqueue() {
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new ArrayList<CS_Processamento>();
        this.ultimaTarefaConcluida = null;
        this.servidoresOcupados = 0;
    }
    
    @Override
    public void iniciar() {
        tarefaEnviada = new ArrayList<Tarefa>(escravos.size());
        for(int i = 0; i < escravos.size(); i++){
            tarefaEnviada.add(null);
        }
    }

    @Override
    public void atualizar() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Tarefa escalonarTarefa() {
        if (tarefas.size() > 0) {
            return tarefas.remove(0);
        }
        return null;
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if (ultimaTarefaConcluida != null) {
            int index = tarefaEnviada.indexOf(ultimaTarefaConcluida);
            return this.escravos.get(index);
        }else{
            for(int i = 0; i < tarefaEnviada.size(); i++){
                if(tarefaEnviada.get(i) == null){
                    return this.escravos.get(i);
                }
            }
        }
        return null;
    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = escravos.indexOf(destino);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoEscravo.get(index));
    }

    @Override
    public void escalonar(Mestre mestre) {
        if(servidoresOcupados == 0){
            mestre.setTipoEscalonamento(Mestre.ENQUANTO_HOUVER_TAREFAS);
        }else if(servidoresOcupados == escravos.size()){
            mestre.setTipoEscalonamento(Mestre.QUANDO_RECEBE_RESULTADO);
        }
        CS_Processamento rec = escalonarRecurso();
        if(rec != null){
            Tarefa trf = escalonarTarefa();
            if(trf != null){
                tarefaEnviada.set(escravos.indexOf(rec), trf);
                ultimaTarefaConcluida = null;
                servidoresOcupados++;
                trf.setCaminho(escalonarRota(rec));
                mestre.enviarTarefa(trf);
            }
        }
    }

    @Override
    public void adicionarTarefa(Tarefa tarefa) {
        this.tarefas.add(tarefa);
    }

    @Override
    public void adicionarFilaTarefa(ArrayList<Tarefa> tarefa) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        if (ultimaTarefaConcluida != null) {
            int index = tarefaEnviada.indexOf(ultimaTarefaConcluida);
            servidoresOcupados--;
            tarefaEnviada.set(index, null);
        }
        this.ultimaTarefaConcluida = tarefa;
    }
}
