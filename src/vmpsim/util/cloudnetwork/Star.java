package vmpsim.util.cloudnetwork;

import java.util.ArrayList;

import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.CS_VMM;

public class Star {
	
    public static ArrayList<CS_Comunicacao> assemblyLinks(ArrayList<CS_Processamento> masters, ArrayList<CS_MaquinaCloud> hosts, double speed, double latency){
    	ArrayList<CS_Comunicacao> links = new ArrayList<CS_Comunicacao>();
    	CS_Switch cs_switch = new CS_Switch("Switch #1", speed, 0.0, latency);
    	for(int i = 0; i < masters.size(); i++){
    		cs_switch.addConexoesEntrada(masters.get(i));
    		cs_switch.addConexoesSaida(masters.get(i));
                ((CS_VMM)masters.get(i)).addConexoesEntrada(cs_switch);
                ((CS_VMM)masters.get(i)).addConexoesSaida(cs_switch);
    	}
    		
    	for(int i = 0; i < hosts.size(); i++){
    		cs_switch.addConexoesEntrada(hosts.get(i));
    		cs_switch.addConexoesSaida(hosts.get(i));
                hosts.get(i).addConexoesEntrada(cs_switch);
                hosts.get(i).addConexoesSaida(cs_switch);
    	}
    		
    	
    	links.add(cs_switch);
    	return links;
    }
 }
