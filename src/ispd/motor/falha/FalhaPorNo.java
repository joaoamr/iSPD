/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.falha;

import ispd.motor.Simulacao;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.servidores.CentroServico;
import java.util.HashMap;
import java.util.List;

public class FalhaPorNo extends Falha {
    HashMap<String, int[]> maquinasfalhadas;

    public FalhaPorNo(HashMap<String, int[]> maquinasfalhadas) {
        this.maquinasfalhadas = maquinasfalhadas;
    }
    
    @Override
    public void gerarFalha(List<CentroServico> cslist, RedeDeFilas rdf, Simulacao sim) {
        for(CentroServico cs : cslist){
            int t[];
            t = maquinasfalhadas.get(cs.getId());
            if(t != null){
                cs.setInicioFalha(t[0]);
                cs.setFimFalha(t[1]);
                
                for(int i  = 0; i < rdf.getMaquinas().size(); i++){
                    notificarCS(rdf.getMaquinas().get(i), cs, t[0], t[1], sim);
                }
        
                for(int i  = 0; i < rdf.getMestres().size(); i++){
                    notificarCS(rdf.getMestres().get(i), cs, t[0], t[1], sim);
                }
        
                for(int i  = 0; i < rdf.getLinks().size(); i++){
                    notificarCS(rdf.getLinks().get(i), cs, t[0], t[1], sim);
                }   
                
            }
        }
    }
    
}
