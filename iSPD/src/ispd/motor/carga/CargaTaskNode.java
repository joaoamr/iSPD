/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.carga;

import NumerosAleatorios.GeracaoNumAleatorios;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Descreve como gerar tarefas para um nó escalonador
 * @author denison_usuario
 */
public class CargaTaskNode extends GerarCarga {

    private String aplicacao;
    private String proprietario;
    private String escalonador;
    private int numeroTarefas;
    private Double minComputacao;
    private Double maxComputacao;
    private Double minComunicacao;
    private Double maxComunicacao;

    public CargaTaskNode(String aplicacao, String proprietario, String escalonador, int numeroTarefas, double maxComputacao, double minComputacao, double maxComunicacao, double minComunicacao) {
        this.aplicacao = aplicacao;
        this.proprietario = proprietario;
        this.escalonador = escalonador;
        this.numeroTarefas = numeroTarefas;
        this.minComputacao = minComputacao;
        this.maxComputacao = maxComputacao;
        this.minComunicacao = minComunicacao;
        this.maxComunicacao = maxComunicacao;
    }

    @Override
    public Vector toVector() {
        Vector temp = new Vector<Integer>(8);
        temp.add(0, aplicacao);
        temp.add(1, proprietario);
        temp.add(2, escalonador);
        temp.add(3, numeroTarefas);
        temp.add(4, maxComputacao);
        temp.add(5, minComputacao);
        temp.add(6, maxComunicacao);
        temp.add(7, minComunicacao);
        return temp;
    }

    @Override
    public List<Tarefa> toTarefaList(List<CS_Processamento> mestres) {
        List<Tarefa> tarefas = new ArrayList<Tarefa>();
        CS_Processamento mestre = null;
        int i = 0;
        boolean encontrou = false;
        while (!encontrou && i < mestres.size()) {
            if (mestres.get(i).getId().equals(this.escalonador)) {
                encontrou = true;
                mestre = mestres.get(i);
            }
            i++;
        }
        if (encontrou) {
            GeracaoNumAleatorios gerador = new GeracaoNumAleatorios((int)System.currentTimeMillis());
            for (i = 0; i < this.getNumeroTarefas(); i++) {
                Tarefa tarefa = new Tarefa(
                        aplicacao,
                        mestre,
                        gerador.twoStageUniform(minComunicacao, minComunicacao + (maxComunicacao - minComunicacao) / 2, maxComunicacao, 1),
                        0.0009765625 /*arquivo recebimento*/,
                        gerador.twoStageUniform(minComputacao, minComputacao + (maxComputacao - minComputacao) / 2, maxComputacao, 1),
                        gerador.exponencial(0.05));
                tarefas.add(tarefa);
            }
        }
        return tarefas;
    }

    @Override
    public String toString() {
        return String.format("%s %d %f %f %f %f",
                this.escalonador, this.numeroTarefas,
                this.maxComputacao, this.minComputacao,
                this.maxComunicacao, this.minComunicacao);
    }

    public static GerarCarga newGerarCarga(String entrada) {
        CargaTaskNode newObj = null;
        //try {
        String[] valores = entrada.split(" ");
        String aplicacao = "application0";
        String proprietario = "user1";
        String escalonador = valores[0];
        int numeroTarefas = Integer.parseInt(valores[1]);
        double maxComputacao = Double.parseDouble(valores[2]);
        double minComputacao = Double.parseDouble(valores[3]);
        double maxComunicacao = Double.parseDouble(valores[4]);
        double minComunicacao = Double.parseDouble(valores[5]);
        newObj = new CargaTaskNode(aplicacao, proprietario, escalonador,
                numeroTarefas, maxComputacao, minComputacao, maxComunicacao, minComunicacao);
        //} catch (Exception e) {
        //Logger.getLogger(CargaTaskNode.class.getName()).log(Level.SEVERE, null, e);
        //}
        return newObj;
    }

    public int getTipo() {
        return NULL;
    }

    //Gets e Sets
    public String getEscalonador() {
        return escalonador;
    }

    public void setEscalonador(String escalonador) {
        this.escalonador = escalonador;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }

    public Double getMaxComputacao() {
        return maxComputacao;
    }

    public void setMaxComputacao(double maxComputacao) {
        this.maxComputacao = maxComputacao;
    }

    public Double getMaxComunicacao() {
        return maxComunicacao;
    }

    public void setMaxComunicacao(double maxComunicacao) {
        this.maxComunicacao = maxComunicacao;
    }

    public Double getMinComputacao() {
        return minComputacao;
    }

    public void setMinComputacao(double minComputacao) {
        this.minComputacao = minComputacao;
    }

    public Double getMinComunicacao() {
        return minComunicacao;
    }

    public void setMinComunicacao(double minComunicacao) {
        this.minComunicacao = minComunicacao;
    }

    public Integer getNumeroTarefas() {
        return numeroTarefas;
    }

    public void setNumeroTarefas(int numeroTarefas) {
        this.numeroTarefas = numeroTarefas;
    }

    public String getProprietario() {
        return proprietario;
    }

    public void setProprietario(String proprietario) {
        this.proprietario = proprietario;
    }
}
