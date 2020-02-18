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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author João Antonio Magri Rodrigues
 */
public class KLVMP2 extends AlocadorGlobal {
    
    public static int HIGHBAND = 5;
    public static int MEDIUMBAND = 6;
    public static int LOWBAND = 7;
    private CS_MaquinaCloud [] pms;
    private int n;
    private double[][] matrizTrafego;
    private HashMap<CS_VirtualMac, CS_MaquinaCloud> assignMap = new HashMap<CS_VirtualMac, CS_MaquinaCloud>();
    
    public KLVMP2(){
        this.maquinasVirtuais = new ArrayList<CS_VirtualMac>();
        this.maquinasFisicas = new ArrayList<CS_Processamento>();
        this.VMsRejeitadas = new ArrayList<CS_VirtualMac>();
    }
    
    @Override
    public void iniciar(Simulacao simulacao) {
        n = maquinasFisicas.size();
        pms = new CS_MaquinaCloud[n];
        
        ArrayList<CS_Processamento> sortpms = new ArrayList<CS_Processamento>(maquinasFisicas);
        /*maquinasFisicas.clear();
        
        while(sortpms.size() > 0){
            CS_MaquinaCloud pm = (CS_MaquinaCloud) sortpms.get(0);
            double vol = pm.getMemoriaDisponivel() * pm.getDiscoDisponivel() * pm.getPoderDisponivel();
            int index = 0;
            for(int i = 1; i < sortpms.size(); i++){
                pm = (CS_MaquinaCloud) sortpms.get(i);
                if(pm.getMemoriaDisponivel() * pm.getDiscoDisponivel() * pm.getPoderDisponivel() > vol){
                    index = i;
                    vol = pm.getMemoriaDisponivel() * pm.getDiscoDisponivel() * pm.getPoderDisponivel();
                }
            }
            maquinasFisicas.add(sortpms.remove(index));
        }
        */
        for(int i = 0; i < maquinasFisicas.size(); i++)
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
    public void escalonar() {
        if(VMsRejeitadas.isEmpty())
            return;
        
        FileWriter vms = null, pms = null;
        try {vms = new FileWriter("vms"); } catch (IOException ex) {
            Logger.getLogger(KLVMP2.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        PrintWriter saida = new PrintWriter(vms);
        saida.println(maquinasVirtuais.size());
        for(int i = 0; i < maquinasVirtuais.size(); i++){
            saida.print((int)maquinasVirtuais.get(i).getMemoriaDisponivel() + " " +
                          (int)maquinasVirtuais.get(i).getDiscoDisponivel() + " " +
                          (int)maquinasVirtuais.get(i).getPoderComputacional() + " ");
            
            for(int j = 0; j < maquinasVirtuais.size(); j++)
                saida.print(" " + super.matrizTrafego[i][j]);
            
            saida.print("\n");
        }
        
        try {
            vms.close();
        } catch (IOException ex) {
            Logger.getLogger(KLVMP2.class.getName()).log(Level.SEVERE, null, ex);
        }
        saida.close();
        
        try {pms = new FileWriter("pms"); } catch (IOException ex) {
            Logger.getLogger(KLVMP2.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        saida = new PrintWriter(pms);
        saida.println(maquinasFisicas.size() + " " + topologia + " " + k);
        
        for(int i = 0; i < this.pms.length; i++){
            saida.println((int)this.pms[i].getMemoriaDisponivel() + " " +
                          (int)this.pms[i].getDiscoDisponivel() + " " +
                          (int)this.pms[i].getPoderComputacional());
        }
        saida.close();
        
        Process escalonador = null;
        ProcessBuilder pb = new ProcessBuilder("scheduler.exe", "klvmp", "-v");
        
        try {
            escalonador = pb.start();
            escalonador.getErrorStream().close();
            escalonador.getInputStream().close();
        } catch (IOException ex) {
            Logger.getLogger(KLVMP2.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            escalonador.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(KLVMP2.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        FileReader entrada = null;
        
        try {
            entrada = new FileReader("placement");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KLVMP2.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        BufferedReader bf;
        
        bf = new BufferedReader(entrada);
        String mapa = null;
        
        try {
            mapa = bf.readLine();
        } catch (IOException ex) {
            Logger.getLogger(KLVMP2.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String[] ids = mapa.split(" ");
        
        for(int i = 0; i < ids.length; i++){
            int pmid = Integer.parseInt(ids[i]);
            placeVM(maquinasVirtuais.get(i), this.pms[pmid]);
        }
        
        try {
            entrada.close();
        } catch (IOException ex) {
            Logger.getLogger(KLVMP2.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            bf.close();
        } catch (IOException ex) {
            Logger.getLogger(KLVMP2.class.getName()).log(Level.SEVERE, null, ex);
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
    
    
    private boolean fit(CS_VirtualMac vm, CS_MaquinaCloud pm){
        if(pm.getDiscoDisponivel() >= vm.getDiscoDisponivel())
            if(pm.getMemoriaDisponivel() >= vm.getMemoriaDisponivel())
                if(pm.getPoderDisponivel() >= vm.getPoderNecessario())
                    return true;
        
        return false;
                
    }
    
    private void computeCost(){
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
