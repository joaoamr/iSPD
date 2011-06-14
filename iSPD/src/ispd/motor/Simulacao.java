/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor;

//import java.util.ArrayList;

import ispd.janela.JSimulacao;
import java.util.List;
import java.util.PriorityQueue;


/**
 *
 * @author denison_usuario
 */
public class Simulacao {

    private List<Mestre> mestres;
    private List<Link> links;
    private List<Tarefa> tarefas;
    private PriorityQueue<EventoFuturo> eventos;
    private JSimulacao janelaSimulacao;
    private boolean erroDeExecucao;
    private boolean fimExecucao;

    public Simulacao(JSimulacao janela, List<Mestre> mestres, List<Link> links, List<Tarefa> tarefas){
        this.mestres = mestres;
        this.links = links;
        this.tarefas = tarefas;
        if(mestres == null || links == null || tarefas == null) erroDeExecucao = true;
        else if(mestres.isEmpty() || links.isEmpty() || tarefas.isEmpty()) erroDeExecucao = true;
        this.fimExecucao = false;
        this.eventos = new PriorityQueue<EventoFuturo>();
    }

    public void simular() {
        if(!erroDeExecucao){
            //adiciona chegada das tarefas na lista de eventos futuros
            addEventos(tarefas);
            while(!erroDeExecucao && !fimExecucao){
                //executa proximo evento
                executarEvento();
            }
        }
    }

    private void addEventos(List<Tarefa> tarefas) {
        for (Tarefa tarefa : tarefas) {
            EventoFuturo evt = new EventoFuturo(tarefa.getTimeCriacao(), EventoFuturo.CRIAR_TAREFA, tarefa.getOrigem(), tarefa);
            eventos.add(evt);
        }
    }

    private void executarEvento() {
        //recupera o próximo evento e o executa.
        //executa estes eventos de acordo com sua ordem de chegada
        //de forma a evitar a execução de um evento antes de outro
        //que seria criado anteriormente
        EventoFuturo eventoAtual = eventos.poll();
        switch (eventoAtual.getTipo()) {
            case EventoFuturo.CRIAR_TAREFA:
                eventoAtual.getAgente().criarTarefa(eventoAtual.getTarefa());
            break;
            case EventoFuturo.ENVIAR_MENSAGEM:
                eventoAtual.getAgente().enviarMensagem(eventoAtual.getDestino(),eventoAtual.getTarefa());
            break;
            case EventoFuturo.RECEBER_MENSAGEM:
                eventoAtual.getAgente().recebeMensagem(eventoAtual.getOrigem(),eventoAtual.getTarefa());
            break;
        }
    }

}
