/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor.filas;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.metricas.MetricasTarefa;
import java.util.List;

/**
 * Classe que representa o cliente do modelo de filas, ele será atendo pelos centros de serviços
 * Os clientes podem ser: Tarefas
 * @author denison_usuario
 */
public class Tarefa implements Cliente {
    //Estados que a tarefa pode estar
    public static final int PARADO = 1;
    public static final int PROCESSANDO = 2;
    public static final int CANCELADO = 3;
    public static final int CONCLUIDO = 4;
    private static int contador = 0;
    
    private String proprietario;
    private String aplicacao;
    private int identificador;
    private boolean copia;
    private double porcentagemProcessado;
    /**
     * Tamanho do arquivo em Mbits que será enviado para o escravo
     */
    private double arquivoEnvio;
    /**
     * Tamanho do arquivo em Mbits que será devolvido para o mestre
     */
    private double arquivoRecebimento;
    /**
     * Tamanho em Mflops para processar
     */
    private double tamProcessamento;
    /**
     * Local de origem da mensagem/tarefa
     */
    private CentroServico origem;
    /**
     * Local de destino da mensagem/tarefa
     */
    private CentroServico localProcessamento;
    /**
     * Caminho que o pacote deve percorrer até o destino
     * O destino é o ultimo item desta lista
     */
    private List<CentroServico> caminho;
    private double inicioEspera;
    private MetricasTarefa metricas;
    private double tempoCriacao;
    //Criando o tempo em que a tarefa acabou.
    private double tempoFinal;
    //Criando o tempo em que a tarefa começou a ser executada.
    private double tempoInicial;

    private int estado;
    private double tamComunicacao;

    public Tarefa(String proprietario, String aplicacao, CentroServico origem, double arquivoEnvio, double tamProcessamento, double tempoCriacao) {
        this.proprietario = proprietario;
        this.aplicacao = aplicacao;
        this.identificador = Tarefa.contador;Tarefa.contador++;//hashCode();
        this.copia = false;
        this.origem = origem;
        this.tamComunicacao = arquivoEnvio;
        this.arquivoEnvio = arquivoEnvio;
        this.arquivoRecebimento = 0;
        this.tamProcessamento = tamProcessamento;
        this.metricas = new MetricasTarefa();
        this.tempoCriacao = tempoCriacao;
        this.estado = PARADO;
        this.porcentagemProcessado = 0;
        this.tempoInicial = 0;
        this.tempoFinal = 0;
    }
    
    public Tarefa(String proprietario, String aplicacao, CentroServico origem, double arquivoEnvio, double arquivoRecebimento, double tamProcessamento, double tempoCriacao) {
        this.proprietario = proprietario;
        this.aplicacao = aplicacao;
        this.identificador = Tarefa.contador;Tarefa.contador++;//hashCode();
        this.copia = false;
        this.origem = origem;
        this.tamComunicacao = arquivoEnvio;
        this.arquivoEnvio = arquivoEnvio;
        this.arquivoRecebimento = arquivoRecebimento;
        this.tamProcessamento = tamProcessamento;
        this.metricas = new MetricasTarefa();
        this.tempoCriacao = tempoCriacao;
        this.estado = PARADO;
        this.porcentagemProcessado = 0;
        this.tempoInicial = 0;
        this.tempoFinal = 0;
    }
    
    public Tarefa(Tarefa tarefa){
        this.proprietario = tarefa.proprietario;
        this.aplicacao = tarefa.getAplicacao();
        this.identificador = tarefa.identificador;
        this.copia = true;
        this.origem = tarefa.getOrigem();
        this.tamComunicacao = tarefa.arquivoEnvio;
        this.arquivoEnvio = tarefa.arquivoEnvio;
        this.arquivoRecebimento = tarefa.arquivoRecebimento;
        this.tamProcessamento = tarefa.getTamProcessamento();
        this.metricas = new MetricasTarefa();
        this.tempoCriacao = tarefa.getTimeCriacao();
        this.estado = PARADO;
        this.porcentagemProcessado = 0;
        this.tempoInicial = 0;
        this.tempoFinal = 0;
    }
    
    public double getTamComunicacao() {
        return tamComunicacao;
    }
    
    public double getTamProcessamento() {
        return tamProcessamento;
    }

    public String getProprietario() {
        return proprietario;
    }

    public CentroServico getOrigem() {
        return origem;
    }
    
    public CentroServico getLocalProcessamento() {
        return localProcessamento;
    }
    
    public CS_Processamento getCSLProcessamento() {
        return (CS_Processamento) localProcessamento;
    }
    
    public List<CentroServico> getCaminho() {
        return caminho;
    }

    public void setLocalProcessamento(CentroServico localProcessamento) {
        this.localProcessamento = localProcessamento;
    }
    
    public void setCaminho(List<CentroServico> caminho) {
        this.caminho = caminho;
    }

    public void iniciarEsperaComunicacao(double tempo) {
        this.inicioEspera = tempo;
    }

    public void finalizarEsperaComunicacao(double tempo) {
        this.metricas.incTempoEsperaComu(tempo - inicioEspera);
    }

    public void iniciarAtendimentoComunicacao(double tempo) {
        this.inicioEspera = tempo;
    }

    public void finalizarAtendimentoComunicacao(double tempo) {
        this.metricas.incTempoComunicacao(tempo - inicioEspera);
    }

    public void iniciarEsperaProcessamento(double tempo) {
        this.inicioEspera = tempo;
    }

    public void finalizarEsperaProcessamento(double tempo) {
        this.metricas.incTempoEsperaProc(tempo - inicioEspera);
    }

    public void iniciarAtendimentoProcessamento(double tempo) {
        this.estado = PROCESSANDO;
        this.inicioEspera = tempo;
        this.tempoInicial = tempo;
    }

    public void finalizarAtendimentoProcessamento(double tempo) {
        this.estado = CONCLUIDO;
        this.metricas.incTempoProcessamento(tempo - inicioEspera);
        this.tempoFinal = tempo;
        this.tamComunicacao = arquivoRecebimento;
    }
    
    public double cancelar(double tempo) {
        if(estado == PARADO || estado == PROCESSANDO){
            this.estado = CANCELADO;
            this.metricas.incTempoProcessamento(tempo - inicioEspera);
            this.tempoFinal = tempo;
            return inicioEspera;
        }else{
            this.estado = CANCELADO;
            return tempo;
        }
    }
    
    public void calcEficiencia(double capacidadeRecebida){
        this.metricas.calcEficiencia(capacidadeRecebida, tamProcessamento);
    }

    public double getTimeCriacao() {
        return tempoCriacao;
    }
    
    public double getTempoInicial(){
        return tempoInicial;
    }
    
    public double getTempoFinal(){
        return tempoFinal;
    }
    
    public MetricasTarefa getMetricas() {
        return metricas;
    }

    public int getEstado() {
        return this.estado;
    }
    
    public int getIdentificador() {
        return this.identificador;
    }
    public String getAplicacao() {
        return aplicacao;
    }

    public boolean isCopy() {
        return copia;
    }

    public boolean isCopyOf(Tarefa tarefa) {
        if(this.identificador == tarefa.identificador && !this.equals(tarefa)){
            return true;
        }else{
            return false;
        }
    }

    public double getPorcentagemProcessado() {
        return porcentagemProcessado;
    }

    public void setPorcentagemProcessado(double porcentagemProcessado) {
        this.porcentagemProcessado = porcentagemProcessado;
    }

    public static void setContador(int contador) {
        Tarefa.contador = contador;
    }
   
}
