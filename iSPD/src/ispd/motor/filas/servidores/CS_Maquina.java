/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores;

import ispd.motor.EventoFuturo;
import ispd.motor.Simulacao;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author denison_usuario
 */
public class CS_Maquina extends CS_Processamento {

    private List<CS_Comunicacao> conexoesEntrada;
    private List<CS_Comunicacao> conexoesSaida;
    private List<Tarefa> filaTarefas;
    private List<CS_Processamento> mestres;
    List<List> caminhoMestre;
    private int processadoresDisponiveis;
    //Dados dinamicos
    private List<Tarefa> filaTarefasDinamica = new ArrayList<Tarefa>();
    private List<Tarefa> tarefaEmExecucao;
    public int cont = 0;
    public int cancel = 0;

    public CS_Maquina(String id, String proprietario, double PoderComputacional, int numeroProcessadores, double Ocupacao) {
        super(id, proprietario, PoderComputacional, numeroProcessadores, Ocupacao);
        this.conexoesEntrada = new ArrayList<CS_Comunicacao>();
        this.conexoesSaida = new ArrayList<CS_Comunicacao>();
        this.filaTarefas = new ArrayList<Tarefa>();
        this.mestres = new ArrayList<CS_Processamento>();
        this.processadoresDisponiveis = numeroProcessadores;
        this.tarefaEmExecucao = new ArrayList<Tarefa>(numeroProcessadores);
    }

    public void addConexoesEntrada(CS_Link conexao) {
        this.conexoesEntrada.add(conexao);
    }

    public void addConexoesSaida(CS_Link conexao) {
        this.conexoesSaida.add(conexao);
    }

    public void addConexoesEntrada(CS_Switch conexao) {
        this.conexoesEntrada.add(conexao);
    }

    public void addConexoesSaida(CS_Switch conexao) {
        this.conexoesSaida.add(conexao);
    }

    public void addMestre(CS_Processamento mestre) {
        this.mestres.add(mestre);
    }

    @Override
    public List<CS_Comunicacao> getConexoesSaida() {
        return this.conexoesSaida;
    }

    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        if (cliente.getEstado() != Tarefa.CANCELADO) {
            cliente.iniciarEsperaProcessamento(simulacao.getTime());
            if (processadoresDisponiveis != 0) {
                //indica que recurso está ocupado
                processadoresDisponiveis--;
                //cria evento para iniciar o atendimento imediatamente
                EventoFuturo novoEvt = new EventoFuturo(
                        simulacao.getTime(),
                        EventoFuturo.ATENDIMENTO,
                        this,
                        cliente);
                simulacao.getEventos().offer(novoEvt);
            } else {
                filaTarefas.add(cliente);
            }
        }
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        cliente.finalizarEsperaProcessamento(simulacao.getTime());
        cliente.iniciarAtendimentoProcessamento(simulacao.getTime());
        tarefaEmExecucao.add(cliente);
        //Gera evento para atender proximo cliente da lista
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime() + tempoProcessar(cliente.getTamProcessamento()),
                EventoFuturo.SAÍDA,
                this, cliente);
        //Event adicionado a lista de evntos futuros
        simulacao.getEventos().offer(evtFut);

    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        cont++;
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMflopsProcessados(cliente.getTamProcessamento());
        //Incrementa o tempo de transmissão
        double tempoProc = this.tempoProcessar(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoProcessamento(simulacao.getTime());
        tarefaEmExecucao.remove(cliente);
        //eficiencia calculada apenas nas classes CS_Maquina
        cliente.calcEficiencia(this.getPoderComputacional());
        //Devolve tarefa para o mestre
        if (mestres.contains(cliente.getOrigem())) {
            int index = mestres.indexOf(cliente.getOrigem());
            List<CentroServico> caminho = new ArrayList<CentroServico>((List<CentroServico>) caminhoMestre.get(index));
            cliente.setCaminho(caminho);
            //Gera evento para chegada da tarefa no proximo servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(),
                    EventoFuturo.CHEGADA,
                    cliente.getCaminho().remove(0),
                    cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.getEventos().offer(evtFut);
        } else {
            //buscar menor caminho!!!
            CS_Processamento novoMestre = (CS_Processamento) cliente.getOrigem();
            List<CentroServico> caminho = new ArrayList<CentroServico>(
                    CS_Maquina.getMenorCaminhoIndireto(this, novoMestre));
            this.addMestre(novoMestre);
            this.caminhoMestre.add(caminho);
            cliente.setCaminho(new ArrayList<CentroServico>(caminho));
            //Gera evento para chegada da tarefa no proximo servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(),
                    EventoFuturo.CHEGADA,
                    cliente.getCaminho().remove(0),
                    cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.getEventos().offer(evtFut);
        }
        if (filaTarefas.isEmpty()) {
            //Indica que está livre
            this.processadoresDisponiveis++;
        } else {
            //Gera evento para atender proximo cliente da lista
            Tarefa proxCliente = filaTarefas.remove(0);
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(),
                    EventoFuturo.ATENDIMENTO,
                    this, proxCliente);
            //Event adicionado a lista de evntos futuros
            simulacao.getEventos().offer(evtFut);
        }
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem cliente, int tipo) {
        if (cliente != null) {
            if (cliente.getTarefa() != null && cliente.getTarefa().getLocalProcessamento().equals(this)) {
                if (cliente.getTarefa().getEstado() == Tarefa.PARADO && cliente.getTipo() != Mensagem.PARAR) {
                    boolean remover = filaTarefas.remove(cliente.getTarefa());
                    if (remover && (cliente.getTipo() == Mensagem.DEVOLVER || cliente.getTipo() == Mensagem.DEVOLVER_COM_PREEMPCAO)) {
                        EventoFuturo evtFut = new EventoFuturo(
                                simulacao.getTime(),
                                EventoFuturo.CHEGADA,
                                cliente.getTarefa().getOrigem(),
                                cliente.getTarefa());
                        //Event adicionado a lista de evntos futuros
                        simulacao.getEventos().offer(evtFut);
                    }
                } else if (cliente.getTarefa().getEstado() == Tarefa.PROCESSANDO && cliente.getTipo() != Mensagem.DEVOLVER) {
                    //remover evento de saida do cliente do servidor
                    java.util.Iterator<EventoFuturo> interator = simulacao.getEventos().iterator();
                    boolean achou = false;
                    while (!achou && interator.hasNext()) {
                        EventoFuturo ev = interator.next();
                        if (ev.getCliente().equals(cliente.getTarefa())
                                && ev.getServidor().equals(this)
                                && ev.getTipo() == EventoFuturo.SAÍDA) {
                            achou = true;
                            simulacao.getEventos().remove(ev);
                        }
                    }
                    //gerar evento para atender proximo cliente
                    if (filaTarefas.isEmpty()) {
                        //Indica que está livre
                        this.processadoresDisponiveis++;
                    } else {
                        //Gera evento para atender proximo cliente da lista
                        Tarefa proxCliente = filaTarefas.remove(0);
                        EventoFuturo evtFut = new EventoFuturo(
                                simulacao.getTime(),
                                EventoFuturo.ATENDIMENTO,
                                this, proxCliente);
                        //Event adicionado a lista de evntos futuros
                        simulacao.getEventos().offer(evtFut);
                    }
                }
                switch (cliente.getTipo()) {
                    case Mensagem.CANCELAR:
                        cancel++;
                        double inicioAtendimento = cliente.getTarefa().cancelar(simulacao.getTime());
                        double tempoProc = simulacao.getTime() - inicioAtendimento;
                        double mflopsProcessados = this.getMflopsProcessados(tempoProc);
                        //Incrementa o número de Mflops processados por este recurso
                        this.getMetrica().incMflopsProcessados(mflopsProcessados);
                        //Incrementa o tempo de processamento
                        this.getMetrica().incSegundosDeProcessamento(tempoProc);
                        //Incrementa procentagem da tarefa processada
                        cliente.getTarefa().setPorcentagemProcessado(mflopsProcessados * 100 / cliente.getTarefa().getTamProcessamento());
                        break;
                    case Mensagem.DEVOLVER_COM_PREEMPCAO:
                        throw new UnsupportedOperationException("Not supported yet.");
                    //break;
                    case Mensagem.PARAR:
                        throw new UnsupportedOperationException("Not supported yet.");
                    //break;
                }
            } else if (cliente.getTipo() == Mensagem.ATUALIZAR) {
                //atualizar dados dinamicos
                this.filaTarefasDinamica.clear();
                for (Tarefa tarefa : tarefaEmExecucao) {
                    this.filaTarefasDinamica.add(tarefa);
                }
                for (Tarefa tarefa : filaTarefas) {
                    this.filaTarefasDinamica.add(tarefa);
                }
                //enviar resultados
                int index = mestres.indexOf(cliente.getOrigem());
                List<CentroServico> caminho = new ArrayList<CentroServico>((List<CentroServico>) caminhoMestre.get(index));
                Mensagem novoCliente = new Mensagem(this, cliente.getTamComunicacao(), Mensagem.RESULTADO_ATUALIZAR);
                novoCliente.setCaminho(caminho);
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(),
                        EventoFuturo.MENSAGEM,
                        novoCliente.getCaminho().remove(0),
                        novoCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.getEventos().offer(evtFut);
            }
        }
    }

    @Override
    public void determinarCaminhos() throws LinkageError {
        //Instancia objetos
        caminhoMestre = new ArrayList<List>(mestres.size());
        //Busca pelos caminhos
        for (int i = 0; i < mestres.size(); i++) {
            caminhoMestre.add(i, CS_Maquina.getMenorCaminho(this, mestres.get(i)));
        }
        //verifica se todos os mestres são alcansaveis
        for (int i = 0; i < mestres.size(); i++) {
            if (caminhoMestre.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }
    }

    public List<Tarefa> getInformacaoDinamicaFila() {
        return filaTarefasDinamica;
    }
}
