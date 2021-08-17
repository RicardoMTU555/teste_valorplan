package com.ctbli9.valorplan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.enumeradores.MedidaOrcamento;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;
import com.ctbli9.valorplan.negocio.CategoriaReceitaService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.enumeradores.TipoCargo;
import ctbli9.recursos.FacesMessages;

@ManagedBean(name="catReceitaBean")
@ViewScoped
public class CategoriaReceitaBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<CategoriaReceita> categorias = new ArrayList<CategoriaReceita>();
	private CategoriaReceita categoria;
	
	private FacesMessages messages;

	public CategoriaReceitaBean() {
		messages = new FacesMessages();		
	}
	
	/*
	 * get e set dos Atributos
	 */
	public List<CategoriaReceita> getCategorias() {
		return categorias;
	}
	public void setCategorias(List<CategoriaReceita> cargos) {
		this.categorias = cargos;
	}
	public CategoriaReceita getCategoria() {
		return categoria;
	}
	public void setCategoria(CategoriaReceita cargo) {
		this.categoria = cargo;
	}
	
	public TipoCargo[] getTiposCargo() {
		return TipoCargo.values();
	}

	public MedidaOrcamento[] getMedidasOrcamento() {
		return MedidaOrcamento.values();
	}

	/*
	 * METODOS
	 */
	
	public void novoRegistro() {
		this.categorias.add(0, new CategoriaReceita());
	}
	
	public boolean isItemSelecionado() {
		return categoria != null && categoria.getCdCategoria() != 0;
	}
	
	public void listarTudo() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.setCategorias(new CategoriaReceitaService(con).listarCategoriaReceita());
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}


	public void salvar(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new CategoriaReceitaService(con).salvar(this.categorias);
			ConexaoDB.gravarTransacao(con);
			
			messages.info("Cagetoria gravada com sucesso!");
			
			this.categoria = null;
			
			listarTudo();
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);

		} finally {
			ConexaoDB.close(con);
		}
	}//end salvar
	
	
	public void excluir() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new CategoriaReceitaService(con).excluir(categoria);
			ConexaoDB.gravarTransacao(con);
			
			int i = 0;
			for (CategoriaReceita categ : this.categorias) {
				if (categ.equals(this.categoria)) {
					this.categorias.remove(i);
					break;
				}
				i++;
			}
			
			messages.info("Categoria excluída!");
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
			
		} finally {
			ConexaoDB.close(con);		
		}
	}
	
}
