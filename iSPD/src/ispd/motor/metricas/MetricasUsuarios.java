/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.metricas;

import ispd.motor.filas.Tarefa;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author denison_usuario
 */
public class MetricasUsuarios {

    private List<String> usuarios;
    private List<Double> poderComputacional;
    private List<List> tarefasSubmetidas;
    private List<List> tarefasConcluidas;
    
    public MetricasUsuarios(){
        usuarios = new ArrayList<String>();
        poderComputacional = new ArrayList<Double>();
        tarefasSubmetidas = new ArrayList<List>();
        tarefasConcluidas = new ArrayList<List>();
    }
    
    public void addUsuario(String nome, Double poderComputacional){
        this.usuarios.add(nome);
        this.poderComputacional.add(poderComputacional);
        this.tarefasSubmetidas.add(new ArrayList<Tarefa>());
        this.tarefasConcluidas.add(new ArrayList<Tarefa>());
    }
    
    public void addAllUsuarios(List<String> nomes, List<Double> poderComputacional){
        for(int i = 0; i < nomes.size(); i++){
            this.usuarios.add(nomes.get(i));
            this.poderComputacional.add(poderComputacional.get(i));
            this.tarefasSubmetidas.add(new ArrayList<Tarefa>());
            this.tarefasConcluidas.add(new ArrayList<Tarefa>());
        }
    }
    
    public void addMetricasUsuarios(MetricasUsuarios mtc){
        for (int i = 0; i < mtc.usuarios.size(); i++) {
            int index = this.usuarios.indexOf(mtc.usuarios.get(i));
            if(index == -1){
                this.usuarios.add(mtc.usuarios.get(i));
                this.poderComputacional.add(mtc.poderComputacional.get(i));
                this.tarefasSubmetidas.add(mtc.tarefasSubmetidas.get(i));
                this.tarefasConcluidas.add(mtc.tarefasConcluidas.get(i));
            }else{
                this.tarefasSubmetidas.get(index).addAll(mtc.tarefasSubmetidas.get(i));
                this.tarefasConcluidas.get(index).addAll(mtc.tarefasConcluidas.get(i));
            }
        }
    }
    
    public void incTarefasSubmetidas(Tarefa tarefa){
        int index = this.usuarios.indexOf(tarefa.getProprietario());
        if(!this.tarefasSubmetidas.get(index).contains(tarefa)){
            this.tarefasSubmetidas.get(index).add(tarefa);
        }
    }
    
    public void incTarefasConcluidas(Tarefa tarefa){
        int index = this.usuarios.indexOf(tarefa.getProprietario());
        if(!this.tarefasConcluidas.get(index).contains(tarefa)){
            this.tarefasConcluidas.get(index).add(tarefa);
        }
    }
    
    @Override
    public String toString(){
        String texto = "";
        for (int i = 0; i < usuarios.size(); i++) {
            texto += "Usuario: "+usuarios.get(i)+" tarefas: sub "+tarefasSubmetidas.get(i).size()+" con "+tarefasConcluidas.get(i).size()+"\n";
        }
        return texto;
    }
}