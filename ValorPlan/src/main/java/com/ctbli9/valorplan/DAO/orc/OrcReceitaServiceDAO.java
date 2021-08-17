package com.ctbli9.valorplan.DAO.orc;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.CentroCustoServiceDAO;
import com.ctbli9.valorplan.DAO.ContaContabilServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.ReceitaServiceDAO;
import com.ctbli9.valorplan.enumeradores.MedidaOrcamento;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.orc.OrcamentoDespesaVenda;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaAcum;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaMes;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaMonitor;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.modelo.receita.Receita;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.modelo.Plano;
import ctbli9.recursos.RegraNegocioException;
import ctbli9.recursos.xls.GeraXLS;

public class OrcReceitaServiceDAO {
	/*
	 * Lista receitas para LANCAMENTO pelo Vendedor
	 */
	public static List<OrcamentoReceitaAcum> listarReceitasOrc(Connection con, Recurso func) throws Exception {
		List<OrcamentoReceitaAcum> modelos = new ArrayList<OrcamentoReceitaAcum>();
		Statement stmt = con.createStatement();
		
		String sql = String.format(
				"SELECT cr.*, catr.*, ct.*, c.*, g.*, "
				+ "valor.total AS total "
				+ "FROM CadReceita cr "
				+ "JOIN CadCategReceita catr ON cr.cdCategoria = catr.cdCategoria "
				+ "JOIN CtbConta ct ON cr.ID_ContaReceita = ct.ID_Conta "
				+ "LEFT OUTER JOIN CtbClasseDre c ON ct.cdClasse = c.cdClasse "
				+ "LEFT OUTER JOIN CtbGrupoClasse g ON c.cdGrupoClasse = g.cdGrupoClasse "
				+ "LEFT OUTER JOIN "
				+ "("
				+ " SELECT r.cdReceita, SUM(r.qtReceita * r.vrUnitario) AS total FROM OrcReceita r "
				+ " WHERE r.cdPlano = %d AND r.nrAnoMes BETWEEN %04d01 AND %04d12 AND r.ID_Recurso = %d GROUP BY r.cdReceita "
				+ ") valor ON valor.cdReceita = cr.cdReceita "
				+ "WHERE cr.idAtivo = 'S' "
				+ "AND catr.cdCategoria IN "
				+ "  ( SELECT ct.cdCategoria FROM OrcEquipe eq "
				+ "    JOIN CadCencusCatrec ct ON eq.cdCentroCusto = ct.cdCentroCusto "
				+ "    WHERE eq.ID_Recurso = %d "
				+ "  )"
				+ "ORDER BY cr.sgReceita",
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				func.getCdRecurso(),
				func.getCdRecurso());
		System.out.println(sql);
		ResultSet rs = stmt.executeQuery(sql);
	    
	    while (rs.next()) {
	    	OrcamentoReceitaAcum orcamReceita = new OrcamentoReceitaAcum();
	    	orcamReceita.setReceita(ReceitaServiceDAO.carregarReceita(rs));
	    	orcamReceita.setValorAcumulado(rs.getBigDecimal("total"));

	    	modelos.add(orcamReceita);
	    }//end while
		
		//pstmt.close();
		//pstmt = null;
		
		stmt.close();
		rs.close();
		rs = null;
		return modelos;
	}
	
	public static List<OrcamentoReceitaMes> listarValoresReceitasOrc(Connection con, 
			Recurso func, OrcamentoReceitaAcum receita) throws Exception {
		List<OrcamentoReceitaMes> lista = new ArrayList<OrcamentoReceitaMes>();
		Statement stmt = con.createStatement();
		
		BigDecimal valorUnitario = buscarPreco(con, receita.getReceita(), func.getSetor());
    	
		for (int i = 1; i <= 12; i++) {
			MesAnoOrcamento mes = new MesAnoOrcamento();
			mes.setMes(i);
			
			OrcamentoReceitaMes valRec = new OrcamentoReceitaMes();
			valRec.setMesRef(mes);
			if (receita.getReceita().getCategoria().getMedida().equals(MedidaOrcamento.V))
				valRec.setQuantidade(1);
			valRec.setValorUnitario(valorUnitario);
			lista.add(valRec);
		}
		
		String sql = String.format(
				"SELECT ort.nrAnoMes, ort.qtReceita, ort.vrUnitario, "
				+      "ort.ValorReceita, ort.pcDespesa, ort.ValorDeducao "
				+ "FROM (" + 
				montaDeducoesReceitaTot("AND r.id_recurso = " + func.getCdRecurso() + 
						" AND r.cdReceita = " + receita.getReceita().getCdReceita(), false) + 
				        ") ort "
				+ "ORDER BY ort.nrAnoMes",
				func.getCdRecurso());
		
		ResultSet rs = stmt.executeQuery(sql);
	    
	    while (rs.next()) {
	    	for (OrcamentoReceitaMes recei : lista) {
				if (recei.getMesRef().getAnoMes() == rs.getInt("nrAnoMes")) {
					// Orçamento tipo "VALOR": considera quantidade = 1
					
			    	if (receita.getReceita().getCategoria().getMedida().equals(MedidaOrcamento.V))
			    		recei.setQuantidade(1);
			    	else
			    		recei.setQuantidade(rs.getInt("qtReceita"));
			    	
			    	recei.setValorUnitario(rs.getBigDecimal("vrUnitario"));
			    	recei.setPercDespesa(rs.getBigDecimal("pcDespesa"));
			    	recei.setValorDespesa(rs.getBigDecimal("ValorDeducao"));
			    	recei.setListaDespesas(null);
					break;
				}
			}
	    }
		
		stmt.close();
		rs.close();
		rs = null;
		
		return lista;
	}

	/*
	 * Lista receitas para VER DADOS AGRUPADOS
	 */
	public static List<OrcamentoReceitaMonitor> listarReceitasOrcAgr(Connection con, Recurso func, MesAnoOrcamento mesRef, boolean anual) throws Exception {
		List<OrcamentoReceitaMonitor> modelos = new ArrayList<OrcamentoReceitaMonitor>();
		Statement stmt = con.createStatement();
		String sql = "";
		String condicao = "";
		
		if (!anual) {
			condicao = String.format("AND r.qtReceita <> 0 AND r.vrUnitario <> 0 "
					+ "AND r.ID_Recurso IN (%s) "
					+ "AND r.nrAnoMes = %d ", 
					func.getCodEquipeVenda(),
					mesRef.getAnoMes());		
		} else {
			condicao = String.format("AND r.qtReceita <> 0 AND r.vrUnitario <> 0 "
					+ "AND r.ID_Recurso IN (%s) "
					+ "AND r.nrAnoMes BETWEEN %d01 AND %d12 ", 
					func.getCodEquipeVenda(),
					mesRef.getAno(),
					mesRef.getAno());	
		}
		sql = "SELECT ort.cdReceita, "
				+ "SUM(ort.qtReceita) AS quantReceita, "
				+ "SUM(ort.valorReceita) AS valorReceita, "
				+ "SUM(ort.ValorDeducao) AS ValorDeducao "
				+ "FROM (" + montaDeducoesReceitaTot(condicao, false) + ") ort "
				+ "JOIN CadReceita cr ON cr.cdReceita = ort.cdReceita AND cr.idAtivo = 'S' "
				+ "GROUP BY ort.cdReceita";

		ResultSet rs = stmt.executeQuery(sql);
	    
	    while (rs.next()) {
	    	OrcamentoReceitaMonitor orcamReceita = new OrcamentoReceitaMonitor();
	    	
	    	orcamReceita.setReceita(ReceitaServiceDAO.buscarReceita(con, rs.getInt("cdReceita"), false));
	    	
	    	if (orcamReceita.getReceita().getCategoria().getMedida().equals(MedidaOrcamento.V))
	    		orcamReceita.setQuantidade(1);
	    	else
	    		orcamReceita.setQuantidade(rs.getInt("quantReceita"));
	    	orcamReceita.setValorUnitario(rs.getBigDecimal("valorReceita"));
	    	orcamReceita.setValorDespesa(rs.getBigDecimal("ValorDeducao"));
	    	orcamReceita.setPercDespesa(null);
	    	orcamReceita.setListaDespesas(null);

	    	modelos.add(orcamReceita);
	    }//end while
		
		stmt.close();
		rs.close();
		rs = null;
		return modelos;
	}
	
	public static List<SelectItem> detalharEquipe(Connection con, MesAnoOrcamento mesRef, Recurso func, Receita receita) 
			throws SQLException, NamingException {
		
		Statement stmt = con.createStatement();
		
		String condicao = String.format("AND r.ID_Recurso IN (%s) "
				+ "AND r.nrAnoMes = %d "
				+ "AND r.cdReceita = %d "
				+ "AND r.qtReceita <> 0 AND r.vrUnitario <> 0 ", 
				func.getCodEquipeVenda(),
				mesRef.getAnoMes(),
				receita.getCdReceita());

		String sql = "SELECT COALESCE(f.nmFuncionario, eq.nmRecurso) AS nomeFunc, cc.cecCdExterno, cc.cecDsResumida, "
				+ "ort.cdReceita, tr.tprTpOrcamento, "
				+ "ort.qtReceita, ort.valorReceita, ort.ValorDeducao "
				+ "FROM (" + montaDeducoesReceitaTot(condicao, false) + ") ort "
				
				+ "JOIN OrcEquipe eq ON ort.ID_Recurso = eq.ID_Recurso "
				+ "LEFT OUTER JOIN OrcEquipeRecurs er ON eq.ID_Recurso = er.ID_Recurso "
				+             "AND er.nrAnoMesInicial <= ort.nrAnoMes "
				+             "AND (er.nrAnoMesFinal IS NULL OR er.nrAnoMesFinal >= ort.nrAnoMes) "
				
				+ "LEFT OUTER JOIN CadFuncionario f ON er.cdFuncVinculo = f.cdFuncionario "
				+ "JOIN EmpCentroCusto cc ON eq.cdCentroCusto = cc.cdCentroCusto "
				+ "JOIN CadTipoReceita tr ON ort.cdTipoReceita = tr.cdTipoReceita ";

	    ResultSet rs = stmt.executeQuery(sql);
	    
	    List<SelectItem> detalhe = new ArrayList<SelectItem>();
		while (rs.next()) {
			String descr = "";
			if (rs.getString("tprTpOrcamento").equals("Q"))
				descr = String.format("%-100s : %03d", 
					rs.getString("nomeFunc") + " - " + rs.getString("cecCdExterno") + " - " + rs.getString("cecDsResumida"), 
					rs.getInt("qtReceita"));
			else
				descr = String.format("%-100s : %12.2f", 
						rs.getString("nomeFunc") + " - " + rs.getString("cecCdExterno") + " - " + rs.getString("cecDsResumida"), 
						rs.getDouble("valorReceita"));
			
			descr = descr.replace("  ", "..");
			detalhe.add(new SelectItem(null, descr));
	    }
		
		stmt.close();
		rs.close();
		rs = null;
		
		return detalhe;
	}

	public static List<SelectItem> detalharMeses(Connection con, MesAnoOrcamento mesRef, Recurso func, Receita receita) 
			throws SQLException, NamingException, RegraNegocioException {
		
		Statement stmt = con.createStatement();
		
		String condicao = String.format("AND r.ID_Recurso IN (%s) "
				+ "AND r.nrAnoMes BETWEEN %d01 AND %d12 "
				+ "AND r.cdReceita = %d ", 
				func.getCodEquipeVenda(),
				mesRef.getAno(),
				mesRef.getAno(),
				receita.getCdReceita());

		String sql = "SELECT ort.nrAnoMes, tr.tprTpOrcamento, sum(ort.qtReceita) AS qtReceita, "
				+ " sum(ort.valorReceita) AS valorReceita, sum(ort.ValorDeducao) "
				+ "FROM (" + montaDeducoesReceitaTot(condicao, false) + ") ort "
				+ "JOIN CadTipoReceita tr ON ort.cdTipoReceita = tr.cdTipoReceita "
				+ "GROUP BY ort.nrAnoMes, tr.tprTpOrcamento";
		
	    ResultSet rs = stmt.executeQuery(sql);
	    
	    List<SelectItem> detalhe = new ArrayList<SelectItem>();
		while (rs.next()) {
			MesAnoOrcamento mes = new MesAnoOrcamento();
			mes.setAnoMes(rs.getInt("nrAnoMes"));
			
			String descr = "";
			if (rs.getString("tprTpOrcamento").equals("Q"))
				descr = String.format("%04d - %-50s : %03d", 
					mes.getAno(), 
					mes.getNomeMes(), 
					rs.getInt("qtReceita"));
			else
				descr = String.format("%04d - %-50s : %12.2f", 
						mes.getAno(), 
						mes.getNomeMes(), 
						rs.getDouble("valorReceita"));
			
			descr = descr.replace("  ", "..");
			detalhe.add(new SelectItem(null, descr));
	    }
		
		stmt.close();
		rs.close();
		rs = null;
		
		return detalhe;
	}
	
	
	public static PreparedStatement inicializaSel(Connection con) throws SQLException, NamingException {
		String sql = "SELECT 1 FROM OrcReceita orc "
				+ "WHERE orc.cdPlano = ? "
				+ "AND orc.nrAnoMes = ? "
				+ "AND orc.ID_Recurso = ? "
				+ "AND orc.cdReceita = ?";
		return con.prepareStatement(sql);
	}
	
	public static PreparedStatement inicializaIns(Connection con) throws SQLException, NamingException {
		return con.prepareStatement("INSERT INTO OrcReceita VALUES (?, ?, ?, ?, ?, ?)");
	}
	
	public static PreparedStatement inicializaUpd(Connection con) throws SQLException, NamingException {
		String sql = "UPDATE OrcReceita SET "
    			+ "qtReceita = ?, "
    			+ "vrUnitario = ? "
    			+ "WHERE cdPlano = ? "
    			+ "AND nrAnomes = ? "
    			+ "AND ID_Recurso = ? "
    			+ "AND cdReceita = ?"; 
		return con.prepareStatement(sql);
	}

	public static PreparedStatement inicializaDel(Connection con) throws SQLException, NamingException {
		String sql = "DELETE FROM OrcReceita orc "
				+ "WHERE orc.cdPlano = ? "
				+ "AND orc.nrAnoMes = ? "
				+ "AND orc.ID_Recurso = ? "
				+ "AND orc.cdReceita = ?";
		return con.prepareStatement(sql);
	}
	
	
	public static void gravarOrcamento(Recurso func, OrcamentoReceitaAcum receita, OrcamentoReceitaMes orc, Plano plano,
			PreparedStatement pstmtSel, PreparedStatement pstmtIns, PreparedStatement pstmtUpd, PreparedStatement pstmtDel) 
			throws Exception {
		
		ResultSet rs = null;
		
		int ind = 0;
		pstmtSel.setInt(++ind, plano.getCdPlano());
		pstmtSel.setInt(++ind, orc.getMesRef().getAnoMes());
		pstmtSel.setLong(++ind, func.getCdRecurso());
		pstmtSel.setInt(++ind, receita.getReceita().getCdReceita());
		
		rs = pstmtSel.executeQuery();
		if (!rs.next()) {
			if (orc.getValorUnitario().compareTo(BigDecimal.ZERO) > 0) {
				ind = 0;
				pstmtIns.setInt(++ind, plano.getCdPlano());
				pstmtIns.setInt(++ind, orc.getMesRef().getAnoMes());
				pstmtIns.setLong(++ind, func.getCdRecurso());
				pstmtIns.setInt(++ind, receita.getReceita().getCdReceita());

				pstmtIns.setInt(++ind, orc.getQuantidade());
				pstmtIns.setBigDecimal(++ind, orc.getValorUnitario());

				pstmtIns.executeUpdate();
			}
	    } else {
			if (orc.getValorUnitario().compareTo(BigDecimal.ZERO) > 0) {
		    	ind = 0;
		    	pstmtUpd.setInt(++ind, orc.getQuantidade());
				pstmtUpd.setBigDecimal(++ind, orc.getValorUnitario());

				pstmtUpd.setInt(++ind, plano.getCdPlano());
				pstmtUpd.setInt(++ind, orc.getMesRef().getAnoMes());
				pstmtUpd.setLong(++ind, func.getCdRecurso());
				pstmtUpd.setInt(++ind, receita.getReceita().getCdReceita());

				pstmtUpd.executeUpdate();
			} else {
				ind = 0;
				pstmtDel.setInt(++ind, plano.getCdPlano());
				pstmtDel.setInt(++ind, orc.getMesRef().getAnoMes());
				pstmtDel.setLong(++ind, func.getCdRecurso());
				pstmtDel.setInt(++ind, receita.getReceita().getCdReceita());

				pstmtDel.executeUpdate();
			}
	    }
		receita.addValorAcumulado(orc.getValorTotal());
		    
	    rs.close();
		rs = null;		
	}
	
	public static List<OrcamentoDespesaVenda> listarCustoVenda(Connection con, 
			boolean anual, MesAnoOrcamento mesRef, Recurso func, Receita receita) throws Exception {
		
		Statement stmtAux = con.createStatement();
		String sql = null;
		String condicao = null;
		if (anual) {
			condicao = String.format("AND orcRec.nrAnoMes BETWEEN %d01 AND %d12 "
					+ "AND orcRec.cdReceita = %d "
					+ "AND orcRec.ID_Recurso IN (%s) ", 
					mesRef.getAno(),
					mesRef.getAno(),
					receita.getCdReceita(),
					func.getCodEquipeVenda()
					);

			sql = "SELECT r.ID_Conta, SUM(r.valorReceita) AS ValorReceita, SUM(r.ValorDeducao) AS ValorDespesa "
					+ "FROM (" + montaDeducoesReceita(condicao, false) + ") r "
					+ "GROUP BY r.ID_Conta";
		}
		else {
			condicao = String.format("AND orcRec.nrAnoMes = %d "
					+ "AND orcRec.cdReceita = %d "
					+ "AND orcRec.ID_Recurso IN (%s) ", 
					mesRef.getAnoMes(),
					receita.getCdReceita(),
					func.getCodEquipeVenda()
					);

			sql = "SELECT r.ID_Conta, SUM(r.valorReceita) AS ValorReceita, SUM(r.ValorDeducao) AS ValorDespesa "
					+ "FROM (" + montaDeducoesReceita(condicao, false) + ") r "
					+ "GROUP BY r.ID_Conta";
		}

		ResultSet rs = stmtAux.executeQuery(sql);
		
		List<OrcamentoDespesaVenda> listaDespesasVenda = new ArrayList<OrcamentoDespesaVenda>();
		
		while (rs.next()) {
			OrcamentoDespesaVenda odv = new OrcamentoDespesaVenda();
			odv.setConta(ContaContabilServiceDAO.pesquisarIDConta(con, rs.getInt("ID_Conta")));
			odv.setValorReceita(rs.getBigDecimal("ValorReceita"));
			odv.setValorDespesa(rs.getBigDecimal("valorDespesa"));
			odv.setPercDespesa(null);
			
			listaDespesasVenda.add(odv);
		}
		
		stmtAux.close();
		rs.close();
		rs = null;
		return listaDespesasVenda;
	}//listarCustoVenda

		
	//
	//sp_DeducoesReceita
	//
	public static String montaDeducoesReceita(String condicao, boolean filtraCenCusto) {
		if (condicao == null)
			condicao = "";
		
		int codPlano =  0;
		try {
			codPlano = PlanoServiceDAO.getPlanoSelecionado().getCdPlano();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		String condicaoCencusto = filtraCenCusto 
				? "JOIN OrcEquipe eq ON orcRec.ID_Recurso = eq.ID_Recurso "
					+ "JOIN TmpCencus ON TmpCencus.cdCentroCusto = eq.cdCentroCusto " 
				: "";
		
		return "SELECT "
				+ "orcRec.nrAnoMes, "
				+ "orcRec.id_recurso, "
				+ "orcRec.cdReceita, "
				+ "pdr.ID_Conta, "
				+ " orcRec.qtReceita, "
				+ "orcRec.vrUnitario, "
				+ "(orcRec.qtReceita * orcRec.vrUnitario) AS valorReceita, " 
				+ " COALESCE(pdr.pcDespesa, 0) AS pcDespesa, "
				+ " CAST( ((COALESCE(pdr.pcDespesa, 0) / 100.0000) * (orcRec.qtReceita * orcRec.vrUnitario)) AS DECIMAL(13,2)) AS valorDeducao " 
				+ "FROM OrcReceita orcRec "
				+ "JOIN CadReceita cr ON orcRec.cdReceita = cr.cdReceita "
				+ "JOIN CadCategReceita cat ON cr.cdCategoria = cat.cdCategoria "
				+ condicaoCencusto
				+ "LEFT OUTER JOIN OrcParamDeducao pdr ON pdr.cdPlano = orcRec.cdPlano "
					+ "AND pdr.nrAnoMes = orcRec.nrAnoMes "
					+ "AND pdr.cdReceita = orcRec.cdReceita "
					+ "AND pdr.id_recurso = orcRec.id_recurso "
				+ "LEFT OUTER JOIN CadDeducaoVenda dv ON pdr.cdReceita = dv.cdReceita AND pdr.ID_Conta = dv.ID_Conta "
				+ "LEFT OUTER JOIN CtbConta ct ON dv.ID_Conta = ct.ID_Conta "
				+ "WHERE orcRec.cdPlano = " + codPlano + " "
				+ condicao;
	}

	//
	//  sp_DeducoesReceita_Tot
	//
	public static String montaDeducoesReceitaTot(String condicao, boolean filtraCenCusto) {
		if (condicao == null)
			condicao = "";
		
		int cdPlano =  0;
		try {
			cdPlano = PlanoServiceDAO.getPlanoSelecionado().getCdPlano();
		} catch (Exception e) {
			// TODO: handle exception
		}
		String condicaoCencusto = filtraCenCusto 
				? "JOIN OrcEquipe eq ON r.ID_Recurso = eq.ID_Recurso "
					+ "JOIN TmpCencus ON TmpCencus.cdCentroCusto = eq.cdCentroCusto " 
				: "";
		
		return "SELECT r.nrAnoMes, r.ID_Recurso, r.cdReceita, r.qtReceita, r.vrUnitario, " + 
			       "(r.qtReceita * r.vrUnitario) AS valorReceita, " + 
			       "COALESCE(pdr.pcDespesa, 0) AS pcDespesa, " + 
			       "CAST( ((COALESCE(pdr.pcDespesa,0) / 100.0000)*(r.qtReceita*r.vrUnitario)) AS DECIMAL(13,2) ) AS valorDeducao " + 
			"FROM OrcReceita r " + 
			"JOIN CadReceita cr ON r.cdReceita = cr.cdReceita " + 
			"JOIN CadCategReceita cat ON cr.cdCategoria = cat.cdCategoria " +
			condicaoCencusto +
			"LEFT OUTER JOIN (" + 
				"SELECT p.nrAnoMes, p.cdReceita, p.id_recurso, SUM(p.pcDespesa) AS pcDespesa " + 
				"FROM OrcParamDeducao p " + 
				"WHERE p.cdPlano = " + cdPlano + " " + 
				"GROUP BY p.nrAnoMes, p.cdReceita, p.id_recurso " + 
				") pdr ON  pdr.nrAnoMes = r.nrAnoMes AND pdr.cdReceita = r.cdReceita AND pdr.id_recurso = r.id_recurso " + 
			"WHERE r.cdPlano = " + cdPlano + " " +
			condicao;
	}

	public static OrcamentoReceitaMonitor valoresHistoricos(Connection con, Recurso vendedorFiltro, MesAnoOrcamento periodoAnt,
			Receita receita) throws SQLException {
		OrcamentoReceitaMonitor receitaAnterior = new OrcamentoReceitaMonitor();
		//1. Achar Plano no ano anterior
		String sql = String.format("SELECT max(cdPlano) as cod FROM OrcPlano WHERE nrAno = %d "
				+ "AND stPlano = (SELECT MIN(stPlano) FROM OrcPlano WHERE nrAno = %d AND stPlano <> 0)",
				periodoAnt.getAno(),
				periodoAnt.getAno()
				);
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		if (res.next()) {
			int codPlano = res.getInt("cod");

			/*--2. Pegar recurso no mesmo centro de custo
			SELECT * FROM OrcEquipe WHERE nrAno = 2020 AND cdCentroCusto = 10;
			*/
			//3. Achar receita no mesmo centro de custo, no plano, no ano/mes
			sql = String.format("SELECT * FROM OrcReceita rec " 
					+ "WHERE rec.cdPlano = %d "  //Plano obtido no passo anterior
					+ "AND rec.nrAnoMes = %d "
					+ "AND rec.ID_Recurso IN (SELECT ID_Recurso FROM OrcEquipe WHERE nrAno = %d AND cdCentroCusto = %d) "
					+ "AND cdReceita = %d ",
					codPlano,
					periodoAnt.getAnoMes(),
					periodoAnt.getAno(),
					vendedorFiltro.getSetor().getCdCentroCusto(),
					receita.getCdReceita());
			
			res.close();
			
			res = stmt.executeQuery(sql);
			if (res.next()) {
				receitaAnterior.setQuantidade(res.getInt("qtReceita"));
				receitaAnterior.setValorUnitario(res.getBigDecimal("vrUnitario"));
				
			} else {
				sql = String.format("SELECT * FROM OrcReceita rec " 
						+ "WHERE rec.cdPlano = %d "  //Plano obtido no passo anterior
						+ "AND rec.nrAnoMes = %d "
						+ "AND rec.ID_Recurso IN (SELECT ID_Recurso FROM OrcEquipe WHERE nrAno = %d AND cdCentroCusto = %d) "
						+ "AND cdReceita IN (SELECT cdReceita FROM CadReceita WHERE ID_ContaReceita = %d) ",
						codPlano,
						periodoAnt.getAnoMes(),
						periodoAnt.getAno(),
						vendedorFiltro.getSetor().getCdCentroCusto(),
						receita.getContaReceita().getIdConta()
						);
				res.close();
				
				res = stmt.executeQuery(sql);
				if (res.next()) {
					receitaAnterior.setQuantidade(res.getInt("qtReceita"));
					receitaAnterior.setValorUnitario(res.getBigDecimal("vrUnitario"));
				}				
			}
		}
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		
		
		return receitaAnterior;
	}

	public static BigDecimal buscarPreco(Connection con, Receita receita, CentroCusto setor) throws SQLException {
		BigDecimal preco = BigDecimal.ZERO;
		String sql = String.format("SELECT vrPreco FROM CadValorReceita WHERE cdReceita = %d AND cdFilial = %d",
				receita.getCdReceita(),
				setor.getFilial().getCdFilial());
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		if (res.next()) {
			preco = res.getBigDecimal("vrPreco");
		}
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		return preco ;
	}

	public static void planilhaLancamentoReceita(Connection con, String nomePlanilha) throws IOException, SQLException, NamingException {
		MesAnoOrcamento mesRef = new MesAnoOrcamento();
		String condicao = String.format("AND r.nrAnomes BETWEEN %04d01 AND %04d12", 
				mesRef.getAno(), 
				mesRef.getAno());

		String sql = String.format(
				"SELECT eq.cecDsResumida, eq.nomeVinculo, cad.sgReceita, cad.dsReceita, " + 
				"  lpad(eq.cdCentroCusto, 10, '0') || lpad(rec.ID_Recurso, 10, '0') || lpad(rec.cdReceita, 10, '0') AS id, " + 
				"  rec.nrAnoMes, rec.qtReceita, rec.vrUnitario, rec.valorReceita, rec.pcDespesa, rec.valorDeducao, " + 
				"  (rec.valorReceita - rec.ValorDeducao) as valorLiquido " + 
				"FROM (" + montaDeducoesReceitaTot(condicao, true) + ") rec " + 
				"JOIN CadReceita cad ON rec.cdReceita = cad.cdReceita " + 
				"JOIN sp_ListaRecurso(%d) eq ON eq.ID_Recurso = rec.ID_Recurso " + 
				"ORDER BY 1, 2, 3, 6;",
				mesRef.getAnoMes());
		
		GeraXLS excel = new GeraXLS("Saldos", nomePlanilha);
		
		Statement stmt = con.createStatement();
		
		CentroCustoServiceDAO.montarCentrosCustoGerente(con, Global.getFuncionarioLogado(), null);
		
		
		ResultSet res = stmt.executeQuery(sql);
		
		int numLin = 0;
		int numCol = 0;

		excel.setEstilo(GeraXLS.ESTILO_TITULO);
		String linha = "SETOR|RECURSO|RECEITA|DESCRIÇÃO|MÊS 01|MÊS 02|MÊS 03|MÊS 04|MÊS 05|MÊS 06|MÊS 07|MÊS 08|MÊS 09|MÊS 10|MÊS 11|MÊS 12";
		excel.linha(linha);
		
		excel.escreveCelula(numLin, numCol++, "Setor");
		excel.escreveCelula(numLin, numCol++, "Recurso");
		excel.escreveCelula(numLin, numCol++, "Receita");
		excel.escreveCelula(numLin, numCol++, "Descrição");
		
		for (int i = 1; i <= 12; i++) {
			excel.escreveCelula(numLin, numCol++, String.format("Mês %02d", i), 2);
			numCol += 2;			
		}
		
		numLin++;
		numCol = 4;
		for (int i = 1; i <= 12; i++) {
			excel.escreveCelula(numLin, numCol++, "Receita");
			excel.escreveCelula(numLin, numCol++, "Dedução");
			excel.escreveCelula(numLin, numCol++, "Líquido");
		}
		
		numLin++;
		numCol = 0;
		
		excel.setFonte("Arial", 10, "preto", false, false);
		excel.setCorFundo("branco");
		excel.setBorda(1);
		excel.criarEstilo();

		excel.setEstilo(GeraXLS.ESTILO_NORMAL);
		
		boolean temRegistro = res.next();
		while (temRegistro) {
			String monitor = res.getString("ID");
			
			excel.escreveCelula(numLin, numCol++, res.getString("cecDsResumida"));
			excel.escreveCelula(numLin, numCol++, res.getString("nomeVinculo"));
			excel.escreveCelula(numLin, numCol++, res.getString("sgReceita"));
			excel.escreveCelula(numLin, numCol++, res.getString("dsReceita"));
			
			int mes = 1;
			try {
				do {
					mesRef.setMes(mes);
					if (res.getInt("nrAnoMes") == mesRef.getAnoMes() && monitor.equals(res.getString("ID"))) {
						excel.escreveCelula(numLin, numCol++, res.getDouble("valorReceita"));
						excel.escreveCelula(numLin, numCol++, res.getDouble("valorDeducao"));
						excel.escreveCelula(numLin, numCol++, res.getDouble("valorLiquido"));

						temRegistro = res.next();
						if (!temRegistro)
							break;
					}
					else {
						excel.escreveCelula(numLin, numCol++, "");
					}
					mes++;
					
				} while ((temRegistro && monitor.equals(res.getString("ID"))) || mes <= 12);
				
				numLin++;
				numCol = 0;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		excel.salvaXLS();
		
		res.close();
		stmt.close();
		
		res = null;
		stmt = null;
		
	}
	
	
	public static void main(String[] args) {
		System.out.println("gerando view de receita com deduções analitica");
		System.out.println(montaDeducoesReceita("", true));
		System.out.println("view gerada");
		
		System.out.println("gerando view de receita com deduções consolidada");
		System.out.println(montaDeducoesReceitaTot("", true));
		System.out.println("view gerada");
			
		
	}
}
