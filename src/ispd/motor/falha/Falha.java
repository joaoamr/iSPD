/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.falha;

import ispd.motor.EventoFuturo;
import ispd.motor.Mensagens;
import ispd.motor.Simulacao;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.List;

public abstract class Falha {
  
  public abstract void gerarFalha(List<CentroServico> cslist, RedeDeFilas rdf, Simulacao sim);
  
  protected void notificarCS(CentroServico recursonotificado, ArrayList<CentroServico> maquinas, double tempoinicial, double tempofinal, Simulacao sim){
        for(int i = 0; i < maquinas.size(); i++)
        {
            Mensagem msgfalha = new Mensagem(maquinas.get(i), Mensagens.NOTIFICAR_FALHA);
            
            EventoFuturo evt1 = new EventoFuturo(
                        tempoinicial,
                        EventoFuturo.MENSAGEM,
                        recursonotificado,
                        msgfalha);
            
            Mensagem msgrec = new Mensagem(maquinas.get(i), Mensagens.NOTIFICAR_RECUPERACAO);

            EventoFuturo evt2 = new EventoFuturo(
                        tempofinal,
                        EventoFuturo.MENSAGEM,
                        recursonotificado,
                        msgrec);
            
            sim.addEventoFuturo(evt1);
            sim.addEventoFuturo(evt2);
        }
    }
  
    protected void notificarCS(CentroServico recursonotificado, CentroServico maqfalhada, double tempoinicial, double tempofinal, Simulacao sim){
        Mensagem msgfalha = new Mensagem(maqfalhada, Mensagens.NOTIFICAR_FALHA);
            
        EventoFuturo evt1 = new EventoFuturo(
            tempoinicial,
            EventoFuturo.MENSAGEM,
            recursonotificado,
            msgfalha);
            
        Mensagem msgrec = new Mensagem(maqfalhada, Mensagens.NOTIFICAR_RECUPERACAO);

        EventoFuturo evt2 = new EventoFuturo(
            tempofinal,
            EventoFuturo.MENSAGEM,
            recursonotificado,
            msgrec);
            
        sim.addEventoFuturo(evt1);
        sim.addEventoFuturo(evt2);
    }
  
}