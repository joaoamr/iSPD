/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui;

import java.awt.Frame;

/**
 *
 * @author Diogo Tavares
 */
public class EscolherClasse extends javax.swing.JDialog {
    
    private int escolha;
    public static final int GRID = 0;
    public static final int IAAS = 1;
    public static final int PAAS = 2;
    /**
     * Creates new form escolherClasse
     * @param owner
     * @param modal
     */
    public EscolherClasse(Frame owner, boolean modal) {
        super(owner, modal);
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

        jPanelEscolherNovo = new javax.swing.JPanel();
        jRadioGrid = new javax.swing.JRadioButton();
        jRadioIaaS = new javax.swing.JRadioButton();
        jRadioPaaS = new javax.swing.JRadioButton();
        jButtonEscNovoOK = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Escolher tipo de serviço");
        setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jRadioGrid.setText("Grid");
        jRadioGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioGridActionPerformed(evt);
            }
        });

        jRadioIaaS.setText("Cloud - IaaS");
        jRadioIaaS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioIaaSActionPerformed(evt);
            }
        });

        jRadioPaaS.setText("Cloud - PaaS");
        jRadioPaaS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioPaaSActionPerformed(evt);
            }
        });

        jButtonEscNovoOK.setText("OK!");
        jButtonEscNovoOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEscNovoOKActionPerformed(evt);
            }
        });

        jLabel1.setText("Choose the service that do you want to model");

        javax.swing.GroupLayout jPanelEscolherNovoLayout = new javax.swing.GroupLayout(jPanelEscolherNovo);
        jPanelEscolherNovo.setLayout(jPanelEscolherNovoLayout);
        jPanelEscolherNovoLayout.setHorizontalGroup(
            jPanelEscolherNovoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelEscolherNovoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonEscNovoOK)
                .addGap(33, 33, 33))
            .addGroup(jPanelEscolherNovoLayout.createSequentialGroup()
                .addGroup(jPanelEscolherNovoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelEscolherNovoLayout.createSequentialGroup()
                        .addGap(132, 132, 132)
                        .addGroup(jPanelEscolherNovoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioGrid)
                            .addComponent(jRadioIaaS)
                            .addComponent(jRadioPaaS)))
                    .addGroup(jPanelEscolherNovoLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel1)))
                .addContainerGap(114, Short.MAX_VALUE))
        );
        jPanelEscolherNovoLayout.setVerticalGroup(
            jPanelEscolherNovoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEscolherNovoLayout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jRadioGrid)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioIaaS)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioPaaS)
                .addGap(18, 18, 18)
                .addComponent(jButtonEscNovoOK)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanelEscolherNovo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanelEscolherNovo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public int getChooseReturn (){
        return escolha;
}
    
    private void jRadioGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioGridActionPerformed
        // TODO add your handling code here:
        if(jRadioGrid.isSelected()){
            jRadioGrid.setSelected(true);
            jRadioIaaS.setSelected(false);
            jRadioPaaS.setSelected(false);
        }
    }//GEN-LAST:event_jRadioGridActionPerformed

    private void jRadioIaaSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioIaaSActionPerformed
        // TODO add your handling code here:
        if(jRadioIaaS.isSelected()){
            jRadioGrid.setSelected(false);
            jRadioIaaS.setSelected(true);
            jRadioPaaS.setSelected(false);
        }
    }//GEN-LAST:event_jRadioIaaSActionPerformed

    private void jRadioPaaSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioPaaSActionPerformed
        // TODO add your handling code here:
        if(jRadioPaaS.isSelected()){
            jRadioGrid.setSelected(false);
            jRadioIaaS.setSelected(false);
            jRadioPaaS.setSelected(true);
        }
    }//GEN-LAST:event_jRadioPaaSActionPerformed

    private void jButtonEscNovoOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEscNovoOKActionPerformed
        // TODO add your handling code here:
        if(jRadioGrid.isSelected()){
            escolha=GRID;
           
//abrir interface Grid
        }
        else if(jRadioIaaS.isSelected()){
             escolha=IAAS;
            
//abrir interface IaaS
        }
        else if(jRadioPaaS.isSelected()){
             escolha=PAAS;
            
//abrir código PaaS
        }
        this.setVisible(false);
    }//GEN-LAST:event_jButtonEscNovoOKActionPerformed
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonEscNovoOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanelEscolherNovo;
    private javax.swing.JRadioButton jRadioGrid;
    private javax.swing.JRadioButton jRadioIaaS;
    private javax.swing.JRadioButton jRadioPaaS;
    // End of variables declaration//GEN-END:variables
}
