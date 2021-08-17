package com.ctbli9.valorplan.DAO.orc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.ClassificacaoCtbServiceDAO;
import com.ctbli9.valorplan.DAO.ContaContabilServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.orc.OrcamentoDespesa;
import com.ctbli9.valorplan.modelo.orc.ValorTotalMes;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.modelo.Plano;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.DataUtil;

public class OrcDespesaGeralServiceDAO {
	
	public static List<OrcamentoDespesa> listarDespesaGeral(Connection con, CentroCusto cenCusto) throws Exception {
		List<OrcamentoDespesa> lista = new ArrayList<OrcamentoDespesa>();
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		
		// TODO aqui colocar filtro em ORCCONTARESPON
		
		String sql = String.format("SELECT ct.*, c.*, g.*, "
				+ "despAcum.acumulado, cct.cdClasse, dre.dsClasse01 "
				+ "FROM CtbConta ct "
				+ "LEFT OUTER JOIN CtbClaConta cct ON cct.nrAno = %d AND cct.ID_Conta = ct.ID_Conta "
				+             "AND (cct.cdCentroCusto = %d or cct.cdCentroCusto = 0)"
				+ "LEFT OUTER JOIN CtbClasseDre c ON c.cdClasse = cct.cdClasse "
				+ "LEFT OUTER JOIN CtbGrupoClasse g ON c.cdGrupoClasse = g.cdGrupoClasse "
				+ "LEFT OUTER JOIN "
				+ "( "
				+   "SELECT ID_Conta, SUM(vrDespesa) AS Acumulado "
				+   "FROM OrcDespesaGeral WHERE cdPlano = %d AND nrAnoMes BETWEEN %d01 AND %d12 AND cdCentroCusto = %d "
				+   "GROUP BY ID_Conta "
				+ ") AS despAcum ON ct.ID_Conta = despAcum.ID_Conta "
				+ "LEFT OUTER JOIN ( "
				+ ClassificacaoCtbServiceDAO.montaScriptClasseDRE(con, PlanoServiceDAO.getPlanoSelecionado().getNrAno(), 0) 
				+ " ) AS dre ON dre.cdClasse = cct.cdClasse "
				+ "WHERE ct.tpConta = 'DO' ",
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				cenCusto.getCdCentroCusto(),
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				cenCusto.getCdCentroCusto()
				);
		System.out.println(sql);
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			OrcamentoDespesa valDesp = new OrcamentoDespesa();
			
			valDesp.setConta(ContaContabilServiceDAO.carregarContaContabil(rs));
			valDesp.setCenCusto(null);
			valDesp.setVrConta(null);
			valDesp.setVrContaAcum(rs.getBigDecimal("acumulado"));
			valDesp.setClassifDRE(rs.getString("dsClasse01"));
			if (valDesp.getClassifDRE() == null || valDesp.getClassifDRE().isEmpty())
				valDesp.setClassifDRE("DESP.NÃO RELACIONADA");
			
			lista.add(valDesp);
		}
		
		rs.close();
		stmt.close();
		rs = null;
		stmt = null;
		
		return lista;
	}
	
	

	/*
	 * Grava o orcamento de despesas avulsas
	 */
	public static void gravarOrcDespesaGeral(Connection con, Plano plano, 
			CentroCusto cenCusto, OrcamentoDespesa despesa, List<ValorTotalMes> valores) 
			throws SQLException, NamingException {
		
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		
		String sql = "";
		
		despesa.setVrContaAcum(BigDecimal.ZERO);
		// TODO bloquear ano anterior
		for (ValorTotalMes orc : valores) {
			sql = String.format("SELECT 1 FROM OrcDespesaGeral orc "
					+ "WHERE orc.cdPlano = %d AND orc.nrAnoMes = %d AND orc.cdCentroCusto = %d AND orc.ID_Conta = %d", 
					plano.getCdPlano(),
					orc.getMesRef().getAnoMes(),
					cenCusto.getCdCentroCusto(),
					despesa.getConta().getIdConta());
			
		    rs = stmt.executeQuery(sql);
		    if (!rs.next()) {
		    	if (orc.getVrOrcado().compareTo(BigDecimal.ZERO) > 0) {
			    	sql = String.format(Locale.US,
			    			"INSERT INTO OrcDespesaGeral VALUES "
			    			+ "(%d, %d, %d, %d, %13.2f)", 
			    			plano.getCdPlano(),
			    			orc.getMesRef().getAnoMes(),
			    			cenCusto.getCdCentroCusto(),
							despesa.getConta().getIdConta(),
							orc.getVrOrcado()
							);
			    	stmt.executeUpdate(sql);
		    	}
		    } else {
		    	
		    	if (orc.getVrOrcado().compareTo(BigDecimal.ZERO) > 0) {
				    sql = String.format(Locale.US,
			    			"UPDATE OrcDespesaGeral SET "
			    			+ "   vrDespesa = %13.2f "
			    			+ "WHERE cdPlano = %d AND nrAnoMes = %d AND cdCentroCusto = %d AND ID_Conta = %d", 
			    			orc.getVrOrcado(),						
			    			plano.getCdPlano(),
			    			orc.getMesRef().getAnoMes(),
							cenCusto.getCdCentroCusto(),
							despesa.getConta().getIdConta()
							);
			    	stmt.executeUpdate(sql);
		    	} else {
		    		sql = String.format(Locale.US,
			    			"DELETE FROM OrcDespesaGeral "
			    			+ "WHERE cdPlano = %d AND nrAnoMes = %d AND cdCentroCusto = %d AND ID_Conta = %d", 
			    			plano.getCdPlano(),
			    			orc.getMesRef().getAnoMes(),
							cenCusto.getCdCentroCusto(),
							despesa.getConta().getIdConta()
							);
			    	stmt.executeUpdate(sql);
		    	}
		    }
		    
		    rs.close();
		    
		    despesa.addVrDespesaAcum(orc.getVrOrcado());
		}
		
		stmt.close();
		rs = null;	
	}
	

	
	

	public static List<ValorTotalMes> listarOrcDespesaGeral(Connection con, CentroCusto cenCusto, ContaContabil despesa) 
			throws SQLException, NamingException {
		
		List<ValorTotalMes> lista = new ArrayList<ValorTotalMes>();
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		
		for (int i = 1; i <= 12; i++) {
			MesAnoOrcamento mes = new MesAnoOrcamento();
			mes.setMes(i);
			
			ValorTotalMes valDesp = new ValorTotalMes();
			valDesp.setMesRef(mes);
			lista.add(valDesp);
		}
		
		String sql = String.format("SELECT * FROM OrcDespesaGeral "
				+ "WHERE cdPlano = %d "
				+ "AND cdCentroCusto = %d "
				+ "AND ID_Conta = %d "
				+ "ORDER BY nrAnoMes;",
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				cenCusto.getCdCentroCusto(),
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
	
	public static List<ValorTotalMes> valoresHistoricos(Connection con, CentroCusto cenCusto,
			ContaContabil conta, int anoAnt) throws SQLException {
		
		List<ValorTotalMes> lista = new ArrayList<ValorTotalMes>();
		for (int i = 1; i <= 12; i++) {
			MesAnoOrcamento mes = new MesAnoOrcamento();
			mes.setAno(anoAnt);
			mes.setMes(i);
			
			ValorTotalMes valDesp = new ValorTotalMes();
			valDesp.setMesRef(mes);
			lista.add(valDesp);
		}
		
		//1. Achar Plano no ano anterior
		String sql = String.format("SELECT max(cdPlano) as cod FROM OrcPlano WHERE nrAno = %d "
				+ "AND stPlano = (SELECT MIN(stPlano) FROM OrcPlano WHERE nrAno = %d AND stPlano <> 0)",
				anoAnt,
				anoAnt
				);
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		if (res.next()) {
			int codPlano = res.getInt("cod");
			
			// ORÇADO
			sql = String.format("SELECT * FROM OrcDespesaGeral des " + 
					"WHERE des.cdPlano = %d" + 
					"AND des.nrAnoMes BETWEEN %04d01 and %04d12 " + 
					"AND des.cdCentroCusto = %d " + 
					"AND des.ID_Conta = %d " + 
					"ORDER BY nrAnoMes;", 
					codPlano,
					anoAnt,
					anoAnt,
					cenCusto.getCdCentroCusto(),
					conta.getIdConta());
			
			res.close();
			
			res = stmt.executeQuery(sql);
			while (res.next()) {
				for (ValorTotalMes desp : lista) {
					if (desp.getMesRef().getAnoMes() == res.getInt("nrAnoMes")) {
						desp.setVrOrcado(res.getBigDecimal("vrDespesa"));
								
						break;
					}
				}
			}
			
			// REALIZADO
			Date dataI = DataUtil.stringToData(String.format("%04d-01-01",anoAnt), "yyyy-MM-dd");
			Date dataF = DataUtil.stringToData(String.format("%04d-12-31", anoAnt), "yyyy-MM-dd");
			
			sql = String.format("SELECT EXTRACT(YEAR FROM dtLancto) || LPAD(EXTRACT(MONTH FROM dtLancto), 2, '0') as nrAnoMes, "
					+ "SUM( CASE WHEN idNatMovto = 'C' THEN vlMovto ELSE vlMovto * -1 END ) AS vrDespesa "
					+ "FROM CtbLancto "
					+ "WHERE cdCentroCusto = %d "
					+ "AND ID_Conta = %d "
					+ "AND dtLancto BETWEEN '%s' AND '%s' "
					+ "GROUP BY 1",
					cenCusto.getCdCentroCusto(),
					conta.getIdConta(),
					DataUtil.dataString(dataI, "MM/dd/yyyy"),
					DataUtil.dataString(dataF, "MM/dd/yyyy")
					);
			res.close();
			
			res = stmt.executeQuery(sql);
			while (res.next()) {
				for (ValorTotalMes desp : lista) {
					if (desp.getMesRef().getAnoMes() == res.getInt("nrAnoMes")) {
						desp.setVrRealizado(res.getBigDecimal("vrDespesa"));
								
						break;
					}
				}
			}
		}
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		
		return lista;
	}	
}

