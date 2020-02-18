/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

import ispd.motor.EventoFuturo;
import ispd.motor.Mensagens;
import ispd.motor.Simulacao;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author João Antonio Magri Rodrigues
 */
public class CS_NucleoProcRR extends CS_Maquina implements Mensagens, Vertice {

    private List<Tarefa> tarefaEmExecucao;
    private double poderatual;
    private double podertotal;
    private int numprocessadores;
    private HashMap<Integer, Double> ultimaTimestamp;
    private int nucleosTotais = 0;
    private HashMap<Integer, Double> tempoChegada;
    EventoFuturo saida = null;
    
    public CS_NucleoProcRR(CS_Processamento host) {
        super(host.getId(), host.getProprietario(), host.getPoderComputacional(), host.getNumeroProcessadores(), 0.0, 0);
        this.tarefaEmExecucao = new ArrayList<Tarefa>();
        podertotal = host.getPoderComputacional();
        poderatual = podertotal;
        numprocessadores = host.getNumeroProcessadores();
        ultimaTimestamp = new HashMap<Integer, Double>();
        tempoChegada = new HashMap<Integer, Double>();
        
    }

    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        if (cliente.getEstado() != Tarefa.CANCELADO) {
            if(cliente.getNucleosAlocados() == 0)
                cliente.setNucleosAlocados(numprocessadores);
            
            nucleosTotais += cliente.getNucleosAlocados();

            ultimaTimestamp.put(cliente.getIdentificador(), simulacao.getTime(this));
            tempoChegada.put(cliente.getIdentificador(), simulacao.getTime(this));
       
            tarefaEmExecucao.add(cliente);
            
            if(saida != null)
                saida.cancelar();
            
            processarAtendimento(simulacao);
        }
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) { 
        /* Método nao utilizado */
        
    }
    private void processarAtendimento (Simulacao simulacao) {
        recalcularMflops(simulacao.getTime(this));
        Tarefa primeiraSaida = tarefaEmExecucao.get(0);
        double next = simulacao.getTime(this) + (primeiraSaida.getTamProcessamento() - primeiraSaida.getMflopsProcessado())/primeiraSaida.getMflopsAlocados();
        
        for(int i = 1; i < tarefaEmExecucao.size(); i++){
            Tarefa t = tarefaEmExecucao.get(i);
            double atual = simulacao.getTime(this) + (t.getTamProcessamento() - t.getMflopsProcessado())/t.getMflopsAlocados();
            if(atual < next){
                primeiraSaida = t;
                next = atual;
            }
        }
        
        saida = new EventoFuturo(
            next,
            EventoFuturo.SAÍDA,
            this, primeiraSaida);
        
        simulacao.addEventoFuturo(saida);
    }  

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        if(cliente.getEstado() == Tarefa.CONCLUIDO || cliente.getEstado() == Tarefa.CANCELADO)
            return;
        
        if(!tarefaEmExecucao.remove(cliente))
            return;
        
        nucleosTotais -= cliente.getNucleosAlocados();
        cliente.setEstado(Tarefa.CONCLUIDO);
        double tempoProc = simulacao.getTime(this) - tempoChegada.get(cliente.getIdentificador());
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
       
        ultimaTimestamp.remove(cliente.getIdentificador());
        tempoChegada.remove(cliente.getIdentificador());
 
        cliente.calcEficiencia(this.getPoderComputacional());       
        
        CentroServico machine = cliente.getLocalProcessamento();
        
        EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.SAÍDA,
                    machine, cliente);
            //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
            
        if(tarefaEmExecucao.isEmpty())
            return;
        
        processarAtendimento(simulacao);
    }
    
    @Override
    public void recalcularMflops(double tempo){
        if(numprocessadores > nucleosTotais){
            poderatual = podertotal;
        }
        else
            poderatual = (podertotal*numprocessadores)/nucleosTotais;
           
        for(Tarefa tarefa: tarefaEmExecucao){
            double atualizacao = ultimaTimestamp.get(tarefa.getIdentificador());
            double tempoProc = tempo - atualizacao;
            ultimaTimestamp.put(tarefa.getIdentificador(), tempo);  
        	
            double poderalocado;
            poderalocado = poderatual * tarefa.getNucleosAlocados();
           
            if(poderalocado > podertotal)
                poderalocado = podertotal;
            
            tarefa.setMflopsAlocados(poderalocado);
            tarefa.setMflopsProcessado(tarefa.getMflopsProcessado() + tempoProc * poderalocado);        
 
        }
    }
}
   