/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.escalonador;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Carrega as classes dos escalonadores dinamicamente
 *
 * @author denison_usuario
 *
 */
public class Carregar {

    private URLClassLoader loader = null;
    
    public Carregar() {
        File diretorio = new File("politicasescalonamento");
        if (diretorio.exists()) {
            try {
                URL[] aux = new URL[1];
                aux[0] = diretorio.toURI().toURL();
                this.loader = URLClassLoader.newInstance(aux,this.getClass().getClassLoader());
            } catch (MalformedURLException ex) {
                //Logger.getLogger(CarregarClasses.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * Recebe o nome de um algoritmo de escalonamento e retorna uma nova instancia
     * de um objeto com este nome ou null caso não encontre ou ocorra um erro.
     * @param nome
     * @return Nova instancia do objeto Escalonador
     */
    public Escalonador getNewEscalonador(String nome) {
        if (loader != null) {
            try {
                Class cl = loader.loadClass("externo.escalonador."+nome);
                Escalonador escalonador = (Escalonador) cl.newInstance();
                //Escalonador escalonador = (Escalonador) Class.forName("novoescalonador."+nome, true, loader).newInstance();
                return escalonador;
            } catch (RuntimeException ex) {
                Logger.getLogger(Carregar.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(Carregar.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Carregar.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Carregar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
}