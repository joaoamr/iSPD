/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.alocacaoVM;

import ispd.motor.Simulacao;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author João Antonio Magri Rodrigues
 */
public abstract class AlocadorLocal {
    protected List<CS_VirtualMac> maquinasVirtuais;
    protected VMM mestre; 
    protected CS_MaquinaCloud host;
    
    public void iniciar(Simulacao simulacao) {
        maquinasVirtuais = new ArrayList<CS_VirtualMac>();
    }
    
    public void adicionarVm (CS_VirtualMac vm){
        host.setMemoriaDisponivel(host.getMemoriaDisponivel() - vm.getMemoriaDisponivel());
        host.setDiscoDisponivel(host.getDiscoDisponivel() - vm.getDiscoDisponivel());
        host.setPoderDisponivel(host.getPoderDisponivel() - vm.getPoderNecessario());
        vm.setMaquinaHospedeira(host);
        
        maquinasVirtuais.add(vm);
    }
    
    public boolean encaixarVm(CS_VirtualMac vm){
        if(host.getMemoriaDisponivel() < vm.getMemoriaDisponivel())
            return false;
        
        if(host.getDiscoDisponivel() < vm.getDiscoDisponivel())
            return false;
        
        return true;
    }

    public List<CS_VirtualMac> getMaquinasVirtuais() {
        return maquinasVirtuais;
    }

    public void setMaquinasVirtuais(List<CS_VirtualMac> maquinasVirtuais) {
        this.maquinasVirtuais = maquinasVirtuais;
    }

    public VMM getMestre() {
        return mestre;
    }

    public void setMestre(VMM mestre) {
        this.mestre = mestre;
    }

    public CS_MaquinaCloud getHost() {
        return host;
    }

    public void setHost(CS_MaquinaCloud host) {
        this.host = host;
    }    
}


