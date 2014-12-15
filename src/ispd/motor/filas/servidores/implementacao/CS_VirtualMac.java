/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

import ispd.motor.Mensagens;
import ispd.motor.Simulacao;
import ispd.motor.filas.Cliente;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.List;

/**
 *
 * @author Diogo Tavares
 */
public class CS_VirtualMac extends CS_Processamento implements Cliente, Mensagens {
    
    public static final int LIVRE = 1;
    public static final int ALOCADA = 2;
    public static final int REJEITADA = 3;
    
    //Lista de atributos
    private CS_VMM vmmResponsavel;
    private int processadoresDisponiveis;
    private double poderProcessamento;
    private double memoriaDisponivel;
    private double discoDisponivel;
    private String OS;
    private CS_MaquinaCloud maquinaHospedeira;
    private List<List> caminhoVMM;
    private int status;
    
    
    
    
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
    
    public CS_VirtualMac(String id, String proprietario, int numeroProcessadores, double memoria, double disco, String OS) {
        super(id, proprietario, 0, numeroProcessadores, 0, 0);
        this.memoriaDisponivel = memoria;
        this.discoDisponivel = disco;
        this.OS = OS;
        this.maquinaHospedeira = null;
        this.caminhoVMM = null;
        this.status = LIVRE;
    }

    public CS_VMM getVmmResponsavel() {
        return vmmResponsavel;
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

    public void setPoderProcessamento(double poderProcessamento) {
        this.poderProcessamento = poderProcessamento;
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
    }

    public List<List> getCaminhoVMM() {
        return caminhoVMM;
    }

    public void setCaminhoVMM() {
        this.caminhoVMM = this.maquinaHospedeira.getCaminhoMestre();
        
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

    
    
    
    
    
    
    
    @Override
    public void determinarCaminhos() throws LinkageError {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem cliente, int tipo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getConexoesSaida() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer getCargaTarefas() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getTamComunicacao() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getTamProcessamento() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getTimeCriacao() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CentroServico getOrigem() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<CentroServico> getCaminho() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCaminho(List<CentroServico> caminho) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderCancelamento(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderParada(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderDevolucao(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderDevolucaoPreemptiva(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderAtualizacao(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderRetornoAtualizacao(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderFalha(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
