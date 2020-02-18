/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc.NHCVMPutils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author João Antonio Magri Rodrigues
 */
public class GroupToPm {
    
    private HashMap<String, int[]> map;
    private int size;
    private int lastassigned;
    private int groupnumber;
    
    public GroupToPm(int size) {
        map = new HashMap<String, int[]>();
        this.size = size;
        groupnumber = 0;
        lastassigned = 0;
    }
    
    public void place(String groupid, int pm) { 
        if(!map.containsKey(groupid)) {
            int pmlist[] = new int [size + 1];
            for(int i = 0; i < size + 1; i++)
                pmlist[i] = 0;

            pmlist[pm] = 1;
            pmlist[size] = pm;
            map.put(groupid, pmlist);
            groupnumber++;
            return;
        }
        map.get(groupid)[pm] = 1;
        map.get(groupid)[size] = pm;
    }
    
    public int[] getPmList (String groupid) {
       if(!map.containsKey(groupid))
           return null;
       
       return map.get(groupid);
    }

    public int getSize() {
        return size;
    }

    public HashMap<String, int[]> getMap() {
        return map;
    }

    public void setMap(HashMap<String, int[]> map) {
        this.map = map;
    }

    public int getLastassigned() {
        return lastassigned;
    }

    public void setLastassigned(int lastassigned) {
        this.lastassigned = lastassigned;
    }

    public int getGroupNumber() {
        return groupnumber;
    }
   
    
}
