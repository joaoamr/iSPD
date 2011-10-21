/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo;

import ispd.escalonador.Escalonador;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do algoritmo de escalonamento Workqueue com replicação
 * Atribui a proxima tarefa da fila (FIFO), ou uma copia de uma tarefa em execução,
 * para um recurso que está livre
 * @author denison_usuario
 */
public class WQR extends Escalonador {
    private Tarefa ultimaTarefaConcluida;
    private List<Tarefa> tarefaEnviada;
    private int servidoresOcupados;
    private int cont;
    
    public WQR() {
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new ArrayList<CS_Processamento>();
        this.ultimaTarefaConcluida = null;
        this.servidoresOcupados = 0;
        this.cont = 0;
    }
    
    @Override
    public void iniciar() {
        tarefaEnviada = new ArrayList<Tarefa>(escravos.size());
        for(int i = 0; i < escravos.size(); i++){
            tarefaEnviada.add(null);
        }
    }

    @Override
    public Tarefa escalonarTarefa() {
        if (!tarefas.isEmpty()) {
            return tarefas.remove(0);
        }
        if(cont >= tarefaEnviada.size()){
            cont = 0;
        }
        if(servidoresOcupados >= escravos.size()){
            return null;
        }
        for(int i = cont; i < tarefaEnviada.size(); i++){
            if(tarefaEnviada.get(i) != null){
                cont = i;
                return mestre.criarCopia(tarefaEnviada.get(i));
            }
        }
        return null;
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        int index = tarefaEnviada.indexOf(ultimaTarefaConcluida);
        if (ultimaTarefaConcluida != null && index != -1) {    
            return this.escravos.get(index);
        }else{
            for(int i = 0; i < tarefaEnviada.size(); i++){
                if(tarefaEnviada.get(i) == null){
                    return this.escravos.get(i);
                }
            }
        }
        for(int i = 0; i < tarefaEnviada.size(); i++){
            if(tarefaEnviada.get(i) != null && tarefaEnviada.get(i).isCopy()){
                return this.escravos.get(i);
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
    public void escalonar() {
        CS_Processamento rec = escalonarRecurso();
        if(rec != null){
            Tarefa trf = escalonarTarefa();
            if(trf != null){
                if(tarefaEnviada.get(escravos.indexOf(rec)) != null){
                    mestre.enviarMensagem(tarefaEnviada.get(escravos.indexOf(rec)), rec, Mensagem.CANCELAR);
                }else{
                    servidoresOcupados++;
                }
                tarefaEnviada.set(escravos.indexOf(rec), trf);
                ultimaTarefaConcluida = null;
                trf.setLocalProcessamento(rec);
                trf.setCaminho(escalonarRota(rec));
                mestre.enviarTarefa(trf);
            }
        }
        if(servidoresOcupados > 0 && servidoresOcupados < escravos.size() && tarefas.isEmpty()){
            mestre.executarEscalonamento();
        }
    }

    @Override
    public void adicionarTarefa(Tarefa tarefa) {
        this.tarefas.add(tarefa);
    }

    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        int index = tarefaEnviada.indexOf(tarefa);
        if (index != -1) {
            servidoresOcupados--;
            tarefaEnviada.set(index, null);
        }
        for(int i = 0; i < tarefaEnviada.size(); i++){
            if(tarefaEnviada.get(i) != null && tarefaEnviada.get(i).isCopyOf(tarefa)){
                mestre.enviarMensagem(tarefaEnviada.get(i), escravos.get(i), Mensagem.CANCELAR);
                servidoresOcupados--;
                tarefaEnviada.set(i,null);
            }
        }
        this.ultimaTarefaConcluida = tarefa;
        if((servidoresOcupados > 0 && servidoresOcupados < escravos.size()) || !tarefas.isEmpty()){
                mestre.executarEscalonamento();
        }
    }

}
