/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor.carga;

import ispd.motor.Tarefa;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Descreve como gerar tarefas na forma por nó mestre
 * @author denison_usuario
 */
public class CargaForNode extends GerarCarga{

    private List<CargaTaskNode> configuracaoNo;

    public CargaForNode(List configuracaoNo) {
        this.configuracaoNo = configuracaoNo;
    }

    public static GerarCarga newGerarCarga(String entrada) {
        CargaForNode newObj = null;
        String[] linhas = entrada.split("\n");
        List<CargaTaskNode> nos = new ArrayList<CargaTaskNode>();
        for(int i = 0; i < linhas.length; i++){
                CargaTaskNode newNo = (CargaTaskNode) CargaTaskNode.newGerarCarga(linhas[i]);
                if(newNo!=null){
                    nos.add(newNo);
                }
        }
        if(nos.size()>0){
            newObj = new CargaForNode(nos);
        }
        return newObj;
    }

    public Vector toVector() {
        Vector<Vector> temp = new Vector<Vector>(configuracaoNo.size());
        for (CargaTaskNode item : configuracaoNo) {
            temp.add(item.toVector());
        }
        return temp;
    }

    @Override
    public List<Tarefa> toTarefaList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        StringBuffer saida = new StringBuffer();
        for (CargaTaskNode cargaTaskNode : configuracaoNo) {
            saida.append(cargaTaskNode.toString() + "\n");
        }
        return saida.toString();
    }

    public int getTipo() {
        return GerarCarga.FORNODE;
    }

    public List<CargaTaskNode> getConfiguracaoNo() {
        return configuracaoNo;
    }
}
