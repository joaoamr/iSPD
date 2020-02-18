/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

import ispd.motor.EventoFuturo;
import ispd.motor.Mensagens;
import ispd.motor.Simulacao;
import ispd.motor.SimulacaoSequencial;
import ispd.motor.filas.Cliente;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.metricas.MetricasCusto;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Diogo Tavares
 */
public class CS_VirtualMacOriginal extends CS_Processamento implements Mensagens {
    
    public static final int LIVRE = 1;
    public static final int ALOCADA = 2;
    public static final int REJEITADA = 3;
    public static final int DESTRUIDA = 4;
    public static final int HPCVM = 5;
    public static final int MISCVM = 7;
    
    //Lista de atributos
    private CS_MasterVirtualMac vmMestre;
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
    private List<CentroServico> caminhoMestre;
    private int type = MISCVM;
    
    
    
    
    /**
     * @author Diogo Tavares
     * 
     * @param id
     * @param proprietario
     * @param PoderComputacional
     * @param numeroProcessadores
     * @param Ocupacao
     * @param numeroMaquina
     * @param memoria
     * @param disco 
     */ 
    
    public CS_VirtualMacOriginal(String id, String proprietario, int numeroProcessadores, double memoria, double disco, String OS) {
        super(id, proprietario, 0, numeroProcessadores, 0, 0);
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
        this.tarefaEmExecucao = new ArrayList<Tarefa>(numeroProcessadores);
        this.filaTarefas = new ArrayList<Tarefa>();
        this.vmMestre = null;
        this.caminhoMestre = null;
    }

    
     @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {     
        if (cliente.getEstado() != Tarefa.CANCELADO) { //se a tarefa estiver parada ou executando
            cliente.iniciarEsperaProcessamento(simulacao.getTime(this));
            if (processadoresDisponiveis != 0) {
                //indica que recurso está ocupado
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
        cliente.finalizarEsperaProcessamento(simulacao.getTime(this));
        cliente.iniciarAtendimentoProcessamento(simulacao.getTime(this));
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
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMflopsProcessados(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
        //Incrementa o tempo de processamento
        double tempoProc = this.tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoProcessamento(simulacao.getTime(this));
        tarefaEmExecucao.remove(cliente);
        //eficiencia calculada apenas nas classes CS_Maquina
        cliente.calcEficiencia(this.getPoderComputacional());
        //Devolve tarefa para o mestre
        
        CentroServico Origem = cliente.getOrigem();
        
        ArrayList<CentroServico> caminho = null;
        
        if(Origem.equals(vmMestre)){
            cliente.setLocalProcessamento(Origem);
            if(caminhoMestre == null)
                caminhoMestre = CS_VMM.getMenorCaminhoCloud(maquinaHospedeira, vmMestre.getMaquinaHospedeira());
            
            if(caminhoMestre == null){
                caminho = new ArrayList<CentroServico>();
                caminho.add(maquinaHospedeira.getVirtualBridge());
                caminho.add(maquinaHospedeira);
            }
            else
                caminho = new ArrayList<CentroServico>(caminhoMestre);
            
       }else{ 
            if(Origem.equals(this.vmmResponsavel)){
                caminho =  new ArrayList<CentroServico>(caminhoVMM);
            
            }else{
                int index = VMMsIntermediarios.indexOf((CS_VMM) Origem);
                if(index == -1){
                    CS_MaquinaCloud auxMaq = this.getMaquinaHospedeira();
                    ArrayList<CentroServico> caminhoInter = new ArrayList<CentroServico>(getMenorCaminhoIndiretoCloud(auxMaq, (CS_Processamento) Origem));
                    caminho = new ArrayList<CentroServico>(caminhoInter);
                    VMMsIntermediarios.add((CS_VMM) Origem);
                    int idx = VMMsIntermediarios.indexOf((CS_VMM) Origem);
                    caminhoIntermediarios.add(idx, caminhoInter);

                }else{
                    caminho = new ArrayList<CentroServico>(caminhoIntermediarios.get(index));
                }
            }
        }
            
            cliente.setCaminho(caminho);
           
            //Gera evento para chegada da tarefa no proximo servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.CHEGADA,
                    cliente.getCaminho().remove(0),
                    cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        
        if (filaTarefas.isEmpty()) {
            //Indica que está livre
            this.processadoresDisponiveis++;
        } else {
            //Gera evento para atender proximo cliente da lista
            Tarefa proxCliente = filaTarefas.remove(0);
            EventoFuturo NovoEvt = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.ATENDIMENTO,
                    this, proxCliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(NovoEvt);
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
                    case Mensagens.FALHAR:
                        atenderFalha(simulacao, mensagem);
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
    public void atenderAtualizacao(Simulacao simulacao, Mensagem mensagem) {
        //enviar resultados
        List<CentroServico> caminho = new ArrayList<CentroServico>((List<CentroServico>) caminhoVMM);
        Mensagem novaMensagem = new Mensagem(this, mensagem.getTamComunicacao(), Mensagens.RESULTADO_ATUALIZAR);
        //Obtem informações dinâmicas
        novaMensagem.setProcessadorEscravo(new ArrayList<Tarefa>(tarefaEmExecucao));
        novaMensagem.setFilaEscravo(new ArrayList<Tarefa>(filaTarefas));
        novaMensagem.setCaminho(caminho);
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
    
    public void setCaminhoMestre(List<CentroServico> caminhoMestre) {
        this.caminhoMestre = new ArrayList<CentroServico>(caminhoMestre);        
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public MetricasCusto getMetricaCusto() {
        return metricaCusto;
    }
    
        
    @Override
    public void determinarCaminhos() throws LinkageError {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public Object getConexoesSaida() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    public CS_MasterVirtualMac getMestre() {
        return vmMestre;
    }

    public void setMestre(CS_MasterVirtualMac mestre) {
        this.vmMestre = mestre;
    }
    
    public String getUsuario(){
        return super.getProprietario();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    public List<CentroServico> getCaminho() {
        return this.caminho;
    }

  
    public void setCaminho(List<CentroServico> caminho) {
        this.caminho = caminho;
    }
    
}
