/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui;
import ispd.gui.ParesOrdenadosUso;
import java.util.LinkedList;

/**
 *
 * @author dancosta
 */
public class usoUsuarios 
{
    private String nome;
    private LinkedList<ParesOrdenadosUso> lista_users = new LinkedList();
    private double poder=0;
    public usoUsuarios(String nome)    
    {
        this.nome=nome;
    }
    
    public void insere_periodos(double tempoInicial, double tempoFinal)
    {
        ParesOrdenadosUso par = new ParesOrdenadosUso(tempoInicial, tempoFinal);
        this.lista_users.add(par);
    }
    
    public String getNome()
    {
        return this.nome;
    }
    
    public void inserePoderLocalProcessamento(double poder)
    {
        this.poder = poder;
    }
    
    public double getPoderProcessamento()
    {
        return this.poder;
    }
    
    public LinkedList getPares()
    {
        return this.lista_users;
    }
}
