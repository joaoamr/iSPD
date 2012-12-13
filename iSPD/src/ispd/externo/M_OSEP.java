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
import ispd.motor.metricas.MetricasUsuarios;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cassio
 */
public class M_OSEP extends Escalonador {

    int contadorEscravos;
    Tarefa tarefaSelec;
    List<StatusUser> status;

    public M_OSEP() {
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new ArrayList<CS_Processamento>();
    }

    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        int indexUser = metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
        status.get(indexUser).AtualizaUso(maq.getPoderComputacional(),0);
        super.addTarefaConcluida(tarefa);
       /* if (!tarefa.getProprietario().equals(maq.getProprietario())) {//Tarefas 
            int indexUser, indexOwner;
            indexUser = metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
            indexOwner = metricaUsuarios.getUsuarios().indexOf(maq.getProprietario());
            creditos.get(indexUser).AtualizaUsado(tarefa.getTamProcessamento());
            creditos.get(indexOwner).AtualizaDoado(tarefa.getTamProcessamento());
            creditos.get(indexUser).RemoveAtual(maq.getPoderComputacional());
        } */
        maq.getInformacaoDinamicaProcessador().remove(tarefa);
        System.out.println("Tarefa " + tarefa.getIdentificador() + " do user "+tarefa.getProprietario()+" concluida "+mestre.getSimulacao().getTime()+" O usuário perdeu "+ maq.getPoderComputacional()+" MFLOPS");
    }

    @Override
    public void adicionarTarefa(Tarefa tarefa){
        super.adicionarTarefa(tarefa);
        System.out.println("Tarefa " + tarefa.getIdentificador() + " do user "+tarefa.getProprietario()+" chegou "+mestre.getSimulacao().getTime());
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        int indexUser;
        if(tarefa.getLocalProcessamento() != null){
            System.out.printf("Tarefa %d do usuário %s sofreu preempção\n",tarefa.getIdentificador(),tarefa.getProprietario());
            indexUser = metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
            //creditos.get(indexUser).RemoveAtual(maq.getPoderComputacional());
            ((CS_Processamento) tarefa.getLocalProcessamento()).getInformacaoDinamicaProcessador().remove(tarefa);
            status.get(indexUser).AtualizaUso(maq.getPoderComputacional(),0);
        }
    }

    @Override
    public void iniciar() {
        this.mestre.setTipoEscalonamento(Mestre.AMBOS);
        contadorEscravos = 0;
        status = new ArrayList<StatusUser>();
        for (int i = 0;i < metricaUsuarios.getUsuarios().size();i++) {
            status.add(new StatusUser(metricaUsuarios.getUsuarios().get(i),metricaUsuarios.getPoderComputacional(metricaUsuarios.getUsuarios().get(i))));
        }
    }

    @Override
    public Tarefa escalonarTarefa() {
        int i = 0;
        return tarefas.remove(0);
        /*if (escravoLivre()) {
            Tarefa selec = tarefas.get(0);
            for (i = 1; i < tarefas.size(); i++) {
                int indexSelec = metricaUsuarios.getUsuarios().indexOf(selec.getProprietario());
                int indexTar = metricaUsuarios.getUsuarios().indexOf(tarefas.get(i).getProprietario());
                if (creditos.get(indexTar).GetCredito() > creditos.get(indexSelec).GetCredito()) {
                    selec = tarefas.get(i);
                } else if (creditos.get(indexTar).GetCredito() == creditos.get(indexSelec).GetCredito() && tarefas.get(i).getTimeCriacao() < selec.getTimeCriacao()) {
                    selec = tarefas.get(i);
                }
            }
            tarefas.remove(selec);
            selec.getProprietario();
            return selec;
        } else {
            double total = 0.0;     
            int usuario = -1;
            for(i = 0; i<creditos.size();i++){
                if(total < creditos.get(i).GetAtual() -  metricaUsuarios.getPoderComputacional(creditos.get(i).GetUsuario())){
                    total = creditos.get(i).GetAtual() - metricaUsuarios.getPoderComputacional(creditos.get(i).GetUsuario());
                    usuario = i;
                }
            }
            if(usuario == -1){
                return null;
            }
            else{
                Tarefa selec = tarefas.get(0);
            for (i = 1; i < tarefas.size(); i++) {
                int indexSelec = metricaUsuarios.getUsuarios().indexOf(selec.getProprietario());
                int indexTar = metricaUsuarios.getUsuarios().indexOf(tarefas.get(i).getProprietario());
                if (creditos.get(indexTar).GetCredito() > creditos.get(indexSelec).GetCredito()) {
                    selec = tarefas.get(i);
                } else if (creditos.get(indexTar).GetCredito() == creditos.get(indexSelec).GetCredito() && tarefas.get(i).getTimeCriacao() < selec.getTimeCriacao()) {
                    selec = tarefas.get(i);
                }
            }
            if(creditos.get(metricaUsuarios.getUsuarios().indexOf(selec.getProprietario())).GetAtual() - metricaUsuarios.getPoderComputacional(selec.getProprietario()) > 0){
                return null;
            }
            Double creditoTar = creditos.get(metricaUsuarios.getUsuarios().indexOf(selec.getProprietario())).GetCredito();
            CS_Processamento rec;
            rec = null;
            for (i = 1; i < escravos.size(); i++) {
                if(escravos.get(i).getProprietario() == creditos.get(usuario).GetUsuario()){
                    Tarefa tar = (Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0);
                int indexEscravo = metricaUsuarios.getUsuarios().indexOf(tar.getProprietario());
                if (creditoTar > creditos.get(indexEscravo).GetCredito()) {
                    if (rec == null) {
                        rec = escravos.get(i);
                    } else if (Math.abs(escravos.get(i).getPoderComputacional() - tarefaSelec.getTamProcessamento())
                            < Math.abs(rec.getPoderComputacional() - tarefaSelec.getTamProcessamento())) {
                        rec = escravos.get(i);
                    }
                }
                }
                
                
            }
            if((creditos.get(usuario).GetAtual() - rec.getPoderComputacional()) - metricaUsuarios.getPoderComputacional(creditos.get(usuario).GetUsuario()) >= 0){
                mestre.enviarMensagem( (Tarefa) rec.getInformacaoDinamicaProcessador().get(0),rec, Mensagens.DEVOLVER_COM_PREEMPCAO);
                rec.getInformacaoDinamicaProcessador().remove(0);
                tarefas.remove(selec);
                return selec;
            }
                    
            }
            //mestre.enviarMensagem(tarefa em execução, escravo selecionado, Mensagens.DEVOLVER_COM_PREEMPCAO);
            return null;
        }*/

    }
    
    
    @Override
    public CS_Processamento escalonarRecurso() {
        int i;
        if (contadorEscravos < escravos.size()) {
            return escravos.get(contadorEscravos);
        } else {
            //Buscando recurso livre
            CS_Processamento selec = null;
            for (i = 1; i < escravos.size(); i++) {
                if (escravos.get(i).getInformacaoDinamicaProcessador().isEmpty()) {
                    if (selec == null) {
                        selec = escravos.get(i);
                    } else if (Math.abs(escravos.get(i).getPoderComputacional() - tarefas.get(0).getTamProcessamento())
                            < Math.abs(selec.getPoderComputacional() - tarefas.get(0).getTamProcessamento())) {
                        selec = escravos.get(i);
                    }
                }
            }
            if (selec != null) {
                return selec;
            }
            //Buscando tarefa com menor prioridade
            /*Double creditoTar = creditos.get(metricaUsuarios.getUsuarios().indexOf(tarefaSelec.getProprietario())).GetCredito();
        for (i = 1; i < escravos.size(); i++){
                Tarefa tar = (Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0);
                int indexEscravo = metricaUsuarios.getUsuarios().indexOf(tar.getProprietario());
                if (creditoTar > creditos.get(indexEscravo).GetCredito()) {
                    if (selec == null) {
                        selec = escravos.get(i);
                    } else if (Math.abs(escravos.get(i).getPoderComputacional() - tarefaSelec.getTamProcessamento())
                            < Math.abs(selec.getPoderComputacional() - tarefaSelec.getTamProcessamento())) {
                        selec = escravos.get(i);
                    }
                }
            }*/
            int escolhido = -1;
            Double penalidade = 0.0;
            i = 0;
            for(i = 0; i<escravos.size() ; i++){
                if(escravos.get(i).getInformacaoDinamicaProcessador().isEmpty()){
                    return escravos.get(i);
                }
                Tarefa tar = (Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0);
                int indexEscravo = metricaUsuarios.getUsuarios().indexOf(tar.getProprietario());
                Double cota = status.get(indexEscravo).GetCota();
                Double uso = status.get(indexEscravo).GetUso();
                if(uso > cota){
                    if(penalidade == 0.0){
                         escolhido = i;
                         penalidade = uso - escravos.get(i).getPoderComputacional() - cota;
                    }
                    else{
                        if(penalidade < (uso - escravos.get(i).getPoderComputacional() - cota) /*&& (uso - escravos.get(i).getPoderComputacional() - cota) > 0)*/){
                          escolhido = i;
                          penalidade = uso - escravos.get(i).getPoderComputacional() - cota;
                       }
                    }
                }
            }
            if(escolhido != -1){
                System.out.println("Teste1"+escravos.get(escolhido).getId() + " " +escravos.get(escolhido).getInformacaoDinamicaProcessador().size()+" "+escravos.get(escolhido).getListaProcessamento().size());
                mestre.enviarMensagem( (Tarefa) escravos.get(escolhido).getInformacaoDinamicaProcessador().get(0),escravos.get(escolhido), Mensagens.DEVOLVER_COM_PREEMPCAO);
                escravos.get(escolhido).getInformacaoDinamicaProcessador().remove(0);
                return escravos.get(escolhido);
            }
            else{
                return null;
            }
     }
    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = escravos.indexOf(destino);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        CS_Processamento rec = escalonarRecurso();
        if (rec != null) {
            Tarefa trf = escalonarTarefa();
            if (trf != null) {
                
                trf.setLocalProcessamento(rec);
                trf.setCaminho(escalonarRota(rec));
                System.out.println("Tarefa " + trf.getIdentificador() + " do user "+trf.getProprietario()+" foi escalonado"+mestre.getSimulacao().getTime());
                
                mestre.enviarTarefa(trf);
                rec.getInformacaoDinamicaProcessador().add(trf);
                contadorEscravos++;
                //creditos.get(metricaUsuarios.getUsuarios().indexOf(tarefaSelec.getProprietario())).AdidcionaAtual(rec.getPoderComputacional());                
                status.get(metricaUsuarios.getUsuarios().indexOf(trf.getProprietario())).AtualizaUso(rec.getPoderComputacional(),1);
                for(int i = 0;i<status.size();i++){
                   System.out.printf("Usuário %s : %f de %f\n",status.get(i).usuario,status.get(i).GetUso(),status.get(i).GetCota());
                }
            }
        }
    }

    @Override
    public Double getTempoAtualizar() {
        return 1.0;
    }

    private boolean escravoLivre() {
        if (contadorEscravos < escravos.size()) {
            return true;
        } else {
            for (int i = 1; i < escravos.size(); i++) {
                 if (escravos.get(i).getInformacaoDinamicaProcessador().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
    //penalidade =  (poderEmUso - recurso) - cota , poderEmuso > cota 
    private class StatusUser {
        private String usuario;
        private Double PoderEmUso;
        private Double Cota;
        
        public StatusUser(String usuario,Double poder){
            this.usuario = usuario;
            this.PoderEmUso = 0.0;
            this.Cota =  poder;
        }
        
        public void AtualizaUso(Double poder,int opc){
            if(opc == 1)
                this.PoderEmUso = this.PoderEmUso + poder;
            else{
                this.PoderEmUso = this.PoderEmUso - poder;
            }
        }
        
        public Double GetCota(){
            return this.Cota;
        }
        
        public Double GetUso(){
            return this.PoderEmUso;
        }
    }

    /*private class CreditosUser {
            
        private String usuario;
        private Double RecursoUsado;
        private Double RecursoDoado;
        private Double PoderEmUso;

        public CreditosUser(String usuario) {
            this.usuario = usuario;
            this.RecursoDoado = 0.0;
            this.RecursoUsado = 0.0;
            this.PoderEmUso = 0.0;
        }
        
        public void AdidcionaAtual(Double poder){
            this.PoderEmUso +=  poder;
        }
        
        public void RemoveAtual(Double poder){
            this.PoderEmUso -=  poder;
        }
            

        public void AtualizaDoado(Double poder) {
            this.RecursoDoado += poder;
        }

        public void AtualizaUsado(Double poder) {
            this.RecursoUsado += poder;
        }

        public Double GetCredito() {
            return RecursoDoado - RecursoUsado;
        }

        public String GetUsuario() {
            return usuario;
        }
        
        public Double GetAtual(){
            return this.PoderEmUso;
        }
    }*/
}
