/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui;

/**
 *
 * @author dancosta
 */
public class ParesOrdenadosUso {
    Double inicio, fim;

    public ParesOrdenadosUso(double inicio, double fim) {
        this.inicio = inicio;
        this.fim = fim;
    }
    
    public Double getInicio(){
        return this.inicio;
    }
    
    public Double getFim(){
        return this.fim;
    }
    
    
    public String toString(){
        return inicio+" "+fim;
    }
    
}
