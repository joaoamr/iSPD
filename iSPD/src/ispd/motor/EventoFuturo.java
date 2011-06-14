/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor;

/**
 *
 * @author denison_usuario
 */
public class EventoFuturo implements Comparable<EventoFuturo>{
    public static final int CRIAR_TAREFA = 1;
    public static final int RECEBER_MENSAGEM = 2;
    public static final int ENVIAR_MENSAGEM = 3;
    public static final int RECEBER_MENSAGEM_BLOQUEAR = 4;
    public static final int ENVIAR_MENSAGEM_BLOQUEAR = 5;

    private Double tempoOcorrencia;
    private int tipo;
    private Recurso agente;
    private Object tarefa;

    /**
     * Criacao de nova tarefa
     * @param time tempo do relógio em que foi criada
     * @param tipo deve ser CRIAR_TAREFA
     * @param agente recurso que vai criar a tarefa
     * @param tarefa tarefa a ser criada
     */
    public EventoFuturo(double time, int tipo, Recurso agente, Tarefa tarefa) {
        this.tempoOcorrencia = time;
        this.tipo = tipo;
        this.tarefa = tarefa;
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Informa o tipo do evento
     * @return Retorna o tipo do evento de acordo com as constantes da classe
     */
    public int getTipo() {
        return this.tipo;
    }

    /**
     * Retorna recurso que realiza a ação
     * @return recurso que deve executar ação
     */
    public Recurso getAgente() {
        return this.agente;
    }
    /**
     * Retorna tarefa alvo da ação
     * @return
     */
    public Tarefa getTarefa() {
        return (Tarefa) this.tarefa;
    }
    /**
     * Retorna tarefa alvo da ação
     * @return
     */
    public Recurso getDestino() {
        return null;
    }
    /**
     * Retorna tarefa alvo da ação
     * @return
     */
    public Recurso getOrigem() {
        return null;
    }
    /**
     * Comparação necessaria para utilizar PriorityQueue
     * @param o evento que será comparado
     * @return 0 se valores iguais, um menor que 0 se "o" inferior, e maior que 0 se "o" for maior.
     */
    public int compareTo(EventoFuturo o) {
        return tempoOcorrencia.compareTo( o.tempoOcorrencia );
    }

}
