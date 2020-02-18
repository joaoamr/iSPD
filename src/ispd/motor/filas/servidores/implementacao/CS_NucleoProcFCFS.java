/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

import ispd.motor.EventoFuturo;
import ispd.motor.Mensagens;
import ispd.motor.Simulacao;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author João Antonio Magri Rodrigues
 */
public class CS_NucleoProcFCFS extends CS_Maquina implements Mensagens, Vertice {

    private List<Tarefa> filaTarefas;
    private int processadoresDisponiveis;
    //Dados dinamicos
    private List<Tarefa> tarefaEmExecucao;
    //Adição de falhas
    private List<Double> falhas = new ArrayList<Double>();
    private List<Double> recuperacao = new ArrayList<Double>();
    private boolean erroRecuperavel;
    private boolean falha = false; 
    private double inicioFalha = -1;
    private double fimFalha = -1;

    public CS_NucleoProcFCFS(CS_Processamento host) {
        super(host.getId(), host.getProprietario(), host.getPoderComputacional(), host.getNumeroProcessadores(), 0.0, 0);
        this.filaTarefas = new ArrayList<Tarefa>();
        this.processadoresDisponiveis = host.getNumeroProcessadores();
        this.tarefaEmExecucao = new ArrayList<Tarefa>(host.getNumeroProcessadores());
    }

    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        if (cliente.getEstado() != Tarefa.CANCELADO) {
            if (processadoresDisponiveis > 0) {
               // indica que recurso está ocupado
                processadoresDisponiveis--;
                //cria evento para iniciar o atendimento imediatamente
                EventoFuturo novoEvt = new EventoFuturo(
                        simulacao.getTime(this),
                        EventoFuturo.ATENDIMENTO,
                        this,
                        cliente);
                simulacao.addEventoFuturo(novoEvt);
            } else {
                filaTarefas.add(cliente);
            }
        }
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) { 
        tarefaEmExecucao.add(cliente);
        Double next = simulacao.getTime(this) + tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
       
        if (!falhas.isEmpty() && next > falhas.get(0)) {
            Double tFalha = falhas.remove(0);
            if (tFalha < simulacao.getTime(this)) {
                tFalha = simulacao.getTime(this);
            }
            Mensagem msg = new Mensagem(this, Mensagens.FALHAR, cliente);
            EventoFuturo evt = new EventoFuturo(
                    tFalha,
                    EventoFuturo.MENSAGEM,
                    this,
                    msg);
            simulacao.addEventoFuturo(evt);
        } else {
            falha = false;
            //Gera evento para atender proximo cliente da lista
            EventoFuturo evtFut = new EventoFuturo(
                    next,
                    EventoFuturo.SAÍDA,
                    this, cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        }
    }  

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {  
        double tempoProc = this.tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //cliente.finalizarAtendimentoProcessamento(simulacao.getTime(this));
        tarefaEmExecucao.remove(cliente);
 
        cliente.calcEficiencia(this.getPoderComputacional());       
        
        CentroServico machine = cliente.getLocalProcessamento();
        
        EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.SAÍDA,
                    machine, cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
            
        if (filaTarefas.isEmpty()) {
            //Indica que está livre
            this.processadoresDisponiveis++;
        } else {
            //Gera evento para atender proximo cliente da lista
            Tarefa proxCliente = filaTarefas.remove(0);
            EventoFuturo proxTarefa = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.ATENDIMENTO,
                    this, proxCliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(proxTarefa);
        }
          
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem mensagem, int tipo) {
        if (mensagem != null) {
            if (mensagem.getTipo() == Mensagens.ATUALIZAR) {
                atenderAtualizacao(simulacao, mensagem);
            } else if (mensagem.getTarefa() != null && mensagem.getTarefa().getLocalProcessamento().equals(this)) {
                switch (mensagem.getTipo()) {
                    case Mensagens.PARAR:
                        atenderParada(simulacao, mensagem);
                        break;
                    case Mensagens.CANCELAR:
                        atenderCancelamento(simulacao, mensagem);
                        break;
                    case Mensagens.DEVOLVER:
                        atenderDevolucao(simulacao, mensagem);
                        break;
                    case Mensagens.DEVOLVER_COM_PREEMPCAO:
                        atenderDevolucaoPreemptiva(simulacao, mensagem);
                        break;
                }
            }
        }
    }

    @Override
    public void atenderCancelamento(Simulacao simulacao, Mensagem mensagem) {
        if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            simulacao.removeEventoFuturo(EventoFuturo.SAÍDA, this, mensagem.getTarefa());
            tarefaEmExecucao.remove(mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                Tarefa proxCliente = filaTarefas.remove(0);
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(this),
                        EventoFuturo.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addEventoFuturo(evtFut);
            }
        }
        double inicioAtendimento = mensagem.getTarefa().cancelar(simulacao.getTime(this));
        double tempoProc = simulacao.getTime(this) - inicioAtendimento;
        double mflopsProcessados = this.getMflopsProcessados(tempoProc);
        //Incrementa o número de Mflops processados por este recurso
        this.getMetrica().incMflopsProcessados(mflopsProcessados);
        //Incrementa o tempo de processamento
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //Incrementa porcentagem da tarefa processada
        mensagem.getTarefa().setMflopsProcessado(mflopsProcessados);
    }

    @Override
    public void atenderParada(Simulacao simulacao, Mensagem mensagem) {
        if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            boolean remover = simulacao.removeEventoFuturo(
                    EventoFuturo.SAÍDA,
                    this,
                    mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                Tarefa proxCliente = filaTarefas.remove(0);
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(this),
                        EventoFuturo.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addEventoFuturo(evtFut);
            }
            double inicioAtendimento = mensagem.getTarefa().parar(simulacao.getTime(this));
            double tempoProc = simulacao.getTime(this) - inicioAtendimento;
            double mflopsProcessados = this.getMflopsProcessados(tempoProc);
            //Incrementa o número de Mflops processados por este recurso
            this.getMetrica().incMflopsProcessados(mflopsProcessados);
            //Incrementa o tempo de processamento
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa procentagem da tarefa processada
            mensagem.getTarefa().setMflopsProcessado(mflopsProcessados);
            tarefaEmExecucao.remove(mensagem.getTarefa());
            filaTarefas.add(mensagem.getTarefa());
        }
    }

    @Override
    public void atenderDevolucao(Simulacao simulacao, Mensagem mensagem) {
        boolean remover = filaTarefas.remove(mensagem.getTarefa());
        if (remover) {
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.CHEGADA,
                    mensagem.getTarefa().getOrigem(),
                    mensagem.getTarefa());
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        }
    }

    @Override
    public void atenderDevolucaoPreemptiva(Simulacao simulacao, Mensagem mensagem) {
        boolean remover = false;
        if (mensagem.getTarefa().getEstado() == Tarefa.PARADO) {
            remover = filaTarefas.remove(mensagem.getTarefa());
        } else if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            remover = simulacao.removeEventoFuturo(
                    EventoFuturo.SAÍDA,
                    this,
                    mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                Tarefa proxCliente = filaTarefas.remove(0);
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(this),
                        EventoFuturo.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addEventoFuturo(evtFut);
            }
            double inicioAtendimento = mensagem.getTarefa().parar(simulacao.getTime(this));
            double tempoProc = simulacao.getTime(this) - inicioAtendimento;
            double mflopsProcessados = this.getMflopsProcessados(tempoProc);
            //Incrementa o número de Mflops processados por este recurso
            this.getMetrica().incMflopsProcessados(mflopsProcessados);
            //Incrementa o tempo de processamento
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa procentagem da tarefa processada
            int numCP = (int) (mflopsProcessados / mensagem.getTarefa().getCheckPoint());
            mensagem.getTarefa().setMflopsProcessado(numCP * mensagem.getTarefa().getCheckPoint());
            tarefaEmExecucao.remove(mensagem.getTarefa());
        }
        if (remover) {
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.CHEGADA,
                    mensagem.getTarefa().getOrigem(),
                    mensagem.getTarefa());
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        }
    }

    @Override
    public void atenderRetornoAtualizacao(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void atenderFalha(Simulacao simulacao, Mensagem mensagem) {
        double tempoRec = recuperacao.remove(0);
        for (Tarefa tar : tarefaEmExecucao) {
            if (tar.getEstado() == Tarefa.PROCESSANDO) {
                falha = true;
                double inicioAtendimento = tar.parar(simulacao.getTime(this));
                double tempoProc = simulacao.getTime(this) - inicioAtendimento;
                double mflopsProcessados = this.getMflopsProcessados(tempoProc);
                //Incrementa o número de Mflops processados por este recurso
                this.getMetrica().incMflopsProcessados(mflopsProcessados);
                //Incrementa o tempo de processamento
                this.getMetrica().incSegundosDeProcessamento(tempoProc);
                //Incrementa procentagem da tarefa processada
                int numCP = (int) (mflopsProcessados / tar.getCheckPoint());
                tar.setMflopsProcessado(numCP * tar.getCheckPoint());
                if (erroRecuperavel) {
                    //Reiniciar atendimento da tarefa
                    tar.iniciarEsperaProcessamento(simulacao.getTime(this));
                    //cria evento para iniciar o atendimento imediatamente
                    EventoFuturo novoEvt = new EventoFuturo(
                            simulacao.getTime(this) + tempoRec,
                            EventoFuturo.ATENDIMENTO,
                            this,
                            tar);
                    simulacao.addEventoFuturo(novoEvt);
                } else {
                    tar.setEstado(Tarefa.FALHA);
                }
            }
        }
        if (!erroRecuperavel) {
            processadoresDisponiveis += tarefaEmExecucao.size();
            filaTarefas.clear();
        }
        tarefaEmExecucao.clear();
    }

    @Override
    public Integer getCargaTarefas() {
        if (falha) {
            return -100;
        } else {
            return (filaTarefas.size() + tarefaEmExecucao.size());
        }
    }

    public void addFalha(Double tFalha, double tRec, boolean recuperavel) {
        falhas.add(tFalha);
        recuperacao.add(tRec);
        erroRecuperavel = recuperavel;
    }

    
    @Override
    public boolean isInoperante() {
        return this.inoperante;
    }

    private boolean verificarFalhaRecurso(Simulacao sim, Tarefa cliente){
        if(inicioFalha == -1)
            return false;
        
        if(sim.getTime(this) >= inicioFalha && sim.getTime(this) <= fimFalha)
            return true;
            
        return inoperante;
    }
    
    @Override
    public double getInicioFalha() {
        return this.inicioFalha;
    }

    @Override
    public void setInicioFalha(double inicioFalha) {
        super.getMetrica().setTempoFalha(inicioFalha);
        this.inicioFalha = inicioFalha;
    }

    @Override
    public double getFimFalha() {
        return this.fimFalha;
    }

    @Override
    public void setFimFalha(double fimFalha) {
        super.getMetrica().setTempoRecuperacao(fimFalha);
        this.fimFalha = fimFalha;
    }
    
}