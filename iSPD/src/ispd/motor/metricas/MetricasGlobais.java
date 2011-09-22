/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor.metricas;

import ispd.motor.filas.RedeDeFilas;

/**
 *
 * @author denison_usuario
 */
public class MetricasGlobais {
    private double tempoSimulacao;
    private double satisfacaoMedia;
    private double ociosidadeMedia;
    private double eficiencia;
    
    public MetricasGlobais(RedeDeFilas redeDeFilas, double tempoSimulacao){
        this.tempoSimulacao = tempoSimulacao;
        this.satisfacaoMedia = 100;
    }

    public double getEficiencia() {
        return eficiencia;
    }

    public double getOciosidadeMedia() {
        return ociosidadeMedia;
    }

    public double getSatisfacaoMedia() {
        return satisfacaoMedia;
    }

    public double getTempoSimulacao() {
        return tempoSimulacao;
    }
    
}
