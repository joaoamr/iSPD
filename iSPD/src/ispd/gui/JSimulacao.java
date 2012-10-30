package ispd.gui;

import InterpretadorInterno.ModeloIconico.InterpretadorIconico;
import InterpretadorInterno.ModeloSimulavel.InterpretadorSimulavel;
import ispd.motor.Simulacao;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Realiza faz chamada ao motor de simulação e apresenta os passos realizados
 * e porcentagem da simulação concluida
 * @author denison_usuario
 */
public class JSimulacao extends javax.swing.JDialog implements Runnable {

    /** Creates new form AguardaSimulacao */
    public JSimulacao(java.awt.Frame parent, boolean modal, AreaDesenho area, ResourceBundle palavras) {
        super(parent, modal);
        this.palavras = palavras;
        initComponents();
        this.aDesenho = area;
        this.tarefas = null;
        this.redeDeFilas = null;
        this.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (threadSim != null) {
                    //threadSim.stop();
                    threadSim = null;
                }
                dispose();
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar = new javax.swing.JProgressBar();
        jButtonCancelar = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        jTextPaneNotificacao = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(palavras.getString("Running Simulation")); // NOI18N

        jProgressBar.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);

        jButtonCancelar.setText(palavras.getString("Cancel")); // NOI18N
        jButtonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelarActionPerformed(evt);
            }
        });

        jTextPaneNotificacao.setEditable(false);
        jTextPaneNotificacao.setFont(new java.awt.Font("Arial", 1, 12));
        jScrollPane.setViewportView(jTextPaneNotificacao);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonCancelar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                    .addComponent(jScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                    .addComponent(jProgressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelarActionPerformed
        // TODO add your handling code here:
        if (this.threadSim != null) {
            //this.threadSim.stop();
            this.threadSim = null;
        }
        this.dispose();
    }//GEN-LAST:event_jButtonCancelarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancelar;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTextPane jTextPaneNotificacao;
    // End of variables declaration//GEN-END:variables
    private SimpleAttributeSet configuraCor = new SimpleAttributeSet();
    private Thread threadSim;
    private RedeDeFilas redeDeFilas;
    private List<Tarefa> tarefas;
    private AreaDesenho aDesenho;
    private ResourceBundle palavras;
    private double porcentagem = 0;

    public void setRedeDeFilas(RedeDeFilas redeDeFilas) {
        this.redeDeFilas = redeDeFilas;
    }

    public void setTarefas(List<Tarefa> tarefas) {
        this.tarefas = tarefas;
    }

    public List<Tarefa> getTarefas() {
        return tarefas;
    }
    
    public void iniciarSimulacao() {
        threadSim = new Thread(this);
        threadSim.start();
    }

    public void setMaxProgresso(int n) {
        jProgressBar.setMaximum(n);
    }

    public void setProgresso(int n) {
        this.porcentagem = n;
        jProgressBar.setValue(n);
    }

    public void incProgresso(int n) {
        this.porcentagem += n;
        int value = (int) porcentagem;
        jProgressBar.setValue(value);
    }
    
    public void incProgresso(double n) {
        this.porcentagem += n;
        int value = (int) porcentagem;
        jProgressBar.setValue(value);
    }

    public void println(String text, Color cor) {
        this.print(text, cor);
        this.print("\n", cor);
    }
    public void println(String text) {
        this.print(text, Color.black);
        this.print("\n", Color.black);
    }
    
    public void print(String text) {
        this.print(text, Color.black);
    }

    public void print(String text, Color cor) {
        Document doc = jTextPaneNotificacao.getDocument();
        try {
            if (cor != null) {
                StyleConstants.setForeground(configuraCor, cor);
            } else {
                StyleConstants.setForeground(configuraCor, Color.black);
            }
            if(palavras.containsKey(text)){
                doc.insertString(doc.getLength(), palavras.getString(text), configuraCor);
            }else{
                doc.insertString(doc.getLength(), text, configuraCor);
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(JSimulacao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {
        println("Simulation Initiated.");
        try{
            //0%
            //Verifica se foi construido modelo na area de desenho
            validarInicioSimulacao();//[5%] --> 5%
            //escreve modelo iconico
            this.print("Writing iconic model.");
            this.print(" -> ");
            File arquivo = new File("modeloiconico");
            try {
                FileWriter writer = new FileWriter(arquivo);
                PrintWriter saida = new PrintWriter(writer, true);
                saida.print(aDesenho.toString());
                saida.close();
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(JSimulacao.class.getName()).log(Level.SEVERE, null, ex);
            }
            incProgresso(5);//[5%] --> 10%
            this.println("OK", Color.green);
            //interpreta modelo iconico
            this.print("Interpreting iconic model.");
            this.print(" -> ");
            InterpretadorIconico parser = new InterpretadorIconico();
            parser.leArquivo(arquivo);
            incProgresso(5);//[5%] --> 15%
            this.println("OK", Color.green);
            this.print("Writing simulation model.");
            this.print(" -> ");
            parser.escreveArquivo();
            incProgresso(5);//[5%] --> 20%
            this.println("OK", Color.green);
            this.print("Interpreting simulation model.");
            this.print(" -> ");
            InterpretadorSimulavel parser2 = new InterpretadorSimulavel();
            parser2.leArquivo(new File("modelosimulavel"));
            incProgresso(5);//[5%] --> 25%
            this.println("OK", Color.green);
            //criar grade
            this.print("Mounting network queue.");
            this.print(" -> ");
            this.redeDeFilas = aDesenho.getRedeDeFilas();
            incProgresso(10);//[10%] --> 35%
            this.println("OK", Color.green);
            //criar tarefas
            this.print("Creating tasks.");
            this.print(" -> ");
            Tarefa.setContador(0);
            this.tarefas = aDesenho.getCargasConfiguracao().toTarefaList(redeDeFilas);
            incProgresso(10);//[10%] --> 45%
            this.println("OK", Color.green);
            //Verifica recursos do modelo e define roteamento
            Simulacao sim = new Simulacao(this, redeDeFilas, tarefas);//[10%] --> 55 %
            //Realiza asimulação
            this.println("Simulating.");
            //recebe instante de tempo em milissegundos ao iniciar a simulação
            double t1 = System.currentTimeMillis();
            
            sim.simular();//[30%] --> 85%
            
            //Recebe instnte de tempo em milissegundos ao fim da execução da simulação
            double t2 = System.currentTimeMillis();
            //Calcula tempo de simulação em segundos
            double tempototal = (t2-t1)/1000;
            //Obter Resultados
            //[5%] --> 90%
            //Apresentar resultados
            this.print("Showing results.");
            this.print(" -> ");
            JResultados janelaResultados = new JResultados(null, redeDeFilas, tarefas);
            incProgresso(10);//[10%] --> 100%
            this.println("OK", Color.green);
            this.println("Simulation Execution Time = " + tempototal + "seconds"); 
            janelaResultados.setLocationRelativeTo(this);
            janelaResultados.setVisible(true);
        }catch(IllegalArgumentException erro){
            Logger.getLogger(JSimulacao.class.getName()).log(Level.SEVERE, null, erro);
            this.println(erro.getMessage(), Color.red);
            this.print("Simulation Aborted", Color.red);
            this.println("!", Color.red);
        }
    }
    
    private void validarInicioSimulacao(){
        this.print("Verifying configuration of the icons.");
        this.print(" -> ");
        if (aDesenho == null || aDesenho.getIcones().isEmpty()) {
            this.println("Error!", Color.red);
            throw new IllegalArgumentException("The model has no icons.");
        }
        for (Icone I : aDesenho.getIcones()) {
            if (I.getConfigurado() == false) {
                this.println("Error!", Color.red);
                throw new IllegalArgumentException("One or more parameters have not been configured.");
            }
        }
        this.incProgresso(4);
        this.println("OK", Color.green);
        this.print("Verifying configuration of the tasks.");
        this.print(" -> ");
        if (aDesenho.getCargasConfiguracao() == null) {
            this.println("Error!", Color.red);
            throw new IllegalArgumentException("One or more  workloads have not been configured.");
        }
        this.incProgresso(1);
        this.println("OK", Color.green);
    }
}
