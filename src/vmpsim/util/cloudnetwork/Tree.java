package vmpsim.util.cloudnetwork;

import java.util.ArrayList;

import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.CS_VMM;

public class Tree {
	
    public static ArrayList<CS_Comunicacao> assemblyLinks(ArrayList<CS_Processamento> masters, ArrayList<CS_MaquinaCloud> hosts, double speed, double latency, int ports){
    	ArrayList<CS_Comunicacao> links = new ArrayList<CS_Comunicacao>();
    	int pnodes = masters.size() + hosts.size();
        int n = 0;
        int nlinks = ports - 1;
    	double h = Math.ceil(Math.log(pnodes)/Math.log(nlinks));
    	    	
    	for (int i = 0; i < h; i++)
    		n += Math.pow(nlinks, i);
        
        int totalsize = masters.size() + hosts.size() + n;
    	
    	CentroServico[] nodes = new CentroServico[totalsize];
    		
    	for (int i = 0; i < n; i++)
    		nodes[i] = new CS_Switch("Switch #" + i, speed, 0.0, latency);
        
        for (int i = n; i < n + masters.size(); i++)
    		nodes[i] = masters.get(i - n);
    	
        for (int i = n + masters.size(); i < n + masters.size() + hosts.size(); i++)
    		nodes[i] = hosts.get(i - n - masters.size());
        
        for(int i = 0; i < n; i++){
            boolean conexao = true;
                    
            for(int j = i*nlinks + 1; j < i*nlinks + 1 + nlinks; j++){
                if(j >= nodes.length){
                    conexao = false;
                    break;
                }
                
                ((CS_Switch)nodes[i]).addConexoesEntrada(nodes[j]);
                ((CS_Switch)nodes[i]).addConexoesSaida(nodes[j]);
                
                if (nodes[j] instanceof CS_Switch) {
                    ((CS_Switch)nodes[j]).addConexoesEntrada(nodes[i]);
                    ((CS_Switch)nodes[j]).addConexoesSaida(nodes[i]);
                }

                if (nodes[j] instanceof CS_MaquinaCloud){
                    ((CS_MaquinaCloud)nodes[j]).addConexoesEntrada((CS_Switch)nodes[i]);
                    ((CS_MaquinaCloud)nodes[j]).addConexoesSaida((CS_Switch)nodes[i]);
                }
                    
                if (nodes[j] instanceof CS_VMM){
                    ((CS_VMM)nodes[j]).addConexoesEntrada((CS_Switch)nodes[i]);
                    ((CS_VMM)nodes[j]).addConexoesSaida((CS_Switch)nodes[i]);
                }

            }
            if(conexao)
                links.add((CS_Switch)nodes[i]);
        }
        
    	return links;
    }
 }
