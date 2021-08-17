package com.ctbli9.valorplan.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.CategImobilizado;

public class CategImobilizadoServiceDAO {

	public static CategImobilizado pesquisarCategImobilizado(Connection con, int codigo) throws Exception {
		CategImobilizado imobilizado = null;

		Statement stmt = con.createStatement();
		String sql = String.format("SELECT * FROM CadCategImobili WHERE cdCategImobili = %d", 
				codigo);
		
		ResultSet rs = stmt.executeQuery(sql);
	    if (rs.next()) {
			imobilizado = new CategImobilizado();
			imobilizado.setCdCategImobili(rs.getInt("cdCategImobili"));
			imobilizado.setDsCategImobili(rs.getString("dsCategImobili"));
			imobilizado.setPcTaxaDepreciacao(rs.getBigDecimal("pcTaxaDeprec"));
			imobilizado.setPcResidual(rs.getBigDecimal("pcResidual"));
			imobilizado.setContaAtivo(ContaContabilServiceDAO.pesquisarIDConta(con, rs.getInt("ID_ContaAtivo")));
			imobilizado.setContaDepreciacao(ContaContabilServiceDAO.pesquisarIDConta(con, rs.getInt("ID_ContaDeprec")));
	    }//end while
		
	    rs.close();
	    rs = null;
		stmt.close();
		stmt = null;		
		
		return imobilizado;
	}
	

	public static boolean existe(Connection con, int codigo) throws SQLException, NamingException {
		return ConexaoDB.existeRegistro(con, "CadCategImobili", "cdCategImobili = " + codigo);
	}

	public static void incluir(Connection con, CategImobilizado categ) throws SQLException, NamingException {
		String sql = "INSERT INTO CadCategImobili VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		categ.setCdCategImobili(ConexaoDB.gerarNovoCodigo(con, "CadCategImobili", "cdCategImobili", null));
		
		int i = 0;
		pstmt.setInt(++i, categ.getCdCategImobili());
		pstmt.setString(++i, categ.getDsCategImobili());
		pstmt.setBigDecimal(++i, categ.getPcTaxaDepreciacao());
		pstmt.setBigDecimal(++i, categ.getPcResidual());
		pstmt.setInt(++i, categ.getContaAtivo().getIdConta());
		pstmt.setInt(++i, categ.getContaDepreciacao().getIdConta());
		
		pstmt.executeUpdate();
		
		pstmt.close();
		pstmt = null;
		
	}

	public static void alterar(Connection con, CategImobilizado categ) throws SQLException, NamingException {
		String sql = "UPDATE CadCategImobili SET "
				+ "dscategimobili = ?, "
				+ "pctaxadeprec   = ?, "
				+ "pcresidual     = ?, "
				+ "ID_ContaAtivo   = ?, "
				+ "ID_ContaDeprec  = ? "
				+ "WHERE cdcategimobili = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setString(++i, categ.getDsCategImobili());
		pstmt.setBigDecimal(++i, categ.getPcTaxaDepreciacao());
		pstmt.setBigDecimal(++i, categ.getPcResidual());
		pstmt.setInt(++i, categ.getContaAtivo().getIdConta());
		pstmt.setInt(++i, categ.getContaDepreciacao().getIdConta());
		pstmt.setInt(++i, categ.getCdCategImobili());
		
		pstmt.executeUpdate();
		
		pstmt.close();
		pstmt = null;
	}

	public static void excluir(Connection con, CategImobilizado categ) throws SQLException, NamingException {
		String sql = "DELETE FROM CadCategImobili "
				+ "WHERE cdcategimobili = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setInt(++i, categ.getCdCategImobili());
		
		pstmt.executeUpdate();
		
		pstmt.close();
		pstmt = null;
	}

}
