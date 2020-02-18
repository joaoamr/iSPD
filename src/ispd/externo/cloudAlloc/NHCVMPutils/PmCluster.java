/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc.NHCVMPutils;

import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

import java.util.ArrayList;

/**
 *
 * @author João Antonio Magri Rodrigues
 */
public class PmCluster {
    CS_MaquinaCloud[] machines;
    int cut;
    int size;
    double dim;

    public PmCluster(CS_MaquinaCloud[] machines, int cut) {
        this.machines = machines;
        this.cut = cut;
        size = machines.length;
    }
    
    public double getDim(){
        dim = 0;
        for(int i = 0; i < machines.length; i++)
            dim += machines[i].getMemoriaDisponivel();
        
        return dim;
    }
    
    public boolean constains(CS_MaquinaCloud mac){
    	if(mac == null)
    		return true;
    	
        for(int i = 0; i < machines.length; i++)
            if(mac == machines[i])
                return true;
        
        return false;
    }
    
    public int fit(CS_VirtualMac vm){
    	int n = 0;
    	for(int i = 0; i < machines.length; i++){
    		int k;
    		k = machines[i].getNumeroProcessadores()/vm.getNumeroProcessadores();
    		if(k < machines[i].getMemoriaDisponivel()/vm.getMemoriaDisponivel())
    			k = (int)(machines[i].getMemoriaDisponivel()/vm.getMemoriaDisponivel());
    		if(k < machines[i].getDiscoDisponivel()/vm.getDiscoDisponivel())
    			k = (int)(machines[i].getMemoriaDisponivel()/vm.getMemoriaDisponivel());
    		
    		n+= k;
    	}
    	
    	return n;
    }
    
    public CS_MaquinaCloud[] getMachines(){
    	return machines;
    }
}
