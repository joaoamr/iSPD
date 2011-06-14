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
public class Mestre extends Recurso{

    private List<Recurso> escravos;
    private List<Link> conexoes;

    @Override
    public void recebeMensagem(Recurso origem, Object pacote) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void enviarMensagem(Recurso destino, Object pacote) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void escalonar(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getNome(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void criarTarefa(Tarefa tarefa) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
