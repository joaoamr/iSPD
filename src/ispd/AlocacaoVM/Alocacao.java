/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.AlocacaoVM;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.List;

/**
 *
 * @author Diogo Tavares
 */
public class Alocacao {
    private List<CS_Processamento> maquinasFisicas;
    private List<CS_VirtualMac> maquinasVirtuais;
    private CS_VMM hypervisor;
    
}
