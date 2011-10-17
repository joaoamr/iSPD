/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores;

import ispd.motor.EventoFuturo;
import ispd.motor.Simulacao;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author denison_usuario
 */
public class CS_Link extends CS_Comunicacao {

    private CentroServico conexoesEntrada;
    private CentroServico conexoesSaida;
    private List<Tarefa> filaPacotes;
    private List<Mensagem> filaMensagens;
    private boolean linkDisponivel;
    private boolean linkDisponivelMensagem;
    private double tempoTransmitirMensagem;

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

    public CentroServico getConexoesSaida() {
        return conexoesSaida;
    }

    public void setConexoesSaida(CentroServico conexoesSaida) {
        this.conexoesSaida = conexoesSaida;
    }

    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        cliente.iniciarEsperaComunicacao(simulacao.getTime());
        if (linkDisponivel) {
            //indica que recurso está ocupado
            linkDisponivel = false;
            //cria evento para iniciar o atendimento imediatamente
            EventoFuturo novoEvt = new EventoFuturo(
                    simulacao.getTime(),
                    EventoFuturo.ATENDIMENTO,
                    this,
                    cliente);
            simulacao.getEventos().offer(novoEvt);
        } else {
            filaPacotes.add(cliente);
        }
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        if (!conexoesSaida.equals(cliente.getCaminho().get(0))) {
            throw new IllegalArgumentException("O destino da mensagem é um recurso sem conexão com este link");
        } else {
            cliente.finalizarEsperaComunicacao(simulacao.getTime());
            cliente.iniciarAtendimentoComunicacao(simulacao.getTime());
            //Gera evento para atender proximo cliente da lista
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime() + tempoTransmitir(cliente.getTamComunicacao()),
                    EventoFuturo.SAÍDA,
                    this, cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.getEventos().offer(evtFut);
        }
    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmissão
        double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoComunicacao(simulacao.getTime());
        //Gera evento para chegada da tarefa no proximo servidor
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(),
                EventoFuturo.CHEGADA,
                cliente.getCaminho().remove(0), cliente);
        //Event adicionado a lista de evntos futuros
        simulacao.getEventos().offer(evtFut);
        if (filaPacotes.isEmpty()) {
            //Indica que está livre
            this.linkDisponivel = true;
        } else {
            //Gera evento para atender proximo cliente da lista
            Tarefa proxCliente = filaPacotes.remove(0);
            evtFut = new EventoFuturo(
                    simulacao.getTime(),
                    EventoFuturo.ATENDIMENTO,
                    this, proxCliente);
            //Event adicionado a lista de evntos futuros
            simulacao.getEventos().offer(evtFut);
        }
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem cliente, int tipo) {
        if (tipo == EventoFuturo.SAIDA_MENSAGEM) {
            tempoTransmitirMensagem += tempoTransmitir(cliente.getTamComunicacao());
            //Incrementa o número de Mbits transmitido por este link
            this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
            //Incrementa o tempo de transmissão
            double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
            this.getMetrica().incSegundosDeTransmissao(tempoTrans);
            //Gera evento para chegada da mensagem no proximo servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime() + tempoTrans,
                    EventoFuturo.MENSAGEM,
                    cliente.getCaminho().remove(0), cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.getEventos().offer(evtFut);
            if (!filaMensagens.isEmpty()) {
                //Gera evento para chegada da mensagem no proximo servidor
                evtFut = new EventoFuturo(
                        simulacao.getTime() + tempoTrans,
                        EventoFuturo.SAIDA_MENSAGEM,
                        this, filaMensagens.remove(0));
                //Event adicionado a lista de evntos futuros
                simulacao.getEventos().offer(evtFut);
            }else{
                linkDisponivelMensagem = true;
            }
        } else if(linkDisponivelMensagem){
            linkDisponivelMensagem = false;
                //Gera evento para chegada da mensagem no proximo servidor
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(),
                        EventoFuturo.SAIDA_MENSAGEM,
                        this, cliente);
                //Event adicionado a lista de evntos futuros
                simulacao.getEventos().offer(evtFut);
        }else{
            filaMensagens.add(cliente);
        }
    }
}
