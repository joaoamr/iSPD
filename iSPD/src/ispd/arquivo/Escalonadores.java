/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.arquivo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Esta classe realiza o gerenciamento dos arquivos de escalonamento do simulador
 *
 * @author denison_usuario
 */
public class Escalonadores {
    private final String DIRETORIO = "ispd/externo/escalonador";
    /**
     * guarda a lista de escalonadores disponiveis
     */
    private ArrayList<String> escalonadores;
    /**
     * mantem o caminho do pacote escalonador
     */
    private File diretorio = null;
    /**
     * @return diretório onde fica os arquivos dos escalonadores
     */
    public File getDiretorio() {
        return diretorio;
    }

    /**
     * Atribui o caminho do pacote escalonador e os escalonadores (.class) contidos nele
     */
    public Escalonadores() {
        diretorio = new File(DIRETORIO);
        escalonadores = new ArrayList<String>();
        //Verifica se pacote existe caso não exista cria ele
        if (!diretorio.exists()) {
            diretorio.mkdirs();
            //executando a partir de um jar
            if (getClass().getResource("Escalonadores.class").toString().startsWith("jar:")) {
                File jar = new File(System.getProperty("java.class.path"));
                //carrega dependencias para compilação
                try {
                    //File destino = new File("politicasescalonamento/externo/escalonador");
                    extrairDiretorioJar(jar, "motor");
                    extrairDiretorioJar(jar, "escalonador");
                } catch (ZipException ex) {
                    //Logger.getLogger(ArquivosEscalonadores2.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    //Logger.getLogger(ArquivosEscalonadores2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            criarWorkqueue();
            compilar("Workqueue");
        } else {
            //busca apenas arquivos .class
            FilenameFilter ff = new FilenameFilter() {

                public boolean accept(File b, String name) {
                    return name.endsWith(".class");
                }
            };
            String[] aux = diretorio.list(ff);
            for (int i = 0; i < aux.length; i++) {
                //remove o .class da string
                aux[i] = aux[i].substring(0, aux[i].length() - 6);
                //atribui escalonadores a lista de strings exceto a classe Escalonador
                //if (!aux[i].equals("Escalonador")) {
                escalonadores.add(aux[i]);
                //}
            }
        }
    }
    /**
     * Método responsável por listar os escalonadores existentes no simulador
     * ele retorna o nome de cada escalonador contido no pacote com arquivo .class
     */
    public ArrayList<String> listar() {
        return escalonadores;
    }
    /**
     * Método responsável por remover um escalonador no simulador
     * ele recebe o nome do escalonador e remove do pacote a classe .java e .class
     */
    public boolean remover(String nomeEscalonador) {
        boolean deletado = false;
        File escalonador = new File(diretorio, nomeEscalonador + ".class");
        if (escalonador.exists()) {
            escalonador.delete();
            removerLista(nomeEscalonador);
            deletado = true;
        }
        escalonador = new File(diretorio, nomeEscalonador + ".java");
        if (escalonador.exists()) {
                escalonador.delete();
                deletado = true;
        }
        return deletado;
    }
    /**
     * Realiza a leitura do arquivo .java do escalonador e retorna um String do conteudo
     */
    public String ler(String escalonador) {
        try {
            FileReader fileInput = new FileReader(new File(diretorio, escalonador + ".java"));
            BufferedReader leitor = new BufferedReader(fileInput);
            StringBuffer buffer = new StringBuffer();
            String linha = leitor.readLine();
            while (linha != null) {
                buffer.append(linha);
                buffer.append('\n');
                linha = leitor.readLine();
            }
            if(buffer.length()>0)
                buffer.deleteCharAt(buffer.length() - 1);
            return buffer.toString();
        } catch (IOException ex) {
            Logger.getLogger(Escalonadores.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    /**
     * Este método sobrescreve o arquivo .java do escalonador informado com o buffer
     */
    public boolean escrever(String escalonador, String conteudo) {
        FileWriter arquivoFonte;
        try {
            File local = new File(diretorio, escalonador + ".java");
            arquivoFonte = new FileWriter(local);
            arquivoFonte.write(conteudo); //grava no arquivo o codigo-fonte Java
            arquivoFonte.close();
        } catch (IOException ex) {
            Logger.getLogger(Escalonadores.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    /**
     * Compila o arquivo .java do escalonador informado
     * caso ocorra algum erro retorna o erro caso contrario retorna null
     * @param
     * escalonador nome do escalonador
     * @return
     * erros da compilação
     */
    public String compilar(String escalonador) {
        //Compilação
        File arquivo = new File(diretorio, escalonador + ".java");
        String errosStr;
        JavaCompiler compilador = ToolProvider.getSystemJavaCompiler();
        if (compilador == null) {
            try {
                Process processo = Runtime.getRuntime().exec("javac " + arquivo.getPath());
                StringBuffer errosdoComando = new StringBuffer();
                InputStream StreamErro = processo.getErrorStream();
                InputStreamReader inpStrAux = new InputStreamReader(StreamErro);
                BufferedReader SaidadoProcesso = new BufferedReader(inpStrAux);
                String linha = SaidadoProcesso.readLine();
                while (linha != null) {
                    errosdoComando.append(linha + "\n");
                    linha = SaidadoProcesso.readLine();
                }
                SaidadoProcesso.close();
                errosStr = errosdoComando.toString();
            } catch (IOException ex) {
                errosStr = "Não foi possível compilar";
                //Logger.getLogger(GerenciaPacoteEscalonadorJar.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            OutputStream erros = new ByteArrayOutputStream();
            compilador.run(null, null, erros, arquivo.getPath());
            errosStr = erros.toString();
        }
        if (errosStr.length() == 0) {
            File test = new File(diretorio , escalonador+".class");
            if(test.exists())
                inserirLista(escalonador);
            return null;
        } else {
            return errosStr;
        }
    }
    /**
     * recebe nome do escalonar e remove ele da lista de escalonadores
     */
    private void removerLista(String nomeEscalonador) {
        escalonadores.remove(nomeEscalonador);
    }
    /**
     * recebe nome do escalonar e adiciona ele na lista de escalonadores
     */
    private void inserirLista(String nome) {
        if (!escalonadores.contains(nome)) {
            escalonadores.add(nome);
        }
    }

    /**
     * cria arquivo java com a politica Workqueue
     */
    private void criarWorkqueue() {
        String codigoFonte =
                "package ispd.externo.escalonador;"
                + "\n" + "import ispd.motor.Tarefa;"
                + "\n" + "import ispd.escalonador.Escalonador;"
                + "\n" + "import java.util.ArrayList;"
                + "\n" + "public class Workqueue extends Escalonador {"
                + "\n" + "  int totalDeTarefas;"
                + "\n" + "  int contador;"
                + "\n" + "  public void iniciar() {"
                + "\n" + "    totalDeTarefas = tarefas.size();"
                + "\n" + "    contador = 0;"
                + "\n" + "  }"
                + "\n" + "  public void atualizar() {"
                + "\n" + "    System.out.println(\"não é utilizado neste algoritmo\");"
                + "\n" + "  }"
                + "\n" + "  public void escalonarTarefa() {}"
                + "\n" + "  public void escalonarRecurso() {"
                + "\n" + "    for (int i = 0; i < recursos.size()&&totalDeTarefas!=contador; i++) {"
                + "\n" + "      if (recursos.get(i).numeroDeTarefas()==0) {"
                + "\n" + "        recursos.get(i).AdicionarTarefa(tarefas.get(contador));"
                + "\n" + "        contador++;"
                + "\n" + "      }"
                + "\n" + "    }"
                + "\n" + "  }"
                + "\n" + "  public void adicionarTarefa(Tarefa tarefa) {}"
                + "\n" + "  public void adicionarFilaTarefa(ArrayList<Tarefa> tarefa) {}"
                + "\n" + "}";
        FileWriter arquivoFonte;
        try {
            File local = new File(diretorio,"Workqueue.java");
            arquivoFonte = new FileWriter(local);
            arquivoFonte.write(codigoFonte); //grava no arquivo o codigo-fonte Java
            arquivoFonte.close();
        } catch (IOException ex) {
            //Logger.getLogger(GerarEscalonador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * @param escalonador
     * @return conteudo de básico para criar uma classe que implemente um escalonador
     */
    public static String getEscalonadorJava(String escalonador) {
        String saida =
                "package ispd.externo.escalonador;"
                + "\n"
                + "\n" + "import ispd.motor.Tarefa;"
                + "\n" + "import ispd.escalonador.Escalonador;"
                + "\n" + "import java.util.ArrayList;"
                + "\n"
                + "\n" + "public class " + escalonador + " extends Escalonador {"
                + "\n"
                + "\n" + "    public void iniciar() {"
                + "\n" + "        throw new UnsupportedOperationException(\"Not yet implemented\");"
                + "\n" + "    }"
                + "\n"
                + "\n" + "    public void atualizar() {"
                + "\n" + "        throw new UnsupportedOperationException(\"Not yet implemented\");"
                + "\n" + "    }"
                + "\n"
                + "\n" + "    public void escalonarTarefa() {"
                + "\n" + "        throw new UnsupportedOperationException(\"Not yet implemented\");"
                + "\n" + "    }"
                + "\n"
                + "\n" + "    public void escalonarRecurso() {"
                + "\n" + "        throw new UnsupportedOperationException(\"Not yet implemented\");"
                + "\n" + "    }"
                + "\n"
                + "\n" + "    public void adicionarTarefa(Tarefa tarefa) {"
                + "\n" + "        throw new UnsupportedOperationException(\"Not yet implemented\");"
                + "\n" + "    }"
                + "\n"
                + "\n" + "    public void adicionarFilaTarefa(ArrayList<Tarefa> tarefa) {"
                + "\n" + "        throw new UnsupportedOperationException(\"Not yet implemented\");"
                + "\n" + "    }"
                + "\n"
                + "\n" + "}";
        return saida;
    }
    /**
     * extrai arquivos que são necessarios fora do jar
     */
    private void extrairDiretorioJar(File arquivoJar, String diretorio) throws ZipException, IOException {
        ZipFile jar = null;
        File arquivo = null;
        InputStream is = null;
        OutputStream os = null;
        byte[] buffer = new byte[2048]; // 2 Kb //TAMANHO_BUFFER
        try {
            jar = new JarFile(arquivoJar);
            Enumeration e = jar.entries();
            while (e.hasMoreElements()) {
                ZipEntry entrada = (JarEntry) e.nextElement();
                if (entrada.getName().contains(diretorio)) {
                    arquivo = new File(entrada.getName());
                    //se for diretório inexistente, cria a estrutura
                    //e pula pra próxima entrada
                    if (entrada.isDirectory() && !arquivo.exists()) {
                        arquivo.mkdirs();
                        continue;
                    }
                    //se a estrutura de diretórios não existe, cria
                    if (!arquivo.getParentFile().exists()) {
                        arquivo.getParentFile().mkdirs();
                    }
                    try {
                        //lê o arquivo do zip e grava em disco
                        is = jar.getInputStream(entrada);
                        os = new FileOutputStream(arquivo);
                        int bytesLidos = 0;
                        if (is == null) {
                            throw new ZipException("Erro ao ler a entrada do zip: " + entrada.getName());
                        }
                        while ((bytesLidos = is.read(buffer)) > 0) {
                            os.write(buffer, 0, bytesLidos);
                        }
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (Exception ex) {
                            }
                        }
                        if (os != null) {
                            try {
                                os.close();
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
            }
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (Exception e) {
                }
            }
        }
    }
    /**
     * Método responsável por adicionar um escalonador no simulador
     * ele recebe uma classe Java compila e adiciona ao pacote a classe .java e .class
     * @param nomeArquivoJava
     * @return true se importar corretamente e false se ocorrer algum erro no processo
     */
    public boolean importarEscalonadorJava(File nomeArquivoJava) {
        //streams
        File localDestino = new File(diretorio, nomeArquivoJava.getName());
        String errosStr;
        //copiar para diretório
        if (!localDestino.getPath().equals(nomeArquivoJava.getPath())) {
            FileInputStream origem;
            FileOutputStream destino;
            FileChannel fcOrigem;
            FileChannel fcDestino;
            try {
                origem = new FileInputStream(nomeArquivoJava);
                destino = new FileOutputStream(localDestino);
                fcOrigem = origem.getChannel();
                fcDestino = destino.getChannel();
                //Faz a copia
                fcOrigem.transferTo(0, fcOrigem.size(), fcDestino);
                origem.close();
                destino.close();
            } catch (IOException e) {
                return false;
                //e.printStackTrace();
            }
        }
        //Compilação
        JavaCompiler compilador = ToolProvider.getSystemJavaCompiler();
        if (compilador == null) {
            try {
                Process processo = Runtime.getRuntime().exec("javac " + localDestino.getPath());
                StringBuffer errosdoComando = new StringBuffer();
                InputStream StreamErro = processo.getErrorStream();
                InputStreamReader inpStrAux = new InputStreamReader(StreamErro);
                BufferedReader SaidadoProcesso = new BufferedReader(inpStrAux);
                String linha = SaidadoProcesso.readLine();
                while (linha != null) {
                    errosdoComando.append(linha + "\n");
                    linha = SaidadoProcesso.readLine();
                }
                SaidadoProcesso.close();
                errosStr = errosdoComando.toString();
            } catch (IOException ex) {
                return false;
                //Logger.getLogger(GerenciaPacoteEscalonadorJar.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            OutputStream erros = new ByteArrayOutputStream();
            compilador.run(null, null, erros, localDestino.getPath());
            errosStr = erros.toString();
        }
        if (errosStr.length() != 0) {
            return false;
        } else {
            String nome = nomeArquivoJava.getName().substring(0,nomeArquivoJava.getName().length()-5);
            File test = new File(diretorio , nome+".class");
            if(!test.exists())
                return false;
            inserirLista(nome);
        }
        return true;
    }
}