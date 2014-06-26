/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.iconico;

/**
 *
 * @author denison
 */
public abstract class Aresta implements Icone {

    private Vertice origem;
    private Vertice destino;

    public Aresta(Vertice origem, Vertice destino) {
        this.origem = origem;
        this.destino = destino;
    }

    public Vertice getOrigem() {
        return origem;
    }

    public Vertice getDestino() {
        return destino;
    }
    
    public void setPosition(Vertice origem, Vertice destino) {
        this.origem = origem;
        this.destino = destino;
    }
    
    /**
     * Posição central da aresta no eixo X
     * @return 
     */
    @Override
    public Integer getX() {
        return getOrigem().getX() + (getDestino().getX() - getOrigem().getX()) / 2;
    }
    
    /**
     * Posição central da aresta no eixo Y
     * @return 
     */
    @Override
    public Integer getY() {
        return getOrigem().getY() + (getDestino().getY() - getOrigem().getY()) / 2;
    }
}