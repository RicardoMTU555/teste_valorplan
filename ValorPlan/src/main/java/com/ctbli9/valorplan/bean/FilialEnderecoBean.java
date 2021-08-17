package com.ctbli9.valorplan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.negocio.FilialService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.enumeradores.TipoEndereco;
import ctbli9.enumeradores.UF;
import ctbli9.modelo.Bairro;
import ctbli9.modelo.Cidade;
import ctbli9.modelo.Filial;
import ctbli9.modelo.FilialEndereco;
import ctbli9.negocio.CidadeService;
import ctbli9.recursos.FacesMessages;

@ManagedBean(name="filialEnderecoBean")
@ViewScoped
public class FilialEnderecoBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Filial filial;
	private FilialEndereco endereco;
	private UF uf;
	private Cidade cidade;
	
	private List<Cidade> cidadesUF = new ArrayList<Cidade>();
	private List<Bairro> bairrosCidade = new ArrayList<Bairro>();
	
	private FacesMessages msg;

	public FilialEnderecoBean() {
		msg = new FacesMessages();
		this.endereco = new FilialEndereco();
	}

	/*
	 * ATRIBUTOS
	 */	
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

	public TipoEndereco[] getTiposEndereco() {
		return TipoEndereco.values();
	}

	public UF[] getEstadosBrasileiros() {
		return UF.values();
	}

	/*
	 * METODOS
	 */

	public void recebeParametroFilial() {
    	HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    	int cdFilial = 0;
    	
    	try {
    		cdFilial = Integer.parseInt(request.getParameter("filial"));
		} catch (Exception e) {
			cdFilial = 0;
		}

    	if (cdFilial > 0) {
    		ConexaoDB con = null;
    		try {
    			con = new ConexaoDB();
				this.filial = new FilialService(con).pesquisar(cdFilial);
				
			} catch (Exception e) {
				Global.erro(con, e, msg, null);
			} finally {
				ConexaoDB.close(con);
			}
    	}

	}
	
	public void inicializarRegistro() {
		filial = new Filial();
	}

	public boolean isItemSelecionado() {
		return filial != null && filial.getCdFilial() != 0;
	}

	public void reinicializa() {
		if (this.endereco != null) {	
			this.uf = this.endereco.getBairro().getCidade().getEstado();
			listarCidadesUF();
			this.cidade = this.endereco.getBairro().getCidade();
			listarBairrosCidade();
		} else {
			this.endereco = new FilialEndereco();
		}
		
	}
	
    public void listarCidadesUF() {
    	ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.cidadesUF = new CidadeService(con.getConexao()).listar("", this.uf.toString());
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 
    }
    
    public void listarBairrosCidade() {
    	ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.bairrosCidade = new CidadeService(con.getConexao()).listarBairro(this.cidade.getCdCidade());
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 
    }	
	
	public void salvarEndereco() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			new FilialService(con).salvarEndereco(this.filial, this.endereco);
			
			msg.info("Endereço salvo com sucesso!");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}//end salvarEndereco

}
