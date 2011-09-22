/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor.metricas;

/**
 * Cada centro de serviço usado para processamento deve ter um objeto desta classe
 * Responsavel por armazenar o total de processamento realizado em MFlops e segundos
 * @author denison_usuario
 */
public class MetricasProcessamento {
    /**
     * Armazena o total de processamento realizado em MFlops
     */
    private double MFlopsProcessados;
    /**
     * armazena o total de processamento realizado em segundos
     */
    private double SegundosDeProcessamento;

    public MetricasProcessamento() {
        this.MFlopsProcessados = 0;
        this.SegundosDeProcessamento = 0;
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
}