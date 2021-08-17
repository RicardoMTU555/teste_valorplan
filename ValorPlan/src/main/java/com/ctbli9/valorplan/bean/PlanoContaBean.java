package com.ctbli9.valorplan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.negocio.ContaContabilService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.enumeradores.GrupoContaContabil;
import ctbli9.enumeradores.NatContaContabil;
import ctbli9.enumeradores.TipoConta;
import ctbli9.modelo.FiltroContaContabil;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.LibUtilFaces;
import ctbli9.recursos.RegraNegocioException;


@ManagedBean(name="placonBean")
@ViewScoped
public class PlanoContaBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private FiltroContaContabil filtro;
	private List<ContaContabil> listaPlacon = new ArrayList<ContaContabil>();
	private List<ContaContabil> listaPlaconFiltrado = new ArrayList<ContaContabil>();
	private ContaContabil conta;
	private boolean nova;
	
	private FacesMessages msg;
	
	// Contrutor
	public PlanoContaBean() {
		msg = new FacesMessages();
		filtro = new FiltroContaContabil();
	}
	
	public FiltroContaContabil getFiltro() {
		return filtro;
	}
	public void setFiltro(FiltroContaContabil filtro) {
		this.filtro = filtro;
	}
	public List<ContaContabil> getListaPlacon() {
		return listaPlacon;
	}
	
	public List<ContaContabil> getListaPlaconFiltrado() {
		return listaPlaconFiltrado;
	}
	public void setListaPlaconFiltrado(List<ContaContabil> listaPlaconFiltrado) {
		this.listaPlaconFiltrado = listaPlaconFiltrado;
	}

	public ContaContabil getConta() {
		return conta;
	}
	public void setConta(ContaContabil conta) {
		this.conta = conta;
	}

	public boolean isNova() {
		return nova;
	}
	
	public NatContaContabil[] getNaturezas() {
		return NatContaContabil.values();
	}
		
	public GrupoContaContabil[] getGrupos() {
		return GrupoContaContabil.values();
	}

	public TipoConta[] getTiposConta() {
		return TipoConta.values();
	}
	
	public void inicializarRegistro() {
		conta = new ContaContabil();
		conta.setDsConta(" ");
		this.nova = true;
	}

	public void alterarRegistro() {
		this.nova = false;
	}
	
	public boolean isItemSelecionado() {
		return conta != null && !conta.getCdConta().isEmpty();
	}
	
	public void listar() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.listaPlacon = new ContaContabilService(con).listar(this.filtro);
			this.listaPlaconFiltrado = this.listaPlacon;
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			
		} finally {
			ConexaoDB.close(con);	

		}	
	}
	
	public void salvar(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			if (this.nova) {
				if (new ContaContabilService(con).existe(this.conta.getCdConta()))
					throw new RegraNegocioException("Conta " + this.conta.getCdConta() + " já existe.");
			}
				
			new ContaContabilService(con).salvar(conta);
			msg.info("Conta Contábil gravada com sucesso!");
			ConexaoDB.gravarTransacao(con);
			listar();
			
			if (nova)
				this.conta = null;
			
			LibUtilFaces.atualizarView("frm:placonDataTable", "frm:placonToolbar", "frm:messages");

		} catch (RegraNegocioException e) {
			Global.erro(con, e, msg, null);
			FacesContext.getCurrentInstance().validationFailed();
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			this.msg.erro(e.getMessage());
		} finally {
			ConexaoDB.close(con);			
		}
		
	}//end salvar
	
	
	public void excluir(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new ContaContabilService(con).excluir(conta);
			conta = null;
			ConexaoDB.gravarTransacao(con);
			
			listar();
			
			msg.info("Conta excluída!");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
		
	}//excluir	
}
