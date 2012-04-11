/*  
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui;

/**
 *
 * @author dancosta
 */
public class poderComputacionalTotal {
   
    double soma;
    
    public poderComputacionalTotal()
    {
        
        this.soma = 0;
        
    }
    
    public void adiciona_maq_poder(double valor_proc)
    {
       
        this.soma+=valor_proc;
    }
    
    public double getSoma()
    {
        return this.soma;
    }
    
  
}
