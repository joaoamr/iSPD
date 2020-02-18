/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc.NHCVMPutils;

import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Chico Bioca
 */
public class GroupToCluster {
    
    private HashMap<String, ArrayList> map;
    PmCluster lastassigned;
    PmCluster lastmiscassigned;
    
    public GroupToCluster() {
        map = new HashMap<String, ArrayList>();
        lastassigned = null;
        lastmiscassigned = null;
    }
    
    public void place(String groupid, PmCluster cluster, int type) { 
        lastassigned = cluster;
        if(type == CS_VirtualMac.BESTFIT)
            lastmiscassigned = cluster;
                    
        if(!map.containsKey(groupid)) {
            ArrayList<PmCluster> list = new ArrayList<PmCluster>();
            list.add(cluster);
            map.put(groupid, list);
            return;
        }
        map.get(groupid).add(cluster);
    }
    
    public ArrayList getClusterList (String groupid) {
       if(!map.containsKey(groupid))
           return null;
       
       return map.get(groupid);
    }

    public HashMap<String, ArrayList> getMap() {
        return map;
    }

    public PmCluster getLastAssigned() {
        return lastassigned;
    }

    public PmCluster getLastMiscAssigned() {
        return lastmiscassigned;
    }
    
}
