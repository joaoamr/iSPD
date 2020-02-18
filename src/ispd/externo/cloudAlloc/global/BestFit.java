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
public class BestFit extends AlocadorGlobal {

    private boolean fit;
    private int maqIndex;
    private CS_MaquinaCloud [] pms;

    public BestFit() {
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
            CS_VirtualMac auxVM = maquinasVirtuais.remove(0);  
            boolean fit = false;
            for(int i = 0; i < pms.length; i++){
                CS_MaquinaCloud maq = pms[i];
                AlocadorLocal local = maq.getAlocador();
                if (local.encaixarVm(auxVM)) {
                    local.adicionarVm(auxVM);
                    auxVM.setCaminho(escalonarRota(maq));
                    VMM.enviarVM(auxVM);
                    fit = true;
                    break;

                }
            }
            if(!fit){
                auxVM.setStatus(CS_VirtualMac.REJEITADA);
                VMsRejeitadas.add(auxVM);
            }
            
        }
            
    }

    @Override
    public void migrarVM() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
