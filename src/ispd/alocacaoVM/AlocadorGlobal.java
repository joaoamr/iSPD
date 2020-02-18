/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.alocacaoVM;

import ispd.externo.cloudAlloc.NHCVMPutils.GroupToPm;
import ispd.externo.cloudAlloc.TAVMPutils.Cut;
import ispd.externo.cloudAlloc.TAVMPutils.Slot;
import ispd.externo.cloudAlloc.local.AlocadorLocalPadrao;
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
 * @author Diogo Tavares
 */
public abstract class AlocadorGlobal {
    protected List<CS_Processamento> maquinasFisicas; //lista de "escravos"
    protected List<List> infoMaquinas; // lista de informações armazenada sobre cada máquina física
    protected List<CS_VirtualMac> maquinasVirtuais; //lista de vms "tarefas"
    protected VMM VMM; //vmm responsável por implementar a política de alocação
    protected List<CS_VirtualMac> VMsRejeitadas;    
    protected List<List> caminhoMaquina;
    protected int costtable[][] = null;
    protected boolean costtableset = false;
    protected int MAXHOP = 0;
    protected int MINHOP = 0;
    protected int MEDHOP = 0;
    protected ArrayList<Integer> hoplist;
    protected float[][] matrizTrafego = null;
    protected int dummyIndex = -1;
    int lastpm;
    GroupToPm map;
    protected ArrayList<Slot> slots = new ArrayList<Slot>();
    protected HashSet<CS_MaquinaCloud> slotted = new HashSet<CS_MaquinaCloud>();
    protected double[][] temp;
    protected ArrayList<CS_VirtualMac> vmstotal = new ArrayList<CS_VirtualMac>();
    protected int k;
    protected String topologia;
    protected HashMap<CentroServico, List<CentroServico>> mapaCaminhos = new HashMap<CentroServico, List<CentroServico>>();
    
    public void processarArgumentos(String args){
        
    }
    
    //iniciar a alocação
    public abstract void iniciar(Simulacao simulacao);
    //selecionar o critério de seleção da vm
    public abstract CS_VirtualMac escalonarVM();
    //selecionar o critério de seleção do recurso
    public abstract CS_Processamento escalonarRecurso();
    //implementar a rota até o recurso selecionado
    
    public double computarCustoDeRede(){
        double d = 0;
        for(int i = 0; i < vmstotal.size() - 1; i++){
            for(int j = i + 1; j < vmstotal.size(); j++){
                int pmi = maquinasFisicas.indexOf(vmstotal.get(i).getMaquinaHospedeira());
                int pmj = maquinasFisicas.indexOf(vmstotal.get(j).getMaquinaHospedeira());
                d += matrizTrafego[vmstotal.get(i).getGlobalId()][vmstotal.get(j).getGlobalId()]*
                     costtable[pmi][pmj];
            }
        }
        
        System.out.println("Custo " + d);
        return d;
    }
    
    public int maquinasAtivas(){
        int n = 0;
        for(int i = 0; i < maquinasFisicas.size(); i++){
            CS_MaquinaCloud host = (CS_MaquinaCloud) maquinasFisicas.get(i);
            if(host.getAlocador().getMaquinasVirtuais().size() > 0)
                n++;
        }
        
        System.out.println("Maquinas ativas: " + n);
        return n;
    }
    
    public List<CentroServico> escalonarRota(CentroServico destino) {
        if(mapaCaminhos.containsKey(destino))
            return new ArrayList<CentroServico>((List<CentroServico>) mapaCaminhos.get(destino));
        
        List<CentroServico> caminho = new ArrayList<CentroServico>();
        
        //List<CentroServico> caminho = CS_Processamento.getMenorCaminho((CS_Processamento)VMM, (CS_Processamento)destino);
        
        //mapaCaminhos.put(destino, caminho);
        
        return new ArrayList<CentroServico>(caminho);
    }
    //realiza o escalonamento de fato
    public abstract void escalonar();
    
    public abstract void migrarVM();
    
    public void addVM(CS_VirtualMac vm){
        maquinasVirtuais.add(vm);
        vmstotal.add(vm);
    }

    public List<CS_Processamento> getMaquinasFisicas() {
        return maquinasFisicas;
    }

    public void setMaquinasFisicas(List<CS_Processamento> maquinasFisicas) {
        this.maquinasFisicas = maquinasFisicas;
    }
    
    public void addMaquinaFisica(CS_MaquinaCloud maq){
        this.maquinasFisicas.add(maq);
        AlocadorLocal local = new AlocadorLocalPadrao();
        local.setHost(maq);
        local.setMestre(VMM);
       
        maq.setAlocador(local);   
    }

    public List<CS_VirtualMac> getMaquinasVirtuais() {
        return maquinasVirtuais;
    }

    public void setMaquinasVirtuais(List<CS_VirtualMac> maquinasVirtuais) {
        this.maquinasVirtuais = maquinasVirtuais;
        this.vmstotal = new ArrayList<CS_VirtualMac>(maquinasVirtuais);
    }

    public VMM getVMM() {
        return VMM;
    }

    public void setVMM(CS_VMM hypervisor) {
        this.VMM = (ispd.alocacaoVM.VMM) hypervisor;
    }

    public List<List> getCaminhoMaquinas() {
        return caminhoMaquina;
    }

    public void setCaminhoMaquinas(List<List> caminhoMaquinas) {
        this.caminhoMaquina = caminhoMaquinas;
    }

    public List<CS_VirtualMac> getVMsRejeitadas() {
        return VMsRejeitadas;
    }
    
    
    
    
    /**
     * Indica o intervalo de tempo utilizado pelo escalonador para realizar atualização dos dados dos escravos
     * Retornar null para escalonadores estáticos, nos dinâmicos o método deve ser reescrito
     * @return Intervalo em segundos para atualização
     */
    public Double getTempoAtualizar(){
        return null;
    }
    
    public void iniciarMatrizDeCustos(String topologia, int n, int k){
        this.k = k;
        this.topologia = topologia;
        
        hoplist = new ArrayList<Integer>();
        
    	if(topologia.equals("Fat-tree")){
            costtable = new int [n][n];
            for(int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++){
                    int vi, vj, vii, vjj;
                    vi = 2*i/k;
                    vj = 2*j/k;
                    vii = (4*i)/(k*k);
                    vjj = (4*j)/(k*k);
                    
                    if (i == j)
                    	costtable[i][j] = 0;
                    else
                        if(vi == vj)
                        	costtable[i][j] = 1;
                        else        
                            if(vii == vjj)
                            	costtable[i][j] = 3;
                            else    
                                if(vi != vj)
                                    costtable[i][j] = 5;
                    
                    if(MAXHOP < costtable[i][j])
                        MAXHOP = costtable[i][j];

                }
            }
           
            costtableset = true;
            hoplist.add(0);
            hoplist.add(1);
            hoplist.add(3);
            hoplist.add(5);
    	}
        
    }
    
    public void setMatrizCusto(int[][] m){
        costtable = m;
    }

    public float[][] getMatrizTrafego() {
        return matrizTrafego;
    }

    public void setMatrizTrafego(float[][] matrizTrafego) {
        this.matrizTrafego = matrizTrafego;
    }        

}


