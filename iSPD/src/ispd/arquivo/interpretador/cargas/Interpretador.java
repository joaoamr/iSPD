/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.arquivo.interpretador.cargas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Diogo Tavares
 */
public class Interpretador {
    /**
     * 
     * @param args the command line arguments
     */
     /*public static void main(String[] args) {
        // TODO code application logic here
         Interpretador inter;
         String caminho, tipo, saida;
         
         inter = new Interpretador();
         caminho="nordugrid.gwf";
         tipo="GWF";
         saida="nordugrid";
         inter.convert(caminho,tipo,saida);
         System.out.println("Arquivo "+saida+".wmsx gerado");
    }*/
     
    public void convert(String caminho, String tipo, String saida){    
        // TODO code application logic here
        try {   BufferedReader in = new BufferedReader(new FileReader(caminho));
                BufferedWriter out = new BufferedWriter(new FileWriter(saida+".wmsx"));
                String str;
                //iniciando a escrita do arquivo
                out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n" +
                          "<!DOCTYPE system SYSTEM \"iSPDcarga.dtd\">");
                out.write("\n<load>");
                out.write("\n<trace>");
                out.write("\n<format kind=\""+tipo+"\" />");
                while (in.ready()) {
                    
                    str = in.readLine();
                    if(str.equals("")){/*System.out.println("linha em branco");*/}
                    else if("SWF".equals(tipo)){
                        p_tasksSWF(str,out);
                    }
                    else if("GWF".equals(tipo)){
                        p_tasksGWF(str,out);
                    }                    
                }    
                out.write("\n</trace>");
                out.write("\n</load>");
                
                //FECHANDO ARQUIVOS
                
                in.close();
                out.close();
        }
        catch (IOException e) {}
    }


    private static void p_tasksGWF(String str,BufferedWriter out) throws IOException{
        if(str.charAt(0)=='#'){/*System.out.println("Comentário:"+str);*/}//Verifica se é linha de comentário, para os casos do SWF e GWF
        else{
            //System.out.println(str);
            str = str.replaceAll("\t"," ");//elimina espaços em branco desnecessários
            str = str.trim();
            //System.out.println(str);
            
            String[] campos = str.split(" ");
            if(campos[3].equals("-1")){}
            else{
                out.write("\n<task ");
                out.write("id=" + "\"tsk"+campos[0]+ 
                            "\" arr=\"" + campos[1]+ 
                            "\" sts=\"" + campos[10]+
                            "\" cpsz =\"" + campos[3] +
                            "\" cmsz=\"" + campos[20] +
                            "\" usr=\"" + campos[11]);
                out.write("\" />");
            }
        }
    }
    
    private static void p_tasksSWF(String str, BufferedWriter out) throws IOException {
        if(str.charAt(0) == ';'){/*ignore*/} //Verifica se é linha de comentário, para os casos do SWF e GWF
        else{
            str = str.replaceAll("\\s\\s++"," ");//elimina espaços em branco desnecessários
            str = str.trim();
            out.write("\n<task ");
            String[] campos = str.split(" ");
            out.write("id=" + "\"task"+campos[0]+ 
                            "\" arrival=\"" + campos[1]+ 
                            "\" status=\"" + campos[10]+
                            "\" comp_size =\"" + campos[3] +
                            "\" comm_size=\"-1" +
                            "\" user=\"" + "user"+campos[11]);
            out.write("\" />");
        }
    }
}
