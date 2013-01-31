/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd;

import ispd.gui.AreaDesenho;
import ispd.motor.ProgressoSimulacao;
import ispd.motor.Simulacao;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Link;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author denison
 */
public class Terminal {

    private File arquivo;
    private int opcao;
    private int numExecucoes;
    private ProgressoSimulacao progrSim;

    public Terminal(String[] args) {
        if (args[0].equals("help") || args[0].equals("-help")) {
            opcao = 0;
        } else {
            int nomeA = 0;
            numExecucoes = 1;
            if (args[0].equals("-n")) {
                numExecucoes = Integer.getInteger(args[1]);
                nomeA = 2;
            }
            opcao = 1;
            String nomeArquivo = args[nomeA];
            for (int i = nomeA + 1; i < args.length; i++) {
                nomeArquivo = nomeArquivo + " " + args[i];
            }
            arquivo = new File(nomeArquivo);
            progrSim = new ProgressoSimulacao() {
                @Override
                public void incProgresso(int n) {
                }

                @Override
                public void print(String text, Color cor) {
                    System.out.print(text);
                }
            };
        }
    }

    void executar() {
        switch (opcao) {
            case 0:
                System.out.println("Usage: java -jar iSPD.jar");
                System.out.println("\t\t(to execute the graphical interface of the iSPD)");
                System.out.println("\tjava -jar iSPD.jar [-n number] [model file.imsx]");
                System.out.println("\t\t(to execute a model in terminal)");
                System.out.println("\t-n\tnumber of simulation");
                break;
            case 1:
                if (arquivo.getName().endsWith(".imsx") && arquivo.exists()) {
                    AreaDesenho aDesenho = new ispd.gui.AreaDesenho(0, 0);
                    try {
                        aDesenho.setDadosSalvos(ispd.arquivo.IconicoXML.ler(arquivo));
                    } catch (ParserConfigurationException ex) {
                        System.out.println(ex.getMessage());
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    } catch (SAXException ex) {
                        System.out.println(ex.getMessage());
                    }
                    //Verficar se todos os icones estão configurados
                    aDesenho.createImage();
                    this.simular(aDesenho);
                } else {
                    System.out.println("iSPD can not open the file: " + arquivo.getName());
                }
                break;
        }
    }

    private void simular(AreaDesenho aDesenho) {
        progrSim.println("Simulation Initiated.");
        try {
            //Verifica se foi construido modelo na area de desenho
            progrSim.validarInicioSimulacao(aDesenho);
            //Constrói e verifica modelos icônicos e simuláveis
            progrSim.AnalisarModelos(aDesenho.toString());
            //criar grade
            progrSim.print("Mounting network queue.");
            progrSim.print(" -> ");
            RedeDeFilas redeDeFilas = aDesenho.getRedeDeFilas();
            progrSim.println("OK", Color.green);
            //Escrever Modelo
            this.modelo(redeDeFilas);
            //criar tarefas
            for (int i = 1; i <= numExecucoes; i++) {
                progrSim.println("* Simulation 1");
                progrSim.print("  Creating tasks.");
                progrSim.print(" -> ");
                Tarefa.setContador(0);
                List<Tarefa> tarefas = aDesenho.getCargasConfiguracao().toTarefaList(redeDeFilas);
                progrSim.print("OK\n  ", Color.green);
                //Verifica recursos do modelo e define roteamento
                Simulacao sim = new Simulacao(progrSim, redeDeFilas, tarefas);//[10%] --> 55 %
                //Realiza asimulação
                progrSim.println("  Simulating.\n  ");
                //recebe instante de tempo em milissegundos ao iniciar a simulação
                double t1 = System.currentTimeMillis();
                sim.simular();//[30%] --> 85%
                //Recebe instnte de tempo em milissegundos ao fim da execução da simulação
                double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                double tempototal = (t2 - t1) / 1000;
                progrSim.println("  Simulation Execution Time = " + tempototal + "seconds");
            }
            progrSim.print("Results:");
            progrSim.print(" -> ");
            //JResultados janelaResultados = new JResultados(null, redeDeFilas, tarefas);
            progrSim.println("OK", Color.green);
            
            //janelaResultados.setLocationRelativeTo(this);
            //janelaResultados.setVisible(true);
        } catch (IllegalArgumentException erro) {
            progrSim.println(erro.getMessage(), Color.red);
            progrSim.print("Simulation Aborted", Color.red);
            progrSim.println("!", Color.red);
        }
    }

    private void modelo(RedeDeFilas redeDeFilas) {
        int cs_maq = 0, cs_link = 0, cs_mestre = 0;
        
        for (CS_Maquina maq : redeDeFilas.getMaquinas()) {
            if(maq instanceof CS_Maquina){
                cs_maq++;
            }
        }
        for (CS_Comunicacao link : redeDeFilas.getLinks()) {
            if(link instanceof CS_Link){
                cs_link++;
            }
        }
        for (CS_Processamento mestre : redeDeFilas.getMestres()) {
            if(mestre instanceof CS_Mestre){
                cs_mestre++;
            }
        }
        progrSim.println("* Grid:");
        progrSim.println("  - Number of Masters: "+cs_mestre);
        progrSim.println("  - Number of Slaves: "+cs_maq);
        progrSim.println("  - Number of Links: "+cs_link);
        //progrSim.println("  - Number of Tasks: "+task);
    }
}
