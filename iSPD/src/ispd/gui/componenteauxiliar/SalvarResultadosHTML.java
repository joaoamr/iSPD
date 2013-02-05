package ispd.gui.componenteauxiliar;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.metricas.MetricasGlobais;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author denison
 */
public class SalvarResultadosHTML {

    private String tabela;
    private String globais;
    private String tarefas;
    private String chartstxt;
    private BufferedImage charts[];

    public Object[][] setTabelaRecurso(RedeDeFilas rdf) {
        List<String> recurso = new ArrayList<String>();
        List<Object[]> tabela = new ArrayList<Object[]>();
        //linha [Nome] [Proprietario] [Processamento] [comunicacao]
        String nome;
        String prop;
        Double proc;
        Double comu;
        if (rdf.getMestres() != null) {
            for (CS_Processamento mestre : rdf.getMestres()) {
                if (recurso.contains(mestre.getId())) {
                    int i = 0;
                    while (!tabela.get(i)[0].equals(mestre.getId())) {
                        i++;
                    }
                    tabela.get(i)[2] = (Double) tabela.get(i)[2] + mestre.getMetrica().getSegundosDeProcessamento();
                } else {
                    nome = mestre.getId();
                    prop = mestre.getProprietario();
                    proc = mestre.getMetrica().getSegundosDeProcessamento();
                    comu = 0.0;
                    tabela.add(Arrays.asList(nome, prop, proc, comu).toArray());
                    recurso.add(mestre.getId());
                }
            }
        }
        if (rdf.getMaquinas() != null) {
            for (CS_Processamento maq : rdf.getMaquinas()) {
                if (recurso.contains(maq.getId())) {
                    int i = 0;
                    while (!tabela.get(i)[0].equals(maq.getId())) {
                        i++;
                    }
                    proc = maq.getMetrica().getSegundosDeProcessamento();
                    proc += Double.valueOf(tabela.get(i)[2].toString());
                    tabela.get(i)[2] = proc;
                } else {
                    nome = maq.getId();
                    prop = maq.getProprietario();
                    proc = maq.getMetrica().getSegundosDeProcessamento();
                    comu = 0.0;
                    tabela.add(Arrays.asList(nome, prop, proc, comu).toArray());
                    recurso.add(maq.getId());
                }
            }
        }
        if (rdf.getInternets() != null) {
            for (CS_Comunicacao net : rdf.getInternets()) {
                nome = net.getId();
                prop = "---";
                proc = 0.0;
                comu = net.getMetrica().getSegundosDeTransmissao();
                tabela.add(Arrays.asList(nome, prop, proc, comu).toArray());
            }
        }
        if (rdf.getLinks() != null) {
            for (CS_Comunicacao link : rdf.getLinks()) {
                nome = link.getId();
                prop = "---";
                proc = 0.0;
                comu = link.getMetrica().getSegundosDeTransmissao();
                tabela.add(Arrays.asList(nome, prop, proc, comu).toArray());
                recurso.add(link.getId());
            }
        }
        Object[][] temp = new Object[tabela.size()][4];
        for (int i = 0; i < tabela.size(); i++) {
            temp[i] = tabela.get(i);
        }
        //Adicionando resultados na tabela do html
        this.tabela = "";
        for (Object[] item : temp) {
            this.tabela += "<tr><td>" + item[0] + "</td><td>" + item[1] + "</td><td>" + item[2] + "</td><td>" + item[3] + "</td></tr>\n";
        }
        return temp;
    }

    public void setCharts(BufferedImage charts[]) {
        int cont = 0;
        for (BufferedImage item : charts) {
            if (item != null) {
                cont++;
            }
        }
        this.charts = new BufferedImage[cont];
        cont = 0;
        this.chartstxt = "";
        for (BufferedImage item : charts) {
            if (item != null) {
                this.charts[cont] = item;
                this.chartstxt += "<img alt=\"\" src=\"chart"+cont+".png\" style=\"width: 600px; height: 300px;\" />\n";
                cont++;
            }
        }

    }

    public void setMetricasGlobais(MetricasGlobais globais) {
        this.globais = "<li><strong>Total Simulated Time </strong>= " + globais.getTempoSimulacao() + "</li>\n"
                + "<li><strong>Satisfaction</strong> = " + globais.getSatisfacaoMedia() + " %</li>\n"
                + "<li><strong>Idleness of processing resources</strong> = " + globais.getOciosidadeCompuacao() + " %</li>\n"
                + "<li><strong>Idleness of communication resources</strong> = " + globais.getOciosidadeComunicacao() + " %</li>\n"
                + "<li><strong>Efficiency</strong> = " + globais.getEficiencia() + " %</li>\n";
        if (globais.getEficiencia() > 70.0) {
            this.globais += "<li><span style=\"color:##00ff00;\"><strong>Efficiency GOOD</strong></span></li>\n";
        } else if (globais.getEficiencia() > 40.0) {
            this.globais += "<li><strong>Efficiency MEDIA</strong></li>\n ";
        } else {
            this.globais += "<li><span style=\"color:#ff0000;\"><strong>Efficiency BAD</strong></span></li>\n";
        }
    }

    public void setMetricasTarefas(
            double tempoMedioFilaComunicacao,
            double tempoMedioComunicacao,
            double tempoMedioSistemaComunicacao,
            double tempoMedioFilaProcessamento,
            double tempoMedioProcessamento,
            double tempoMedioSistemaProcessamento) {
        this.tarefas = "<ul><li><h2>Tasks</h2><ul><li><strong>Communication</strong><ul>\n"
                + "<li>Queue average time: " + tempoMedioFilaComunicacao + " seconds.</li>\n"
                + "<li>Communication average time: " + tempoMedioComunicacao + " seconds.</li>\n"
                + "<li>System average time: " + tempoMedioSistemaComunicacao + " seconds.</li>\n"
                + "</ul></li><li><strong>Processing</strong><ul>\n"
                + "<li>Queue average time: " + tempoMedioFilaProcessamento + " seconds.</li>\n"
                + "<li>Processing average time: " + tempoMedioProcessamento + " seconds.</li>\n"
                + "<li>System average time: " + tempoMedioSistemaProcessamento + " seconds.</li></ul></li></ul></li></ul>";
    }

    public String getHTMLText() {
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <title>Simulation Results</title>\n"
                + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                + "    </head>\n"
                + "    <body background=\"logogspd.png\">\n"
                + "        <h1 id=\"topo\" style=\"text-align: center;\">\n"
                + "            <span style=\"color:#8b4513;\">Simulation Results</span></h1>\n"
                + "        <hr />\n"
                + "        <div>\n"
                + "            <a href=\"#global\">Global metrics</a> <br>\n"
                + "            <a href=\"#table\">Table of Resource</a> <br>\n"
                + "            <a href=\"#chart\">Charts</a> <br>\n"
                + "        </div>\n"
                + "        <hr />\n"
                + "        <h2 id=\"global\" style=\"text-align: center;\">\n"
                + "            Global metrics</h2>\n"
                + "        " + globais + tarefas
                + "        <div>\n"
                + "            <a href=\"#topo\">Inicio</a>\n"
                + "        </div>\n"
                + "        <hr />\n"
                + "        <h2 id=\"table\" style=\"text-align: center;\">\n"
                + "            Table of Resource\n"
                + "        </h2>\n"
                + "        <table align=\"center\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\" style=\"width: 80%;\">\n"
                + "            <thead>\n"
                + "                <tr>\n"
                + "                    <th scope=\"col\">\n"
                + "                        <span style=\"color:#800000;\">Label</span></th>\n"
                + "                    <th scope=\"col\">\n"
                + "                        <span style=\"color:#800000;\">Owner</span></th>\n"
                + "                    <th scope=\"col\">\n"
                + "                        <span style=\"color:#800000;\">Processing performed</span></th>\n"
                + "                    <th scope=\"col\">\n"
                + "                        <span style=\"color:#800000;\">Communication&nbsp;performed</span></th>\n"
                + "                </tr>\n"
                + "            </thead>\n"
                + "            <tbody>\n"
                + "                " + tabela
                + "            </tbody>\n"
                + "        </table>\n"
                + "        <div>\n"
                + "            <a href=\"#topo\">Inicio</a>\n"
                + "        </div>\n"
                + "        <hr />\n"
                + "        <h2 id=\"chart\" style=\"text-align: center;\">\n"
                + "            Charts\n"
                + "        </h2>\n"
                + "        <p style=\"text-align: center;\">\n"
                + "        " + chartstxt
                + "        </p>\n"
                + "        <div>\n"
                + "            <a href=\"#topo\">Inicio</a>\n"
                + "        </div>\n"
                + "        <hr />\n"
                + "        <p style=\"font-size:10px;\">\n"
                + "            <a href=\"http://gspd.dcce.ibilce.unesp.br/\">GSPD</a></p>\n"
                + "    </body>\n"
                + "</html>";
    }

    public void gerarHTML(File diretorio) throws IOException {
        if (!diretorio.exists()) {
            if (!diretorio.mkdir()) {
                throw new IOException("Could not create directory");
            }
        }
        File arquivo = new File(diretorio, "result.html");
        FileWriter writer;
        writer = new FileWriter(arquivo);
        PrintWriter saida = new PrintWriter(writer, true);
        saida.print(this.getHTMLText());
        saida.close();
        writer.close();
        for (int i = 0; i < charts.length; i++) {
            arquivo = new  File(diretorio, "chart"+i+".png");
            ImageIO.write(charts[i], "png", arquivo);
        }
    }
}