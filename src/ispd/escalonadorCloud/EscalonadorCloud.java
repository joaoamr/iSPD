/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.escalonadorCloud;

import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.motor.metricas.MetricasUsuarios;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Diogo Tavares
 */
public abstract class EscalonadorCloud {

    protected List<CS_Processamento> escravos;
    protected List<CS_Processamento> maqFisicas;
    protected List<List> filaEscravo;
    protected List<Tarefa> tarefas;
    protected MetricasUsuarios metricaUsuarios;
    protected MestreCloud mestre;
    /**
     * Armazena os caminhos possiveis para alcançar cada escravo
     */
    protected List<List> caminhoEscravo;
    protected List<List> caminhoMaquinas;
    private HashSet<String> indices = new HashSet<String>();
    protected HashMap<CentroServico, List<CentroServico>> mapaCaminhos = new HashMap<CentroServico, List<CentroServico>>();
    
    public List<CentroServico> escalonarRota(CentroServico destino) {
        if(mapaCaminhos.containsKey(destino))
            return new ArrayList<CentroServico>((List<CentroServico>) mapaCaminhos.get(destino));
        
        List<CentroServico> caminho = CS_Processamento.getMenorCaminho((CS_Processamento)mestre, (CS_Processamento)destino);
        
        mapaCaminhos.put(destino, caminho);
        
        return new ArrayList<CentroServico>(caminho);
    }

    //Métodos
    public abstract void iniciar();

    public abstract Tarefa escalonarTarefa();

    public abstract CS_Processamento escalonarRecurso();


    public abstract void escalonar();

    public void adicionarTarefa(Tarefa tarefa) {
        if (tarefa.getOrigem().equals(mestre)) {
            this.metricaUsuarios.incTarefasSubmetidas(tarefa);
        }
        this.tarefas.add(tarefa);
    }

    //Get e Set
    public List<CS_Processamento> getEscravos() {
        return escravos;
    }

    public void setCaminhoEscravo(List<List> caminhoEscravo) {
        this.caminhoEscravo = caminhoEscravo;
    }

    public void addEscravo(CS_Processamento vm) {
        if(!indices.contains(vm.getId())){
            this.escravos.add(vm);
            this.indices.add(vm.getId());
        }
    }

    public void addTarefaConcluida(Tarefa tarefa) {
        //if (tarefa.getOrigem().equals(mestre)) {
            this.metricaUsuarios.incTarefasConcluidas(tarefa);
        //}
        //System.out.println("Tarefa concluida: " + tarefa.getIdentificador());
    }

    public List<Tarefa> getFilaTarefas() {
        return this.tarefas;
    }

    public MetricasUsuarios getMetricaUsuarios() {
        return metricaUsuarios;
    }

    public void setMetricaUsuarios(MetricasUsuarios metricaUsuarios) {
        this.metricaUsuarios = metricaUsuarios;
    }

    public void setMestre(MestreCloud mestre) {
        this.mestre = mestre;
    }

    public List<CS_Processamento> getMaqFisicas() {
        return maqFisicas;
    }
   
    public void setMaqFisicas(List<CS_Processamento> maqFisicas) {
        this.maqFisicas = maqFisicas;
    }

    public List<List> getCaminhoMaquinas() {
        return caminhoMaquinas;
    }

    public void setCaminhoMaquinas(List<List> caminhoMaquinas) {
        this.caminhoMaquinas = caminhoMaquinas;
    }
    
    
    public void addCaminhoEscravo(List caminho){
        this.caminhoEscravo.add(caminho);
    }
    
    public void addCaminhoEscravo(String id, List caminho){
        this.caminhoEscravo.add(caminho);
    }
    
    public List<List> getCaminhoEscravo() {
        return caminhoEscravo;
    }
    
    public List<CS_Processamento> getVMsAdequadas(String usuario, List<CS_Processamento> Escravos){
        LinkedList<CS_Processamento> escravosUsuario = new LinkedList<CS_Processamento>();
        for(CS_Processamento slave : Escravos){
            CS_VirtualMac slaveVM = (CS_VirtualMac) slave;
            
            if (slave.getProprietario().equals(usuario) && slaveVM.getStatus()==CS_VirtualMac.ALOCADA) {
                escravosUsuario.add(slave);
            } 
        }
        return escravosUsuario;
    }

    /**
     * Indica o intervalo de tempo utilizado pelo escalonador para realizar
     * atualização dos dados dos escravos Retornar null para escalonadores
     * estáticos, nos dinâmicos o método deve ser reescrito
     *
     * @return Intervalo em segundos para atualização
     */
    public Double getTempoAtualizar() {
        return null;
    }

    public void resultadoAtualizar(Mensagem mensagem) {
        int index = escravos.indexOf(mensagem.getOrigem());
        filaEscravo.set(index, mensagem.getFilaEscravo());
    }
}
