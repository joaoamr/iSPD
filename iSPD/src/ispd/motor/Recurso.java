/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor;

import java.util.List;

/**
 *
 * @author denison_usuario
 */
public abstract class Recurso {
    public static final int MACHINE = 1;
    public static final int NETWORK = 2;
    public static final int CLUSTER = 3;
    public static final int INTERNET = 4;
    List<Tarefa> tarefas;
    /**
     * Recurso deve definir o que fazer ao receber uma mensagem
     * @param origem local de origem da mensagem
     * @param pacote mensagem recebida
     */
    public abstract void recebeMensagem(Recurso origem, Object pacote);
    /**
     * Recurso deve definir o que fazer para enviar uma mensagem utilizando classe Link
     * @param destino local que a mensagem deve chegar
     * @param pacote mensagem que será enviada
     */
    public abstract void enviarMensagem(Recurso destino, Object pacote);
    public abstract void criarTarefa(Tarefa tarefa);
}