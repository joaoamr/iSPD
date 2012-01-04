package ispd.janela;

import DescreveSistema.DescreveIcone;
import DescreveSistema.DescreveSistema;
import Interface.AguardaSimulacao;
import ispd.ValidaValores;
import ispd.janela.configuracao.ConfiguraCluster;
import ispd.janela.configuracao.ConfiguraInternet;
import ispd.janela.configuracao.ConfiguraMaquina;
import ispd.janela.configuracao.ConfiguraRede;
import ispd.motor.carga.CargaForNode;
import ispd.motor.carga.CargaRandom;
import ispd.motor.carga.CargaTaskNode;
import ispd.motor.carga.GerarCarga;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Internet;
import ispd.motor.filas.servidores.CS_Link;
import ispd.motor.filas.servidores.CS_Maquina;
import ispd.motor.filas.servidores.CS_Mestre;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CS_Switch;
import ispd.motor.metricas.MetricasUsuarios;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class AreaDesenho extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

    private ResourceBundle palavras;
    //Objetos principais da classe
    private int w, h;
    /**
     * Lista com os icones presentes na area de desenho
     */
    private HashSet<Icone> icones;
    /**
     * Lista com os usuarios/proprietarios do modelo criado
     */
    private HashSet<String> usuarios;
    /**
     * Objeto para Manipular as cargas
     */
    private GerarCarga cargasConfiguracao;
    /**
     * número de icones excluindo os links
     */
    private int numArestas;
    /**
     * número de links
     */
    private int numVertices;
    /**
     * número total de icones
     */
    private int numIcones;
    //Objetos usados para controlas os popupmenus
    private JPopupMenu popupMenu;
    private JPopupMenu popupMenu2;
    private JMenuItem botaoRemove;
    private JMenuItem botaoCopiar;
    private JMenuItem botaoColar;
    private JMenuItem botaoInverter;
    private JSeparator jSeparator1;
    //Objetos advindo da classe JanelaPrincipal
    private JPrincipal janelaPrincipal;
    //Objetos usados para desenhar a regua e as grades
    private int units;
    private boolean metric;
    private int INCH;
    private boolean gridOn;
    //Objetos para Selecionar texto na Area Lateral
    private boolean imprimeNosConectados;
    private boolean imprimeNosIndiretos;
    private boolean imprimeNosEscalonaveis;
    //Objetos usados para add um icone
    private int tipoIcone;
    private boolean botaoSelecaoIconeClicado;
    private boolean primeiroClique;
    private int posPrimeiroCliqueX;
    private int posPrimeiroCliqueY;
    private int posSegundoCliqueX;
    private int posSegundoCliqueY;
    private int verticeInicio;
    private int verticeFim;
    //Objeots usados para minipular os icones
    private Icone iconeAuxiliar;
    private int posicaoMouseX;
    private int posicaoMouseY;
    private Icone iconeAuxiliarMatchRede;
    private Icone iconeNulo;
    private boolean iconeSelecionado;
    //Objetos para remover um icone
    private Icone iconeAuxiliaRemover;
    //Obejtos para copiar um icone
    private Icone iconeCopiado;
    private boolean acaoColar;

    public AreaDesenho(int w, int h) {

        //Utiliza o idioma do sistema como padrão
        Locale locale = Locale.getDefault();
        palavras = ResourceBundle.getBundle("ispd.idioma.Idioma", locale);

        addMouseListener(this);
        addMouseMotionListener(this);

        this.w = w;
        this.h = h;
        this.numArestas = 0;
        this.numVertices = 0;
        this.numIcones = 0;
        icones = new HashSet<Icone>();
        usuarios = new HashSet<String>();
        usuarios.add("user1");
        ValidaValores.removeTodosNomeIcone();
        metric = true;
        gridOn = false;
        INCH = Toolkit.getDefaultToolkit().getScreenResolution();
        tipoIcone = 0;
        botaoSelecaoIconeClicado = false;
        primeiroClique = false;
        cargasConfiguracao = null;
        imprimeNosConectados = false;
        imprimeNosIndiretos = false;
        imprimeNosEscalonaveis = true;
        acaoColar = false;
        iconeNulo = new Icone(-100, -100, -1, 0, 0);
        iconeAuxiliarMatchRede = iconeNulo;

    }

    public void setPaineis(JPrincipal janelaPrincipal) {
        this.janelaPrincipal = janelaPrincipal;
        this.initPopupMenu();
        this.initTexts();
    }

    public void initPopupMenu() {
        popupMenu = new JPopupMenu();
        popupMenu2 = new JPopupMenu();
        jSeparator1 = new JSeparator();

        botaoCopiar = new JMenuItem();
        botaoCopiar.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCopiarActionPerformed(evt);
            }
        });
        //popupMenu.add(botaoCopiar);

        botaoInverter = new JMenuItem();
        //botaoInverter.setEnabled(false);
        botaoInverter.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoInverterActionPerformed(evt);
            }
        });
        //popupMenu.add(botaoInverter);

        popupMenu.add(jSeparator1);

        botaoRemove = new JMenuItem();
        botaoRemove.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoRemoveActionPerformed(evt);
            }
        });
        //popupMenu.add(botaoRemove);

        botaoColar = new JMenuItem();
        botaoColar.setEnabled(false);
        botaoColar.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoColarActionPerformed(evt);
            }
        });
        popupMenu2.add(botaoColar);

    }

    //utilizado para inserir novo valor nas Strings dos componentes
    private void initTexts() {
        botaoCopiar.setText(palavras.getString("Copy"));
        botaoInverter.setText(palavras.getString("Turn Over"));
        botaoRemove.setText(palavras.getString("Remove"));
        botaoColar.setText(palavras.getString("Paste"));
    }

    private void montarBotoesPopupMenu(int tipo) {
        popupMenu.remove(botaoCopiar);
        popupMenu.remove(botaoRemove);
        popupMenu.remove(jSeparator1);
        popupMenu.remove(botaoInverter);
        if (tipo == 2) {
            popupMenu.add(botaoInverter);
            popupMenu.add(jSeparator1);
            popupMenu.add(botaoRemove);
        } else {
            popupMenu.add(botaoCopiar);
            popupMenu.add(jSeparator1);
            popupMenu.add(botaoRemove);
        }
    }

    public Icone adicionaAresta(int x, int y, int posPrimeiroCliqueX, int posPrimeiroCliqueY, int tipoIcone) {
        Icone I = new Icone(x, y, posPrimeiroCliqueX, posPrimeiroCliqueY, tipoIcone, numArestas, numIcones);
        numArestas++;
        numIcones++;
        icones.add(I);
        I.setEstaAtivo(true);
        I.setNoOrigem(verticeInicio);
        I.setNoDestino(verticeFim);
        I.setNome("icon" + I.getIdGlobal());
        ValidaValores.addNomeIcone(I.getNome());
        return I;
    }

    public Icone adicionaVertice(int x, int y, int tipoIcone) {
        Icone I = new Icone(x, y, tipoIcone, numVertices, numIcones);
        numVertices++;
        numIcones++;
        icones.add(I);
        I.setEstaAtivo(true);
        switch (I.getTipoIcone()) {
            case 1:
                this.janelaPrincipal.appendNotificacao(palavras.getString("Machine icon added."));
                break;
            case 3:
                this.janelaPrincipal.appendNotificacao(palavras.getString("Cluster icon added."));
                break;
            case 4:
                this.janelaPrincipal.appendNotificacao(palavras.getString("Internet icon added."));
                break;
        }
        I.setNome("icon" + I.getIdGlobal());
        ValidaValores.addNomeIcone(I.getNome());
        return I;
    }

    public int getIconWidth() {
        return w;
    }

    public int getIconHeight() {
        return h;
    }

    public HashSet<Icone> getIcones() {
        return icones;
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(w, h);
    }

    public void setConectados(boolean imprimeNosConectados) {
        this.imprimeNosConectados = imprimeNosConectados;
    }

    public void setIndiretos(boolean imprimeNosIndiretos) {
        this.imprimeNosIndiretos = imprimeNosIndiretos;
    }

    public void setEscalonaveis(boolean imprimeNosEscalonaveis) {
        this.imprimeNosEscalonaveis = imprimeNosEscalonaveis;
    }

    public void setIsMetric(boolean metric) {
        this.metric = metric;
        repaint();
    }

    public void setGrid(boolean gridOn) {
        this.gridOn = gridOn;
        repaint();
    }

    public HashSet<String> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(HashSet<String> usuarios) {
        this.usuarios = usuarios;
    }

    public GerarCarga getCargasConfiguracao() {
        return cargasConfiguracao;
    }

    public void setCargasConfiguracao(GerarCarga cargasConfiguracao) {
        this.cargasConfiguracao = cargasConfiguracao;
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(255, 255, 255));
        g2d.fillRect(0, 0, w, h);

        g2d.setColor(new Color(220, 220, 220));

        if (metric) {
            units = (int) ((double) INCH / (double) 2.54);
        } else {
            units = (int) INCH / 2;
        }

        if (gridOn) {
            for (int _w = 0; _w <= w; _w += units) {
                g2d.drawLine(_w, 0, _w, h);
            }
            for (int _h = 0; _h <= h; _h += units) {
                g2d.drawLine(0, _h, w, _h);
            }
        }


        //Desenha a linha da conexão de rede antes dela se estabelcer.
        if (botaoSelecaoIconeClicado && primeiroClique) {
            g2d.setColor(new Color(0, 0, 0));
            g2d.drawLine(posPrimeiroCliqueX, posPrimeiroCliqueY, posicaoMouseX, posicaoMouseY);
        }

        // Desenhamos todos os icones
        for (Icone I : icones) {
            if (I.getTipoIcone() == 2) {
                I.draw(g2d);
            }
        }
        for (Icone I : icones) {
            if (I.getTipoIcone() != 2) {
                I.draw(g2d);
            }
        }

    }

    public void mouseClicked(MouseEvent e) {

        if (botaoSelecaoIconeClicado) {
            //Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            for (Icone I : icones) {
                I.setEstaAtivo(false);
            }
            posicaoMouseX = e.getX();
            posicaoMouseY = e.getY();
            if (tipoIcone == 2) {
                if (!primeiroClique) {
                    boolean achouIcone = false;
                    for (Icone I : icones) {
                        boolean clicado = I.getRectEnvolvente(e.getX(), e.getY());
                        if (clicado) {
                            posPrimeiroCliqueX = I.getNumX();
                            posPrimeiroCliqueY = I.getNumY();
                            verticeInicio = I.getIdGlobal();
                            achouIcone = true;
                            break;
                        }
                    }
                    if (achouIcone) {
                        primeiroClique = true;
                    } else {
                        JOptionPane.showMessageDialog(null, palavras.getString("You must click an icon."), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
                        //setCursor(normalCursor);
                        //botaoSelecaoIconeClicado = false;
                    }
                } else {
                    boolean achouIcone = false;
                    for (Icone I : icones) {
                        boolean clicado = I.getRectEnvolvente(e.getX(), e.getY());
                        if (clicado && I.getTipoIcone() != 2) {
                            posSegundoCliqueX = I.getNumX();
                            posSegundoCliqueY = I.getNumY();
                            verticeFim = I.getIdGlobal();
                            if (verticeInicio != verticeFim) {
                                achouIcone = true;
                            }
                            break;
                        }
                    }
                    if (achouIcone) {
                        primeiroClique = false;
                        Icone I = adicionaAresta(posSegundoCliqueX, posSegundoCliqueY, posPrimeiroCliqueX, posPrimeiroCliqueY, tipoIcone);
                        this.janelaPrincipal.appendNotificacao(palavras.getString("Network connection added."));
                        this.janelaPrincipal.modificar();
                        this.setLabelAtributos(I);
                        //setCursor(normalCursor);
                        //botaoSelecaoIconeClicado = false;
                        //fors para adicionar numero do destino na origem e vice versa
                        for (Icone Ico : icones) {
                            if (Ico.getIdGlobal() == verticeInicio && Ico.getTipoIcone() != 2) {
                                Ico.addIdConexaoSaida(verticeFim);
                                break;
                            }
                        }
                        for (Icone Ico : icones) {
                            if (Ico.getIdGlobal() == verticeFim && Ico.getTipoIcone() != 2) {
                                Ico.addIdConexaoEntrada(verticeInicio);
                                break;
                            }
                        }
                        atualizaNosIndiretos();
                    } else {
                        JOptionPane.showMessageDialog(null, palavras.getString("You must click an icon."), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
                        //setCursor(normalCursor);
                        //botaoSelecaoIconeClicado = false;
                    }
                }
            } else {
                Icone I = adicionaVertice(posicaoMouseX, posicaoMouseY, tipoIcone);
                this.janelaPrincipal.modificar();
                this.setLabelAtributos(I);
                //setCursor(normalCursor);
                //botaoSelecaoIconeClicado = false;
            }

        } else {
            janelaPrincipal.setSelectedIcon(null, null);
            iconeAuxiliarMatchRede = iconeNulo;
            for (Icone I : icones) {
                I.setEstaAtivo(false);
            }
            for (Icone I : icones) {
                boolean clicado = I.getRectEnvolvente(e.getX(), e.getY());
                if (clicado) {
                    I.setEstaAtivo(true);
                    this.setLabelAtributos(I);
                    switch (e.getButton()) {
                        case MouseEvent.BUTTON1:
                            if (e.getClickCount() == 2) {
                                setAtributos(I);
                            } else {
                                iconeAuxiliarMatchRede = I;
                            }
                            break;
                        case MouseEvent.BUTTON2:
                            break;
                        case MouseEvent.BUTTON3:
                            iconeAuxiliaRemover = I;
                            this.montarBotoesPopupMenu(I.getTipoIcone());
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                            break;
                    }
                    break;
                } else {
                    switch (e.getButton()) {
                        case MouseEvent.BUTTON1:
                            break;
                        case MouseEvent.BUTTON2:
                            break;
                        case MouseEvent.BUTTON3:
                            posicaoMouseX = e.getX();
                            posicaoMouseY = e.getY();
                            popupMenu2.show(e.getComponent(), e.getX(), e.getY());
                            break;
                    }
                }
            }

        }
        repaint();
    }

    public void mouseEntered(MouseEvent e) {
        repaint();
    }

    public void mouseExited(MouseEvent e) {
        repaint();
    }

    public void mouseReleased(MouseEvent e) {
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
        posicaoMouseX = e.getX();
        posicaoMouseY = e.getY();
        if (botaoSelecaoIconeClicado) {
            Cursor hourglassCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
            setCursor(hourglassCursor);
        } else {
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
        }
        repaint();
    }

    public void mousePressed(MouseEvent e) {
        for (Icone I : icones) {
            I.setEstaAtivo(false);
        }
        for (Icone I : icones) {
            boolean clicado = I.getRectEnvolvente(e.getX(), e.getY());
            if (clicado && I.getTipoIcone() != 2) {
                iconeAuxiliar = I;
                iconeSelecionado = true;
                break;
            } else {
                iconeSelecionado = false;
            }
        }
        repaint();
    }

    public void mouseDragged(MouseEvent e) {
        if (iconeSelecionado) {
            if (iconeAuxiliar.getTipoIcone() != 2) {
                iconeAuxiliar.setEstaAtivo(true);
                if (iconeAuxiliar.getIdGlobal() != -1) {
                    this.setLabelAtributos(iconeAuxiliar);
                } else {
                    //this.jTextAreaBarraLateral.setText(palavras.getString("No icon selected."));
                    janelaPrincipal.setSelectedIcon(null, null);
                }
                posicaoMouseX = e.getX();
                posicaoMouseY = e.getY();
                for (Icone I : icones) {
                    if (I.getTipoIcone() == 2 && I.getNumX() == iconeAuxiliar.getNumX() && I.getNumY() == iconeAuxiliar.getNumY() && (I.getNoOrigem() == iconeAuxiliar.getIdGlobal() || I.getNoDestino() == iconeAuxiliar.getIdGlobal())) {
                        I.setPosition(posicaoMouseX, posicaoMouseY);
                    }
                }
                for (Icone I : icones) {
                    if (I.getTipoIcone() == 2 && I.getNumPreX() == iconeAuxiliar.getNumX() && I.getNumPreY() == iconeAuxiliar.getNumY() && (I.getNoOrigem() == iconeAuxiliar.getIdGlobal() || I.getNoDestino() == iconeAuxiliar.getIdGlobal())) {
                        I.setPrePosition(posicaoMouseX, posicaoMouseY);
                    }
                }
                iconeAuxiliar.setPosition(posicaoMouseX, posicaoMouseY);
            }
        }
        repaint();
    }

    public void actionPerformed(ActionEvent e) {
        for (Icone I : icones) {
            I.move();
        }
        atualizaNosIndiretos();
        repaint();
    }

    public void iniciarSimulacao() {
        Object objetos[] = new Object[4];
        objetos[0] = true;
        objetos[1] = cargasConfiguracao.getTipo();
        objetos[2] = cargasConfiguracao.toString();
        objetos[3] = icones;
        AguardaSimulacao janela = new AguardaSimulacao(objetos);
    }

    public void setIconeSelecionado(int tipoIcone) {
        this.tipoIcone = tipoIcone;
        this.botaoSelecaoIconeClicado = true;
        if (tipoIcone == 2) {
            this.primeiroClique = false;
        }
    }

    public void semIconeSelecionado() {
        this.botaoSelecaoIconeClicado = false;
        this.primeiroClique = false;
    }

    public void removeIcone(Icone I) {
        icones.remove(I);
    }

    public void atualizaNosIndiretos() {
        //Remover nodes Indiretos
        for (Icone I : icones) {
            I.clearNosIndiretosEntrada();
            I.clearNosIndiretosSaida();
        }

        //Inserir Nodes Indiretos
        int numIcoInternet = 0;
        for (Icone I : icones) {
            if (I.getTipoIcone() == 4) {
                numIcoInternet++;
            }
        }
        for (int i = 0; i < numIcoInternet; i++) {
            for (Icone I1 : icones) {
                if (I1.getTipoIcone() == 4) {
                    HashSet<Integer> listaOrigem = I1.getObjetoConexaoEntrada();
                    HashSet<Integer> listaDestino = I1.getObjetoConexaoSaida();
                    for (int temp1 : listaDestino) {
                        for (Icone I2 : icones) {
                            if (I2.getIdGlobal() == temp1) {
                                HashSet<Integer> listaIndiretosEntrada = I2.getObjetoNosIndiretosEntrada();
                                for (Integer temp2 : listaOrigem) {
                                    if (!listaIndiretosEntrada.contains(temp2) && I2.getIdGlobal() != temp2) {
                                        listaIndiretosEntrada.add(temp2);
                                    }
                                }
                                I2.setObjetoNosIndiretosEntrada(listaIndiretosEntrada);
                            }
                        }
                    }
                    listaOrigem = I1.getObjetoConexaoEntrada();
                    listaDestino = I1.getObjetoConexaoSaida();
                    for (int temp1 : listaOrigem) {
                        for (Icone I2 : icones) {
                            if (I2.getIdGlobal() == temp1) {
                                HashSet<Integer> listaIndiretosSaida = I2.getObjetoNosIndiretosSaida();
                                for (Integer temp2 : listaDestino) {
                                    if (!listaIndiretosSaida.contains(temp2) && I2.getIdGlobal() != temp2) {
                                        listaIndiretosSaida.add(temp2);
                                    }
                                }
                                I2.setObjetoNosIndiretosSaida(listaIndiretosSaida);
                            }
                        }
                    }
                    listaOrigem = I1.getObjetoNosIndiretosEntrada();
                    listaDestino = I1.getObjetoConexaoSaida();
                    for (int temp1 : listaOrigem) {
                        for (Icone I2 : icones) {
                            if (I2.getIdGlobal() == temp1) {
                                HashSet<Integer> listaIndiretosSaida = I2.getObjetoNosIndiretosSaida();
                                for (Integer temp2 : listaDestino) {
                                    if (!listaIndiretosSaida.contains(temp2) && I2.getIdGlobal() != temp2) {
                                        listaIndiretosSaida.add(temp2);
                                    }
                                }
                                I2.setObjetoNosIndiretosSaida(listaIndiretosSaida);
                            }
                        }
                    }
                    listaOrigem = I1.getObjetoConexaoEntrada();
                    listaDestino = I1.getObjetoNosIndiretosSaida();
                    for (int temp1 : listaDestino) {
                        for (Icone I2 : icones) {
                            if (I2.getIdGlobal() == temp1) {
                                HashSet<Integer> listaIndiretosEntrada = I2.getObjetoNosIndiretosEntrada();
                                for (Integer temp2 : listaOrigem) {
                                    if (!listaIndiretosEntrada.contains(temp2) && I2.getIdGlobal() != temp2) {
                                        listaIndiretosEntrada.add(temp2);
                                    }
                                }
                                I2.setObjetoNosIndiretosEntrada(listaIndiretosEntrada);
                            }
                        }
                    }

                    /*HashSet<Integer> listaIndiretosEntrada = I1.getObjetoNosIndiretosEntrada();
                    for(int temp1:listaIndiretosEntrada){
                    for(Icone I2:icones){
                    if(I2.getIdGlobal()==temp1){
                    HashSet<Integer> listaDestino = I2.getObjetoNosIndiretos();
                    HashSet<Integer> listaOrigem2 = I1.getObjetoNosIndiretos();
                    for(int temp2:listaOrigem2){
                    if(!listaDestino.contains(temp2) && temp2!=I2.getID()){
                    listaDestino.add(temp2);
                    }
                    }
                    I2.setObjetoNosIndiretos(listaDestino);
                    }
                    }
                    }*/
                }
            }
        }

        //Atualiza nos escalonaveis
        //Remover nos Escalonaveis
        for (Icone I : icones) {
            I.clearNosEscalonaveis();
        }

        //adiciona nos escalonaveis
        for (Icone I : icones) {
            if (I.getTipoIcone() != 2 && I.getTipoIcone() != 4) {
                HashSet<Integer> listaOrigem1 = I.getObjetoConexaoSaida();
                HashSet<Integer> listaOrigem2 = I.getObjetoNosIndiretosSaida();
                HashSet<Integer> listaDestino = I.getObjetoNosEscalonaveis();
                //listaDestino.add(I.getIdGlobal());
                for (int temp1 : listaOrigem1) {
                    for (Icone I2 : icones) {
                        if (I2.getTipoIcone() != 2 && I2.getTipoIcone() != 4 && temp1 == I2.getIdGlobal()) {
                            listaDestino.add(temp1);
                        }
                    }
                }
                for (int temp1 : listaOrigem2) {
                    for (Icone I2 : icones) {
                        if (I2.getTipoIcone() != 2 && I2.getTipoIcone() != 4 && temp1 == I2.getIdGlobal()) {
                            listaDestino.add(temp1);
                        }
                    }
                }
                I.setObjetoNosEscalonaveis(listaDestino);
            }
        }
    }

    private void botaoRemoveActionPerformed(java.awt.event.ActionEvent evt) {
        acaoRemove();
    }

    private void acaoRemove() {
        int opcao = JOptionPane.showConfirmDialog(null, palavras.getString("Remove this icon?"), palavras.getString("Remove"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcao == JOptionPane.YES_OPTION) {
            if (iconeAuxiliaRemover.getTipoIcone() == 2) {
                int j = 0;
                for (Icone I1 : icones) {
                    if (I1.getIdGlobal() == iconeAuxiliaRemover.getNoOrigem() && I1.getTipoIcone() != 2) {
                        for (Icone I2 : icones) {
                            if (I2.getIdGlobal() == iconeAuxiliaRemover.getNoDestino() && I2.getTipoIcone() != 2) {
                                I1.removeConexaoSaida(I2.getIdGlobal());
                                I2.removeConexaoEntrada(I1.getIdGlobal());
                                break;
                            }
                        }
                        break;
                    }
                }
                ValidaValores.removeNomeIcone(iconeAuxiliaRemover.getNome());
                removeIcone(iconeAuxiliaRemover);
                this.janelaPrincipal.modificar();
                atualizaNosIndiretos();
            } else {
                int cont = 0;
                //Remover dados das conexoes q entram
                HashSet<Integer> listanos = iconeAuxiliaRemover.getObjetoConexaoEntrada();
                for (int i : listanos) {
                    for (Icone I : icones) {
                        if (i == I.getIdGlobal() && I.getTipoIcone() != 2) {
                            I.removeConexaoSaida(iconeAuxiliaRemover.getIdGlobal());
                            break;
                        }
                    }
                }
                //Remover dados das conexoes q saem
                listanos = iconeAuxiliaRemover.getObjetoConexaoSaida();
                for (int i : listanos) {
                    for (Icone I : icones) {
                        if (i == I.getIdGlobal() && I.getTipoIcone() != 2) {
                            I.removeConexaoEntrada(iconeAuxiliaRemover.getIdGlobal());
                            break;
                        }
                    }
                }
                for (Icone I : icones) {
                    if (I.getTipoIcone() == 2 && ((I.getNumX() == iconeAuxiliaRemover.getNumX() && I.getNumY() == iconeAuxiliaRemover.getNumY()) || (I.getNumPreX() == iconeAuxiliaRemover.getNumX() && I.getNumPreY() == iconeAuxiliaRemover.getNumY()))) {
                        cont++;
                    }
                }
                for (int j = 0; j < cont; j++) {
                    for (Icone I : icones) {
                        if (I.getTipoIcone() == 2 && ((I.getNumX() == iconeAuxiliaRemover.getNumX() && I.getNumY() == iconeAuxiliaRemover.getNumY()) || (I.getNumPreX() == iconeAuxiliaRemover.getNumX() && I.getNumPreY() == iconeAuxiliaRemover.getNumY()))) {
                            ValidaValores.removeNomeIcone(I.getNome());
                            removeIcone(I);
                            break;
                        }
                    }
                }
                ValidaValores.removeNomeIcone(iconeAuxiliaRemover.getNome());
                removeIcone(iconeAuxiliaRemover);
                this.janelaPrincipal.modificar();
                atualizaNosIndiretos();
            }
            repaint();
        }
    }

    public void deletarIcone() {
        boolean iconeEncontrado = false;
        for (Icone I : icones) {
            if (I.getEstaAtivo() == true) {
                iconeEncontrado = true;
                iconeAuxiliaRemover = I;
                acaoRemove();
                break;
            }
        }
        if (!iconeEncontrado) {
            JOptionPane.showMessageDialog(null, palavras.getString("No icon selected."), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void botaoCopiarActionPerformed(java.awt.event.ActionEvent evt) {
        //Não copia conexão de rede
        if (iconeAuxiliaRemover.getTipoIcone() != 2) {
            iconeCopiado = iconeAuxiliaRemover;
            acaoColar = true;
            botaoColar.setEnabled(true);
        }
    }

    public void acaoCopiarIcone() {
        boolean iconeEncontrado = false;
        for (Icone I : icones) {
            if (I.getEstaAtivo() == true) {
                iconeEncontrado = true;
                iconeCopiado = I;
                acaoColar = true;
                botaoColar.setEnabled(true);
                break;
            }
        }
        if (!iconeEncontrado) {
            JOptionPane.showMessageDialog(null, palavras.getString("No icon selected."), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void botaoColarActionPerformed(java.awt.event.ActionEvent evt) {
        acaoColarIcone();
    }

    public void acaoColarIcone() {
        for (Icone i : icones) {
            i.setEstaAtivo(false);
        }
        if (acaoColar == true && iconeCopiado.getTipoIcone() != 2) {
            Icone I = adicionaVertice(posicaoMouseX, posicaoMouseY, iconeCopiado.getTipoIcone());
            I.setNome("icon" + I.getIdGlobal());
            ValidaValores.addNomeIcone(I.getNome());
            I.setPoderComputacional(iconeCopiado.getPoderComputacional());
            I.setTaxaOcupacao(iconeCopiado.getTaxaOcupacao());
            I.setLatencia(iconeCopiado.getLatencia());
            I.setBanda(iconeCopiado.getBanda());
            I.setAlgoritmo(iconeCopiado.getAlgoritmo());
            I.setNumeroEscravos(iconeCopiado.getNumeroEscravos());
            this.janelaPrincipal.modificar();
        } else {
            //colar conexão de rede
        }
        repaint();
    }

    private void botaoInverterActionPerformed(java.awt.event.ActionEvent evt) {
        if (iconeAuxiliaRemover.getTipoIcone() == 2) {
            iconeAuxiliaRemover.setEstaAtivo(false);
            Icone I = adicionaAresta(iconeAuxiliaRemover.getNumPreX(), iconeAuxiliaRemover.getNumPreY(), iconeAuxiliaRemover.getNumX(), iconeAuxiliaRemover.getNumY(), iconeAuxiliaRemover.getTipoIcone());
            I.setNoOrigem(iconeAuxiliaRemover.getNoDestino());
            I.setNoDestino(iconeAuxiliaRemover.getNoOrigem());
            I.setPoderComputacional(iconeAuxiliaRemover.getPoderComputacional());
            I.setTaxaOcupacao(iconeAuxiliaRemover.getTaxaOcupacao());
            I.setLatencia(iconeAuxiliaRemover.getLatencia());
            I.setBanda(iconeAuxiliaRemover.getBanda());
            I.setAlgoritmo(iconeAuxiliaRemover.getAlgoritmo());
            I.setNumeroEscravos(iconeAuxiliaRemover.getNumeroEscravos());
            this.janelaPrincipal.modificar();
            //fors para adicionar numero do destino na origem e vice versa
            for (Icone Ico : icones) {
                if (Ico.getIdGlobal() == iconeAuxiliaRemover.getNoDestino() && Ico.getTipoIcone() != 2) {
                    Ico.addIdConexaoSaida(iconeAuxiliaRemover.getNoOrigem());
                    break;
                }
            }
            for (Icone Ico : icones) {
                if (Ico.getIdGlobal() == iconeAuxiliaRemover.getNoOrigem() && Ico.getTipoIcone() != 2) {
                    Ico.addIdConexaoEntrada(iconeAuxiliaRemover.getNoDestino());
                    break;
                }
            }
            this.janelaPrincipal.appendNotificacao(palavras.getString("Network connection added."));
            this.janelaPrincipal.modificar();
            this.setLabelAtributos(I);
            atualizaNosIndiretos();
        }
    }

    private void setAtributos(Icone I) {
        ConfiguraMaquina configMaquina;
        ConfiguraRede configuraRede;
        ConfiguraCluster configuraCluster;
        ConfiguraInternet configuraInternet;

        this.janelaPrincipal.modificar();
        atualizaNosIndiretos();

        switch (I.getTipoIcone()) {
            case 1: {
                configMaquina = new ConfiguraMaquina(I);
                configMaquina.setVisible(true);
            }
            break;
            case 2: {
                configuraRede = new ConfiguraRede(I);
                configuraRede.setVisible(true);
            }
            break;
            case 3: {
                configuraCluster = new ConfiguraCluster(I);
                configuraCluster.setVisible(true);
            }
            break;
            case 4: {
                configuraInternet = new ConfiguraInternet(I);
                configuraInternet.setVisible(true);
            }
            break;
        }
        this.setLabelAtributos(I);
        repaint();
    }

    @Override
    public String toString() {
        StringBuilder saida = new StringBuilder();
        for (Icone I : icones) {
            if (I.getTipoIcone() == 1) {
                saida.append(String.format("MAQ %s %f %f ", I.getNome(), I.getPoderComputacional(), I.getTaxaOcupacao()));
                if (I.isMestre()) {
                    saida.append(String.format("MESTRE " + I.getAlgoritmo() + " LMAQ"));
                    List<Integer> lista = I.getEscravos();
                    for (int temp : lista) {
                        for (Icone Ico : icones) {
                            if (Ico.getIdGlobal() == temp && Ico.getTipoIcone() != 2) {
                                saida.append(" ").append(Ico.getNome());
                            }
                        }
                    }
                } else {
                    saida.append("ESCRAVO");
                }
                saida.append("\n");
            }
        }

        for (Icone I : icones) {
            if (I.getTipoIcone() == 3) {
                saida.append(String.format("CLUSTER %s %d %f %f %f %s\n", I.getNome(), I.getNumeroEscravos(), I.getPoderComputacional(), I.getBanda(), I.getLatencia(), I.getAlgoritmo()));
            }
        }

        for (Icone I : icones) {
            if (I.getTipoIcone() == 4) {
                saida.append(String.format("INET %s %f %f %f\n", I.getNome(), I.getBanda(), I.getLatencia(), I.getTaxaOcupacao()));
            }
        }

        for (Icone I : icones) {
            if (I.getTipoIcone() == 2) {
                saida.append(String.format("REDE %s %f %f %f CONECTA", I.getNome(), I.getBanda(), I.getLatencia(), I.getTaxaOcupacao()));
                for (Icone Ico : icones) {
                    if (Ico.getIdGlobal() == I.getNoOrigem() && Ico.getTipoIcone() != 2) {
                        saida.append(" ").append(Ico.getNome());
                    }
                }
                for (Icone Ico : icones) {
                    if (Ico.getIdGlobal() == I.getNoDestino() && Ico.getTipoIcone() != 2) {
                        saida.append(" ").append(Ico.getNome());
                    }
                }
                saida.append("\n");
            }
        }

        saida.append("CARGA");
        if (cargasConfiguracao != null) {
            switch (cargasConfiguracao.getTipo()) {
                case GerarCarga.RANDOM:
                    saida.append(" RANDOM\n").append(cargasConfiguracao.toString()).append("\n");
                    break;
                case GerarCarga.FORNODE:
                    saida.append(" MAQUINA\n").append(cargasConfiguracao.toString()).append("\n");
                    break;
                case GerarCarga.TRACE:
                    saida.append(" TRACE\n").append(cargasConfiguracao.toString()).append("\n");
                    break;
            }
        }
        return saida.toString();
    }

    public void setLabelAtributos(Icone I) {
        String Texto = "<html>";
        HashSet<Integer> listaEntrada = I.getObjetoConexaoEntrada();
        HashSet<Integer> listaSaida = I.getObjetoConexaoSaida();
        switch (I.getTipoIcone()) {
            case 1: {
                Texto += palavras.getString("Local ID:") + " " + String.valueOf(I.getIdLocal())
                        + "<br>" + palavras.getString("Global ID:") + " " + String.valueOf(I.getIdGlobal())
                        + "<br>" + palavras.getString("Label:") + " " + I.getNome()
                        + "<br>" + palavras.getString("X-coordinate:") + " " + String.valueOf(I.getNumX())
                        + "<br>" + palavras.getString("Y-coordinate:") + " " + String.valueOf(I.getNumY())
                        + "<br>" + palavras.getString("Computational power:") + " " + String.valueOf(I.getPoderComputacional())
                        + "<br>" + palavras.getString("Load Factor:") + " " + String.valueOf(I.getTaxaOcupacao());
                if (I.isMestre()) {
                    Texto = Texto
                            + "<br>" + palavras.getString("MASTER")
                            + "<br>" + palavras.getString("Scheduling algorithm:") + " " + I.getAlgoritmo();
                } else {
                    Texto = Texto
                            + "<br>" + palavras.getString("SLAVE");
                }
            }
            break;
            case 2: {
                Texto += palavras.getString("Local ID:") + " " + String.valueOf(I.getIdLocal())
                        + "<br>" + palavras.getString("Global ID:") + " " + String.valueOf(I.getIdGlobal())
                        + "<br>" + palavras.getString("Label:") + " " + I.getNome()
                        + "<br>" + palavras.getString("X1-coordinate:") + " " + String.valueOf(I.getNumX())
                        + "<br>" + palavras.getString("Y1-coordinate:") + " " + String.valueOf(I.getNumY())
                        + "<br>" + palavras.getString("X2-coordinate:") + " " + String.valueOf(I.getNumPreX())
                        + "<br>" + palavras.getString("Y2-coordinate:") + " " + String.valueOf(I.getNumPreY())
                        + "<br>" + palavras.getString("Bandwidth:") + " " + String.valueOf(I.getBanda())
                        + "<br>" + palavras.getString("Latency:") + " " + String.valueOf(I.getLatencia())
                        + "<br>" + palavras.getString("Load Factor:") + " " + String.valueOf(I.getTaxaOcupacao());
            }
            break;
            case 3: {
                Texto += palavras.getString("Local ID:") + " " + String.valueOf(I.getIdLocal())
                        + "<br>" + palavras.getString("Global ID:") + " " + String.valueOf(I.getIdGlobal())
                        + "<br>" + palavras.getString("Label:") + " " + I.getNome()
                        + "<br>" + palavras.getString("X-coordinate:") + " " + String.valueOf(I.getNumX())
                        + "<br>" + palavras.getString("Y-coordinate:") + " " + String.valueOf(I.getNumY())
                        + "<br>" + palavras.getString("Number of slaves:") + " " + String.valueOf(I.getNumeroEscravos())
                        + "<br>" + palavras.getString("Computing power:") + " " + String.valueOf(I.getPoderComputacional())
                        + "<br>" + palavras.getString("Bandwidth:") + " " + String.valueOf(I.getBanda())
                        + "<br>" + palavras.getString("Latency:") + " " + String.valueOf(I.getLatencia())
                        + "<br>" + palavras.getString("Scheduling algorithm:") + " " + I.getAlgoritmo();
            }
            break;
            case 4: {
                Texto += palavras.getString("Local ID:") + " " + String.valueOf(I.getIdLocal())
                        + "<br>" + palavras.getString("Global ID:") + " " + String.valueOf(I.getIdGlobal())
                        + "<br>" + palavras.getString("Label:") + " " + I.getNome()
                        + "<br>" + palavras.getString("X-coordinate:") + " " + String.valueOf(I.getNumX())
                        + "<br>" + palavras.getString("Y-coordinate:") + " " + String.valueOf(I.getNumY())
                        + "<br>" + palavras.getString("Bandwidth:") + " " + String.valueOf(I.getBanda())
                        + "<br>" + palavras.getString("Latency:") + " " + String.valueOf(I.getLatencia())
                        + "<br>" + palavras.getString("Load Factor:") + " " + String.valueOf(I.getTaxaOcupacao());
            }
            break;
        }
        if (imprimeNosConectados && I.getTipoIcone() != 2) {
            Texto = Texto + "<br>" + palavras.getString("Output Connection:");
            for (int i : listaSaida) {
                Texto = Texto + "<br>" + String.valueOf(i);
            }
            Texto = Texto + "<br>" + palavras.getString("Input Connection:");
            for (int i : listaEntrada) {
                Texto = Texto + "<br>" + String.valueOf(i);
            }
        }
        if (imprimeNosConectados && I.getTipoIcone() == 2) {
            Texto = Texto + "<br>" + palavras.getString("Source Node:") + " " + String.valueOf(I.getNoOrigem());
            Texto = Texto + "<br>" + palavras.getString("Destination Node:") + " " + String.valueOf(I.getNoDestino());
        }
        if (imprimeNosIndiretos && I.getTipoIcone() != 2) {
            listaEntrada = I.getObjetoNosIndiretosEntrada();
            listaSaida = I.getObjetoNosIndiretosSaida();
            Texto = Texto + "<br>" + palavras.getString("Output Nodes Indirectly Connected:");
            for (int i : listaSaida) {
                Texto = Texto + "<br>" + String.valueOf(i);
            }
            Texto = Texto + "<br>" + palavras.getString("Input Nodes Indirectly Connected:");
            for (int i : listaEntrada) {
                Texto = Texto + "<br>" + String.valueOf(i);
            }
        }
        if (imprimeNosEscalonaveis && I.getTipoIcone() != 2) {
            listaSaida = I.getObjetoNosEscalonaveis();
            Texto = Texto + "<br>" + palavras.getString("Schedulable Nodes:");
            for (int i : listaSaida) {
                Texto = Texto + "<br>" + String.valueOf(i);
            }
        }
        if (I.getTipoIcone() == 1 && I.isMestre()) {
            List<Integer> escravos = I.getEscravos();
            Texto = Texto + "<br>" + palavras.getString("Slave Nodes:");
            for (int i : escravos) {
                Texto = Texto + "<br>" + String.valueOf(i);
            }
        }
        Texto += "</html>";
        janelaPrincipal.setSelectedIcon(I, Texto);
    }

    /**
     * Transforma os icones da area de desenho em um Document xml dom
     * @param descricao
     */
    public Document getDadosASalvar() {
        Document descricao = ispd.arquivo.IconicoXML.novoDocumento();
        Element system = descricao.createElement("system");
        system.setAttribute("version", "1");
        descricao.appendChild(system);
        for (String user : usuarios) {
            Element owner = descricao.createElement("owner");
            owner.setAttribute("id", user);
            system.appendChild(owner);
        }
        for (Icone I : icones) {
            Element aux = null;
            Element posicao = descricao.createElement("position");
            posicao.setAttribute("x", Integer.toString(I.getNumX()));
            posicao.setAttribute("y", Integer.toString(I.getNumY()));
            Element icon_id = descricao.createElement("icon_id");
            icon_id.setAttribute("global", Integer.toString(I.getIdGlobal()));
            icon_id.setAttribute("local", Integer.toString(I.getIdLocal()));
            switch (I.getTipoIcone()) {
                case Icone.MACHINE:
                    aux = descricao.createElement("machine");
                    aux.setAttribute("power", Double.toString(I.getPoderComputacional()));
                    aux.setAttribute("load", Double.toString(I.getTaxaOcupacao()));
                    aux.setAttribute("owner", I.getProprietario());
                    if (I.isMestre()) {
                        //preenche escravos
                        Element master = descricao.createElement("master");
                        master.setAttribute("scheduler", I.getAlgoritmo());
                        for (Integer escravo : I.getEscravos()) {
                            Element slave = descricao.createElement("slave");
                            slave.setAttribute("id", escravo.toString());
                            master.appendChild(slave);
                        }
                        aux.appendChild(master);
                    }
                    break;
                case Icone.NETWORK:
                    aux = descricao.createElement("link");
                    aux.setAttribute("bandwidth", Double.toString(I.getBanda()));
                    aux.setAttribute("load", Double.toString(I.getTaxaOcupacao()));
                    aux.setAttribute("latency", Double.toString(I.getLatencia()));
                    Element connect = descricao.createElement("connect");
                    connect.setAttribute("origination", Integer.toString(I.getNoOrigem()));
                    connect.setAttribute("destination", Integer.toString(I.getNoDestino()));
                    aux.appendChild(connect);
                    aux.appendChild(posicao);
                    posicao = descricao.createElement("position");
                    posicao.setAttribute("x", Integer.toString(I.getNumPreX()));
                    posicao.setAttribute("y", Integer.toString(I.getNumPreY()));
                    break;
                case Icone.CLUSTER:
                    aux = descricao.createElement("cluster");
                    aux.setAttribute("nodes", Integer.toString(I.getNumeroEscravos()));
                    aux.setAttribute("power", Double.toString(I.getPoderComputacional()));
                    aux.setAttribute("bandwidth", Double.toString(I.getBanda()));
                    aux.setAttribute("latency", Double.toString(I.getLatencia()));
                    aux.setAttribute("scheduler", I.getAlgoritmo());
                    aux.setAttribute("owner", I.getProprietario());
                    aux.setAttribute("master", I.isMestre().toString());
                    break;
                case Icone.INTERNET:
                    aux = descricao.createElement("internet");
                    aux.setAttribute("bandwidth", Double.toString(I.getBanda()));
                    aux.setAttribute("load", Double.toString(I.getTaxaOcupacao()));
                    aux.setAttribute("latency", Double.toString(I.getLatencia()));
                    break;
            }
            if (aux != null) {
                aux.setAttribute("id", I.getNome());
                aux.appendChild(posicao);
                aux.appendChild(icon_id);
                system.appendChild(aux);
            }
        }
        //configurar carga
        if (cargasConfiguracao != null) {
            Element load = descricao.createElement("load");
            if (cargasConfiguracao.getTipo() == GerarCarga.RANDOM) {
                CargaRandom random = (CargaRandom) cargasConfiguracao;
                Element xmlRandom = descricao.createElement("random");
                xmlRandom.setAttribute("tasks", random.getNumeroTarefas().toString());
                xmlRandom.setAttribute("time_arrival", random.getTimeToArrival().toString());
                Element size = descricao.createElement("size");
                size.setAttribute("type", "computing");
                size.setAttribute("maximum", random.getMaxComputacao().toString());
                size.setAttribute("average", random.getAverageComputacao().toString());
                size.setAttribute("minimum", random.getMinComputacao().toString());
                size.setAttribute("probability", random.getProbabilityComputacao().toString());
                xmlRandom.appendChild(size);
                size = descricao.createElement("size");
                size.setAttribute("type", "communication");
                size.setAttribute("maximum", random.getMaxComunicacao().toString());
                size.setAttribute("average", random.getAverageComunicacao().toString());
                size.setAttribute("minimum", random.getMinComunicacao().toString());
                size.setAttribute("probability", random.getProbabilityComunicacao().toString());
                xmlRandom.appendChild(size);
                load.appendChild(xmlRandom);
            } else if (cargasConfiguracao.getTipo() == GerarCarga.FORNODE) {
                CargaForNode cargaNo = (CargaForNode) cargasConfiguracao;
                List<CargaTaskNode> listaCargas = cargaNo.getConfiguracaoNo();
                for (int i = 0; i < listaCargas.size(); i++) {
                    Element xmlNode = descricao.createElement("node");
                    xmlNode.setAttribute("application", listaCargas.get(i).getAplicacao());
                    xmlNode.setAttribute("owner", listaCargas.get(i).getProprietario());
                    xmlNode.setAttribute("id_master", listaCargas.get(i).getEscalonador());
                    xmlNode.setAttribute("tasks", listaCargas.get(i).getNumeroTarefas().toString());
                    Element size = descricao.createElement("size");
                    size.setAttribute("type", "computing");
                    size.setAttribute("maximum", listaCargas.get(i).getMaxComputacao().toString());
                    size.setAttribute("minimum", listaCargas.get(i).getMinComputacao().toString());
                    xmlNode.appendChild(size);
                    size = descricao.createElement("size");
                    size.setAttribute("type", "communication");
                    size.setAttribute("maximum", listaCargas.get(i).getMaxComunicacao().toString());
                    size.setAttribute("minimum", listaCargas.get(i).getMinComunicacao().toString());
                    xmlNode.appendChild(size);
                    load.appendChild(xmlNode);
                }
            }
            system.appendChild(load);
        }
        return descricao;
    }

    /**
     * Carrega a estrutura da arvore contida no Document para os icones da area de desenho
     * @param descricao carregado a partir de um arquivo .imsx
     */
    public void setDadosSalvos(Document descricao) {

        NodeList owners = descricao.getElementsByTagName("owner");
        NodeList maquinas = descricao.getElementsByTagName("machine");
        NodeList clusters = descricao.getElementsByTagName("cluster");
        NodeList internet = descricao.getElementsByTagName("internet");
        NodeList links = descricao.getElementsByTagName("link");
        NodeList cargas = descricao.getElementsByTagName("load");

        this.numIcones = maquinas.getLength() + clusters.getLength() + internet.getLength() + links.getLength();
        this.numVertices = maquinas.getLength() + clusters.getLength() + internet.getLength();
        this.numArestas = links.getLength();

        //Realiza leitura dos usuários/proprietários do modelo
        for (int i = 0; i < owners.getLength(); i++) {
            Element owner = (Element) owners.item(i);
            usuarios.add(owner.getAttribute("id"));
        }
        //Realiza leitura dos icones de máquina
        for (int i = 0; i < maquinas.getLength(); i++) {
            Element maquina = (Element) maquinas.item(i);
            Element pos = (Element) maquina.getElementsByTagName("position").item(0);
            int x = Integer.parseInt(pos.getAttribute("x"));
            int y = Integer.parseInt(pos.getAttribute("y"));
            Element id = (Element) maquina.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            int local = Integer.parseInt(id.getAttribute("local"));
            Icone I = new Icone(x, y, 0, 0, Icone.MACHINE, local, global);
            icones.add(I);
            I.setNome(maquina.getAttribute("id"));
            ValidaValores.addNomeIcone(I.getNome());
            I.setPoderComputacional(Double.parseDouble(maquina.getAttribute("power")));
            I.setTaxaOcupacao(Double.parseDouble(maquina.getAttribute("load")));
            I.setProprietario(maquina.getAttribute("owner"));
            if (maquina.getElementsByTagName("master").getLength() > 0) {
                Element master = (Element) maquina.getElementsByTagName("master").item(0);
                I.setAlgoritmo(master.getAttribute("scheduler"));
                I.setMestre(true);
                NodeList slaves = master.getElementsByTagName("slave");
                List<Integer> escravos = new ArrayList<Integer>(slaves.getLength());
                for (int j = 0; j < slaves.getLength(); j++) {
                    Element slave = (Element) slaves.item(j);
                    escravos.add(Integer.parseInt(slave.getAttribute("id")));
                }
                I.setEscravos(escravos);
            }
        }
        //Realiza leitura dos icones de cluster
        for (int i = 0; i < clusters.getLength(); i++) {
            Element cluster = (Element) clusters.item(i);
            Element pos = (Element) cluster.getElementsByTagName("position").item(0);
            int x = Integer.parseInt(pos.getAttribute("x"));
            int y = Integer.parseInt(pos.getAttribute("y"));
            Element id = (Element) cluster.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            int local = Integer.parseInt(id.getAttribute("local"));
            Icone I = new Icone(x, y, 0, 0, Icone.CLUSTER, local, global);
            icones.add(I);
            I.setNome(cluster.getAttribute("id"));
            ValidaValores.addNomeIcone(I.getNome());
            I.setNumeroEscravos(Integer.parseInt(cluster.getAttribute("nodes")));
            I.setPoderComputacional(Double.parseDouble(cluster.getAttribute("power")));
            I.setBanda(Double.parseDouble(cluster.getAttribute("bandwidth")));
            I.setLatencia(Double.parseDouble(cluster.getAttribute("latency")));
            I.setAlgoritmo(cluster.getAttribute("scheduler"));
            I.setProprietario(cluster.getAttribute("owner"));
            I.setMestre(Boolean.parseBoolean(cluster.getAttribute("master")));
        }
        //Realiza leitura dos icones de internet
        for (int i = 0; i < internet.getLength(); i++) {
            Element inet = (Element) internet.item(i);
            Element pos = (Element) inet.getElementsByTagName("position").item(0);
            int x = Integer.parseInt(pos.getAttribute("x"));
            int y = Integer.parseInt(pos.getAttribute("y"));
            Element id = (Element) inet.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            int local = Integer.parseInt(id.getAttribute("local"));
            Icone I = new Icone(x, y, 0, 0, Icone.INTERNET, local, global);
            icones.add(I);
            I.setNome(inet.getAttribute("id"));
            ValidaValores.addNomeIcone(I.getNome());
            I.setBanda(Double.parseDouble(inet.getAttribute("bandwidth")));
            I.setTaxaOcupacao(Double.parseDouble(inet.getAttribute("load")));
            I.setLatencia(Double.parseDouble(inet.getAttribute("latency")));
        }
        //Realiza leitura dos icones de rede
        for (int i = 0; i < links.getLength(); i++) {
            Element link = (Element) links.item(i);
            Element id = (Element) link.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            int local = Integer.parseInt(id.getAttribute("local"));
            Element pos = (Element) link.getElementsByTagName("position").item(0);
            int x = Integer.parseInt(pos.getAttribute("x"));
            int y = Integer.parseInt(pos.getAttribute("y"));
            pos = (Element) link.getElementsByTagName("position").item(1);
            int px = Integer.parseInt(pos.getAttribute("x"));
            int py = Integer.parseInt(pos.getAttribute("y"));
            Icone I = new Icone(x, y, px, py, Icone.NETWORK, local, global);
            icones.add(I);
            I.setNome(link.getAttribute("id"));
            ValidaValores.addNomeIcone(I.getNome());
            I.setBanda(Double.parseDouble(link.getAttribute("bandwidth")));
            I.setTaxaOcupacao(Double.parseDouble(link.getAttribute("load")));
            I.setLatencia(Double.parseDouble(link.getAttribute("latency")));
            Element connect = (Element) link.getElementsByTagName("connect").item(0);
            I.setNoOrigem(Integer.parseInt(connect.getAttribute("origination")));
            I.setNoDestino(Integer.parseInt(connect.getAttribute("destination")));
            //adiciona entrada e saida desta conexão
            for (Icone Ico : icones) {
                if (Ico.getIdGlobal() == I.getNoOrigem()) {
                    Ico.addIdConexaoSaida(I.getNoDestino());
                    break;
                }
            }
            for (Icone Ico : icones) {
                if (Ico.getIdGlobal() == I.getNoDestino()) {
                    Ico.addIdConexaoEntrada(I.getNoOrigem());
                    break;
                }
            }
        }
        //Realiza leitura da configuração de carga do modelo
        if (cargas.getLength() != 0) {
            Element cargaAux = (Element) cargas.item(0);
            cargas = cargaAux.getElementsByTagName("random");
            if (cargas.getLength() != 0) {
                Element carga = (Element) cargas.item(0);
                int numeroTarefas = Integer.parseInt(carga.getAttribute("tasks"));
                int timeOfArrival = Integer.parseInt(carga.getAttribute("time_arrival"));
                int minComputacao = 0;
                int maxComputacao = 0;
                int AverageComputacao = 0;
                double ProbabilityComputacao = 0;
                int minComunicacao = 0;
                int maxComunicacao = 0;
                int AverageComunicacao = 0;
                double ProbabilityComunicacao = 0;
                NodeList size = carga.getElementsByTagName("size");
                for (int i = 0; i < size.getLength(); i++) {
                    Element size1 = (Element) size.item(i);
                    if (size1.getAttribute("type").equals("computing")) {
                        minComputacao = Integer.parseInt(size1.getAttribute("minimum"));
                        maxComputacao = Integer.parseInt(size1.getAttribute("maximum"));
                        AverageComputacao = Integer.parseInt(size1.getAttribute("average"));
                        ProbabilityComputacao = Double.parseDouble(size1.getAttribute("probability"));
                    } else if (size1.getAttribute("type").equals("communication")) {
                        minComunicacao = Integer.parseInt(size1.getAttribute("minimum"));
                        maxComunicacao = Integer.parseInt(size1.getAttribute("maximum"));
                        AverageComunicacao = Integer.parseInt(size1.getAttribute("average"));
                        ProbabilityComunicacao = Double.parseDouble(size1.getAttribute("probability"));
                    }
                }
                cargasConfiguracao = new CargaRandom(numeroTarefas, minComputacao, maxComputacao, AverageComputacao, ProbabilityComputacao, minComunicacao, maxComunicacao, AverageComunicacao, ProbabilityComunicacao, timeOfArrival);
            }
            cargas = cargaAux.getElementsByTagName("node");
            if (cargas.getLength() != 0) {
                List<CargaTaskNode> tarefasDoNo = new ArrayList<CargaTaskNode>();
                for (int i = 0; i < cargas.getLength(); i++) {
                    Element carga = (Element) cargas.item(i);
                    String aplicacao = carga.getAttribute("application");
                    String proprietario = carga.getAttribute("owner");
                    String escalonador = carga.getAttribute("id_master");
                    int numeroTarefas = Integer.parseInt(carga.getAttribute("tasks"));
                    double minComputacao = 0;
                    double maxComputacao = 0;
                    double minComunicacao = 0;
                    double maxComunicacao = 0;
                    NodeList size = carga.getElementsByTagName("size");
                    for (int j = 0; j < size.getLength(); j++) {
                        Element size1 = (Element) size.item(j);
                        if (size1.getAttribute("type").equals("computing")) {
                            minComputacao = Double.parseDouble(size1.getAttribute("minimum"));
                            maxComputacao = Double.parseDouble(size1.getAttribute("maximum"));
                        } else if (size1.getAttribute("type").equals("communication")) {
                            minComunicacao = Double.parseDouble(size1.getAttribute("minimum"));
                            maxComunicacao = Double.parseDouble(size1.getAttribute("maximum"));
                        }
                    }
                    CargaTaskNode item = new CargaTaskNode(aplicacao, proprietario, escalonador, numeroTarefas, maxComputacao, minComputacao, maxComunicacao, minComunicacao);
                    tarefasDoNo.add(item);
                }
                cargasConfiguracao = new CargaForNode(tarefasDoNo);
            }
        }
        atualizaNosIndiretos();
        repaint();
    }

    public BufferedImage createImage() {
        int maiorx = 0;
        int maiory = 0;

        for (Icone I : icones) {
            if (I.getNumX() > maiorx) {
                maiorx = I.getNumX();
            }
            if (I.getNumY() > maiory) {
                maiory = I.getNumY();
            }
        }

        BufferedImage image = new BufferedImage(maiorx + 50, maiory + 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D gc = (Graphics2D) image.getGraphics();
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gc.setColor(new Color(255, 255, 255));
        gc.fillRect(0, 0, maiorx + 50, maiory + 50);
        gc.setColor(new Color(220, 220, 220));

        if (metric) {
            units = (int) ((double) INCH / (double) 2.54);
        } else {
            units = (int) INCH / 2;
        }
        if (gridOn) {
            for (int _w = 0; _w
                    <= maiorx + 50; _w += units) {
                gc.drawLine(_w, 0, _w, maiory + 50);
            }
            for (int _h = 0; _h
                    <= maiory + 50; _h += units) {
                gc.drawLine(0, _h, maiorx + 50, _h);
            }
        }

        // Desenhamos todos os icones
        for (Icone I : icones) {
            if (I.getTipoIcone() == 2) {
                I.draw(gc);
            }
        }
        for (Icone I : icones) {
            if (I.getTipoIcone() != 2) {
                I.draw(gc);
            }
        }
        return image;
    }

    /**
     * Metodo publico para efetuar a copia dos valores de uma conexão de rede
     * especifica informada pelo usuário para as demais conexões de rede.
     */
    public void matchNetwork() {
        if (iconeAuxiliarMatchRede.getTipoIcone() == 2) {
            double banda = 0.0, taxa = 0.0, latencia = 0.0;
            int intMatch = iconeAuxiliarMatchRede.getIdGlobal();
            banda = iconeAuxiliarMatchRede.getBanda();
            taxa = iconeAuxiliarMatchRede.getTaxaOcupacao();
            latencia = iconeAuxiliarMatchRede.getLatencia();

            for (Icone I : icones) {
                if (I.getTipoIcone() == 2 && I.getIdGlobal() != intMatch) {
                    I.setNome("lan" + I.getIdGlobal());
                    I.setBanda(banda);
                    I.setTaxaOcupacao(taxa);
                    I.setLatencia(latencia);
                    ValidaValores.addNomeIcone(I.getNome());
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, palavras.getString("Please select a network icon"), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
        }
    }

    public void setIdioma(ResourceBundle palavras) {
        this.palavras = palavras;
        this.initTexts();
    }

    void setDadosSalvos(DescreveSistema descricao) {
        List<DescreveIcone> lista = new ArrayList<DescreveIcone>();
        ValidaValores.setListaNos(descricao.getListaNosLista());
        this.numIcones = descricao.getNumIcones();
        this.numVertices = descricao.getNumVertices();
        this.numArestas = descricao.getNumArestas();
        if (descricao.getCargasTipoConfiguracao() == GerarCarga.RANDOM) {
            this.cargasConfiguracao = CargaRandom.newGerarCarga(descricao.getCargasConfiguracao());
        } else if (descricao.getCargasTipoConfiguracao() == GerarCarga.FORNODE) {
            this.cargasConfiguracao = CargaForNode.newGerarCarga(descricao.getCargasConfiguracao());
        }

        lista = descricao.getIconeLista();
        for (DescreveIcone Ico : lista) {
            Icone I = new Icone(Ico.getX(), Ico.getY(), Ico.getPreX(), Ico.getPreY(), Ico.getTipoIcone(), Ico.getIdLocal(), Ico.getIdGlobal());
            icones.add(I);
            I.setNome(Ico.getNome());
            I.setPoderComputacional(Ico.getPoderComputacional());
            I.setTaxaOcupacao(Ico.getTaxaOcupacao());
            I.setLatencia(Ico.getLatencia());
            I.setBanda(Ico.getBanda());
            I.setMestre(Ico.getMestre());
            I.setAlgoritmo(Ico.getAlgoritmoEscalonamento());
            I.setEscravos(Ico.getEscravos());
            I.setConexaoEntrada(Ico.getConexaoEntrada());
            I.setConexaoSaida(Ico.getConexaoSaida());
            I.setNoDestino(Ico.getNoDestino());
            I.setNoOrigem(Ico.getNoOrigem());
            I.setNumeroEscravos(Ico.getNumeroEscravos());
        }
        atualizaNosIndiretos();
        repaint();
    }

    public Vector<String> getNosEscalonadores() {
        Vector<String> maquinas = new Vector<String>();

        for (Icone I : icones) {
            if (I.getTipoIcone() == 1 && I.isMestre()) {
                maquinas.add(I.getNome());
            }
            if (I.getTipoIcone() == 3 && I.isMestre()) {
                maquinas.add(I.getNome());
            }
        }
        return maquinas;
    }

    public RedeDeFilas getRedeDeFilas() {
        List<CS_Processamento> mestres = new ArrayList<CS_Processamento>();
        List<Integer> mestresNome = new ArrayList<Integer>();
        List<CS_Switch> clusters = new ArrayList<CS_Switch>();
        List<Integer> clustersNome = new ArrayList<Integer>();
        List<CS_Maquina> maqs = new ArrayList<CS_Maquina>();
        List<Integer> maqsNome = new ArrayList<Integer>();
        List<CS_Comunicacao> links = new ArrayList<CS_Comunicacao>();
        List<CS_Internet> nets = new ArrayList<CS_Internet>();
        List<Integer> netsNome = new ArrayList<Integer>();
        //cria lista de usuarios e o poder computacional cedido por cada um
        List<String> proprietarios = new ArrayList<String>();
        List<Double> poderComp = new ArrayList<Double>();
        for (Icone icone : getIcones()) {
            if(icone.getTipoIcone() == Icone.MACHINE){
                if(proprietarios.contains(icone.getProprietario())){
                    int index = proprietarios.indexOf(icone.getProprietario());
                    poderComp.set(index, poderComp.get(index) + icone.getPoderComputacional());
                }else{
                    proprietarios.add(icone.getProprietario());
                    poderComp.add(icone.getPoderComputacional());
                }
            }else if(icone.getTipoIcone() == Icone.CLUSTER && !icone.isMestre()){
                if(proprietarios.contains(icone.getProprietario())){
                    int index = proprietarios.indexOf(icone.getProprietario());
                    poderComp.set(index, poderComp.get(index) + (icone.getPoderComputacional() * icone.getNumeroEscravos()));
                }else{
                    proprietarios.add(icone.getProprietario());
                    poderComp.add(icone.getPoderComputacional() * icone.getNumeroEscravos());
                }
            }else if(icone.getTipoIcone() == Icone.CLUSTER && icone.isMestre()){
                if(proprietarios.contains(icone.getProprietario())){
                    int index = proprietarios.indexOf(icone.getProprietario());
                    poderComp.set(index, poderComp.get(index) + (icone.getPoderComputacional() * icone.getNumeroEscravos()) + icone.getPoderComputacional());
                }else{
                    proprietarios.add(icone.getProprietario());
                    poderComp.add((icone.getPoderComputacional() * icone.getNumeroEscravos()) + icone.getPoderComputacional());
                }
            }
        }
        //cria maquinas, mestres, internets e mestres dos clusters
        for (Icone icone : getIcones()) {
            switch (icone.getTipoIcone()) {
                case Icone.MACHINE:
                    if (icone.isMestre()) {
                        CS_Mestre mestre = new CS_Mestre(
                                icone.getNome(),
                                icone.getProprietario(),
                                icone.getPoderComputacional(),
                                icone.getTaxaOcupacao(),
                                icone.getAlgoritmo()/*Escalonador*/);
                        mestres.add(mestre);
                        mestresNome.add(icone.getIdGlobal());
                    } else {
                        CS_Maquina maq = new CS_Maquina(
                                icone.getNome(),
                                icone.getProprietario(),
                                icone.getPoderComputacional(),
                                1/*num processadores*/,
                                icone.getTaxaOcupacao());
                        maqs.add(maq);
                        maqsNome.add(icone.getIdGlobal());
                    }
                    break;
                case Icone.CLUSTER:
                    if (icone.isMestre()) {
                        CS_Mestre clust = new CS_Mestre(
                                icone.getNome(),
                                icone.getProprietario(),
                                icone.getPoderComputacional(),
                                icone.getTaxaOcupacao(),
                                icone.getAlgoritmo()/*Escalonador*/);
                        mestres.add(clust);
                        mestresNome.add(icone.getIdGlobal());
                    } else {
                        CS_Switch Switch = new CS_Switch(
                                icone.getNome(),
                                icone.getBanda(),
                                icone.getTaxaOcupacao(),
                                icone.getLatencia());
                        clusters.add(Switch);
                        clustersNome.add(icone.getIdGlobal());
                    }
                    break;
                case Icone.INTERNET:
                    CS_Internet net = new CS_Internet(
                            icone.getNome(),
                            icone.getBanda(),
                            icone.getTaxaOcupacao(),
                            icone.getLatencia());
                    nets.add(net);
                    netsNome.add(icone.getIdGlobal());
                    break;
            }
        }
        //cria os links e realiza a conexão entre os recursos
        for (Icone icone : getIcones()) {
            if (icone.getTipoIcone() == Icone.NETWORK) {
                CS_Link link = new CS_Link(
                        icone.getNome(),
                        icone.getBanda(),
                        icone.getTaxaOcupacao(),
                        icone.getLatencia());
                links.add(link);
                if (mestresNome.contains(icone.getNoDestino())) {
                    int index = mestresNome.indexOf(icone.getNoDestino());
                    CS_Mestre mestre = (CS_Mestre) mestres.get(index);
                    link.setConexoesSaida(mestre);
                    mestre.addConexoesEntrada(link);
                } else if (maqsNome.contains(icone.getNoDestino())) {
                    int index = maqsNome.indexOf(icone.getNoDestino());
                    CS_Maquina maq = (CS_Maquina) maqs.get(index);
                    link.setConexoesSaida(maq);
                    maq.addConexoesEntrada(link);
                } else if (netsNome.contains(icone.getNoDestino())) {
                    int index = netsNome.indexOf(icone.getNoDestino());
                    CS_Internet net = nets.get(index);
                    link.setConexoesSaida(net);
                    net.addConexoesEntrada(link);
                } else if (clustersNome.contains(icone.getNoDestino())) {
                    int index = clustersNome.indexOf(icone.getNoDestino());
                    CS_Switch swt = clusters.get(index);
                    link.setConexoesSaida(swt);
                    swt.addConexoesEntrada(link);
                }
                if (mestresNome.contains(icone.getNoOrigem())) {
                    int index = mestresNome.indexOf(icone.getNoOrigem());
                    CS_Mestre mestre = (CS_Mestre) mestres.get(index);
                    link.setConexoesEntrada(mestre);
                    mestre.addConexoesSaida(link);
                } else if (maqsNome.contains(icone.getNoOrigem())) {
                    int index = maqsNome.indexOf(icone.getNoOrigem());
                    CS_Maquina maq = (CS_Maquina) maqs.get(index);
                    link.setConexoesEntrada(maq);
                    maq.addConexoesSaida(link);
                } else if (netsNome.contains(icone.getNoOrigem())) {
                    int index = netsNome.indexOf(icone.getNoOrigem());
                    CS_Internet net = nets.get(index);
                    link.setConexoesEntrada(net);
                    net.addConexoesSaida(link);
                } else if (clustersNome.contains(icone.getNoOrigem())) {
                    int index = clustersNome.indexOf(icone.getNoOrigem());
                    CS_Switch swt = clusters.get(index);
                    link.setConexoesEntrada(swt);
                    swt.addConexoesSaida(link);
                }
            }
        }
        //adiciona os escravos aos mestres
        for (Icone icone : getIcones()) {
            if (icone.isMestre() && icone.getTipoIcone() != Icone.CLUSTER) {
                for (Integer escravo : icone.getEscravos()) {
                    if (maqsNome.contains(escravo)) {
                        int index = maqsNome.indexOf(escravo);
                        CS_Processamento maq = maqs.get(index);
                        index = mestresNome.indexOf(icone.getIdGlobal());
                        CS_Mestre mest = (CS_Mestre) mestres.get(index);
                        mest.addEscravo(maq);
                        if (maq instanceof CS_Maquina) {
                            CS_Maquina maqTemp = (CS_Maquina) maq;
                            maqTemp.addMestre(mest);
                        }
                    } else if (mestresNome.contains(escravo)) {
                        int index = mestresNome.indexOf(escravo);
                        CS_Processamento maq = mestres.get(index);
                        index = mestresNome.indexOf(icone.getIdGlobal());
                        CS_Mestre mest = (CS_Mestre) mestres.get(index);
                        mest.addEscravo(maq);
                    }
                }
            }
        }
        //cria os escravos dos clusters e realiza a conexão
        for (Icone icone : getIcones()) {
            if (icone.isMestre() && icone.getTipoIcone() == Icone.CLUSTER) {
                int index = mestresNome.indexOf(icone.getIdGlobal());
                CS_Mestre mestreCluster = (CS_Mestre) mestres.get(index);
                CS_Switch Switch = new CS_Switch(
                        icone.getNome(),
                        icone.getBanda(),
                        icone.getTaxaOcupacao(),
                        icone.getLatencia());
                links.add(Switch);
                mestreCluster.addConexoesEntrada(Switch);
                mestreCluster.addConexoesSaida(Switch);
                Switch.addConexoesEntrada(mestreCluster);
                Switch.addConexoesSaida(mestreCluster);
                for (int i = 0; i < icone.getNumeroEscravos(); i++) {
                    CS_Maquina maq = new CS_Maquina(
                            icone.getNome(),
                            icone.getProprietario(),
                            icone.getPoderComputacional(),
                            1/*numero de processadores*/,
                            icone.getTaxaOcupacao());
                    maq.addConexoesSaida(Switch);
                    maq.addConexoesEntrada(Switch);
                    Switch.addConexoesEntrada(maq);
                    Switch.addConexoesSaida(maq);
                    maq.addMestre(mestreCluster);
                    mestreCluster.addEscravo(maq);
                    maqs.add(maq);
                }
            } else if (icone.getTipoIcone() == Icone.CLUSTER) {
                List<CS_Maquina> maqTemp = new ArrayList<CS_Maquina>();
                int index = clustersNome.indexOf(icone.getIdGlobal());
                CS_Switch Switch = clusters.get(index);
                links.add(Switch);
                for (int i = 0; i < icone.getNumeroEscravos(); i++) {
                    CS_Maquina maq = new CS_Maquina(
                            icone.getNome(),
                            icone.getProprietario(),
                            icone.getPoderComputacional(),
                            1/*numero de processadores*/,
                            icone.getTaxaOcupacao());
                    maq.addConexoesSaida(Switch);
                    maq.addConexoesEntrada(Switch);
                    Switch.addConexoesEntrada(maq);
                    Switch.addConexoesSaida(maq);
                    maqTemp.add(maq);
                    maqs.add(maq);
                }
                //adiciona os escravos aos mestres
                Icone[] mestresArray = new Icone[getIcones().size()];
                getIcones().toArray(mestresArray);
                for (int i = 0; i < mestresArray.length; i++) {
                    if (mestresArray[i].isMestre() && mestresArray[i].getTipoIcone() != Icone.CLUSTER) {
                        if (mestresArray[i].getEscravos().contains(icone.getIdGlobal())) {
                            index = mestresNome.indexOf(mestresArray[i].getIdGlobal());
                            CS_Mestre mest = (CS_Mestre) mestres.get(index);
                            for (CS_Maquina maq : maqTemp) {
                                mest.addEscravo(maq);
                                maq.addMestre(mest);
                            }
                        }
                    }
                }
            }
        }
        //cria as métricas de usuarios para cada mestre
        for (CS_Processamento mestre : mestres) {
            CS_Mestre mst = (CS_Mestre) mestre;
            MetricasUsuarios mu = new MetricasUsuarios();
            mu.addAllUsuarios(proprietarios, poderComp);
            mst.getEscalonador().setMetricaUsuarios(mu);
        }
        RedeDeFilas rdf = new RedeDeFilas(mestres, maqs, links, nets);
        //cria as métricas de usuarios globais da rede de filas
        MetricasUsuarios mu = new MetricasUsuarios();
        mu.addAllUsuarios(proprietarios, poderComp);
        rdf.setMetricasUsuarios(mu);
        return rdf;
    }
}
