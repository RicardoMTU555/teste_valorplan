package com.ctbli9.valorplan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.negocio.FilialService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.enumeradores.TipoEndereco;
import ctbli9.enumeradores.UF;
import ctbli9.modelo.Bairro;
import ctbli9.modelo.Cidade;
import ctbli9.modelo.Filial;
import ctbli9.modelo.FilialEndereco;
import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.LibUtilFaces;


@ManagedBean(name="filialBean")
@ViewScoped
public class FilialBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<Filial> listaFilial = new ArrayList<Filial>();
	private Filial filial;
	private FilialEndereco endereco;
	private String termoPesquisa;
	private UF uf;
	private Cidade cidade;
	
	private List<Cidade> cidadesUF = new ArrayList<Cidade>();
	private List<Bairro> bairrosCidade = new ArrayList<Bairro>();
	
	private boolean nova;

	private FacesMessages msg;

	public FilialBean() {
		msg = new FacesMessages();
	}

	/*
	 * ATRIBUTOS
	 */	
	public List<Filial> getListaFilial() {
		return listaFilial;
	}
	
	public Filial getFilial() {
		return filial;
	}
	public void setFilial(Filial filial) {
		this.filial = filial;
	}
	
	public FilialEndereco getEndereco() {
		return endereco;
	}
	public void setEndereco(FilialEndereco endereco) {
		this.endereco = endereco;
	}
	
	public String getTermoPesquisa() {
		return termoPesquisa;
	}
	public void setTermoPesquisa(String termoPesquisa) {
		this.termoPesquisa = termoPesquisa;
	}
	
	public UF getUf() {
		return uf;
	}
	public void setUf(UF uf) {
		this.uf = uf;
	}
	
	public Cidade getCidade() {
		return cidade;
	}
	public void setCidade(Cidade cidade) {
		this.cidade = cidade;
	}
	
	public List<Cidade> getCidadesUF() {
		return cidadesUF;
	}
	
	public List<Bairro> getBairrosCidade() {
		return bairrosCidade;
	}

	public boolean isNova() {
		return nova;
	}

	public TipoEndereco[] getTiposEndereco() {
		return TipoEndereco.values();
	}

	public UF[] getEstadosBrasileiros() {
		return UF.values();
	}

	/*
	 * METODOS
	 */

	public void inicializarRegistro() {
		filial = new Filial();
		this.nova = true;
	}

	public void alterarRegistro() {
		this.nova = false;
	}

	public boolean isItemSelecionado() {
		return filial != null && filial.getCdFilial() != 0;
	}

    public void listarCidadesUF() {
    	// TODO Reginaldo: unificar esse metodo
    	//this.cidadesUF = new EnderecoService().listarCidadesUF(this.uf.toString());
    }
    
    public void listarBairrosCidade() {
    	// TODO Reginaldo: unificar esse metodo
    	//this.bairrosCidade = new EnderecoService().listarBairrosCidade(this.cidade.getCdCidade());
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
			
			this.listaFilial = new FilialService(con).listar();
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void listarFiltro() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			this.listaFilial = new FilialService(con).listar(this.termoPesquisa);
			
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
			
			if (this.nova)
				new FilialService(con).incluir(filial);
			else
				new FilialService(con).alterar(filial);
			ConexaoDB.gravarTransacao(con);
			
			msg.info("Filial gravada com sucesso!");
			
			atualizarRegistros();
			
			LibUtilFaces.atualizarView("frm:filialDataTable", "frm:messages");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
			else
				LibUtilFaces.atualizarView("frm:messages");
			
		} finally {
			ConexaoDB.close(con);
		}		
	}//end salvar
	
	
	public void excluir(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new FilialService(con).excluir(filial);
			ConexaoDB.gravarTransacao(con);
			
			atualizarRegistros();
			
			msg.info("Filial excluída!");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
			
		} finally {
			ConexaoDB.close(con);
		}
	}//excluir
	
	
	public String irParaEndereco() {
		return "cadFilialEndereco?filial=" + this.filial.getCdFilial() 
				+ "&faces-redirect=true";
	}
	
	public void salvarEndereco() {
		// TODO Reginaldo: desenvolver o metodo
	}
}
