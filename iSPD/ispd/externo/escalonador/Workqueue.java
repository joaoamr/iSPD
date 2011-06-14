package ispd.externo.escalonador;
import ispd.motor.Tarefa;
import ispd.escalonador.Escalonador;
import java.util.ArrayList;
public class Workqueue extends Escalonador {
  int totalDeTarefas;
  int contador;
  public void iniciar() {
    totalDeTarefas = tarefas.size();
    contador = 0;
  }
  public void atualizar() {
    System.out.println("não é utilizado neste algoritmo");
  }
  public void escalonarTarefa() {}
  public void escalonarRecurso() {
    for (int i = 0; i < recursos.size()&&totalDeTarefas!=contador; i++) {
      if (recursos.get(i).numeroDeTarefas()==0) {
        recursos.get(i).AdicionarTarefa(tarefas.get(contador));
        contador++;
      }
    }
  }
  public void adicionarTarefa(Tarefa tarefa) {}
  public void adicionarFilaTarefa(ArrayList<Tarefa> tarefa) {}
}