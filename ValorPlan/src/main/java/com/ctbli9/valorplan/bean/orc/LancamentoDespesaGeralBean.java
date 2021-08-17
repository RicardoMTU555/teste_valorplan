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
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.orc.ValorTotalMes;
import com.ctbli9.valorplan.negocio.orc.OrcDespesaService;
import com.ctbli9.valorplan.modelo.orc.OrcamentoDespesa;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.enumeradores.StatusPlano;
import ctbli9.recursos.FacesMessages;

@ManagedBean(name="lancDespesaGeralBean")
@ViewScoped
public class LancamentoDespesaGeralBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private CentroCusto cenCusto;
	private List<OrcamentoDespesa> listaDespesa = new ArrayList<OrcamentoDespesa>();
	private OrcamentoDespesa despesa;
	private List<ValorTotalMes> valores;
	private List<ValorTotalMes> valoresAnt;
	
	private ValorTotalMes totalAnterior;

	private FacesMessages messages = new FacesMessages();

	public CentroCusto getCenCusto() {
		return cenCusto;
	}
	public void setCenCusto(CentroCusto cenCusto) {
		this.cenCusto = cenCusto;
	}
	
	public List<OrcamentoDespesa> getListaDespesa() {
		return listaDespesa;
	}
	public List<ValorTotalMes> getValores() {
		return valores;
	}
	public void setValores(List<ValorTotalMes> valores) {
		this.valores = valores;
	}
	
	public List<ValorTotalMes> getValoresAnt() {
		return valoresAnt;
	}
	public ValorTotalMes getTotalAnterior() {
		return totalAnterior;
	}
	
	public OrcamentoDespesa getDespesa() {
		return despesa;
	}
	public void setDespesa(OrcamentoDespesa despesa) {
		this.despesa = despesa;
	}
	
	public boolean isInativo() {
		return listaDespesa == null || listaDespesa.isEmpty() ||
				!PlanoServiceDAO.getPlanoSelecionado().getStatus().equals(StatusPlano._0);
	}

	/*
	 * Métodos
	 */
	public void listarDespesaOrcamento() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
	    	
    		this.listaDespesa = new OrcDespesaService(con).listarDespesaGeral(this.cenCusto);
    		
		} catch (Exception e) {
			Global.erro(con, e, messages, null);			
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void limpaDespesa() {
		this.despesa = null;
		listaValores();
		listarDespesaOrcamento();
	}
	
	public void limpaDespesa(TreeNode elo) {
		this.cenCusto = new CentroCusto();
		this.despesa = null;
		this.listaDespesa = new ArrayList<OrcamentoDespesa>();
		listaValores();
		
		if(elo != null) {
			if (elo.getData().getClass().getName().indexOf("CentroCusto") > -1) {
				this.cenCusto = (CentroCusto) elo.getData(); 
				listarDespesaOrcamento();
			}
		}
	}
	
	
	public void listaValores() {
		if (this.despesa == null) {
			this.valores = null;
		} else {
			ConexaoDB con = null;
			try {
				con = new ConexaoDB();
		    	this.valores = new OrcDespesaService(con).listarOrcDespesaGeral(this.cenCusto, this.despesa.getConta());
				
		    	System.out.println(this.valores.get(0).getVrOrcado());
			} catch (Exception e) {
				Global.erro(con, e, messages, null);			
			} finally {
				ConexaoDB.close(con);
			}
		}
	}

	
	
	public void mostrarDespesaAnterior() {
		ConexaoDB con = null;
		try {
			
			int anoAnt = PlanoServiceDAO.getPlanoSelecionado().getNrAno() - 1;
			
			con = new ConexaoDB();
			this.valoresAnt = new OrcDespesaService(con).valoresHistoricos(this.cenCusto, this.despesa.getConta(),
					anoAnt);
			
			this.totalAnterior = new ValorTotalMes();
			
			for (ValorTotalMes item : this.valoresAnt) {
				this.totalAnterior.setVrOrcado(this.totalAnterior.getVrOrcado().add(item.getVrOrcado()));
				this.totalAnterior.setVrRealizado(this.totalAnterior.getVrRealizado().add(item.getVrRealizado()));
			}
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void gravarOrcamento(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
	    	new OrcDespesaService(con).gravarOrcDespesaGeral(this.cenCusto, this.despesa, this.valores);
	    	ConexaoDB.gravarTransacao(con);
	    	
			listarDespesaOrcamento();
			
			messages.info("Orçamento gravado com sucesso.");
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);			
		} finally {
			ConexaoDB.close(con);
		}
	}
}
