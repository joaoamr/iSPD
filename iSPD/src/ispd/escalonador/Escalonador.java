
package ispd.escalonador;

import ispd.motor.Recurso;
import ispd.motor.Tarefa;
import java.util.ArrayList;
/**
 * Classe abstrata que define os métodos que devem ser implementados por cada política de escalonamento
 * 
 * @author diogots
 */
public abstract class Escalonador {
    //Atributos
    protected ArrayList<Recurso> recursos;
    protected ArrayList<Tarefa> tarefas;
    //Métodos
    public abstract void iniciar();
    public abstract void atualizar();
    public abstract void escalonarTarefa();
    public abstract void escalonarRecurso();
    public abstract void adicionarTarefa(Tarefa tarefa);
    public abstract void adicionarFilaTarefa(ArrayList<Tarefa> tarefa);
    //Get e Set
    public void setRecursos(ArrayList<Recurso> recursos) {
        this.recursos = recursos;
    }
    public void setTarefas(ArrayList<Tarefa> tarefas) {
        this.tarefas = tarefas;
    }
}