/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc.TAVMPutils;

import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;

/**
 *
 * @author João Antonio Magri Rodrigues
 */
public class Slot {
    private int pmIndex;
    private CS_MaquinaCloud pm;

    public int getPmIndex() {
        return pmIndex;
    }

    public void setPmIndex(int pmIndex) {
        this.pmIndex = pmIndex;
    }

    public CS_MaquinaCloud getPm() {
        return pm;
    }

    public void setPm(CS_MaquinaCloud pm) {
        this.pm = pm;
    }

    public Slot(int pmIndex, CS_MaquinaCloud pm) {
        this.pmIndex = pmIndex;
        this.pm = pm;
    }
    
    
    
}
