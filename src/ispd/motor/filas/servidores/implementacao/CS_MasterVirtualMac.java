/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

import ispd.alocacaoVM.VMM;
import ispd.escalonadorCloud.CarregarCloud;
import ispd.escalonadorCloud.EscalonadorCloud;
import ispd.escalonadorCloud.MestreCloud;
import static ispd.escalonadorCloud.MestreCloud.AMBOS;
import static ispd.escalonadorCloud.MestreCloud.QUANDO_RECEBE_RESULTADO;
import ispd.motor.filas.servidores.implementacao.*;
import ispd.motor.EventoFuturo;
import ispd.motor.Mensagens;
import ispd.motor.Simulacao;
import ispd.motor.SimulacaoSequencial;
import ispd.motor.filas.Cliente;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.TarefaVM;
import ispd.motor.filas.servidores.CS_Processamento;
import static ispd.motor.filas.servidores.CS_Processamento.getMenorCaminhoIndiretoCloud;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.metricas.MetricasCusto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author João Antonio Magri Rodrigues
 * @author Diogo Tavares 
 */
public class CS_MasterVirtualMac extends CS_VirtualMac implements VMM, MestreCloud, Mensagens {
    
    public static final int LIVRE = 1;
    public static final int ALOCADA = 2;
    public static final int REJEITADA = 3;
    public static final int DESTRUIDA = 4;
    
    //Lista de atributos
    private CS_VMM vmmResponsavel;
    private int processadoresDisponiveis;
    private double poderProcessamento;
    private double memoriaDisponivel;
    private double discoDisponivel;
    private double instanteAloc;
    private double tempoDeExec;
    private String OS;
    private CS_MaquinaCloud maquinaHospedeira;
    private List<CentroServico> caminho;
    private List<CentroServico> caminhoVMM;
    private List<List> caminhoIntermediarios;
    private int status;
    private List<Tarefa> filaTarefas;
    private List<Tarefa> tarefaEmExecucao;
    private List<CS_VMM> VMMsIntermediarios;
    private MetricasCusto metricaCusto;
    private List<Double> falhas = new ArrayList<Double>();
    private List<Double> recuperacao = new ArrayList<Double>();
    private boolean erroRecuperavel;
    private boolean falha = false;
    private EscalonadorCloud escalonador;
    private int tipoEscalonamento;
    private boolean escDisponivel;
    private boolean vmsAlocadas;
    private double tempofinal;
    
    private List<List> caminhoEscravo;
    private HashMap<String, List> caminhoVMs;
    private HashMap<String, List> caminhoEscravos = new HashMap<String, List>();
    private Simulacao simulacao;
    private ArrayList<CS_VirtualMac> escravos;
    private ArrayList<CS_VirtualMac> escravosNaoAlocados;
    private int tarefasrecebidas = 0;
    
    
    /**
     * @author Diogo Tavares
     * @author João Antonio Magri Rodrigues
     * 
     * @param id
     * @param proprietario
     * @param numeroProcessadores
     * @param Ocupacao
     * @param numeroMaquina
     * @param memoria
     * @param disco 
     */ 
    
    public CS_MasterVirtualMac(String id, String proprietario, int numeroProcessadores, double poderNecessario, double memoria, double disco, String escalonador, String OS) {
        super(id, proprietario, numeroProcessadores, poderNecessario, memoria, disco, OS);
        this.processadoresDisponiveis = numeroProcessadores;
        this.memoriaDisponivel = memoria;
        this.discoDisponivel = disco;
        this.OS = OS;
        this.metricaCusto = new MetricasCusto(id);
        this.maquinaHospedeira = null;
        this.caminhoVMM = null;
        this.VMMsIntermediarios = new ArrayList<CS_VMM>();
        this.caminhoIntermediarios = new ArrayList<List>();
        this.tempoDeExec = 0;
        this.status = LIVRE;
        this.tarefaEmExecucao = new ArrayList<Tarefa>();
        this.filaTarefas = new ArrayList<Tarefa>();
        this.escalonador = CarregarCloud.getNewEscalonadorCloud(escalonador);
        this.escalonador.setMestre(this);
        this.tipoEscalonamento = ENQUANTO_HOUVER_TAREFAS;
        this.escDisponivel = false;
        this.vmsAlocadas = false;
        this.maquinaHospedeira = null;
        this.escravosNaoAlocados = new ArrayList<CS_VirtualMac>();
        this.escravos = new ArrayList<CS_VirtualMac>();
        this.caminhoVMs = new HashMap<String, List>();
        this.tempofinal = 0;
    }

    
    @Override
     public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        cliente.setUltimoCS(this);
        if(maquinaHospedeira == null)
        {
            System.out.println("Mestre nao alocado!");
            return;
        }
           if (cliente.getEstado() != Tarefa.CANCELADO) {
                //Tarefas concluida possuem tratamento diferencial
                if (cliente.getEstado() == Tarefa.CONCLUIDO) {
                    //se não for origem da tarefa ela deve ser encaminhada
                    if (!cliente.getOrigem().equals(this)) {
                        //encaminhar tarefa!
                        //Gera evento para chegada da tarefa no proximo servidor
                        EventoFuturo evtFut = new EventoFuturo(
                                simulacao.getTime(this),
                                EventoFuturo.CHEGADA,
                                cliente.getCaminho().remove(0),
                                cliente);
                        //Adicionar  na lista de eventos futuros
                        simulacao.addEventoFuturo(evtFut);
                    }
                    //caso seja este o centro de serviço de origem
                    //System.out.println(tarefasrecebidas + ": Concluindo tarefa: " + cliente.getIdentificador() + " tempo: " + simulacao.getTime(this));
                    
                    this.escalonador.addTarefaConcluida(cliente);
                    tempofinal = simulacao.getTime(this);

                    if (tipoEscalonamento == QUANDO_RECEBE_RESULTADO || tipoEscalonamento == AMBOS) {
                        if (this.escalonador.getFilaTarefas().isEmpty()) {
                            this.escDisponivel = true;
                        } else {
                            executarEscalonamento();
                        }
                    }
                } //Caso a tarefa esteja chegando pra ser escalonada
                else {
                    if (!(cliente.getLocalProcessamento() == null)) {
                        if(cliente.getCaminho() == null){
                            List<CentroServico> caminhosub = CS_VMM.getMenorCaminhoCloud(maquinaHospedeira, ((CS_VirtualMac)cliente.getLocalProcessamento()).getMaquinaHospedeira());
            
                            if(caminhosub == null){
                                caminhosub = new ArrayList<CentroServico>();
                                caminhosub.add(maquinaHospedeira.getVirtualBridge());
                                caminhosub.add(maquinaHospedeira);
                            }
                            
                            cliente.setCaminho(caminhosub);
                        }
                        
                        EventoFuturo evtFut = new EventoFuturo(
                                simulacao.getTime(this),
                                EventoFuturo.CHEGADA,
                                cliente.getCaminho().remove(0),
                                cliente);
                        simulacao.addEventoFuturo(evtFut);
                    } else {
                        if (escDisponivel) {
                            //escalonador adiciona nova tarefa
                            escalonador.adicionarTarefa(cliente);
                            //como o escalonador está disponível vai executar o escalonamento diretamente
                            executarEscalonamento();
                            escDisponivel = false;
                        } else {
                            //escalonador apenas adiciona a tarefa
                            escalonador.adicionarTarefa(cliente);
                        }
                    }
                }
            }
    }

     
    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        return;
    }

    @Override
     public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
         cliente.setUltimoCS(this);
        //trecho de debbuging
           EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.CHEGADA,
                    cliente.getCaminho().remove(0), cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
            if (tipoEscalonamento == ENQUANTO_HOUVER_TAREFAS || tipoEscalonamento == AMBOS) {
                //se fila de tarefas do servidor não estiver vazia escalona proxima tarefa
                if (!escalonador.getFilaTarefas().isEmpty()) {
                    executarEscalonamento();
                } else {
                    this.escDisponivel = true;
                }
            }
            
            getMetrica().atualizarMapaDados(cliente.getLocalProcessamento().getId(), cliente.getTamComunicacao());
        
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem mensagem, int tipo) {
        if (tipo == EventoFuturo.ESCALONAR) {
           escalonador.escalonar();
        } else if (mensagem != null) {
            if (mensagem.getTipo() == Mensagens.ATUALIZAR) {
                atenderAtualizacao(simulacao, mensagem);
            } else if (mensagem.getTipo() == Mensagens.ALOCAR_ACK) {
                atenderAckAlocacao(simulacao, mensagem);

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
            } else if (mensagem.getTipo() == Mensagens.RESULTADO_ATUALIZAR) {
                atenderRetornoAtualizacao(simulacao, mensagem);
            } else if (mensagem.getTarefa() != null) {
                //encaminhando mensagem para o destino
                this.enviarMensagem(mensagem.getTarefa(), (CS_Processamento) mensagem.getTarefa().getLocalProcessamento(), mensagem.getTipo());
            }
        }
        //deve incluir requisição para alocar..
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
    
    //métodos do Mestre
    @Override
    public void enviarTarefa(Tarefa tarefa) {
        //Gera evento para atender proximo cliente da lista
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.SAÍDA,
                this, tarefa);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
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
    public void atenderAtualizacao(Simulacao simulacao, Mensagem mensagem) {
        //enviar resultados
        
        List<CentroServico> caminho = new ArrayList<CentroServico>((List<CentroServico>) caminhoVMM);
        Mensagem novaMensagem = new Mensagem(this, mensagem.getTamComunicacao(), Mensagens.RESULTADO_ATUALIZAR);
        //Obtem informações dinâmicas
        novaMensagem.setProcessadorEscravo(new ArrayList<Tarefa>(tarefaEmExecucao));
        novaMensagem.setFilaEscravo(new ArrayList<Tarefa>(filaTarefas));
        novaMensagem.setCaminho(caminho);
        novaMensagem.setUltimoCS(this);
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.MENSAGEM,
                novaMensagem.getCaminho().remove(0),
                novaMensagem);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void atenderRetornoAtualizacao(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    public CS_VMM getVmmResponsavel() {
        return vmmResponsavel;
    }
    
    public List<CS_VMM> getVMMsIntermediarios(){
        return this.VMMsIntermediarios;
    }

    public List<List> getCaminhoIntermediarios() {
        return caminhoIntermediarios;
    }
    
    public void addIntermediario(CS_VMM aux){
       this.VMMsIntermediarios.add(aux);
    }
    
    public void addCaminhoIntermediario(int i, List<CentroServico> caminho){
        this.caminhoIntermediarios.add(i, caminho);
    }

       public int getProcessadoresDisponiveis() {
        return processadoresDisponiveis;
    }

    public void setProcessadoresDisponiveis(int processadoresDisponiveis) {
        this.processadoresDisponiveis = processadoresDisponiveis;
    }

    public double getPoderProcessamento() {
        return poderProcessamento;
    }

    public void setPoderProcessamentoPorNucleo(double poderProcessamento) {
        super.setPoderComputacionalDisponivelPorProcessador(poderProcessamento);
        super.setPoderComputacional(poderProcessamento);
    }

    public double getMemoriaDisponivel() {
        return memoriaDisponivel;
    }

    public void setMemoriaDisponivel(double memoriaDisponivel) {
        this.memoriaDisponivel = memoriaDisponivel;
    }

    public double getDiscoDisponivel() {
        return discoDisponivel;
    }

    public void setDiscoDisponivel(double discoDisponivel) {
        this.discoDisponivel = discoDisponivel;
    }

    public CS_MaquinaCloud getMaquinaHospedeira() {
        return maquinaHospedeira;
    }

    public void setMaquinaHospedeira(CS_MaquinaCloud maquinaHospedeira) {
        this.maquinaHospedeira = maquinaHospedeira;
        this.getMetrica().setHostid(maquinaHospedeira.getId());
    }

    public List<CentroServico> getCaminhoVMM() {
        return caminhoVMM;
    }

    public void setCaminhoVMM(List<CentroServico> caminhoMestre) {
        this.caminhoVMM = caminhoMestre;        
    }

     public void addVMM(CS_VMM vmmResponsavel) {
        this.vmmResponsavel = vmmResponsavel;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public MetricasCusto getMetricaCusto() {
        return metricaCusto;
    }
    
        
    @Override
    public void determinarCaminhos() throws LinkageError {
        List<CS_Processamento> escravos = getVmmResponsavel().getAlocador().getMaquinasFisicas(); //lista de maquinas fisicas
        //Instancia objetos
        caminhoEscravo = new ArrayList<List>(escravos.size());
        //Busca pelo melhor caminho
        for (int i = 0; i < escravos.size(); i++) {
            if(!maquinaHospedeira.equals(escravos.get(i)))
                caminhoEscravo.add(i, CS_VMM.getMenorCaminhoCloud(this.getMaquinaHospedeira(), escravos.get(i)));
        }
        //verifica se todos os escravos são alcansaveis
        for (int i = 0; i < escravos.size(); i++) {
            if (caminhoEscravo.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }

        escalonador.setMaqFisicas(escravos);
        escalonador.setCaminhoMaquinas(caminhoEscravo);
    }


    @Override
    public Object getConexoesSaida() {
        return maquinaHospedeira.getConexoesSaida();
    }

    @Override
    public Integer getCargaTarefas() {
         if (falha) {
            return -100;
        } else {
            return (filaTarefas.size() + tarefaEmExecucao.size());
        }
    }
 
    @Override
    public void atenderAckAlocacao(Simulacao simulacao, Mensagem mensagem) {
        TarefaVM trf = (TarefaVM) mensagem.getTarefa();
        CS_VirtualMac auxVM = (CS_VirtualMac)trf.getVM_enviada();
        CS_MaquinaCloud auxMaq = auxVM.getMaquinaHospedeira();
        ArrayList<CentroServico> caminho;
        caminho = new ArrayList<CentroServico>(getMenorCaminhoCloud(this, auxMaq));
        determinarCaminhoVM(auxVM, caminho);
        auxVM.setStatus(CS_VirtualMac.ALOCADA);
        if (this.vmsAlocadas == false) {
            this.vmsAlocadas = true;
            atualizarEscalonador(auxVM);
        }
    }
    
    public void atenderAckAlocacao(CS_VirtualMac auxVM) {
        //Atende ACK por loopback
        CS_MaquinaCloud auxMaq = auxVM.getMaquinaHospedeira();
        //tratar o ack
        //primeiro encontrar o caminho pra máquina onde a vm está alocada
        ArrayList<CentroServico> caminho;
        
        if(caminhoEscravos.containsKey(auxMaq.getId()))
            caminho = new ArrayList<CentroServico>(caminhoEscravos.get(auxMaq.getId()));
        else{
            caminhoEscravos.put(auxVM.getId(), getMenorCaminhoCloud(this, auxMaq));
            caminho = new ArrayList<CentroServico>(caminhoEscravos.get(auxVM.getId()));
        }
        
        if (this.vmsAlocadas == false) {
            this.vmsAlocadas = true;
        }
        atualizarEscalonador(auxVM);
    }
    
    public void determinarCaminhoVM(CS_VirtualMac vm, ArrayList<CentroServico> caminhoVM) {
        caminhoVMs.put(vm.getId(), caminhoVM);
        escalonador.addCaminhoEscravo(vm.getId(), caminhoVM);
    }
    
    @Override
    public void atenderDesligamento(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public double getInstanteAloc() {
        return instanteAloc;
    }

    public void setInstanteAloc(double instanteAloc) {
        this.instanteAloc = instanteAloc;
    }

    public double getTempoDeExec() {
        return tempoDeExec;
    }

    public void setTempoDeExec(double tempoDestruir) {
        this.tempoDeExec = tempoDestruir - getInstanteAloc();
    }

    @Override
    public void enviarVM(CS_VirtualMac vm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void executarAlocacao() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void enviarMensagemAlloc(Tarefa tarefa, CS_Processamento maquina, int tipo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atualizarAlloc(CS_Processamento maquina) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getTipoAlocacao() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTipoAlocacao(int tipo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Simulacao getSimulacaoAlloc() {
        return simulacao;
    }

    @Override
    public void processarTarefa(Tarefa tarefa) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void executarEscalonamento() {        
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.ESCALONAR,
                this, null);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void enviarMensagem(Tarefa tarefa, CS_Processamento escravo, int tipo) {
        tarefa.setUltimoCS(this);
        Mensagem msg = new Mensagem(this, tipo, tarefa);
        msg.setCaminho(escalonador.escalonarRota(escravo));
        msg.setUltimoCS(this);
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.MENSAGEM,
                msg.getCaminho().remove(0),
                msg);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void atualizar(CS_Processamento escravo) {
        Mensagem msg = new Mensagem(this, 0.011444091796875, Mensagens.ATUALIZAR);
        msg.setCaminho(escalonador.escalonarRota(escravo));
        msg.setUltimoCS(this);
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.MENSAGEM,
                msg.getCaminho().remove(0),
                msg);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    public void atualizar(CS_Processamento escravo, Double time) {
        Mensagem msg = new Mensagem(this, 0.011444091796875, Mensagens.ATUALIZAR);
        msg.setCaminho(escalonador.escalonarRota(escravo));
        msg.setUltimoCS(this);
        EventoFuturo evtFut = new EventoFuturo(
                time,
                EventoFuturo.MENSAGEM,
                msg.getCaminho().remove(0),
                msg);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }
    
    
    public void instanciarCaminhosVMs() {
        
    }

    @Override
    public void liberarEscalonador() {
        escDisponivel = true;
    }

    @Override
    public void setSimulacao(Simulacao simulacao) {
       this.simulacao = simulacao;
    }
    @Override
    public void setSimulacaoAlloc(Simulacao simulacao) {
        this.simulacao = simulacao;
    }
    
    @Override
    public int getTipoEscalonamento() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTipoEscalonamento(int tipo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Tarefa criarCopia(Tarefa get) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Simulacao getSimulacao() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public EscalonadorCloud getEscalonador() {
        return escalonador;
    }
    
    public void addEscravos (CS_VirtualMac escravo){
        escravos.add(escravo);
        escravosNaoAlocados.add(escravo);
    }
    
    public void atualizarEscalonador(){
        for(int i = 0; i < escravosNaoAlocados.size(); i++)
        {
            if(escravosNaoAlocados.get(i).getStatus() != LIVRE)
            {
                CS_VirtualMac vm = escravosNaoAlocados.remove(i);
                escalonador.addEscravo(vm);
                escalonador.addCaminhoEscravo(vm.getId(), caminhoVMs.get(vm.getId()));
                if(vm.getStatus() == ALOCADA)
                    atenderAckAlocacao(vm);
                
                i--;
            }
        }
        if(escravosNaoAlocados.isEmpty())
            if(escalonador.getFilaTarefas().isEmpty())
                escDisponivel = true;
            else
                executarEscalonamento();
    }
    
    public void atualizarEscalonador(CS_VirtualMac vm){
        escravosNaoAlocados.remove(vm);
        escalonador.addEscravo(vm);
        if(escravosNaoAlocados.isEmpty())
            if(escalonador.getFilaTarefas().isEmpty())
                escDisponivel = true;
            else
                executarEscalonamento();
    }
    
    public String getUsuario(){
        return super.getProprietario();
    }
    
    @Override
    public double getTempoFinal(){
        return tempofinal;
    }
    
    public void setTempoFinal(double t){
        tempofinal = t;
    }
}
