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
    List<ControleEscravos> controleEscravos;
    List<Tarefa> esperaTarefas;
    List<ControlePreempcao> controlePreempcao;
    
    
    int contadorEscravos = 0;
    int contadorEscalonamento = 0;
    int numEscravosLivres = 0;
    int numEscravosPreemp = 0;

    public M_OSEP() {
        
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new ArrayList<CS_Processamento>();
        this.controleEscravos = new ArrayList<ControleEscravos>();
        this.esperaTarefas = new ArrayList<Tarefa>();
        this.controlePreempcao = new ArrayList<ControlePreempcao>();
    }

    @Override
    public void iniciar() {
        this.mestre.setTipoEscalonamento(Mestre.AMBOS);//Escalonamento quando chegam tarefas e quando tarefas são concluídas
        status = new ArrayList<StatusUser>();
        
        for (int i = 0; i < metricaUsuarios.getUsuarios().size(); i++) {//Objetos de controle de uso e cota para cada um dos usuários
            status.add(new StatusUser(metricaUsuarios.getUsuarios().get(i), metricaUsuarios.getPoderComputacional(metricaUsuarios.getUsuarios().get(i))));
        }
        
        for(int i = 0; i < escravos.size(); i++){//Contadores para lidar com a dinamicidade dos dados
            controleEscravos.add(new ControleEscravos());
        }
        
        numEscravosLivres = escravos.size();
    }

    @Override
    public Tarefa escalonarTarefa() {
        if(numEscravosLivres > 0 || numEscravosPreemp > 0){//Faz a busca por tarefas apenas caso haja possibilidade de alocar recursos 
               //Usuários com maior diferença entre uso e posse terão preferência
               double difUsuarioMinimo = -1.0; 
               int indexUsuarioMinimo = -1;
               //Encontrar o usuário que está mais abaixo da sua propriedade 
               for(int i = 0; i < metricaUsuarios.getUsuarios().size(); i++){
                   //Verificar se existem tarefas do usuário corrente
                   boolean demanda = false;
                   
                   for( int j = 0; j < tarefas.size(); j++){
                       if( tarefas.get(j).getProprietario().equals(metricaUsuarios.getUsuarios().get(i))){
                           demanda = true;
                           break;
                       }
                   }
                   
                   //Caso existam tarefas do usuário corrente e ele esteja com uso menor que sua posse
                   if((status.get(i).GetUso() < status.get(i).GetCota()) && demanda){

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
                    //Caso não tenha encontrado tarefas do usuario com menor uso em relação à sua posse, retorna a primeira tarefa da lista de tarefas
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
       
        //Buscando recurso livre
        CS_Processamento selec = null;
        if( numEscravosLivres > 0 ){
            for (int i = 0; i < escravos.size(); i++) {

                if (escravos.get(i).getInformacaoDinamicaFila().isEmpty() && escravos.get(i).getInformacaoDinamicaProcessador().isEmpty() && controleEscravos.get(i).Livre()){//Garantir que o escravo está de fato livre e que não há nenhuma tarefa em trânsito para o escravo
                    if ( selec == null ) {

                        selec = escravos.get(i);

                    } else if ( escravos.get(i).getProprietario().equals(tarefaSelec.getProprietario()) && ( selec.getPoderComputacional() < escravos.get(i).getPoderComputacional() || !selec.getProprietario().equals(escravos.get(i).getProprietario()))) {//Best Fit

                        selec = escravos.get(i);

                   }

                }

            }
            if( selec == null){
                for (int i = 0; i < escravos.size(); i++) {

                    if (escravos.get(i).getInformacaoDinamicaFila().isEmpty() && escravos.get(i).getInformacaoDinamicaProcessador().isEmpty() && controleEscravos.get(i).Livre()){//Garantir que o escravo está de fato livre e que não há nenhuma tarefa em trânsito para o escravo
                        if (selec == null) {

                            selec = escravos.get(i);

                        } else if (Math.abs(escravos.get(i).getPoderComputacional() - tarefaSelec.getTamProcessamento()) < Math.abs(selec.getPoderComputacional() - tarefaSelec.getTamProcessamento())) {//Best Fit

                            selec = escravos.get(i);

                       }

                    }

                }
            }
            if (selec != null) {

               controleEscravos.get(escravos.indexOf(selec)).SetBloqueado();//Inidcar que uma tarefa será enviada e que , portanto , este escravo deve ser bloqueada até a próxima atualização

               return selec;

            }
        }
        else if( numEscravosPreemp > 0 ){
            //Buscando recurso para sofrer preempção
            Double penalidade = null;
            for (int i = 0; i < escravos.size(); i++) {

                if(escravos.get(i).getInformacaoDinamicaProcessador().size() == 1 && controleEscravos.get(i).Ocupado() && escravos.get(i).getInformacaoDinamicaFila().isEmpty()){//Garante que está executando apenas uma tarefa está sendo executada e que não há tarefa em trânsito para este escravo

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

                                //Verifica se o escravo atualmente slecionado tem penalidade menor que o escravo anterior. O cálculo é feito dessa maneira devido à forma com que a penalidade é calculada
                                if (penalidade < (uso - escravos.get(i).getPoderComputacional() - cota) || penalidade == 0) {

                                    penalidade = uso - escravos.get(i).getPoderComputacional() - cota;

                                    selec = escravos.get(i);

                                }
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

                //Penalidade do usuário dono da tarefa em execução, caso a preempção seja feita
                Double penalidaUserEscravoPosterior = status.get(indexUserEscravo).GetUso() - selec.getPoderComputacional() - status.get(indexUserEscravo).GetCota();

                //Penalidade do usuário dono da tarefa slecionada para ser posta em execução, caso a preempção seja feita
                Double penalidaUserEsperaPosterior = status.get(indexUserEspera).GetUso() + selec.getPoderComputacional() - status.get(indexUserEspera).GetCota();
                
                //Penalidade do usuário dono da tarefa em execução, caso a preempção seja feita
                Double penalidaUserEscravoAtual = status.get(indexUserEscravo).GetUso() - selec.getPoderComputacional() - status.get(indexUserEscravo).GetCota();

                //Penalidade do usuário dono da tarefa slecionada para ser posta em execução, caso a preempção seja feita
                Double penalidaUserEsperaAtual = status.get(indexUserEspera).GetUso() + selec.getPoderComputacional() - status.get(indexUserEspera).GetCota();

                //Caso o usuário em espera apresente menor penalidade e os donos das tarefas em execução e em espera não sejam a mesma pessoa , e , ainda, o escravo esteja executando apenas uma tarefa
                if ((penalidaUserEscravoPosterior < penalidaUserEsperaPosterior  && !((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getProprietario().equals(tarefaSelec.getProprietario())) || (penalidaUserEscravoPosterior > 0 && penalidaUserEsperaPosterior < 0 && !((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getProprietario().equals(tarefaSelec.getProprietario()))){

                    System.out.println("Preempção: Tarefa " + ((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getIdentificador() + " do user " + ((Tarefa) selec.getInformacaoDinamicaProcessador().get(0)).getProprietario() + " <=> " + tarefaSelec.getIdentificador() + " do user " + tarefaSelec.getProprietario());

                    controleEscravos.get(escravos.indexOf(selec)).setPreemp();
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
          if( contadorEscravos == escravos.size() ){          
            numEscravosPreemp = 0;
            numEscravosLivres = 0;
            for(int i = 0; i < escravos.size(); i++){
                if(escravos.get(i).getInformacaoDinamicaProcessador().size() == 1 && controleEscravos.get(i).Bloqueado()){
                   controleEscravos.get(i).SetOcupado();
                   if( status.get( metricaUsuarios.getUsuarios().indexOf(((Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0)).getProprietario())).GetUso() > status.get( metricaUsuarios.getUsuarios().indexOf(((Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0)).getProprietario())).GetCota()){
                       numEscravosPreemp++;
                   }
                }
                else if(escravos.get(i).getInformacaoDinamicaProcessador().isEmpty() && controleEscravos.get(i).Ocupado()){
                    controleEscravos.get(i).SetLivre();
                    numEscravosLivres++;
                }
                /*else{
                    if( controleEscravos.get(i).Preemp()){
                        controleEscravos.get(i).SetBloqueado(); 
                    }
                }*/
            }
            System.out.printf("Atualizou %d livres e %d preempções\n",numEscravosLivres,numEscravosPreemp);
            System.out.println("Tempo :"+ mestre.getSimulacao().getTime());
            for (int i = 0; i < status.size(); i++) {
                System.out.printf("Usuário %s : %f de %f\n", status.get(i).usuario, status.get(i).GetUso(), status.get(i).GetCota());
            }
            
            if( tarefas.size() > 0 ){
                mestre.executarEscalonamento();
            }    
            contadorEscravos = 0;
        }
    }
    
@Override
public void escalonar() {
        System.out.println("Escalonamento "+ contadorEscalonamento);
        contadorEscalonamento++;
        Tarefa trf = escalonarTarefa();
        tarefaSelec = trf;
        if (trf != null) {
            CS_Processamento rec = escalonarRecurso();
            if (rec != null) {
                trf.setLocalProcessamento(rec);
                trf.setCaminho(escalonarRota(rec));
                //Verifica se não é caso de preempção
                if(!controleEscravos.get(escravos.indexOf(rec)).Preemp()){
                    numEscravosLivres--;
                    status.get(metricaUsuarios.getUsuarios().indexOf(trf.getProprietario())).AtualizaUso(rec.getPoderComputacional(), 1);
                }
                else{
                    numEscravosPreemp--;
                    esperaTarefas.add(trf);
                    controlePreempcao.add(new ControlePreempcao(((Tarefa)rec.getInformacaoDinamicaProcessador().get(0)).getProprietario(),((Tarefa)rec.getInformacaoDinamicaProcessador().get(0)).getIdentificador(),trf.getProprietario(),trf.getIdentificador()));
                    int indexUser = metricaUsuarios.getUsuarios().indexOf(((Tarefa)rec.getInformacaoDinamicaProcessador().get(0)).getProprietario());
                    status.get(indexUser).AtualizaUso(rec.getPoderComputacional(), 0);
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
        /*if( numEscravosLivres == 0 && numEscravosPreemp == 0 ){
            for (int i = 0; i < escravos.size(); i++) {

                if(escravos.get(i).getInformacaoDinamicaProcessador().size() == 1 && controleEscravos.get(i).Ocupado() && escravos.get(i).getInformacaoDinamicaFila().isEmpty() && !escravos.get(i).getProprietario().equals(tarefaSelec.getProprietario())){//Garante que está executando apenas uma tarefa está sendo executada e que não há tarefa em trânsito para este escravo

                        Tarefa tar = (Tarefa) escravos.get(i).getInformacaoDinamicaProcessador().get(0);

                        int indexEscravo = metricaUsuarios.getUsuarios().indexOf(tar.getProprietario());
                        Double cota = status.get(indexEscravo).GetCota();//Cota do usuário dono da tarefa em execução
                        Double uso = status.get(indexEscravo).GetUso();//Uso do usuário da tarefa em execução
                        
                        if( uso > cota ){
                            numEscravosPreemp++;
                        }
                }
            }
        }*/
        if(numEscravosLivres > 0 && tarefas.size() > 0){
            mestre.executarEscalonamento();
        }
}

    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        
        controleEscravos.get(escravos.indexOf(tarefa.getLocalProcessamento())).SetLivre();
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        maq.getInformacaoDinamicaProcessador().remove(tarefa);
        numEscravosLivres++;
        if(tarefas.size() > 0 && (numEscravosPreemp > 0 || numEscravosLivres > 0)){
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
                    for(j = 0; j < controlePreempcao.size(); j++){
                        if(controlePreempcao.get(j).getPreempID() == tarefa.getIdentificador() && controlePreempcao.get(j).getUsuarioPreemp().equals(tarefa.getProprietario())){
                            indexControle = j;
                            break;
                        }
                    }
                    
                    for(int i = 0; i < esperaTarefas.size(); i++){
                        if(esperaTarefas.get(i).getProprietario().equals(controlePreempcao.get(indexControle).getUsuarioAlloc()) && esperaTarefas.get(i).getIdentificador() == controlePreempcao.get(j).getAllocID()){
                            indexUser = metricaUsuarios.getUsuarios().indexOf(controlePreempcao.get(indexControle).getUsuarioAlloc());
                            status.get(indexUser).AtualizaUso(maq.getPoderComputacional(), 1);
                            esperaTarefas.remove(i);
                            controlePreempcao.remove(j);
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
            /*if( numEscravosLivres > 0 && tarefas.size() > 0){
                mestre.executarEscalonamento();
            }*/
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