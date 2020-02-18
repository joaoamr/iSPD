/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

import ispd.motor.EventoFuturo;
import ispd.motor.Mensagens;
import ispd.motor.Simulacao;
import ispd.motor.SimulacaoSequencial;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author denison
 */
public class CS_Link extends CS_Comunicacao {

    private CentroServico conexoesEntrada;
    private CentroServico conexoesSaida;
    private List<Tarefa> filaPacotes;
    private List<Mensagem> filaMensagens;
    private boolean linkDisponivel;
    private boolean linkDisponivelMensagem;
    private double tempoTransmitirMensagem;
    private CentroServico maquinaFalhada;
    
    public CS_Link(String id, double LarguraBanda, double Ocupacao, double Latencia) {
        super(id, LarguraBanda, Ocupacao, Latencia);
        this.conexoesEntrada = null;
        this.conexoesSaida = null;
        this.linkDisponivel = true;
        this.filaPacotes = new ArrayList<Tarefa>();
        this.filaMensagens = new ArrayList<Mensagem>();
        this.tempoTransmitirMensagem = 0;
        this.linkDisponivelMensagem = true;
    }

    public CentroServico getConexoesEntrada() {
        return conexoesEntrada;
    }

    public void setConexoesEntrada(CentroServico conexoesEntrada) {
        this.conexoesEntrada = conexoesEntrada;
    }

    @Override
    public CentroServico getConexoesSaida() {
        return conexoesSaida;
    }

    public void setConexoesSaida(CentroServico conexoesSaida) {
        this.conexoesSaida = conexoesSaida;
    }

    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        if(inoperante){
        	linkDisponivel = true;
            return;
        }
        
        cliente.iniciarEsperaComunicacao(simulacao.getTime(this));
        if (linkDisponivel) {
            //indica que recurso está ocupado
            linkDisponivel = false;
            //cria evento para iniciar o atendimento imediatamente
            EventoFuturo novoEvt = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.ATENDIMENTO,
                    this,
                    cliente);
            simulacao.addEventoFuturo(novoEvt);
        } else {
            filaPacotes.add(cliente);
        }
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        if(verificarFalhaAtendimento(simulacao, cliente)){
        	linkDisponivel = true;
            return;
        }
        
        if (!conexoesSaida.equals(cliente.getCaminho().get(0))) {
            throw new IllegalArgumentException("O destino da mensagem (" + cliente.getCaminho().get(0).getId() +") é um recurso sem conexão com este link");
        } else {
            cliente.finalizarEsperaComunicacao(simulacao.getTime(this));
            cliente.iniciarAtendimentoComunicacao(simulacao.getTime(this));
            //Gera evento para atender proximo cliente da lista
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this) + tempoTransmitir(cliente.getTamComunicacao()),
                    EventoFuturo.SAÍDA,
                    this, cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        }
    }
    
    private boolean verificarFalhaAtendimento(Simulacao sim, Tarefa cliente){
        if(inicioFalha == -1)
            return false;
        
        if(inoperante){        
            linkDisponivel = true;
            return true;
        }
        
        Double next = sim.getTime(this) + tempoTransmitir(cliente.getTamComunicacao());
        
        if(next >= inicioFalha && sim.getTime(this) < inicioFalha){
            inoperante = true;
            filaPacotes.clear();
            return true;
        }
            
        return inoperante;
    }
    
   
    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        if(inoperante){
        	linkDisponivel = true;
            return;
        }
        
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmissão
        double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para chegada da tarefa no proximo servidor
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.CHEGADA,
                cliente.getCaminho().remove(0), cliente);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
        if (filaPacotes.isEmpty()) {
            //Indica que está livre
            this.linkDisponivel = true;
        } else {
            //Gera evento para atender proximo cliente da lista
            Tarefa proxCliente = filaPacotes.remove(0);
            evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.ATENDIMENTO,
                    this, proxCliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        }
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem cliente, int tipo) {
        if(cliente.getTarefa() == null)
            return;
        
        if (tipo == EventoFuturo.SAIDA_MENSAGEM) {
            
            tempoTransmitirMensagem += tempoTransmitir(cliente.getTamComunicacao());
            //Incrementa o número de Mbits transmitido por este link
            this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
            //Incrementa o tempo de transmissão
            double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
            this.getMetrica().incSegundosDeTransmissao(tempoTrans);
            //Gera evento para chegada da mensagem no proximo servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this) + tempoTrans,
                    EventoFuturo.MENSAGEM,
                    cliente.getCaminho().remove(0), cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
            if (!filaMensagens.isEmpty()) {
                //Gera evento para chegada da mensagem no proximo servidor
                evtFut = new EventoFuturo(
                        simulacao.getTime(this) + tempoTrans,
                        EventoFuturo.SAIDA_MENSAGEM,
                        this, filaMensagens.remove(0));
                //Event adicionado a lista de evntos futuros
                simulacao.addEventoFuturo(evtFut);
            } else {
                linkDisponivelMensagem = true;
            }
        } else if (linkDisponivelMensagem) {
            linkDisponivelMensagem = false;
            //Gera evento para chegada da mensagem no proximo servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.SAIDA_MENSAGEM,
                    this, cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        } else {
            filaMensagens.add(cliente);
        }
        
    }
    
    @Override
    public Integer getCargaTarefas() {
        if (linkDisponivel && linkDisponivelMensagem) {
            return 0;
        } else {
            return (filaMensagens.size() + filaPacotes.size()) + 1;
        }
    }

    @Override
    public void limparEscalonador() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void setInicioFalha(double t){
        this.getMetrica().setTempoFalha(t);  
        super.setInicioFalha(t);
        inicioFalha = t;
    }
    
    @Override
    public void setFimFalha(double t){
        this.getMetrica().setTempoRecuperacao(t);  
        fimFalha = t;
    }

    @Override
    public boolean isInoperante() {
        return inoperante;
    }
    
    @Override
    public void setInoperante(boolean inoperante) {
        this.inoperante = inoperante;
    }
    
    
}
