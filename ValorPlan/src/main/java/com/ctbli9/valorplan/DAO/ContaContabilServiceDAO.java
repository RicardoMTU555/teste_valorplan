package com.ctbli9.valorplan.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.ctbli9.valorplan.modelo.ctb.EstornoConta;

import ctbli9.modelo.ctb.ContaContabil;

public class ContaContabilServiceDAO extends ctbli9.DAO.ContaContabilServiceDAO {
	
	public static ContaContabil pesquisarContaReduzida(Connection con, String cdConta) throws Exception {
		Statement stmt = con.createStatement();
		
		ContaContabil conta = null;
		
		String sql = String.format("SELECT ctb.*, 'S' as TemConta "
				+ "FROM CtbConta ctb "
				+ "WHERE cdContaReduzi = '%s'", cdConta);
	    ResultSet rs = stmt.executeQuery(sql);
	    if(rs.next()) {
	    	conta = carregarContaContabil(rs);
	    }
		stmt.close();
		stmt = null;
		rs.close();
		rs = null;	
		
		return conta;
	}//perquisarConta

	public static List<EstornoConta> listarEstornoConta(Connection con) throws Exception {
		List<EstornoConta> lista = new ArrayList<EstornoConta>();
		
		Statement stmt = con.createStatement();
		
		EstornoConta conta = null;
		
		String sql = "SELECT * FROM CtbEstornaConta";
	    ResultSet rs = stmt.executeQuery(sql);
	    while (rs.next()) {
	    	conta = new EstornoConta();
	    	conta.setConta(ContaContabilServiceDAO.pesquisarIDConta(con, rs.getInt("ID_Conta")));
	    	conta.setCenCusto(CentroCustoServiceDAO.pesquisarCentroCusto(con, rs.getInt("cdCentroCusto")));
	    	lista.add(conta);
	    }
		
	    stmt.close();
		stmt = null;
		rs.close();
		rs = null;	
		
		return lista;
	}

	public static void gravarEstornoConta(Connection con, EstornoConta conta) throws SQLException, NamingException {
		String sql = "INSERT INTO CtbEstornaConta VALUES (?, ?)";
		PreparedStatement pstmtIns = con.prepareStatement(sql);
		
		sql = "UPDATE CtbEstornaConta SET "
				+ "cdCentroCusto = ? "
				+ "WHERE ID_Conta = ?";
		PreparedStatement pstmtUpd = con.prepareStatement(sql);
		
		sql = "SELECT 1 FROM CtbEstornaConta WHERE ID_Conta = ?";
		PreparedStatement pstmtPesq = con.prepareStatement(sql);
		ResultSet res = null;
		
		int i;
		
		i = 0;
		pstmtPesq.setInt(++i, conta.getConta().getIdConta());
		res = pstmtPesq.executeQuery();
		if (res.next()) {
			i = 0;
			pstmtUpd.setInt(++i, conta.getCenCusto().getCdCentroCusto());
			pstmtUpd.setInt(++i, conta.getConta().getIdConta());
			pstmtUpd.executeUpdate();
			
		} else {
			i = 0;
			pstmtIns.setInt(++i, conta.getConta().getIdConta());
			pstmtIns.setInt(++i, conta.getCenCusto().getCdCentroCusto());
			pstmtIns.executeUpdate();			
		}
				
		res.close();
		res = null;
		
		pstmtPesq.close();
		pstmtUpd.close();
		pstmtIns.close();
		
		pstmtPesq = null;
		pstmtUpd = null;
		pstmtIns = null;
	}

	public static void excluirEstornoConta(Connection con, EstornoConta conta) throws SQLException, NamingException {
		String sql = "DELETE FROM CtbEstornaConta WHERE ID_Conta = ?";
		PreparedStatement pstmtDel = con.prepareStatement(sql);
		
		int i = 0;
		pstmtDel.setInt(++i, conta.getConta().getIdConta());
		pstmtDel.executeUpdate();
		
		pstmtDel.close();
		pstmtDel = null;
	}

}
