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
public class OSEP extends Escalonador {

    int contadorEscravos;
    Tarefa tarefaSelec;
    List<CreditosUser> creditos;

    public OSEP() {
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new ArrayList<CS_Processamento>();
    }

    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        if (!tarefa.getProprietario().equals(maq.getProprietario())) {
            int indexUser, indexOwner;
            indexUser = metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
            indexOwner = metricaUsuarios.getUsuarios().indexOf(maq.getProprietario());
            creditos.get(indexUser).AtualizaUsado(tarefa.getTamProcessamento());
            creditos.get(indexOwner).AtualizaDoado(tarefa.getTamProcessamento());
        }
        maq.getInformacaoDinamicaProcessador().remove(tarefa);
        System.out.println("Tarefa " + tarefa.getIdentificador() + " do user "+tarefa.getProprietario()+" concluida "+mestre.getSimulacao().getTime());
    }

    @Override
    public void adicionarTarefa(Tarefa tarefa){
        super.adicionarTarefa(tarefa);
        System.out.println("Tarefa " + tarefa.getIdentificador() + " do user "+tarefa.getProprietario()+" chegou "+mestre.getSimulacao().getTime());
    }

    @Override
    public void iniciar() {
        this.mestre.setTipoEscalonamento(Mestre.AMBOS);
        contadorEscravos = 0;
        creditos = new ArrayList<CreditosUser>();
        for (String user : metricaUsuarios.getUsuarios()) {
            creditos.add(new CreditosUser(user));
        }
    }

    @Override
    public Tarefa escalonarTarefa() {
        int i = 0;
        if (escravoLivre()) {
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
            return selec;
        } else {
            //modificar...
            //mestre.enviarMensagem(tarefa em execução, escravo selecionado, Mensagens.DEVOLVER_COM_PREEMPCAO);
            return null;
        }

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
                    } else if (Math.abs(escravos.get(i).getPoderComputacional() - tarefaSelec.getTamProcessamento())
                            < Math.abs(selec.getPoderComputacional() - tarefaSelec.getTamProcessamento())) {
                        selec = escravos.get(i);
                    }
                }
            }
            if (selec != null) {
                return selec;
            }
            //Buscando tarefa com menor prioridade
            Double creditoTar = creditos.get(metricaUsuarios.getUsuarios().indexOf(tarefaSelec.getProprietario())).GetCredito();
            for (i = 1; i < escravos.size(); i++) {
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
            }
            return selec;
        }
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
                contadorEscravos++;
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

    private class CreditosUser {

        private String usuario;
        private Double RecursoUsado;
        private Double RecursoDoado;

        public CreditosUser(String usuario) {
            this.usuario = usuario;
            this.RecursoDoado = 0.0;
            this.RecursoUsado = 0.0;
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
    }
}
