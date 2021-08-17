package com.ctbli9.valorplan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.CategImobilizado;
import com.ctbli9.valorplan.negocio.CategImobilizadoService;
import com.ctbli9.valorplan.negocio.ContaContabilService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.modelo.FiltroContaContabil;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.LibUtilFaces;

@ManagedBean(name="catImobiliBean")
@ViewScoped
public class CategoriaImobilizadoBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private List<CategImobilizado> listaCategImob = new ArrayList<CategImobilizado>();
	private List<ContaContabil> listaContas;
	private CategImobilizado CategImobilizado;
	private String termoPesquisa;
		
	private boolean nova;

	private FacesMessages msg = new FacesMessages();

	/*
	 * ATRIBUTOS
	 */	
	public List<CategImobilizado> getListaCategImob() {
		return listaCategImob;
	}
	
	public CategImobilizado getCategImobilizado() {
		return CategImobilizado;
	}
	public void setCategImobilizado(CategImobilizado CategImobilizado) {
		this.CategImobilizado = CategImobilizado;
	}
	
	public List<ContaContabil> getListaContas() {
		return listaContas;
	}
	
	public String getTermoPesquisa() {
		return termoPesquisa;
	}
	public void setTermoPesquisa(String termoPesquisa) {
		this.termoPesquisa = termoPesquisa;
	}
	
	public boolean isNova() {
		return nova;
	}

	/*
	 * Construtor
	 */
	
	public CategoriaImobilizadoBean() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			FiltroContaContabil filtro = new FiltroContaContabil();
			filtro.setGrupos(new String[] {"D", "C", "R"});
			this.listaContas = new ContaContabilService(con).listar(filtro);
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}
	
	/*
	 * METODOS
	 */

	public void inicializarRegistro() {
		CategImobilizado = new CategImobilizado();
		this.nova = true;
	}

	public void alterarRegistro() {
		this.nova = false;
	}

	public boolean isItemSelecionado() {
		return CategImobilizado != null && CategImobilizado.getCdCategImobili() != 0;
	}
    
	public void listarTudo() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			// TODO
			//this.listaCategImob = new CategImobilizadoService(con.getConexao()).listar();
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
			new CategImobilizadoService(con.getConexao()).salvar(this.CategImobilizado);
			ConexaoDB.gravarTransacao(con);
			
			msg.info("Categoria de Imobilizado gravada com sucesso!");
			
			listarTudo();
			
			LibUtilFaces.atualizarView("frm:cadDataTable", "frm:messages");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			LibUtilFaces.atualizarView("frm:messages");
		} finally {
			ConexaoDB.close(con);
		}
	}//end salvar
	
	
	public void excluir(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new CategImobilizadoService(con.getConexao()).excluir(this.CategImobilizado);
			ConexaoDB.gravarTransacao(con);
			
			this.CategImobilizado = null;
			
			listarTudo();
			
			msg.info("Categoria de Imobilizado excluída!");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}	
	}//excluir

}
