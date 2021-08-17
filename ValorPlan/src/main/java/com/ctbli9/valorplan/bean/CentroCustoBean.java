package com.ctbli9.valorplan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.enumeradores.TipoRecurso;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Departamento;
import com.ctbli9.valorplan.modelo.FiltroCentroCusto;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;
import com.ctbli9.valorplan.negocio.CategoriaReceitaService;
import com.ctbli9.valorplan.negocio.CentroCustoService;
import com.ctbli9.valorplan.negocio.DepartamentoService;
import com.ctbli9.valorplan.negocio.EquipeService;
import com.ctbli9.valorplan.negocio.FilialService;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.LibUtilFaces;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.enumeradores.AreaSetor;
import ctbli9.modelo.Filial;
import ctbli9.recursos.FacesMessages;

@ManagedBean(name="cencusBean")
@ViewScoped
public class CentroCustoBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<CentroCusto> listaCentroCusto;
	private String[] origensReceita = new String[0];
	private List<CategoriaReceita> listaTiposReceita;
	private List<Recurso> listaRecurso;
	private CentroCusto centroCusto;
	private List<Filial> filiais;
	
	private FiltroCentroCusto filtro;
	private Departamento filtroDepartamento; 
    private MesAnoOrcamento anoReferencia = new MesAnoOrcamento();
	
	private FacesMessages msg;

	/*
	 * Construtor
	 */	
	public CentroCustoBean() {
		this.msg = new FacesMessages();
		this.filtroDepartamento = new Departamento();
		this.filtro = new FiltroCentroCusto();
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			filiais = new FilialService(con).listar();
    		this.listaTiposReceita = new CategoriaReceitaService(con).listarCategoriaReceita();
    		this.listaRecurso = new EquipeService(con.getConexao()).listarRecursos(TipoRecurso.G);
			
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
	
	public String[] getOrigensReceita() {
		return origensReceita;
	}
	public void setOrigensReceita(String[] origensReceita) {
		this.origensReceita = origensReceita;
	}
	
	public List<CategoriaReceita> getListaTiposReceita() {
		return listaTiposReceita;
	}
	public List<Recurso> getListaRecurso() {
		return listaRecurso;
	}
	
	public CentroCusto getCentroCusto() {
		return centroCusto;
	}
	public void setCentroCusto(CentroCusto centroCusto) {
		this.centroCusto = centroCusto;
	}
	
	public List<Filial> getFiliais() {
		return filiais;
	}
	
	public FiltroCentroCusto getFiltro() {
		return filtro;
	}
	public void setFiltro(FiltroCentroCusto filtro) {
		this.filtro = filtro;
	}
	
	public Departamento getFiltroDepartamento() {
		return filtroDepartamento;
	}
	public void setFiltroDepartamento(Departamento filtroDepartamento) {
		this.filtroDepartamento = filtroDepartamento;
	}
	
	public MesAnoOrcamento getAnoReferencia() {
		return anoReferencia;
	}
	public void setAnoReferencia(MesAnoOrcamento anoReferencia) {
		this.anoReferencia = anoReferencia;
	}
	
	public AreaSetor[] getTiposArea() {
		return AreaSetor.values();
	}
	
	public boolean isItemSelecionado() {
		return centroCusto != null && centroCusto.getCdCentroCusto() != 0;
	}
	
	/*
	 * Metodos
	 */
	
	public void limparTela() {
		this.listaCentroCusto = null;
		this.centroCusto = null;
	}
	
	public void inicializarRegistro() {
		this.centroCusto = new CentroCusto();
	}
	
	public void alterarRegistro() {
		this.origensReceita = new String[this.centroCusto.getTipos().size()];
		
		for (int i = 0; i < this.origensReceita.length; i++) {
			origensReceita[i] = Integer.toString(this.centroCusto.getTipos().get(i).getCdCategoria());
		}

	}
	
	public void listarCentroCusto() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.listaCentroCusto = new CentroCustoService(con).listar(filtro);
				
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}//listarCentroCusto
	
	
	public void associarDepartamento(long codDepto) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.centroCusto.setDepartamento(new DepartamentoService(con).pesquisarDepartamento(codDepto));
			FacesContext.getCurrentInstance().validationFailed();
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			LibUtilFaces.atualizarView("frm:messages");
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void salvarCentroCustoDepartamento(long codDepto) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.centroCusto.setDepartamento(new DepartamentoService(con).pesquisarDepartamento(codDepto));
			new CentroCustoService(con).salvar(this.centroCusto);
			ConexaoDB.gravarTransacao(con);
			
			LibUtilFaces.atualizarView("frm:messages", "frm:cadDataTable");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			LibUtilFaces.atualizarView("frm:messages");
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	
	public void salvar(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			this.centroCusto.setTipos(new ArrayList<CategoriaReceita>());
			
			for (int i = 0; i < this.origensReceita.length; i++) {
				CategoriaReceita tipRec = new CategoriaReceita();
				tipRec.setCdCategoria(Integer.parseInt(this.origensReceita[i]));
				this.centroCusto.getTipos().add(tipRec);
			}
			
			new CentroCustoService(con).salvar(this.centroCusto);
			ConexaoDB.gravarTransacao(con);
			
			msg.info("Centro de custo gravado com sucesso!");
			
			listarCentroCusto();
			
			LibUtilFaces.atualizarView("frm:cadDataTable", "frm:messages");
		
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			FacesContext.getCurrentInstance().validationFailed();
		} finally {
			ConexaoDB.close(con);
		}
	}//end salvar
	
	
	public void excluir(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			new CentroCustoService(con).excluir(centroCusto);
			ConexaoDB.gravarTransacao(con);
			
			listarCentroCusto();
			
			msg.info("Centro de Custo excluído!");
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}//excluir	
}
