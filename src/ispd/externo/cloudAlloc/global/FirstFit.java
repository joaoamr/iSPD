/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc.global;

import ispd.alocacaoVM.AlocadorGlobal;
import ispd.alocacaoVM.AlocadorLocal;
import ispd.motor.Simulacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Diogo Tavares
 */
public class FirstFit extends AlocadorGlobal {

    private boolean fit;
    private int maqIndex;
    private CS_MaquinaCloud [] pms;

    public FirstFit() {
        this.maquinasVirtuais = new ArrayList<CS_VirtualMac>();
        this.maquinasFisicas = new ArrayList<CS_Processamento>();
        this.VMsRejeitadas = new ArrayList<CS_VirtualMac>();

    }

    @Override
    public void iniciar(Simulacao simulacao) {
        pms = new CS_MaquinaCloud[maquinasFisicas.size()];
        
        for(int i = 0; i < maquinasFisicas.size(); i++)
            pms[i] = (CS_MaquinaCloud)maquinasFisicas.get(i);

        escalonar();
    }

    @Override
    public CS_VirtualMac escalonarVM() {
        return maquinasVirtuais.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if (fit) {
            return maquinasFisicas.get(0);
        } else {
            return maquinasFisicas.get(maqIndex);
        }
    }

    @Override
    public void escalonar() {

        while (!(maquinasVirtuais.isEmpty())) {
            CS_VirtualMac vm = maquinasVirtuais.remove(0);
            
            for(int i = 0; i < pms.length; i++){
                if(fit(vm, pms[i])){
                    vm.setCaminho(escalonarRota(pms[i]));
                    vm.setStatus(CS_VirtualMac.LIVRE);
                    AlocadorLocal local = this.pms[i].getAlocador();
                    local.adicionarVm(vm);
                    VMM.enviarVM(vm);
                    break;
                }
            }
        }
            
    }

    @Override
    public void migrarVM() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private boolean fit(CS_VirtualMac vm, CS_MaquinaCloud pm){
        if(pm.getDiscoDisponivel() >= vm.getDiscoDisponivel())
            if(pm.getMemoriaDisponivel() >= vm.getMemoriaDisponivel())
                if(pm.getPoderDisponivel() >= vm.getPoderNecessario())
                    return true;
        
        return false;
                
    }
}
