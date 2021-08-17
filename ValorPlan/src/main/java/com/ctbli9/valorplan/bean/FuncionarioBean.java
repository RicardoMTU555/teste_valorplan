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
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.FiltroCentroCusto;
import com.ctbli9.valorplan.modelo.FiltroFuncionario;
import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.negocio.CargoService;
import com.ctbli9.valorplan.negocio.CentroCustoService;
import com.ctbli9.valorplan.negocio.FuncionarioService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.enumeradores.AreaSetor;
import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.LibUtilFaces;

@ManagedBean(name="funcionarioBean")
@ViewScoped
public class FuncionarioBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<CentroCusto> listaCentroCusto;
	private List<Funcionario> funcionarios = new ArrayList<Funcionario>();
	private Funcionario funcionario;
	private List<Cargo> cargos;
	
	private FiltroFuncionario filtro;
	
	private FacesMessages msg;

	/*
	 * Construtor
	 */
	public FuncionarioBean() {
		msg = new FacesMessages();
		filtro = new FiltroFuncionario();
		
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			FiltroCentroCusto filtroCencus = new FiltroCentroCusto();
			this.listaCentroCusto = new CentroCustoService(con).listar(filtroCencus);
				
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			
		} finally {
			ConexaoDB.close(con);		
		} 
	}

	/*
	 * Atributos
	 */
	public List<CentroCusto> getListaCentroCusto() {
		return listaCentroCusto;
	}
	public List<Funcionario> getFuncionarios() {
		return funcionarios;
	}
	public void setFuncionarios(List<Funcionario> funcionarios) {
		this.funcionarios = funcionarios;
	}
	
	public Funcionario getFuncionario() {
		return funcionario;
	}
	public void setFuncionario(Funcionario funcionario) {
		this.funcionario = funcionario;
	}
	
	public List<Cargo> getCargos() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			if (this.cargos == null) {
				this.cargos = new CargoService(con).listar();
			}
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			
		} finally {
			ConexaoDB.close(con);		
		} 
		
		return this.cargos;
	}
	
	public AreaSetor[] getAreas() {
		return AreaSetor.values();
	}
	
	public FiltroFuncionario getFiltro() {
		return filtro;
	}
	public void setFiltro(FiltroFuncionario filtro) {
		this.filtro = filtro;
	}
	
	/*
	 * Metodos
	 */	
	public void inicializarRegistro() {
		funcionario = new Funcionario();
		funcionario.setFunIdAtivo(true);
	}
		
	public boolean isItemSelecionado() {
		return funcionario != null && funcionario.getCdFuncionario() != 0;
	}

	
	public void listarFiltro() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.funcionarios = new FuncionarioService(con.getConexao()).listar(this.filtro);
			this.cargos = null;
			
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
			new FuncionarioService(con.getConexao()).salvar(funcionario);
			ConexaoDB.gravarTransacao(con);
			
			msg.info("Funcionário gravado com sucesso!");
			
			listarFiltro();
			
			LibUtilFaces.atualizarView("frm:funcionarioDataTable", "frm:messages");
			
		} catch (Exception e) {
			FacesContext.getCurrentInstance().validationFailed();
			Global.erro(con, e, msg, null);
			
		} finally {
			ConexaoDB.close(con);		
		} 
	}//end salvar
	
	
	public void excluir(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			new FuncionarioService(con.getConexao()).excluir(funcionario);
			ConexaoDB.gravarTransacao(con);
			
			listarFiltro();
			
			msg.info("Funcionário excluído!");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 
	}//excluir	
}
