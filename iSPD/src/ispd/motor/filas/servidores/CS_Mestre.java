/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores;

import ispd.escalonador.Carregar;
import ispd.escalonador.Escalonador;
import ispd.escalonador.Mestre;
import ispd.motor.EventoFuturo;
import ispd.motor.Simulacao;
import ispd.motor.filas.Tarefa;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author denison_usuario
 */
public class CS_Mestre extends CS_Processamento implements Mestre {

    private List<CS_Comunicacao> conexoesEntrada;
    private List<CS_Comunicacao> conexoesSaida;
    private Escalonador escalonador;
    private List<Tarefa> filaProcessamento;
    private boolean maqDisponivel;
    private boolean escDisponivel;
    private int tipoEscalonamento;
    /**
     * Armazena os caminhos possiveis para alcançar cada escravo
     */
    private List<List> caminhoEscravo;
    private Simulacao simulacao;

    public CS_Mestre(String id, String proprietario, double PoderComputacional, double Ocupacao, String Escalonador) {
        super(id, proprietario, PoderComputacional, 1, Ocupacao);
        this.escalonador = Carregar.getNewEscalonador(Escalonador);
        this.filaProcessamento = new ArrayList<Tarefa>();
        this.maqDisponivel = true;
        this.escDisponivel = true;
        this.conexoesEntrada = new ArrayList<CS_Comunicacao>();
        this.conexoesSaida = new ArrayList<CS_Comunicacao>();
        this.tipoEscalonamento = ENQUANTO_HOUVER_TAREFAS;
    }

    //Métodos do centro de serviços
    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        //Tarefas concluida possuem tratamento diferencial
        if (cliente.getEstado() == Tarefa.CONCLUIDO) {
            //se não for origem da tarefa ela deve ser encaminhada
            if (!cliente.getOrigem().equals(this)) {
                //encaminhar tarefa!
                //Gera evento para chegada da tarefa no proximo servidor
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(),
                        EventoFuturo.CHEGADA,
                        cliente.getCaminho().remove(0),
                        cliente);
                //Event adicionado a lista de evntos futuros
                simulacao.getEventos().offer(evtFut);
            }else{
                this.escalonador.addTarefaConcluida(cliente);
            }
            if(tipoEscalonamento == QUANDO_RECEBE_RESULTADO){
                if(this.escalonador.getFilaTarefas().isEmpty()){
                    this.escDisponivel = true;
                }else{
                    executarEscalonamento();
                }
            }
        } else if (escDisponivel) {
            this.escDisponivel = false;
            //escalonador decide qual ação tomar na chegada de uma tarefa
            escalonador.adicionarTarefa(cliente);
            //Se não tiver tarefa na fila a primeira tarefa será escalonada
            executarEscalonamento();
        } else {
            //escalonador decide qual ação tomar na chegada de uma tarefa
            escalonador.adicionarTarefa(cliente);
        }
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        //o atendimento pode realiza o processamento da tarefa como em uma maquina qualquer
        if (this.maqDisponivel) {
            this.maqDisponivel = false;
            cliente.finalizarEsperaProcessamento(simulacao.getTime());
            cliente.iniciarAtendimentoProcessamento(simulacao.getTime());
            //Gera evento para saida do cliente do servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime() + tempoProcessar(cliente.getTamProcessamento()),
                    EventoFuturo.SAÍDA,
                    this, cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.getEventos().offer(evtFut);
        } else {
            filaProcessamento.add(cliente);
        }
    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        if (cliente.getEstado() == Tarefa.PROCESSANDO) {
            //Incrementa o número de Mbits transmitido por este link
            this.getMetrica().incMflopsProcessados(cliente.getTamProcessamento());
            //Incrementa o tempo de transmissão
            double tempoProc = this.tempoProcessar(cliente.getTamComunicacao());
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa o tempo de transmissão no pacote
            cliente.finalizarAtendimentoProcessamento(simulacao.getTime());
            //Gera evento para chegada da tarefa no proximo servidor
            if (filaProcessamento.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                ////Indica que está livre
                this.maqDisponivel = true;
                //Gera evento para atender proximo cliente da lista
                Tarefa proxCliente = filaProcessamento.remove(0);
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(),
                        EventoFuturo.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.getEventos().offer(evtFut);
            }
        } else {
            //Gera evento para chegada da tarefa no proximo servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(),
                    EventoFuturo.CHEGADA,
                    cliente.getCaminho().remove(0), cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.getEventos().offer(evtFut);
            if(tipoEscalonamento == ENQUANTO_HOUVER_TAREFAS){
                //se fila de tarefas do servidor não estiver vazia escalona proxima tarefa
                if (!escalonador.getFilaTarefas().isEmpty()) {
                    executarEscalonamento();
                } else {
                    this.escDisponivel = true;
                }
            }
        }
    }

    @Override
    public void requisicao(Simulacao simulacao, Tarefa cliente, int tipo) {
        escalonador.escalonar(this);
    }

    //métodos do Mestre
    public void enviarTarefa(Tarefa tarefa) {
        //Gera evento para atender proximo cliente da lista
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(),
                EventoFuturo.SAÍDA,
                this, tarefa);
        //Event adicionado a lista de evntos futuros
        simulacao.getEventos().offer(evtFut);
    }

    public void processarTarefa(Tarefa tarefa) {
        tarefa.iniciarEsperaProcessamento(simulacao.getTime());
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(),
                EventoFuturo.ATENDIMENTO,
                this, tarefa);
        //Event adicionado a lista de evntos futuros
        simulacao.getEventos().offer(evtFut);
    }

    public void executarEscalonamento() {
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(),
                EventoFuturo.ESCALONAR,
                this, null);
        //Event adicionado a lista de evntos futuros
        simulacao.getEventos().offer(evtFut);
    }

    public void setSimulacao(Simulacao simulacao) {
        this.simulacao = simulacao;
    }

    public Escalonador getEscalonador() {
        return escalonador;
    }
    
    public void addConexoesSaida(CS_Link link) {
        conexoesSaida.add(link);
    }

    public void addConexoesEntrada(CS_Link link) {
        conexoesEntrada.add(link);
    }

    public void addConexoesSaida(CS_Switch Switch) {
        conexoesSaida.add(Switch);
    }

    public void addConexoesEntrada(CS_Switch Switch) {
        conexoesEntrada.add(Switch);
    }

    public void addEscravo(CS_Processamento maquina) {
        escalonador.addEscravo(maquina);
    }

    @Override
    public List<CS_Comunicacao> getConexoesSaida() {
        return this.conexoesSaida;
    }

    /**
     * Encontra caminhos para chegar até um escravo e adiciona no caminhoEscravo
     */
    public void determinarCaminhos() throws LinkageError {
        List<CS_Processamento> escravos = escalonador.getEscravos();
        //Instancia objetos
        caminhoEscravo = new ArrayList<List>(escravos.size());
        //Busca pelo melhor caminho
        for (int i = 0; i < escravos.size(); i++) {
            caminhoEscravo.add(i, CS_Mestre.getMenorCaminho(this, escravos.get(i)));
        }
        //verifica se todos os escravos são alcansaveis
        for (int i = 0; i < escravos.size(); i++) {
            if (caminhoEscravo.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }
        escalonador.setCaminhoEscravo(caminhoEscravo);
    }

    public int getTipoEscalonamento() {
        return tipoEscalonamento;
    }
    
    public void setTipoEscalonamento(int tipo) {
        tipoEscalonamento = tipo;
    }
}
