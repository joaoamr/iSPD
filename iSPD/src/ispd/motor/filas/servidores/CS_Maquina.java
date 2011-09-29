/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores;

import ispd.motor.EventoFuturo;
import ispd.motor.Simulacao;
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
    
    public CS_Maquina(String id, String proprietario, double PoderComputacional, int numeroProcessadores, double Ocupacao) {
        super(id, proprietario, PoderComputacional, numeroProcessadores, Ocupacao);
        this.conexoesEntrada = new ArrayList<CS_Comunicacao>();
        this.conexoesSaida = new ArrayList<CS_Comunicacao>();
        this.filaTarefas = new ArrayList<Tarefa>();
        this.mestres = new ArrayList<CS_Processamento>();
        this.processadoresDisponiveis = numeroProcessadores;
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

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        cliente.finalizarEsperaProcessamento(simulacao.getTime());
        cliente.iniciarAtendimentoProcessamento(simulacao.getTime());
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
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMflopsProcessados(cliente.getTamProcessamento());
        //Incrementa o tempo de transmissão
        double tempoProc = this.tempoProcessar(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoProcessamento(simulacao.getTime());
        //Devolve tarefa para o mestre
        if(mestres.contains(cliente.getOrigem())){
            int index = mestres.indexOf(cliente.getOrigem());
            List<CentroServico> caminho = new ArrayList<CentroServico>((List<CentroServico>)caminhoMestre.get(index));
            cliente.setCaminho(caminho);
            //Gera evento para chegada da tarefa no proximo servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(),
                    EventoFuturo.CHEGADA,
                    cliente.getCaminho().remove(0),
                    cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.getEventos().offer(evtFut);
        }else{
            //buscar menor caminho!!!
            CS_Processamento novoMestre = (CS_Processamento)cliente.getOrigem();
            List<CentroServico> caminho = new ArrayList<CentroServico>(
                    CS_Maquina.getMenorCaminhoIndireto(this, novoMestre)
            );
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
        //Gera evento para chegada da tarefa no proximo servidor
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
    public void requisicao(Simulacao simulacao, Tarefa cliente, int tipo) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        for(int i = 0; i < mestres.size(); i++){
            if(caminhoMestre.get(i).isEmpty()){
                throw new LinkageError();
            }
        }
    }

}
