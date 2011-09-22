/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor;

//import java.util.ArrayList;
import ispd.escalonador.Mestre;
import ispd.janela.JSimulacao;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Maquina;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.metricas.MetricasGlobais;
import java.awt.Color;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author denison_usuario
 */
public class Simulacao {

    private double time = 0;
    private RedeDeFilas redeDeFilas;
    private List<Tarefa> tarefas;
    private PriorityQueue<EventoFuturo> eventos;
    private JSimulacao janela;

    public Simulacao(JSimulacao janela, RedeDeFilas redeDeFilas, List<Tarefa> tarefas) throws IllegalArgumentException {
        this.time = 0;
        this.eventos = new PriorityQueue<EventoFuturo>();
        this.janela = janela;

        this.redeDeFilas = redeDeFilas;
        this.tarefas = tarefas;
        if (redeDeFilas == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (redeDeFilas.getMestres() == null || redeDeFilas.getMestres().isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (redeDeFilas.getLinks() == null || redeDeFilas.getLinks().isEmpty()) {
            janela.println("The model has no Networks.", Color.orange);
        }
        if (tarefas == null || tarefas.isEmpty()) {
            throw new IllegalArgumentException("One or more  workloads have not been configured.");
        }
        janela.print("Creating routing." + " -> ");
        for (CS_Processamento mst : redeDeFilas.getMestres()) {
            Mestre temp = (Mestre) mst;
            //Cede acesso ao mestre a fila de eventos futuros
            temp.setSimulacao(this);
            //Encontra menor caminho entre o mestre e seus escravos
            mst.determinarCaminhos();
        }
        janela.incProgresso(5);
        janela.println("OK", Color.green);
        if (redeDeFilas.getMaquinas() == null || redeDeFilas.getMaquinas().isEmpty()) {
            janela.println("The model has no processing slaves.", Color.orange);
        } else {
            for (CS_Maquina maq : redeDeFilas.getMaquinas()) {
                //Encontra menor caminho entre o escravo e seu mestre
                maq.determinarCaminhos();
            }
        }
        janela.incProgresso(5);
    }

    public void simular() {
        //adiciona chegada das tarefas na lista de eventos futuros
        addEventos(tarefas);
        while (!eventos.isEmpty()) {
            //recupera o próximo evento e o executa.
            //executa estes eventos de acordo com sua ordem de chegada
            //de forma a evitar a execução de um evento antes de outro
            //que seria criado anteriormente
            EventoFuturo eventoAtual = eventos.poll();
            time = eventoAtual.getTempoOcorrencia();
            switch (eventoAtual.getTipo()) {
                case EventoFuturo.CHEGADA:
                    eventoAtual.getServidor().chegadaDeCliente(this, eventoAtual.getCliente());
                    break;
                case EventoFuturo.ATENDIMENTO:
                    eventoAtual.getServidor().atendimento(this, eventoAtual.getCliente());
                    break;
                case EventoFuturo.SAÍDA:
                    eventoAtual.getServidor().saidaDeCliente(this, eventoAtual.getCliente());
                    break;
                case EventoFuturo.ESCALONAR:
                    eventoAtual.getServidor().requisicao(this, null, EventoFuturo.ESCALONAR);
                    break;
            }
        }
        janela.incProgresso(30);
        janela.println("Simulation completed.", Color.green);
        janela.print("Getting Results." + " -> ");
        redeDeFilas.setMetricasGlobais(new MetricasGlobais(redeDeFilas, getTime()));
        janela.incProgresso(5);
        janela.println("OK", Color.green);
    }

    private void addEventos(List<Tarefa> tarefas) {
        for (Tarefa tarefa : tarefas) {
            EventoFuturo evt = new EventoFuturo(tarefa.getTimeCriacao(), EventoFuturo.CHEGADA, tarefa.getOrigem(), tarefa);
            eventos.add(evt);
        }
    }

    public PriorityQueue<EventoFuturo> getEventos() {
        return eventos;
    }

    public double getTime() {
        return time;
    }
}
