package com.ctbli9.valorplan.bean.orc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.enumeradores.TipoRecurso;
import com.ctbli9.valorplan.modelo.Cargo;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.FiltroFuncionario;
import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.negocio.CargoService;
import com.ctbli9.valorplan.negocio.EquipeService;
import com.ctbli9.valorplan.negocio.FuncionarioService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.LibUtilFaces;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

@ManagedBean(name="paramEquipeBean")
@ViewScoped
public class ParEquipeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String descrDepartamento;
	private List<Recurso> listaRecurso = new ArrayList<Recurso>();
	private Recurso recursoSel;
	private Funcionario[] funcionariosSelecionados;
	
	private List<Cargo> cargos;
	private List<Funcionario> funcionarios;
	private CentroCusto setor;
	
	private FacesMessages messages = new FacesMessages();
	private boolean visualizarTotal = false;
	
	public String getDescrDepartamento() {
		return descrDepartamento;
	}
	public void setDescrDepartamento(String descrDepartamento) {
		this.descrDepartamento = descrDepartamento;
	}
	public List<Recurso> getListaRecurso() {
		return listaRecurso;
	}
	public void setListaRecurso(List<Recurso> listaRecurso) {
		this.listaRecurso = listaRecurso;
	}
	
	public Recurso getRecursoSel() {
		return recursoSel;
	}
	public void setRecursoSel(Recurso recursoSel) {
		this.recursoSel = recursoSel;
	}
	
	/*public int[] getFuncionariosSelecionados() {
		return funcionariosSelecionados;
	}
	public void setFuncionariosSelecionados(int[] funcionariosSelecionados) {
		this.funcionariosSelecionados = funcionariosSelecionados;
	}*/
	
	public Funcionario[] getFuncionariosSelecionados() {
		return funcionariosSelecionados;
	}
	public void setFuncionariosSelecionados(Funcionario[] funcionariosSelecionados) {
		this.funcionariosSelecionados = funcionariosSelecionados;
	}
	
	public List<Funcionario> getFuncionarios() {
		return funcionarios;
	}

	public List<Cargo> getCargos() {
		return this.cargos;
	}
	
	public boolean isSetorSelecionado() {
		return this.setor != null;
	}
	
	public TipoRecurso[] getTiposRecurso() {
		return TipoRecurso.values();
	}
	
	
	/*
	 * Metodos
	 */
	
	public ParEquipeBean() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			if (this.cargos == null) {
				this.cargos = new CargoService(con).listar();
			}
			
    		FiltroFuncionario filtro = new FiltroFuncionario();
			this.funcionarios = new FuncionarioService(con.getConexao()).listar(filtro);
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
			
		} finally {
			ConexaoDB.close(con);		
		} 
	}
		
	public void inicializarRegistro() {
		if (this.setor == null) {
			messages.erro("Posicione em um centro de custo.");
			LibUtilFaces.atualizarView("frm:messages");
			FacesContext.getCurrentInstance().validationFailed();
		}
		
		this.recursoSel = new Recurso(new MesAnoOrcamento());
	}
	
	public void reiniciarLista() {
		if (this.setor == null) {
			messages.erro("Posicione em um centro de custo.");
			LibUtilFaces.atualizarView("frm:messages");
			FacesContext.getCurrentInstance().validationFailed();
		}

		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
    		FiltroFuncionario filtro = new FiltroFuncionario();
    		filtro.setCentroCusto(setor);
			this.funcionarios = new FuncionarioService(con.getConexao()).listar(filtro);
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
			
		} finally {
			ConexaoDB.close(con);		
		}
	}
	
	public void listarRecursos(TreeNode elo) {
		this.listaRecurso = new ArrayList<Recurso>();
		this.setor = null;
		
		if(elo != null) {
			if (elo.getData().getClass().getName().indexOf("CentroCusto") > -1) {
				this.setor = (CentroCusto) elo.getData(); 
				listarRecursosSetor();
			}
		}
	}
	
	private void listarRecursosSetor() {
		
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.listaRecurso = new EquipeService(con.getConexao()).lisarEquipeSetor(setor);
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public String mudarVisualizacao() {
		if (visualizarTotal) 
			this.listaRecurso = new ArrayList<Recurso>();
		else
			listarRecursosGeral();
		
		visualizarTotal = !visualizarTotal;
		return null;
	}
	
	public void listarRecursosGeral() {
		
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.listaRecurso = new EquipeService(con.getConexao()).lisarEquipeGeral();
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void limparRecurso() {
		this.listaRecurso = new ArrayList<Recurso>();
		this.setor = null;
	}

	public void salvar(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			this.recursoSel.setSetor(this.setor);
			this.recursoSel.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
			new EquipeService(con.getConexao()).salvar(this.recursoSel);
			ConexaoDB.gravarTransacao(con);
			
			messages.info("Recurso gravado com sucesso!");
			
			if (this.listaRecurso.indexOf(this.recursoSel) == -1)
				this.listaRecurso.add(0, this.recursoSel);
			
			LibUtilFaces.atualizarView("frm:recursoDT", "frm:messages");
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
			else
				LibUtilFaces.atualizarView("frm:messages");
			
		} finally {
			ConexaoDB.close(con);
		}		
	}//end salvar
	
	public void criarVinculo() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new EquipeService(con.getConexao()).vincularRecursos(this.setor, this.funcionariosSelecionados);
			ConexaoDB.gravarTransacao(con);
			listarRecursosSetor();
			
			messages.info("Recursos vinculados!");
			LibUtilFaces.atualizarView("frm:recursoDT", "frm:messages");
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
			
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void excluirRecurso() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new EquipeService(con.getConexao()).excluir(this.recursoSel);
			ConexaoDB.gravarTransacao(con);
			
			this.listaRecurso.remove(this.recursoSel);
			this.recursoSel = null;
			
			messages.info("Recurso removido com sucesso!");
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
			
		} finally {
			ConexaoDB.close(con);
		}
	}
	
}
