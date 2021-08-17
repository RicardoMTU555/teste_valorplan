package com.ctbli9.valorplan.bean.adm;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletContext;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.negocio.PlanoService;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.enumeradores.StatusPlano;
import ctbli9.modelo.Plano;
import ctbli9.recursos.DataUtil;
import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.LibUtil;
import com.ctbli9.valorplan.recursos.LibUtilFaces;

@ManagedBean(name="planoBean")
@SessionScoped
public class PlanoBean implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Plano cenarioSelecionado;
	private List<Plano> listaPlanos;
	
	private MesAnoOrcamento mesAno = new MesAnoOrcamento();
    private List<String> mesesAbertos = new ArrayList<>();
	
	private Plano planoSel;
	private Plano planoOrigem;
	private String termoPesquisa;
	private String mensagem;
	
	FacesMessages messages = new FacesMessages();

	public Plano getCenarioSelecionado() {
		return cenarioSelecionado;
	}
	public void setCenarioSelecionado(Plano cenarioSelecionado) {
		this.cenarioSelecionado = cenarioSelecionado;
	}
	public List<Plano> getListaPlanos() {
		return listaPlanos;
	}
	
	public MesAnoOrcamento getMesAno() {
		return mesAno;
	}
	
    public List<String> getMesesAbertos() {
		return mesesAbertos;
	}
    public void setMesesAbertos(List<String> mesesAbertos) {
		this.mesesAbertos = mesesAbertos;
	}
	
	public Plano getPlanoSel() {
		return planoSel;
	}
	public void setPlanoSel(Plano plano) {
		this.planoSel = plano;
	}
	
	public Plano getPlanoOrigem() {
		return planoOrigem;
	}
	public void setPlanoOrigem(Plano planoOrigem) {
		this.planoOrigem = planoOrigem;
	}
	
	public String getTermoPesquisa() {
		return termoPesquisa;
	}
	public void setTermoPesquisa(String termoPesquisa) {
		this.termoPesquisa = termoPesquisa;
	}
	public String getMensagem() {
		return mensagem;
	}
	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}
	
	public StatusPlano[] getListaStatus() {
		return StatusPlano.values();
	}

	/*
	 * Construtor
	 */
	public PlanoBean() {
		this.mensagem = "";
		listar(true);
		
		if (this.cenarioSelecionado == null && this.listaPlanos != null && this.listaPlanos.size() > 0) {
			this.cenarioSelecionado = this.listaPlanos.get(0);
			for (Plano plano : this.listaPlanos) {
				if (plano.getStatus().compareTo(this.cenarioSelecionado.getStatus()) < 0 ) {
					this.cenarioSelecionado = plano;
				}					
			}
		}
		
		if (this.cenarioSelecionado == null) {
			this.cenarioSelecionado = new Plano();
			this.cenarioSelecionado.setNrAno(DataUtil.getAno());
			this.cenarioSelecionado.setMesAberto(new char[] {'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S'});
		}
		
		LibUtil.setParametroSessao("PLANO_ORCAMENTO", this.cenarioSelecionado);
		
	}

	/*
	 * Métodos
	 */
	public void listar(boolean verificar) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			 
			if (!verificar || (verificar && this.listaPlanos == null))
				this.listaPlanos = new PlanoService(con).listar();
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}//iniciar

	public void selecionarPlano() {
		LibUtil.setParametroSessao("PLANO_ORCAMENTO", this.cenarioSelecionado);
		
		ServletContext sc = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		String raiz = sc.getRealPath("/");
		if (raiz.endsWith("\\")) {
			raiz = raiz.substring(0, raiz.length() - 1);
			raiz = raiz.substring(raiz.lastIndexOf("\\") + 1);

		} else if (raiz.endsWith("/")) {
			raiz = raiz.substring(0, raiz.length() - 1);
			raiz = raiz.substring(raiz.lastIndexOf("/") + 1);
		}
		
		
		String URL = "/" + raiz.toLowerCase() + "/home.xhtml";
		
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(URL);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void inicializarRegistro() {
		this.planoSel = new Plano();
		this.planoSel.setMesAberto(new char[] {'N','N','N','N','N','N','N','N','N','N','N','N'});
		this.planoSel.setStatus(StatusPlano._0);
		this.mesesAbertos = new ArrayList<>();
		for (int i = 0; i < this.mesAno.getMeses().size(); i++) {
			this.mesesAbertos.add(this.mesAno.getMeses().get(i).getValue().toString());
		}
		
	}
	
	public void prepararAlteracao() {
		this.mesesAbertos = new ArrayList<>();
		for (int i = 0; i < this.mesAno.getMeses().size(); i++) {
			if (this.planoSel.getMesAberto()[i] == 'S') {
				this.mesesAbertos.add(mesAno.getMeses().get(i).getValue().toString());
			}			
		}
	}	
	
	public boolean isItemSelecionado() {
		return this.planoSel != null && this.planoSel.getCdPlano() != 0;
	}
		
	public void salvar(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			// Desliga todos os meses
			for (int i = 0; i < this.mesAno.getMeses().size(); i++) {
				this.planoSel.getMesAberto()[i] = 'N';
			}

			for (String nroMes : this.mesesAbertos) {
				this.planoSel.getMesAberto()[Integer.parseInt(nroMes)-1] = 'S';
			}
			
			// Religa os válidos
			
			new PlanoService(con).salvar(this.planoSel);
			ConexaoDB.gravarTransacao(con);
			
			messages.info("Plano gravado com sucesso!");
			
			listar(false);
			
			LibUtilFaces.atualizarView("frm:cadDataTable", "frm:messages");
						
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}//end salvar
	
	
	public void excluir(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			new PlanoService(con).excluir(this.planoSel);
			ConexaoDB.gravarTransacao(con);
			
			messages.info("Plano excluído!");
			listar(false);
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
		} finally {
			ConexaoDB.close(con);
		}
	}//excluir
	
	public void prepararValidacao() {
		this.mensagem = "";
		this.cenarioSelecionado = null;
	}
	
	public boolean isValido() {
		return this.planoOrigem != null && this.planoOrigem.getCdPlano() > 0 && this.mensagem.isEmpty();
	}
	
	public void validarImportacao() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			if (this.planoOrigem != null && this.planoOrigem.getCdPlano() > 0)
				new PlanoService(con).validarImportacao(this.planoOrigem, this.planoSel);
			this.mensagem = "";
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}	
	}
	
	public void executarImportacao() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			new PlanoService(con).executarImportacao(this.planoOrigem, this.planoSel);
			ConexaoDB.gravarTransacao(con);
			
			messages.info("Plano importado com sucesso");
			
			LibUtilFaces.atualizarView("frm:messages");
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
			LibUtilFaces.atualizarView("frm:messages");
		} finally {
			ConexaoDB.close(con);
		}	
	}

}
