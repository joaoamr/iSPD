/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanelConfiguraMaquina.java
 *
 * Created on 03/03/2011, 13:27:54
 */
package ispd.gui.configuracao;

import ispd.gui.Icone;
import ispd.ValidaValores;
import ispd.arquivo.Escalonadores;
import ispd.escalonador.ManipularArquivos;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;

/**
 *
 * @author denison_usuario
 */
public class JPanelConfiguraMaquina extends javax.swing.JPanel {

    private Icone icone;
    private ResourceBundle palavras;
    private Vector<String> nomesDosEscalonadores;
    private ManipularArquivos escalonadores;

    /** Creates new form JPanelConfiguraMaquina */
    public JPanelConfiguraMaquina() {
        Locale locale = Locale.getDefault();
        palavras = ResourceBundle.getBundle("ispd.idioma.Idioma", locale);
        this.nomesDosEscalonadores = new Vector<String>(Arrays.asList(Escalonadores.ESCALONADORES));
        jComboBoxAlgoritmos = new JComboBox(nomesDosEscalonadores);
        initComponents();
        jTableComboBox.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(jComboBoxAlgoritmos));
        jTableComboBox1.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(jComboBoxUsuarios));
        jTableDouble.getColumnModel().getColumn(0).setPreferredWidth(100);
        jTableComboBox.getColumnModel().getColumn(1).setPreferredWidth(100);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBoxUsuarios = new javax.swing.JComboBox();
        jLabelTitle = new javax.swing.JLabel();
        jLabelInicial = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListEscravo = new javax.swing.JList();
        jTableString = new javax.swing.JTable();
        jTableDouble = new javax.swing.JTable();
        jLabel = new javax.swing.JLabel();
        jTableMestre = new javax.swing.JTable();
        jTableComboBox = new javax.swing.JTable();
        jTableComboBox1 = new javax.swing.JTable();

        jComboBoxUsuarios.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "user1" }));

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabelTitle.setText(palavras.getString("Machine icon configuration")); // NOI18N

        jLabelInicial.setText(palavras.getString("Configuration for the icon") + "#: " + "0");

        jListEscravo.setBorder(javax.swing.BorderFactory.createTitledBorder(palavras.getString("Slave Nodes:")));
        jListEscravo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListEscravoMouseClicked(evt);
            }
        });
        jListEscravo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jListEscravoKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jListEscravo);

        jTableString.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Label:", "nome"}
            },
            new String [] {
                "", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableString.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableString.setSelectionBackground(new java.awt.Color(255, 255, 255));
        jTableString.setSelectionForeground(new java.awt.Color(0, 0, 0));
        jTableString.getTableHeader().setResizingAllowed(false);
        jTableString.getTableHeader().setReorderingAllowed(false);
        jTableString.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTableStringPropertyChange(evt);
            }
        });

        jTableDouble.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Processing:", null},
                {"Load Factor:", null}
            },
            new String [] {
                "", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableDouble.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableDouble.setSelectionBackground(new java.awt.Color(255, 255, 255));
        jTableDouble.setSelectionForeground(new java.awt.Color(0, 0, 0));
        jTableDouble.getTableHeader().setResizingAllowed(false);
        jTableDouble.getTableHeader().setReorderingAllowed(false);
        jTableDouble.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTableDoublePropertyChange(evt);
            }
        });

        jLabel.setText("<html>\nMflop/s\n<br>\n%\n</html>");

        jTableMestre.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"MASTER", null}
            },
            new String [] {
                "", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableMestre.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableMestre.setSelectionBackground(new java.awt.Color(255, 255, 255));
        jTableMestre.setSelectionForeground(new java.awt.Color(0, 0, 0));
        jTableMestre.getTableHeader().setResizingAllowed(false);
        jTableMestre.getTableHeader().setReorderingAllowed(false);
        jTableMestre.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTableMestrePropertyChange(evt);
            }
        });

        jTableComboBox.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Scheduler:", null}
            },
            new String [] {
                "", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableComboBox.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableComboBox.setSelectionBackground(new java.awt.Color(255, 255, 255));
        jTableComboBox.setSelectionForeground(new java.awt.Color(0, 0, 0));
        jTableComboBox.getTableHeader().setResizingAllowed(false);
        jTableComboBox.getTableHeader().setReorderingAllowed(false);
        jTableComboBox.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTableComboBoxPropertyChange(evt);
            }
        });

        jTableComboBox1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Owner:", null}
            },
            new String [] {
                "", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableComboBox1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableComboBox1.setSelectionBackground(new java.awt.Color(255, 255, 255));
        jTableComboBox1.setSelectionForeground(new java.awt.Color(0, 0, 0));
        jTableComboBox1.getTableHeader().setResizingAllowed(false);
        jTableComboBox1.getTableHeader().setReorderingAllowed(false);
        jTableComboBox1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTableComboBox1PropertyChange(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabelTitle)
            .addComponent(jLabelInicial)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTableComboBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addComponent(jTableString, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addComponent(jTableDouble, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addComponent(jTableMestre, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTableComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabelTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelInicial)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTableString, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTableComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTableDouble, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTableMestre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTableComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTableStringPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTableStringPropertyChange
        // TODO add your handling code here:
        if (jTableString.getValueAt(0, 1) != null && !icone.getNome().equals(jTableString.getValueAt(0, 1).toString())) {
            if (ValidaValores.NomeIconeNaoExiste(jTableString.getValueAt(0, 1).toString()) && ValidaValores.validaNomeIcone(jTableString.getValueAt(0, 1).toString())) {
                ValidaValores.removeNomeIcone(icone.getNome());
                icone.setNome(jTableString.getValueAt(0, 1).toString());
                ValidaValores.addNomeIcone(jTableString.getValueAt(0, 1).toString());
            } else {
                jTableString.setValueAt(icone.getNome(), 0, 1);
            }
        } else {
            setIcone(icone);
        }
    }//GEN-LAST:event_jTableStringPropertyChange

    private void jTableDoublePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTableDoublePropertyChange
        // TODO add your handling code here:
            switch (jTableDouble.getSelectedRow()) {
                case 0:
                    if (jTableDouble.getValueAt(0, 1)!=null && ValidaValores.validaDouble(jTableDouble.getValueAt(0, 1).toString())) {
                        icone.setPoderComputacional((Double) jTableDouble.getValueAt(0, 1));
                    }
                    break;
                case 1:
                    if (jTableDouble.getValueAt(1, 1)!=null &&ValidaValores.validaDouble(jTableDouble.getValueAt(1, 1).toString())) {
                        icone.setTaxaOcupacao((Double) jTableDouble.getValueAt(1, 1));
                    }
                    break;
            }
            setIcone(icone);
    }//GEN-LAST:event_jTableDoublePropertyChange

    private void jTableMestrePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTableMestrePropertyChange
        // TODO add your handling code here:
        if ((Boolean) jTableMestre.getValueAt(0, 1)) {
            icone.setMestre(true);
            jTableComboBox.setVisible(true);
            jComboBoxAlgoritmos.setVisible(true);
            jListEscravo.setVisible(true);
            jScrollPane1.setVisible(true);
        } else {
            icone.setMestre(false);
            jTableComboBox.setVisible(false);
            jComboBoxAlgoritmos.setVisible(false);
            jListEscravo.setVisible(false);
            jScrollPane1.setVisible(false);
        }
    }//GEN-LAST:event_jTableMestrePropertyChange

    private void jTableComboBoxPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTableComboBoxPropertyChange
        // TODO add your handling code here:
        if (icone != null) {
            icone.setAlgoritmo(jComboBoxAlgoritmos.getSelectedItem().toString());
            jTableComboBox.setValueAt(jComboBoxAlgoritmos.getSelectedItem().toString(), 0, 1);
        }
    }//GEN-LAST:event_jTableComboBoxPropertyChange

    private void jListEscravoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListEscravoMouseClicked
        // TODO add your handling code here:
        if (icone != null) {
            Vector<Integer> listaConectados = icone.getNosEscalonaveis();
            int indices[] = jListEscravo.getSelectedIndices();
            List<Integer> escravos = new ArrayList<Integer>();
            for (int i = 0; i < indices.length; i++) {
                escravos.add(listaConectados.get(indices[i]));
            }
            icone.setEscravos(escravos);
        }
    }//GEN-LAST:event_jListEscravoMouseClicked

    private void jListEscravoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListEscravoKeyPressed
        // TODO add your handling code here:
        jListEscravoMouseClicked(null);
    }//GEN-LAST:event_jListEscravoKeyPressed

    private void jTableComboBox1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTableComboBox1PropertyChange
        // TODO add your handling code here:
        icone.setProprietario(jComboBoxUsuarios.getSelectedItem().toString());
        jTableComboBox1.setValueAt(jComboBoxUsuarios.getSelectedItem().toString(), 0, 1);
    }//GEN-LAST:event_jTableComboBox1PropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxUsuarios;
    private javax.swing.JLabel jLabel;
    private javax.swing.JLabel jLabelInicial;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JList jListEscravo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableComboBox;
    private javax.swing.JTable jTableComboBox1;
    private javax.swing.JTable jTableDouble;
    private javax.swing.JTable jTableMestre;
    private javax.swing.JTable jTableString;
    // End of variables declaration//GEN-END:variables
    private JComboBox jComboBoxAlgoritmos;

    public void setIcone(Icone icone) {
        int numEscal = Escalonadores.ESCALONADORES.length + escalonadores.listar().size();
        if (nomesDosEscalonadores.size() > numEscal) {
            for (int i = Escalonadores.ESCALONADORES.length; i <  nomesDosEscalonadores.size(); i++) {
                String nome = nomesDosEscalonadores.get(i);
                if (!escalonadores.listar().contains(nome)) {
                    nomesDosEscalonadores.remove(nome);
                }
            }
        } else if (nomesDosEscalonadores.size() < numEscal) {
            for (int i = 0; i <  escalonadores.listar().size(); i++) {
                String nome = escalonadores.listar().get(i);
                if (!nomesDosEscalonadores.contains(nome)) {
                    nomesDosEscalonadores.add(nome);
                }
            }
        }
        this.icone = icone;
        this.jLabelInicial.setText(palavras.getString("Configuration for the icon") + "#: " + String.valueOf(icone.getIdGlobal()));
        jTableString.setValueAt(icone.getNome(), 0, 1);
        jTableDouble.setValueAt(icone.getPoderComputacional(), 0, 1);
        jTableDouble.setValueAt(icone.getTaxaOcupacao(), 1, 1);
        jTableMestre.setValueAt(icone.isMestre(), 0, 1);
        int index = nomesDosEscalonadores.indexOf(icone.getAlgoritmo());
        if (index != -1) {
            jComboBoxAlgoritmos.setSelectedIndex(index);
        } else {
            jComboBoxAlgoritmos.setSelectedIndex(0);
        }
        jTableComboBox.setValueAt(icone.getAlgoritmo(), 0, 1);
        jListEscravo.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        DefaultListModel listModel = new DefaultListModel();
        for (int i = 0; i < icone.getNosEscalonaveis().size(); i++) {
            listModel.addElement(icone.getNosEscalonaveis().get(i));
        }
        jListEscravo.setModel(listModel);
        List<Integer> escravos = icone.getEscravos();
        Vector<Integer> listaConectados = icone.getNosEscalonaveis();
        int tempIndices[] = new int[escravos.size()];
        int x = 0;
        for (int i : escravos) {
            tempIndices[x] = listaConectados.indexOf(i);
            x++;
        }
        jListEscravo.setSelectedIndices(tempIndices);
        jComboBoxUsuarios.setSelectedItem(icone.getProprietario());
        jTableComboBox.setValueAt(icone.getProprietario(), 0, 1);
    }

    public void setIdioma(ResourceBundle palavras) {
        this.palavras = palavras;
        initTexts();
    }

    public void setEscalonadores(ManipularArquivos escalonadores) {
        this.escalonadores = escalonadores;
    }

    private void initTexts() {
        jLabelTitle.setText(palavras.getString("Machine icon configuration"));
        if (icone == null) {
            jLabelInicial.setText(palavras.getString("Configuration for the icon") + "#: 0");
        } else {
            jLabelInicial.setText(palavras.getString("Configuration for the icon") + "#: " + String.valueOf(icone.getIdGlobal()));
        }
        jTableString.setValueAt(palavras.getString("Label:"), 0, 0);
        jTableComboBox1.setValueAt(palavras.getString("Owner:"), 0, 0);
        jTableDouble.setValueAt(palavras.getString("Processing:"), 0, 0);
        jTableDouble.setValueAt(palavras.getString("Load Factor:"), 1, 0);
        jTableMestre.setValueAt(palavras.getString("MASTER"), 0, 0);
        jTableComboBox.setValueAt(palavras.getString("Scheduler") + ":", 0, 0);
        jListEscravo.setBorder(javax.swing.BorderFactory.createTitledBorder(palavras.getString("Slave Nodes:")));
    }

    public void setUsuarios(HashSet<String> usuarios) {
        jComboBoxUsuarios.setModel(new javax.swing.DefaultComboBoxModel(usuarios.toArray()));
    }
}