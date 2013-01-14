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
    List<ControleEscravos> contadores_escravos;
    List<Tarefa> espera;
    List<ControlePreempcao> controle_espera;
    
    int aux;
    int contador = 0;
    int contadorEscravos = 0;
    int conta_esca = 0;
    int numLivres = 0;
    int numPreemp = 0;

    public M_OSEP() {
        
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new ArrayList<CS_Processamento>();
        this.contadores_escravos = new ArrayList<ControleEscravos>();
        this.espera = new ArrayList<Tarefa>();
        this.controle_espera = new ArrayList<ControlePreempcao>();
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
        
        numLivres = escravos.size();
    }

    @Override
    public Tarefa escalonarTarefa() {
        if(numLivres > 0 || numPreemp > 0){
               double difUsuarioMinimo = -1.0; 

               int indexUsuarioMinimo = -1;

               //Encontrar o usuário que está mais abaixo da sua propriedade 
               for(int i = 0; i < metricaUsuarios.getUsuarios().size(); i++){
                   if((status.get(i).GetUso() < status.get(i).GetCota())){

                       if(difUsuarioMinimo == -1.0){
                           difUsuarioMinimo = status.get(i).GetCota() - status.get(i).GetUso();
                           indexUsuarioMinimo = i;
                       }
                       else{
                           if(difUsuarioMinimo < status.get(i).GetCota() - status.get(i).GetUso()){
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
        }
        return null;
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        
        aux = 0;
        
        //Buscando recurso livre
        CS_Processamento selec = null;
        if( numLivres > 0 ){
            for (int i = 0; i < escravos.size(); i++) {

                if (escravos.get(i).getInformacaoDinamicaFila().isEmpty() && escravos.get(i).getInformacaoDinamicaProcessador().isEmpty() && contadores_escravos.get(i).Livre()){//Garantir que o escravo está de fato livre e que não há nenhuma tarefa em trânsito para o escravo
                    if (selec == null) {

                        selec = escravos.get(i);

                    } else if (Math.abs(escravos.get(i).getPoderComputacional() - tarefaSelec.getTamProcessamento()) < Math.abs(selec.getPoderComputacional() - tarefaSelec.getTamProcessamento())) {//Best Fit

                        selec = escravos.get(i);

                   }

                }

            }
            if (selec != null) {

               contadores_escravos.get(escravos.indexOf(selec)).SetBloqueado();//Inidcar que uma tarefa será enviada e que , portanto , este escravo deve ser bloqueada até a próxima atualização

               return selec;

            }
        }
        else if( numPreemp > 0 ){
            //Buscando recurso para sofrer preempção
            Double penalidade = null;
            for (int i = 0; i < escravos.size(); i++) {

                if(escravos.get(i).getInformacaoDinamicaProcessador().size() == 1 && contadores_escravos.get(i).Ocupado() && escravos.get(i).getInformacaoDinamicaFila().isEmpty() && !escravos.get(i).getProprietario().equals(tarefaSelec.getProprietario())){//Garante que está executando apenas uma tarefa está sendo executada e que não há tarefa em trânsito para este escravo

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
                                if (penalidade < (uso - escravos.get(i).getPoderComputacional() - cota) || penalidade == 0) {

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

                    contadores_escravos.get(escravos.indexOf(selec)).setPreemp();
                    mestre.enviarMensagem((Tarefa) selec.getInformacaoDinamicaProcessador().get(0), selec, Mensagens.DEVOLVER_COM_PREEMPCAO);
                    return selec;
                 }
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
                 if(escravos.get(i).getInformacaoDinamicaProcessador().isEmpty() && contadores_escravos.get(i).Ocupado()){
                     ocupados = false;
                 }
            }
            if(tarefas.size() > 0 && ocupados){
            for(int i = 0; i < escravos.size(); i++){
                if(escravos.get(i).getInformacaoDinamicaProcessador().size() == 1 && contadores_escravos.get(i).Bloqueado()){
                   contadores_escravos.get(i).SetOcupado();
                }
                else if(escravos.get(i).getInformacaoDinamicaProcessador().isEmpty() && contadores_escravos.get(i).Ocupado()){
                    contadores_escravos.get(i).SetLivre();
                    numLivres++;
                }
                else{
                    if( contadores_escravos.get(i).Preemp()){
                        contadores_escravos.get(i).SetBloqueado(); 
                    }
                }
            }
            for(int i = 0; i < metricaUsuarios.getUsuarios().size() ; i++){
                if(status.get(i).GetUso() > status.get(i).GetCota()){
                    numPreemp++;
                }
            }
//                atualizado = true;
            System.out.println("Tempo :"+ mestre.getSimulacao().getTime());
            for (int i = 0; i < status.size(); i++) {
                System.out.printf("Usuário %s : %f de %f\n", status.get(i).usuario, status.get(i).GetUso(), status.get(i).GetCota());
            }

            mestre.executarEscalonamento();
                
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
                if(!contadores_escravos.get(escravos.indexOf(rec)).Preemp()){
                    numLivres--;
                    status.get(metricaUsuarios.getUsuarios().indexOf(trf.getProprietario())).AtualizaUso(rec.getPoderComputacional(), 1);
                }
                else{
                    numPreemp--;
                    espera.add(trf);
                    controle_espera.add(new ControlePreempcao(((Tarefa)rec.getInformacaoDinamicaProcessador().get(0)).getProprietario(),((Tarefa)rec.getInformacaoDinamicaProcessador().get(0)).getIdentificador(),trf.getProprietario(),trf.getIdentificador()));
                }
                mestre.enviarTarefa(trf);
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

                
            }else {
                tarefas.add(trf);
                tarefaSelec = null;
            }
        }
        System.out.println("Tempo :"+ mestre.getSimulacao().getTime());
        for (int i = 0; i < status.size(); i++) {
              System.out.printf("Usuário %s : %f de %f\n", status.get(i).usuario, status.get(i).GetUso(), status.get(i).GetCota());
        }
        if( numLivres == 0 && numPreemp == 0 ){
            for (int i = 0; i < escravos.size(); i++) {

                if(escravos.get(i).getInformacaoDinamicaProcessador().size() == 1 && contadores_escravos.get(i).Ocupado() && escravos.get(i).getInformacaoDinamicaFila().isEmpty() && !escravos.get(i).getProprietario().equals(tarefaSelec.getProprietario())){//Garante que está executando apenas uma tarefa está sendo executada e que não há tarefa em trânsito para este escravo

                        Tarefa tar = (Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0);

                        int indexEscravo = metricaUsuarios.getUsuarios().indexOf(tar.getProprietario());
                        Double cota = status.get(indexEscravo).GetCota();//Cota do usuário dono da tarefa em execução
                        Double uso = status.get(indexEscravo).GetUso();//Uso do usuário da tarefa em execução
                        
                        if( uso > cota ){
                            numPreemp++;
                        }
                }
            }
        }
        if( numPreemp > 0){
            mestre.executarEscalonamento();
        }
}

    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        
        contadores_escravos.get(escravos.indexOf(tarefa.getLocalProcessamento())).SetLivre();
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        maq.getInformacaoDinamicaProcessador().remove(tarefa);
        numLivres++;
        if(tarefas.size() > 0 && (numPreemp > 0 || numLivres > 0)){
            mestre.executarEscalonamento();
        }
                
        int indexUser = metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
        status.get(indexUser).AtualizaUso(maq.getPoderComputacional(), 0);
        
        System.out.println("Tarefa " + tarefa.getIdentificador() + " do user " + tarefa.getProprietario() + " concluida " + mestre.getSimulacao().getTime() + " O usuário perdeu " + maq.getPoderComputacional() + " MFLOPS");
    }

    @Override
    public void adicionarTarefa(Tarefa tarefa) {
        super.adicionarTarefa(tarefa);
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        int indexUser;
        //Em caso de preempção, é procurada a tarefa correspondente para ser enviada ao escravo agora desocupado
        if (tarefa.getLocalProcessamento() != null) {
            
                    //contadores_escravos.get(escravos.indexOf(maq)).SetOcupado();
                    System.out.printf("Tarefa %d do usuário %s sofreu preempção\n", tarefa.getIdentificador(), tarefa.getProprietario());
                    
                    int j;
                    int indexControle = -1;
                    for(j = 0; j < controle_espera.size(); j++){
                        if(controle_espera.get(j).getPreempID() == tarefa.getIdentificador() && controle_espera.get(j).getUsuarioPreemp().equals(tarefa.getProprietario())){
                            indexControle = j;
                            break;
                        }
                    }
                    
                    for(int i = 0; i < espera.size(); i++){
                        if(espera.get(i).getProprietario().equals(controle_espera.get(indexControle).getUsuarioAlloc()) && espera.get(i).getIdentificador() == controle_espera.get(j).getAllocID()){
                            indexUser = metricaUsuarios.getUsuarios().indexOf(tarefa.getProprietario());
                            status.get(indexUser).AtualizaUso(maq.getPoderComputacional(), 0);
                            indexUser = metricaUsuarios.getUsuarios().indexOf(espera.get(i).getProprietario());
                            status.get(indexUser).AtualizaUso(maq.getPoderComputacional(), 1);
                            espera.remove(i);
                            controle_espera.remove(j);
                            break;
                        }
                    }
                    
            System.out.println("Tempo :"+ mestre.getSimulacao().getTime());
            for (int i = 0; i < status.size(); i++) {
                  System.out.printf("Usuário %s : %f de %f\n", status.get(i).usuario, status.get(i).GetUso(), status.get(i).GetCota());
            }
         }
         else {
            System.out.println("Tarefa " + tarefa.getIdentificador() + " do user " + tarefa.getProprietario() + " chegou " + mestre.getSimulacao().getTime());
            if( numLivres > 0 && tarefas.size() > 0){
                mestre.executarEscalonamento();
            }
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
        
        private int numCota;
        private int numUso;

        public StatusUser(String usuario, Double poder) {
            this.usuario = usuario;
            this.PoderEmUso = 0.0;
            this.Cota = poder;
            this.numCota = 0;
            this.numUso = 0;
            
            for( int i = 0; i < escravos.size(); i++){
                if( escravos.get(i).getProprietario().equals(this.usuario) ){
                    numCota++;
                }
            }
            
            
        }

        public void AtualizaUso(Double poder, int opc) {
            if (opc == 1) {
                this.PoderEmUso = this.PoderEmUso + poder;
                this.numUso++;
            } else {
                this.PoderEmUso = this.PoderEmUso - poder;
                this.numUso--;
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
    }
    
    private class ControleEscravos{
        
        private int contador;
        
        public ControleEscravos(){
            this.contador = 0;
        }
        
        public boolean Ocupado(){
            if( this.contador == 1){
                return true;
            }
            else{
                return false;
            }
        }
        
        public boolean Livre(){
            if( this.contador == 0){
                return true;
            }
            else{
                return false;
            }
        }
        
        public boolean Bloqueado(){
            if( this.contador == 2){
                return true;
            }
            else{
                return false;
            }
        }
        
        public boolean Preemp(){
            if( this.contador == 3){
                return true;
            }
            else{
                return false;
            }
        }
                
        public void SetOcupado(){
            this.contador = 1;
        }
        
        public void SetLivre(){
            this.contador = 0;
        }
       
        public void SetBloqueado(){
            this.contador = 2;
        }
        
        public void setPreemp(){
            this.contador = 3;
        }
    }
    
    public class ControlePreempcao{
        private String usuarioPreemp;
        private String usuarioAlloc;
        private int preempID;
        private int allocID;
        
        public ControlePreempcao(String user1, int pID, String user2, int aID){
            this.usuarioPreemp = user1;
            this.preempID = pID;
            this.usuarioAlloc = user2;
            this.allocID = aID;
        }
        
        public String getUsuarioPreemp(){
            return this.usuarioPreemp;
        }
        
        public int getPreempID(){
            return this.preempID;
        }
        
        public String getUsuarioAlloc(){
            return this.usuarioAlloc;
        }
        
        public int getAllocID(){
            return this.allocID;
        }
    }
}