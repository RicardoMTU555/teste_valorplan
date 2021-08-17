package com.ctbli9.valorplan.DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.ctbli9.valorplan.modelo.DespesaRH;

import ctbli9.modelo.ctb.ContaContabil;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;
import ctbli9.recursos.RegraNegocioException;

public class ParamDespesaRHServiceDAO {
	public static PreparedStatement inicializaSel(Connection con) throws SQLException, NamingException {
		String sql = "SELECT 1 FROM OrcParamFolha p " + 
				"WHERE p.cdPlano = ? " + 
				"AND p.ID_ContaSal = ? " +
				"AND p.ID_ContaEnc = ?" +
				"AND p.nrAnoMes = ? ";
		return con.prepareStatement(sql);
	}
	
	public static PreparedStatement inicializaIns(Connection con) throws SQLException, NamingException {
		return con.prepareStatement("INSERT INTO OrcParamFolha VALUES (?, ?, ?, ?, ?)");
	}
	
	public static PreparedStatement inicializaUpd(Connection con) throws SQLException, NamingException {
		String sql = "UPDATE OrcParamFolha p SET p.pcDespesa = ? " + 
					"WHERE p.cdPlano = ? " + 
					"AND p.ID_ContaSal = ? " +
					"AND p.ID_ContaEnc = ?" +
					"AND p.nrAnoMes = ?"; 
		return con.prepareStatement(sql);
	}

	public static PreparedStatement inicializaDel(Connection con) throws SQLException, NamingException {
		String sql = "DELETE FROM OrcParamFolha p " + 
				"WHERE p.cdPlano = ? " + 
				"AND p.ID_ContaSal = ? " +
				"AND p.ID_ContaEnc = ?" +
				"AND p.nrAnoMes = ?";
		return con.prepareStatement(sql);
	}
	
	
	public static List<DespesaRH> listarDespesaFolha(Connection con, ContaContabil contaSalario) throws Exception {
		List<DespesaRH> lista = new ArrayList<DespesaRH>();
		Statement stmt = con.createStatement();
				
		String sql = String.format("SELECT ct.*, c.*, g.*, "
				+ "COALESCE(pf.nrAnoMes, 0) AS nrAnoMes, pf.pcDespesa "
				+ "FROM CtbConta ct "
				+ "LEFT OUTER JOIN CtbClasseDre c ON ct.cdClasse = c.cdClasse "
				+ "LEFT OUTER JOIN CtbGrupoClasse g ON c.cdGrupoClasse = g.cdGrupoClasse "
				+ "LEFT OUTER JOIN OrcParamFolha pf ON pf.cdPlano = %d "
				+             "AND pf.nrAnoMes BETWEEN %d01 AND %d12 "
				+             "AND pf.ID_ContaSal = %d "
				+             "AND ct.ID_Conta = pf.ID_ContaEnc "
				+ "WHERE ct.tpConta IN ('DP') "
				+ "ORDER BY ct.cdConta, pf.nrAnoMes;", 
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				contaSalario.getIdConta()
				);

	    ResultSet rs = stmt.executeQuery(sql);
	    boolean temRegistro = rs.next();
	    while (temRegistro) {
	    	String codDespesa = rs.getString("cdConta");
	    	
	    	DespesaRH pd = new DespesaRH();
	    	
	    	pd.setSalario(contaSalario);
	    	pd.setEncargo(ContaContabilServiceDAO.carregarContaContabil(rs));
	    
	    	int mes = 1;
	    	int ano = PlanoServiceDAO.getPlanoSelecionado().getNrAno();
	    	BigDecimal[] despesas = new BigDecimal[12];
			do {
	    		if (temRegistro && rs.getString("nrAnoMes").equals(String.format("%04d%02d", ano, mes)) && 
						codDespesa.equals(rs.getString("cdConta"))) {
					
	    			despesas[mes-1] = rs.getBigDecimal("pcDespesa");
					
	    			temRegistro = rs.next();
				}
				else {
					despesas[mes-1] = BigDecimal.ZERO;
					
					if (temRegistro && rs.getInt("nrAnoMes") == 0 && codDespesa.equals(rs.getString("cdConta"))) {
						temRegistro = rs.next();
					}
				}
				mes++;

	    	} while ((temRegistro && codDespesa.equals(rs.getString("cdConta"))) || mes <= 12);
	    	
	    	pd.setPercDespesa(despesas);
	    	
	    	lista.add(pd);
	    }//end while
	    
		stmt.close();
		stmt = null;
		
		rs.close();
		rs = null;
		return lista;
	}


	public static void atualizaParametros(PreparedStatement pstmtSel, PreparedStatement pstmtIns, PreparedStatement pstmtUpd, PreparedStatement pstmtDel, 
			int cdPlano, int anoMes, int idContaSalario, int idContaEncargo, BigDecimal pcDespesa) throws SQLException {
		
		MesAnoOrcamento mesRef = new MesAnoOrcamento();
		try {
			mesRef.setAnoMes(anoMes);
		} catch (RegraNegocioException e) {
			e.printStackTrace();
		}
		if (!mesRef.isMesAberto())
			return;
				
		int ind = 0;
		pstmtSel.setInt(++ind, cdPlano);
		pstmtSel.setInt(++ind, idContaSalario);		
		pstmtSel.setInt(++ind, idContaEncargo);
		pstmtSel.setInt(++ind, anoMes);		
		
		ResultSet rs = pstmtSel.executeQuery();
		if (!rs.next()) {
			if (pcDespesa != null && pcDespesa.compareTo(BigDecimal.ZERO) > 0) {
				ind = 0;
				pstmtIns.setInt(++ind, cdPlano);
				pstmtIns.setInt(++ind, idContaSalario);		
				pstmtIns.setInt(++ind, idContaEncargo);
				pstmtIns.setInt(++ind, anoMes);			
				pstmtIns.setBigDecimal(++ind, pcDespesa);
		    	pstmtIns.executeUpdate();			
			}
	    	
	    } else {
	    	if (pcDespesa.compareTo(BigDecimal.ZERO) > 0) {
				ind = 0;
		    	pstmtUpd.setBigDecimal(++ind, pcDespesa);
	
				pstmtUpd.setInt(++ind, cdPlano);
				pstmtUpd.setInt(++ind, idContaSalario);		
				pstmtUpd.setInt(++ind, idContaEncargo);
				pstmtUpd.setInt(++ind, anoMes);			
		    	
		    	pstmtUpd.executeUpdate();
				
	    	} else {
	    		ind = 0;
		    	pstmtDel.setInt(++ind, cdPlano);
		    	pstmtDel.setInt(++ind, idContaSalario);		
		    	pstmtDel.setInt(++ind, idContaEncargo);
				pstmtDel.setInt(++ind, anoMes);			
		    	
		    	pstmtDel.executeUpdate();
	    	}
	    }
		rs.close();
		rs = null;
	}
}
