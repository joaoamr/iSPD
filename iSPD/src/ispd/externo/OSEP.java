/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo;

import Simulacao.Simulacao;
import ispd.escalonador.Escalonador;
import ispd.escalonador.Mestre;
import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
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
public class OSEP extends Escalonador {

    Tarefa tarefaSelec;
    List<StatusUser> status;
    List<ControleEscravos> controleEscravos;
    List<Tarefa> esperaTarefas;
    List<ControlePreempcao> controlePreempcao;
    int contadorEscravos;
    int numEscravosLivres;
    int numEscravosPreemp;

    public OSEP() {

        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new ArrayList<CS_Processamento>();
        this.controleEscravos = new ArrayList<ControleEscravos>();
        this.esperaTarefas = new ArrayList<Tarefa>();
        this.controlePreempcao = new ArrayList<ControlePreempcao>();
        this.status = new ArrayList<StatusUser>();
        this.contadorEscravos = 0;
        this.numEscravosLivres = 0;
        this.numEscravosPreemp = 0;

    }

    @Override
    public void iniciar() {

        this.mestre.setTipoEscalonamento(Mestre.AMBOS);//Escalonamento quando chegam tarefas e quando tarefas são concluídas

        for (int i = 0; i < metricaUsuarios.getUsuarios().size(); i++) {//Objetos de controle de uso e cota para cada um dos usuários
            String user = metricaUsuarios.getUsuarios().get(i);
            status.add(new StatusUser(user, metricaUsuarios.getPoderComputacional(user)));
        }

        for (int j = 0; j < escravos.size(); j++) {//Contadores para lidar com a dinamicidade dos dados
            controleEscravos.add(new ControleEscravos());
        }

        numEscravosLivres = escravos.size();

        for (int k = 0; k < status.size(); k++) {
            System.out.printf("Usuário %s : %d Máquinas\n", status.get(k).usuario, status.get(k).GetNumCota());
        }

    }

    @Override
    public Tarefa escalonarTarefa() {

        int indexFmax = -1;
        int fmax = -1;
        int indexTarefa = -1;


        for (int i = 0; i < metricaUsuarios.getUsuarios().size(); i++) {
            if (status.get(i).GetNumUso() < status.get(i).GetNumCota() && status.get(i).GetDemanda() > 0) {
                if (indexFmax == -1) {
                    indexFmax = i;
                    fmax = status.get(i).GetNumCota() - status.get(i).GetNumUso();
                } else {
                    if (fmax < status.get(i).GetNumCota() - status.get(i).GetNumUso()) {
                        indexFmax = i;
                        fmax = status.get(i).GetNumCota() - status.get(i).GetNumUso();
                    }
                }
            }
        }

        if (indexFmax != -1) {
            for (int i = 0; i < tarefas.size(); i++) {
                if (tarefas.get(i).getProprietario().equals(metricaUsuarios.getUsuarios().get(indexFmax))) {

                    indexTarefa = i;
                    break;

                }
            }
        }



        if (indexTarefa != -1) {
            return tarefas.get(indexTarefa);
        } else {
            if (tarefas.size() > 0) {
                return tarefas.get(0);
            }
            return null;
        }

    }

    @Override
    public CS_Processamento escalonarRecurso() {
        //Buscando recurso livre
        CS_Processamento selec = null;
        int j;
        for (j = 0; j < escravos.size(); j++) {

            if (escravos.get(j).getInformacaoDinamicaFila().isEmpty() && escravos.get(j).getInformacaoDinamicaProcessador().isEmpty() && controleEscravos.get(j).Livre()) {//Garantir que o escravo está de fato livre e que não há nenhuma tarefa em trânsito para o escravo
                selec = escravos.get(j);
                break;
            }

        }
        if (selec != null) {

            controleEscravos.get(j).SetBloqueado();//Inidcar que uma tarefa será enviada e que , portanto , este escravo deve ser bloqueada até a próxima atualização
            return selec;

        }

        //Buscando recurso para sofrer preempção
        int indexMaqMin = -1;
        int indexUMin = -1;
        int fmin = -1;
        int i;

        for (i = 0; i < metricaUsuarios.getUsuarios().size(); i++) {
            if (status.get(i).GetNumUso() > status.get(i).GetNumCota()) {
                if (indexUMin == -1) {

                    indexUMin = i;
                    fmin = status.get(i).GetNumUso() - status.get(i).GetNumCota();

                } else {

                    if (status.get(i).GetNumUso() - status.get(i).GetNumCota() > fmin) {
                        indexUMin = i;
                        fmin = status.get(i).GetNumUso() - status.get(i).GetNumCota();
                    }

                }
            }
        }

        if (indexUMin != -1) {


            Double tempoExec = null;

            for (i = 0; i < escravos.size(); i++) {

                if (escravos.get(i).getInformacaoDinamicaProcessador().size() == 1 && controleEscravos.get(i).Ocupado() && escravos.get(i).getInformacaoDinamicaFila().isEmpty() ) {

                    Tarefa tar = (Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0);
                    if (tar.getProprietario().equals(metricaUsuarios.getUsuarios().get(indexUMin)) && !tar.getProprietario().equals(tarefaSelec.getProprietario())) {

                        if (indexMaqMin == -1) {

                            indexMaqMin = i;
                            tempoExec = mestre.getSimulacao().getTime() - tar.getTempoInicial().get(tar.getTempoInicial().size() -1);

                        } else if (tempoExec > mestre.getSimulacao().getTime() - tar.getTempoInicial().get(tar.getTempoInicial().size() -1)) {

                            indexMaqMin = i;
                            tempoExec = mestre.getSimulacao().getTime() - tar.getTempoInicial().get(tar.getTempoInicial().size() -1);

                        }
                    }
                }
            }
        }
        if (indexMaqMin != -1) {
            mestre.enviarMensagem((Tarefa) escravos.get(indexMaqMin).getInformacaoDinamicaProcessador().get(0), escravos.get(indexMaqMin), Mensagens.DEVOLVER_COM_PREEMPCAO);
            controleEscravos.get(indexMaqMin).setPreemp();
            return escravos.get(indexMaqMin);

        } else {
            numEscravosPreemp--;
            return null;

        }

    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = escravos.indexOf(destino);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoEscravo.get(index));
    }

    @Override
    public void resultadoAtualizar(Mensagem mensagem) {
        contadorEscravos++;

        if (contadorEscravos == escravos.size()) {

            numEscravosPreemp = 0;
            numEscravosLivres = 0;

            for (int i = 0; i < escravos.size(); i++) {
                if (escravos.get(i).getInformacaoDinamicaProcessador().size() == 1 && (controleEscravos.get(i).Bloqueado() || controleEscravos.get(i).Ocupado()) && escravos.get(i).getInformacaoDinamicaFila().isEmpty()) {
                    controleEscravos.get(i).SetOcupado();
                    /*if (status.get(metricaUsuarios.getUsuarios().indexOf(((Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0)).getProprietario())).GetNumUso() > status.get(metricaUsuarios.getUsuarios().indexOf(((Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0)).getProprietario())).GetNumCota()) {
                     numEscravosPreemp++;
                     }*/
                } else if (escravos.get(i).getInformacaoDinamicaProcessador().isEmpty() && controleEscravos.get(i).Ocupado() && escravos.get(i).getInformacaoDinamicaFila().isEmpty()) {
                    controleEscravos.get(i).SetLivre();
                    numEscravosLivres++;
                }
            }
            contadorEscravos = 0;

            for (int j = 0; j < metricaUsuarios.getUsuarios().size(); j++) {
                if (status.get(j).GetNumUso() > status.get(j).GetNumCota()) {
                    numEscravosPreemp += status.get(j).GetNumUso() - status.get(j).GetNumCota();
                }
            }

            if ((numEscravosLivres > 0 || numEscravosPreemp > 0) && tarefas.size() > 0) {
                mestre.executarEscalonamento();
            }
        }
    }

    @Override
    public void escalonar() {

        Tarefa trf = escalonarTarefa();
        tarefaSelec = trf;

        if (trf != null) {

            CS_Processamento rec = escalonarRecurso();

            if (rec != null) {
                trf = tarefas.remove(tarefas.indexOf(trf));
                trf.setLocalProcessamento(rec);
                trf.setCaminho(escalonarRota(rec));

                //Verifica se não é caso de preempção
                if (controleEscravos.get(escravos.indexOf(rec)).Preemp() == false) {

                    numEscravosLivres--;
                    status.get(metricaUsuarios.getUsuarios().indexOf(trf.getProprietario())).AtualizaUso(1);
                    status.get(metricaUsuarios.getUsuarios().indexOf(trf.getProprietario())).AtualizaDemanda(0);
                    controleEscravos.get(escravos.indexOf(rec)).SetBloqueado();

                } else {

                    numEscravosPreemp--;
                    esperaTarefas.add(trf);
                    controlePreempcao.add(new ControlePreempcao(((Tarefa) rec.getInformacaoDinamicaProcessador().get(0)).getProprietario(), ((Tarefa) rec.getInformacaoDinamicaProcessador().get(0)).getIdentificador(), trf.getProprietario(), trf.getIdentificador()));
                    int indexUser = metricaUsuarios.getUsuarios().indexOf(((Tarefa) rec.getInformacaoDinamicaProcessador().get(0)).getProprietario());
                    status.get(indexUser).AtualizaUso(0);
                    indexUser = metricaUsuarios.getUsuarios().indexOf(trf.getProprietario());
                    status.get(indexUser).AtualizaDemanda(0);
                    controleEscravos.get(escravos.indexOf(rec)).SetBloqueado();

                }

                mestre.enviarTarefa(trf);
                for (int i = 0; i < escravos.size(); i++) {
                    if (escravos.get(i).getInformacaoDinamicaProcessador().size() > 1) {
                        System.out.printf("Escravo %s executando %d\n", escravos.get(i).getId(), escravos.get(i).getInformacaoDinamicaProcessador().size());
                        System.out.println("PROBLEMA1");
                    }
                    if (escravos.get(i).getInformacaoDinamicaFila().size() > 0) {
                        System.out.println("Tem Fila");
                    }
                }


            } else {
                tarefaSelec = null;
            }


            if ((numEscravosPreemp > 0 || numEscravosLivres > 0) && tarefas.size() > 0) {
                mestre.executarEscalonamento();
            }
        }
    }

    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);

        controleEscravos.get(escravos.indexOf(tarefa.getLocalProcessamento())).SetLivre();
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        maq.getInformacaoDinamicaProcessador().remove(tarefa);
        numEscravosLivres++;
        if (tarefas.size() > 0) {
            mestre.executarEscalonamento();
        }

        int indexUser = metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
        status.get(indexUser).AtualizaUso(0);

    }

    @Override
    public void adicionarTarefa(Tarefa tarefa) {
        super.adicionarTarefa(tarefa);
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        int indexUser;

        if (tarefa.getLocalProcessamento() != null) {
            int j;
            int indexControle = -1;
            for (j = 0; j < controlePreempcao.size(); j++) {
                if (controlePreempcao.get(j).getPreempID() == tarefa.getIdentificador() && controlePreempcao.get(j).getUsuarioPreemp().equals(tarefa.getProprietario())) {
                    indexControle = j;
                    break;
                }
            }

            for (int i = 0; i < esperaTarefas.size(); i++) {
                if (esperaTarefas.get(i).getProprietario().equals(controlePreempcao.get(indexControle).getUsuarioAlloc()) && esperaTarefas.get(i).getIdentificador() == controlePreempcao.get(j).getAllocID()) {
                    indexUser = metricaUsuarios.getUsuarios().indexOf(controlePreempcao.get(indexControle).getUsuarioAlloc());
                    status.get(indexUser).AtualizaUso(1);
                    indexUser = metricaUsuarios.getUsuarios().indexOf(controlePreempcao.get(indexControle).getUsuarioPreemp());
                    status.get(indexUser).AtualizaDemanda(1);
                    esperaTarefas.remove(i);
                    controlePreempcao.remove(j);
                    break;
                }
            }
        } else {
            int indexProp = metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
            status.get(indexProp).AtualizaDemanda(1);
            mestre.executarEscalonamento();
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
        private int demanda;
        private int numCota;
        private int numUso;

        public StatusUser(String usuario, Double poder) {
            this.usuario = usuario;
            this.PoderEmUso = 0.0;
            this.Cota = poder;
            this.numCota = 0;
            this.numUso = 0;
            this.demanda = 0;

            for (int i = 0; i < escravos.size(); i++) {
                if (escravos.get(i).getProprietario().equals(this.usuario)) {
                    numCota++;
                }
            }


        }

        public void AtualizaUso(int opc) {
            if (opc == 1) {
                this.numUso++;
            } else {
                this.numUso--;
            }
        }

        public void AtualizaDemanda(int opc) {
            if (opc == 1) {
                this.demanda++;
            } else if (opc == 0) {
                this.demanda--;
            }
        }

        public Double GetCota() {
            return this.Cota;
        }

        public Double GetUso() {
            return this.PoderEmUso;
        }

        public int GetNumCota() {
            return this.numCota;
        }

        public int GetNumUso() {
            return this.numUso;
        }

        public int GetDemanda() {
            return this.demanda;
        }
    }

    private class ControleEscravos {

        private int contador;

        public ControleEscravos() {
            this.contador = 0;
        }

        public boolean Ocupado() {
            if (this.contador == 1) {
                return true;
            } else {
                return false;
            }
        }

        public boolean Livre() {
            if (this.contador == 0) {
                return true;
            } else {
                return false;
            }
        }

        public boolean Bloqueado() {
            if (this.contador == 2) {
                return true;
            } else {
                return false;
            }
        }

        public boolean Preemp() {
            if (this.contador == 3) {
                return true;
            } else {
                return false;
            }
        }

        public void SetOcupado() {
            this.contador = 1;
        }

        public void SetLivre() {
            this.contador = 0;
        }

        public void SetBloqueado() {
            this.contador = 2;
        }

        public void setPreemp() {
            this.contador = 3;
        }
    }

    public class ControlePreempcao {

        private String usuarioPreemp;
        private String usuarioAlloc;
        private int preempID;
        private int allocID;

        public ControlePreempcao(String user1, int pID, String user2, int aID) {
            this.usuarioPreemp = user1;
            this.preempID = pID;
            this.usuarioAlloc = user2;
            this.allocID = aID;
        }

        public String getUsuarioPreemp() {
            return this.usuarioPreemp;
        }

        public int getPreempID() {
            return this.preempID;
        }

        public String getUsuarioAlloc() {
            return this.usuarioAlloc;
        }

        public int getAllocID() {
            return this.allocID;
        }
    }
}