/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.arquivo.xml;

import ispd.ValidaValores;
import ispd.gui.iconico.Aresta;
import ispd.gui.iconico.grade.Cluster;
import ispd.gui.iconico.grade.Internet;
import ispd.gui.iconico.grade.ItemGrade;
import ispd.gui.iconico.grade.Link;
import ispd.gui.iconico.grade.Machine;
import ispd.motor.carga.CargaList;
import ispd.motor.carga.CargaRandom;
import ispd.motor.carga.CargaForNode;
import ispd.motor.carga.CargaTrace;
import ispd.motor.carga.GerarCarga;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_Link;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.Vertice;
import ispd.motor.metricas.MetricasUsuarios;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Realiza leitura do arquivo xml do modelo icônico
 *
 * @author denison_usuario
 */
public class IconicoXML {

    private Document descricao;
    private Element system;
    private Element load;

    public IconicoXML() {
        descricao = ManipuladorXML.novoDocumento();
        system = descricao.createElement("system");
        system.setAttribute("version", "1");
        load = null;
        descricao.appendChild(system);
    }

    /**
     * Este método sobrescreve ou cria arquivo xml do modelo iconico
     */
    public static boolean escrever(Document documento, File arquivo) {
        return ManipuladorXML.escrever(documento, arquivo, "iSPD.dtd");
    }
    
    public static Document ler(File xmlFile) throws ParserConfigurationException, IOException, SAXException {
        return ManipuladorXML.ler(xmlFile, "iSPD.dtd");
    }

    public static void iSPDtoSimGrid(Document documento, File arquivo) {
        validarModelo(documento);

        NodeList docCarga = documento.getElementsByTagName("load");
        Integer numeroTarefas = 0;
        Double maxComputacao = 0.0;
        Double maxComunicacao = 0.0;
        //Realiza leitura da configuração de carga random
        if (docCarga.getLength() != 0) {
            Element cargaAux = (Element) docCarga.item(0);
            docCarga = cargaAux.getElementsByTagName("random");
            if (docCarga.getLength() != 0) {
                Element carga = (Element) docCarga.item(0);
                numeroTarefas = Integer.parseInt(carga.getAttribute("tasks"));
                NodeList size = carga.getElementsByTagName("size");
                for (int i = 0; i < size.getLength(); i++) {
                    Element size1 = (Element) size.item(i);
                    if (size1.getAttribute("type").equals("computing")) {
                        maxComputacao = Double.parseDouble(size1.getAttribute("maximum"));
                    } else if (size1.getAttribute("type").equals("communication")) {
                        maxComunicacao = Double.parseDouble(size1.getAttribute("maximum"));
                    }
                }
            } else {
                docCarga = cargaAux.getElementsByTagName("node");
            }
        }

        Document docApplication = ManipuladorXML.novoDocumento();
        Element tagApplication = docApplication.createElement("platform_description");
        tagApplication.setAttribute("version", "1");
        docApplication.appendChild(tagApplication);

        Document docPlataform = ManipuladorXML.novoDocumento();
        Element tagPlataform = docPlataform.createElement("platform_description");
        tagPlataform.setAttribute("version", "1");
        docPlataform.appendChild(tagPlataform);

        NodeList docmaquinas = documento.getElementsByTagName("machine");
        NodeList docCluster = documento.getElementsByTagName("cluster");
        NodeList doclinks = documento.getElementsByTagName("link");
        NodeList docNets = documento.getElementsByTagName("internet");

        int numMestre = 0;
        HashMap<Integer, String> maquinas = new HashMap<Integer, String>();
        HashMap<Integer, String> comutacao = new HashMap<Integer, String>();
        HashMap<Integer, Integer> link_origem = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> link_destino = new HashMap<Integer, Integer>();

        //Busca poder das máquinas
        for (int i = 0; i < docmaquinas.getLength(); i++) {
            Element maquina = (Element) docmaquinas.item(i);
            if (maquina.getElementsByTagName("master").getLength() > 0) {
                numMestre++;
            }
            Element id = (Element) maquina.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            maquinas.put(global, maquina.getAttribute("id"));
            Element cpu = docPlataform.createElement("cpu");
            cpu.setAttribute("name", maquina.getAttribute("id"));
            cpu.setAttribute("power", maquina.getAttribute("power"));
            tagPlataform.appendChild(cpu);
        }
        //Busca clusters
        for (int i = 0; i < docCluster.getLength(); i++) {
            Element cluster = (Element) docCluster.item(i);
            Element id = (Element) cluster.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            maquinas.put(global, cluster.getAttribute("id"));
            Element cpu = docPlataform.createElement("cpu");
            cpu.setAttribute("name", cluster.getAttribute("id"));
            cpu.setAttribute("power", cluster.getAttribute("power"));
            tagPlataform.appendChild(cpu);
        }
        //Busca busca banda das redes
        for (int i = 0; i < doclinks.getLength(); i++) {
            Element link = (Element) doclinks.item(i);
            Element network = docPlataform.createElement("network_link");
            network.setAttribute("name", link.getAttribute("id"));
            network.setAttribute("bandwidth", link.getAttribute("bandwidth"));
            network.setAttribute("latency", link.getAttribute("latency"));
            tagPlataform.appendChild(network);
            Element connect = (Element) link.getElementsByTagName("connect").item(0);
            //salva origem e destino do link
            Element id = (Element) link.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            comutacao.put(global, link.getAttribute("id"));
            link_origem.put(global, Integer.parseInt(connect.getAttribute("origination")));
            link_destino.put(global, Integer.parseInt(connect.getAttribute("destination")));
        }
        for (int i = 0; i < docNets.getLength(); i++) {
            Element link = (Element) docNets.item(i);
            Element network = docPlataform.createElement("network_link");
            network.setAttribute("name", link.getAttribute("id"));
            network.setAttribute("bandwidth", link.getAttribute("bandwidth"));
            network.setAttribute("latency", link.getAttribute("latency"));
            tagPlataform.appendChild(network);
            //salva elemento na lista com os elementos de comutação
            Element id = (Element) link.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            comutacao.put(global, link.getAttribute("id"));
        }
        //Escreve mestres
        boolean primeiro = true;
        for (int i = 0; i < docmaquinas.getLength(); i++) {
            Element maquina = (Element) docmaquinas.item(i);
            if (maquina.getElementsByTagName("master").getLength() > 0) {
                Element mestre = docApplication.createElement("process");
                String idMestre = maquina.getAttribute("id");
                mestre.setAttribute("host", idMestre);
                mestre.setAttribute("function", "master");
                //Adicionar tarefas
                if (docCarga.item(0).getNodeName().equals("random")) {
                    //Number of tasks
                    Element carga = docApplication.createElement("argument");
                    if (primeiro) {
                        primeiro = false;
                        Integer temp = (numeroTarefas / numMestre) + (numeroTarefas % numMestre);
                        carga.setAttribute("value", temp.toString());
                    } else {
                        Integer temp = (numeroTarefas / numMestre);
                        carga.setAttribute("value", temp.toString());
                    }
                    mestre.appendChild(carga);
                    //Max computation size of tasks
                    carga = docApplication.createElement("argument");
                    carga.setAttribute("value", maxComputacao.toString());
                    mestre.appendChild(carga);
                    //Max communication size of tasks
                    carga = docApplication.createElement("argument");
                    carga.setAttribute("value", maxComunicacao.toString());
                    mestre.appendChild(carga);
                } else if (docCarga.item(0).getNodeName().equals("node")) {
                    numeroTarefas = 0;
                    for (int j = 0; j < docCarga.getLength(); j++) {
                        Element carga = (Element) docCarga.item(j);
                        String escalonador = carga.getAttribute("id_master");
                        if (escalonador.equals(idMestre)) {
                            numeroTarefas += Integer.parseInt(carga.getAttribute("tasks"));
                            NodeList size = carga.getElementsByTagName("size");
                            for (int k = 0; k < size.getLength(); k++) {
                                Element size1 = (Element) size.item(k);
                                if (size1.getAttribute("type").equals("computing")) {
                                    double temp = Double.parseDouble(size1.getAttribute("maximum"));
                                    if (temp > maxComputacao) {
                                        maxComputacao = temp;
                                    }
                                } else if (size1.getAttribute("type").equals("communication")) {
                                    double temp = Double.parseDouble(size1.getAttribute("maximum"));
                                    if (temp > maxComunicacao) {
                                        maxComunicacao = temp;
                                    }
                                }
                            }
                        }
                    }
                    //Number of tasks
                    Element carga = docApplication.createElement("argument");
                    carga.setAttribute("value", numeroTarefas.toString());
                    mestre.appendChild(carga);
                    //Max computation size of tasks
                    carga = docApplication.createElement("argument");
                    carga.setAttribute("value", maxComputacao.toString());
                    mestre.appendChild(carga);
                    //Max communication size of tasks
                    carga = docApplication.createElement("argument");
                    carga.setAttribute("value", maxComunicacao.toString());
                    mestre.appendChild(carga);
                }
                //Adiciona escravos ao mestre
                NodeList slaves = maquina.getElementsByTagName("slave");
                for (int j = 0; j < slaves.getLength(); j++) {
                    Element escravo = docApplication.createElement("argument");
                    Element slave = (Element) slaves.item(j);
                    String idEscravo = maquinas.get(Integer.parseInt(slave.getAttribute("id")));
                    escravo.setAttribute("value", idEscravo);
                    mestre.appendChild(escravo);
                    //Define roteamento entre mestre e escravo
                    Element rota = docPlataform.createElement("route");
                    rota.setAttribute("src", idMestre);
                    rota.setAttribute("dst", idEscravo);
                    ArrayList<Element> caminho = caminho(idMestre, idEscravo, docPlataform, link_origem, link_destino, comutacao, maquinas, null);
                    if (caminho != null) {
                        for (Element element : caminho) {
                            rota.appendChild(element);
                        }
                    }
                    tagPlataform.appendChild(rota);
                    rota = docPlataform.createElement("route");
                    rota.setAttribute("src", idEscravo);
                    rota.setAttribute("dst", idMestre);
                    caminho = caminho(idEscravo, idMestre, docPlataform, link_origem, link_destino, comutacao, maquinas, null);
                    if (caminho != null) {
                        for (Element element : caminho) {
                            rota.appendChild(element);
                        }
                    }
                    tagPlataform.appendChild(rota);
                }
                tagApplication.appendChild(mestre);
            }
        }
        //Escreve escravos
        for (int i = 0; i < docmaquinas.getLength(); i++) {
            Element maquina = (Element) docmaquinas.item(i);
            if (maquina.getElementsByTagName("master").getLength() == 0) {
                Element escravo = docApplication.createElement("process");
                escravo.setAttribute("host", maquina.getAttribute("id"));
                escravo.setAttribute("function", "slave");
                tagApplication.appendChild(escravo);
            }
        }
        for (int i = 0; i < docCluster.getLength(); i++) {
            Element cluster = (Element) docCluster.item(i);
            Element escravo = docApplication.createElement("process");
            escravo.setAttribute("host", cluster.getAttribute("id"));
            escravo.setAttribute("function", "slave");
            Element maqs = docApplication.createElement("argument");
            maqs.setAttribute("value", cluster.getAttribute("nodes"));
            escravo.appendChild(maqs);
            tagApplication.appendChild(escravo);
        }
        String name = arquivo.getName();
        String diretorio = arquivo.getPath();
        ManipuladorXML.escrever(docPlataform, new File(arquivo.getParentFile(), "plataform_" + name), "surfxml.dtd");
        ManipuladorXML.escrever(docApplication, new File(arquivo.getParentFile(), "application_" + name), "surfxml.dtd");
    }

    /**
     * Verifica se modelo está completo
     */
    public static void validarModelo(Document documento) throws IllegalArgumentException {
        org.w3c.dom.NodeList owner = documento.getElementsByTagName("owner");
        org.w3c.dom.NodeList maquinas = documento.getElementsByTagName("machine");
        org.w3c.dom.NodeList clusters = documento.getElementsByTagName("cluster");
        org.w3c.dom.NodeList internet = documento.getElementsByTagName("internet");
        org.w3c.dom.NodeList links = documento.getElementsByTagName("link");
        org.w3c.dom.NodeList cargas = documento.getElementsByTagName("load");
        if (owner.getLength() == 0) {
            throw new IllegalArgumentException("The model has no users.");
        }
        if (maquinas.getLength() == 0 && clusters.getLength() == 0) {
            throw new IllegalArgumentException("The model has no icons.");
        }
        if (cargas.getLength() == 0) {
            throw new IllegalArgumentException("One or more  workloads have not been configured.");
        }
        boolean achou = false;
        int i = 0;
        while (!achou && i < maquinas.getLength()) {
            org.w3c.dom.Element maquina = (org.w3c.dom.Element) maquinas.item(i);
            if (maquina.getElementsByTagName("master").getLength() > 0) {
                achou = true;
            }
            i++;
        }
        if (!achou) {
            throw new IllegalArgumentException("One or more parameters have not been configured.");
        }
        //laço para validar cada elemento do modelo
        for (i = 0; i < maquinas.getLength(); i++) {
            maquinas.item(i).getAttributes();
        }
    }

    /**
     *
     * @param modelo Objeto obtido a partir do xml com a grade computacional
     * modelada
     * @return Rede de filas simulável contruida conforme modelo
     */
    public static RedeDeFilas newRedeDeFilas(Document modelo) {
        NodeList docmaquinas = modelo.getElementsByTagName("machine");
        NodeList docclusters = modelo.getElementsByTagName("cluster");
        NodeList docinternet = modelo.getElementsByTagName("internet");
        NodeList doclinks = modelo.getElementsByTagName("link");
        NodeList owners = modelo.getElementsByTagName("owner");

        HashMap<Integer, CentroServico> centroDeServicos = new HashMap<Integer, CentroServico>();
        HashMap<CentroServico, List<CS_Maquina>> escravosCluster = new HashMap<CentroServico, List<CS_Maquina>>();
        List<CS_Processamento> mestres = new ArrayList<CS_Processamento>();
        List<CS_Maquina> maqs = new ArrayList<CS_Maquina>();
        List<CS_Comunicacao> links = new ArrayList<CS_Comunicacao>();
        List<CS_Internet> nets = new ArrayList<CS_Internet>();
        //cria lista de usuarios e o poder computacional cedido por cada um
        HashMap<String, Double> usuarios = new HashMap<String, Double>();
        for (int i = 0; i < owners.getLength(); i++) {
            Element owner = (Element) owners.item(i);
            usuarios.put(owner.getAttribute("id"), 0.0);
        }
        //cria maquinas, mestres, internets e mestres dos clusters
        //Realiza leitura dos icones de máquina
        for (int i = 0; i < docmaquinas.getLength(); i++) {
            Element maquina = (Element) docmaquinas.item(i);
            Element id = (Element) maquina.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            if (maquina.getElementsByTagName("master").getLength() > 0) {
                Element master = (Element) maquina.getElementsByTagName("master").item(0);
                CS_Mestre mestre = new CS_Mestre(
                        maquina.getAttribute("id"),
                        maquina.getAttribute("owner"),
                        Double.parseDouble(maquina.getAttribute("power")),
                        Double.parseDouble(maquina.getAttribute("load")),
                        master.getAttribute("scheduler")/*Escalonador*/);
                mestres.add(mestre);
                centroDeServicos.put(global, mestre);
                //Contabiliza para o usuario poder computacional do mestre
                usuarios.put(mestre.getProprietario(), usuarios.get(mestre.getProprietario()) + mestre.getPoderComputacional());
            } else {
                CS_Maquina maq = new CS_Maquina(
                        maquina.getAttribute("id"),
                        maquina.getAttribute("owner"),
                        Double.parseDouble(maquina.getAttribute("power")),
                        1/*num processadores*/,
                        Double.parseDouble(maquina.getAttribute("load")));
                maqs.add(maq);
                centroDeServicos.put(global, maq);
                usuarios.put(maq.getProprietario(), usuarios.get(maq.getProprietario()) + maq.getPoderComputacional());
            }
        }
        //Realiza leitura dos icones de cluster
        for (int i = 0; i < docclusters.getLength(); i++) {
            Element cluster = (Element) docclusters.item(i);
            Element id = (Element) cluster.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            if (Boolean.parseBoolean(cluster.getAttribute("master"))) {
                CS_Mestre clust = new CS_Mestre(
                        cluster.getAttribute("id"),
                        cluster.getAttribute("owner"),
                        Double.parseDouble(cluster.getAttribute("power")),
                        0.0,
                        cluster.getAttribute("scheduler")/*Escalonador*/);
                mestres.add(clust);
                centroDeServicos.put(global, clust);
                //Contabiliza para o usuario poder computacional do mestre
                int numeroEscravos = Integer.parseInt(cluster.getAttribute("nodes"));
                double total = clust.getPoderComputacional() + (clust.getPoderComputacional() * numeroEscravos);
                usuarios.put(clust.getProprietario(), total + usuarios.get(clust.getProprietario()));
                CS_Switch Switch = new CS_Switch(
                        cluster.getAttribute("id"),
                        Double.parseDouble(cluster.getAttribute("bandwidth")),
                        0.0,
                        Double.parseDouble(cluster.getAttribute("latency")));
                links.add(Switch);
                clust.addConexoesEntrada(Switch);
                clust.addConexoesSaida(Switch);
                Switch.addConexoesEntrada(clust);
                Switch.addConexoesSaida(clust);
                for (int j = 0; j < numeroEscravos; j++) {
                    CS_Maquina maq = new CS_Maquina(
                            cluster.getAttribute("id"),
                            cluster.getAttribute("owner"),
                            Double.parseDouble(cluster.getAttribute("power")),
                            1/*numero de processadores*/,
                            0.0/*TaxaOcupacao*/,
                            j + 1/*identificador da maquina no cluster*/);
                    maq.addConexoesSaida(Switch);
                    maq.addConexoesEntrada(Switch);
                    Switch.addConexoesEntrada(maq);
                    Switch.addConexoesSaida(maq);
                    maq.addMestre(clust);
                    clust.addEscravo(maq);
                    maqs.add(maq);
                    //não adicionei referencia ao switch nem aos escrevos do cluster aos centros de serviços
                }
            } else {
                CS_Switch Switch = new CS_Switch(
                        cluster.getAttribute("id"),
                        Double.parseDouble(cluster.getAttribute("bandwidth")),
                        0.0,
                        Double.parseDouble(cluster.getAttribute("latency")));
                links.add(Switch);
                centroDeServicos.put(global, Switch);
                //Contabiliza para o usuario poder computacional do mestre
                double total = Double.parseDouble(cluster.getAttribute("power"))
                        * Integer.parseInt(cluster.getAttribute("nodes"));
                usuarios.put(cluster.getAttribute("owner"), total + usuarios.get(cluster.getAttribute("owner")));
                ArrayList<CS_Maquina> maqTemp = new ArrayList<CS_Maquina>();
                int numeroEscravos = Integer.parseInt(cluster.getAttribute("nodes"));
                for (int j = 0; j < numeroEscravos; j++) {
                    CS_Maquina maq = new CS_Maquina(
                            cluster.getAttribute("id"),
                            cluster.getAttribute("owner"),
                            Double.parseDouble(cluster.getAttribute("power")),
                            1/*numero de processadores*/,
                            0.0/*TaxaOcupacao*/,
                            j + 1/*identificador da maquina no cluster*/);
                    maq.addConexoesSaida(Switch);
                    maq.addConexoesEntrada(Switch);
                    Switch.addConexoesEntrada(maq);
                    Switch.addConexoesSaida(maq);
                    maqTemp.add(maq);
                    maqs.add(maq);
                }
                escravosCluster.put(Switch, maqTemp);
            }
        }
        //Realiza leitura dos icones de internet
        for (int i = 0; i < docinternet.getLength(); i++) {
            Element inet = (Element) docinternet.item(i);
            Element id = (Element) inet.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            CS_Internet net = new CS_Internet(
                    inet.getAttribute("id"),
                    Double.parseDouble(inet.getAttribute("bandwidth")),
                    Double.parseDouble(inet.getAttribute("load")),
                    Double.parseDouble(inet.getAttribute("latency")));
            nets.add(net);
            centroDeServicos.put(global, net);
        }
        //cria os links e realiza a conexão entre os recursos
        for (int i = 0; i < doclinks.getLength(); i++) {
            Element link = (Element) doclinks.item(i);

            CS_Link cslink = new CS_Link(
                    link.getAttribute("id"),
                    Double.parseDouble(link.getAttribute("bandwidth")),
                    Double.parseDouble(link.getAttribute("load")),
                    Double.parseDouble(link.getAttribute("latency")));
            links.add(cslink);

            //adiciona entrada e saida desta conexão
            Element connect = (Element) link.getElementsByTagName("connect").item(0);
            Vertice origem = (Vertice) centroDeServicos.get(Integer.parseInt(connect.getAttribute("origination")));
            Vertice destino = (Vertice) centroDeServicos.get(Integer.parseInt(connect.getAttribute("destination")));
            cslink.setConexoesSaida((CentroServico) destino);
            destino.addConexoesEntrada(cslink);
            cslink.setConexoesEntrada((CentroServico) origem);
            origem.addConexoesSaida(cslink);
        }
        //adiciona os escravos aos mestres
        for (int i = 0; i < docmaquinas.getLength(); i++) {
            Element maquina = (Element) docmaquinas.item(i);
            Element id = (Element) maquina.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            if (maquina.getElementsByTagName("master").getLength() > 0) {
                Element master = (Element) maquina.getElementsByTagName("master").item(0);
                NodeList slaves = master.getElementsByTagName("slave");
                CS_Mestre mestre = (CS_Mestre) centroDeServicos.get(global);
                for (int j = 0; j < slaves.getLength(); j++) {
                    Element slave = (Element) slaves.item(j);
                    CentroServico maq = centroDeServicos.get(Integer.parseInt(slave.getAttribute("id")));
                    if (maq instanceof CS_Processamento) {
                        mestre.addEscravo((CS_Processamento) maq);
                        if (maq instanceof CS_Maquina) {
                            CS_Maquina maqTemp = (CS_Maquina) maq;
                            maqTemp.addMestre(mestre);
                        }
                    } else if (maq instanceof CS_Switch) {
                        for (CS_Maquina escr : escravosCluster.get(maq)) {
                            escr.addMestre(mestre);
                            mestre.addEscravo(escr);
                        }
                    }
                }
            }
        }
        //verifica se há usuarios sem nenhum recurso
        ArrayList<String> proprietarios = new ArrayList<String>();
        ArrayList<Double> poderComp = new ArrayList<Double>();
        for (String user : usuarios.keySet()) {
            proprietarios.add(user);
            poderComp.add(usuarios.get(user));
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
        rdf.setUsuarios(proprietarios);
        return rdf;
    }

    public static GerarCarga newGerarCarga(Document modelo) {
        org.w3c.dom.NodeList cargas = modelo.getElementsByTagName("load");
        GerarCarga cargasConfiguracao = null;
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
                List<CargaForNode> tarefasDoNo = new ArrayList<CargaForNode>();
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
                    CargaForNode item = new CargaForNode(aplicacao, proprietario, escalonador, numeroTarefas, maxComputacao, minComputacao, maxComunicacao, minComunicacao);
                    tarefasDoNo.add(item);
                }
                cargasConfiguracao = new CargaList(tarefasDoNo,GerarCarga.FORNODE);
            }
            cargas = cargaAux.getElementsByTagName("trace");
            if (cargas.getLength() != 0) {
                Element carga = (Element) cargas.item(0);
                File filepath = new File(carga.getAttribute("file_path"));
                Integer num_tarefas = Integer.parseInt(carga.getAttribute("tasks"));
                String formato = carga.getAttribute("format");
                if (filepath.exists()) {
                    cargasConfiguracao = new CargaTrace(filepath, num_tarefas, formato);
                }
            }
        }
        return cargasConfiguracao;
    }

    private static void setCaracteristicas(ItemGrade item, NodeList elementsByTagName) {
        Machine maq = null;
        Cluster clust = null;
        if (item instanceof Machine) {
            maq = (Machine) item;
        } else if (item instanceof Cluster) {
            clust = (Cluster) item;
        }
        if (elementsByTagName.getLength() > 0 && clust != null) {
            Element caracteristicas = (Element) elementsByTagName.item(0);
            Element process = (Element) caracteristicas.getElementsByTagName("process").item(0);
            clust.setPoderComputacional(Double.valueOf(process.getAttribute("power")));
            clust.setNucleosProcessador(Integer.valueOf(process.getAttribute("number")));
            Element memory = (Element) caracteristicas.getElementsByTagName("memory").item(0);
            clust.setMemoriaRAM(Double.valueOf(memory.getAttribute("size")));
            Element disk = (Element) caracteristicas.getElementsByTagName("hard_disk").item(0);
            clust.setDiscoRigido(Double.valueOf(disk.getAttribute("size")));
        } else if (elementsByTagName.getLength() > 0 && maq != null) {
            Element caracteristicas = (Element) elementsByTagName.item(0);
            Element process = (Element) caracteristicas.getElementsByTagName("process").item(0);
            maq.setPoderComputacional(Double.valueOf(process.getAttribute("power")));
            maq.setNucleosProcessador(Integer.valueOf(process.getAttribute("number")));
            Element memory = (Element) caracteristicas.getElementsByTagName("memory").item(0);
            maq.setMemoriaRAM(Double.valueOf(memory.getAttribute("size")));
            Element disk = (Element) caracteristicas.getElementsByTagName("hard_disk").item(0);
            maq.setDiscoRigido(Double.valueOf(disk.getAttribute("size")));
        }
    }

    public static void newGrade(Document descricao, Set<ispd.gui.iconico.Vertice> vertices, Set<Aresta> arestas) {
        HashMap<Integer, Object> icones = new HashMap<Integer, Object>();
        NodeList maquinas = descricao.getElementsByTagName("machine");
        NodeList clusters = descricao.getElementsByTagName("cluster");
        NodeList internet = descricao.getElementsByTagName("internet");
        NodeList links = descricao.getElementsByTagName("link");
        //Realiza leitura dos icones de cluster
        for (int i = 0; i < clusters.getLength(); i++) {
            Element cluster = (Element) clusters.item(i);
            Element pos = (Element) cluster.getElementsByTagName("position").item(0);
            int x = Integer.parseInt(pos.getAttribute("x"));
            int y = Integer.parseInt(pos.getAttribute("y"));
            Element id = (Element) cluster.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            int local = Integer.parseInt(id.getAttribute("local"));
            Cluster clust = new Cluster(x, y, local, global);
            clust.setSelected(false);
            vertices.add(clust);
            icones.put(global, clust);
            clust.getId().setNome(cluster.getAttribute("id"));
            ValidaValores.addNomeIcone(clust.getId().getNome());
            clust.setPoderComputacional(Double.parseDouble(cluster.getAttribute("power")));
            setCaracteristicas(clust, cluster.getElementsByTagName("characteristic"));
            clust.setNumeroEscravos(Integer.parseInt(cluster.getAttribute("nodes")));
            clust.setBanda(Double.parseDouble(cluster.getAttribute("bandwidth")));
            clust.setLatencia(Double.parseDouble(cluster.getAttribute("latency")));
            clust.setAlgoritmo(cluster.getAttribute("scheduler"));
            clust.setProprietario(cluster.getAttribute("owner"));
            clust.setMestre(Boolean.parseBoolean(cluster.getAttribute("master")));
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
            Internet net = new Internet(x, y, local, global);
            net.setSelected(false);
            vertices.add(net);
            icones.put(global, net);
            net.getId().setNome(inet.getAttribute("id"));
            ValidaValores.addNomeIcone(net.getId().getNome());
            net.setBanda(Double.parseDouble(inet.getAttribute("bandwidth")));
            net.setTaxaOcupacao(Double.parseDouble(inet.getAttribute("load")));
            net.setLatencia(Double.parseDouble(inet.getAttribute("latency")));
        }
        //Realiza leitura dos icones de máquina
        for (int i = 0; i < maquinas.getLength(); i++) {
            Element maquina = (Element) maquinas.item(i);
            if (maquina.getElementsByTagName("master").getLength() <= 0) {
                Element pos = (Element) maquina.getElementsByTagName("position").item(0);
                int x = Integer.parseInt(pos.getAttribute("x"));
                int y = Integer.parseInt(pos.getAttribute("y"));
                Element id = (Element) maquina.getElementsByTagName("icon_id").item(0);
                int global = Integer.parseInt(id.getAttribute("global"));
                int local = Integer.parseInt(id.getAttribute("local"));
                Machine maq = new Machine(x, y, local, global);
                maq.setSelected(false);
                icones.put(global, maq);
                vertices.add(maq);
                maq.getId().setNome(maquina.getAttribute("id"));
                ValidaValores.addNomeIcone(maq.getId().getNome());
                maq.setPoderComputacional(Double.parseDouble(maquina.getAttribute("power")));
                setCaracteristicas(maq, maquina.getElementsByTagName("characteristic"));
                maq.setTaxaOcupacao(Double.parseDouble(maquina.getAttribute("load")));
                maq.setProprietario(maquina.getAttribute("owner"));
            } else {
                Element pos = (Element) maquina.getElementsByTagName("position").item(0);
                int x = Integer.parseInt(pos.getAttribute("x"));
                int y = Integer.parseInt(pos.getAttribute("y"));
                Element id = (Element) maquina.getElementsByTagName("icon_id").item(0);
                int global = Integer.parseInt(id.getAttribute("global"));
                int local = Integer.parseInt(id.getAttribute("local"));
                Machine maq = new Machine(x, y, local, global);
                maq.setSelected(false);
                icones.put(global, maq);
            }
        }
        //Realiza leitura dos mestres
        for (int i = 0; i < maquinas.getLength(); i++) {
            Element maquina = (Element) maquinas.item(i);
            if (maquina.getElementsByTagName("master").getLength() > 0) {
                Element id = (Element) maquina.getElementsByTagName("icon_id").item(0);
                int global = Integer.parseInt(id.getAttribute("global"));
                Machine maq = (Machine) icones.get(global);
                vertices.add(maq);
                maq.getId().setNome(maquina.getAttribute("id"));
                ValidaValores.addNomeIcone(maq.getId().getNome());
                maq.setPoderComputacional(Double.parseDouble(maquina.getAttribute("power")));
                setCaracteristicas(maq, maquina.getElementsByTagName("characteristic"));
                maq.setTaxaOcupacao(Double.parseDouble(maquina.getAttribute("load")));
                maq.setProprietario(maquina.getAttribute("owner"));
                Element master = (Element) maquina.getElementsByTagName("master").item(0);
                maq.setAlgoritmo(master.getAttribute("scheduler"));
                maq.setMestre(true);
                NodeList slaves = master.getElementsByTagName("slave");
                List<ItemGrade> escravos = new ArrayList<ItemGrade>(slaves.getLength());
                for (int j = 0; j < slaves.getLength(); j++) {
                    Element slave = (Element) slaves.item(j);
                    escravos.add((ItemGrade) icones.get(Integer.parseInt(slave.getAttribute("id"))));
                }
                maq.setEscravos(escravos);
            }
        }
        //Realiza leitura dos icones de rede
        for (int i = 0; i < links.getLength(); i++) {
            Element link = (Element) links.item(i);
            Element id = (Element) link.getElementsByTagName("icon_id").item(0);
            int global = Integer.parseInt(id.getAttribute("global"));
            int local = Integer.parseInt(id.getAttribute("local"));
            int x = 0, y = 0, px = 0, py = 0;
            Element connect = (Element) link.getElementsByTagName("connect").item(0);
            ispd.gui.iconico.Vertice origem = (ispd.gui.iconico.Vertice) icones.get(Integer.parseInt(connect.getAttribute("origination")));
            ispd.gui.iconico.Vertice destino = (ispd.gui.iconico.Vertice) icones.get(Integer.parseInt(connect.getAttribute("destination")));
            Link lk = new Link(origem, destino, local, global);
            lk.setSelected(false);
            ((ItemGrade) origem).getConexoesSaida().add(lk);
            ((ItemGrade) destino).getConexoesEntrada().add(lk);
            arestas.add(lk);
            lk.getId().setNome(link.getAttribute("id"));
            ValidaValores.addNomeIcone(lk.getId().getNome());
            lk.setBanda(Double.parseDouble(link.getAttribute("bandwidth")));
            lk.setTaxaOcupacao(Double.parseDouble(link.getAttribute("load")));
            lk.setLatencia(Double.parseDouble(link.getAttribute("latency")));
        }
    }

    public static HashSet<String> newSetUsers(Document descricao) {
        NodeList owners = descricao.getElementsByTagName("owner");
        HashSet<String> usuarios = new HashSet<String>();
        //Realiza leitura dos usuários/proprietários do modelo
        for (int i = 0; i < owners.getLength(); i++) {
            Element owner = (Element) owners.item(i);
            usuarios.add(owner.getAttribute("id"));
        }
        return usuarios;
    }

    public static List<String> newListUsers(Document descricao) {
        NodeList owners = descricao.getElementsByTagName("owner");
        List<String> usuarios = new ArrayList<String>();
        //Realiza leitura dos usuários/proprietários do modelo
        for (int i = 0; i < owners.getLength(); i++) {
            Element owner = (Element) owners.item(i);
            usuarios.add(owner.getAttribute("id"));
        }
        return usuarios;
    }

    public static Document[] clone(File file, int number) throws ParserConfigurationException, IOException, SAXException {
        Document[] documento = new Document[number];
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        //Indicar local do arquivo .dtd
        for (int i = 0; i < number; i++) {
            builder.setEntityResolver(new EntityResolver() {
                InputSource substitute = new InputSource(IconicoXML.class.getResourceAsStream("iSPD.dtd"));

                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    return substitute;
                }
            });
            documento[i] = builder.parse(file);
        }
        //inputStream.close();
        return documento;
    }

    private static ArrayList<Element> caminho(String origem, String destino, Document docPlataform, HashMap<Integer, Integer> link_origem, HashMap<Integer, Integer> link_destino, HashMap<Integer, String> comutacao, HashMap<Integer, String> maquinas, ArrayList<String> expandido) {
        ArrayList<Element> caminho = new ArrayList<Element>();
        for (Map.Entry<Integer, Integer> entry : link_origem.entrySet()) {
            Integer chaveLink = entry.getKey();
            Integer idOrigem = entry.getValue();
            if (maquinas.get(idOrigem) != null && maquinas.get(idOrigem).equals(origem)) {
                if (maquinas.get(link_destino.get(chaveLink)) == null) {
                    ArrayList<Element> temp = caminho(comutacao.get(link_destino.get(chaveLink)), destino, docPlataform, link_origem, link_destino, comutacao, maquinas, new ArrayList<String>());
                    if (temp != null) {
                        Element elemento = docPlataform.createElement("route_element");
                        elemento.setAttribute("name", comutacao.get(chaveLink));
                        caminho.add(elemento);
                        for (Element element : temp) {
                            caminho.add(element);
                        }
                        return caminho;
                    }
                } else {
                    if (maquinas.get(link_destino.get(chaveLink)).equals(destino)) {
                        Element elemento = docPlataform.createElement("route_element");
                        elemento.setAttribute("name", comutacao.get(chaveLink));
                        caminho.add(elemento);
                        return caminho;
                    }
                }
            } else if (comutacao.get(idOrigem) != null && comutacao.get(idOrigem).equals(origem)) {
                if (maquinas.get(link_destino.get(chaveLink)) == null) {
                    if (!expandido.contains(comutacao.get(link_destino.get(chaveLink)))) {
                        ArrayList<String> tempExp = new ArrayList<String>(expandido);
                        tempExp.add(comutacao.get(idOrigem));
                        ArrayList<Element> temp = caminho(comutacao.get(link_destino.get(chaveLink)), destino, docPlataform, link_origem, link_destino, comutacao, maquinas, tempExp);
                        if (temp != null) {
                            Element elemento = docPlataform.createElement("route_element");
                            elemento.setAttribute("name", comutacao.get(idOrigem));
                            caminho.add(elemento);
                            elemento = docPlataform.createElement("route_element");
                            elemento.setAttribute("name", comutacao.get(chaveLink));
                            caminho.add(elemento);
                            for (Element element : temp) {
                                caminho.add(element);
                            }
                            return caminho;
                        }
                    }
                } else {
                    if (maquinas.get(link_destino.get(chaveLink)).equals(destino)) {
                        Element elemento = docPlataform.createElement("route_element");
                        elemento.setAttribute("name", comutacao.get(idOrigem));
                        caminho.add(elemento);
                        elemento = docPlataform.createElement("route_element");
                        elemento.setAttribute("name", comutacao.get(chaveLink));
                        caminho.add(elemento);
                        return caminho;
                    }
                }
            }
        }
        return null;
    }

    public void addUsers(Collection<String> usuarios) {
        for (String user : usuarios) {
            Element owner = descricao.createElement("owner");
            owner.setAttribute("id", user);
            system.appendChild(owner);
        }
    }

    public void addInternet(int x, int y, int idLocal, int idGlobal, String nome,
            double banda, double ocupacao, double latencia) {
        Element aux;
        Element posicao = descricao.createElement("position");
        posicao.setAttribute("x", Integer.toString(x));
        posicao.setAttribute("y", Integer.toString(y));
        Element icon_id = descricao.createElement("icon_id");
        icon_id.setAttribute("global", Integer.toString(idGlobal));
        icon_id.setAttribute("local", Integer.toString(idLocal));

        aux = descricao.createElement("internet");
        aux.setAttribute("bandwidth", Double.toString(banda));
        aux.setAttribute("load", Double.toString(ocupacao));
        aux.setAttribute("latency", Double.toString(latencia));

        aux.setAttribute("id", nome);
        aux.appendChild(posicao);
        aux.appendChild(icon_id);
        system.appendChild(aux);
    }

    public void addCluster(Integer x, Integer y, Integer idLocal, Integer idGlobal, String nome,
            Integer numeroEscravos, Double poderComputacional, Integer numeroNucleos,
            Double memoriaRAM, Double discoRigido,
            Double banda, Double latencia,
            String algoritmo, String proprietario, Boolean mestre) {
        Element aux;
        Element posicao = descricao.createElement("position");
        posicao.setAttribute("x", x.toString());
        posicao.setAttribute("y", y.toString());
        Element icon_id = descricao.createElement("icon_id");
        icon_id.setAttribute("global", idGlobal.toString());
        icon_id.setAttribute("local", idLocal.toString());

        aux = descricao.createElement("cluster");
        aux.setAttribute("nodes", numeroEscravos.toString());
        aux.setAttribute("power", poderComputacional.toString());
        aux.setAttribute("bandwidth", banda.toString());
        aux.setAttribute("latency", latencia.toString());
        aux.setAttribute("scheduler", algoritmo);
        aux.setAttribute("owner", proprietario);
        aux.setAttribute("master", mestre.toString());

        aux.setAttribute("id", nome);
        aux.appendChild(posicao);
        aux.appendChild(icon_id);
        aux.appendChild(newCharacteristic(poderComputacional, numeroNucleos, memoriaRAM, discoRigido));
        system.appendChild(aux);
    }

    public void addMachine(Integer x, Integer y, Integer idLocal, Integer idGlobal, String nome,
            Double poderComputacional, Double ocupacao, String algoritmo, String proprietario,
            Integer numeroNucleos, Double memoriaRAM, Double discoRigido,
            boolean mestre, Collection<Integer> escravos) {
        Element aux;
        Element posicao = descricao.createElement("position");
        posicao.setAttribute("x", x.toString());
        posicao.setAttribute("y", y.toString());
        Element icon_id = descricao.createElement("icon_id");
        icon_id.setAttribute("global", idGlobal.toString());
        icon_id.setAttribute("local", idLocal.toString());

        aux = descricao.createElement("machine");
        aux.setAttribute("power", Double.toString(poderComputacional));
        aux.setAttribute("load", Double.toString(ocupacao));
        aux.setAttribute("owner", proprietario);
        if (mestre) {
            //preenche escravos
            Element master = descricao.createElement("master");
            master.setAttribute("scheduler", algoritmo);
            for (Integer escravo : escravos) {
                Element slave = descricao.createElement("slave");
                slave.setAttribute("id", escravo.toString());
                master.appendChild(slave);
            }
            aux.appendChild(master);
        }
        aux.setAttribute("id", nome);
        aux.appendChild(posicao);
        aux.appendChild(icon_id);
        aux.appendChild(newCharacteristic(poderComputacional, numeroNucleos, memoriaRAM, discoRigido));
        system.appendChild(aux);
    }

    public void addLink(int x0, int y0, int x1, int y1, int idLocal, int idGlobal, String nome,
            double banda, double taxaOcupacao, double latencia, int origem, int destino) {
        Element aux = null;
        Element posicao = descricao.createElement("position");
        posicao.setAttribute("x", Integer.toString(x0));
        posicao.setAttribute("y", Integer.toString(y0));
        Element icon_id = descricao.createElement("icon_id");
        icon_id.setAttribute("global", Integer.toString(idGlobal));
        icon_id.setAttribute("local", Integer.toString(idLocal));

        aux = descricao.createElement("link");
        aux.setAttribute("bandwidth", Double.toString(banda));
        aux.setAttribute("load", Double.toString(taxaOcupacao));
        aux.setAttribute("latency", Double.toString(latencia));
        Element connect = descricao.createElement("connect");
        connect.setAttribute("origination", Integer.toString(origem));
        connect.setAttribute("destination", Integer.toString(destino));
        aux.appendChild(connect);
        aux.appendChild(posicao);
        posicao = descricao.createElement("position");
        posicao.setAttribute("x", Integer.toString(x1));
        posicao.setAttribute("y", Integer.toString(y1));

        aux.setAttribute("id", nome);
        aux.appendChild(posicao);
        aux.appendChild(icon_id);
        system.appendChild(aux);
    }

    public void setLoadRandom(Integer numeroTarefas, Integer timeToArrival,
            Integer maxComputacao, Integer averageComputacao, Integer minComputacao, Double probabilityComputacao,
            Integer maxComunicacao, Integer averageComunicacao, Integer minComunicacao, Double probabilityComunicacao) {
        if (load == null) {
            load = descricao.createElement("load");
            system.appendChild(load);
        }
        Element xmlRandom = descricao.createElement("random");
        xmlRandom.setAttribute("tasks", numeroTarefas.toString());
        xmlRandom.setAttribute("time_arrival", timeToArrival.toString());
        Element size = descricao.createElement("size");
        size.setAttribute("type", "computing");
        size.setAttribute("maximum", maxComputacao.toString());
        size.setAttribute("average", averageComputacao.toString());
        size.setAttribute("minimum", minComputacao.toString());
        size.setAttribute("probability", probabilityComputacao.toString());
        xmlRandom.appendChild(size);
        size = descricao.createElement("size");
        size.setAttribute("type", "communication");
        size.setAttribute("maximum", maxComunicacao.toString());
        size.setAttribute("average", averageComunicacao.toString());
        size.setAttribute("minimum", minComunicacao.toString());
        size.setAttribute("probability", probabilityComunicacao.toString());
        xmlRandom.appendChild(size);
        load.appendChild(xmlRandom);
    }

    public void addLoadNo(
            String aplicacao, String proprietario, String escalonador, Integer numeroTarefas,
            Double maxComputacao, Double minComputacao,
            Double maxComunicacao, Double minComunicacao) {
        if (load == null) {
            load = descricao.createElement("load");
            system.appendChild(load);
        }
        Element xmlNode = descricao.createElement("node");
        xmlNode.setAttribute("application", aplicacao);
        xmlNode.setAttribute("owner", proprietario);
        xmlNode.setAttribute("id_master", escalonador);
        xmlNode.setAttribute("tasks", numeroTarefas.toString());
        Element size = descricao.createElement("size");
        size.setAttribute("type", "computing");
        size.setAttribute("maximum", maxComputacao.toString());
        size.setAttribute("minimum", minComputacao.toString());
        xmlNode.appendChild(size);
        size = descricao.createElement("size");
        size.setAttribute("type", "communication");
        size.setAttribute("maximum", maxComunicacao.toString());
        size.setAttribute("minimum", minComunicacao.toString());
        xmlNode.appendChild(size);
        load.appendChild(xmlNode);
    }

    public void setLoadTrace(String file, String task, String format) {
        if (load == null) {
            load = descricao.createElement("load");
            system.appendChild(load);
        }
        Element xmlTrace = descricao.createElement("trace");
        xmlTrace.setAttribute("file_path", file);
        xmlTrace.setAttribute("tasks", task);
        xmlTrace.setAttribute("format", format);
        load.appendChild(xmlTrace);
    }

    public Document getDescricao() {
        return descricao;
    }

    private Node newCharacteristic(Double poderComputacional, Integer numeroNucleos, Double memoriaRAM, Double discoRigido) {
        Element characteristic = descricao.createElement("characteristic");
        Element process = descricao.createElement("process");
        process.setAttribute("power", poderComputacional.toString());
        process.setAttribute("number", numeroNucleos.toString());
        Element memory = descricao.createElement("memory");
        memory.setAttribute("size", memoriaRAM.toString());
        Element hard_disk = descricao.createElement("hard_disk");
        hard_disk.setAttribute("size", discoRigido.toString());
        characteristic.appendChild(process);
        characteristic.appendChild(memory);
        characteristic.appendChild(hard_disk);
        return characteristic;
    }
}