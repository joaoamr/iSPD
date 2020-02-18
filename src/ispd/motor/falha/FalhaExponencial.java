/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.falha;

import NumerosAleatorios.GeracaoNumAleatorios;
import ispd.motor.EventoFuturo;
import ispd.motor.Mensagens;
import ispd.motor.Simulacao;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class FalhaExponencial extends Falha {

    private double p;
    private double media;
    private double mediainicial;
    private double duracao;
    private ArrayList<CentroServico> recursosfalhados;
    private int n;
    
    public FalhaExponencial(double mediainicial, double duracao, int n) {
        this.mediainicial = mediainicial;
        this.duracao = duracao;
        this.n = n;
        recursosfalhados = new ArrayList<CentroServico>();
    }
    
    @Override
    public void gerarFalha(List<CentroServico> cslist, RedeDeFilas rdf, Simulacao sim){
        recursosfalhados = new ArrayList<CentroServico>();
        GeracaoNumAleatorios random = new GeracaoNumAleatorios((int)System.currentTimeMillis());
        double tempoinicial, tempofinal;
        tempoinicial = random.exponencial(mediainicial);      
        tempofinal = tempoinicial + random.exponencial(duracao);
        
        int n = (int)random.exponencial(this.n);
        
        ArrayList<CentroServico> sorteiofalhas  = new ArrayList<CentroServico>(cslist);
        
        while(n > 0 && !sorteiofalhas.isEmpty()){
            Random rnd = new Random();
            int i = rnd.nextInt(sorteiofalhas.size());
            CentroServico cssorteado = sorteiofalhas.remove(i);
            cssorteado.setInicioFalha(tempoinicial);
            cssorteado.setFimFalha(tempofinal);
            recursosfalhados.add(cssorteado);
            n--;
        }
        
        for(int i  = 0; i < rdf.getMaquinas().size(); i++){
            notificarCS(rdf.getMaquinas().get(i), recursosfalhados, tempoinicial, tempofinal, sim);
        }
        
        for(int i  = 0; i < rdf.getMestres().size(); i++){
            notificarCS(rdf.getMestres().get(i), recursosfalhados, tempoinicial, tempofinal, sim);
        }
        
        for(int i  = 0; i < rdf.getLinks().size(); i++){
            notificarCS(rdf.getLinks().get(i), recursosfalhados, tempoinicial, tempofinal, sim);
        }
    }
    
    public double getMedia() {
        return media;
    }

    public ArrayList<CentroServico> getRecursosfalhados() {
        return recursosfalhados;
    }      

}
