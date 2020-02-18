/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor.metricas;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Cada centro de serviço usado para processamento deve ter um objeto desta classe
 * Responsavel por armazenar o total de processamento realizado em MFlops e segundos
 * @author denison_usuario
 */
public class MetricasProcessamento implements Serializable{
    /**
     * Armazena o total de processamento realizado em MFlops
     */
    private double MFlopsProcessados;
    /**
     * armazena o total de processamento realizado em segundos
     */
    private double SegundosDeProcessamento;
    private String id;
    private String proprietario;
    private int numeroMaquina;
    private boolean falha;
    private double tempoFalha;
    private double tempoRecuperacao;
    private String hostid;
    private double fatorSLA = -1;
    private HashMap<String, Double> mapaDados = new HashMap<String, Double>();
    
    public MetricasProcessamento(String id, int numeroMaquina, String proprietario) {
        this.MFlopsProcessados = 0;
        this.SegundosDeProcessamento = 0;
        this.id = id;
        this.numeroMaquina = numeroMaquina;
        this.proprietario = proprietario;
        falha = false;
        tempoFalha = -1;
    }

    public void incMflopsProcessados(double MflopsProcessados) {
        this.MFlopsProcessados += MflopsProcessados;
    }

    public void incSegundosDeProcessamento(double SegundosProcessados) {
        this.SegundosDeProcessamento += SegundosProcessados;
    }

    public double getMFlopsProcessados() {
        return MFlopsProcessados;
    }

    public double getSegundosDeProcessamento() {
        return SegundosDeProcessamento;
    }

    public String getId() {
        return id;
    }

    public String getProprietario() {
        return proprietario;
    }

    public int getnumeroMaquina() {
        return numeroMaquina;
    }

    void setMflopsProcessados(double d) {
        this.MFlopsProcessados = d;
    }

    void setSegundosDeProcessamento(double d) {
        this.SegundosDeProcessamento = d;
    }

    public boolean isFalha() {
        return falha;
    }

    public void setFalha(boolean falha) {
        this.falha = falha;
    }

    public int getNumeroMaquina() {
        return numeroMaquina;
    }

    public void setNumeroMaquina(int numeroMaquina) {
        this.numeroMaquina = numeroMaquina;
    }

    public double getTempoFalha() {
        return tempoFalha;
    }

    public void setTempoFalha(double tempoFalha) {
        this.tempoFalha = tempoFalha;
    }

    public double getTempoRecuperacao() {
        return tempoRecuperacao;
    }

    public void setTempoRecuperacao(double tempoRecuperacao) {
        this.tempoRecuperacao = tempoRecuperacao;
    }

    public String getHostid() {
        return hostid;
    }

    public void setHostid(String hostid) {
        this.hostid = hostid;
    }    

    public double getFatorSLA() {
        return fatorSLA;
    }

    public void setFatorSLA(double fatoSLA) {
        this.fatorSLA = fatoSLA;
    }
    
    public void atualizarMapaDados(String id, double incremento){
        double dados = incremento;
        
        if(mapaDados.containsKey(id))
            dados += mapaDados.get(id);
        
        mapaDados.put(id, dados);
    }

    public HashMap<String, Double> getMapaDados() {
        return mapaDados;
    }
    
    
}