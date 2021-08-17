package com.ctbli9.valorplan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.enumeradores.UF;
import ctbli9.modelo.Bairro;
import ctbli9.modelo.Cidade;
import ctbli9.negocio.CidadeService;
import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.LibUtilFaces;

@ManagedBean(name="cidadeBean")
@ViewScoped
public class CidadeBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private ConexaoDB con = null;
	
	private List<Cidade> cidades = new ArrayList<Cidade>();
	private List<Bairro> bairros = new ArrayList<Bairro>();
	
	private Cidade cidade;
	private Bairro bairro;
	private String filtroNmCidade;
	private String filtroCdEstado;
	
	private FacesMessages msg;

	/*
	 * Construtor
	 */
	public CidadeBean() {
		msg = new FacesMessages();
	}

	/*
	 * Atributos
	 */
	public List<Cidade> getCidades() {
		return cidades;
	}
	
	public List<Bairro> getBairros() {
		return bairros;
	}

	public Cidade getCidade() {
		return cidade;
	}
	public void setCidade(Cidade cidade) {
		this.cidade = cidade;
	}
	
	public Bairro getBairro() {
		return bairro;
	}
	public void setBairro(Bairro bairro) {
		this.bairro = bairro;
	}
	
	public String getFiltroNmCidade() {
		return filtroNmCidade;
	}
	public void setFiltroNmCidade(String filtroNmCidade) {
		this.filtroNmCidade = filtroNmCidade;
	}
	
	public String getFiltroCdEstado() {
		return filtroCdEstado;
	}
	public void setFiltroCdEstado(String filtroCdEstado) {
		this.filtroCdEstado = filtroCdEstado;
	}

	public void inicializarRegistro() {
		cidade = new Cidade();
	}
	
	public boolean isItemSelecionado() {
		return cidade != null && cidade.getCdCidade() != 0;
	}

	public UF[] getEstadosBrasileiros() {
		return UF.values();
	}
	
	
	/*
	 * Operações
	 */
	public void atualizarRegistros() {
		if (jaHouvePesquisa()) {
			listarFiltro();
		} else {
			listarTudo();
		}
	}
	private boolean jaHouvePesquisa() {
		return filtroNmCidade != null && !"".equals(filtroNmCidade);
	}
	
	public void listarTudo() {
		try {
			con = new ConexaoDB();
			this.cidades = new CidadeService(con.getConexao()).listar();
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}
	
	public void listarFiltro() {
		try {
			con = new ConexaoDB();
			this.cidades = new CidadeService(con.getConexao()).listar(this.filtroNmCidade, this.filtroCdEstado);
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}
	
	public void salvar(ActionEvent event) {
		try {
			con = new ConexaoDB();
			new CidadeService(con.getConexao()).salvar(this.cidade);
			ConexaoDB.gravarTransacao(con);
			
			msg.info("Cidade gravada com sucesso!");
			
			this.cidade = null;
			
			atualizarRegistros();
			
			LibUtilFaces.atualizarView("frm:cidadeDataTable", "frm:cidadeToolbar", "frm:messages");
		
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
		} finally {
			ConexaoDB.close(con);
		} 
	}//end salvar
	
	public void excluir(ActionEvent event) {
		try {
			con = new ConexaoDB();
			new CidadeService(con.getConexao()).excluir(cidade);
			ConexaoDB.gravarTransacao(con);
			atualizarRegistros();
			
			this.cidade = null;

			msg.info("Cidade excluida!");
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}//excluir	

	/*
	 * Metodos de BAIRROS
	 */
	
	public void listarBairrosCidade() {
		try {
			con = new ConexaoDB();
			this.bairros = new CidadeService(con.getConexao()).listarBairro(cidade.getCdCidade());
			inicializarRegBairro();
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}//listarBairrosCidade
	
	public void inicializarRegBairro() {
		this.bairro = new Bairro();
		this.bairro.setCidade(this.cidade);
		
	}
	
	public void salvarBairro() {
		try {
			con = new ConexaoDB();
			new CidadeService(con.getConexao()).salvarBairro(this.bairro);
			ConexaoDB.gravarTransacao(con);
			
			msg.info("Bairro gravado com sucesso!");
			
			this.bairro = null;
			
			listarBairrosCidade();
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}//end salvarBairro

	public void excluirBairro() {
		try {
			con = new ConexaoDB();
			new CidadeService(con.getConexao()).excluirBairro(this.bairro);
			ConexaoDB.gravarTransacao(con);
			
			msg.info("Bairro removido!");
			
			this.bairro = null;
			
			listarBairrosCidade();
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}//end excluirBairro
}
