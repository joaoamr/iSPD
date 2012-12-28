/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo;

import ispd.escalonador.Escalonador;
import ispd.escalonador.Mestre;
import ispd.motor.Mensagens;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cassio
 */
public class M_OSEP extends Escalonador {

    Tarefa tarefaSelec;
    List<StatusUser> status;
    int contador = 0;
    double poderTotal = 0.0;

    public M_OSEP() {
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new ArrayList<CS_Processamento>();
    }

    @Override
    public void iniciar() {
        this.mestre.setTipoEscalonamento(Mestre.AMBOS);
        status = new ArrayList<StatusUser>();
        for (int i = 0; i < metricaUsuarios.getUsuarios().size(); i++) {
            status.add(new StatusUser(metricaUsuarios.getUsuarios().get(i), metricaUsuarios.getPoderComputacional(metricaUsuarios.getUsuarios().get(i))));
        }
    }

    @Override
    public Tarefa escalonarTarefa() {
        //Escalona tarefa com política FIFO
        if(tarefas.size() > 0){
            return tarefas.remove(0);
        }
        else{
            return null;
        }
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        int aux = 0;
        //Buscando recurso livre
        CS_Processamento selec = null;
        for (int i = 0; i < escravos.size(); i++) {
            if (escravos.get(i).getInformacaoDinamicaFila().isEmpty() && escravos.get(i).getInformacaoDinamicaProcessador().isEmpty()){
                if (selec == null) {
                    selec = escravos.get(i);
                } else if (Math.abs(escravos.get(i).getPoderComputacional() - tarefaSelec.getTamProcessamento())
                        < Math.abs(selec.getPoderComputacional() - tarefaSelec.getTamProcessamento())) {
                    selec = escravos.get(i);
               }
            }
        }
        if (selec != null) {
           return selec;
        }
        //Buscando recurso para sofrer preempção
        Double penalidade = null;
        for (int i = 0; i < escravos.size(); i++) {
            if(escravos.get(i).getInformacaoDinamicaProcessador().size() == 1){
                if(escravos.get(i).getInformacaoDinamicaFila().isEmpty()){
                    Tarefa tar = (Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0);
                    int indexEscravo = metricaUsuarios.getUsuarios().indexOf(tar.getProprietario());
                    Double cota = status.get(indexEscravo).GetCota();
                    Double uso = status.get(indexEscravo).GetUso();
                    if (uso > cota ) {
                        if (penalidade == null) {
                            penalidade = uso - escravos.get(i).getPoderComputacional() - cota;
                            selec = escravos.get(i);
                        } else {
                            if (penalidade < (uso - escravos.get(i).getPoderComputacional() - cota)) {
                                penalidade = uso - escravos.get(i).getPoderComputacional() - cota;
                                selec = escravos.get(i);
                            }
                        }
                        aux++;
                    }
                }
            }
        }
        //Fazer a preempção
        if (selec != null) {
            //Verifica se vale apena fazer preempção
            Tarefa tar = (Tarefa) selec.getInformacaoDinamicaProcessador().get(0);
            int indexUserEscravo = metricaUsuarios.getUsuarios().indexOf(tar.getProprietario());
            int indexUserEspera = metricaUsuarios.getUsuarios().indexOf(tarefaSelec.getProprietario());
            Double penalidaUserEscravo = status.get(indexUserEscravo).GetUso() - selec.getPoderComputacional() - status.get(indexUserEscravo).GetCota();
            Double penalidaUserEspera = status.get(indexUserEspera).GetUso() + selec.getPoderComputacional() - status.get(indexUserEspera).GetCota();
            if (penalidaUserEscravo < penalidaUserEspera && !((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getProprietario().equals(tarefaSelec.getProprietario()) && selec.getInformacaoDinamicaFila().isEmpty() && selec.getInformacaoDinamicaProcessador().size() == 1) {
                System.out.println("Preempção: Tarefa " + ((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getIdentificador() + " do user " + ((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getProprietario() + " <=> " + tarefaSelec.getIdentificador() + " do user " + tarefaSelec.getProprietario());
                mestre.enviarMensagem((Tarefa) selec.getInformacaoDinamicaProcessador().get(0), selec, Mensagens.DEVOLVER_COM_PREEMPCAO);
                selec.getInformacaoDinamicaProcessador().remove(selec.getInformacaoDinamicaProcessador().get(0));
                return selec;
             }
            if(tarefas.size() > 0 && contador > 0){
                contador--;
                mestre.executarEscalonamento();
            }
            else{
                contador = aux;
            }
        }
        return null;
    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = escravos.indexOf(destino);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        Tarefa trf = escalonarTarefa();
        tarefaSelec = trf;
        if (trf != null) {
            CS_Processamento rec = escalonarRecurso();
            if (rec != null) {
        
                    trf.setLocalProcessamento(rec);
                    trf.setCaminho(escalonarRota(rec));
                    mestre.enviarTarefa(trf);
                    rec.getInformacaoDinamicaProcessador().add(trf);
                    status.get(metricaUsuarios.getUsuarios().indexOf(trf.getProprietario())).AtualizaUso(rec.getPoderComputacional(), 1);
                    System.out.println("Tarefa " + trf.getIdentificador() + " do user " + trf.getProprietario() + " foi escalonado" + mestre.getSimulacao().getTime());
                    System.out.printf("Escravo %s executando %d\n", rec.getId(), rec.getInformacaoDinamicaProcessador().size());
                    for(int i = 0;i < escravos.size(); i++){
                        if(escravos.get(i).getInformacaoDinamicaProcessador().size() > 1){
                            if(escravos.get(i).getInformacaoDinamicaFila().size() > 0){
                                System.out.println("Tem Fila");
                            }
                            System.out.println("PROBLEMA1");
                            System.out.printf("Escravo %s executando %d\n", escravos.get(i).getId(), escravos.get(i).getInformacaoDinamicaProcessador().size());
                        }
                    }
            }else {
                tarefas.add(trf);
                tarefaSelec = null;
            }
        }
    }

    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        int indexUser = metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
        status.get(indexUser).AtualizaUso(maq.getPoderComputacional(), 0);
        maq.getInformacaoDinamicaProcessador().remove(tarefa);
        System.out.println("Tarefa " + tarefa.getIdentificador() + " do user " + tarefa.getProprietario() + " concluida " + mestre.getSimulacao().getTime() + " O usuário perdeu " + maq.getPoderComputacional() + " MFLOPS");
   }

    @Override
    public void adicionarTarefa(Tarefa tarefa) {
        super.adicionarTarefa(tarefa);
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        int indexUser;
        if (tarefa.getLocalProcessamento() != null) {
            System.out.printf("Tarefa %d do usuário %s sofreu preempção\n", tarefa.getIdentificador(), tarefa.getProprietario());
            indexUser = metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
            status.get(indexUser).AtualizaUso(maq.getPoderComputacional(), 0);
        } else {
            System.out.println("Tarefa " + tarefa.getIdentificador() + " do user " + tarefa.getProprietario() + " chegou " + mestre.getSimulacao().getTime());
        }
        for (int i = 0; i < status.size(); i++) {
              System.out.printf("Usuário %s : %f de %f\n", status.get(i).usuario, status.get(i).GetUso(), status.get(i).GetCota());
        }
    }

    @Override
    public Double getTempoAtualizar() {
        return 1.0;
    }

    private class StatusUser {

        private String usuario;
        private Double PoderEmUso;
        private Double Cota;

        public StatusUser(String usuario, Double poder) {
            this.usuario = usuario;
            this.PoderEmUso = 0.0;
            this.Cota = poder;
            poderTotal+=poder;
        }

        public void AtualizaUso(Double poder, int opc) {
            if (opc == 1) {
                this.PoderEmUso = this.PoderEmUso + poder;
            } else {
                this.PoderEmUso = this.PoderEmUso - poder;
            }
        }

        public Double GetCota() {
            return this.Cota;
        }

        public Double GetUso() {
            return this.PoderEmUso;
        }
    }
}