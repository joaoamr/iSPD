/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo;

import ispd.escalonador.Escalonador;
import ispd.escalonador.Mestre;
import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
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
    int contadorEscravos = 0;
    int conta_esca = 0;
    

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
                         
           double difUsuarioMinimo = -1.0; 
           int indexUsuarioMinimo = -1;
           
           //Encontrar o usuário que está mais abaixo da sua propriedade 
           for(int i = 0; i < metricaUsuarios.getUsuarios().size(); i++){
               if(status.get(i).GetUso() < status.get(i).GetCota() && status.get(i).GetUso() > 0.0){
                   if(difUsuarioMinimo == -1.0){
                       difUsuarioMinimo = status.get(i).GetCota() - status.get(i).GetUso();
                       indexUsuarioMinimo = i;
                   }
                   else{
                       if(difUsuarioMinimo > status.get(i).GetCota() - status.get(i).GetUso()){
                           difUsuarioMinimo = status.get(i).GetCota() - status.get(i).GetUso();
                           indexUsuarioMinimo = i;
                       }
                   }
               }
           }
           
           if(indexUsuarioMinimo != -1){
               int i = 0;
               while(i < tarefas.size()){
                   if(tarefas.get(i).getProprietario().equals(metricaUsuarios.getUsuarios().get(indexUsuarioMinimo))){
                       return tarefas.remove(i); 
                   }
                   i++;
               }
           }
           else{
                if(tarefas.size() > 0){
                    return tarefas.remove(0);
                }
                else{
                    return null;
                }
           }
        
        return null;
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
            if(escravos.get(i).getInformacaoDinamicaProcessador().size() == 1 && contadores_escravos.get(i).GetContador() == 1 && escravos.get(i).getInformacaoDinamicaFila().isEmpty()){//Garante que está executando apenas uma tarefa está sendo executada e que não há tarefa em trânsito para este escravo
                    Tarefa tar = (Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0);
                    int indexEscravo = metricaUsuarios.getUsuarios().indexOf(tar.getProprietario());
                    Double cota = status.get(indexEscravo).GetCota();//Cota do usuário dono da tarefa em execução
                    Double uso = status.get(indexEscravo).GetUso();//Uso do usuário da tarefa em execução
                    if (uso > cota) {//Se este usuário está extrapolando sua cota
                        System.out.println("Possível Preempção : " + escravos.get(i).getPoderComputacional()+" MFLOPS");
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
            if ((penalidaUserEscravo < penalidaUserEspera && !((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getProprietario().equals(tarefaSelec.getProprietario())) || (penalidaUserEscravo > 0 && penalidaUserEspera < 0 && !((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getProprietario().equals(tarefaSelec.getProprietario()))) {
                System.out.println("Preempção: Tarefa " + ((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getIdentificador() + " do user " + ((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getProprietario() + " <=> " + tarefaSelec.getIdentificador() + " do user " + tarefaSelec.getProprietario());
                contadores_escravos.get(escravos.indexOf(selec)).SetPreemp();
                mestre.enviarMensagem((Tarefa) selec.getInformacaoDinamicaProcessador().get(0), selec, Mensagens.DEVOLVER_COM_PREEMPCAO);
                //selec.getInformacaoDinamicaProcessador().remove(selec.getInformacaoDinamicaProcessador().get(0));
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
    public void resultadoAtualizar(Mensagem mensagem) {
        contadorEscravos++;
        boolean ocupados = true;
        if(contadorEscravos == escravos.size()){
            for(int i = 0;i < escravos.size(); i++){
                 if(escravos.get(i).getInformacaoDinamicaProcessador().isEmpty()){
                     ocupados = false;
                 }
            }
            if(tarefas.size() > 0 && ocupados){
                for(int i = 0; i < escravos.size(); i++){
                    if(escravos.get(i).getInformacaoDinamicaProcessador().size() == 1){
                       contadores_escravos.get(i).SetOcupado();
                    }
                    else if(escravos.get(i).getInformacaoDinamicaProcessador().isEmpty()){
                        contadores_escravos.get(i).ResetContador();
                    }
                }
                System.out.println("Tempo :"+ mestre.getSimulacao().getTime());
                for (int i = 0; i < status.size(); i++) {
                    System.out.printf("Usuário %s : %f de %f\n", status.get(i).usuario, status.get(i).GetUso(), status.get(i).GetCota());
                }
                
            }
            contadorEscravos = 0;
        }
    }
    
    @Override
    public void escalonar() {
        System.out.println("Escalonamento "+ conta_esca);
        conta_esca++;
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
                    //rec.getInformacaoDinamicaProcessador().add(trf);
                    status.get(metricaUsuarios.getUsuarios().indexOf(trf.getProprietario())).AtualizaUso(rec.getPoderComputacional(), 1);
                    System.out.println("Tarefa " + trf.getIdentificador() + " do user " + trf.getProprietario() + " foi escalonado" + mestre.getSimulacao().getTime());
                    System.out.printf("Escravo %s executando %d\n", rec.getId(), rec.getInformacaoDinamicaProcessador().size());
                    for(int i = 0;i < escravos.size(); i++){
                        if(escravos.get(i).getInformacaoDinamicaProcessador().size() > 1){
                            System.out.printf("Escravo %s executando %d\n", escravos.get(i).getId(), escravos.get(i).getInformacaoDinamicaProcessador().size());
                            System.out.println("PROBLEMA1");
                        }
                        if(escravos.get(i).getInformacaoDinamicaFila().size() > 0){
                                System.out.println("Tem Fila");
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
        System.out.println("Tempo :"+ mestre.getSimulacao().getTime());
        for (int i = 0; i < status.size(); i++) {
              System.out.printf("Usuário %s : %f de %f\n", status.get(i).usuario, status.get(i).GetUso(), status.get(i).GetCota());
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
                    contadores_escravos.get(escravos.indexOf(maq)).SetOcupado();
                    System.out.printf("Tarefa %d do usuário %s sofreu preempção\n", tarefa.getIdentificador(), tarefa.getProprietario());
                    indexUser = metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
                    status.get(indexUser).AtualizaUso(maq.getPoderComputacional(), 0);
                    indexUser = metricaUsuarios.getUsuarios().indexOf(espera.get(i).getProprietario());
                    status.get(indexUser).AtualizaUso(((CS_Processamento) tarefa.getLocalProcessamento()).getPoderComputacional(), 1);
                    espera.remove(i);
                    i = espera.size();
               }
            }
            System.out.println("Tempo :"+ mestre.getSimulacao().getTime());
            for (int i = 0; i < status.size(); i++) {
                  System.out.printf("Usuário %s : %f de %f\n", status.get(i).usuario, status.get(i).GetUso(), status.get(i).GetCota());
            }
        } else {
            System.out.println("Tarefa " + tarefa.getIdentificador() + " do user " + tarefa.getProprietario() + " chegou " + mestre.getSimulacao().getTime());
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