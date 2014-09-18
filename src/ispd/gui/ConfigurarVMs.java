/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui;

import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Diogo Tavares
 */
public class ConfigurarVMs extends javax.swing.JDialog {

    /**
     * Creates new form ConfigurarVMs
     */
    public ConfigurarVMs(java.awt.Frame parent, boolean modal, Object[] users, Object[] vmms) {
        super(parent, modal);
        this.usuarios = new Vector<String>();
        for (Object object : users)
            usuarios.add((String) object);
        this.VMMs = new Vector<String>();
        for (Object object : vmms)
            VMMs.add((String) object);
        this.tabelaLinha = new Vector<Vector>(); //vetor de vetores com os valores dos atributos das máquinas virtuais
        //a tabela coluna possui os nomes dos atributos exibidos nas colunas
        this.tabelaColuna.add("VM Label");
        this.tabelaColuna.add("User");
        this.tabelaColuna.add("VMM");
        this.tabelaColuna.add("Proc alloc");
        this.tabelaColuna.add("Mem alloc");
        this.tabelaColuna.add("Disk alloc");
        this.tabelaColuna.add("OS");
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        VMconfigPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jUserComboBox = new javax.swing.JComboBox();
        jVMMComboBox = new javax.swing.JComboBox();
        jLabeluser = new javax.swing.JLabel();
        jLabelVMM = new javax.swing.JLabel();
        jSpinnerProc = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jSpinnerMem = new javax.swing.JSpinner();
        jSpinnerDisc = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jSOComboBox = new javax.swing.JComboBox();
        jButtonAddVM = new javax.swing.JButton();
        jButtonRemoveVM = new javax.swing.JButton();
        jScrollPaneTabela = new javax.swing.JScrollPane();
        jTableVMs = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Virtual machines configuration:");

        jUserComboBox.setModel(new DefaultComboBoxModel(usuarios)
        );
        jUserComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jUserComboBoxActionPerformed(evt);
            }
        });

        jVMMComboBox.setModel(new DefaultComboBoxModel(VMMs)
        );

        jLabeluser.setText("User:");

        jLabelVMM.setText("VMM:");

        jLabel2.setText("Processing Allocated (GFLops)");

        jLabel3.setText("Memory Allocated (GB):");

        jLabel4.setText("Disk Allocated (GB):");

        jLabel5.setText("Operational System:");

        jSOComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Linux", "Macintosh", "Windows" }));
        jSOComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSOComboBoxActionPerformed(evt);
            }
        });

        jButtonAddVM.setText("Add VM");
        jButtonAddVM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddVMActionPerformed(evt);
            }
        });

        jButtonRemoveVM.setText("Remove VM");

        jTableVMs.setModel(new DefaultTableModel(this.tabelaLinha,this.tabelaColuna));
        jScrollPaneTabela.setViewportView(jTableVMs);

        jButton1.setText("Add User");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout VMconfigPanelLayout = new javax.swing.GroupLayout(VMconfigPanel);
        VMconfigPanel.setLayout(VMconfigPanelLayout);
        VMconfigPanelLayout.setHorizontalGroup(
            VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VMconfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneTabela)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, VMconfigPanelLayout.createSequentialGroup()
                        .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(VMconfigPanelLayout.createSequentialGroup()
                                .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(VMconfigPanelLayout.createSequentialGroup()
                                        .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jUserComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabeluser))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabelVMM)
                                            .addComponent(jVMMComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(36, 36, 36)
                                .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jSpinnerProc)
                                        .addComponent(jSpinnerDisc)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3)
                            .addComponent(jSpinnerMem)
                            .addComponent(jLabel5)
                            .addComponent(jSOComboBox, 0, 142, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, VMconfigPanelLayout.createSequentialGroup()
                        .addComponent(jButtonAddVM, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonRemoveVM, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        VMconfigPanelLayout.setVerticalGroup(
            VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VMconfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabeluser)
                    .addComponent(jLabelVMM))
                .addGap(7, 7, 7)
                .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerProc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinnerMem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jUserComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jVMMComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerDisc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSOComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(18, 18, 18)
                .addGroup(VMconfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddVM)
                    .addComponent(jButtonRemoveVM))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneTabela, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(VMconfigPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(VMconfigPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jUserComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jUserComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jUserComboBoxActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        String newUser = JOptionPane.showInputDialog(this,"Enter the name","Add user", JOptionPane.QUESTION_MESSAGE);
        if (!usuarios.contains(newUser) && !newUser.equals("")) {
            usuarios.add(newUser);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jSOComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSOComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jSOComboBoxActionPerformed

    private void jButtonAddVMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddVMActionPerformed
        // TODO add your handling code here:
        Vector linha = new Vector(7);
        linha.add("VM"+tabelaIndex);
        tabelaIndex++;
        linha.add(jUserComboBox.getSelectedItem());
        linha.add(jVMMComboBox.getSelectedItem());
        linha.add(jSpinnerProc.getValue());
        linha.add(jSpinnerMem.getValue());
        linha.add(jSpinnerDisc.getValue());
        linha.add(jSOComboBox.getSelectedItem());
        tabelaLinha.add(linha);
        jScrollPaneTabela.setViewportView(jTableVMs);
    }//GEN-LAST:event_jButtonAddVMActionPerformed

    /**
     * @param args the command line arguments
     */
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel VMconfigPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonAddVM;
    private javax.swing.JButton jButtonRemoveVM;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelVMM;
    private javax.swing.JLabel jLabeluser;
    private javax.swing.JComboBox jSOComboBox;
    private javax.swing.JScrollPane jScrollPaneTabela;
    private javax.swing.JSpinner jSpinnerDisc;
    private javax.swing.JSpinner jSpinnerMem;
    private javax.swing.JSpinner jSpinnerProc;
    private javax.swing.JTable jTableVMs;
    private javax.swing.JComboBox jUserComboBox;
    private javax.swing.JComboBox jVMMComboBox;
    // End of variables declaration//GEN-END:variables
    private Vector<String> usuarios;
    private Vector<String> VMMs;
    private Vector<Vector> tabelaLinha;
    private Vector<String> tabelaColuna = new Vector<String>(7);
    private int tabelaIndex = 0;
}
