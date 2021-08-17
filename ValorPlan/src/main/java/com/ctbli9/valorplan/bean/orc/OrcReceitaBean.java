package com.ctbli9.valorplan.bean.orc;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.enumeradores.MedidaOrcamento;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Departamento;
import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.modelo.orc.OrcamentoDespesa;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaMonitor;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.negocio.EquipeService;
import com.ctbli9.valorplan.negocio.orc.OrcReceitaService;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.recursos.FacesMessages;

@ManagedBean(name="orcReceitaBean")
@ViewScoped
public class OrcReceitaBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Recurso vendedorFiltro;
	private MesAnoOrcamento mesRef;
	private List<OrcamentoReceitaMonitor> listaOrcRec = new ArrayList<OrcamentoReceitaMonitor>();
	//private List<OrcamentoDespesa> despesasEquipe; // TODO Apagar
	private OrcamentoDespesa totalDespesaEquipe;
	private OrcamentoReceitaMonitor totalOrcReceita;
	private OrcamentoReceitaMonitor orcReceita;
	private int operacao;
	private String tituloDialogo;
	private boolean anual;
	private List<SelectItem> detalhesVendedor;
	private String descrDepartamento;

    private CentroCusto cencus;
    private Departamento depto;
	
	private FacesMessages messages;
	
	@SuppressWarnings("unused")
	private TreeNode elo;
	
	/*
	 * Construtor
	 */
	public OrcReceitaBean() {
		messages = new FacesMessages();
		mesRef = new MesAnoOrcamento();
	}

	public Funcionario getFuncionarioLogado() {
		return Global.getFuncionarioLogado();
	}

	public Recurso getVendedorFiltro() {
		return vendedorFiltro;
	}
	public void setVendedorFiltro(Recurso vendedorFiltro) {
		this.vendedorFiltro = vendedorFiltro;
	}
	public MesAnoOrcamento getMesRef() {
		return mesRef;
	}
	public void setMesRef(MesAnoOrcamento mesRef) {
		this.mesRef = mesRef;
	}

	public List<OrcamentoReceitaMonitor> getListaOrcRec() {
		return listaOrcRec;
	}
	// TODO Apagar
	/*public List<OrcamentoDespesa> getDespesasEquipe() {
		return despesasEquipe;
	}*/
	public OrcamentoDespesa getTotalDespesaEquipe() {
		return totalDespesaEquipe;
	}
	
	public OrcamentoReceitaMonitor getTotalOrcReceita() {
		return totalOrcReceita;
	}
	
	public BigDecimal getLucroLiquidoTotal() {
		if (this.totalOrcReceita != null && this.totalDespesaEquipe != null)
			return this.totalOrcReceita.getLucroBrutoUnitario().subtract(this.totalDespesaEquipe.getVrConta());
		else
			return BigDecimal.ZERO;
	}
	
	public OrcamentoReceitaMonitor getOrcReceita() {
		return orcReceita;
	}
	public void setOrcReceita(OrcamentoReceitaMonitor orcReceita) {
		this.orcReceita = orcReceita;
	}
	public int getOperacao() {
		return operacao;
	}
	public String getTituloDialogo() {
		return tituloDialogo;
	}
	
	public boolean isAnual() {
		return anual;
	}
	public void setAnual(boolean anual) {
		this.anual = anual;
	}
	
	public List<SelectItem> getDetalhesVendedor() {
		return detalhesVendedor;
	}
	
	public String getDescrDepartamento() {
		return descrDepartamento;
	}
	
	public boolean isTabVazio() {
		return listaOrcRec == null || listaOrcRec.isEmpty();
	}

	public void mesAnterior(TreeNode elo) {
		int anoAux = this.mesRef.getAno();
		this.mesRef.sub();
		if (this.mesRef.getAno() == anoAux)
			listarReceitaOrcamento(elo);
		else
			this.mesRef.add();
	}

	public void mesPosterior(TreeNode elo) {
		int anoAux = this.mesRef.getAno();
		this.mesRef.add();
		if (this.mesRef.getAno() == anoAux)
			listarReceitaOrcamento(elo);
		else
			this.mesRef.sub();
	}
	
	public void listarReceitaOrcamento(TreeNode elo) {
		this.elo = elo;
		
		long cdGerente = 0;
		String codigoEquipeVenda = "";
		
		if(elo != null) {
			if (elo.getData().getClass().getName().indexOf("Recurso") > -1) {
				this.vendedorFiltro = (Recurso) elo.getData(); 
				cdGerente = this.vendedorFiltro.getCdRecurso();
				codigoEquipeVenda += cdGerente;
				 
            	this.descrDepartamento = this.vendedorFiltro.getNomeVinculo() + " - " + 
            			this.vendedorFiltro.getCargo().getDsCargo();

			} else {
				if (elo.getData().getClass().getName().indexOf("CentroCusto") > -1) {
	        		this.depto = null;
	            	this.cencus = (CentroCusto) elo.getData();
	            	cdGerente = this.cencus.getResponsavel().getCdRecurso();
	            	codigoEquipeVenda = this.cencus.getResponsavel().getCodEquipeVenda();
	            	
	            	this.descrDepartamento = "Centro de Custo: " + this.cencus.getCodExterno() + " - " +
	            			this.cencus.getCecDsCompleta() + " - " +
	            			this.cencus.getResponsavel().getNomeVinculo();
	            			
	            } else if (elo.getData().getClass().getName().indexOf("Departamento") > -1) {
	            	this.cencus = null;
	        		this.depto = (Departamento) elo.getData();
	        		cdGerente = this.depto.getResponsavel().getCdRecurso();
	        		codigoEquipeVenda = this.depto.getResponsavel().getCodEquipeVenda();
	        		
	        		this.descrDepartamento = "Departamento: " +
	            			this.depto.getDsDepartamento() + " - " +
	            			this.depto.getResponsavel().getNomeVinculo();
	            }
			}
			
			ConexaoDB con = null;
	        try {
	        	con = new ConexaoDB();
	        	if (codigoEquipeVenda.isEmpty())
	        		codigoEquipeVenda = "0";
	        	
				this.vendedorFiltro = new EquipeService(con.getConexao()).getRecurso(cdGerente);
				this.vendedorFiltro.setCodEquipeVenda(codigoEquipeVenda);
				if (codigoEquipeVenda.isEmpty())
					codigoEquipeVenda = "0"; 

				this.listaOrcRec = new OrcReceitaService(con).listarReceitasOrcAgr(this.vendedorFiltro, mesRef, this.anual);
				//this.despesasEquipe = new OrcDespesaService(con).listarDespesaRHEquipe(mesRef, this.vendedorFiltro);

				this.totalOrcReceita = new OrcamentoReceitaMonitor();
				for (OrcamentoReceitaMonitor receAux : this.listaOrcRec) {
					if (receAux.getReceita().getCategoria().getMedida().equals(MedidaOrcamento.Q))
						this.totalOrcReceita.addQuant(receAux.getQuantidade());
					this.totalOrcReceita.addValorUnitario(receAux.getValorUnitario());
					this.totalOrcReceita.addValorDespesa(receAux.getValorDespesa());
				}
				
				/*this.totalDespesaEquipe = new OrcamentoDespesa();
				for (OrcamentoDespesa despAux : this.despesasEquipe) {
					this.totalDespesaEquipe.addVrDespesa(despAux.getVrDespesa());
				}*/
				
			} catch (Exception e) {
				Global.erro(con, e, messages, null);
				
			} finally {
				ConexaoDB.close(con);
			}
		}
	}
	
	public void limparReceitaOrcamento() {
		this.descrDepartamento = "";
		this.listaOrcRec = null;
	}

	public void listarCustoVenda() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.orcReceita.setListaDespesas(new OrcReceitaService(con).listarCustoVenda(this.anual, this.mesRef, 
					this.vendedorFiltro, this.orcReceita.getReceita()));
			
			this.operacao = 1;
			this.tituloDialogo = "Deduções de Venda";
			
		}  catch (Exception e) {
			Global.erro(con, e, messages, null);
			
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void listarDetalheEquipe() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			if (this.anual)
				this.detalhesVendedor = new OrcReceitaService(con).detalharMeses(this.mesRef, 
						this.vendedorFiltro, this.orcReceita.getReceita());
			else
				this.detalhesVendedor = new OrcReceitaService(con).detalharEquipe(this.mesRef, 
					this.vendedorFiltro, this.orcReceita.getReceita());
			
			this.operacao = 2;
			if (this.anual)
				this.tituloDialogo = "Quantidades por mês";
			else
				this.tituloDialogo = "Quantidades por Vendedor";
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
			
		} finally {
			ConexaoDB.close(con);
		}
	}
}
