/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc;

import ispd.alocacaoVM.Alocacao;
import ispd.motor.filas.TarefaVM;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Diogo Tavares
 */
public class RoundRobin extends Alocacao {

    private ListIterator<CS_Processamento> maqFisica;
    private ArrayList<CS_VirtualMac> VMsRejeitadas;

    public RoundRobin() {
        this.maquinasVirtuais = new ArrayList<CS_VirtualMac>();
        this.maquinasFisicas = new LinkedList<CS_Processamento>();
    }

    @Override
    public void iniciar() {
        System.out.println("Alocador RR iniciado");
        
        //trecho de teste.. excluir depois
        
        if(maquinasVirtuais.isEmpty())
            System.out.println("sem vms setadas");
        
        for(CS_VirtualMac aux : maquinasVirtuais){
            System.out.println(aux.getId());
        }
        
        //fim do trecho de teste
        
        maqFisica = maquinasFisicas.listIterator(0);
        VMsRejeitadas = new ArrayList<CS_VirtualMac>();
        if(!maquinasFisicas.isEmpty() && !maquinasVirtuais.isEmpty()){
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
    public List<CentroServico> escalonarRota(CentroServico destino) {
        System.out.println("entrei no escalonar rota");
        int index = maquinasFisicas.indexOf(destino);
        System.out.println("indice da maquina é:" + index);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoMaquina.get(index));
    }

    @Override
    public void escalonar() {

        while(!(maquinasVirtuais.isEmpty())) {
            int num_escravos;
            num_escravos = maquinasFisicas.size();
           

            CS_VirtualMac auxVM = escalonarVM();

            while (num_escravos >= 0) {
                if (num_escravos > 0) { //caso existam máquinas livres
                    CS_Processamento auxMaq = escalonarRecurso(); //escalona o recurso

                    CS_MaquinaCloud maq = (CS_MaquinaCloud) auxMaq;
                    double memoriaMaq = maq.getMemoriaDisponivel();
                    System.out.println("memoriaMaq:" + memoriaMaq);
                    double memoriaNecessaria = auxVM.getMemoriaDisponivel();
                    System.out.println("memorianecessaria:" + memoriaNecessaria);
                    double discoMaq = maq.getDiscoDisponivel();
                    System.out.println("discoMaq:" + discoMaq);
                    double discoNecessario = auxVM.getDiscoDisponivel();
                    System.out.println("disconecessario" + discoNecessario);
                    int maqProc = maq.getProcessadoresDisponiveis();
                    System.out.println("ProcMaq:" + maqProc);
                    int procVM = auxVM.getProcessadoresDisponiveis();
                    System.out.println("ProcVM:" + procVM);

                    if ((memoriaNecessaria <= memoriaMaq && discoNecessario <= discoMaq && procVM <= maqProc)) {
                        maq.setMemoriaDisponivel(memoriaMaq - memoriaNecessaria);
                        System.out.println("memoria atual da maq" + (memoriaMaq - memoriaNecessaria));
                        maq.setDiscoDisponivel(discoMaq - discoNecessario);
                        System.out.println("disco atual maq" + (discoMaq - discoNecessario));
                        maq.setProcessadoresDisponiveis(maqProc - procVM);
                        System.out.println("proc atual" + (maqProc - procVM));
                        auxVM.setMaquinaHospedeira((CS_MaquinaCloud) auxMaq);
                        auxVM.setCaminho(escalonarRota(auxMaq));
                        System.out.println("escalonar rota" + auxVM.getId());
                        VMM.enviarVM(auxVM);
                        
                        break;
                    } else {
                        num_escravos--;
                    }
                } else {
                    auxVM.setStatus(CS_VirtualMac.REJEITADA);
                    VMsRejeitadas.add(auxVM);
                }
            }
        }

    }

    @Override
    public void migrarVM() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
