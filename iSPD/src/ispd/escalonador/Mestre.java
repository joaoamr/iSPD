/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.escalonador;

import ispd.motor.Simulacao;
import ispd.motor.filas.Tarefa;

/**
 * Interface que possui métodos implementados penas em um nó Mestre,
 * os métodos desta interface são utilizados pelos escalonadores
 * @author denison
 */
public interface Mestre {
    //Métodos que geram eventos
    public void enviarTarefa(Tarefa tarefa);
    public void processarTarefa(Tarefa tarefa);
    public void executarEscalonamento();
    //Get e Set
    public void setSimulacao(Simulacao simulacao);
}
