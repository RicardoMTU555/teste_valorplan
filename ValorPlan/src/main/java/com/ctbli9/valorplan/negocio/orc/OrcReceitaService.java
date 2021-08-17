package com.ctbli9.valorplan.negocio.orc;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.DAO.orc.OrcReceitaServiceDAO;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.orc.OrcamentoDespesaVenda;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaAcum;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaMes;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaMonitor;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.modelo.receita.Receita;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.recursos.RegraNegocioException;

public class OrcReceitaService {
	private ConexaoDB con;
	
	public OrcReceitaService(ConexaoDB con) {
		this.con = con;
	}

	public List<OrcamentoReceitaAcum> listarReceitasOrc(Recurso func) throws Exception {
		if (func == null)
			throw new RegraNegocioException("Selecione um Vendedor.");
		
		return OrcReceitaServiceDAO.listarReceitasOrc(con.getConexao(), func);
	}
	
	public List<OrcamentoReceitaMes> listarValoresReceitasOrc(Recurso func, OrcamentoReceitaAcum receita) throws Exception {
		return OrcReceitaServiceDAO.listarValoresReceitasOrc(con.getConexao(), func, receita);
	}
	

	public List<OrcamentoReceitaMonitor> listarReceitasOrcAgr(Recurso func, MesAnoOrcamento mesRef, boolean anual) throws Exception {
		if (func == null)
			throw new RegraNegocioException("Selecione um Vendedor.");
		
		return OrcReceitaServiceDAO.listarReceitasOrcAgr(con.getConexao(), func, mesRef, anual);
	}
	
	public List<SelectItem> detalharEquipe(MesAnoOrcamento mesRef, Recurso func, Receita receita) 
			throws RegraNegocioException, SQLException, NamingException {
		if (func == null)
			throw new RegraNegocioException("Selecione um Vendedor.");
		
		return OrcReceitaServiceDAO.detalharEquipe(con.getConexao(), mesRef, func, receita);
	}

	public List<SelectItem> detalharMeses(MesAnoOrcamento mesRef, Recurso func, Receita receita) 
			throws RegraNegocioException, SQLException, NamingException {
		if (func == null)
			throw new RegraNegocioException("Selecione um Vendedor.");
		
		return OrcReceitaServiceDAO.detalharMeses(con.getConexao(), mesRef, func, receita);
	}

	public List<OrcamentoDespesaVenda> listarCustoVenda(boolean anual, MesAnoOrcamento mesRef, Recurso func, 
			Receita receita) 
			throws Exception {
		return OrcReceitaServiceDAO.listarCustoVenda(con.getConexao(), anual, mesRef, func, receita);
	}
	
	public void gravarOrcamento(Recurso func, OrcamentoReceitaAcum receita, List<OrcamentoReceitaMes> listaValores) 
			throws Exception {

		System.out.println(func.getNomeVinculo());
		System.out.println(receita.getReceita().getSgReceita());
		
		PreparedStatement pstmtSel = OrcReceitaServiceDAO.inicializaSel(con.getConexao());
		PreparedStatement pstmtIns = OrcReceitaServiceDAO.inicializaIns(con.getConexao());
		PreparedStatement pstmtUpd = OrcReceitaServiceDAO.inicializaUpd(con.getConexao());
		PreparedStatement pstmtDel = OrcReceitaServiceDAO.inicializaDel(con.getConexao());

		receita.setValorAcumulado(null);
		for (OrcamentoReceitaMes orc : listaValores) {
			OrcReceitaServiceDAO.gravarOrcamento(func, receita, orc, PlanoServiceDAO.getPlanoSelecionado(),
					pstmtSel, pstmtIns, pstmtUpd, pstmtDel);
		}
		
		pstmtSel.close();
		pstmtIns.close();
		pstmtUpd.close();
		pstmtDel.close();
		
		pstmtSel = null;
		pstmtIns = null;
		pstmtUpd = null;
		pstmtDel = null;

	}

	public OrcamentoReceitaMonitor valoresHistoricos(Recurso vendedorFiltro, MesAnoOrcamento periodoAnt, Receita receita) 
			throws SQLException {
		return OrcReceitaServiceDAO.valoresHistoricos(con.getConexao(), vendedorFiltro, periodoAnt, receita);
	}

	public BigDecimal buscarPreco(Receita receita, CentroCusto centroCusto) throws SQLException {
		return OrcReceitaServiceDAO.buscarPreco(con.getConexao(), receita, centroCusto);
	}

	public void gerarPlanilhaLancReceita(String caminho) throws IOException, SQLException, NamingException {
		OrcReceitaServiceDAO.planilhaLancamentoReceita(con.getConexao(), caminho);
	}
}
