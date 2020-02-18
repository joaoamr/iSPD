/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc.global;

import ispd.alocacaoVM.AlocadorGlobal;
import ispd.alocacaoVM.AlocadorLocal;
import ispd.motor.Simulacao;
import ispd.motor.filas.TarefaVM;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

/**
 *
 * @author Diogo Tavares
 */
public class RandomPlacement extends AlocadorGlobal {

    private ListIterator<CS_Processamento> maqFisica;
   

    public RandomPlacement() {
        
        this.maquinasVirtuais = new ArrayList<CS_VirtualMac>();
        this.maquinasFisicas = new LinkedList<CS_Processamento>();
        this.VMsRejeitadas = new ArrayList<CS_VirtualMac>();
    }

    @Override
    public void iniciar(Simulacao simulacao) {
     //trecho de teste.. excluir depois
        //fim do trecho de teste
        maqFisica = maquinasFisicas.listIterator(0);
        if (!maquinasVirtuais.isEmpty()) {

            escalonar();
        }
    }

    @Override
    public CS_VirtualMac escalonarVM() {
        return maquinasVirtuais.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if (maqFisica.hasNext()) {
            return maqFisica.next();
        } else {
            maqFisica = maquinasFisicas.listIterator(0);
            return maqFisica.next();
        }
    }

    @Override
    public void escalonar() {
        

        while (!(maquinasVirtuais.isEmpty())) {
            int num_escravos;
            ArrayList<CS_Processamento> hosts = new ArrayList<CS_Processamento>(maquinasFisicas);
            Random rnd = new Random();

            CS_VirtualMac vm = maquinasVirtuais.remove(0);
            
            while(!hosts.isEmpty()){
                CS_MaquinaCloud host = (CS_MaquinaCloud)hosts.remove(rnd.nextInt(hosts.size()));
                if(fit(vm, host)){
                    vm.setCaminho(escalonarRota(host));
                    vm.setStatus(CS_VirtualMac.LIVRE);
                    AlocadorLocal local = ((CS_MaquinaCloud)host).getAlocador();
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
