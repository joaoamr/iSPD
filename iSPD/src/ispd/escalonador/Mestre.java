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
    //Tipos de escalonamentos
    public static final int ENQUANTO_HOUVER_TAREFAS = 1;
    public static final int QUANDO_RECEBE_RESULTADO = 2;
    //Métodos que geram eventos
    public void enviarTarefa(Tarefa tarefa);
    public void processarTarefa(Tarefa tarefa);
    public void executarEscalonamento();
    //Get e Set
    public void setSimulacao(Simulacao simulacao);
    public int getTipoEscalonamento();
    public void setTipoEscalonamento(int tipo);
}
