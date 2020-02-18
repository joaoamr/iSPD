/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc.global;

import ispd.alocacaoVM.AlocadorGlobal;
import ispd.alocacaoVM.AlocadorLocal;
import ispd.externo.cloudAlloc.NHCVMPutils.ClusterMap;
import ispd.externo.cloudAlloc.NHCVMPutils.GroupToPm;
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
public class NHCVMPonline extends AlocadorGlobal {
    
    public static int HIGHBAND = 5;
    public static int MEDIUMBAND = 6;
    public static int LOWBAND = 7;
    private CS_MaquinaCloud [] pms;
    private int n;
    int lastpm;
    GroupToPm map;
    ClusterMap clusters;
    
    public NHCVMPonline(){
        this.maquinasVirtuais = new ArrayList<CS_VirtualMac>();
        this.maquinasFisicas = new ArrayList<CS_Processamento>();
        this.VMsRejeitadas = new ArrayList<CS_VirtualMac>();
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
        clusters = new ClusterMap(costtable, pms, hoplist);
        
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
            lastpm = selectPm(vm, pms, costtable, map, lastpm, vm.getType());
        }
        
    }

    @Override
    public void migrarVM() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private int selectPm(CS_VirtualMac vm, CS_MaquinaCloud [] pm, int costtable[][], GroupToPm map, int lastpm, int band) {
        int fpm = lastpm;
        int hop = 0;
        int[] pms = map.getPmList(vm.getUsuario());
        HashMap <Integer, Integer> visitedNodes = new HashMap<Integer, Integer>();
        boolean b = false;
        boolean up = false;
        
        if (band == HIGHBAND) {
            hop = MINHOP;
            up = true;
        }
            
        if (band == MEDIUMBAND) {
            hop = MEDHOP;
            up = true;
        }
        if (band == LOWBAND) {
            hop = MAXHOP;
            up = false;
        }
            

        while(true) {    
            for (int j = fpm; j < pm.length && !b; j++)
                if (costtable[fpm][j] == hop && !visitedNodes.containsKey(j)) {
                    if(pms == null){
                        fpm = nextPm(map.getLastassigned());
                        b = true;
                    }
                    else{
                        fpm = j;
                        b = true;
                        
                    }
                }
            
            for (int j = 0; j < fpm && !b; j++)
                if (costtable[fpm][j] == hop && !visitedNodes.containsKey(j)) {
                   if(pms == null){
                        fpm = nextPm(map.getLastassigned());
                        b = true;
                    }
                    else{
                        fpm = j;
                        b = true;

                   }
            }
            if(b && pm[fpm].getAlocador().encaixarVm(vm)) {
                placeVM(vm, pm[fpm]);
                map.place(vm.getProprietario(), fpm);
                //System.out.println(vm.getId() + " instanciada no host " + fpm + " " + hop);
                map.setLastassigned(fpm);
                return fpm;
            } else {
                visitedNodes.put(fpm, fpm);
            }
            
            if(!b)
            {
            if (up)
                hop++;
            else
                hop--;

            if(hop > MAXHOP) {
                up = false;
                hop--;
            }

            if(hop < MINHOP) {
                up = true;
                hop++;
            }
            
            }
            b = false;
            
            //System.out.println("Salto incrementado " + hop + " fpm " + fpm + " " + visitedNodes.size());
            if(visitedNodes.size() == pm.length - 1)
                return fpm;
        }
    }
    
    private int nextPm(int lastassigned){
        int pm = - 1;
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
               if(costtable[i][j] == MAXHOP && i != lastassigned){
                   if(pm == -1){
                       pm = i;
                   }else{
                       if((pms[i].getMemoriaDisponivel() > pms[pm].getMemoriaDisponivel()) && i != lastassigned)
                           pm = i;
                   }
               } 
            }
        }
        if(pm == -1)
            pm = 0;
        
        return pm;
    }
    
    private void placeVM(CS_VirtualMac vm, CS_MaquinaCloud pm){
        vm.setCaminho(escalonarRota(pm));
        vm.setStatus(CS_VirtualMac.LIVRE);
        VMsRejeitadas.remove(vm);
        AlocadorLocal local = pm.getAlocador();
        local.adicionarVm(vm);
        VMM.enviarVM(vm);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  
}
