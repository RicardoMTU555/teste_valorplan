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

import com.ctbli9.valorplan.modelo.DespesaVenda;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.modelo.receita.Receita;
import com.ctbli9.valorplan.recursos.Global;

import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.modelo.Plano;
import ctbli9.recursos.RegraNegocioException;
import ctbli9.recursos.xls.GeraXLS;

public class ParamDeducaoServiceDAO {

	/*
	 * DESPESAS DE VENDA
	 */
	public static List<DespesaVenda> listarDespesaVenda(Connection con, Receita receita, Recurso funcionario)
			throws Exception {
		List<DespesaVenda> lista = new ArrayList<DespesaVenda>();
		Statement stmt = con.createStatement();
		
		String sql = String.format("SELECT ct.ID_Conta as ID, COALESCE(pd.nrAnoMes, 0) AS nrAnoMes, pd.pcDespesa, "
				+ "d.*, ct.*, c.*, g.* "
				+ "FROM CadDeducaoVenda d "
				+ "JOIN CtbConta ct ON d.ID_Conta = ct.ID_Conta "
				+ "LEFT OUTER JOIN CtbClasseDre c ON ct.cdClasse = c.cdClasse "
				+ "LEFT OUTER JOIN CtbGrupoClasse g ON c.cdGrupoClasse = g.cdGrupoClasse "
				+ "LEFT OUTER JOIN OrcParamDeducao pd ON pd.cdPlano = %d "
				+      "AND pd.nrAnoMes BETWEEN %d01 AND %d12 "
				+      "AND pd.cdReceita = d.cdReceita "
				+      "AND pd.ID_Conta = d.ID_Conta "
				+      "AND pd.ID_Recurso = %d "
				+ "WHERE d.cdReceita = %d "
				+ "ORDER BY ct.cdConta, pd.nrAnoMes", 
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				funcionario.getCdRecurso(),
				receita.getCdReceita());

		ResultSet rs = stmt.executeQuery(sql);
	    boolean temRegistro = rs.next();
	    while (temRegistro) {
	    	long monitor = rs.getLong("ID");
	    	
	    	DespesaVenda pd = new DespesaVenda();
	    	
	    	pd.setCdReceita(receita.getCdReceita());
	    	pd.setDeducao(ReceitaServiceDAO.carregarDeducaoReceita(con, rs));

	    	int mesAux = 1;
	    	MesAnoOrcamento mesRef = new MesAnoOrcamento();
		    mesRef.setMes(mesAux);
	    	BigDecimal[] despesas = new BigDecimal[12];
			do {
	    		if (temRegistro && rs.getInt("nrAnoMes") == mesRef.getAnoMes() && 
	    				monitor == rs.getLong("ID")) {
					
	    			despesas[mesRef.getMes()-1] = rs.getBigDecimal("pcDespesa");
					temRegistro = rs.next();
				}
				else {
					despesas[mesRef.getMes()-1] = null;
					
					if (temRegistro && rs.getInt("nrAnoMes") == 0 && monitor == rs.getLong("ID")) {
						temRegistro = rs.next();
					}
				}
	    		mesRef.add();
	    		mesAux++;
	    	} while ((temRegistro && monitor == rs.getLong("ID")) || mesAux <= 12);
	    	
	    	pd.setPercDespesa(despesas);
	   	
	    	
	    	lista.add(pd);
	    }//end while
	    
		stmt.close();
		stmt = null;
		
		rs.close();
		rs = null;
		return lista;
	}
	
	public static void atualizaParametros(PreparedStatement pstmtSel, PreparedStatement pstmtIns, PreparedStatement pstmtUpd, 
			PreparedStatement pstmtDel, 
			int cdPlano, int anoMes, int cdReceita, int idConta, long cdRecurso,
			BigDecimal pcDespesa) throws SQLException {
		
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
		pstmtSel.setInt(++ind, cdReceita);		
		pstmtSel.setInt(++ind, idConta);
		pstmtSel.setLong(++ind, cdRecurso);
		pstmtSel.setInt(++ind, anoMes);		
		
		ResultSet rs = pstmtSel.executeQuery();
		if (!rs.next()) {
			if (pcDespesa != null && pcDespesa.compareTo(BigDecimal.ZERO) > 0) {
				ind = 0;
				pstmtIns.setInt(++ind, cdPlano);
				pstmtIns.setInt(++ind, cdReceita);			
				pstmtIns.setInt(++ind, idConta);
				pstmtIns.setLong(++ind, cdRecurso);
				pstmtIns.setInt(++ind, anoMes);	
				
				pstmtIns.setBigDecimal(++ind, pcDespesa);
		    	
				pstmtIns.executeUpdate();		    	
			}
	    	
	    } else {
	    	if (pcDespesa != null && pcDespesa.compareTo(BigDecimal.ZERO) > 0) {
				ind = 0;
		    	pstmtUpd.setBigDecimal(++ind, pcDespesa);
	
				pstmtUpd.setInt(++ind, cdPlano);
		    	pstmtUpd.setInt(++ind, cdReceita);			
		    	pstmtUpd.setInt(++ind, idConta);
		    	pstmtUpd.setLong(++ind, cdRecurso);
		    	pstmtUpd.setInt(++ind, anoMes);			
		    	
		    	pstmtUpd.executeUpdate();
				
	    	} else {
	    		ind = 0;
		    	pstmtDel.setInt(++ind, cdPlano);
		    	pstmtDel.setInt(++ind, cdReceita);			
		    	pstmtDel.setInt(++ind, idConta);
		    	pstmtDel.setLong(++ind, cdRecurso);
		    	pstmtDel.setInt(++ind, anoMes);			
		    	
		    	pstmtDel.executeUpdate();				
	    	}
	    }
		rs.close();
		rs = null;
	}

	public static PreparedStatement inicializaSel(Connection con) throws SQLException, NamingException {
		String sql = "SELECT 1 FROM OrcParamDeducao p " + 
				"WHERE p.cdPlano = ? " + 
				"AND p.cdReceita = ? " + 
				"AND p.ID_Conta = ? " +
				"AND p.ID_Recurso = ? " + 
				"AND p.nrAnoMes = ?";
		return con.prepareStatement(sql);
	}
	
	public static PreparedStatement inicializaIns(Connection con) throws SQLException, NamingException {
		return con.prepareStatement("INSERT INTO OrcParamDeducao VALUES (?, ?, ?, ?, ?, ?)");
	}
	
	public static PreparedStatement inicializaUpd(Connection con) throws SQLException, NamingException {
		String sql = "UPDATE OrcParamDeducao p SET p.pcDespesa = ? " + 
					"WHERE p.cdPlano = ? " + 
					"AND p.cdReceita = ? " +  
					"AND p.ID_Conta = ? " +
					"AND p.ID_Recurso = ? " + 
					"AND p.nrAnoMes = ?"; 
		return con.prepareStatement(sql);
	}

	public static PreparedStatement inicializaDel(Connection con) throws SQLException, NamingException {
		String sql = "DELETE FROM OrcParamDeducao p " + 
				"WHERE p.cdPlano = ? " + 
				"AND p.cdReceita = ? " + 
				"AND p.ID_Conta = ? " +
				"AND p.ID_Recurso = ? " + 
				"AND p.nrAnoMes = ?";
		return con.prepareStatement(sql);
	}

	public static void gerarPlanilha(Connection con, String nomePlanilha) throws Exception {
		GeraXLS excel = new GeraXLS("Saldos", nomePlanilha);
		
		Statement stmt = con.createStatement();
		
		CentroCustoServiceDAO.montarCentrosCustoGerente(con, Global.getFuncionarioLogado(), null);
		
		String sql = String.format("SELECT lpad(cr.cdReceita, 7, '0') || lpad(eq.cdCentroCusto, 7, '0') || " +
				"lpad(opd.ID_Recurso, 10, '0') || lpad(opd.ID_Conta, 7, '0') AS ID, " +
				"cr.cdReceita, eq.cdCentroCusto, opd.ID_Recurso, eq.nomeVinculo, " +
				"ct.cdConta, ct.dsConta, eq.cecCdExterno, eq.cecDsResumida, " + //f.logUsuario, 
				"cr.sgReceita, cr.dsReceita, opd.pcDespesa, opd.nrAnoMes " +
				"FROM OrcParamDeducao opd  " +
				"JOIN sp_ListaRecurso(%d) eq ON opd.ID_Recurso = eq.ID_Recurso " + 
				"JOIN CadReceita cr ON opd.cdReceita = cr.cdReceita " +
				"JOIN CadDeducaoVenda dv ON opd.cdReceita = dv.cdReceita AND opd.ID_Conta = dv.ID_Conta " +
				"JOIN CtbConta ct ON opd.ID_Conta = ct.ID_Conta " +
				"JOIN TmpCencus ON TmpCencus.cdCentroCusto = eq.cdCentroCusto " + 
				"WHERE opd.cdPlano = %d " + 
				"ORDER BY cr.sgReceita, eq.cecCdExterno, eq.nomeVinculo, ct.cdConta, opd.nrAnoMes; ",
				new MesAnoOrcamento().getAnoMes(),
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano());
		
		System.out.println(sql);
		ResultSet res = stmt.executeQuery(sql);
		
		excel.setEstilo(GeraXLS.ESTILO_TITULO);
		String linha = "RECEITA|DESCRIÇÃO|CC|DESCR.RESUMIDA|COLABORADOR|DEDUÇÃO|DESCRIÇÃO|% MÊS 01|% MÊS 02|% MÊS 03|% MÊS 04|% MÊS 05|% MÊS 06|% MÊS 07|% MÊS 08|% MÊS 09|% MÊS 10|% MÊS 11|% MÊS 12";
		excel.linha(linha);
		int numLin = 1;
		int numCol = 0;

		excel.setEstilo(GeraXLS.ESTILO_NORMAL);
		
		boolean temRegistro = res.next();
		while (temRegistro) {
			String monitor = res.getString("ID");
			BigDecimal percDespesa = BigDecimal.ZERO;
			
			excel.escreveCelula(numLin, numCol++, res.getString("sgReceita"));
			excel.escreveCelula(numLin, numCol++, res.getString("dsReceita"));
			excel.escreveCelula(numLin, numCol++, res.getString("cecCdExterno"));
			excel.escreveCelula(numLin, numCol++, res.getString("cecDsResumida"));
			excel.escreveCelula(numLin, numCol++, res.getString("nomeVinculo"));
			excel.escreveCelula(numLin, numCol++, res.getString("cdConta"));
			excel.escreveCelula(numLin, numCol++, res.getString("dsConta"));
			
			int mes = 1;
			MesAnoOrcamento mesRef = new MesAnoOrcamento();
			try {
				do {
					mesRef.setMes(mes);
					if (res.getInt("nrAnoMes") == mesRef.getAnoMes() && monitor.equals(res.getString("ID"))) {
						excel.escreveCelula(numLin, numCol++, res.getDouble("pcDespesa"));
						percDespesa = res.getBigDecimal("pcDespesa");
						
						temRegistro = res.next();
						if (!temRegistro)
							break;
					}
					else {
						if (percDespesa.compareTo(BigDecimal.ZERO) > 0) {
							excel.escreveCelula(numLin, numCol++, "");
						} else
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

	public static void duplicarDadosCusvoVendaVendedor(Connection con, Receita receita, Recurso destino,
			List<DespesaVenda> listaParDespesas) throws NumberFormatException, SQLException, NamingException {

		Statement stmt = con.createStatement();
		
		Plano plano = PlanoServiceDAO.getPlanoSelecionado();

		PreparedStatement pstmtSel = inicializaSel(con); 
		PreparedStatement pstmtIns = inicializaIns(con); 
		PreparedStatement pstmtUpd = inicializaUpd(con); 
		PreparedStatement pstmtDel = inicializaDel(con); 
		
		for (DespesaVenda dv : listaParDespesas) {
			for (int i = 0; i < dv.getPercDespesa().length; i++) {
				atualizaParametros(pstmtSel, pstmtIns, pstmtUpd, pstmtDel, 
						plano.getCdPlano(),
						Integer.parseInt(String.format("%04d%02d", plano.getNrAno(), i+1)),
						receita.getCdReceita(),
						dv.getDeducao().getConta().getIdConta(),
						destino.getCdRecurso(),
						dv.getPercDespesa()[i]
						);				
			}
		}

		plano = null;
		
		pstmtSel.close();
		pstmtIns.close();
		pstmtUpd.close();
		pstmtDel.close();
		
		pstmtSel = null;
		pstmtIns = null;
		pstmtUpd = null;
		pstmtDel = null;
		
		/////res.close();
		/////res = null;
		
		stmt.close();
		stmt = null;
		
	}
}
