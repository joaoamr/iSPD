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

/**
 *
 * @author Diogo Tavares
 */
public class RoundRobin extends AlocadorGlobal {

    private ListIterator<CS_Processamento> maqFisica;
   

    public RoundRobin() {
        
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
            num_escravos = maquinasFisicas.size();
            

            CS_VirtualMac auxVM = escalonarVM();

            while (num_escravos >= 0) {
                if (num_escravos > 0) {//caso existam máquinas livres
                    CS_Processamento auxMaq = escalonarRecurso(); //escalona o recurso
                    if (auxMaq instanceof CS_VMM) {
                        
                        auxVM.setCaminho(escalonarRota(auxMaq));
                        //salvando uma lista de VMMs intermediarios no caminho da vm e seus respectivos caminhos
                        //CS_VMM maq = (CS_VMM) auxMaq;
                        //auxVM.addIntermediario(maq);
                        //List<CS_VMM> inter = auxVM.getVMMsIntermediarios();
                        //int index = inter.indexOf((CS_VMM) auxMaq);
                        //ArrayList<CentroServico> caminhoInter = new ArrayList<CentroServico>(escalonarRota(auxMaq));
                        //auxVM.addCaminhoIntermediario(index, caminhoInter);
                        VMM.enviarVM(auxVM);
                        break;
                    } else {
                        CS_MaquinaCloud maq = (CS_MaquinaCloud) auxMaq;
                        AlocadorLocal local = maq.getAlocador();
                        
                        if (local.encaixarVm(auxVM)) {
                            local.adicionarVm(auxVM);
                            auxVM.setCaminho(escalonarRota(auxMaq));
                            VMM.enviarVM(auxVM);
  
                            break;

                        } else {
                            num_escravos--;
                        }
                    }
                } else {
                    auxVM.setStatus(CS_VirtualMac.REJEITADA);
                    VMsRejeitadas.add(auxVM);
                    num_escravos--;
                }
            }
        }

    }

    @Override
    public void migrarVM() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
