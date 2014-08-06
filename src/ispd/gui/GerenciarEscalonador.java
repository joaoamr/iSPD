/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GerenciarEscalonador2.java
 *
 * Created on 05/04/2011, 09:33:05
 */
package ispd.gui;

import ispd.ValidaValores;
import ispd.gui.componenteauxiliar.FiltroDeArquivos;
import ispd.arquivo.Escalonadores;
import ispd.escalonador.ManipularArquivos;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.actions.CaretMonitor;

/**
 *
 * @author denison_usuario
 */
public class GerenciarEscalonador extends javax.swing.JFrame {

    /** Creates new form GerenciarEscalonador2 */
    public GerenciarEscalonador() {
        Locale locale = Locale.getDefault();
        palavras = ResourceBundle.getBundle("ispd.idioma.Idioma", locale);
        //Inicia o editor
        DefaultSyntaxKit.initKit();
        initComponents();
        //Define a linguagem do editor
        jEditorPane.setContentType("text/java");
        fecharEdicao();
        //Obtem e escreve posição atual no texto
        CaretMonitor caretMonitor = new CaretMonitor(jEditorPane, jLabelCaretPos);
        // Eventos de desfazer e refazer
        javax.swing.text.Document doc = jEditorPane.getDocument();
        doc.addUndoableEditListener(new UndoableEditListener() {

            @Override
            public void undoableEditHappened(UndoableEditEvent evt) {
                undo.addEdit(evt.getEdit());
            }
        });
        // Evento verifica alterações
        doc.addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!modificado) {
                    modificar();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!modificado) {
                    modificar();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        //Gerenciamento dos escalonadores
        this.escalonadores = new Escalonadores();
        atualizarEscalonadores(escalonadores.listar());
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (modificado) {
                    int escolha = savarAlteracao();
                    if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
                        setVisible(false);//System.exit(0);
                    }
                } else {
                    setVisible(false);//System.exit(0);
                }
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

        jPopupMenuTexto = new javax.swing.JPopupMenu();
        jMenuItemCut1 = new javax.swing.JMenuItem();
        jMenuItemCopy1 = new javax.swing.JMenuItem();
        jMenuItemPaste1 = new javax.swing.JMenuItem();
        jFileChooser1 = new javax.swing.JFileChooser();
        jToolBar1 = new javax.swing.JToolBar();
        jButtonNovo = new javax.swing.JButton();
        jButtonSalvar = new javax.swing.JButton();
        jButtonCompilar = new javax.swing.JButton();
        jPanelEscalonadores = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListEscalonadores = new javax.swing.JList();
        jPanelEditorTexto = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane = new javax.swing.JEditorPane();
        jLabelCaretPos = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuArquivo = new javax.swing.JMenu();
        jMenuItemNovo = new javax.swing.JMenuItem();
        jMenuItemAbrir = new javax.swing.JMenuItem();
        jMenuItemSalvar = new javax.swing.JMenuItem();
        jMenuItemImportar = new javax.swing.JMenuItem();
        jMenuEditar = new javax.swing.JMenu();
        jMenuItemDesfazer = new javax.swing.JMenuItem();
        jMenuItemRefazer = new javax.swing.JMenuItem();
        javax.swing.JPopupMenu.Separator jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItemCut = new javax.swing.JMenuItem();
        jMenuItemCopy = new javax.swing.JMenuItem();
        jMenuItemPaste = new javax.swing.JMenuItem();
        javax.swing.JPopupMenu.Separator jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItemDelete = new javax.swing.JMenuItem();

        jMenuItemCut1.setText(palavras.getString("Cut")); // NOI18N
        jMenuItemCut1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCutActionPerformed(evt);
            }
        });
        jPopupMenuTexto.add(jMenuItemCut1);

        jMenuItemCopy1.setText(palavras.getString("Copy")); // NOI18N
        jMenuItemCopy1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopyActionPerformed(evt);
            }
        });
        jPopupMenuTexto.add(jMenuItemCopy1);

        jMenuItemPaste1.setText(palavras.getString("Paste")); // NOI18N
        jMenuItemPaste1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPasteActionPerformed(evt);
            }
        });
        jPopupMenuTexto.add(jMenuItemPaste1);

        jFileChooser1.setAcceptAllFileFilterUsed(false);
        jFileChooser1.setFileFilter(new FiltroDeArquivos(palavras.getString("Java Source Files (. java)"), ".java", true));

        setTitle(palavras.getString("Manage Schedulers")); // NOI18N
        setAlwaysOnTop(true);
        setFocusable(false);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("imagens/Logo_iSPD_25.png")));

        jToolBar1.setRollover(true);

        jButtonNovo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/insert-object.png"))); // NOI18N
        jButtonNovo.setToolTipText(palavras.getString("Creates a new scheduler")); // NOI18N
        jButtonNovo.setFocusable(false);
        jButtonNovo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonNovo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNovojButtonNovoActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonNovo);
        jButtonNovo.getAccessibleContext().setAccessibleDescription(palavras.getString("Creates a new scheduler")); // NOI18N

        jButtonSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/document-save.png"))); // NOI18N
        jButtonSalvar.setToolTipText(palavras.getString("Save the open file")); // NOI18N
        jButtonSalvar.setFocusable(false);
        jButtonSalvar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSalvar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSalvarActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonSalvar);

        jButtonCompilar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/system-run.png"))); // NOI18N
        jButtonCompilar.setToolTipText(palavras.getString("Compile")); // NOI18N
        jButtonCompilar.setFocusable(false);
        jButtonCompilar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCompilar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonCompilar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCompilarActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonCompilar);

        jListEscalonadores.setBorder(javax.swing.BorderFactory.createTitledBorder(null, palavras.getString("Scheduler"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION)); // NOI18N
        jListEscalonadores.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListEscalonadores.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListEscalonadoresMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(jListEscalonadores);

        javax.swing.GroupLayout jPanelEscalonadoresLayout = new javax.swing.GroupLayout(jPanelEscalonadores);
        jPanelEscalonadores.setLayout(jPanelEscalonadoresLayout);
        jPanelEscalonadoresLayout.setHorizontalGroup(
            jPanelEscalonadoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEscalonadoresLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelEscalonadoresLayout.setVerticalGroup(
            jPanelEscalonadoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelEscalonadoresLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
                .addContainerGap())
        );

        jEditorPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jEditorPaneMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jEditorPane);

        javax.swing.GroupLayout jPanelEditorTextoLayout = new javax.swing.GroupLayout(jPanelEditorTexto);
        jPanelEditorTexto.setLayout(jPanelEditorTextoLayout);
        jPanelEditorTextoLayout.setHorizontalGroup(
            jPanelEditorTextoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 758, Short.MAX_VALUE)
            .addGroup(jPanelEditorTextoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelEditorTextoLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1)
                    .addContainerGap()))
        );
        jPanelEditorTextoLayout.setVerticalGroup(
            jPanelEditorTextoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 528, Short.MAX_VALUE)
            .addGroup(jPanelEditorTextoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelEditorTextoLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jMenuArquivo.setText(palavras.getString("File")); // NOI18N
        jMenuArquivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSalvarActionPerformed(evt);
            }
        });

        jMenuItemNovo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemNovo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/insert-object_1.png"))); // NOI18N
        jMenuItemNovo.setText(palavras.getString("New")); // NOI18N
        jMenuItemNovo.setToolTipText(palavras.getString("Creates a new scheduler")); // NOI18N
        jMenuItemNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNovojButtonNovoActionPerformed(evt);
            }
        });
        jMenuArquivo.add(jMenuItemNovo);

        jMenuItemAbrir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemAbrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/document-open.png"))); // NOI18N
        jMenuItemAbrir.setText(palavras.getString("Open")); // NOI18N
        jMenuItemAbrir.setToolTipText(palavras.getString("Opens an existing scheduler")); // NOI18N
        jMenuItemAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAbrirActionPerformed(evt);
            }
        });
        jMenuArquivo.add(jMenuItemAbrir);

        jMenuItemSalvar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/document-save_1.png"))); // NOI18N
        jMenuItemSalvar.setText(palavras.getString("Save")); // NOI18N
        jMenuItemSalvar.setToolTipText(palavras.getString("Save the open file")); // NOI18N
        jMenuItemSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSalvarActionPerformed(evt);
            }
        });
        jMenuArquivo.add(jMenuItemSalvar);

        jMenuItemImportar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemImportar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/document-import.png"))); // NOI18N
        jMenuItemImportar.setText(palavras.getString("Import")); // NOI18N
        jMenuItemImportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemImportarActionPerformed(evt);
            }
        });
        jMenuArquivo.add(jMenuItemImportar);

        jMenuBar1.add(jMenuArquivo);

        jMenuEditar.setText(palavras.getString("Edit")); // NOI18N

        jMenuItemDesfazer.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemDesfazer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/edit-undo.png"))); // NOI18N
        jMenuItemDesfazer.setText(palavras.getString("Undo")); // NOI18N
        jMenuItemDesfazer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDesfazerActionPerformed(evt);
            }
        });
        jMenuEditar.add(jMenuItemDesfazer);

        jMenuItemRefazer.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemRefazer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/edit-redo.png"))); // NOI18N
        jMenuItemRefazer.setText(palavras.getString("Redo")); // NOI18N
        jMenuItemRefazer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRefazerActionPerformed(evt);
            }
        });
        jMenuEditar.add(jMenuItemRefazer);
        jMenuEditar.add(jSeparator1);

        jMenuItemCut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemCut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/edit-cut.png"))); // NOI18N
        jMenuItemCut.setText(palavras.getString("Cut")); // NOI18N
        jMenuItemCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCutActionPerformed(evt);
            }
        });
        jMenuEditar.add(jMenuItemCut);

        jMenuItemCopy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/edit-copy.png"))); // NOI18N
        jMenuItemCopy.setText(palavras.getString("Copy")); // NOI18N
        jMenuItemCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopyActionPerformed(evt);
            }
        });
        jMenuEditar.add(jMenuItemCopy);

        jMenuItemPaste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/edit-paste.png"))); // NOI18N
        jMenuItemPaste.setText(palavras.getString("Paste")); // NOI18N
        jMenuItemPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPasteActionPerformed(evt);
            }
        });
        jMenuEditar.add(jMenuItemPaste);
        jMenuEditar.add(jSeparator2);

        jMenuItemDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/edit-delete.png"))); // NOI18N
        jMenuItemDelete.setText(palavras.getString("Delete")); // NOI18N
        jMenuItemDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeleteActionPerformed(evt);
            }
        });
        jMenuEditar.add(jMenuItemDelete);

        jMenuBar1.add(jMenuEditar);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelEscalonadores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelEditorTexto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(904, Short.MAX_VALUE)
                        .addComponent(jLabelCaretPos))
                    .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 904, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelCaretPos, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelEscalonadores, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelEditorTexto, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemNovojButtonNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNovojButtonNovoActionPerformed
        // TODO add your handling code here:
        int escolha = JOptionPane.YES_OPTION;
        if (modificado) {
            escolha = savarAlteracao();
        }
        if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
            String[] ops = {"Edit java class", "Generator schedulers"};
            String result = (String) JOptionPane.showInputDialog(this, "Creating the scheduler with:", null, JOptionPane.INFORMATION_MESSAGE, null, ops, ops[0]);
            if (result != null) {
                if (result.equals(ops[0])) {
                    String result1 = JOptionPane.showInputDialog(this, "Enter the name of the scheduler");
                    boolean nomeOk = false;
                    if (result1 != null) {
                        nomeOk = ValidaValores.validaNomeClasse(result1);
                    }
                    if(nomeOk){
                        //Carregar classe para esditar java
                        abrirEdicao(result1, Escalonadores.getEscalonadorJava(result1));
                        modificar();
                    }
                } else if (result.equals(ops[1])) {
                    //Carregar classe para construir escalonador automaticamente
                    GerarEscalonador ge = new GerarEscalonador(this, true, escalonadores.getDiretorio().getAbsolutePath(), palavras);
                    ge.setLocationRelativeTo(this);
                    ge.setVisible(true);
                    if(ge.getParse() != null){
                        escalonadores.escrever(ge.getParse().getNome(), ge.getParse().getCodigo());
                        String erros = escalonadores.compilar(ge.getParse().getNome());
                        if (erros != null) {
                            JOptionPane.showMessageDialog(this, erros, "Erros encontrados", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "Escalonador" + escalonadorAberto + "\nCompilador com sucesso");
                        }
                        atualizarEscalonadores(escalonadores.listar());
                    }
                }
            }
        }
}//GEN-LAST:event_jMenuItemNovojButtonNovoActionPerformed

    private void jEditorPaneMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jEditorPaneMouseReleased
        // TODO add your handling code here:
        if (evt.isPopupTrigger()) {
            jPopupMenuTexto.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_jEditorPaneMouseReleased

    private void jMenuItemCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCutActionPerformed
        // TODO add your handling code here:
        jEditorPane.cut();
    }//GEN-LAST:event_jMenuItemCutActionPerformed

    private void jMenuItemCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyActionPerformed
        // TODO add your handling code here:
        jEditorPane.copy();
    }//GEN-LAST:event_jMenuItemCopyActionPerformed

    private void jMenuItemPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPasteActionPerformed
        // TODO add your handling code here:
        jEditorPane.paste();
    }//GEN-LAST:event_jMenuItemPasteActionPerformed

    private void jMenuItemDesfazerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDesfazerActionPerformed
        // TODO add your handling code here:
        try {
            undo.undo();
        } catch (CannotUndoException e) {
        }
    }//GEN-LAST:event_jMenuItemDesfazerActionPerformed

    private void jMenuItemRefazerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRefazerActionPerformed
        // TODO add your handling code here:
        try {
            undo.redo();
        } catch (CannotRedoException e) {
        }
    }//GEN-LAST:event_jMenuItemRefazerActionPerformed

    private void jListEscalonadoresMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListEscalonadoresMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            int escolha = JOptionPane.YES_OPTION;
            if (modificado) {
                escolha = savarAlteracao();
            }
            if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
                //Escalonador a ser aberto
                String result = (String) jListEscalonadores.getSelectedValue();
                //Conteudo do arquivo
                String conteud = escalonadores.ler(result);
                //Adicionar ao editor
                abrirEdicao(result, conteud.toString());
            }
        }
    }//GEN-LAST:event_jListEscalonadoresMouseClicked

    private void jButtonSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSalvarActionPerformed
        // TODO add your handling code here:
        if (escalonadorAberto != null && modificado) {
            escalonadores.escrever(escalonadorAberto, this.jEditorPane.getText());
            salvarModificacao();
        }
    }//GEN-LAST:event_jButtonSalvarActionPerformed

    private void jMenuItemDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeleteActionPerformed
        // TODO add your handling code here:
        if (!jListEscalonadores.isSelectionEmpty()) {
            String aux = this.jListEscalonadores.getSelectedValue().toString();
            int escolha = JOptionPane.showConfirmDialog(this, "Are you sure want delete this scheduler: \n" + aux, null, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (escolha == JOptionPane.YES_OPTION) {
                if (!this.escalonadores.remover(aux)) {
                    JOptionPane.showMessageDialog(this, "Failed to remove " + aux);
                } else if (escalonadorAberto != null) {
                    if (escalonadorAberto.equals(aux)) {
                        fecharEdicao();
                    }
                    atualizarEscalonadores(this.escalonadores.listar());
                } else {
                    atualizarEscalonadores(this.escalonadores.listar());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "A scheduler should be selected");
        }
    }//GEN-LAST:event_jMenuItemDeleteActionPerformed

    private void jButtonCompilarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCompilarActionPerformed
        // TODO add your handling code here:
        if (escalonadorAberto != null) {
            if (modificado) {
                escalonadores.escrever(escalonadorAberto, this.jEditorPane.getText());
                salvarModificacao();
            }
            String erros = escalonadores.compilar(escalonadorAberto);
            if (erros != null) {
                JOptionPane.showMessageDialog(this, erros, "Erros encontrados", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Escalonador" + escalonadorAberto + "\nCompilador com sucesso");
            }
            atualizarEscalonadores(escalonadores.listar());
        }
    }//GEN-LAST:event_jButtonCompilarActionPerformed

    private void jMenuItemAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAbrirActionPerformed
        // TODO add your handling code here:
        int escolha = JOptionPane.YES_OPTION;
        if (modificado) {
            escolha = savarAlteracao();
        }
        if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
            BorderLayout chooserLayout = (BorderLayout) jFileChooser1.getLayout();
            chooserLayout.getLayoutComponent(BorderLayout.NORTH).setVisible(false);
            //aqui está o X da questão ;D
            jFileChooser1.getComponent(0).setVisible(false);
            jFileChooser1.setCurrentDirectory(escalonadores.getDiretorio());
            escolha = jFileChooser1.showOpenDialog(this);
            if (escolha == JFileChooser.APPROVE_OPTION) {
                File arquivo = jFileChooser1.getSelectedFile();
                if (arquivo != null) {
                    String nome = arquivo.getName().substring(0, arquivo.getName().length() - 5);
                    String conteud = escalonadores.ler(nome);
                    //Adicionar ao editor
                    abrirEdicao(nome, conteud.toString());
                }
            }
        }
    }//GEN-LAST:event_jMenuItemAbrirActionPerformed

    private void jMenuItemImportarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemImportarActionPerformed
        // TODO add your handling code here:
        int escolha = JOptionPane.YES_OPTION;
        if (modificado) {
            escolha = savarAlteracao();
        }
        if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
            BorderLayout chooserLayout = (BorderLayout) jFileChooser1.getLayout();
            chooserLayout.getLayoutComponent(BorderLayout.NORTH).setVisible(true);
            //aqui está o X da questão ;D
            jFileChooser1.getComponent(0).setVisible(true);
            jFileChooser1.setCurrentDirectory(null);
            jFileChooser1.showOpenDialog(this);
            if (escolha == JFileChooser.APPROVE_OPTION) {
                File arquivo = jFileChooser1.getSelectedFile();
                if (arquivo != null) {
                    if (escalonadores.importarEscalonadorJava(arquivo)) {
                        atualizarEscalonadores(escalonadores.listar());
                        String nome = arquivo.getName().substring(0, arquivo.getName().length() - 5);
                        String conteud = escalonadores.ler(nome);
                        //Adicionar ao editor
                        abrirEdicao(nome, conteud.toString());
                    } else {
                        JOptionPane.showMessageDialog(this, "Falha na importação", "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }//GEN-LAST:event_jMenuItemImportarActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCompilar;
    private javax.swing.JButton jButtonNovo;
    private javax.swing.JButton jButtonSalvar;
    private javax.swing.JEditorPane jEditorPane;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabelCaretPos;
    private javax.swing.JList jListEscalonadores;
    private javax.swing.JMenu jMenuArquivo;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuEditar;
    private javax.swing.JMenuItem jMenuItemAbrir;
    private javax.swing.JMenuItem jMenuItemCopy;
    private javax.swing.JMenuItem jMenuItemCopy1;
    private javax.swing.JMenuItem jMenuItemCut;
    private javax.swing.JMenuItem jMenuItemCut1;
    private javax.swing.JMenuItem jMenuItemDelete;
    private javax.swing.JMenuItem jMenuItemDesfazer;
    private javax.swing.JMenuItem jMenuItemImportar;
    private javax.swing.JMenuItem jMenuItemNovo;
    private javax.swing.JMenuItem jMenuItemPaste;
    private javax.swing.JMenuItem jMenuItemPaste1;
    private javax.swing.JMenuItem jMenuItemRefazer;
    private javax.swing.JMenuItem jMenuItemSalvar;
    private javax.swing.JPanel jPanelEditorTexto;
    private javax.swing.JPanel jPanelEscalonadores;
    private javax.swing.JPopupMenu jPopupMenuTexto;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
    private final UndoManager undo = new UndoManager();
    private ManipularArquivos escalonadores;
    private boolean modificado;//indica se arquivo atual foi modificado
    private String escalonadorAberto;
    private ResourceBundle palavras;

    public void atualizarEscalonadores(){
        atualizarEscalonadores(escalonadores.listar());
    }
    private void atualizarEscalonadores(ArrayList<String> escal) {
        this.jListEscalonadores.setListData(escal.toArray());
    }

    private int savarAlteracao() {
        int escolha = JOptionPane.showConfirmDialog(this, palavras.getString("Do you want to save changes to") + " " + escalonadorAberto + ".java");
        if (escolha == JOptionPane.YES_OPTION) {
            escalonadores.escrever(escalonadorAberto, this.jEditorPane.getText());
            salvarModificacao();
        }
        return escolha;
    }

    private void fecharEdicao() {
        this.setTitle(palavras.getString("Manage Schedulers"));
        this.escalonadorAberto = null;
        this.jEditorPane.setText("");
        this.jEditorPane.setEnabled(false);
        this.modificado = false;
    }

    private void abrirEdicao(String nome, String conteudo) {
        this.escalonadorAberto = nome;
        this.jEditorPane.setText(conteudo);
        this.jEditorPane.setEnabled(true);
        salvarModificacao();
    }

    private void modificar() {
        this.setTitle(escalonadorAberto + ".java [" + palavras.getString("modified") + "] - " + palavras.getString("Manage Schedulers"));
        this.modificado = true;
    }

    private void salvarModificacao() {
        this.setTitle(escalonadorAberto + ".java - " + palavras.getString("Manage Schedulers"));
        this.modificado = false;
    }

    public ManipularArquivos getEscalonadores() {
        return escalonadores;
    }
}