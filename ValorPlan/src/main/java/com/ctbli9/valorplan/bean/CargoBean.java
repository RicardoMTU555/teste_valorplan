package com.ctbli9.valorplan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.Cargo;
import com.ctbli9.valorplan.negocio.CargoService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.enumeradores.TipoCargo;
import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.LibUtilFaces;

@ManagedBean(name="cargoBean")
@ViewScoped
public class CargoBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<Cargo> cargos = new ArrayList<Cargo>();
	private Cargo cargo;
	private String termoPesquisa;
	
	private FacesMessages messages;

	public CargoBean() {
		messages = new FacesMessages();
	}
	
	/*
	 * get e set dos Atributos
	 */
	public List<Cargo> getCargos() {
		return cargos;
	}
	public void setCargos(List<Cargo> cargos) {
		this.cargos = cargos;
	}
	public Cargo getCargo() {
		return cargo;
	}
	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}
	public String getTermoPesquisa() {
		return termoPesquisa;
	}
	public void setTermoPesquisa(String termoPesquisa) {
		this.termoPesquisa = termoPesquisa;
	}
	
	public TipoCargo[] getTiposCargo() {
		return TipoCargo.values();
	}

	/*
	 * METODOS
	 */
	
	public void inicializarRegistro() {
		cargo = new Cargo();
	}
	
	public boolean isItemSelecionado() {
		return cargo != null && cargo.getCdCargo() != 0;
	}
	
	public void atualizarRegistros() {
		if (jaHouvePesquisa()) {
			listarFiltro();
		} else {
			listarTudo();
		}
	}
	
	private boolean jaHouvePesquisa() {
		return termoPesquisa != null && !"".equals(termoPesquisa);
	}

	public void listarTudo() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			this.setCargos(new CargoService(con).listar());
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}

	public void listarFiltro() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			this.setCargos(new CargoService(con).pesquisar(this.termoPesquisa));
			
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
			
			new CargoService(con).salvar(cargo);
			ConexaoDB.gravarTransacao(con);
			
			messages.info("Cargo gravado com sucesso!");
			
			atualizarRegistros();
			
			LibUtilFaces.atualizarView("frm:cadDataTable", "frm:messages");
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
		} finally {
			ConexaoDB.close(con);
		}
	}//end salvar
	
	
	public void excluir(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new CargoService(con).excluir(cargo);
			ConexaoDB.gravarTransacao(con);
			
			messages.info("Cargo excluído!");
			
			atualizarRegistros();
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
		} finally {
			ConexaoDB.close(con);
		}
	}//excluir	
}
