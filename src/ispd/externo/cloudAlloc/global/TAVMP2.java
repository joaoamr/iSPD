/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc.global;

import ispd.alocacaoVM.AlocadorGlobal;
import ispd.alocacaoVM.AlocadorLocal;
import ispd.externo.cloudAlloc.NHCVMPutils.GroupToPm;
import ispd.externo.cloudAlloc.TAVMPutils.Cut;
import ispd.externo.cloudAlloc.TAVMPutils.Slot;
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
public class TAVMP2 extends AlocadorGlobal {
    
    public static int HIGHBAND = 5;
    public static int MEDIUMBAND = 6;
    public static int LOWBAND = 7;
    private CS_MaquinaCloud [] pms;
    private int n;
    int lastpm;
    private ArrayList<Slot> slots = null;
    private int dummyIndex = -1;
    private double[][] matrizTrafego;

    
    GroupToPm map;
    
    public TAVMP2(){
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
                            if(!hoplist.contains(costtable[i][j]))
                                hoplist.add(costtable[i][j]);
                            
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
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = maquinasFisicas.indexOf(destino);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoMaquina.get(index));
    }

    @Override
    public void escalonar() {
        int k = 12;
        while(!VMsRejeitadas.isEmpty()){
            if(!slotPartition())
                return;        

            ArrayList<Integer> vms = new ArrayList<Integer>(VMsRejeitadas.size());
            for(int i = 0; i < VMsRejeitadas.size(); i++){
                vms.add(i);
                if(VMsRejeitadas.get(i) != null)
                    VMsRejeitadas.get(i).setStatus(CS_VirtualMac.REJEITADA);

            }
            maquinasVirtuais = new ArrayList<CS_VirtualMac>(VMsRejeitadas);
            
            clusterAndCut(slots, vms, k);
        }
        
    }

    @Override
    public void migrarVM() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    private void placeVM(CS_VirtualMac vm, CS_MaquinaCloud pm){
        VMsRejeitadas.remove(vm);
        if(vm == null)
            return;
        
        vm.setCaminho(escalonarRota(pm));
        vm.setStatus(CS_VirtualMac.LIVRE);
        AlocadorLocal local = pm.getAlocador();
        local.adicionarVm(vm);
        VMM.enviarVM(vm);
    }
    
    @Override
    public CS_Processamento escalonarRecurso() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private ArrayList<ArrayList<Integer>> vmKminCut(ArrayList<ArrayList<Slot>> sclusters, ArrayList<Integer> vms, double[][] temp){
        ArrayList<ArrayList<Integer>> cuts = new ArrayList<ArrayList<Integer>>();
        ArrayList<Double> arestas = gomoryHu(vms, temp);  
        for(int i = 0; i < sclusters.size(); i++){
            ArrayList<Integer> cluster;
            if(sclusters.get(i).size() > 0){
                cluster = cutAndFit(arestas.get(0), sclusters.get(i).size(), vms);
                for(int j = 1 ; j < arestas.size(); j++){
                    ArrayList<Integer> tempcut = cutAndFit(arestas.get(j), sclusters.get(i).size(), vms);
                    if(tempcut.get(tempcut.size() - 1) < cluster.get(cluster.size() - 1))
                        cluster = tempcut;
                }
                cluster.remove(cluster.size() - 1);
            
                for(int j = 0 ; j < cluster.size(); j++){
                    int index = vms.indexOf(cluster.get(j));
                    if(index > -1)
                        vms.remove(index);                
                }
            }
            else
                cluster = new ArrayList<Integer>();
            
            cuts.add(cluster);
        }
        
        
        return cuts;
    }
    
    private void clusterAndCut(ArrayList<Slot> slots, ArrayList<Integer> vms, int k){        
        if(vms.size() == 1){
            placeVM(maquinasVirtuais.get(vms.get(0)), slots.get(0).getPm());
            this.slots.remove(slots.get(0));
            return;
        }
        
        if(vms.isEmpty())
            return;
        
        double[][] temp = new double[matrizTrafego.length][matrizTrafego.length];
        
        for(int i = 0; i < matrizTrafego.length; i++)
            for(int j = 0; j < matrizTrafego.length; j++)
                temp[i][j] = matrizTrafego[i][j]; 
        
        ArrayList<ArrayList<Slot>> sclusters = approxSlotClustering(slots, k);
        ArrayList<ArrayList<Integer>> vmsclusters = vmKminCut(sclusters, vms, temp);
        
        for(int i = 0; i < sclusters.size(); i++)
            clusterAndCut(sclusters.get(i), vmsclusters.get(i), sclusters.get(i).size());
    }
    
    private boolean slotPartition(){
        double minMem = 0;
        double minDisk = 0;
        
        slots = new ArrayList<Slot>();
        
        for(CS_VirtualMac vm : VMsRejeitadas){
            if(vm == null)
                break;
            
            if(vm.getMemoriaDisponivel() > minMem)
                minMem = vm.getMemoriaDisponivel();
            
            if(vm.getDiscoDisponivel() > minDisk)
                minDisk = vm.getDiscoDisponivel();

        }
        
        for(int i = 0; i < pms.length; i++){
            int mem = 0;
            int disk = 0;
            int n;
            mem = (int)(pms[i].getMemoriaDisponivel()/minMem);
            disk = (int)(pms[i].getDiscoDisponivel()/minDisk);
            
            if(mem < disk)
                n = mem;
            else
                n = disk;
            
            for(int j = 0; j < n; j++)
                slots.add(new Slot(i, pms[i]));
        }
        
        if(slots.isEmpty())
            return false;
        
        int ndummies = slots.size() - VMsRejeitadas.size();
        dummyIndex = VMsRejeitadas.size();
        
        for(int i = 0 ; i < ndummies; i++)
            VMsRejeitadas.add(null);
        
        matrizTrafego = new double[VMsRejeitadas.size()][VMsRejeitadas.size()];
        
        for (int i = 0; i < VMsRejeitadas.size(); i++)
            for (int j = 0; j < VMsRejeitadas.size(); j++){
                if(i >= dummyIndex || j >= dummyIndex)
                    matrizTrafego[i][j] = 0;
                else
                    matrizTrafego[i][j] = super.matrizTrafego[i][j];
            }
        
        return true;
    }
    
    private ArrayList <ArrayList<Slot>> slotClustering(ArrayList<Slot> slots, int k){
        ArrayList<Slot> temp = new ArrayList<Slot>(slots);
        ArrayList<ArrayList<Slot>> clusters = new ArrayList<ArrayList<Slot>>(k);
        ArrayList<Slot> slotCluster;
        ArrayList<Integer> arestas = null;
      
        int m = 1;
        int r = 0;
        
        if(temp.size() > k + 1){
            m = temp.size()/(k+2);
            r = m;
        }
   
        
        for(int i = 0; i < k; i++){
            int ti = i*m + r;
            slotCluster = new ArrayList<Slot>();
            slotCluster.add(temp.remove(ti));
            clusters.add(slotCluster);
            r--;
        }
        while(!temp.isEmpty()){
            int mind = MAXHOP;
            int clusterindex = 0;
            
            for(int j = 0; j < clusters.size(); j++){
                Slot head = clusters.get(j).get(0);
                if(costtable[head.getPmIndex()][temp.get(0).getPmIndex()] < mind){
                    clusterindex = j;
                    mind = costtable[head.getPmIndex()][temp.get(0).getPmIndex()];
                    if(mind == 0)
                        break;
                }
                    
            }
            
            clusters.get(clusterindex).add(temp.remove(0));
            ArrayList<Slot> list = clusters.remove(clusterindex);
            clusters.add(list);
        }
       
        ArrayList<ArrayList<Slot>> clustersordenados = new ArrayList<ArrayList<Slot>>(k);
        
        arestas = new ArrayList<Integer>();
        for(int i = 0; i < clusters.size(); i++){
            ArrayList<Integer> listahosts = new ArrayList<Integer>();
            int soma = 0;
            
            ArrayList<Slot> listaslots = clusters.get(i);
            for(int j = 0; j < listaslots.size(); j++){
                if(!listahosts.contains(listaslots.get(j).getPmIndex())){
                    for(int l = 0; l < listahosts.size(); l++)
                        soma += costtable[listahosts.get(l)][listaslots.get(j).getPmIndex()];
                    
                    listahosts.add(listaslots.get(j).getPmIndex());
                }
            }
            if(arestas.isEmpty()){
                arestas.add(soma);
                clustersordenados.add(clusters.get(i));
            }
            else{
                boolean alocado = false;
                for(int j = 0; j < arestas.size(); j++){
                    if(soma >= arestas.get(j)){
                        alocado = true;
                        arestas.add(j, soma);
                        clustersordenados.add(j, clusters.get(i));
                        break;
                    }
                }
                if(!alocado){
                   arestas.add(soma);
                   clustersordenados.add(clusters.get(i));
                }
            }
        }
        
        return clustersordenados;
    }
    
    private ArrayList <ArrayList<Slot>> approxSlotClustering(ArrayList<Slot> slots, int k){
        ArrayList<Slot> temp = new ArrayList<Slot>(slots);
        ArrayList<ArrayList<Slot>> clusters = new ArrayList<ArrayList<Slot>>(k);
        ArrayList<Slot> slotCluster;
        ArrayList<Integer> arestas = null;
        if(temp.isEmpty())
            return new ArrayList<ArrayList<Slot>>();
        
        Slot head = temp.get(0);
        
        clusters.add(temp);
        
        for(int i = 0; i < k - 1; i++)
            clusters.add(new ArrayList<Slot>());
        
        for(int i = 0 ; i < k - 1; i++){
            int dMax = 0;
            Slot sMax = null;
            int clusterIndex = 0;
            for (int j = 0; j <= i; j++){
                for(int l = 1 ; l < clusters.get(j).size(); l++){
                    head = clusters.get(j).get(0);
                    Slot s = clusters.get(j).get(l);
                    int headi = head.getPmIndex();
                    int si = s.getPmIndex();
                    if(costtable[headi][si] >= dMax){
                        dMax = costtable[head.getPmIndex()][s.getPmIndex()];
                        sMax = s;
                        clusterIndex = j;
                    }
                }
            }
            temp = clusters.get(clusterIndex);
            temp.remove(sMax);
            temp = clusters.get(i + 1);
            temp.add(0, sMax);
            
            for (int j = 0; j < i; j++){
                for(int l = 0 ; l < clusters.get(j).size(); l++){
                    Slot headj = clusters.get(j).get(0);
                    Slot s = clusters.get(j).get(l);
                    if(costtable[headj.getPmIndex()][s.getPmIndex()] >= costtable[head.getPmIndex()][s.getPmIndex()]){
                        clusters.get(j).remove(s);
                        clusters.get(i).add(s);
                        l--;
                    }
                }
            }
        }

        ArrayList<ArrayList<Slot>> clustersordenados = new ArrayList<ArrayList<Slot>>(k);
        
        arestas = new ArrayList<Integer>();
        for(int i = 0; i < clusters.size(); i++){
            ArrayList<Integer> listahosts = new ArrayList<Integer>();
            int soma = 0;
            
            ArrayList<Slot> listaslots = clusters.get(i);
            for(int j = 0; j < listaslots.size(); j++){
                if(!listahosts.contains(listaslots.get(j).getPmIndex())){
                    for(int l = 0; l < listahosts.size(); l++)
                        soma += costtable[listahosts.get(l)][listaslots.get(j).getPmIndex()];
                    
                    listahosts.add(listaslots.get(j).getPmIndex());
                }
            }
            if(arestas.isEmpty()){
                arestas.add(soma);
                clustersordenados.add(clusters.get(i));
            }
            else{
                boolean alocado = false;
                for(int j = 0; j < arestas.size(); j++){
                    if(soma >= arestas.get(j)){
                        alocado = true;
                        arestas.add(j, soma);
                        clustersordenados.add(j, clusters.get(i));
                        break;
                    }
                }
                if(!alocado){
                   arestas.add(soma);
                   clustersordenados.add(clusters.get(i));
                }
            }
        }
        
        return clustersordenados;
    }
    
    private ArrayList<Integer> cutAndFit(double k, int n, ArrayList<Integer> vms){
        if(vms.isEmpty())
            return new ArrayList<Integer>();
        
        ArrayList<ArrayList<Integer>> cuts = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> tempvms = new ArrayList<Integer>(vms);
        double[][] temp = new double[matrizTrafego.length][matrizTrafego.length];
        int fitfactor = 0;
        
        for(int i = 0;  i < matrizTrafego.length; i++)
            for(int j = 0; j < matrizTrafego.length; j++){
                if(matrizTrafego[i][j] < k)
                    temp[i][j] = 0;
                else
                    temp[i][j] = matrizTrafego[i][j];
            }
        
        cuts = split(tempvms, temp);
        ArrayList<Integer> cut;
        cut = cuts.get(0);
        fitfactor = cuts.get(0).size() - n;
        if(fitfactor < 0)
            fitfactor *= -1; 
                
        for(int i = 0; i < cuts.size(); i++){
            int newfit = cuts.get(i).size() - n;
            if(newfit < 0)
                newfit *= -1; 
            
            if(newfit < fitfactor){
                cut = cuts.get(i);
                fitfactor = newfit;
            }
            
        }
        cuts.remove(cut);    
        
        ArrayList<Integer> p;
        ArrayList<ArrayList<Integer>> sort = new ArrayList<ArrayList<Integer>>(cuts.size());
        
        while(cuts.size() > 0){
            p = cuts.remove(0);
            boolean alocado = false;
            if(sort.isEmpty()){
                sort.add(p);
                continue;
            }
            for(int i = 0 ; i < sort.size(); i++){
                if(sort.get(i).size() > p.size()){
                    sort.add(i, p);
                    alocado = true;
                    break;
                }
            }
            
            if(!alocado)
                sort.add(p);
        }
        
        while(cut.size() < n){
            for(int i = 0; i < sort.size() && cut.size() < n; i++)
                while(sort.get(i).size() > 0 && cut.size() < n)
                    cut.add(sort.get(i).remove(0));            
        }
        
        while(cut.size() > n)
            cut.remove(0);    
      
        cut.add(fitfactor);
        return cut;
    }
    
    ArrayList<Double> gomoryHu(ArrayList<Integer> vms, double[][] D){
        int n = D.length;
        ArrayList<Integer> tempvms = new ArrayList<Integer>(vms);
        ArrayList<Double> arestas = new ArrayList<Double>();
        ArrayList<ArrayList<Integer>> cuts = new ArrayList<ArrayList<Integer>>();
        double mind;
        Cut set = new Cut();
        
        double[][] temp = new double[n][n];
            
        cuts.add(tempvms);
        
        while(!cuts.isEmpty()){
        
            for(int i = 0; i < n; i++)
                for(int j = 0; j < n; j++)
                    temp[i][j] = matrizTrafego[i][j];
        
            ArrayList<Integer> cut = cuts.remove(0);
            if(cut.size() < 2)
                continue;
             
            set.inserirCorte(cut.get(0), cut.get(1), temp[cut.get(0)][cut.get(1)]);
            mind = temp[cut.get(0)][cut.get(1)];
            temp[cut.get(0)][cut.get(1)] = 0;
            ArrayList<Integer> vertices = buscaProfundidade(cut.get(0), cut.get(1), cut, temp);
            while(!vertices.isEmpty()){
                double mincut = temp[vertices.get(0)][vertices.get(1)];
                int minvi = vertices.get(0);
                int minvf = vertices.get(1);
                for(int i = 1; i < vertices.size() - 1; i++){
                    int vi = vertices.get(i);
                    int vf = vertices.get(i + 1);
                    if(temp[vi][vf] < mincut){
                        mincut = temp[vi][vf];
                        minvi = vi;
                        minvf = vf;
                    }
                }
                temp[minvi][minvf] = 0;
                temp[minvf][minvi] = 0;  
                mind += mincut;
                vertices = buscaProfundidade(cut.get(0), cut.get(1), cut, temp);
            }
             
            boolean add = false;
            for(int i = 0; i < arestas.size(); i++){
                if(mind < arestas.get(i)){
                    arestas.add(i, mind);
                    add = true;
                    break;
                }
                else{
                    if(mind == arestas.get(i)){
                        add = true;
                        break;
                    }
                }
            }
            if(!add) 
                arestas.add(mind);
            
            ArrayList<ArrayList<Integer>> sub = split(cut, temp);
            for(int i = 0; i < sub.size(); i++)
                cuts.add(sub.get(i));
        
        }        
        return arestas;
    }
    
    private ArrayList<ArrayList<Integer>> split(ArrayList<Integer> vms, double[][] D){
        ArrayList<ArrayList<Integer>> lista = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> temp = new ArrayList<Integer>(vms);
        
        while(!temp.isEmpty()){
            ArrayList<Integer> sub = new ArrayList<Integer>();
            sub.add(temp.remove(0));
            
            for(int i = 0; i < temp.size(); i++){
                for(int j = 0; j < sub.size(); j++){
                    if(D[sub.get(j)][temp.get(i)] > 0){
                        sub.add(temp.remove(i));
                        i--;
                        break;
                    }
                }
            }
            
            lista.add(sub);
        }
        
        return lista;
    }
    
    public ArrayList<Integer> buscaProfundidade(int vi, int vf, ArrayList<Integer> vms, double[][] D){
        ArrayList<Integer> c = new ArrayList<Integer>();
        c.add(vi);
        HashSet<Integer> vertices = new HashSet<Integer>();
        int k = 0, atual;
        vertices.add(vi);
        while(!c.isEmpty()){
            atual = c.get(c.size() - 1);
            for(int i = k; i < vms.size(); i++){
                if(D[atual][vms.get(i)] > 0 && !vertices.contains(vms.get(i))){
                    c.add(vms.get(i));
                    vertices.add(vms.get(i));
                    if(vms.get(i) == vf)
                        return c;
                    else{
                        atual = vms.get(i);
                        i = 0;
                    }
                }
            }
            atual = c.remove(c.size() - 1);
            k = vms.indexOf(atual) + 1;
            vertices.remove(atual);
            /*if(k == vms.size()){
                c.clear();
            }*/
        }
        return c;
    }
    
    @Override
    public void setMaquinasVirtuais(List<CS_VirtualMac> maquinasVirtuais) {
        this.maquinasVirtuais = maquinasVirtuais;
        this.VMsRejeitadas = new ArrayList<CS_VirtualMac>(maquinasVirtuais);
        super.vmstotal = new ArrayList<CS_VirtualMac>(maquinasVirtuais);
    }
    
    @Override
    public void addVM (CS_VirtualMac vm){
        this.maquinasVirtuais.add(vm);
        this.VMsRejeitadas.add(vm);
        super.vmstotal.add(vm);
    }
}
