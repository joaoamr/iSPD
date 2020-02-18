package vmpsim.util.cloudnetwork;

import java.util.ArrayList;

import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import java.util.LinkedList;

public class FatTree {
    public static ArrayList<CS_Comunicacao> assemblyLinks(ArrayList<CS_Processamento> masters, ArrayList<CS_MaquinaCloud> hosts, double speed, double latency, int ports){
    ArrayList<CS_Comunicacao> links = new ArrayList<CS_Comunicacao>();
    CS_Comunicacao edge[], aggregation[], core[];
    int pnodes = hosts.size();
       int nlinks = ports/2;
       int pods = ports;
       
       CS_Processamento[] proc = new CS_Processamento[pnodes];
       
       for(int i = 0; i < hosts.size(); i++)
           proc[i] = hosts.get(i);
      
       int nswitches = pnodes/2;
       
       edge = new CS_Comunicacao[nswitches];
       aggregation = new CS_Comunicacao[nswitches];
       
       for (int i = 0; i < edge.length; i++){
            edge[i] = new CS_Switch("Edge switch #" + i, speed, 0.0, latency);
            edge[i].setNivel(0);
            aggregation[i] = new CS_Switch("Aggregation switch #" + i, speed, 0.0, latency);
            aggregation[i].setNivel(1);
       }
       
        core = new CS_Comunicacao[(ports/2)*(ports/2)];
       
        for (int i = 0; i < core.length; i++){
            core[i] = new CS_Switch("Core switch #" + i, speed, 0.0, latency);
            core[i].setNivel(2);
        }
    
        //Connect the edges to the processing nodes
        for(int i = 0; i < edge.length; i++){
            for(int j = i*nlinks; j < i*nlinks + nlinks; j++){
               if(j >= proc.length)
                   break;
               
               ((CS_Switch)edge[i]).addConexoesEntrada(proc[j]);
               ((CS_Switch)edge[i]).addConexoesSaida(proc[j]);
               
               if(proc[j] instanceof CS_MaquinaCloud){
                   ((CS_MaquinaCloud)proc[j]).addConexoesEntrada((CS_Switch)edge[i]);
                   ((CS_MaquinaCloud)proc[j]).addConexoesSaida((CS_Switch)edge[i]);
               }
               
               if(proc[j] instanceof CS_VMM){
                   ((CS_VMM)proc[j]).addConexoesEntrada((CS_Switch)edge[i]);
                   ((CS_VMM)proc[j]).addConexoesSaida((CS_Switch)edge[i]);
               }
               
           }
       }
        boolean b = true;
        int visited = 0;
        int factor = 1;
        
        for(int k = 0; k < edge.length; k += pods/2){  
            for(int i = k; i < k + pods/2; i++){
                if(i >= edge.length)
                   break;
               
                for(int j = k; j < k + pods/2; j++){
                    if(j >= edge.length)
                       break;
                
                
               if(visited == nlinks){
                    if(b) b = false;
                    else  b = true;
                    factor *= -1;
                    visited = 0;
                }
                if(b){
                    ((CS_Switch)edge[i]).addConexoesSaida((CS_Switch)aggregation[j]);
                    ((CS_Switch)aggregation[j]).addConexoesEntrada((CS_Switch)edge[i]);
                    ((CS_Switch)edge[i]).addConexoesSaida((CS_Switch)aggregation[j+factor]);
                    ((CS_Switch)aggregation[j+factor]).addConexoesEntrada((CS_Switch)edge[i]);
                    b = false;
                }else{
                    ((CS_Switch)edge[i]).addConexoesEntrada((CS_Switch)aggregation[j]);
                    ((CS_Switch)aggregation[j]).addConexoesSaida((CS_Switch)edge[i]);
                    ((CS_Switch)edge[i]).addConexoesEntrada((CS_Switch)aggregation[j-factor]);
                    ((CS_Switch)aggregation[j-factor]).addConexoesSaida((CS_Switch)edge[i]);
                    b = true;
                }
                }
            }
            visited++;
        }
        
        int k = 0;
        
        //Connect the aggregation to the core
        for(int i = 0; i < aggregation.length; i++){
            if(i%nlinks == 0)
                k = 0;
            else
                k = k + nlinks;
                
            for(int j = k; j < k + nlinks && k < core.length; j++){
                        
                ((CS_Switch)core[j]).addConexoesEntrada((CS_Switch)aggregation[i]);
                ((CS_Switch)aggregation[i]).addConexoesSaida((CS_Switch)core[j]);
                ((CS_Switch)aggregation[i]).addConexoesEntrada((CS_Switch)core[j]);
                ((CS_Switch)core[j]).addConexoesSaida((CS_Switch)aggregation[i]);
                
            }
            
        }
        
 
        for(int i = 0; i < edge.length; i++)
            links.add(edge[i]);
       
        for(int i = 0; i < aggregation.length; i++){
            links.add(aggregation[i]);
        }
        for(int i = 0; i < core.length; i++){
            links.add(core[i]);
        }
        
        
        for(int i = 0; i < masters.size(); i++){
            ((CS_Switch)core[0]).addConexoesEntrada(masters.get(i));
            ((CS_Switch)core[0]).addConexoesSaida(masters.get(i));
               
            ((CS_VMM)masters.get(i)).addConexoesEntrada((CS_Switch)core[0]);
            ((CS_VMM)masters.get(i)).addConexoesSaida((CS_Switch)core[0]);
        }
        
        CS_Processamento.iniciarMapa();
        k = ports;
        /*
        for(int i = 0; i < proc.length - 1 && i < 1023; i++){
            for(int j = i + 1; j < proc.length && i < 1024; j++){
                //Caminho de ida
                int podi = (i)/k, podj = (j)/k, posi = i%(k/2), posj = j%(k/2), edgei = (i)/(k/2), edgej = (j)/(k/2),
                ppodj = (podj+1)*(k/2) - (k/2), ppodi = (podi+1)*(k/2) - (k/2);
                LinkedList<CentroServico> caminhoida = new LinkedList<CentroServico>();
                caminhoida.add(edge[edgei]);
                if(podi == podj && edgei == edgej)
                    caminhoida.add(proc[j]);
                else{
                    caminhoida.add(aggregation[edgei]);
                    if(podi == podj){
                        caminhoida.add(edge[edgej]);
                        caminhoida.add(proc[j]);
                    }else{
                        caminhoida.add(core[podi]);
                        caminhoida.add(aggregation[ppodj + posi]);
                        caminhoida.add(edge[edgej]);
                        caminhoida.add(proc[j]);
                    }
                }

                //caminho de volta
                LinkedList<CentroServico> caminhovolta = new LinkedList<CentroServico>();
                caminhovolta.add(edge[edgej]);
                if(podi == podj && edgei == edgej)
                    caminhovolta.add(proc[i]);  
                else{
                    caminhoida.add(aggregation[edgej]);
                    if(podi == podj){
                        caminhovolta.add(edge[edgei]);
                        caminhovolta.add(proc[i]);
                    }else{
                        caminhovolta.add(core[podj]);
                        caminhovolta.add(aggregation[ppodi + posj]);
                        caminhovolta.add(edge[edgei]);
                        caminhovolta.add(proc[i]);
                    }
                }
                
                //Adição
                CS_Processamento.mapaDeCaminhos.put(proc[i].getId() + "&" + proc[j].getId(), caminhoida);
                CS_Processamento.mapaDeCaminhos.put(proc[j].getId() + "&" + proc[i].getId(), caminhovolta);
            }
        }
        */
        for(int i = 0; i < proc.length; i++){
            ArrayList<CentroServico> caminho = new ArrayList<CentroServico>(1);
            caminho.add(masters.get(0));
            CS_Processamento.mapaDeCaminhos.put(proc[i].getId() + "&" + masters.get(0).getId(), caminho);
            caminho = new ArrayList<CentroServico>(1);
            caminho.add(proc[i]);
            CS_Processamento.mapaDeCaminhos.put(masters.get(0).getId() + "&" + proc[i].getId(), caminho);
        }
        
   	return links;
   }
 }
