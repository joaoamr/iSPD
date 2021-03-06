/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.AlocacaoVM;

import ispd.escalonador.*;
import ispd.motor.Simulacao;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

/**
 * Interface que possui métodos implementados penas em um nó Mestre,
 * os métodos desta interface são utilizados pelos escalonadores
 * @author denison
 */
public interface VMM {
    //Tipos de escalonamentos
    public static final int ENQUANTO_HOUVER_TAREFAS = 1;
    public static final int QUANDO_RECEBE_RESULTADO = 2;
    public static final int AMBOS = 3;
    //Métodos que geram eventos
    public void enviarVM(CS_VirtualMac vm);
    public void processarVM(CS_VirtualMac vm);
    public void executarAlocacao();
    public void enviarTarefa(Tarefa tarefa);
    public void processarTarefa(Tarefa tarefa);
    public void executarEscalonamento();
    public void enviarMensagem(Tarefa tarefa, CS_Processamento maquina, int tipo);
    public void atualizar(CS_Processamento maquina);    
    //Get e Set
    public void setSimulacao(Simulacao simulacao);
    public int getTipoEscalonamento();
    public void setTipoEscalonamento(int tipo);

    public Tarefa criarCopia(Tarefa get);
    public Simulacao getSimulacao();
}