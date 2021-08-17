package com.ctbli9.valorplan.DAO.orc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.BalanceteServiceDAO;
import com.ctbli9.valorplan.enumeradores.TipoSaldo;
import com.ctbli9.valorplan.modelo.orc.OrcamentoDespesa;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

public class ProjecaoContasDAO {

public static TreeNode listarProjecaoContas(Connection con, MesAnoOrcamento mesAno) throws Exception {
		
		BalanceteServiceDAO dao = new BalanceteServiceDAO(con);
		dao.carregarBalancete(true, new int[0], 0, false);
		dao.close();
		dao = null;
		
		StringBuilder sql = new StringBuilder();
		
		sql.append(String.format(
				"SELECT 1, 'VENDAS' AS ID, cc.cecCdExterno, 'RE' AS tpConta, ct.cdConta, " +
				"tmp.ID_Conta, ct.sgConta, ct.dsConta, tmp.cdCentroCusto, cc.cecDsResumida, tmp.nrAnoMes, " + 
				"SUM(vlOrcado) AS valor " + 
				"FROM TmpSaldos tmp " +
				"JOIN EmpCentroCusto cc ON tmp.cdCentroCusto = cc.cdCentroCusto " + 
				"JOIN CtbConta ct ON tmp.ID_Conta = ct.ID_Conta " + 
				"WHERE tmp.tipo = '%s' AND tmp.nrAnoMes BETWEEN %04d01 AND %d " +
				"GROUP BY cc.cecCdExterno, ct.cdConta, tmp.ID_Conta, ct.sgConta, ct.dsConta, tmp.cdCentroCusto, cc.cecDsResumida, tmp.nrAnoMes ",
				TipoSaldo.REC,
				mesAno.getAno(),
				mesAno.getAnoMes()));
				
		sql.append(" union ");
		
		sql.append(String.format(
				"SELECT 2, 'DEDUÇOES' AS ID, cc.cecCdExterno, ct.tpConta, ct.cdConta, "
				+ "tmp.ID_Conta, ct.sgConta, ct.dsConta, tmp.cdCentroCusto, cc.cecDsResumida, tmp.nrAnoMes, " + 
				"SUM( vlOrcado * -1 ) AS valor " + 
				"FROM TmpSaldos tmp " +
				"JOIN EmpCentroCusto cc ON tmp.cdCentroCusto = cc.cdCentroCusto " + 
				"JOIN CtbConta ct ON tmp.ID_Conta = ct.ID_Conta " + 
				"WHERE tmp.tipo = '%s' AND tmp.nrAnoMes BETWEEN %04d01 AND %d AND ct.tpConta <> 'CT' " +
				"GROUP BY cc.cecCdExterno, ct.tpConta, ct.cdConta, tmp.ID_Conta, ct.sgConta, ct.dsConta, tmp.cdCentroCusto, cc.cecDsResumida, tmp.nrAnoMes ",
				TipoSaldo.DED,
				mesAno.getAno(),
				mesAno.getAnoMes()));
				
		sql.append(" union ");
		
		sql.append(String.format(
				"SELECT 3, 'CUSTO' AS ID, cc.cecCdExterno, ct.tpConta, ct.cdConta, " +
				"tmp.ID_Conta, ct.sgConta, ct.dsConta, tmp.cdCentroCusto, cc.cecDsResumida, tmp.nrAnoMes, " + 
				"SUM( vlOrcado * -1 ) AS valor " + 
				"FROM TmpSaldos tmp " +
				"JOIN EmpCentroCusto cc ON tmp.cdCentroCusto = cc.cdCentroCusto " + 
				"JOIN CtbConta ct ON tmp.ID_Conta = ct.ID_Conta " + 
				"WHERE tmp.tipo = '%s' AND tmp.nrAnoMes BETWEEN %04d01 AND %d AND ct.tpConta = 'CT' " +
				"GROUP BY cc.cecCdExterno, ct.tpConta, ct.cdConta, tmp.ID_Conta, ct.sgConta, ct.dsConta, tmp.cdCentroCusto, cc.cecDsResumida, tmp.nrAnoMes ",
				TipoSaldo.DED,
				mesAno.getAno(),
				mesAno.getAnoMes()));
				
		sql.append(" union ");
		
		sql.append(String.format(
				"SELECT 4, 'DESPESAS PESSOAL' AS ID, cc.cecCdExterno, ct.tpConta, ct.cdConta, " +
				"tmp.ID_Conta, ct.sgConta, ct.dsConta, tmp.cdCentroCusto, cc.cecDsResumida, tmp.nrAnoMes, " +
				"SUM( vlOrcado * -1 ) AS valor " + 
				"FROM TmpSaldos tmp " +
				"JOIN EmpCentroCusto cc ON tmp.cdCentroCusto = cc.cdCentroCusto " + 
				"JOIN CtbConta ct ON tmp.ID_Conta = ct.ID_Conta " + 
				"WHERE tmp.tipo = '%s' AND tmp.nrAnoMes BETWEEN %04d01 AND %d " +
				"GROUP BY cc.cecCdExterno, ct.tpConta, ct.cdConta, tmp.ID_Conta, ct.sgConta, ct.dsConta, " +
				"tmp.cdCentroCusto, cc.cecDsResumida, tmp.nrAnoMes ",
				TipoSaldo.DRH,
				mesAno.getAno(),
				mesAno.getAnoMes()));
		
		sql.append(" union ");
		
		sql.append(String.format(
				"SELECT 5, 'DESPESAS GERAIS' AS ID, cc.cecCdExterno, ct.tpConta, ct.cdConta, " +
				"tmp.ID_Conta, ct.sgConta, ct.dsConta, tmp.cdCentroCusto, cc.cecDsResumida, tmp.nrAnoMes, " +
				"SUM( vlOrcado * -1 ) AS valor " + 
				"FROM TmpSaldos tmp " +
				"JOIN EmpCentroCusto cc ON tmp.cdCentroCusto = cc.cdCentroCusto " + 
				"JOIN CtbConta ct ON tmp.ID_Conta = ct.ID_Conta " + 
				"WHERE tmp.tipo = '%s' AND tmp.nrAnoMes BETWEEN %04d01 AND %d " +
				"GROUP BY cc.cecCdExterno, ct.tpConta, ct.cdConta, tmp.ID_Conta, ct.sgConta, ct.dsConta, tmp.cdCentroCusto, cc.cecDsResumida, tmp.nrAnoMes ",
				TipoSaldo.DGE,
				mesAno.getAno(),
				mesAno.getAnoMes()));
			
		sql.append("ORDER BY 1, 5, 3, 11");
		System.out.println(sql.toString());
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql.toString());
		
		OrcamentoDespesa itemRaiz = new OrcamentoDespesa();
		itemRaiz.getConta().setDsConta("RAIZ");;
		TreeNode raiz = new DefaultTreeNode(itemRaiz, null);
		
		TreeNode[] eloDespesa = new TreeNode[3];
		
		String monitorID = "";
		String monitorCC = "";
		String monitorCt = "";
		boolean temRegistro = rs.next();
		while (temRegistro) {
			if (!monitorID.equals(rs.getString("ID"))) {
				OrcamentoDespesa despesa = new OrcamentoDespesa();
				despesa.getConta().setDsConta(rs.getString("ID"));
				eloDespesa[0] = new DefaultTreeNode(despesa, raiz);
				monitorID = rs.getString("ID");
				monitorCt = "";
				monitorCC = "";
			}
			
			if (!monitorCt.equals(rs.getString("cdConta"))) {
				OrcamentoDespesa despesa = new OrcamentoDespesa();
				despesa.getConta().setDsConta(rs.getString("cdConta").trim() + " - " +
						rs.getString("dsConta"));
				eloDespesa[1] = new DefaultTreeNode(despesa, eloDespesa[0]);				
				
				monitorCt = rs.getString("cdConta");
				monitorCC = "";
			}
			
			
			if (!monitorCC.equals(rs.getString("cecCdExterno"))) {
				OrcamentoDespesa despesa = new OrcamentoDespesa();
				despesa.getConta().setDsConta("Setor: " + rs.getString("cecCdExterno") + "-" +
						rs.getString("cecDsResumida"));
				eloDespesa[2] = new DefaultTreeNode(despesa, eloDespesa[1]);
				
				monitorCC = rs.getString("cecCdExterno");

				while (temRegistro &&
						monitorID.equals(rs.getString("ID")) &&
						monitorCt.equals(rs.getString("cdConta")) &&
						monitorCC.equals(rs.getString("cecCdExterno"))) {
				
					if (mesAno.getAnoMes() == rs.getInt("nrAnoMes")) {
						despesa.setVrConta(rs.getBigDecimal("valor"));
						((OrcamentoDespesa) eloDespesa[0].getData()).addVrDespesa(rs.getBigDecimal("valor"));
						((OrcamentoDespesa) eloDespesa[1].getData()).addVrDespesa(rs.getBigDecimal("valor"));						
					}

					despesa.addVrDespesaAcum(rs.getBigDecimal("valor"));
					((OrcamentoDespesa) eloDespesa[0].getData()).addVrDespesaAcum(rs.getBigDecimal("valor"));
					((OrcamentoDespesa) eloDespesa[1].getData()).addVrDespesaAcum(rs.getBigDecimal("valor"));

					/*if (rs.getString("tpConta").equals("SL") || rs.getString("tpConta").equals("DP"))
						mergulhaDP(con, eloDespesa[2], mesAno, rs.getInt("ID_Conta"), rs.getInt("cdCentroCusto"));
						
					if (rs.getString("tpConta").equals("RE"))
						mergulhaRec(con, eloDespesa[2], mesAno, rs.getInt("ID_Conta"), rs.getInt("cdCentroCusto"));
					*/
					
					temRegistro = rs.next();
				}
				
			}
		}
		
		sql = null;
		rs.close();
		stmt.close();
		
		rs = null;
		stmt = null;
		
		return raiz;
	}


/*
	private static void mergulhaRec(Connection con, TreeNode eloPai, MesAnoOrcamento mesAno, int idConta, int codCencus) 
			throws SQLException {
		String sql = String.format("SELECT r.sgReceita || ' - ' || r.dsReceita AS descricao, " + 
				" (rec.qtReceita*rec.vrUnitario) AS valor " + 
				"FROM OrcReceita rec " + 
				"JOIN CadReceita r ON rec.cdReceita = r.cdReceita " + 
				"JOIN OrcEquipe eq ON rec.ID_Recurso = eq.ID_Recurso " + 
				"WHERE rec.cdPlano = %d " + 
				"AND   rec.nrAnoMes = %d " + 
				"AND r.ID_ContaReceita = %d AND eq.cdCentroCusto = %d ", 
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				mesAno.getAnoMes(),
				idConta, 
				codCencus
				);
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			OrcamentoDespesa despesa = new OrcamentoDespesa();
			despesa.getConta().setDsConta(res.getString("descricao"));
			despesa.setVrDespesa(res.getBigDecimal("valor"));
			@SuppressWarnings("unused")
			DefaultTreeNode ultimoElo = new DefaultTreeNode(despesa, eloPai);	
		}
		
		res.close();
		stmt.close();
		
		res = null;
		stmt = null;
	}


	private static void mergulhaDP(Connection con, TreeNode eloPai, MesAnoOrcamento mesAno, int idConta, int codCencus) 
			throws SQLException {
		
		String sql = String.format("SELECT COALESCE(f.nmFuncionario, eq.nmRecurso) AS nomeFunc, od.vrDespesa " + 
				"FROM OrcDespesaRH od " + 
				"JOIN OrcEquipe eq ON od.ID_Recurso = eq.ID_Recurso " + 
				"LEFT OUTER JOIN OrcEquipeRecurs er ON eq.ID_Recurso = er.ID_Recurso AND er.nrAnoMesInicial <= od.nrAnoMes " + 
				           "AND (er.nrAnoMesFinal IS NULL OR er.nrAnoMesFinal >= od.nrAnoMes) " + 
				"LEFT OUTER JOIN CadFuncionario f ON er.cdFuncVinculo = f.cdFuncionario  " + 
				"WHERE od.cdPlano = %d " + 
				"AND od.nrAnoMes = %d " + 
				"AND od.ID_Conta = %d AND eq.cdCentroCusto = %d", 
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				mesAno.getAnoMes(),
				idConta, 
				codCencus
				);
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			OrcamentoDespesa despesa = new OrcamentoDespesa();
			despesa.getConta().setDsConta(res.getString("nomeFunc"));
			despesa.setVrDespesa(res.getBigDecimal("vrDespesa"));
			@SuppressWarnings("unused")
			DefaultTreeNode ultimoElo = new DefaultTreeNode(despesa, eloPai);	
		}
		
		res.close();
		stmt.close();
		
		res = null;
		stmt = null;
	}
*/

}
