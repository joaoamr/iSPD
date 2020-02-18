/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor.metricas;

import java.io.Serializable;

/**
 * Cada centro de serviço usado para conexão deve ter um objeto desta classe
 * Responsavel por armazenar o total de comunicação realizada em Mbits e segundos
 * @author denison_usuario
 */
public class MetricasComunicacao implements Serializable {
    /**
     * Armazena o total de comunicação realizada em Mbits
     */
    private double MbitsTransmitidos;
    /**
     * Armazena o total de comunicação realizada em segundos
     */
    private double SegundosDeTransmissao;
    private String id;
    private double tempofalha;
    private double temporecuperacao;
    
    public MetricasComunicacao(String id) {
        this.id = id;
        this.MbitsTransmitidos = 0;
        this.SegundosDeTransmissao = 0;
        this.tempofalha = -1;
        this.temporecuperacao = -1;
    }

    public void incMbitsTransmitidos(double MbitsTransmitidos) {
        this.MbitsTransmitidos += MbitsTransmitidos;
    }

    public void incSegundosDeTransmissao(double SegundosDeTransmissao) {
        this.SegundosDeTransmissao += SegundosDeTransmissao;
    }

    public double getMbitsTransmitidos() {
        return MbitsTransmitidos;
    }

    public double getSegundosDeTransmissao() {
        return SegundosDeTransmissao;
    }

    public String getId() {
        return id;
    }

    void setMbitsTransmitidos(double d) {
        this.MbitsTransmitidos = d;
    }

    void setSegundosDeTransmissao(double d) {
        this.SegundosDeTransmissao = d;
    }

    public double getTempoFalha() {
        return tempofalha;
    }

    public void setTempoFalha(double tempofalha) {
        this.tempofalha = tempofalha;
    }

    public double getTempoRecuperacao() {
        return temporecuperacao;
    }

    public void setTempoRecuperacao(double temporecuperacao) {
        this.temporecuperacao = temporecuperacao;
    }
    
    
}
