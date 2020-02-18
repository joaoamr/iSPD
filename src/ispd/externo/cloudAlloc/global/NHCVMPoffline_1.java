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
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author João Antonio Magri Rodrigues
 */
public class NHCVMPoffline_1 extends AlocadorGlobal {
    
    public static int HIGHBAND = 5;
    public static int MEDIUMBAND = 6;
    public static int LOWBAND = 7;
    private CS_MaquinaCloud [] pms;
    private int n;
    int lastpm;
    GroupToPm map;
    HashMap<Integer, ArrayList<Integer>> globalMap = new HashMap<Integer, ArrayList<Integer>>();
    ArrayList<CS_VirtualMac> vmstotal;
    HashMap<CS_VirtualMac, CS_MaquinaCloud> assignMap = new HashMap<CS_VirtualMac, CS_MaquinaCloud>();
    
    public NHCVMPoffline_1(){
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
                            if(!hoplist.contains(costtable[i][j])){
                                boolean add = false;
                                if(hoplist.isEmpty())
                                    hoplist.add(costtable[i][j]);
                                else{
                                    for(int k = 0; k < hoplist.size(); k++){
                                        if(hoplist.get(k) > costtable[i][j]){
                                            hoplist.add(k, costtable[i][j]);
                                            add = true;
                                            break;
                                        }
                                        if(!add)
                                            hoplist.add(costtable[i][j]);
                                    }
                                }
                            }
                            
	                    if(MAXHOP < costtable[i][j])
	                        MAXHOP = costtable[i][j];
	                }
	            }
	        }
        }        
        escalonar();
    }

    @Override
    public CS_VirtualMac escalonarVM() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void escalonar() {
        processInput();
        vmstotal = new ArrayList<CS_VirtualMac>(maquinasVirtuais);
        
        while(!maquinasVirtuais.isEmpty()){
            CS_VirtualMac vm = maquinasVirtuais.remove(0);
            VMsRejeitadas.add(vm);
            vm.setStatus(CS_VirtualMac.REJEITADA);
            if(vm.getType() == CS_VirtualMac.NETWORKAWARE)
                lastpm = selectPm(vm, pms, costtable, map, lastpm, vm.getType());
                //solveQAP(vm);
            else
                bestFitApm(vm);
        }
        
        //computeCost();
    }

    @Override
    public void migrarVM() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void solveQAP(CS_VirtualMac vm){
        ArrayList<Integer> pms;
        HashSet<Integer> visited = new HashSet<Integer>();
        int pivo = lastpm;
        if(globalMap.containsKey(vm.getGlobalId())){
            pms = globalMap.get(vm.getGlobalId());
            pivo = pms.get(0);
            if(bestFit(vm, pms))
                return;
            
            for(int i = 0; i < pms.size(); i++)
                visited.add(pms.get(i));
        }
        else{
            for(int i = hoplist.size() - 1; i > -1; i--){
                int hop = hoplist.get(i);
                for(int k = 0; k < costtable.length; k++){
                    if(costtable[lastpm][k] == hop)
                        if(this.pms[k].getAlocador().encaixarVm(vm)){
                            pivo = k;
                        }
                }
            }
        }
        
        pms = new ArrayList<Integer>();
        for(int i = 0; i < hoplist.size(); i++){
            int hop = hoplist.get(i);
            for(int k = 0; k < costtable.length; k++){
                if(costtable[pivo][k] == hop && !visited.contains(k))
                    pms.add(k);
            }
            if(bestFit(vm, pms))
                return;
        }
            
        
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
                placeVM(vm, fpm);
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
    
    private void placeVM(CS_VirtualMac vm, Integer pm){
        ArrayList<Integer> pms = globalMap.get(vm.getGlobalId());
        if(pms == null){
            pms = new ArrayList<Integer>();
            pms.add(pm);
            globalMap.put(vm.getGlobalId(), pms);
        }else
            if(!pms.contains(pm))
                pms.add(pm);
        
        vm.setCaminho(escalonarRota(this.pms[pm]));
        vm.setStatus(CS_VirtualMac.LIVRE);
        VMsRejeitadas.remove(vm);
        AlocadorLocal local = this.pms[pm].getAlocador();
        local.adicionarVm(vm);
        VMM.enviarVM(vm);
        assignMap.put(vm, this.pms[pm]);
    }
    
    private boolean bestFit(CS_VirtualMac vm, ArrayList<Integer> pms){
        if(pms.isEmpty())
            return false;
        
        double mem = this.pms[pms.get(0)].getMemoriaDisponivel() - vm.getMemoriaDisponivel();
        double disk =  this.pms[pms.get(0)].getDiscoDisponivel() - vm.getDiscoDisponivel();
        
        double factor = mem;
        
        if(factor < disk)
            factor = disk;
        
        
        int index = 0;
        
        //Alocar em máquinas ativas
        for(int i = 1; i < pms.size(); i++){
            mem = this.pms[pms.get(0)].getMemoriaDisponivel() - vm.getMemoriaDisponivel();
            disk =  this.pms[pms.get(0)].getDiscoDisponivel() - vm.getDiscoDisponivel();
            if(mem > 0 && disk > 0){
                double newfactor = mem;
                    
                if(newfactor < disk)
                        newfactor = disk;
                
                if(factor < 0 && newfactor >= 0 && newfactor < factor){
                    factor = newfactor;
                    index = i;
                }   
            }
            
        }
        if(factor > 0){
            placeVM(vm, pms.get(index));
            lastpm = pms.get(index);
            return true;
        }
        return false;
    }
    
    private void bestFitApm(CS_VirtualMac vm){
        double mem = pms[0].getMemoriaDisponivel() - vm.getMemoriaDisponivel();
        double disk =  pms[0].getDiscoDisponivel() - vm.getDiscoDisponivel();
        
        double factor = mem;
        
        if(factor < disk)
            factor = disk;
        
        
        int index = 0;
        
        //Alocar em máquinas ativas
        for(int i = 1; i < pms.length; i++){
            if(!pms[i].getAlocador().getMaquinasVirtuais().isEmpty()){
                mem = pms[0].getMemoriaDisponivel() - vm.getMemoriaDisponivel();
                disk =  pms[0].getDiscoDisponivel() - vm.getDiscoDisponivel();
                if(mem > 0 && disk > 0){
                    double newfactor = mem;
                    
                    if(newfactor < disk)
                        newfactor = disk;


                    if(factor < 0 && newfactor >= 0 && newfactor < factor){
                        factor = newfactor;
                        index = i;
                    }   
                }
            }
        }
        if(factor > 0){
            placeVM(vm, index);
            return;
        }
        
        //Alocar em máquinas inativas
        
        for(int i = 1; i < pms.length; i++){
            if(pms[i].getAlocador().getMaquinasVirtuais().isEmpty()){
                mem = pms[0].getMemoriaDisponivel() - vm.getMemoriaDisponivel();
                disk =  pms[0].getDiscoDisponivel() - vm.getDiscoDisponivel();
                if(mem > 0 && disk > 0){
                    double newfactor = mem;
                    
                    if(newfactor < disk)
                        newfactor = disk;


                    if(factor < 0 && newfactor >= 0 && newfactor < factor){
                        factor = newfactor;
                        index = i;
                    }   
                }
            }
        }
        if(factor > 0)
            placeVM(vm, index);
    }
    
    @Override
    public CS_Processamento escalonarRecurso() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  
    private void processInput(){
        if (matrizTrafego == null)
            return;
        
        int n = matrizTrafego.length;
        ArrayList<CS_VirtualMac[]> clusterList = cut(10);
        maquinasVirtuais.clear();
        for(int i = 0; i < clusterList.size(); i++){
            CS_VirtualMac[] grupo = clusterList.get(i);
            for(int j = 0; j < grupo.length; j++){
                maquinasVirtuais.add(grupo[j]);
                grupo[j].setGlobalId(i);
            }
        }
    }
    
    private ArrayList<CS_VirtualMac[]> cut(int k){
        HashMap<Integer, Double> mapaCustos = new HashMap<Integer, Double>();
        ArrayList<Double> arestas = new ArrayList<Double>();
        ArrayList<CS_VirtualMac[]> cuts = new ArrayList<CS_VirtualMac[]>();
        ArrayList<Integer> vms = new ArrayList<Integer>();
        for(int i = 0; i < maquinasVirtuais.size(); i++)
            vms.add(i);
        
        while(!vms.isEmpty()){
            CS_VirtualMac[] list;
            ArrayList<Integer> sub = new ArrayList<Integer>();
            mapaCustos.put(sub.hashCode(), 0.0);
            sub.add(vms.remove(0));
            maquinasVirtuais.get(sub.get(0)).setType(CS_VirtualMac.NETWORKAWARE);
            for(int i = 0; i < vms.size(); i++){
                boolean b = true;
                double d = 0;
                for(int l = 0; l < sub.size(); l++){
                    d = matrizTrafego[vms.get(i)][sub.get(l)];
                    if(d < k){
                       b = false;
                    }
                    else
                        break;
                }
                if(b){
                    int index = vms.remove(i);
                    sub.add(index);
                    maquinasVirtuais.get(index).setType(CS_VirtualMac.NETWORKAWARE);
                    if(mapaCustos.containsKey(sub.hashCode()))
                        d += mapaCustos.get(sub.hashCode());
                    
                    mapaCustos.put(sub.hashCode(), d);
                    i--;
                }
            }
            if(sub.size() > 1){
	        list = new CS_VirtualMac[sub.size()];
	        for(int j = 0; j < sub.size(); j++){
	            list[j] = maquinasVirtuais.get(sub.get(j));
	        }
                if(cuts.isEmpty()){
                    cuts.add(list);
                    arestas.add(mapaCustos.get(sub.hashCode()));
                }else{
                    double d = 0;
                    int i;
                    for(i = 0; i < arestas.size(); i++){
                        d = mapaCustos.get(sub.hashCode());
                        if(d >= arestas.get(i))
                            break;
                    }
                    cuts.add(i, list);
                    arestas.add(i, d);
                }
            }
            else {
                CS_VirtualMac vm =  maquinasVirtuais.get(sub.get(0));
                vm.setType(CS_VirtualMac.BESTFIT);
                list = new CS_VirtualMac[1];
                list[0] = vm;
                cuts.add(list);
            }
        }
        return cuts;
    }
    
    private void computeCost(){
        double d = 0;
        for(int i = 0; i < vmstotal.size() - 1; i++)
            for(int j = i + 1; j < vmstotal.size(); j++){
                int pmi = maquinasFisicas.indexOf(assignMap.get(vmstotal.get(i)));
                int pmj = maquinasFisicas.indexOf(assignMap.get(vmstotal.get(j)));
                double dp = matrizTrafego[i][j]*
                     costtable[pmi][pmj];
                
                d += dp;
            }
        
        System.out.println ("Custo total: " + d);
    }
    
}
