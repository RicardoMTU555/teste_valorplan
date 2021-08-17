package com.ctbli9.valorplan.DAO.orc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.ContaContabilServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.modelo.orc.OrcamentoDespesa;
import com.ctbli9.valorplan.modelo.orc.ValorTotalMes;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.modelo.ctb.ContaContabil;

public class OrcDespesaRHServiceDAO {
	
	public static List<OrcamentoDespesa> listarDespesaRH(Connection con, Recurso funcionario) 
			throws SQLException, NamingException {
		
		List<OrcamentoDespesa> lista = new ArrayList<OrcamentoDespesa>();
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		
		// TODO aqui colocar filtro em ORCCONTARESPON
		
		String sql = String.format("SELECT ct.*, c.*, g.*, despAcum.acumulado "
				+ "FROM CtbConta ct "
				+ "LEFT OUTER JOIN CtbClasseDre c ON ct.cdClasse = c.cdClasse "
				+ "LEFT OUTER JOIN CtbGrupoClasse g ON c.cdGrupoClasse = g.cdGrupoClasse "
				+ "LEFT OUTER JOIN "
				+ "( "
				+   "SELECT ID_Conta, SUM(vrDespesa) AS Acumulado "
				+   "FROM OrcDespesaRH WHERE cdPlano = %d AND nrAnoMes BETWEEN %d01 AND %d12 AND ID_Recurso = %d "
				+   "GROUP BY ID_Conta "
				+ ") AS despAcum ON ct.ID_Conta = despAcum.ID_Conta "
				+ "WHERE ct.tpConta IN ('SL','DP') "
				+ "AND NOT EXISTS (SELECT DISTINCT 1 FROM OrcParamFolha opf WHERE opf.cdPlano = %d AND opf.ID_ContaEnc = ct.ID_Conta AND opf.pcDespesa <> 0) "
				+ "ORDER BY ct.tpConta DESC, ct.sgConta",
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				funcionario.getCdRecurso(),
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano()
				);
		
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			OrcamentoDespesa valDesp = new OrcamentoDespesa();
			
			valDesp.setConta(ContaContabilServiceDAO.carregarContaContabil(rs));
			valDesp.setCenCusto(null);
			valDesp.setVrConta(null);
			valDesp.setVrContaAcum(rs.getBigDecimal("acumulado"));
			
			lista.add(valDesp);
		}
		
		rs.close();
		stmt.close();
		rs = null;
		stmt = null;
		
		return lista;
	}
		
	
	public static List<OrcamentoDespesa> listarDespesaRHEquipe(Connection con, MesAnoOrcamento mesRef, Recurso funcionario) 
			throws Exception {
		
		List<OrcamentoDespesa> lista = new ArrayList<OrcamentoDespesa>();
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		String sql = String.format("SELECT ct.ID_Conta, SUM(od.vrDespesa) AS vrDespesa "
				+ "FROM CtbConta ct "
				+ "LEFT OUTER JOIN OrcDespesaRH od ON od.cdPlano = %d AND od.nrAnoMes = %d AND od.ID_Recurso IN (%s) "
				+ "     AND od.ID_Conta = ct.ID_Conta "
				+ "WHERE ct.tpConta IN ('SL', 'DP') "
				+ "AND NOT EXISTS "
				+ "(SELECT DISTINCT 1 FROM OrcParamFolha opf WHERE opf.cdPlano = %d AND opf.ID_ContaEnc = ct.ID_Conta AND opf.pcDespesa <> 0) "
				+ "GROUP BY ct.ID_Conta",
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				mesRef.getAnoMes(),
				funcionario.getCodEquipeVenda(),
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano()
				);
		
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			OrcamentoDespesa valDesp = new OrcamentoDespesa();
			
			valDesp.setConta(ContaContabilServiceDAO.pesquisarIDConta(con, rs.getInt("ID_Conta")));
			// TODO Apagar valDesp.setMesRef(mesRef);
			valDesp.setCenCusto(null);
			valDesp.setVrConta(rs.getBigDecimal("vrDespesa"));
			
			lista.add(valDesp);
		}
		
		rs.close();
		stmt.close();
		rs = null;
		stmt = null;
		
		return lista;
	}
	
	public static void gravarOrcDespesaRH(Connection con, 
			Recurso funcionario, OrcamentoDespesa despesa, List<ValorTotalMes> valores) 
					throws SQLException, NamingException {
		
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		
		String sql = "";
		
		despesa.setVrContaAcum(BigDecimal.ZERO);
		
		for (ValorTotalMes orc : valores) {
			sql = String.format("SELECT 1 FROM OrcDespesaRH orc "
					+ "WHERE orc.cdPlano = %d AND orc.nrAnoMes = %d AND orc.ID_Recurso = %d "
					+ "AND orc.ID_Conta = %d", 
					PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
					orc.getMesRef().getAnoMes(),
					funcionario.getCdRecurso(),
					despesa.getConta().getIdConta());
			
		    rs = stmt.executeQuery(sql);
		    if (!rs.next()) {
		    	if (orc.getVrOrcado().compareTo(BigDecimal.ZERO) > 0) {
					sql = String.format(Locale.US,
			    			"INSERT INTO OrcDespesaRH VALUES "
			    			+ "(%d, %d, %d, %d, %13.2f)", 
			    			PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
			    			orc.getMesRef().getAnoMes(),
			    			funcionario.getCdRecurso(),
							despesa.getConta().getIdConta(),
							orc.getVrOrcado()
							);
			    	stmt.executeUpdate(sql);
		    	}
		    } else { // Achou
		    	if (orc.getVrOrcado().compareTo(BigDecimal.ZERO) > 0) {
			    	sql = String.format(Locale.US,
			    			"UPDATE OrcDespesaRH SET "
			    			+ "vrDespesa = %13.2f "
			    			+ "WHERE cdPlano = %d AND nrAnoMes = %d AND ID_Recurso = %d AND ID_Conta = %d", 
							orc.getVrOrcado(),						
			    			PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
			    			orc.getMesRef().getAnoMes(),
			    			funcionario.getCdRecurso(),
							despesa.getConta().getIdConta()
							);
			    	stmt.executeUpdate(sql);
		    	} else {
					sql = String.format("DELETE FROM OrcDespesaRH "
							+ "WHERE cdPlano = %d "
							+ "AND nrAnoMes = %d "
							+ "AND ID_Recurso = %d "
							+ "AND ID_Conta = %d", 
							PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
							orc.getMesRef().getAnoMes(),
							funcionario.getCdRecurso(),
							despesa.getConta().getIdConta());
					stmt.executeUpdate(sql);
		    	}
		    }
		    
		    rs.close();
		    
		    despesa.addVrDespesaAcum(orc.getVrOrcado());
		}
		
		stmt.close();
		rs = null;	
	}

	public static List<ValorTotalMes> listarOrcDespesaRH(Connection con, Recurso funcionario, ContaContabil despesa) 
			throws SQLException, NamingException {
		
		List<ValorTotalMes> lista = new ArrayList<ValorTotalMes>();
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		
		// Montar lista de meses
		for (int i = 1; i <= 12; i++) {
			MesAnoOrcamento mes = new MesAnoOrcamento();
			mes.setMes(i);
			
			ValorTotalMes valDesp = new ValorTotalMes();
			valDesp.setMesRef(mes);
			lista.add(valDesp);
		}
		
		String sql = String.format("SELECT * FROM OrcDespesaRH "
				+ "WHERE cdPlano = %d "
				+ "AND ID_Recurso = %d "
				+ "AND ID_Conta = %d "
				+ "ORDER BY nrAnoMes;",
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				funcionario.getCdRecurso(),
				despesa.getIdConta()
				);
		
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			
			for (ValorTotalMes desp : lista) {
				if (desp.getMesRef().getAnoMes() == rs.getInt("nrAnoMes")) {
					desp.setVrOrcado(rs.getBigDecimal("vrDespesa"));
							
					break;
				}
			}
		}
		
		rs.close();
		stmt.close();
		rs = null;
		stmt = null;
		
		return lista;
	}

}
