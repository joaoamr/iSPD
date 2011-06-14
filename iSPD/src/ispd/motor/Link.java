/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor;

/**
 *
 * @author denison_usuario
 */
public class Link extends Recurso{

    @Override
    public void recebeMensagem(Recurso origem, Object pacote) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void enviarMensagem(Recurso destino, Object pacote) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void criarTarefa(Tarefa tarefa) {
        throw new UnsupportedOperationException("Não é implementado neste recurso.");
    }

}
