package com.ctbli9.valorplan.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import com.ctbli9.valorplan.modelo.CentroCusto;
import ctbli9.modelo.ctb.ContaContabil;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.RelatorioBI;

public class RelatorioBIServiceDAO {

	public static RelatorioBI pesquisaDescricao(Connection con, RelatorioBI campo) throws SQLException, NamingException {
		
		Statement stmt = con.createStatement();
		
		String sql = String.format("SELECT * FROM CadCampoBI "
				+ "WHERE nrAno = %d "
				+ "AND dsCampoBI = '%s'", 
				campo.getNrAno(),
				campo.getDsCampoBI());
		ResultSet rs = stmt.executeQuery(sql);
		
	    if(rs.next()) {
	    	campo.setCdCampoBI(rs.getInt("cdCampoBI"));
	    }
		
	    rs.close();
		stmt.close();
	    rs = null;
		stmt = null;
		
		return campo;
	}

	public static void incluir(Connection con, RelatorioBI campo) throws SQLException, NamingException {
		campo.setCdCampoBI(ConexaoDB.gerarNovoCodigo(con, "CadCampoBI", "cdCampoBI", null));
		
		String sql = "INSERT INTO CadCampoBI "
				+ "(cdcampobi, nrano, dscampobi) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setInt(++i, campo.getCdCampoBI());
		pstmt.setInt(++i, campo.getNrAno());
		pstmt.setString(++i, campo.getDsCampoBI());

		pstmt.executeUpdate();

		pstmt.close();
		pstmt = null;
	}

	public static boolean existeRelatorioBI(Connection con, ContaContabil ctaContabil, CentroCusto cencus) 
			throws SQLException, NamingException {
		Statement stmt = con.createStatement();

		String sql = String.format("SELECT DISTINCT 1 FROM CadRelatorioBI WHERE cdConta = '%s' AND cdCentroCusto = %d", 
				ctaContabil.getCdConta(),
				cencus.getCdCentroCusto()
				);			
		ResultSet res = stmt.executeQuery(sql);
		
		boolean retorno = res.next();

		res.close();
		res = null;
		stmt.close();		
		stmt = null;
		
		return retorno;
	}

	public static void incluirRelatorioBI(Connection con, ContaContabil ctaContabil, CentroCusto cencus, 
			RelatorioBI campo1, RelatorioBI campo2, RelatorioBI campo3, RelatorioBI campo4) 
					throws SQLException, NamingException {
		String sql = "INSERT INTO CadRelatorioBI "
				+ "(cdconta, cdcentrocusto, cdcampobi1, cdcampobi2, cdcampobi3, cdcampobi4) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setString(++i, ctaContabil.getCdConta());
		pstmt.setInt(++i, cencus.getCdCentroCusto());
		pstmt.setInt(++i, campo1.getCdCampoBI());
		pstmt.setInt(++i, campo2.getCdCampoBI());
		pstmt.setInt(++i, campo3.getCdCampoBI());
		pstmt.setInt(++i, campo4.getCdCampoBI());

		pstmt.executeUpdate();

		pstmt.close();
		pstmt = null;
	}

}
