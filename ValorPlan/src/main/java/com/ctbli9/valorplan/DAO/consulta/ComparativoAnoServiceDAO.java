package com.ctbli9.valorplan.DAO.consulta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.orc.ComparativoAno;

import ctbli9.modelo.Filial;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.RegraNegocioException;

public class ComparativoAnoServiceDAO {
	private boolean conexaoEstabelecida = false;
	Connection con;
	private String sql;
	private Statement stmt;
	private ResultSet res;
	
	public ComparativoAnoServiceDAO(ConexaoDB con) throws SQLException {
		this.con = con.getConexao();
		stmt = this.con.createStatement();
		
		conexaoEstabelecida = true;
	}
	
	public void close() {
		if (conexaoEstabelecida) {
			try {
				con = null;
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		sql = null;
		res = null;
		stmt = null;
	}

	

	public List<ComparativoAno> listarContasTotalAno(int anoAnterior, int anoAtual) throws RegraNegocioException, SQLException {
		List<ComparativoAno> lista = new ArrayList<>();
		
		
		
		sql = String.format(
				"SELECT base.*, ctb.cdConta, ctb.sgConta, ctb.dsConta, cc.cecCdExterno, cc.cecDsResumida, cc.cdFilial, fil.sgFilial, " +
				"saldo%d.realizado, saldo%d.Orcado " + 
				"FROM (SELECT DISTINCT ID_Conta, cdCentroCusto FROM tmpSaldos) AS base " + 
				"JOIN CtbConta ctb ON base.ID_Conta = ctb.ID_Conta " + 
				"JOIN EmpCentroCusto cc ON base.cdCentroCusto = cc.cdCentroCusto " + 
				"JOIN EmpFilial fil ON cc.cdFilial = fil.cdFilial " +
				"LEFT OUTER JOIN " + 
				"( " + 
				" SELECT ID_Conta, cdCentroCusto, SUM(vlRealizado) as realizado FROM tmpSaldos " + 
				" WHERE substring(nrAnoMes from 1 for 4) = %d " + 
				" GROUP BY 1, 2 " + 
				") as saldo%d ON base.ID_Conta = saldo%d.ID_Conta AND base.cdCentroCusto = saldo%d.cdCentroCusto " + 
				"LEFT OUTER JOIN " + 
				"( " + 
				" SELECT ID_Conta, cdCentroCusto, SUM(vlOrcado) as orcado FROM tmpSaldos " + 
				" WHERE substring(nrAnoMes from 1 for 4) = %d " + 
				" GROUP BY 1, 2 " + 
				") as saldo%d ON base.ID_Conta = saldo%d.ID_Conta AND base.cdCentroCusto = saldo%d.cdCentroCusto ;",
				anoAnterior,
				anoAtual,
				
				anoAnterior,
				anoAnterior,
				anoAnterior,
				anoAnterior,
				
				anoAtual,
				anoAtual,
				anoAtual,
				anoAtual
				);
		
		res = stmt.executeQuery(sql);
		
		while (res.next()) {
			ComparativoAno conta = new ComparativoAno();
			
			conta.setConta(new ContaContabil(res.getInt("ID_Conta"),
					res.getString("cdConta"),
					res.getString("sgConta"),
					res.getString("dsConta")));
			conta.setSetor(new CentroCusto(res.getInt("cdCentroCusto"),
					res.getString("cecCdExterno"), res.getString("cecDsResumida")));
			conta.getSetor().setFilial(new Filial(res.getInt("cdFilial"), res.getString("sgFilial")));
			
			conta.setValorOrcadoAtual(res.getBigDecimal("orcado"));
			conta.setValorRealizadoAnterior(res.getBigDecimal("realizado"));
			
			lista.add(conta);
		}
		
		return lista;
	}
	
	
	
}
