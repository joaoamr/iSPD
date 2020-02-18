/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc.TAVMPutils;

import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author João Antonio Magri Rodrigues
 */
public class Cut {
    private double capacity;
    private ArrayList<int[]> cuts;
    private HashSet<Integer> vertex;

    public Cut() {
        capacity = 0;
        cuts = new ArrayList<int[]>();
        vertex = new HashSet<Integer>();
    } 
    
    public void inserirCorte(int ida, int idb, double c){
        capacity += c;
        int[] par = new int[2];
        par[0] = ida;
        par[1] = idb;
        cuts.add(par);
        vertex.add(ida);
        vertex.add(idb);
    }
    
    public ArrayList<int[]> getLista(){
        return cuts;
    }

    public double getCapacity() {
        return capacity;
    }
    
    public boolean isHead(Integer v){
        if(cuts.get(0)[1] == v || cuts.get(0)[0] == v)
            return true;
        
        return false;
    }
    
    
}

