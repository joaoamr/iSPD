/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc.NHCVMPutils;

import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author João Antonio Magri Rodrigues
 */
public class ClusterMap {
    private int[][] costtable;
    private CS_MaquinaCloud[] pms;
    private ArrayList<Integer> cuts;
    private HashMap<Integer, ArrayList> clusters;
    private HashMap<String, Integer> pmid;
    
    
    public ClusterMap (int[][] costtable, CS_MaquinaCloud[] pms, ArrayList<Integer> cuts){
        this.costtable = costtable;
        this.pms = pms;
        this.cuts = cuts;
        clusters = new HashMap<Integer, ArrayList>();
        pmid = new HashMap<String, Integer>();
        ArrayList<PmCluster> clusterlist;
        ArrayList<Integer> machinedinlist;
        CS_MaquinaCloud[] machinelist;
        clusterlist = new ArrayList<PmCluster>();
        
        for(int i = 0; i < pms.length; i++){
            machinelist = new CS_MaquinaCloud[1];
            machinelist[0] = pms[i];
            clusterlist.add(new PmCluster(machinelist, 0));
            pmid.put(pms[i].getId(), i);
        }
        clusters.put(0, clusterlist);
        
        for(int i = 1;  i < cuts.size(); i++){
            int cut = cuts.get(i);
            clusterlist = new ArrayList<PmCluster>();
            machinedinlist = new ArrayList<Integer>();
            
            for(int k = 0; k < pms.length; k++){
                for(int j = 0; j < pms.length; j++){
                    if(costtable[k][j] == cut){
                        if(!machinedinlist.contains(k)) machinedinlist.add(k);
                        if(!machinedinlist.contains(j)) machinedinlist.add(j);
                    }
                }
            }
            
            ArrayList<CS_MaquinaCloud[]> graphcut = cut(machinedinlist, cut);
            for(int k = 0; k < graphcut.size(); k++){
                clusterlist.add(new PmCluster(graphcut.get(k), cut));
            }
            clusters.put(cut, clusterlist);
        }        
    }
    
    private ArrayList<CS_MaquinaCloud[]> cut(ArrayList<Integer> graph, int k){
        ArrayList<CS_MaquinaCloud[]> cuts = new ArrayList<CS_MaquinaCloud[]>();
        while(!graph.isEmpty()){
            CS_MaquinaCloud[] list;
            ArrayList<Integer> sub = new ArrayList<Integer>();
            sub.add(graph.remove(0));
            for(int i = 0; i < graph.size(); i++){
                boolean b = true;
                for(int l = 0; l < sub.size(); l++){
                    if(costtable[graph.get(i)][sub.get(l)] != k){
                       b = false;
                       break;
                    }
                }
                if(b){
                    sub.add(graph.remove(i));
                    i--;
                }
            }
            if(sub.size() > 1){
	            list = new CS_MaquinaCloud[sub.size()];
	            System.out.println("----- Add cluster:"+ k + " ----- ");
	            for(int j = 0; j < sub.size(); j++){
	                list[j] = pms[sub.get(j)];
	                System.out.println(list[j].getId());
	            }
	            System.out.println("------------------------\n");
	            cuts.add(list);
            }
        }
        return cuts;
    }
    
    public ArrayList<PmCluster> getClusters(int cut){
        ArrayList<PmCluster> clusterlist = new ArrayList<PmCluster>(clusters.get(cut));
        return clusterlist;
    }
    
    public ArrayList<PmCluster> getNextClusterList(PmCluster pmcluster, int dist, int cut){
    	ArrayList<PmCluster> list = clusters.get(cut);
    	ArrayList<PmCluster> out = new ArrayList<PmCluster>();
    	for(int i = 0;  i < list.size(); i++){
    		PmCluster p = list.get(i);
    		boolean b = true;
    		CS_MaquinaCloud[] mac1 = pmcluster.getMachines();
    		CS_MaquinaCloud[] mac2 = p.getMachines();
    		for(int j = 0; j < mac1.length && b; j++){
    			for(int k = 0; k < mac2.length && b; k++){
                            if(costtable[pmid.get(mac1[j].getId())][pmid.get(mac2[k].getId())] == dist){
                                out.add(p);
                                b = false;
                                break;
                            }
    			}
    		}
    	}
    	
    	return out;
    }
}
