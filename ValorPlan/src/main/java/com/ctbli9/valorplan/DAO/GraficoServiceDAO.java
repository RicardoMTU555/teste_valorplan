package com.ctbli9.valorplan.DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import com.ctbli9.valorplan.enumeradores.TipoSaldo;
import com.ctbli9.valorplan.modelo.DadosGrafico;
import com.ctbli9.valorplan.modelo.DadosGraficoFilial;
import com.ctbli9.valorplan.modelo.NivelArvore;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.recursos.RegraNegocioException;

public class GraficoServiceDAO {
	
	public static List<String> listarAreas(Connection con, NivelArvore nivel, int nivelGrafico, boolean produtivo) throws SQLException {
		List<String> lista = new ArrayList<String>();
		
		String sql = String.format("SELECT DISTINCT arvore.dep%02d AS descricao FROM tmpsaldos saldo "
				+ "JOIN EmpCencusDepto cdep ON saldo.cdCentroCusto = cdep.cdCentroCusto "
				+ "JOIN (" + DepartamentoServiceDAO.montaScriptArvore(nivel.getMaximo()) 
				+ ") arvore ON arvore.niv%02d = cdep.cdDepartamento "
				+ (produtivo ? "WHERE saldo.tipo = '"+ TipoSaldo.REC + "'" : ""),
				nivelGrafico,
				nivel.getMaximo());
		System.out.println(sql);
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
					
		while (res.next()) {
			lista.add(res.getString("descricao"));
		}
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
				
		return lista;
	}

	
	public static List<DadosGrafico> gerarValoresRecDed(Connection con, List<String> listaArea, NivelArvore nivel, int nivelGrafico) 
			throws SQLException, RegraNegocioException {
		
		// Carrega grafico
		MesAnoOrcamento anoRef = new MesAnoOrcamento();
		List<DadosGrafico> lista = carregarListaVazia(anoRef, listaArea);
		
		String sql = null;
		String[] tipos = new String[] {TipoSaldo.REC.toString(), TipoSaldo.DED.toString()};
		
		for (int i = 0; i < tipos.length; i++) {
			sql = String.format("SELECT saldo.nrAnoMes, arvore.dep%02d AS descricao, "
					+ "SUM(vlOrcado) AS Valor "
					+ "FROM tmpsaldos saldo "
					+ "JOIN EmpCencusDepto cdep ON saldo.cdCentroCusto = cdep.cdCentroCusto "
					+ "JOIN "
					+ "(" + DepartamentoServiceDAO.montaScriptArvore(nivel.getMaximo()) + ")  arvore ON arvore.niv%02d = cdep.cdDepartamento "
					+ "WHERE saldo.tipo = '" + tipos[i] + "' "
					+ "GROUP BY 1, 2 HAVING SUM(vlOrcado) > 0",
					nivelGrafico,
					nivel.getMaximo());
			
			carregaGraficoRecDed(con, anoRef, sql, tipos[i], lista);
		}
		
		return lista;
	}

	
	public static List<DadosGrafico> gerarGraficoDespesaGeral(Connection con, List<String> listaArea, NivelArvore nivel, int nivelGrafico) 
			throws SQLException, RegraNegocioException {
		
		MesAnoOrcamento anoRef = new MesAnoOrcamento();
		
		List<DadosGrafico> lista = carregarListaVazia(anoRef, listaArea);
		
		String sql = String.format("SELECT saldo.nrAnoMes, arvore.dep%02d AS descricao, "
				+ "SUM(vlOrcado) AS Valor "
				+ "FROM tmpsaldos saldo "
				+ "JOIN EmpCencusDepto cdep ON saldo.cdCentroCusto = cdep.cdCentroCusto "
				+ "JOIN "
				+ "(" + DepartamentoServiceDAO.montaScriptArvore(nivel.getMaximo()) 
				+ ")  arvore ON arvore.niv%02d = cdep.cdDepartamento "
				+ "WHERE saldo.tipo = '%s' "
				+ "GROUP BY 1, 2",
				nivelGrafico,
				nivel.getMaximo(),
				TipoSaldo.DGE);
		
		carregaGraficoRecDed(con, anoRef, sql, TipoSaldo.DGE.toString(), lista);
		
		return lista;
	}
	
	public static List<DadosGraficoFilial> gerarGraficoResultadoFilial(Connection con) throws SQLException, RegraNegocioException {
		
		List<DadosGraficoFilial> lista = new ArrayList<DadosGraficoFilial>();
		
		String sql = "SELECT saldo.nrAnomes, saldo.cdFilial, fil.sgFilial, " + 
				"       SUM(CASE WHEN saldo.tipo = '" + TipoSaldo.REC + "' THEN vlOrcado ELSE 0 END) AS valorReceita, " + 
				"       SUM(CASE WHEN saldo.tipo = '" + TipoSaldo.REC + "' THEN 0 ELSE vlOrcado * -1 END) AS valorDespesa " + 
				"FROM tmpsaldos saldo " + 
				"JOIN EmpFilial fil ON saldo.cdFilial = fil.cdFilial " + 
				"GROUP BY 1, 2, 3";
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		MesAnoOrcamento mesRef = new MesAnoOrcamento();
		while (res.next()) {
			mesRef.setAnoMes(res.getInt("nrAnoMes"));
			
			DadosGraficoFilial graf = new DadosGraficoFilial();
			graf.setSiglaMes(mesRef.getNomeMes().substring(0, 3));
			graf.setSiglaFilial(res.getString("sgFilial"));
			graf.setReceita(res.getBigDecimal("valorReceita"));
			graf.setDespesa(res.getBigDecimal("valorDespesa"));
			
			
			lista.add(graf);
		}
		
		stmt.close();
		stmt = null;
		
		return lista;
	}
	

	public static List<DadosGrafico> gerarGraficoDespesaRH(Connection con, List<String> listaArea, NivelArvore nivel, int nivelGrafico) 
			throws SQLException, RegraNegocioException {
		
		MesAnoOrcamento anoRef = new MesAnoOrcamento();
		
		List<DadosGrafico> lista = carregarListaVazia(anoRef, listaArea);
		
		String sql = String.format("SELECT saldo.nrAnoMes, arvore.dep%02d AS descricao, "
				+ "SUM(vlOrcado) AS Valor "
				+ "FROM tmpsaldos saldo "
				+ "JOIN EmpCencusDepto cdep ON saldo.cdCentroCusto = cdep.cdCentroCusto "
				+ "JOIN "
				+ "(" + DepartamentoServiceDAO.montaScriptArvore(nivel.getMaximo())
				+ ")  arvore ON arvore.niv%02d = cdep.cdDepartamento "
				+ "WHERE saldo.tipo = '%s' "
				+ "GROUP BY 1, 2",
				nivelGrafico,
				nivel.getMaximo(),
				TipoSaldo.DRH);
		
		carregaGraficoRecDed(con, anoRef, sql, TipoSaldo.DRH.toString(), lista);
		
		return lista;
	}

	
	
	
	private static List<DadosGrafico> carregarListaVazia(MesAnoOrcamento anoRef, List<String> listaArea) {
		List<DadosGrafico> lista = new ArrayList<DadosGrafico>();
		
		for (SelectItem mes : anoRef.getMeses()) {
			DadosGrafico graf = new DadosGrafico();
			graf.setSiglaMes(mes.getLabel().substring(0, 3));
			
			for (String descricao : listaArea) {
				graf.getTipoReceita().add(descricao);
				graf.getReceita().add(BigDecimal.ZERO);
				graf.getDeducao().add(BigDecimal.ZERO);
			}
			lista.add(graf);
		}
		return lista;
	}

	private static void carregaGraficoRecDed(Connection con, MesAnoOrcamento anoRef, String sql, String tipo, List<DadosGrafico> lista) 
			throws SQLException, RegraNegocioException {

		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		boolean temRegistro = res.next();
		while (temRegistro) {
			anoRef.setAnoMes(res.getInt("nrAnoMes"));
			
			do {
				for (DadosGrafico graf : lista) {
					if(graf.getSiglaMes().equals(anoRef.getNomeMes().substring(0, 3))) {
						int ind = graf.getTipoReceita().indexOf(res.getString("descricao"));
						if (ind == -1)
							System.out.println(res.getString("descricao"));
						
						if (tipo.equals(TipoSaldo.REC.toString()))
							graf.getReceita().set(ind, res.getBigDecimal("valor"));
						else
							graf.getDeducao().set(ind, res.getBigDecimal("valor").multiply(new BigDecimal("-1")));
						break;
					}
				}
				temRegistro = res.next();
				
			} while (temRegistro && anoRef.getAnoMes() == res.getInt("nrAnoMes"));
			
		}//while
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
	}

}
