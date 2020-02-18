/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

import ispd.motor.EventoFuturo;
import ispd.motor.Simulacao;
import ispd.motor.filas.Cliente;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.TarefaVM;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Joao Antonio Magri Rodrigues
 */
public class CS_Switch extends CS_Comunicacao implements Vertice {

    private List<CentroServico> conexoesEntrada;
    private List<CentroServico> conexoesSaida;
    private List<Cliente> filaPacotes;
    private HashMap<Integer, Double> ultimaTimestamp;
    private HashMap<Integer, Double> tempoChegada;
    private HashMap<Integer, Double> dadosEnviados;
    private double larguraTotal;
    private HashMap<Integer, Double> larguraAtual = new HashMap<Integer, Double>();
    private double latencia;
    private HashMap<String, Integer> destino = new HashMap<String, Integer>();
    private HashMap<String, Integer> origem = new HashMap<String, Integer>();
    private EventoFuturo saida = null;
    private double mbitstransmitidos = 0;

    public CS_Switch(String id, double larguraBanda, double Ocupacao, double Latencia) {
        super(id, larguraBanda, Ocupacao, Latencia);
        larguraTotal = larguraBanda;
        this.conexoesEntrada = new ArrayList<CentroServico>();
        this.conexoesSaida = new ArrayList<CentroServico>();
        tempoChegada = new HashMap<Integer, Double>();
        ultimaTimestamp = new HashMap<Integer, Double>();
        dadosEnviados = new HashMap<Integer, Double>();
        this.latencia = Latencia;
        filaPacotes = new ArrayList<Cliente>();
        
    }

    public void addConexoesEntrada(CentroServico conexao) {
        origem.put(conexao.getId(), 0);
        this.conexoesEntrada.add(conexao);
    }

    public void addConexoesSaida(CentroServico conexao) {
        destino.put(conexao.getId(), 0);
        this.conexoesSaida.add(conexao);
    }
    
    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente){
        processarChegada(simulacao, cliente);
    }
   
    private void processarChegada(Simulacao simulacao, Cliente cliente) {            
        int nEntrada = 0;
        int nSaida = 0;

        //cliente.setUltimoCS(this);
        
        if(!origem.containsKey(cliente.getUltimoCS().getId()))
            origem.put(cliente.getUltimoCS().getId(), 0);
        else 
            nEntrada = origem.get(cliente.getUltimoCS().getId());
        
        nEntrada++;
        origem.put(cliente.getUltimoCS().getId(), nEntrada);
        
        if(!destino.containsKey(cliente.getCaminho().get(0).getId()))
            destino.put(cliente.getCaminho().get(0).getId(), 0);
        else
            nSaida = destino.get(cliente.getCaminho().get(0).getId());
        
        nSaida++;
        destino.put(cliente.getCaminho().get(0).getId(), nSaida);
        
        dadosEnviados.put(cliente.getId(), 0.0);
        tempoChegada.put(cliente.getId(), simulacao.getTime(this));
        ultimaTimestamp.put(cliente.getId(), simulacao.getTime(this));
        filaPacotes.add(cliente);
        
        if(!(cliente instanceof Mensagem)){
            ((Tarefa)cliente).iniciarEsperaComunicacao(simulacao.getTime(this));
            ((Tarefa)cliente).finalizarEsperaComunicacao(simulacao.getTime(this));
            ((Tarefa)cliente).iniciarAtendimentoComunicacao(simulacao.getTime(this));
        }
        
        if(saida != null)
            saida.cancelar();
        
        processarAtendimento(simulacao);
        
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        /* Método nao utilizado */
    }
    
    private void processarAtendimento(Simulacao simulacao){            
        if(filaPacotes.isEmpty())
           return;
       
        recalcularMbps(simulacao.getTime(this));
        
        Cliente primeiraSaida = filaPacotes.get(0);
        double next = (primeiraSaida.getTamComunicacao() - dadosEnviados.get(primeiraSaida.getId()))/larguraAtual.get(primeiraSaida.getId()) + latencia;
        
        for(int i = 1; i < filaPacotes.size(); i++){
            Cliente msg = filaPacotes.get(i);
            double dados = dadosEnviados.get(msg.getId());
            dados = msg.getTamComunicacao() - dados;
            double atual = dados/larguraAtual.get(msg.getId()) + latencia;
            if (atual < next){
                primeiraSaida = msg;
                next = atual;
            }
        }
        int tiposaida;
        
        if(primeiraSaida instanceof Mensagem)
            tiposaida = EventoFuturo.SAIDA_MENSAGEM;
        else
            tiposaida = EventoFuturo.SAÍDA;
      
        EventoFuturo evt = new EventoFuturo(
            simulacao.getTime(this) + next,
            tiposaida,
            this, primeiraSaida);
        
        simulacao.addEventoFuturo(evt);
        
    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        processarSaida(simulacao, cliente);
        
    }
    
    private void processarSaida(Simulacao simulacao, Cliente cliente){       
        if(!filaPacotes.remove(cliente))
            return;

        int nEntrada = origem.get(cliente.getUltimoCS().getId());
        nEntrada--;
        origem.put(cliente.getUltimoCS().getId(), nEntrada);
        
        int nSaida = destino.get(cliente.getCaminho().get(0).getId());
        nSaida--;
        destino.put(cliente.getCaminho().get(0).getId(), nSaida);
        
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        mbitstransmitidos += cliente.getTamComunicacao();
        //Incrementa o tempo de transmissão
        double tempoTrans = simulacao.getTime(this) - tempoChegada.get(cliente.getId());
        ultimaTimestamp.remove(cliente.getId());
        tempoChegada.remove(cliente.getId());
        dadosEnviados.remove(cliente.getId());
        
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Incrementa o tempo de transmissão no pacote
        int tipo;
        if(cliente instanceof Mensagem){
            tipo = EventoFuturo.MENSAGEM;
        }else{
            ((Tarefa)cliente).finalizarAtendimentoComunicacao(simulacao.getTime(this));
            tipo = EventoFuturo.CHEGADA;
        }
        
        //Gera evento para chegada da tarefa no proximo servidor
        saida = new EventoFuturo(
                simulacao.getTime(this),
                tipo,
                cliente.getCaminho().remove(0), cliente);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(saida);
        cliente.setUltimoCS(this);
        
        /*if(cliente instanceof Tarefa && !(cliente instanceof TarefaVM))
            System.out.println("Saida: " + ((Tarefa) cliente).getIdentificador() + " " + getNivel());
        */
        if (!filaPacotes.isEmpty()) {
            processarAtendimento(simulacao);
        }
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem cliente, int tipo) {
        if (tipo == EventoFuturo.MENSAGEM) {
            processarChegada(simulacao, cliente);
        }
        
        if (tipo == EventoFuturo.SAIDA_MENSAGEM){
            processarSaida(simulacao, cliente);
        }
    }

    @Override
    public List<CentroServico> getConexoesSaida() {
        return this.conexoesSaida;
    }

    @Override
    public void addConexoesEntrada(CS_Link conexao) {
        this.conexoesSaida.add(conexao);
    }

    @Override
    public void addConexoesSaida(CS_Link conexao) {
        this.conexoesSaida.add(conexao);
    }
    
    @Override
    public Integer getCargaTarefas() {
       return filaPacotes.size();
    }

    @Override
    public void limparEscalonador() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
     private void recalcularMbps(double tempo){
        for(Cliente tarefa: filaPacotes){
            atualizarBanda(tarefa);
            double atualizacao = ultimaTimestamp.get(tarefa.getId());
            double tempoEnvio  = tempo - atualizacao;
            ultimaTimestamp.put(tarefa.getId(), tempo);  
            
            double dados = dadosEnviados.get(tarefa.getId());
            dados += tempoEnvio * larguraAtual.get(tarefa.getId());

            dadosEnviados.put(tarefa.getId(), dados);
            ultimaTimestamp.put(tarefa.getId(), tempo);
 
        }
    }
     
    private void atualizarBanda(Cliente cliente){
        int entrada = origem.get(cliente.getUltimoCS().getId());
        int saida = destino.get(cliente.getCaminho().get(0).getId());
        int max;
        if(entrada > saida)
            max = entrada;
        else
            max = saida;
        
        double banda;
        
        if(max > 0)
            banda = larguraTotal/max;
        else
            banda = larguraTotal;
        
        larguraAtual.put(cliente.getId(), banda);
    } 

    public double getMbitstransmitidos() {
        return mbitstransmitidos;
    }
 
}
