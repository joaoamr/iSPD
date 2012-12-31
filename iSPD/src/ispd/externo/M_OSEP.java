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
    List<ControleEscravos> contadores_escravos;
    int aux;
    List<Tarefa> espera;
    

    public M_OSEP() {
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new ArrayList<CS_Processamento>();
        this.contadores_escravos = new ArrayList<ControleEscravos>();
        this.espera = new ArrayList<Tarefa>();
    }

    @Override
    public void iniciar() {
        this.mestre.setTipoEscalonamento(Mestre.AMBOS);//Escalonamento quando chegam tarefas e quando tarefas são concluídas
        status = new ArrayList<StatusUser>();
        for (int i = 0; i < metricaUsuarios.getUsuarios().size(); i++) {//Objetos de controle de uso e cota para cada um dos usuários
            status.add(new StatusUser(metricaUsuarios.getUsuarios().get(i), metricaUsuarios.getPoderComputacional(metricaUsuarios.getUsuarios().get(i))));
        }
        for(int i = 0; i < escravos.size(); i++){//Contadores para lidar com a dinamicidade dos dados
            contadores_escravos.add(new ControleEscravos());
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
        aux = 0;
        //Buscando recurso livre
        CS_Processamento selec = null;
        for (int i = 0; i < escravos.size(); i++) {
            if (escravos.get(i).getInformacaoDinamicaFila().isEmpty() && escravos.get(i).getInformacaoDinamicaProcessador().isEmpty() && contadores_escravos.get(i).GetContador() == 0){//Garantir que o escravo está de fato livre e que não há nenhuma tarefa em trânsito para o escravo
                if (selec == null) {
                    selec = escravos.get(i);
                } else if (Math.abs(escravos.get(i).getPoderComputacional() - tarefaSelec.getTamProcessamento()) < Math.abs(selec.getPoderComputacional() - tarefaSelec.getTamProcessamento())) {//Best Fit
                    selec = escravos.get(i);
               }
            }
        }
        if (selec != null) {
           contadores_escravos.get(escravos.indexOf(selec)).SetOcupado();//Inidcar que uma tarefa será enviada e que , portanto , este escravo deve ser considerado ocupado
           return selec;
        }
        //Buscando recurso para sofrer preempção
        Double penalidade = null;
        for (int i = 0; i < escravos.size(); i++) {
            if(escravos.get(i).getInformacaoDinamicaProcessador().size() == 1 && contadores_escravos.get(i).GetContador() == 1 && escravos.get(i).getInformacaoDinamicaFila().isEmpty()){//Garante que está executando apenas uma tarefa está sendo executada e que não hpa tarefa eem trânsito para este escravo
                    Tarefa tar = (Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0);
                    int indexEscravo = metricaUsuarios.getUsuarios().indexOf(tar.getProprietario());
                    Double cota = status.get(indexEscravo).GetCota();//Cota do usuário dono da tarefa em execução
                    Double uso = status.get(indexEscravo).GetUso();//Uso do usuário da tarefa em execução
                    if (uso > cota) {//Se este usuário está extrapolando sua cota
                        if (penalidade == null) {
                            //Calcula a penalidade e seleciona esse escravo
                            penalidade = uso - escravos.get(i).getPoderComputacional() - cota;
                            selec = escravos.get(i);
                        } else {
                            //Verifica se o escravo atualmente slecionado tem penalidade menor que o escravo anterior
                            if (penalidade < (uso - escravos.get(i).getPoderComputacional() - cota)) {
                                penalidade = uso - escravos.get(i).getPoderComputacional() - cota;
                                selec = escravos.get(i);
                            }
                        }
                        aux++;//Incrementa o número de escravos que executam tarefas de usuários extrapoladores
                    }
            }
        }
        //Fazer a preempção
        if (selec != null) {
            //Verifica se vale apena fazer preempção
            Tarefa tar = (Tarefa) selec.getInformacaoDinamicaProcessador().get(0);
            int indexUserEscravo = metricaUsuarios.getUsuarios().indexOf(tar.getProprietario());
            int indexUserEspera = metricaUsuarios.getUsuarios().indexOf(tarefaSelec.getProprietario());
            //Penalidade do usuário dono da tarefa em execução, caso a preempção seja feita
            Double penalidaUserEscravo = status.get(indexUserEscravo).GetUso() - selec.getPoderComputacional() - status.get(indexUserEscravo).GetCota();
            //Penalidade do usuário dono da tarefa slecionada para ser posta em execução, caso a preempção seja feita
            Double penalidaUserEspera = status.get(indexUserEspera).GetUso() + selec.getPoderComputacional() - status.get(indexUserEspera).GetCota();
            //Caso o usuário em espera apresente menor penalidade e os donos das tarefas em execução e em espera não sejam a mesma pessoa , e , ainda, o escravo esteja executando apenas uma tarefa
            if (penalidaUserEscravo < penalidaUserEspera && !((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getProprietario().equals(tarefaSelec.getProprietario()) && selec.getInformacaoDinamicaFila().isEmpty() && selec.getInformacaoDinamicaProcessador().size() == 1) {
                System.out.println("Preempção: Tarefa " + ((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getIdentificador() + " do user " + ((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getProprietario() + " <=> " + tarefaSelec.getIdentificador() + " do user " + tarefaSelec.getProprietario());
                contadores_escravos.get(escravos.indexOf(selec)).SetPreemp();
                mestre.enviarMensagem((Tarefa) selec.getInformacaoDinamicaProcessador().get(0), selec, Mensagens.DEVOLVER_COM_PREEMPCAO);
                selec.getInformacaoDinamicaProcessador().remove(selec.getInformacaoDinamicaProcessador().get(0));
                return selec;
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
                //Verifica se não é caso de preempção
                if(contadores_escravos.get(escravos.indexOf(rec)).GetContador() != -1){
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
                            System.out.printf("Escravo %s executando %d\n", escravos.get(i).getId(), escravos.get(i).getInformacaoDinamicaProcessador().size());
                            System.out.println("PROBLEMA1");
                        }
                    }
                }
                //Em caso de preempção, a tarefa é colocada em uma lista de espera , para ser entregue ao escravo apenas quando este já houver devolvido sua tarefa
                else{
                    espera.add(trf);
                }
            }else {
                tarefas.add(trf);
                tarefaSelec = null;
            }
        }
        //Recursão. O escalonador é chamado para tentar todas as possibilidades de preempção
        if(tarefas.size() > 0 && contador > 0){
            contador--;
            aux = 0;
            mestre.executarEscalonamento();
        }
        else{
            contador = aux;
        }
    }

    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        contadores_escravos.get(escravos.indexOf(tarefa.getLocalProcessamento())).ResetContador();
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
        //Em caso de preempção, é procurada a tarefa correspondente para ser enviada ao escravo agora desocupado
        if (tarefa.getLocalProcessamento() != null) {
            for(int i = 0; i < espera.size(); i++){
                if(espera.get(i).getLocalProcessamento().equals(tarefa.getLocalProcessamento())){
                    mestre.enviarTarefa(espera.get(i));
                    espera.remove(i);
                    System.out.printf("Tarefa %d do usuário %s sofreu preempção\n", tarefa.getIdentificador(), tarefa.getProprietario());
                    indexUser = metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
                    status.get(indexUser).AtualizaUso(maq.getPoderComputacional(), 0);
                    i = espera.size();
               }
            }
            int i = 0;
            //Esperar que a tarefa chegue ao escravo para alterar seu estado de preempção para ocupado
            /*while(maq.getInformacaoDinamicaProcessador().isEmpty()){
                i++;
            }*/
            contadores_escravos.get(escravos.indexOf(maq)).SetOcupado();
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
    
    private class ControleEscravos{
        
        private int contador;
        
        public ControleEscravos(){
            this.contador = 0;
        }
        
        public int GetContador(){
            return this.contador;
        }
        
        public void SetOcupado(){
            this.contador = 1;
        }
        
        public void ResetContador(){
            this.contador = 0;
        }
        
        public void SetPreemp(){
            this.contador = -1;
        }
    }
}