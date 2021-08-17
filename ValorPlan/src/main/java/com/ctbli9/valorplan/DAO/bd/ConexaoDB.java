package com.ctbli9.valorplan.DAO.bd;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import ctbli9.recursos.LibUtil;

public class ConexaoDB {
	private Connection conexao;
	
	public ConexaoDB() throws SQLException {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/" + LibUtil.getUsuarioSessao().getContrato().getAlias());

			this.conexao = ds.getConnection();
			
			this.conexao.setAutoCommit(false);
			
			ds = null;
			ctx.close();
			ctx = null;
			
		} catch (NamingException | SQLException e) {
			try {
				if (conexao != null) {
					conexao.close();
					conexao = null;
				}
			} catch (SQLException e1) {}
			
			throw new SQLException(e.getMessage());
		}
		
	}
	
	
	public Connection getConexao() {
		return conexao;
	}
	public void setConexao(Connection conexao) {
		this.conexao = conexao;
	}
	
	public static void desfazerTransacao(ConexaoDB con) {
		try {
			if (con != null && con.getConexao() != null)
				con.getConexao().rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void gravarTransacao(ConexaoDB con) {
		try {
			if (con != null && con.getConexao() != null)
				con.getConexao().commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	public static void close(ConexaoDB con) {
		try {
			if (con != null) {
				con.getConexao().commit();
				con.getConexao().setAutoCommit(true);
				con.getConexao().close();
				con.setConexao(null);
				con = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public static int gerarNovoCodigo(Connection con, String tabela, String campo, String condicao) throws SQLException, NamingException {
		
		int novoCodigo = 0;
		
		Statement stmt = con.createStatement();
		ResultSet rs = null;
		
		if (condicao == null)
			condicao = "";
		
		if (!condicao.isEmpty() && !condicao.toUpperCase().startsWith("WHERE "))
			condicao = "WHERE " + condicao;

		String sql = String.format("SELECT MAX(%s) AS cod FROM %s %s",
				campo.trim(),
				tabela.trim(),
				condicao.trim());
		
		rs = stmt.executeQuery(sql);
		if (rs.next())
			novoCodigo = rs.getInt("cod") + 1;
		else
			novoCodigo = 1;
		
		stmt.close();
		rs.close();
		stmt = null;
		rs = null;
		
		return novoCodigo;
	}
	
	
	public static boolean existeRegistro(Connection con, String tabela, String condicao) throws SQLException, NamingException {
		Statement stmt = con.createStatement();         // declara variável que comunica com a conexão
		
		String sql = String.format("SELECT DISTINCT 1 FROM %s WHERE %s ", 
				tabela,
				condicao
				);			
		ResultSet res = stmt.executeQuery(sql);
		
		boolean retorno = res.next();

		res.close();
		res = null;
		stmt.close();		
		stmt = null;
		
		return retorno;
	}

	
	/*
	 * 
	 */
	public ConexaoDB(String nomeConexao) throws SQLException {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/" + nomeConexao);

			this.conexao = ds.getConnection();
			
			this.conexao.setAutoCommit(false);
			
			ds = null;
			ctx.close();
			ctx = null;
			
		} catch (NamingException | SQLException e) {
			try {
				if (conexao != null) {
					conexao.close();
					conexao = null;
				}
			} catch (SQLException e1) {}
			
			throw new SQLException(e.getMessage());
		}
		
	}
	
}
