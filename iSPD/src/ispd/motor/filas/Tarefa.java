/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor.filas;

import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.metricas.MetricasTarefa;
import java.util.List;

/**
 * Classe que representa o cliente do modelo de filas, ele será atendo pelos centros de serviços
 * Os clientes podem ser: Tarefas
 * @author denison_usuario
 */
public class Tarefa {
    //Estados que a tarefa pode estar
    public static final int PARADO = 1;
    public static final int PROCESSANDO = 2;
    public static final int CANCELADO = 3;
    public static final int CONCLUIDO = 4;
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
     * Caminho que o pacote deve percorrer até o destino
     * O destino é o ultimo item desta lista
     */
    private List<CentroServico> caminho;
    private double inicioEspera;
    private MetricasTarefa metricas;
    private double tempoCriacao;
    public int estado;
    private double tamComunicacao;

    public Tarefa(CentroServico origem, double arquivoEnvio, double tamProcessamento, double tempoCriacao) {
        this.origem = origem;
        this.tamComunicacao = arquivoEnvio;
        this.arquivoEnvio = arquivoEnvio;
        this.arquivoRecebimento = 0;
        this.tamProcessamento = tamProcessamento;
        this.metricas = new MetricasTarefa();
        this.tempoCriacao = tempoCriacao;
        this.estado = PARADO;
    }
    
    public Tarefa(CentroServico origem, double arquivoEnvio, double arquivoRecebimento, double tamProcessamento, double tempoCriacao) {
        this.origem = origem;
        this.tamComunicacao = arquivoEnvio;
        this.arquivoEnvio = arquivoEnvio;
        this.arquivoRecebimento = arquivoRecebimento;
        this.tamProcessamento = tamProcessamento;
        this.metricas = new MetricasTarefa();
        this.tempoCriacao = tempoCriacao;
        this.estado = PARADO;
    }
    
    public double getTamComunicacao() {
        return tamComunicacao;
    }
    
    public double getTamProcessamento() {
        return tamProcessamento;
    }

    public CentroServico getOrigem() {
        return origem;
    }

    public List<CentroServico> getCaminho() {
        return caminho;
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
    }

    public void finalizarAtendimentoProcessamento(double tempo) {
        this.estado = CONCLUIDO;
        this.metricas.incTempoProcessamento(tempo - inicioEspera);
        this.tamComunicacao = arquivoRecebimento;
    }

    public double getTimeCriacao() {
        return tempoCriacao;
    }

    public MetricasTarefa getMetricas() {
        return metricas;
    }

    public int getEstado() {
        return this.estado;
    }

}
