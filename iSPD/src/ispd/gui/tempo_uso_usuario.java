/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui;

/**
 *
 * @author dancosta
 */
public class tempo_uso_usuario {

    private Double tempo, uso_no;
    private Boolean tipo;
    
    public tempo_uso_usuario(Double tempo, Boolean tipo, Double uso) {
       
       this.tempo = tempo;
       this.uso_no = uso;
       this.tipo = tipo;
    }
    
    public Double get_tempo()
    {
        return this.tempo;
    }
    
    public Boolean get_tipo()
    {
        return this.tipo;
    }
    
    public Double get_uso_no()
    {
        return this.uso_no;
    }
}
