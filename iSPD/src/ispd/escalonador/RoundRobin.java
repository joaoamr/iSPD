/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.escalonador;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do algoritmo de escalonamento Round-Robin
 * Atribui a proxima tarefa da fila (FIFO)
 * para o proximo recurso de uma fila circular de recursos
 * @author denison_usuario
 */
public class RoundRobin extends Escalonador{
    private int escravoAtual = -1;

    public RoundRobin(){
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new ArrayList<CS_Processamento>();
    }

    @Override
    public void iniciar() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void atualizar() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Tarefa escalonarTarefa() {
        return tarefas.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        escravoAtual++;
        if (escravos.size()<=escravoAtual) {
            escravoAtual=0;
        }
        return escravos.get(escravoAtual);
    }

    @Override
    public void escalonar(Mestre mestre) {
        Tarefa trf = escalonarTarefa();
        CS_Processamento rec = escalonarRecurso();
        trf.setCaminho(escalonarRota(rec));
        mestre.enviarTarefa(trf);
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
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = escravos.indexOf(destino);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoEscravo.get(index));
    }

}
