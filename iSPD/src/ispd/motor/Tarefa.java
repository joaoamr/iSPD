/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor;

/**
 *
 * @author denison_usuario
 */
public class Tarefa {

    int tamanho = 0;
    
    public int tamanhoTarefas() {
        return tamanho;
    }

    /**
     * Retorna Mestre que criou a tarefa
     * @return
     */
    public Recurso getOrigem() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public double getTimeCriacao() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
