package com.ctbli9.valorplan.negocio;

import java.io.IOException;
import java.util.List;

import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.BalanceteServiceDAO;
import com.ctbli9.valorplan.DAO.DepartamentoServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.DAO.consulta.DREFiltroServiceDAO;
import com.ctbli9.valorplan.DAO.consulta.DREServiceDAO;
import com.ctbli9.valorplan.DAO.consulta.QuantRecServiceDAO;
import com.ctbli9.valorplan.DAO.consulta.RecDesServiceDAO;
import com.ctbli9.valorplan.modelo.NivelArvore;
import com.ctbli9.valorplan.modelo.ResumoDRE;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.modelo.Filial;
import ctbli9.recursos.xls.GeraXLS;

public class ResumoService {
	private ConexaoDB con;
	
	private int linha;
	private int coluna;
		
	public ResumoService(ConexaoDB con) {
		this.con = con;
	}

	/*
	 * tipo: 1 = mensal, 2 = anual
	 */
	public TreeNode listarResumoDRE(MesAnoOrcamento mesAno, int numNiveis, List<String> filiaisSelecionadas, boolean agrupaConta) 
			throws Exception {
		TreeNode resumo = null;
		
		DREServiceDAO dao = new DREServiceDAO(con.getConexao(), agrupaConta, mesAno, numNiveis, filiaisSelecionadas);
		BalanceteServiceDAO daoBalanc = new BalanceteServiceDAO(con.getConexao());
		
		daoBalanc.carregarBalancete(true, new int[0], 0, true);
		resumo = dao.listarResumo();
		
		resumo.setExpanded(true);
		
		dao = null;
		daoBalanc = null;
		return resumo;
	}
	
	/*
	 * tipo: 1 = mensal, 2 = anual
	 */
	public TreeNode listarResumoFiltroDRE(int tipo, MesAnoOrcamento mesAno, Filial filial, int[] setoresSelecionados) 
			throws Exception {
		TreeNode resumo = null;
		
		NivelArvore nivel = DepartamentoServiceDAO.getNivelArvore(con.getConexao());
		
		DREFiltroServiceDAO dao = new DREFiltroServiceDAO(con.getConexao(), tipo, mesAno, filial, setoresSelecionados);
		BalanceteServiceDAO daoBalanc = new BalanceteServiceDAO(con.getConexao());
		
		if (nivel.getGestor()=='D')
			daoBalanc.carregarBalancete(true, setoresSelecionados, 0, true);
		else
			daoBalanc.carregarBalancete(true, new int[0], 0, true);
		
		resumo = dao.listarResumo();
		resumo.setExpanded(true);
		
		daoBalanc = null;
		
		return resumo;
	}
	
	public void gerarPlanilhaRecDes(String nomePlanilha) throws Exception {
		RecDesServiceDAO.gerarPlanilhaRecDes(con.getConexao(), nomePlanilha);
	}

	public void gerarPlanilhaQuantRec(String nomePlanilha, int numNiveis, CategoriaReceita categoria) throws Exception {
		QuantRecServiceDAO.gerarPlanilhaQuantidade(con.getConexao(), nomePlanilha, numNiveis, categoria);		
	}

	public void gerarPlanilhaSaldosMN(String nomePlanilha, MesAnoOrcamento mesAno, int numNiveis, 
			List<String> filiaisSelecionadas, boolean agrupaConta) 
			throws Exception {
		
		DREServiceDAO dao = new DREServiceDAO(con.getConexao(), agrupaConta, mesAno, numNiveis, filiaisSelecionadas); //mudei isso até agora
		
		BalanceteServiceDAO daoBalanc = new BalanceteServiceDAO(con.getConexao());
		daoBalanc.carregarBalancete(true, new int[0], 0, true);
		dao.sqlConsulta();
		
		daoBalanc.gerarPlanilhaSaldos(nomePlanilha, dao.getRes());
		dao.close();
		dao = null;
		
		daoBalanc.close();
		daoBalanc = null;
	}
	
	public void gerarPlanilhaSaldosSpl(String nomePlanilha, 
			int tipo, MesAnoOrcamento mesAno, Filial filial, int[] setoresSelecionados) throws Exception {
		
		DREFiltroServiceDAO dao = new DREFiltroServiceDAO(con.getConexao(), tipo, mesAno, filial, setoresSelecionados); //mudei isso até agora
		
		BalanceteServiceDAO daoBalanc = new BalanceteServiceDAO(con.getConexao());
		daoBalanc.carregarBalancete(true, setoresSelecionados, 0, true);
		dao.sqlConsulta();
		
		daoBalanc.gerarPlanilhaSaldos(nomePlanilha, dao.getRes());
		dao.close();
		dao = null;
		
		daoBalanc.close();
		daoBalanc = null;
	}
	
	/*
	 * Tipo: 1 - mensal, 2 - anual
	 */
	public void gerarPlanilhaEstrutura(String nomePlanilha, TreeNode root1, int tipo, MesAnoOrcamento anoMes) throws IOException {
		GeraXLS excel = new GeraXLS(PlanoServiceDAO.getPlanoSelecionado().getNmPlano(), nomePlanilha);

		linha = 0;
		coluna = 0;

		excel.setBorda(1);
		excel.criarEstilo();
		
		excel.setEstilo(GeraXLS.ESTILO_TITULO);
		excel.escreveCelula(linha, coluna++, "Conta");
		
		if (tipo == 1) {
			excel.escreveCelula(linha, coluna++, "Orçado " + anoMes.getMesAno());
			excel.escreveCelula(linha, coluna++, "Realizado " + anoMes.getMesAno());
			excel.escreveCelula(linha, coluna++, "Variação " + anoMes.getMesAno());
		} else {
			for (int i = 1; i <= 12; i++) {
				anoMes.setMes(i);
				excel.escreveCelula(linha, coluna++, "Orçado " + anoMes.getMesAno());
				excel.escreveCelula(linha, coluna++, "Realizado " + anoMes.getMesAno());
				excel.escreveCelula(linha, coluna++, "Variação " + anoMes.getMesAno());				
			}
			
		}
		
		excel.escreveCelula(linha, coluna++, "Orçado Acumulado");
		excel.escreveCelula(linha, coluna++, "Realizado Acumulado");
		excel.escreveCelula(linha, coluna++, "Variação Acumulado");
		
		linha++;
		coluna = 0;

		imprimirLinha(excel, root1, tipo, anoMes.getMes());
		
		excel.salvaXLS();
		
	}

	private void imprimirLinha(GeraXLS excel, TreeNode pai, int tipo, int periodo) {
		ResumoDRE elo = (ResumoDRE) pai.getData();

		if (!elo.getTipo().equals("RAI")) {
			if (elo.getTipo().equals("TOT"))
				excel.setEstilo(GeraXLS.ESTILO_TITULO);
			else if (elo.getTipo().equals("GRP"))
				excel.setEstilo(GeraXLS.ESTILO_SUB_TITULO);
			else
				excel.setEstilo(GeraXLS.ESTILO_NORMAL);
	
			excel.escreveCelula(linha, coluna++, elo.getDescricao());
			if (tipo == 1) {
				excel.escreveCelula(linha, coluna++, elo.getValorOrcado(periodo));
				excel.escreveCelula(linha, coluna++, elo.getValorRealizado(periodo));
				excel.escreveCelula(linha, coluna++, elo.obtemPercent(periodo-1));
			} else {
				for (int i = 1; i <= 12; i++) {
					excel.escreveCelula(linha, coluna++, elo.getValorOrcado(i));
					excel.escreveCelula(linha, coluna++, elo.getValorRealizado(i));
					excel.escreveCelula(linha, coluna++, elo.obtemPercent(i-1));
				}
			}
			excel.escreveCelula(linha, coluna++, elo.getValorOrcadoAcum());
			excel.escreveCelula(linha, coluna++, elo.getValorRealizadoAcum());
			excel.escreveCelula(linha, coluna++, elo.getPercentAcum());
			
			linha++;
			coluna = 0;
		}		
		
		if (pai.getChildCount() > 0)
			pai.getChildren().forEach(filho->imprimirLinha(excel, filho, tipo, periodo));
	}
	
	

}
