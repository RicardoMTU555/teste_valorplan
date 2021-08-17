package com.ctbli9.valorplan.bean.orc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.negocio.ResumoService;
import com.ctbli9.valorplan.negocio.orc.OrcDespesaService;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.LibUtil;

@ManagedBean(name="orcDespesaBean")
@ViewScoped
public class OrcDespesaBean implements Serializable {

	private static final long serialVersionUID = 1L;
		
	private MesAnoOrcamento mesAno;
	private TreeNode listaDespesa;
	private TreeNode selectedNode;
	private boolean anual;
	
	private StreamedContent file;
	private String nomeXLS;
	
	private FacesMessages msg = new FacesMessages();
	
	/*
	 * Atributos
	 */
	public MesAnoOrcamento getMesAno() {
		return mesAno;
	}
	public void setMesAno(MesAnoOrcamento mesAno) {
		this.mesAno = mesAno;
	}

	public TreeNode getListaDespesa() {
		return listaDespesa;
	}
	public void setListaDespesa(TreeNode listaDespesa) {
		this.listaDespesa = listaDespesa;
	}
	
	public TreeNode getSelectedNode() {
		return selectedNode;
	}
	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}
	
	
	public boolean isAnual() {
		return anual;
	}
	public void setAnual(boolean anual) {
		this.anual = anual;
	}
	
	/*
	 * Construtor
	 */
	public OrcDespesaBean() {
		this.mesAno = new MesAnoOrcamento();
		
	}//contrutor

	/*
	 * Métodos
	 */		
	
	public void listarDespesasOrcamento() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			this.listaDespesa = new OrcDespesaService(con).listarProjecaoContas(this.mesAno);			
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}
	
	
	public void mesAnterior() {
		int anoAux = this.mesAno.getAno();
		this.mesAno.sub();
		if (this.mesAno.getAno() == anoAux)
			listarDespesasOrcamento();
		else
			this.mesAno.add();
	}

	public void mesPosterior() {
		int anoAux = this.mesAno.getAno();
		this.mesAno.add();
		if (this.mesAno.getAno() == anoAux)
			listarDespesasOrcamento();
		else
			this.mesAno.sub();
	}
	
	
	public void baixarPlanilhaEstrutura() {
		ConexaoDB con = null;
    	try {
			
			String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
			this.nomeXLS = "PLAN_Contas" + LibUtil.getUsuarioSessao().getLogUsuario()  +".xls";
			caminho = caminho + "relatorios/" + this.nomeXLS;
			
			new OrcDespesaService(con).gerarPlanilhaEstrutura(caminho, this.listaDespesa, this.mesAno);
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} 
	}
	
	public StreamedContent getFile() throws FileNotFoundException {
		String caminhoWebInf = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
		caminhoWebInf = caminhoWebInf + "relatorios/" + this.nomeXLS;
		InputStream stream = new FileInputStream(caminhoWebInf); //Caminho onde esta salvo o arquivo.
        
        file = DefaultStreamedContent.builder()
                .name(this.nomeXLS)
                .contentType("application/xls")
                .stream(() -> stream)
                .build();

        return file;  
    } 
	
}
