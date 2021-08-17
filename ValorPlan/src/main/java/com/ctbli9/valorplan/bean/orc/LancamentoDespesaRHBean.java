package com.ctbli9.valorplan.bean.orc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.orc.ValorTotalMes;
import com.ctbli9.valorplan.modelo.orc.OrcamentoDespesa;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.negocio.orc.OrcDespesaService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.enumeradores.StatusPlano;
import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

@ManagedBean(name="lancDespesaRHBean")
@ViewScoped
public class LancamentoDespesaRHBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Recurso funcionario = new Recurso(new MesAnoOrcamento());
	private List<OrcamentoDespesa> listaOrcamentoDespesa = new ArrayList<OrcamentoDespesa>();
	private OrcamentoDespesa despesa;
	private List<ValorTotalMes> valores;
	
	private FacesMessages messages = new FacesMessages();
	
	public Recurso getFuncionario() {
		return funcionario;
	}
	
	public List<OrcamentoDespesa> getListaOrcamentoDespesa() {
		return listaOrcamentoDespesa;
	}
	
	public OrcamentoDespesa getDespesa() {
		return despesa;
	}
	public void setDespesa(OrcamentoDespesa despesa) {
		this.despesa = despesa;
	}
	
	public List<ValorTotalMes> getValores() {
		return valores;
	}
	public void setValores(List<ValorTotalMes> valores) {
		this.valores = valores;
	}
	
	public boolean isInativo() {
		return this.valores == null || this.valores.size() == 0 ||
				!PlanoServiceDAO.getPlanoSelecionado().getStatus().equals(StatusPlano._0);
	}

	/*
	 * Métodos
	 */
	
	public void limpaDespesa(TreeNode elo) {
		this.funcionario = new Recurso(new MesAnoOrcamento());
		this.despesa = null;
		this.listaOrcamentoDespesa = new ArrayList<OrcamentoDespesa>();
		listaValores();
		
		if(elo != null) {
			/*if (elo.getData().getClass().getName().indexOf("Departamento") > -1) {
				Departamento depto = (Departamento) elo.getData(); 
				this.funcionario = depto.getResponsavel();
				listarDespesasOrcamento();
			}
			
			if (elo.getData().getClass().getName().indexOf("CentroCusto") > -1) {
				CentroCusto setor = (CentroCusto) elo.getData(); 
				this.funcionario = setor.getResponsavel();
				listarDespesasOrcamento();
			}*/
			
			if (elo.getData().getClass().getName().indexOf("Recurso") > -1) {
				this.funcionario = (Recurso) elo.getData(); 
				listarDespesasOrcamento();
			}
		}
	}
	
	public void listarDespesasOrcamento() {
		ConexaoDB con = null;
		try {
    		con = new ConexaoDB();
	    	
    		this.listaOrcamentoDespesa = new OrcDespesaService(con).listarDespesaRH(this.funcionario);
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void listaValores() {
		if (this.despesa == null) {
			this.valores = null;
		} else {
			ConexaoDB con = null;
			try {
	    		con = new ConexaoDB();
		    	this.valores = new OrcDespesaService(con).listarOrcDespesaRH(this.funcionario, this.despesa.getConta());
				
				messages.info("Orçamento gravado com sucesso.");
				
			} catch (Exception e) {
				Global.erro(con, e, messages, null);
			} finally {
				ConexaoDB.close(con);
			}
		}
	}

	public void gravarOrcamento(ActionEvent event) {
		ConexaoDB con = null;
		try {
    		con = new ConexaoDB();
	    	new OrcDespesaService(con).gravarOrcDespesaRH(this.funcionario, this.despesa, this.valores);
			
			messages.info("Orçamento gravado com sucesso.");
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}

}
