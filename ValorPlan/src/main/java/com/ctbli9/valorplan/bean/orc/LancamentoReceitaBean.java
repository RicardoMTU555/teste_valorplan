package com.ctbli9.valorplan.bean.orc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaAcum;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaMes;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.negocio.EquipeService;
import com.ctbli9.valorplan.negocio.orc.OrcReceitaService;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.enumeradores.StatusPlano;
import ctbli9.recursos.DataUtil;
import ctbli9.recursos.FacesMessages;

@ManagedBean(name="lancReceitaBean")
@ViewScoped
public class LancamentoReceitaBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Recurso vendedorFiltro;
	private List<Recurso> funcionarios;
	private List<Recurso> funcionariosFiltrados;
	private MesAnoOrcamento mesRef = new MesAnoOrcamento();
	
	private OrcamentoReceitaAcum receita;
	private List<OrcamentoReceitaAcum> listaOrcRec = new ArrayList<OrcamentoReceitaAcum>();
	private List<OrcamentoReceitaAcum> listaOrcRecFiltrado = new ArrayList<OrcamentoReceitaAcum>();

	private OrcamentoReceitaMes recMes;
	private List<OrcamentoReceitaMes> listaValores = new ArrayList<OrcamentoReceitaMes>();
	
	private FacesMessages messages = new FacesMessages();
	
	private StreamedContent file;
	private String nomeXLS;

	public Recurso getVendedorFiltro() {
		return vendedorFiltro;
	}
	public void setVendedorFiltro(Recurso vendedorFiltro) {
		this.vendedorFiltro = vendedorFiltro;
	}
	public MesAnoOrcamento getMesRef() {
		return mesRef;
	}
	
	public List<Recurso> getFuncionarios() {
		return funcionarios;
	}
	public List<Recurso> getFuncionariosFiltrados() {
		return funcionariosFiltrados;
	}
	public void setFuncionariosFiltrados(List<Recurso> funcionariosFiltrados) {
		this.funcionariosFiltrados = funcionariosFiltrados;
	}

	public OrcamentoReceitaAcum getReceita() {
		return receita;
	}
	public void setReceita(OrcamentoReceitaAcum receita) {
		this.receita = receita;
	}
	public List<OrcamentoReceitaAcum> getListaOrcRec() {
		return listaOrcRec;
	}
	public void setListaOrcRec(List<OrcamentoReceitaAcum> listaOrcRec) {
		this.listaOrcRec = listaOrcRec;
	}
	
	public List<OrcamentoReceitaAcum> getListaOrcRecFiltrado() {
		return listaOrcRecFiltrado;
	}
	public void setListaOrcRecFiltrado(List<OrcamentoReceitaAcum> listaOrcRecFiltrado) {
		this.listaOrcRecFiltrado = listaOrcRecFiltrado;
	}
	
	public OrcamentoReceitaMes getRecMes() {
		return recMes;
	}
	public void setRecMes(OrcamentoReceitaMes recMes) {
		this.recMes = recMes;
	}
	public List<OrcamentoReceitaMes> getListaValores() {
		return listaValores;
	}
	public void setListaValores(List<OrcamentoReceitaMes> listaValores) {
		this.listaValores = listaValores;
	}
	
	public boolean isInativo() {
		return listaOrcRec == null || listaOrcRec.isEmpty() ||
				!PlanoServiceDAO.getPlanoSelecionado().getStatus().equals(StatusPlano._0);
	}
	
	public boolean itemSemReceita(OrcamentoReceitaMes item) {
		return item.getValorTotal().compareTo(BigDecimal.ZERO) > 0 &&
				item.getPercDespesa().compareTo(BigDecimal.ZERO) == 0;
	}

	/*
	 * Metodos
	 */
	public void listarFuncionario() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.funcionarios = new EquipeService(con.getConexao()).listarRecursosPorGestor(
					Global.getFuncionarioLogado().getCdFuncionario(), this.mesRef, true);
			
			this.vendedorFiltro = null;				
			listarReceitaOrcamento();
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void listarReceitaOrcamento() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
						
			if (this.vendedorFiltro == null)
				this.listaOrcRec = new ArrayList<OrcamentoReceitaAcum>();
			else
				this.listaOrcRec = new OrcReceitaService(con).listarReceitasOrc(vendedorFiltro);
			
			this.listaValores = new ArrayList<OrcamentoReceitaMes>();
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void listarValoresReceita() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();

			if (this.vendedorFiltro == null || this.receita == null)
				this.listaValores = new ArrayList<OrcamentoReceitaMes>();
			else
				this.listaValores = new OrcReceitaService(con).listarValoresReceitasOrc(this.vendedorFiltro, this.receita);
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void listarCustoVenda() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.recMes.setListaDespesas(new OrcReceitaService(con).listarCustoVenda(false, this.recMes.getMesRef(), 
					this.vendedorFiltro, this.receita.getReceita()));
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public String getTitulo() {
		MesAnoOrcamento mesref = new MesAnoOrcamento();
		mesref.setAno(this.mesRef.getAno() - 1);
		mesref.setMes(this.mesRef.getMes());
		
		return "Receita em " + mesref.getMesAno();
	}
	
	public void mostrarReceitaAnterior() {
		ConexaoDB con = null;
		try {
			MesAnoOrcamento mesref = new MesAnoOrcamento();
			mesref.setAno(this.mesRef.getAno() - 1);
			mesref.setMes(this.mesRef.getMes());
			
			con = new ConexaoDB();
			//this.orcReceitaAnt = new OrcReceitaService(con).valoresHistoricos(this.vendedorFiltro, mesref, 
				//	this.orcReceita.getReceita());
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void obterValor(OrcamentoReceitaMes item) {
		
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			item.setValorUnitario(new OrcReceitaService(con).buscarPreco(this.receita.getReceita(),
					this.vendedorFiltro.getSetor()));
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}

	}
		
	public void gravarOrcamento() {
		ConexaoDB con = null;
		try {
			
			con = new ConexaoDB();
			new OrcReceitaService(con).gravarOrcamento(this.vendedorFiltro, this.receita, this.listaValores);
			ConexaoDB.gravarTransacao(con);
			
			listarValoresReceita();
			
			messages.info("Orçamento gravado com sucesso.");
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}

	
	public void baixarPlanilha() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
			this.nomeXLS = "Lancto_receitas_" + DataUtil.dataString(new Date(), "yyyy_MM_dd_HH_mm_ss") + ".xls";
			caminho = caminho + "relatorios/" + this.nomeXLS;
			
			new OrcReceitaService(con).gerarPlanilhaLancReceita(caminho);
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
			
		} finally {
			ConexaoDB.close(con);
		}
	}
	

	public StreamedContent getFile() throws FileNotFoundException {
		String caminhoWebInf = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
		caminhoWebInf = caminhoWebInf + "relatorios/" + this.nomeXLS;
		InputStream stream = new FileInputStream(caminhoWebInf); //Caminho onde esta salvo o arquivo.
        
		file = DefaultStreamedContent.builder()
                .name(this.nomeXLS)
                .contentType("application/xls")
                .stream(() -> stream)
                .build();   

        return file;  
    }

	
	public boolean mesFechado(int mes) {
		mesRef.setMes(mes);
		return !mesRef.isMesAberto();
	}	
}
