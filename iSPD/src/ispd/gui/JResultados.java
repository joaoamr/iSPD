/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JResultados.java
 *
 * Created on 20/09/2011, 11:01:42
 */
package ispd.gui;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.metricas.MetricasGlobais;
import ispd.motor.metricas.MetricasUsuarios;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import ispd.gui.ParesOrdenadosUso;
import java.util.LinkedList;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
/**
 *
 * @author denison_usuario
 */
public class JResultados extends javax.swing.JDialog {

    /** Creates new form JResultados */
    public JResultados(java.awt.Frame parent, RedeDeFilas rdf, List tarefas) {
        super(parent, true);
        initComponents();
        this.jTextAreaGlobal.setText(getResultadosGlobais(rdf.getMetricasGlobais()));
        this.jTextAreaTarefa.setText(getResultadosTarefas(tarefas));
        setResultadosUsuario(rdf.getMetricasUsuarios());
        graficoBarraProcessamento = new ChartPanel(criarGraficoProcessamento(rdf));
        graficoBarraProcessamento.setPreferredSize(new Dimension(600, 300));
        graficoBarraComunicacao = new ChartPanel(criarGraficoComunicacao(rdf));
        graficoBarraComunicacao.setPreferredSize(new Dimension(600, 300));
        graficoPizzaProcessamento = new ChartPanel(criarGraficoPizzaProcessamento(rdf));
        graficoPizzaProcessamento.setPreferredSize(new Dimension(600, 300));
        graficoPizzaComunicacao = new ChartPanel(criarGraficoPizzaComunicacao(rdf));
        graficoPizzaComunicacao.setPreferredSize(new Dimension(600, 300));
        graficoProcessamentoTempo = new ChartPanel(criarGraficoProcessamentoTempo(rdf));
        graficoProcessamentoTempo.setPreferredSize(new Dimension(600,300));
        tabelaRecurso = setTabelaRecurso(rdf);
        this.jScrollPaneProcessamento.setViewportView(this.graficoBarraProcessamento);
        this.jScrollPaneComunicacao.setViewportView(this.graficoBarraComunicacao);
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempo);
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPaneGobal = new javax.swing.JScrollPane();
        jTextAreaGlobal = new javax.swing.JTextArea();
        jScrollPaneTarefa = new javax.swing.JScrollPane();
        jTextAreaTarefa = new javax.swing.JTextArea();
        jScrollPaneUsuario = new javax.swing.JScrollPane();
        jTextAreaUsuario = new javax.swing.JTextArea();
        jScrollPaneRecurso = new javax.swing.JScrollPane();
        Vector<String> colunas = new Vector<String>(Arrays.asList("Label", "Owner", "Processing performed", "Communication performed"));
        jTableRecurso = new javax.swing.JTable();
        jPanelProcessamento = new javax.swing.JPanel();
        jToolBarProcessamento = new javax.swing.JToolBar();
        jButtonPBarra = new javax.swing.JButton();
        jButtonPPizza = new javax.swing.JButton();
        jScrollPaneProcessamento = new javax.swing.JScrollPane();
        jPanelComunicacao = new javax.swing.JPanel();
        jToolBarComunicacao = new javax.swing.JToolBar();
        jButtonCBarra = new javax.swing.JButton();
        jButtonCPizza = new javax.swing.JButton();
        jScrollPaneComunicacao = new javax.swing.JScrollPane();
        jPanelProcessamentoTempo = new javax.swing.JPanel();
        jScrollPaneProcessamentoTempo = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Simulation Results");

        jTextAreaGlobal.setColumns(20);
        jTextAreaGlobal.setEditable(false);
        jTextAreaGlobal.setFont(new java.awt.Font("Courier New", 1, 14)); // NOI18N
        jTextAreaGlobal.setRows(5);
        jScrollPaneGobal.setViewportView(jTextAreaGlobal);

        jTabbedPane1.addTab("Global", jScrollPaneGobal);

        jTextAreaTarefa.setColumns(20);
        jTextAreaTarefa.setEditable(false);
        jTextAreaTarefa.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        jTextAreaTarefa.setRows(5);
        jScrollPaneTarefa.setViewportView(jTextAreaTarefa);

        jTabbedPane1.addTab("Tasks", jScrollPaneTarefa);

        jTextAreaUsuario.setColumns(20);
        jTextAreaUsuario.setEditable(false);
        jTextAreaUsuario.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        jTextAreaUsuario.setRows(5);
        jScrollPaneUsuario.setViewportView(jTextAreaUsuario);

        jTabbedPane1.addTab("User", jScrollPaneUsuario);

        jTableRecurso.setModel(new javax.swing.table.DefaultTableModel(tabelaRecurso,colunas));
        jScrollPaneRecurso.setViewportView(jTableRecurso);

        jTabbedPane1.addTab("Resources", jScrollPaneRecurso);

        jToolBarProcessamento.setRollover(true);

        jButtonPBarra.setText("Bar Chart");
        jButtonPBarra.setFocusable(false);
        jButtonPBarra.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonPBarra.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonPBarra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPBarraActionPerformed(evt);
            }
        });
        jToolBarProcessamento.add(jButtonPBarra);

        jButtonPPizza.setText("Pie chart");
        jButtonPPizza.setFocusable(false);
        jButtonPPizza.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonPPizza.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonPPizza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPPizzaActionPerformed(evt);
            }
        });
        jToolBarProcessamento.add(jButtonPPizza);

        javax.swing.GroupLayout jPanelProcessamentoLayout = new javax.swing.GroupLayout(jPanelProcessamento);
        jPanelProcessamento.setLayout(jPanelProcessamentoLayout);
        jPanelProcessamentoLayout.setHorizontalGroup(
            jPanelProcessamentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarProcessamento, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
            .addComponent(jScrollPaneProcessamento, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
        );
        jPanelProcessamentoLayout.setVerticalGroup(
            jPanelProcessamentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProcessamentoLayout.createSequentialGroup()
                .addComponent(jToolBarProcessamento, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneProcessamento, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Chart of the processing", jPanelProcessamento);

        jToolBarComunicacao.setRollover(true);

        jButtonCBarra.setText("Bar Chart");
        jButtonCBarra.setFocusable(false);
        jButtonCBarra.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCBarra.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonCBarra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCBarraActionPerformed(evt);
            }
        });
        jToolBarComunicacao.add(jButtonCBarra);

        jButtonCPizza.setText("Pie chart");
        jButtonCPizza.setFocusable(false);
        jButtonCPizza.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCPizza.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonCPizza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCPizzaActionPerformed(evt);
            }
        });
        jToolBarComunicacao.add(jButtonCPizza);

        javax.swing.GroupLayout jPanelComunicacaoLayout = new javax.swing.GroupLayout(jPanelComunicacao);
        jPanelComunicacao.setLayout(jPanelComunicacaoLayout);
        jPanelComunicacaoLayout.setHorizontalGroup(
            jPanelComunicacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarComunicacao, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
            .addComponent(jScrollPaneComunicacao, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
        );
        jPanelComunicacaoLayout.setVerticalGroup(
            jPanelComunicacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelComunicacaoLayout.createSequentialGroup()
                .addComponent(jToolBarComunicacao, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneComunicacao, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Chart of the communication", jPanelComunicacao);

        javax.swing.GroupLayout jPanelProcessamentoTempoLayout = new javax.swing.GroupLayout(jPanelProcessamentoTempo);
        jPanelProcessamentoTempo.setLayout(jPanelProcessamentoTempoLayout);
        jPanelProcessamentoTempoLayout.setHorizontalGroup(
            jPanelProcessamentoTempoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneProcessamentoTempo, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
        );
        jPanelProcessamentoTempoLayout.setVerticalGroup(
            jPanelProcessamentoTempoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneProcessamentoTempo, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Use of computing power through time", jPanelProcessamentoTempo);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCPizzaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCPizzaActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneComunicacao.setViewportView(this.graficoPizzaComunicacao);
    }//GEN-LAST:event_jButtonCPizzaActionPerformed

    private void jButtonCBarraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCBarraActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneComunicacao.setViewportView(this.graficoBarraComunicacao);
    }//GEN-LAST:event_jButtonCBarraActionPerformed

    private void jButtonPPizzaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPPizzaActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneProcessamento.setViewportView(this.graficoPizzaProcessamento);
    }//GEN-LAST:event_jButtonPPizzaActionPerformed

    private void jButtonPBarraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPBarraActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneProcessamento.setViewportView(this.graficoBarraProcessamento);
    }//GEN-LAST:event_jButtonPBarraActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCBarra;
    private javax.swing.JButton jButtonCPizza;
    private javax.swing.JButton jButtonPBarra;
    private javax.swing.JButton jButtonPPizza;
    private javax.swing.JPanel jPanelComunicacao;
    private javax.swing.JPanel jPanelProcessamento;
    private javax.swing.JPanel jPanelProcessamentoTempo;
    private javax.swing.JScrollPane jScrollPaneComunicacao;
    private javax.swing.JScrollPane jScrollPaneGobal;
    private javax.swing.JScrollPane jScrollPaneProcessamento;
    private javax.swing.JScrollPane jScrollPaneProcessamentoTempo;
    private javax.swing.JScrollPane jScrollPaneRecurso;
    private javax.swing.JScrollPane jScrollPaneTarefa;
    private javax.swing.JScrollPane jScrollPaneUsuario;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableRecurso;
    private javax.swing.JTextArea jTextAreaGlobal;
    private javax.swing.JTextArea jTextAreaTarefa;
    private javax.swing.JTextArea jTextAreaUsuario;
    private javax.swing.JToolBar jToolBarComunicacao;
    private javax.swing.JToolBar jToolBarProcessamento;
    // End of variables declaration//GEN-END:variables
    private Vector<Vector> tabelaRecurso;
    private ChartPanel graficoBarraProcessamento;
    private ChartPanel graficoBarraComunicacao;
    private ChartPanel graficoPizzaProcessamento;
    private ChartPanel graficoPizzaComunicacao;
    private ChartPanel graficoProcessamentoTempo;

    private String getResultadosGlobais(MetricasGlobais globais) {
        String texto = "\t\tSimulation Results\n\n";
        texto += String.format("\tTotal Simulated Time = %g \n", globais.getTempoSimulacao());
        texto += String.format("\tSatisfaction = %g %%\n", globais.getSatisfacaoMedia());
        texto += String.format("\tIdleness of processing resources = %g %%\n", globais.getOciosidadeCompuacao());
        texto += String.format("\tIdleness of communication resources = %g %%\n", globais.getOciosidadeComunicacao());
        texto += String.format("\tEfficiency = %g %%\n", globais.getEficiencia());
        if (globais.getEficiencia() > 70.0) {
            texto += "\tEfficiency GOOD\n ";
        } else if (globais.getEficiencia() > 40.0) {
            texto += "\tEfficiency MEDIA\n ";
        } else {
            texto += "\tEfficiency BAD\n ";
        }
        return texto;
    }

    private String getResultadosTarefas(List<Tarefa> tarefas) {
        String texto = "\n\n\t\tTASKS\n ";
        double tempoMedioFilaComunicacao = 0;
        double tempoMedioComunicacao = 0;
        double tempoMedioSistemaComunicacao = 0;
        double tempoMedioFilaProcessamento = 0;
        double tempoMedioProcessamento = 0;
        double tempoMedioSistemaProcessamento = 0;
        
        
        int numTarefasCanceladas = 0;
        double MflopsDesperdicio = 0;
        int numTarefas = 0;
        
        

        for (Tarefa no : tarefas) {
            if (no.getEstado() == Tarefa.CONCLUIDO) {
                tempoMedioFilaComunicacao += no.getMetricas().getTempoEsperaComu();
                tempoMedioComunicacao += no.getMetricas().getTempoComunicacao();
                tempoMedioFilaProcessamento = no.getMetricas().getTempoEsperaProc();
                tempoMedioProcessamento = no.getMetricas().getTempoProcessamento();
                numTarefas++;
                
            } else if (no.getEstado() == Tarefa.CANCELADO) {
                MflopsDesperdicio += no.getTamProcessamento() * no.getPorcentagemProcessado();
                numTarefasCanceladas++;
            }
            
            CS_Processamento temp = (CS_Processamento) no.getLocalProcessamento();
            temp.setTempoProcessamento(no.getTempoInicial(), no.getTempoFinal());
                               
        }
              
        tempoMedioFilaComunicacao = tempoMedioFilaComunicacao / numTarefas;
        tempoMedioComunicacao = tempoMedioComunicacao / numTarefas;
        tempoMedioFilaProcessamento = tempoMedioFilaProcessamento / numTarefas;
        tempoMedioProcessamento = tempoMedioProcessamento / numTarefas;
        tempoMedioSistemaComunicacao = tempoMedioFilaComunicacao + tempoMedioComunicacao;
        tempoMedioSistemaProcessamento = tempoMedioFilaProcessamento + tempoMedioProcessamento;
        texto += "\n Communication \n";
        texto += String.format("    Queue average time: %g seconds.\n", tempoMedioFilaComunicacao);
        texto += String.format("    Communication average time: %g seconds.\n", tempoMedioComunicacao);
        texto += String.format("    System average time: %g seconds.\n", tempoMedioSistemaComunicacao);
        texto += "\n Processing \n";
        texto += String.format("    Queue average time: %g seconds.\n", tempoMedioFilaProcessamento);
        texto += String.format("    Processing average time: %g seconds.\n", tempoMedioProcessamento);
        texto += String.format("    System average time: %g seconds.\n", tempoMedioSistemaProcessamento);
        if(numTarefasCanceladas > 0){
            texto += "\n Tasks Canceled \n";
            texto += String.format("    Number: %d \n", numTarefasCanceladas);
            texto += String.format("    Wasted Processing: %g Mflops", MflopsDesperdicio);
        }
        return texto;
    }

    private Vector<Vector> setTabelaRecurso(RedeDeFilas rdf) {
        List<String> recurso = new ArrayList<String>();
        Vector<Vector> tabela = new Vector<Vector>();
        //linha [Nome] [Proprietario] [Processamento] [comunicacao]
        String nome;
        String prop;
        Double proc;
        Double comu;
        if (rdf.getInternets() != null) {
            for (CS_Comunicacao net : rdf.getInternets()) {
                nome = net.getId();
                prop = "---";
                proc = 0.0;
                comu = net.getMetrica().getSegundosDeTransmissao();
                tabela.add(new Vector(Arrays.asList(nome, prop, proc, comu)));
            }
        }
        if (rdf.getLinks() != null) {
            for (CS_Comunicacao link : rdf.getLinks()) {
                nome = link.getId();
                prop = "---";
                proc = 0.0;
                comu = link.getMetrica().getSegundosDeTransmissao();
                tabela.add(new Vector(Arrays.asList(nome, prop, proc, comu)));
                recurso.add(link.getId());
            }
        }
        if (rdf.getMestres() != null) {
            for (CS_Processamento mestre : rdf.getMestres()) {
                if (recurso.contains(mestre.getId())) {
                    int i = 0;
                    while (!tabela.get(i).get(0).equals(mestre.getId())) {
                        i++;
                    }
                    tabela.get(i).set(1, mestre.getProprietario());
                    tabela.get(i).set(2, mestre.getMetrica().getSegundosDeProcessamento());
                } else {
                    nome = mestre.getId();
                    prop = mestre.getProprietario();
                    proc = mestre.getMetrica().getSegundosDeProcessamento();
                    comu = 0.0;
                    tabela.add(new Vector(Arrays.asList(nome, prop, proc, comu)));
                    recurso.add(mestre.getId());
                }
            }
        }
        if (rdf.getMaquinas() != null) {
            for (CS_Processamento maq : rdf.getMaquinas()) {
                if (recurso.contains(maq.getId())) {
                    int i = 0;
                    while (!tabela.get(i).get(0).equals(maq.getId())) {
                        i++;
                    }
                    proc = maq.getMetrica().getSegundosDeProcessamento();
                    proc += Double.valueOf(tabela.get(i).get(2).toString());
                    tabela.get(i).set(2, proc);
                } else {
                    nome = maq.getId();
                    prop = maq.getProprietario();
                    proc = maq.getMetrica().getSegundosDeProcessamento();
                    comu = 0.0;
                    tabela.add(new Vector(Arrays.asList(nome, prop, proc, comu)));
                    recurso.add(maq.getId());
                }
            }
        }
        return tabela;
    }

    private JFreeChart criarGraficoProcessamento(RedeDeFilas rdf) {
        DefaultCategoryDataset dadosGrafico = new DefaultCategoryDataset();
        List<String> maqNomes = new ArrayList<String>();
        if (rdf.getMestres() != null) {
            for (CS_Processamento mst : rdf.getMestres()) {
                dadosGrafico.addValue(mst.getMetrica().getMFlopsProcessados(), "vermelho", mst.getId());
                maqNomes.add(mst.getId());
            }
        }
        if (rdf.getMaquinas() != null) {
            for (CS_Processamento maq : rdf.getMaquinas()) {
                if (maqNomes.contains(maq.getId())) {
                    Double valor = (Double) dadosGrafico.getValue("vermelho", maq.getId());
                    valor += maq.getMetrica().getMFlopsProcessados();
                    dadosGrafico.setValue(valor, "vermelho", maq.getId());
                } else {
                    dadosGrafico.addValue(maq.getMetrica().getMFlopsProcessados(), "vermelho", maq.getId());
                    maqNomes.add(maq.getId());
                }
            }
        }
        JFreeChart jfc = ChartFactory.createBarChart(
                "Total processed on each resource", //Titulo
                "Resource", // Eixo X
                "Mflops", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                false, false, false); // exibir: legendas, tooltips, url
        return jfc;
    }
    //Cria o gráfico que demonstra o uso de cada recurso do sistema através do tempo. 
    //Ele recebe como parâmetro a lista com as maquinas que processaram durante a simulação.
    private JFreeChart criarGraficoProcessamentoTempo(RedeDeFilas rdf) {
        XYSeriesCollection dadosGrafico = new XYSeriesCollection(); 
        
        
        //Se tiver alguma máquina na lista.
        if (rdf.getMaquinas() != null) {
            //Laço foreach que percorre as máquinas.
            for (CS_Processamento maq : rdf.getMaquinas()) {
                //Lista que recebe os pares de intervalo de tempo em que a máquina executou.
                LinkedList<ParesOrdenadosUso> lista = maq.getListaProcessamento();
           
                //Se a máquina tiver intervalos.
                if(!lista.isEmpty()){
                    //Cria o objeto do tipo XYSeries.
                    XYSeries tmp_series;
                    //Se o atributo numeroMaquina for 0, ou seja, não for um nó de um cluster.
                    if(maq.getnumeroMaquina() == 0)
                        //Estancia com o nome puro.
                        tmp_series = new XYSeries(maq.getId());
                    //Se for 1 ou mais, ou seja, é um nó de cluster.
                    else
                        //Estancia tmp_series com o nome concatenado com a palavra node e seu numero.
                        tmp_series = new XYSeries(maq.getId()+" node "+maq.getnumeroMaquina());
                    
                    int i;
                    //Laço que vai adicionando os pontos para a criação do gráfico.
                    for (i=0;i<lista.size(); i++){
                        //Calcula o uso, que é 100% - taxa de ocupação inicial.
                        Double uso = 100-(maq.getOcupacao());
                        //Adiciona ponto inicial.
                        tmp_series.add(lista.get(i).getInicio(),uso);
                        //Adiciona ponto final.
                        tmp_series.add(lista.get(i).getFim(),uso);
                        
                    
                    }
                    //Add no gráfico.
                    dadosGrafico.addSeries(tmp_series);
                }
                        
            }
            
        }
       
        JFreeChart jfc = ChartFactory.createXYAreaChart(
                "Use of computing power through time", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of use of computing power (%)", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        return jfc;
    }
    
    private JFreeChart criarGraficoComunicacao(RedeDeFilas rdf) {
        DefaultCategoryDataset dadosGrafico = new DefaultCategoryDataset();
        if (rdf.getLinks() != null) {
            for (CS_Comunicacao link : rdf.getLinks()) {
                dadosGrafico.addValue(link.getMetrica().getMbitsTransmitidos(), "vermelho", link.getId());
            }
        }
        if (rdf.getInternets() != null) {
            for (CS_Comunicacao net : rdf.getInternets()) {
                dadosGrafico.addValue(net.getMetrica().getMbitsTransmitidos(), "vermelho", net.getId());
            }
        }
        JFreeChart jfc = ChartFactory.createBarChart(
                "Total communication in each resource", //Titulo
                "Resource", // Eixo X
                "Mbits", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                false, false, false); // exibir: legendas, tooltips, url
        return jfc;
    }

    private JFreeChart criarGraficoPizzaProcessamento(RedeDeFilas rdf) {
        DefaultPieDataset dadosGrafico = new DefaultPieDataset();
        List<String> maqNomes = new ArrayList<String>();
        if (rdf.getMestres() != null) {
            for (CS_Processamento mst : rdf.getMestres()) {
                dadosGrafico.insertValue(0, mst.getId(), mst.getMetrica().getMFlopsProcessados());
                maqNomes.add(mst.getId());
            }
        }
        if (rdf.getMaquinas() != null) {
            for (CS_Processamento maq : rdf.getMaquinas()) {
                if (maqNomes.contains(maq.getId())) {
                    Double valor = (Double) dadosGrafico.getValue(maq.getId());
                    valor += maq.getMetrica().getMFlopsProcessados();
                    dadosGrafico.setValue(maq.getId(), valor);
                } else {
                    dadosGrafico.insertValue(0, maq.getId(), maq.getMetrica().getMFlopsProcessados());
                    maqNomes.add(maq.getId());
                }
            }
        }
        JFreeChart jfc = ChartFactory.createPieChart(
                "Total processed on each resource", //Titulo
                dadosGrafico, // Dados para o grafico
                true, false, false);
        return jfc;
    }

    private JFreeChart criarGraficoPizzaComunicacao(RedeDeFilas rdf) {
        DefaultPieDataset dadosGrafico = new DefaultPieDataset();
        if (rdf.getLinks() != null) {
            for (CS_Comunicacao link : rdf.getLinks()) {
                dadosGrafico.insertValue(0, link.getId(), link.getMetrica().getMbitsTransmitidos());
            }
        }
        if (rdf.getInternets() != null) {
            for (CS_Comunicacao net : rdf.getInternets()) {
                dadosGrafico.insertValue(0, net.getId(), net.getMetrica().getMbitsTransmitidos());
            }
        }
        JFreeChart jfc = ChartFactory.createPieChart(
                "Total communication in each resource", //Titulo
                dadosGrafico, // Dados para o grafico
                true, false, false);
        return jfc;
    }

    private void setResultadosUsuario(MetricasUsuarios metricasUsuarios) {
        if(metricasUsuarios.getUsuarios().size() > 1 ){
            String texto = "";
            for(int i = 0; i < metricasUsuarios.getUsuarios().size(); i++){
                String userName = metricasUsuarios.getUsuarios().get(i);
                texto += "\n\n\t\tUser " + userName + "\n";
                texto += "\nNumber of task: "+ metricasUsuarios.getTarefasConcluidas(userName).size() + "\n";
                //Applications:
                //Name: Number of task: Mflops:
                double tempoMedioFilaComunicacao = 0;
                double tempoMedioComunicacao = 0;
                double tempoMedioSistemaComunicacao = 0;
                double tempoMedioFilaProcessamento = 0;
                double tempoMedioProcessamento = 0;
                double tempoMedioSistemaProcessamento = 0;
                int numTarefasCanceladas = 0;
                int numTarefas = 0;
                for (Tarefa no : metricasUsuarios.getTarefasConcluidas(userName)) {
                    tempoMedioFilaComunicacao += no.getMetricas().getTempoEsperaComu();
                    tempoMedioComunicacao += no.getMetricas().getTempoComunicacao();
                    tempoMedioFilaProcessamento = no.getMetricas().getTempoEsperaProc();
                    tempoMedioProcessamento = no.getMetricas().getTempoProcessamento();
                    numTarefas++;
                }
                tempoMedioFilaComunicacao = tempoMedioFilaComunicacao / numTarefas;
                tempoMedioComunicacao = tempoMedioComunicacao / numTarefas;
                tempoMedioFilaProcessamento = tempoMedioFilaProcessamento / numTarefas;
                tempoMedioProcessamento = tempoMedioProcessamento / numTarefas;
                tempoMedioSistemaComunicacao = tempoMedioFilaComunicacao + tempoMedioComunicacao;
                tempoMedioSistemaProcessamento = tempoMedioFilaProcessamento + tempoMedioProcessamento;
                texto += "\n Communication \n";
                texto += String.format("    Queue average time: %g seconds.\n", tempoMedioFilaComunicacao);
                texto += String.format("    Communication average time: %g seconds.\n", tempoMedioComunicacao);
                texto += String.format("    System average time: %g seconds.\n", tempoMedioSistemaComunicacao);
                texto += "\n Processing \n";
                texto += String.format("    Queue average time: %g seconds.\n", tempoMedioFilaProcessamento);
                texto += String.format("    Processing average time: %g seconds.\n", tempoMedioProcessamento);
                texto += String.format("    System average time: %g seconds.\n", tempoMedioSistemaProcessamento);
            }
            jTextAreaUsuario.setText(texto);
        }else{
            jTabbedPane1.remove(jScrollPaneUsuario);
        }
    }
}
