package com.ctbli9.valorplan.DAO.orc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.CategImobilizadoServiceDAO;
import com.ctbli9.valorplan.DAO.CentroCustoServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.orc.OrcamentoInvestimento;
import com.ctbli9.valorplan.modelo.orc.OrcamentoInvestimentoMes;

import com.ctbli9.valorplan.modelo.CentroCusto;
import ctbli9.recursos.RegraNegocioException;

public class OrcInvestimentoServiceDAO {

	public static List<OrcamentoInvestimento> listarInvestimentos(Connection con, CentroCusto cenCusto) throws Exception {
		List<OrcamentoInvestimento> lista = new ArrayList<OrcamentoInvestimento>();

		String sql = String.format("SELECT * FROM OrcInvestimento "
				+ "WHERE cdPlano = %d "
				+ "AND cdCentroCusto = %d", 
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				cenCusto.getCdCentroCusto());
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			OrcamentoInvestimento orcam = 
					new OrcamentoInvestimento(CategImobilizadoServiceDAO.pesquisarCategImobilizado(con, res.getInt("cdCategImobili")));
			
			orcam.setCdInvestimento(res.getLong("cdInvestimento"));
			orcam.setCencusto(CentroCustoServiceDAO.pesquisarCentroCusto(con, res.getInt("cdCentroCusto")));
			orcam.setSqInvestimento(res.getInt("sqInvestimento"));
			orcam.setDsInvestimento(res.getString("dsInvestimento"));
			listarInvestValores(con, orcam);
			lista.add(orcam);
		}
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		
		return lista;
	}	
	
	private static void listarInvestValores(Connection con, OrcamentoInvestimento investimento) 
			throws SQLException, NamingException, RegraNegocioException {
		
		String sql = String.format("SELECT * FROM OrcInvestLancto "
				+ "WHERE cdInvestimento = %d", 
				investimento.getCdInvestimento());
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			investimento.setValorMes(res.getInt("nrAnoMes"), res.getBigDecimal("VrInvestimento"));
		}
	}

	public static void gravarOrcInvestimento(Connection con, OrcamentoInvestimento investimento) 
			throws SQLException, NamingException {
		if (existe(con, investimento))
			alterarInvestimento(con, investimento);
		else
			incluirInvestimento(con, investimento);
		
		gravarValoresInvestimento(con, investimento);
	}

	private static boolean existe(Connection con, OrcamentoInvestimento investimento) throws SQLException, NamingException {
		return ConexaoDB.existeRegistro(con, "OrcInvestimento", "cdInvestimento = " + investimento.getCdInvestimento());
	}

	public static void incluirInvestimento(Connection con, OrcamentoInvestimento orcam) throws SQLException, NamingException {
		String sql = "INSERT INTO OrcInvestimento VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		orcam.setSqInvestimento(ConexaoDB.gerarNovoCodigo(con, "OrcInvestimento", "sqInvestimento", 
				String.format("cdPlano = %d AND cdCentroCusto = %d AND cdCategImobili = %d", 
						PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
						orcam.getCencusto().getCdCentroCusto(),
						orcam.getImobilizado().getCdCategImobili())));
		
		orcam.setCdInvestimento(Long.parseLong(String.format("%03d%03d%03d%03d", 
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				orcam.getCencusto().getCdCentroCusto(),
				orcam.getImobilizado().getCdCategImobili(),
				orcam.getSqInvestimento())
				));
		
		int i = 0;
		pstmt.setLong(++i, orcam.getCdInvestimento());
		pstmt.setInt(++i, PlanoServiceDAO.getPlanoSelecionado().getCdPlano());
		pstmt.setInt(++i, orcam.getCencusto().getCdCentroCusto());
		pstmt.setInt(++i, orcam.getImobilizado().getCdCategImobili());
		pstmt.setInt(++i, orcam.getSqInvestimento());
		pstmt.setString(++i, orcam.getDsInvestimento());
		
		pstmt.executeUpdate();
		
		pstmt.close();
		pstmt = null;		
	}
	
	private static void alterarInvestimento(Connection con, OrcamentoInvestimento orcam) throws SQLException, NamingException {
		String sql = "UPDATE OrcInvestimento "
				+ "SET dsInvestimento = ? "
				+ "WHERE cdInvestimento = ? ";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setString(++i, orcam.getDsInvestimento());
		pstmt.setLong(++i, orcam.getCdInvestimento());

		pstmt.executeUpdate();
		
		pstmt.close();
		pstmt = null;	
	}

	private static void gravarValoresInvestimento(Connection con, OrcamentoInvestimento investimento) 
			throws SQLException, NamingException {
		String sql = "INSERT INTO OrcInvestLancto VALUES (?, ?, ?)";
		PreparedStatement pstmtIns = con.prepareStatement(sql);
		
		sql = "UPDATE OrcInvestLancto SET vrInvestimento = ? "
				+ "WHERE cdInvestimento = ? "
				+ "AND nrAnoMes = ? ";
		PreparedStatement pstmtUpd = con.prepareStatement(sql);
		
		sql = "SELECT 1 FROM OrcInvestLancto "
				+ "WHERE cdInvestimento = ? "
				+ "AND nrAnoMes = ?";
		
		PreparedStatement pstmtSel = con.prepareStatement(sql);
		ResultSet res = null;		
		
		int i = 0;
		for (OrcamentoInvestimentoMes valor : investimento.getValores()) {
			i = 0;
			pstmtSel.setLong(++i, investimento.getCdInvestimento());
			pstmtSel.setInt(++i, valor.getMesRef().getAnoMes());
			res = pstmtSel.executeQuery();
			
			if (res.next()) {
				i = 0;
				pstmtUpd.setBigDecimal(++i, valor.getVrInvestimento());
				pstmtUpd.setLong(++i, investimento.getCdInvestimento());
				pstmtUpd.setInt(++i, valor.getMesRef().getAnoMes());
				pstmtUpd.executeUpdate();
				
			} else {
				i = 0;
				pstmtIns.setLong(++i, investimento.getCdInvestimento());
				pstmtIns.setInt(++i, valor.getMesRef().getAnoMes());
				pstmtIns.setBigDecimal(++i, valor.getVrInvestimento());
				pstmtIns.executeUpdate();
				
			}
		}
		
		res.close();
		res = null;
		
		pstmtSel.close();
		pstmtSel = null;
		
		pstmtIns.close();
		pstmtIns = null;

		pstmtUpd.close();
		pstmtUpd = null;
	}

	public static void excluirOrcInvestimento(Connection con, OrcamentoInvestimento orcam) throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		String sql = String.format("DELETE FROM OrcInvestLancto WHERE cdInvestimento = %d", orcam.getCdInvestimento());
		stmt.executeUpdate(sql);
		
		sql = String.format("DELETE FROM OrcInvestimento WHERE cdInvestimento = %d", orcam.getCdInvestimento());
		stmt.executeUpdate(sql);
		
		stmt.close();
		stmt = null;	
	}

}
