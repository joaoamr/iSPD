/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc.global;

import ispd.alocacaoVM.AlocadorGlobal;
import ispd.externo.cloudAlloc.NHCVMPutils.ClusterMap;
import ispd.externo.cloudAlloc.NHCVMPutils.GroupToCluster;
import ispd.externo.cloudAlloc.NHCVMPutils.GroupToPm;
import ispd.externo.cloudAlloc.NHCVMPutils.PmCluster;
import ispd.motor.Simulacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author João Antonio Magri Rodrigues
 */
public class NHCVMP3 extends AlocadorGlobal {
    
    public static int HIGHBAND = 5;
    public static int MEDIUMBAND = 6;
    public static int LOWBAND = 7;
    private CS_MaquinaCloud [] pms;
    private int n;
    int lastpm;
    CS_MaquinaCloud lasthost = null;
    CS_MaquinaCloud lastmischost= null;
    PmCluster lastglobal = null;
    GroupToPm map;
    ClusterMap clustermap;
    GroupToCluster grouptocluster;
    
    public NHCVMP3(){
        this.maquinasVirtuais = new ArrayList<CS_VirtualMac>();
        this.maquinasFisicas = new ArrayList<CS_Processamento>();
        this.VMsRejeitadas = new ArrayList<CS_VirtualMac>();
        this.grouptocluster = new GroupToCluster();
        this.lastpm = 0;
    }
    
    @Override
    public void iniciar(Simulacao simulacao) {
        n = maquinasFisicas.size();
        pms = new CS_MaquinaCloud[n];
        map = new GroupToPm(n);
        
        for(int i = 0; i < n; i++)
            pms[i] = (CS_MaquinaCloud)maquinasFisicas.get(i);
        
        if(!costtableset)
        {
            hoplist = new ArrayList<Integer>();
            hoplist.add(0);
            costtable = new int [n][n];
	        for(int i = 0; i < n; i++){
	            for(int j = 0;  j< n; j++){                      
	                if(j == i)
	                    costtable[i][j] = 0;
	                else{
	                    costtable[i][j] = CS_VMM.getMenorCaminhoCloud(pms[i], pms[j]).size() - 1;
                            if(hoplist.contains(costtable[i][j]))
                                hoplist.add(costtable[i][j]);
                            
	                    if(MAXHOP < costtable[i][j])
	                        MAXHOP = costtable[i][j];
	                }
	            }
	        }
        }
        clustermap = new ClusterMap(costtable, pms, hoplist);
        
        for(int i = 0; i < pms.length; i++){
        	for(int j = 0; j < pms.length; j++){
        		System.out.print(costtable[i][j] + " ");
        	}
        	System.out.println(" ");
        }
        escalonar();
    }

    @Override
    public CS_VirtualMac escalonarVM() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = maquinasFisicas.indexOf(destino);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoMaquina.get(index));
    }

    @Override
    public void escalonar() {
        while(!maquinasVirtuais.isEmpty()){
            CS_VirtualMac vm = maquinasVirtuais.remove(0);
            VMsRejeitadas.add(vm);
            vm.setStatus(CS_VirtualMac.REJEITADA);
            ArrayList<CS_VirtualMac> vms = new ArrayList<CS_VirtualMac>();
            vms.add(vm);
            for(int i = 0; i < maquinasVirtuais.size(); i++){
                CS_VirtualMac getvm = maquinasVirtuais.get(i);
                if(getvm.getProprietario().equals(vm.getProprietario())){
                    VMsRejeitadas.add(getvm);
                    getvm.setStatus(CS_VirtualMac.REJEITADA);
                    maquinasVirtuais.remove(i);
                    vms.add(getvm);
                    i--;
                }
            }
            int hop = 0;
            switch(vm.getType()){
                case CS_VirtualMac.NETWORKAWARE:
                    hop = MINHOP;
                break;
                case CS_VirtualMac.BESTFIT:
                    hop = MAXHOP;
                break;
            }
            matchCluster(vms, hop);
        }
        
    }
    
    private void matchCluster(ArrayList<CS_VirtualMac> vms, int hop){
        PmCluster lastcluster = grouptocluster.getLastAssigned();
    	
        for(int i = hoplist.indexOf(hop); i < hoplist.size(); i++){
            hop = hoplist.get(i);
            ArrayList clusterlist = grouptocluster.getClusterList(vms.get(0).getProprietario());
            if(lastcluster == null){
                clusterlist = clustermap.getClusters(hop);
            }else{
            	clusterlist = clustermap.getNextClusterList(lastcluster, MAXHOP, hop);
                clusterlist.remove(grouptocluster.getLastAssigned());
            }
            	
            PmCluster pmcluster = selectCluster(clusterlist, hop, vms.get(0), vms.size());
            if(pmcluster != null)
                fitIntoCluster(vms, pmcluster); 
            lastglobal = pmcluster;
            lastcluster = pmcluster;
            if(vms.isEmpty())
                return;
        }
        fitIntoCluster(vms, null);
    }
    
    private PmCluster selectCluster(ArrayList<PmCluster> clusterlist, int hop, CS_VirtualMac vm, int n){
        PmCluster cluster = clusterlist.get(0);
        int factor = clusterlist.get(0).fit(vm);
        
        if(factor == n)
        	return cluster; 
        
        for(int i = 1; i < clusterlist.size(); i++){
            PmCluster getcluster = clusterlist.get(i);
	    if(factor < getcluster.fit(vm)){
                factor = getcluster.fit(vm);
		cluster = getcluster;
		/*if(factor == n)
                    return getcluster;*/
	            	
	    }
            
        }
        if(factor < n)
            return null;
        
        return cluster;
    }
    
    private CS_MaquinaCloud fitIntoCluster(ArrayList<CS_VirtualMac> vms, PmCluster pmcluster){
        CS_MaquinaCloud lastindex = null;
        CS_MaquinaCloud[] pms;
        if(pmcluster == null)
            pms = pmcluster.getMachines();
        else
            pms = this.pms;
        
        int type = vms.get(0).getType();
        for(int i = 0; i < pms.length; i++){
        		boolean b = !vms.isEmpty();
        		while(b){
	            	CS_VirtualMac vm = vms.get(0);
	                if(pms[i].getMemoriaDisponivel() >= vm.getMemoriaDisponivel() && pms[i].getDiscoDisponivel() >= vm.getDiscoDisponivel() &&  pms[i].getProcessadoresDisponiveis() >= vm.getProcessadoresDisponiveis()){
	                    placeVM(vm, pms[i], pmcluster);
	                    vms.remove(0);
	                    lastindex = pms[i];
	                    b = !vms.isEmpty();
	                } else b = false;
        		}
        }
        if(type == CS_VirtualMac.BESTFIT);
        	lastmischost = lastindex;
        	
        lasthost = lastindex;
        return lastindex;
    }
    
    @Override
    public void migrarVM() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void placeVM(CS_VirtualMac vm, CS_MaquinaCloud pm, PmCluster pmcluster){
        grouptocluster.place(vm.getId(), pmcluster, vm.getType());
        vm.setCaminho(escalonarRota(pm));
        vm.setStatus(CS_VirtualMac.LIVRE);
        VMsRejeitadas.remove(vm);
        vm.setMaquinaHospedeira(pm);
        pm.setDiscoDisponivel(pm.getDiscoDisponivel() - vm.getDiscoDisponivel());
        pm.setMemoriaDisponivel(pm.getMemoriaDisponivel() - vm.getMemoriaDisponivel());
        pm.setProcessadoresDisponiveis(pm.getProcessadoresDisponiveis() - vm.getProcessadoresDisponiveis());
        VMM.enviarVM(vm);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  
}
