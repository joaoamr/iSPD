/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.List;

/**
 *
 * @author denison_usuario
 */
public class Mensagem implements Cliente {
    
    public static final int CANCELAR = 1;
    public static final int PARAR = 2;
    public static final int DEVOLVER = 3;
    public static final int DEVOLVER_COM_PREEMPCAO = 4;
    public static final int ATUALIZAR = 5;
    public static final int RESULTADO_ATUALIZAR = 6;
    public static final int PING = 7;
    public static final int PONG = 8;
    
    private int tipo;
    private Tarefa tarefa;
    private CentroServico origem;
    private List<CentroServico> caminho;
    private double tamComunicacao;

    public Mensagem(CS_Processamento origem, int tipo) {
        this.origem = origem;
        this.tipo = tipo;
        this.tamComunicacao = 0.011444091796875;
    }
    
    public Mensagem(CS_Processamento origem, int tipo, Tarefa tarefa) {
        this.origem = origem;
        this.tipo = tipo;
        this.tamComunicacao = 0.011444091796875;
        this.tarefa = tarefa;
    }
    
    public Mensagem(CS_Processamento origem, double tamComunicacao, int tipo) {
        this.origem = origem;
        this.tipo = tipo;
        this.tamComunicacao = tamComunicacao; 
    }
    
    public double getTamComunicacao() {
        return tamComunicacao;
    }

    public double getTamProcessamento() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CentroServico getOrigem() {
        return origem;
    }

    public List<CentroServico> getCaminho() {
        return caminho;
    }

    public void setCaminho(List<CentroServico> caminho) {
        this.caminho = caminho;
    }
    
    public int getTipo(){
        return tipo;
    }
    
    public Tarefa getTarefa(){
        return tarefa;
    }
}
