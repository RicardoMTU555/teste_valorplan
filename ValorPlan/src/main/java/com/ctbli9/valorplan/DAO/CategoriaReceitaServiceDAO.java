package com.ctbli9.valorplan.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.enumeradores.MedidaOrcamento;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;

import ctbli9.recursos.LibUtil;

public class CategoriaReceitaServiceDAO {

	public static List<CategoriaReceita> listarCategoriaReceita(Connection con, String termoPesquisa) 
			throws SQLException, NamingException {
		List<CategoriaReceita> lista = new ArrayList<CategoriaReceita>();

		String sql = "SELECT * FROM CadCategReceita cat ";
		if (termoPesquisa != null && !termoPesquisa.isEmpty())
			sql += String.format(" WHERE dsCategoria LIKE '%s'", "%" + termoPesquisa + "%");
			
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
	    while (rs.next()) {
	    	lista.add(carregarCategoria(rs));
	    }
		
		stmt.close();
		stmt = null;		
		
		return lista;
	}
	
	public static List<CategoriaReceita> listarCategCencusto(Connection con, int cdCentroCusto) throws SQLException {
		List<CategoriaReceita> lista = new ArrayList<>();
		
		String sql = "SELECT * FROM CadCategReceita cat "
				+ "JOIN CadCencusCatrec ccct ON cat.cdCategoria = ccct.cdCategoria AND ccct.cdCentroCusto = "
				+ cdCentroCusto;
			
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
	    while (rs.next()) {
	    	lista.add(carregarCategoria(rs));
	    }
		
	    return lista;
	}
	
	public static void incluirCategoriaReceita(Connection con, CategoriaReceita categ) throws SQLException, NamingException {
		
		categ.setCdCategoria(ConexaoDB.gerarNovoCodigo(con, "CadCategReceita", "cdCategoria", null));
		
		String sql = "INSERT INTO CadCategReceita (cdCategoria, dsCategoria, tpOrcamento) VALUES (?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		int i = 0;
		pstmt.setInt(++i, categ.getCdCategoria());
		pstmt.setString(++i, categ.getDsCategoria());
		pstmt.setString(++i, categ.getMedida().toString());
		pstmt.executeUpdate();
		pstmt.close();
		pstmt = null;
	}
	
	public static void alterarCategoriaReceita(Connection con, CategoriaReceita categ) throws SQLException, NamingException {
		String sql = "UPDATE CadCategReceita "
				+ "SET dsCategoria = ?, "
				+ "tpOrcamento = ? "
				+ "WHERE cdCategoria = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		int i = 0;
		pstmt.setString(++i, categ.getDsCategoria());
		pstmt.setString(++i, categ.getMedida().toString());
		pstmt.setInt(++i, categ.getCdCategoria());
		pstmt.executeUpdate();

		pstmt.close();
		pstmt = null;
	}

	public static void excluirCategoriaReceita(Connection con, CategoriaReceita categ) throws SQLException, NamingException {
		String sql = "DELETE FROM CadCategReceita WHERE cdCategoria = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		int i = 0;
		pstmt.setInt(++i, categ.getCdCategoria());
		pstmt.executeUpdate();

		pstmt.close();
		pstmt = null;		
	}

	public static CategoriaReceita pesquisarCategoria(Connection con, int codigo) throws SQLException, NamingException {
		CategoriaReceita categ = null;
		Statement stmtm = con.createStatement();
		
		String sql = String.format("SELECT * FROM CadCategReceita cat WHERE cat.cdCategoria = %d", codigo);
	    ResultSet rs = stmtm.executeQuery(sql);
	    if (rs.next()) {
	    	categ = carregarCategoria(rs);
	    }
				
	    stmtm.close();
		rs.close();
		rs = null;
		return categ;
	}
	
	public static CategoriaReceita pesquisarCategoriaPorDescricao(Connection con, String descricao) throws SQLException, NamingException {
		descricao = LibUtil.truncaTexto(descricao, 25);
		
		CategoriaReceita categ = null;
		Statement stmtm = con.createStatement();
		
		String sql = String.format("SELECT * FROM CadCategReceita cat WHERE cat.dsCategoria = '%s'", descricao);
	    ResultSet rs = stmtm.executeQuery(sql);
	    if (rs.next()) {
	    	categ = carregarCategoria(rs);
	    }
				
	    stmtm.close();
		rs.close();
		rs = null;
		return categ;
	}
	
	public static CategoriaReceita carregarCategoria(ResultSet rs) throws SQLException {
		CategoriaReceita categ = new CategoriaReceita();
		
		categ.setCdCategoria(rs.getInt("cdCategoria"));
		categ.setDsCategoria(rs.getString("dsCategoria"));
		categ.setMedida(MedidaOrcamento.valueOf(rs.getString("tpOrcamento")));
		return categ;
	}
	
	
}
