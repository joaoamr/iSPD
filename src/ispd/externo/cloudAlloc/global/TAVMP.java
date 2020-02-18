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
import java.util.Random;

/**
 *
 * @author João Antonio Magri Rodrigues
 */
public class TAVMP extends AlocadorGlobal {
    
    public static int HIGHBAND = 5;
    public static int MEDIUMBAND = 6;
    public static int LOWBAND = 7;
    private CS_MaquinaCloud [] pms;
    private int n;
    int lastpm;
    private ArrayList<Slot> slots = new ArrayList<Slot>();
    private int dummyIndex = -1;
    private float[][] matrizTrafego;
    private double[][] temp;
    private HashMap<CS_VirtualMac, CS_MaquinaCloud> assignMap = new HashMap<CS_VirtualMac, CS_MaquinaCloud>();
    private ArrayList<CS_VirtualMac> vmstotal;
    private HashSet<CS_MaquinaCloud> slotted = new HashSet<CS_MaquinaCloud>();
    private int p;
    private int kcut;
    
    GroupToPm map;
    
    public TAVMP(){
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
    public void processarArgumentos(String args){
        String[] argv = args.split(",");
        p = Integer.parseInt(argv[0]);
        kcut = Integer.parseInt(argv[1]);
    }
    
    @Override
    public void escalonar() {
        int k = 100;
        int p = this.p;
        k = this.kcut;
        boolean b = false;
        /*if(VMsRejeitadas.size() > p * pms.length)
            p = VMsRejeitadas.size()/pms.length;*/
        
        matrizTrafego = super.matrizTrafego;
        vmstotal = new ArrayList<CS_VirtualMac>(VMsRejeitadas);
        while(!VMsRejeitadas.isEmpty() && p > 0){
            if(slots.isEmpty()){
                p--;
                if(!slotPartition(p))
                    return;        
            }
            
            ArrayList<Integer> vms = new ArrayList<Integer>(VMsRejeitadas.size());
            for(int i = 0; i < VMsRejeitadas.size(); i++){
                vms.add(i);
                if(VMsRejeitadas.get(i) != null)
                    VMsRejeitadas.get(i).setStatus(CS_VirtualMac.REJEITADA);

            }
            maquinasVirtuais = new ArrayList<CS_VirtualMac>(VMsRejeitadas);
            
            clusterAndCut(slots, vms, k);
            placeAllVms();
            relocacao();
            b = true;
        }
        for(int i = 0; i < vmstotal.size(); i++){
            if(!VMsRejeitadas.contains(vmstotal))
                VMM.enviarVM(vmstotal.get(i));
        }
        
        //if(b) computeCost();
        
        
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
        assignMap.put(vm, pm);
    }
    
    @Override
    public CS_Processamento escalonarRecurso() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private ArrayList<ArrayList<Integer>> vmKminCut(ArrayList<ArrayList<Slot>> sclusters, ArrayList<Integer> vms, double[][] temp){
        ArrayList<ArrayList<Integer>> cuts = new ArrayList<ArrayList<Integer>>();
        ArrayList<Cut> arestas = gomoryHu(vms, temp);
        
        for(int i = 0; i < sclusters.size(); i++){
            
            for(int j = 0; j < temp.length; j++)
                for(int k = 0; k < temp.length; k++)
                    temp[j][k] = matrizTrafego[j][k];
           
            ArrayList<Integer> cluster = new ArrayList<Integer>(vms);
            vms.clear();
            ArrayList<Integer> tempcut;
            for(int j = 0; j < arestas.size(); j++){
                //tempcut = cutAndFit(arestas.get(j), sclusters.get(i).size() - vms.size(), cluster, temp);
                tempcut = cutAndFit(arestas.get(j), 32, cluster, temp);
                vms.addAll(tempcut);
                cluster.removeAll(tempcut);
                if(cluster.size() <= sclusters.get(i).size()){
                    while(cluster.size() < sclusters.get(i).size())
                        cluster.add(vms.remove(0));
                    
                    break;
                }
            }
            while(cluster.size() > sclusters.get(i).size()){
                if(cluster.isEmpty())
                    break;
                
                vms.add(cluster.remove(0));
            }
            
            cuts.add(cluster);
            
        } 
        return cuts;
    }
    
    private void clusterAndCut(ArrayList<Slot> slots, ArrayList<Integer> vms, int k){        
        if(slots.size() == 1){
            /*
            placeVM(maquinasVirtuais.get(vms.get(0)), slots.get(0).getPm());*/
            CS_VirtualMac vm = null;
            if(!vms.isEmpty())
                vm = maquinasVirtuais.get(vms.get(0));
            
            if(vm != null){
                assignMap.put(vm, slots.get(0).getPm());
                this.slots.remove(0);
            }
            
            
            return;
        }
        
        if(vms.isEmpty())
            return;
        
        temp = new double[matrizTrafego.length][matrizTrafego.length];
        
        for(int i = 0; i < matrizTrafego.length; i++)
            for(int j = 0; j < matrizTrafego.length; j++)
                temp[i][j] = matrizTrafego[i][j]; 
        
        //System.out.println("slot clustering...");
        ArrayList<ArrayList<Slot>> sclusters = approxSlotClustering(slots, k);
        
        for(int i = 0 ; i < sclusters.size(); i++){
            if(sclusters.get(i).isEmpty()){
                sclusters.remove(i);
                i--;
            }
        }
        //System.out.println("vmKminCut ...");
        ArrayList<ArrayList<Integer>> vmsclusters = vmKminCut(sclusters, vms, temp);
        
        for(int i = 0; i < sclusters.size(); i++)
            clusterAndCut(sclusters.get(i), vmsclusters.get(i), sclusters.get(i).size());
    }
    
    private boolean slotPartition(int k){
        double maxMem = 0;
        double maxDisk = 0;
        double maxPower = 0;
        
        if(true){
            slots = new ArrayList<Slot>();

            for(CS_VirtualMac vm : VMsRejeitadas){
                if(vm == null)
                    break;

                if(vm.getMemoriaDisponivel() > maxMem)
                    maxMem = vm.getMemoriaDisponivel();

                if(vm.getDiscoDisponivel() > maxDisk)
                    maxDisk = vm.getDiscoDisponivel();

                if(vm.getDiscoDisponivel() > maxPower)
                    maxPower = vm.getPoderNecessario();

            }

            double minMem = maxMem;
            double minDisk = maxDisk;
            double minPower = maxPower;

            for(CS_VirtualMac vm : VMsRejeitadas){
                if(vm == null)
                    break;

                if(vm.getMemoriaDisponivel() < minMem)
                    minMem = vm.getMemoriaDisponivel();

                if(vm.getDiscoDisponivel() < minDisk)
                    minDisk = vm.getDiscoDisponivel();

                if(vm.getDiscoDisponivel() < minPower)
                    minPower = vm.getPoderNecessario();
            }
            
            if(k == 0)
                k = 1;
     
            int npms = pms.length;
            
            for(int i = 0; i < npms; i++){
                if(pms[i].getAlocador().getMaquinasVirtuais().size() > 0){
                    continue;
                }
                
                int mem = 0;
                int disk = 0;
                int power = 0;
                int n;

                double media = (maxMem + minMem)/2;
                mem = (int)(pms[i].getMemoriaDisponivel()/media);

                media = (maxDisk + minDisk)/2;
                disk = (int)(pms[i].getDiscoDisponivel()/media);

                media = (maxPower + minPower)/2;
                power = (int)(pms[i].getDiscoDisponivel()/media);

                if(mem < disk)
                    n = mem;
                else
                    n = disk;

                if(power < n)
                    n = power;

                n = k;   
                for(int j = 0; j < n; j++){
                    if(slots.size() < VMsRejeitadas.size()){
                    try {slots.add(new Slot(i, pms[i])); } catch(Exception e) {break;}
                    }else break;    
                } 
                //slotted.add(pms[i]);
                
            }
        }
        
        if(slots.isEmpty())
            return false;
        
        int size;
        
        int maxId = 0;
        for (CS_VirtualMac vm : VMsRejeitadas){
            if(vm.getGlobalId() > maxId)
                maxId = vm.getGlobalId();
        }
        
        if(slots.size() == maxId + 1)
            size = slots.size();
        else
            size = maxId + 1 + slots.size();
        
        if(size < matrizTrafego.length)
            size = matrizTrafego.length;
        
        matrizTrafego = new float[size+1][size+1];
        
        for(int i = 0; i < matrizTrafego.length; i++)
            for(int j = 0 ; j <  matrizTrafego.length; j++)
                matrizTrafego[i][j] = 0; 
        
        for(int i = 0; i < super.matrizTrafego.length; i++)
            for(int j = 0 ; j <  super.matrizTrafego.length; j++)
                matrizTrafego[i][j] = super.matrizTrafego[i][j];
        
        int ndummies = slots.size() - VMsRejeitadas.size();
        dummyIndex = VMsRejeitadas.size();
        
        for(int i = 0 ; i < ndummies; i++)
            VMsRejeitadas.add(null);
        
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
   
        slotCluster = new ArrayList<Slot>();
        slotCluster.add(temp.remove(0));
        clusters.add(slotCluster);
        
        double max;
        
        for(int i = 1; i < k; i++){
            max = 0;
            Slot s = temp.get(0);
            for(int j = 1; j < temp.size(); j++){
                if(matrizTrafego[slotCluster.get(0).getPmIndex()][temp.get(j).getPmIndex()] > max){
                    s = temp.get(j);
                    max = matrizTrafego[slotCluster.get(0).getPmIndex()][temp.get(j).getPmIndex()];
                }
            }
            temp.remove(s);
            slotCluster = new ArrayList<Slot>();
            slotCluster.add(s);
            clusters.add(slotCluster);
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
            for(int j = 0; j < listaslots.size() - 1; j++){
                for(int l = j + 1; l < listaslots.size(); l++){
                    if(listaslots.get(l) == listaslots.get(j)){
                        listaslots.remove(l);
                        l--;
                        j--;
                        continue;
                    }
                    soma += costtable[listaslots.get(l).getPmIndex()][listaslots.get(j).getPmIndex()];
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
        
        if(slots.size() == 2 && k > 1){
            clusters.get(1).add(temp.remove(1));
        }
        else
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
                            if(clusters.get(j).remove(s)){
                                clusters.get(i).add(s);
                                l--;
                            }
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
            for(int j = 0; j < listaslots.size() - 1; j++){
                for(int l = j + 1; l < listaslots.size(); l++){
                    if(listaslots.get(l) == listaslots.get(j)){
                        listaslots.remove(l);
                        l--;
                        j--;
                        continue;
                    }
                    soma += costtable[listaslots.get(l).getPmIndex()][listaslots.get(j).getPmIndex()];
                }   
            }
            
            if(arestas.isEmpty()){
                arestas.add(soma);
                clustersordenados.add(clusters.get(i));
            }
            else{
                boolean alocado = false;
                for(int j = 0; j < arestas.size(); j++){
                    if(soma > arestas.get(j) || (soma > arestas.get(j) && clusters.get(i).size() > clustersordenados.get(j).size())){
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
    
    private ArrayList<Integer> cutAndFit(Cut k, int n, ArrayList<Integer> vms, double[][] temp){
        if(vms.isEmpty())
            return new ArrayList<Integer>();
        
        ArrayList<ArrayList<Integer>> cuts = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> tempvms = new ArrayList<Integer>(vms);
  
        int fitfactor = 0;
        
        int c1 = 0, c2 = 0;
        
        if(!k.getLista().isEmpty())
            {c1 = k.getLista().get(0)[0]; c2 = k.getLista().get(0)[1];}
        
        for(int i = 0; i < k.getLista().size(); i++){
            int[] cut = k.getLista().get(i);
            temp[cut[0]][cut[1]] = 0;
            temp[cut[1]][cut[0]] = 0;
        }
        
        while(!tempvms.contains(c1) || !tempvms.contains(c2)){
            if(k.getLista().isEmpty())
                break;
            k.getLista().remove(0);
            
            if(k.getLista().isEmpty())
                break;
            c1 = k.getLista().get(0)[0]; 
            c2 = k.getLista().get(0)[1];
        }
        
        /*if(!tempvms.contains(c1) || !tempvms.contains(c2))
            return new ArrayList<Integer>();*/
            
        if(!k.getLista().isEmpty())
            cuts = split(tempvms, temp, c1, c2);
        else{
            return new ArrayList<Integer>();
        }
        
        if(cuts.get(0).isEmpty() || cuts.get(1).isEmpty())
            return new ArrayList<Integer>();
        
        ArrayList<Integer> cut;
        cut = cuts.get(0);
        fitfactor = cuts.get(0).size();
        
        /*if(fitfactor < 0)
            fitfactor *= -1; */
        
        for(int i = 1; i < cuts.size(); i++){
            int f = cuts.get(i).size();
            //if(f < 0) f *= -1;
            if(f < fitfactor){
                fitfactor = f;
                cut = cuts.get(i);
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
        
        /*while(cut.size() < n){
            for(int i = 0; i < sort.size() && cut.size() < n; i++)
                while(sort.get(i).size() > 0 && cut.size() < n)
                    cut.add(sort.get(i).remove(0));            
        }*/
        
        while(cut.size() > n){
            int index = 0;
            double smin = -1;
            for(int i = 0; i < cut.size(); i++){
                double s = 0;
                for(int j = 0; j < vms.size(); j++){
                    s += matrizTrafego[cut.get(i)][vms.get(j)];
                }
                if(smin == -1){
                    smin = s;
                    index = i;
                }else{
                    if(smin > s){
                        smin = s;
                        index = i;
                    }
                }
            }
            
            cut.remove(index);    
        }
      
        //cut.add(fitfactor);
        return cut;
    }
    
    ArrayList<Cut> gomoryHu(ArrayList<Integer> vms, double[][] D){
        int n = D.length;
        ArrayList<Integer> tempvms = new ArrayList<Integer>(vms);
        ArrayList<Cut> arestas = new ArrayList<Cut>();
        ArrayList<ArrayList<Integer>> cuts = new ArrayList<ArrayList<Integer>>();
        double mind;
        Cut set;
        
        double[][] temp = new double[n][n];
            
        cuts.add(tempvms);
        
        while(!cuts.isEmpty()){            
            for(int i = 0; i < n; i++)  
                for(int j = 0; j < n; j++)
                    temp[i][j] = D[i][j];
        
            ArrayList<Integer> cut = cuts.remove(0);
            
            if(cut.size() < 2)
                continue;
            
            Integer c1 = cut.get(0), c2 = cut.get(1);
            
            boolean b = false;
            for(int j = 0; j < cut.size() - 1; j++){
                c1 = cut.get(j);
                for(int i = j+1; i < cut.size(); i++){
                    if(temp[cut.get(i)][c1] > 0){
                        c2 = cut.get(i);
                        b = true;
                        break;
                    }
                }
                if (b) break;
            }
            
            set = new Cut();
            set.inserirCorte(c1, c2, temp[c1][c2]);
            mind = temp[c1][c2];
            temp[c1][c2] = 0;
            temp[c2][c1] = 0;
            ArrayList<Integer> vertices = buscaProfundidade(c1, c2, vms, temp);
            //int p = 0;
            while(!vertices.isEmpty()){
                //System.out.println(p);
                //p++;
                double mincut = temp[vertices.get(0)][vertices.get(1)];
                int minvi = vertices.get(0);
                int minvf = vertices.get(1);
                for(int i = 1; i < vertices.size() - 1; i++){
                    int vi = vertices.get(i);
                    int vf = vertices.get(i + 1);
                    double tempcut = temp[vi][vf];
                    if(temp[vi][vf] < mincut){
                        mincut = temp[vi][vf];
                        minvi = vi;
                        minvf = vf;
                    }
                }
                temp[minvi][minvf] = 0;
                temp[minvf][minvi] = 0;
                set.inserirCorte(minvi, minvf, mincut);
                mind += mincut;
                //System.out.println("dfs...");
                vertices = buscaProfundidade(c1, c2, vms, temp);
                //System.out.println("dfs...ok");
            }
             
            boolean add = false;
            for(int i = 0; i < arestas.size(); i++){
                if(mind < arestas.get(i).getCapacity()){
                    arestas.add(i, set);
                    add = true;
                    break;
                }
                else{
                    if(mind == arestas.get(i).getCapacity()){
                        add = true;
                        break;
                    }
                }
            }
            if(!add) 
                arestas.add(set);
            
            vertices = buscaProfundidade(c1, c2, cut, temp);
         
            ArrayList<ArrayList<Integer>> sub = split(cut, temp, c1, c2);
            for(int i = 0; i < sub.size(); i++)
                cuts.add(sub.get(i));
        
        }        
        return arestas;
    }
    
    private ArrayList<ArrayList<Integer>> split(ArrayList<Integer> vms, double[][] D, Integer c1, Integer c2){
        ArrayList<ArrayList<Integer>> lista = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> temp = new ArrayList<Integer>(vms);
        
        ArrayList<Integer> p1 = new ArrayList<Integer>();
        ArrayList<Integer> p2 = new ArrayList<Integer>();
        p1.add(c1);
        temp.remove(temp.indexOf(c1));
        
        while(!temp.isEmpty()){
            ArrayList<Integer> vertices = buscaProfundidade(c1, temp.get(0), vms, D);
            
            ArrayList<Integer> vertices2 = buscaProfundidade(c1, c2, vms, D);
                
            if(vertices.isEmpty()){
                p2.add(temp.remove(0));
                continue;
            }
            
            vertices.remove(0);
            for(int j = 0; j < vertices.size(); j++){
                if(p1.contains(vertices.get(j)))
                    continue;
                
                p1.add(vertices.get(j));
                temp.remove(temp.indexOf(vertices.get(j)));
            }
        }
        
        lista.add(p1);
        lista.add(p2);
        
        return lista;
    }
    
    private HashSet<Integer> vertices;
    
    public ArrayList<Integer> buscaProfundidade(int vi, int vf, ArrayList<Integer> vms, double[][] D){
        vertices = new HashSet<Integer>();
        ArrayList<Integer> c = new ArrayList<Integer>();
        c.add(vi);
        int atual;
        vertices.add(vi);
        while(!c.isEmpty()){
            atual = c.get(c.size() - 1);
            for(int i = 0; i < vms.size(); i++){
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
            c.remove(c.size() - 1);
        }
        return c;
    }
    
    @Override
    public void setMaquinasVirtuais(List<CS_VirtualMac> maquinasVirtuais) {
        this.maquinasVirtuais = maquinasVirtuais;
        this.VMsRejeitadas = new ArrayList<CS_VirtualMac>(maquinasVirtuais);
    }
    
    @Override
    public void addVM (CS_VirtualMac vm){
        this.maquinasVirtuais.add(vm);
        this.VMsRejeitadas.add(vm);
    }
    
    private ArrayList<CS_VirtualMac> sortVms(){
        ArrayList<CS_VirtualMac> sorted = new ArrayList<CS_VirtualMac>();
        ArrayList<CS_VirtualMac> temp = new ArrayList<CS_VirtualMac>(VMsRejeitadas);
        
        while(temp.remove(null));
        
        int index = 0; 
        double smax = -1;
        while(!temp.isEmpty()){
            index = 0;
            smax = -1;
            for(int i = 0; i < temp.size(); i++){
                double s = 0;
                for(int j = 0; j < matrizTrafego.length; j++){
                    s += matrizTrafego[temp.get(i).getGlobalId()][j];
                }
                if(smax == -1){
                    smax = s;
                    index = i;
                }else{
                    if(smax < s){
                        smax = s;
                        index = i;
                    }
                }
            }
            sorted.add(temp.remove(index));
        }
      
        return sorted;
    }
    
    private void placeAllVms(){
        ArrayList<CS_VirtualMac> sorted = sortVms();
        
        for(int i = 0 ; i < sorted.size(); i++){
            if(assignMap.containsKey(sorted.get(i))){
                if(fit(sorted.get(i), assignMap.get(sorted.get(i)))){
                    placeVM(sorted.get(i), assignMap.get(sorted.get(i)));
                    VMsRejeitadas.remove(sorted.get(i));
                }
            }
        }
        
        for(int i = 0; i < VMsRejeitadas.size(); i++){
            if(VMsRejeitadas.get(i) == null){
                VMsRejeitadas.remove(i);
                i--;
            }
        }
    }
    
    private boolean fit(CS_VirtualMac vm, CS_MaquinaCloud pm){        
        if(pm.getDiscoDisponivel() >= vm.getDiscoDisponivel())
            if(pm.getMemoriaDisponivel() >= vm.getMemoriaDisponivel())
                if(pm.getPoderDisponivel() >= vm.getPoderNecessario())
                    return true;
        
        return false;
                
    }
    
    @Override
    public double computarCustoDeRede(){
        double d = 0;
        for(int i = 0; i < vmstotal.size() - 1; i++){
            for(int j = i + 1; j < vmstotal.size(); j++){
                int pmi = maquinasFisicas.indexOf(assignMap.get(vmstotal.get(i)));
                int pmj = maquinasFisicas.indexOf(assignMap.get(vmstotal.get(j)));
                d += super.matrizTrafego[vmstotal.get(i).getGlobalId()][vmstotal.get(j).getGlobalId()]*
                     super.costtable[pmi][pmj];
            }
        }
        
        System.out.println("Custo " + d);
        return d;
    }
    
    private void relocacao(){
        System.out.println("VMs em Relocaçao: " + VMsRejeitadas.size());
        double incremento = 0;
        
        ArrayList<CS_VirtualMac> sorted = sortVms();
        
        while(VMsRejeitadas.size() > 0){
            double min = -1, custo;
            CS_MaquinaCloud pm = null;
            for(int i = 0; i < pms.length; i++){
                if(fit(sorted.get(0), pms[i])){
                    if(min == -1){
                        pm = pms[i];
                        min = computarCusto(sorted.get(0), pms[i]);
                    }else{
                        custo = computarCusto(sorted.get(0), pms[i]);
                        if(min > custo){
                            pm = pms[i];
                            min = custo; 
                        }
                    }
                }
            }
            placeVM(sorted.get(0), pm);
            VMsRejeitadas.remove(sorted.remove(0));
            incremento += min;
        }
        
        System.out.println("Incremento total: " + incremento);
    }
    
    double computarCusto(CS_VirtualMac vm, CS_MaquinaCloud pm){
        double d = 0;
        for(int i = 0; i < vmstotal.size(); i++){
            if(VMsRejeitadas.contains(vmstotal.get(i)))
                continue;
            
            int pmi = maquinasFisicas.indexOf(assignMap.get(vmstotal.get(i)));
            int pmj = maquinasFisicas.indexOf(pm);
            
            d += super.matrizTrafego[vmstotal.get(i).getGlobalId()][vm.getGlobalId()]*
                 super.costtable[pmi][pmj];
        }
        
        return d;
    }
    
}
