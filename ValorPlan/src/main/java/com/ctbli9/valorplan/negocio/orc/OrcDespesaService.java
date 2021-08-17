package com.ctbli9.valorplan.negocio.orc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.DAO.orc.OrcDespesaGeralServiceDAO;
import com.ctbli9.valorplan.DAO.orc.OrcDespesaRHServiceDAO;
import com.ctbli9.valorplan.DAO.orc.ProjecaoContasDAO;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.orc.OrcamentoDespesa;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.modelo.orc.ValorTotalMes;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.xls.GeraXLS;


public class OrcDespesaService {
	private ConexaoDB con;
	
	private int linha;
	private int coluna;
	
	public OrcDespesaService(ConexaoDB con) {
		this.con = con;
	}

	public List<OrcamentoDespesa> listarDespesaGeral(CentroCusto cenCusto) throws Exception {
		return OrcDespesaGeralServiceDAO.listarDespesaGeral(con.getConexao(), cenCusto);
	}

	public void gravarOrcDespesaGeral(CentroCusto cenCusto, OrcamentoDespesa despesa, List<ValorTotalMes> valores) throws Exception {
		OrcDespesaGeralServiceDAO.gravarOrcDespesaGeral(con.getConexao(), PlanoServiceDAO.getPlanoSelecionado(), cenCusto, despesa, valores);
	}

	public List<OrcamentoDespesa> listarDespesaRH(Recurso funcionario) throws SQLException, NamingException {
		if (funcionario == null)
			return new ArrayList<OrcamentoDespesa>();
		else
			return OrcDespesaRHServiceDAO.listarDespesaRH(con.getConexao(), funcionario);
	}
	
	public void gravarOrcDespesaRH(Recurso funcionario, OrcamentoDespesa despesa, List<ValorTotalMes> valores) throws Exception {
		OrcDespesaRHServiceDAO.gravarOrcDespesaRH(con.getConexao(), funcionario, despesa, valores);
	}

	/*public List<OrcamentoDespesa> listarDespesaRHEquipe(MesAnoOrcamento mesRef, Recurso vendedorFiltro) throws Exception {
		return OrcDespesaServiceDAO.listarDespesaRHEquipe(con.getConexao(), mesRef, vendedorFiltro);
	}*/

	public List<ValorTotalMes> listarOrcDespesaRH(Recurso funcionario, ContaContabil despesa) throws SQLException, NamingException {
		return OrcDespesaRHServiceDAO.listarOrcDespesaRH(con.getConexao(), funcionario, despesa);
	}

	public List<ValorTotalMes> listarOrcDespesaGeral(CentroCusto cenCusto, ContaContabil despesa) throws SQLException, NamingException {
		return OrcDespesaGeralServiceDAO.listarOrcDespesaGeral(con.getConexao(), cenCusto, despesa);
	}

	public List<ValorTotalMes> valoresHistoricos(CentroCusto cenCusto, ContaContabil conta, int anoAnt) throws SQLException {
		return OrcDespesaGeralServiceDAO.valoresHistoricos(con.getConexao(), cenCusto, conta, anoAnt);
	}
	
	public TreeNode listarProjecaoContas(MesAnoOrcamento mesAno) throws Exception {
		return ProjecaoContasDAO.listarProjecaoContas(con.getConexao(), mesAno);
	}

	public void gerarPlanilhaEstrutura(String nomePlanilha, TreeNode listaContas, MesAnoOrcamento anoMes) throws IOException {
		GeraXLS excel = new GeraXLS(PlanoServiceDAO.getPlanoSelecionado().getNmPlano(), nomePlanilha);

		linha = 0;
		coluna = 0;

		excel.setBorda(1);
		excel.criarEstilo();
		
		excel.setEstilo(GeraXLS.ESTILO_TITULO);
		excel.escreveCelula(linha, coluna++, "Conta");
		
		excel.escreveCelula(linha, coluna++, "Orçado " + anoMes.getMesAno());
		excel.escreveCelula(linha, coluna++, "Acumulado até " + anoMes.getMesAno());
		
		linha++;
		coluna = 0;

		imprimirLinha(excel, listaContas, anoMes.getMes(), -1);
		
		excel.salvaXLS();
	}
	
	
	private void imprimirLinha(GeraXLS excel, TreeNode pai, int periodo, int nivel) {
		OrcamentoDespesa elo = (OrcamentoDespesa) pai.getData();

		if (!elo.getConta().getDsConta().equals("RAIZ")) {
			if (nivel == 0)
				excel.setEstilo(GeraXLS.ESTILO_TITULO);
			else if (nivel == 1)
				excel.setEstilo(GeraXLS.ESTILO_SUB_TITULO);
			else
				excel.setEstilo(GeraXLS.ESTILO_NORMAL);
			
			StringBuilder indentacao = new StringBuilder();
			for (int i = 0; i < (nivel * 5); i++) {
				indentacao.append(' ');
			}
	
			excel.escreveCelula(linha, coluna++, indentacao.toString() + elo.getConta().getDsConta());
			excel.escreveCelula(linha, coluna++, elo.getVrConta().doubleValue());
			excel.escreveCelula(linha, coluna++, elo.getVrContaAcum().doubleValue());
			
			linha++;
			coluna = 0;
		}		
		
		if (pai.getChildCount() > 0) {
			pai.getChildren().forEach(filho->imprimirLinha(excel, filho, periodo, (nivel + 1)));
			
		}
	}
}
