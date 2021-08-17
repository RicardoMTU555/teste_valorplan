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
import com.ctbli9.valorplan.modelo.FiltroReceita;
import com.ctbli9.valorplan.modelo.receita.DeducaoReceita;
import com.ctbli9.valorplan.modelo.receita.Receita;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.DAO.ContaContabilServiceDAO;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.LibUtil;

public class ReceitaServiceDAO {
	
	/*
	 * CADASTRO
	 */
	public static List<Receita> listarReceitas(ConexaoDB con, FiltroReceita filtro) throws Exception {
		List<Receita> listaReceitas = new ArrayList<Receita>();
		Statement stmtm = con.getConexao().createStatement();
		
		String where = "";
		
		if (!filtro.getSgReceita().isEmpty()) {
			where += where.isEmpty() ? " WHERE " : " AND ";
			where += String.format("r.sgReceita LIKE '%s' ", filtro.getSgReceita() + "%");
			
		}

		String categorias = LibUtil.arrayParaString(filtro.getCdCategorias());
		if (!categorias.isEmpty()) {
			where += where.isEmpty() ? " WHERE " : " AND ";
			where += String.format("r.cdCategoria IN (%s) ", categorias);
		}

		if (!filtro.getDsCompleta().isEmpty()) {
			where += where.isEmpty() ? " WHERE " : " AND ";
			where += String.format("r.dsReceita LIKE '%s' ", filtro.getDsCompleta() + "%");
		}
		
		if (filtro.getContaContabil() != null) {
			where += where.isEmpty() ? " WHERE " : " AND ";
			where += String.format("ct.cdConta = '%s' ", filtro.getContaContabil().getCdConta());
		}

		String ativo = "T";
		if (filtro.isContaAtiva())
			ativo = filtro.isContaInativa() ? "T" : "S";
		if (filtro.isContaInativa())
			ativo = filtro.isContaAtiva() ? "T" : "N";
		
		if (!ativo.equals("T")) {
			where += where.isEmpty() ? " WHERE " : " AND ";
			where += String.format("r.idAtivo = '%s'", ativo);			
		}
		
		String sql = montaScriptReceita() + where + "ORDER BY cat.dsCategoria, r.sgReceita";

	    ResultSet rs = stmtm.executeQuery(sql);
	    while (rs.next()) {
	    	listaReceitas.add(carregarReceita(rs));
	    }//end while
		
		
	    stmtm.close();
		rs.close();
		rs = null;
		return listaReceitas;
	}

	public static Receita buscarReceita(Connection con, int cdReceita, boolean listaDespesas) throws Exception {
		return executarPesquisa(con, String.format("WHERE r.cdReceita = %d", cdReceita), listaDespesas);
	}
	
	public static Receita pesquisarPorSigla(Connection con, String sigla) throws Exception {
		return executarPesquisa(con, String.format("WHERE r.sgReceita = '%s'", sigla.trim()), false);
	}
	
	public static Receita pesquisarPorDescricao(Connection con, String descricao) throws Exception {
		descricao = LibUtil.truncaTexto(descricao, 40);
		return executarPesquisa(con, String.format("WHERE r.dsReceita = '%s'", descricao), false);
	}
	
	private static Receita executarPesquisa(Connection con, String condicao, boolean listaDespesas) throws Exception {
		Receita receita = null;
		Statement stmtm = con.createStatement();
		
		String sql = montaScriptReceita() + condicao;
	    ResultSet rs = stmtm.executeQuery(sql);
	    if (rs.next()) {
	    	receita = carregarReceita(rs);
	    	
	    	if (listaDespesas)
	    		receita.setDeducaoSobreVenda(listarDeducoes(con, receita));
	    }//end while
				
	    stmtm.close();
		rs.close();
		rs = null;
		return receita;
	}
	
	public static boolean existeReceita(ConexaoDB con, int cdReceita) throws SQLException, NamingException {
		return ConexaoDB.existeRegistro(con.getConexao(), "CadReceita", "cdReceita = " + cdReceita);
	}

	public static void incluirReceita(Connection con, Receita receita) throws SQLException, NamingException {
		
		receita.setCdReceita(ConexaoDB.gerarNovoCodigo(con, "CadReceita", "cdReceita", null));
		
		String sql = "INSERT INTO CadReceita VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		int i = 0;
		pstmt.setInt(++i, receita.getCdReceita());
		pstmt.setString(++i, LibUtil.truncaTexto(receita.getSgReceita(), 10));
		pstmt.setString(++i, LibUtil.truncaTexto(receita.getDsReceita(), 40));
		pstmt.setInt(++i, receita.getCategoria().getCdCategoria());
		pstmt.setInt(++i, receita.getContaReceita().getIdConta());
		pstmt.setString(++i, receita.isIdAtivo() ? "S" : "N");
		pstmt.executeUpdate();
		pstmt.close();
		pstmt = null;
	}

	public static void alterarReceita(Connection con, Receita receita) throws SQLException, NamingException {
		String sql = "UPDATE CadReceita SET "
				+ "sgReceita = ?, "
				+ "dsReceita = ?, "
				+ "cdCategoria = ?, "
				+ "ID_ContaReceita = ?, "
				+ "idAtivo = ? "
				+ "WHERE cdReceita = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		int i = 0;
		pstmt.setString(++i, receita.getSgReceita());
		pstmt.setString(++i, receita.getDsReceita());
		pstmt.setInt(++i, receita.getCategoria().getCdCategoria());
		pstmt.setInt(++i, receita.getContaReceita().getIdConta());
		pstmt.setString(++i, receita.isIdAtivo() ? "S" : "N");

		pstmt.setInt(++i, receita.getCdReceita());
		pstmt.executeUpdate();
		pstmt.close();
		pstmt = null;
	}

	public static void excluirReceita(ConexaoDB con, Receita receita) throws SQLException, NamingException {
		Statement stmt = con.getConexao().createStatement();
	    
		String sql = String.format("DELETE FROM CadReceita WHERE cdReceita = %d", 
				receita.getCdReceita()
				);
		stmt.executeUpdate(sql);
		stmt.close();
	}

	public static String[] listarTiposReceita(Connection con) throws Exception {
		Statement stmt = con.createStatement();
		
		if (!CentroCustoServiceDAO.montarCentrosCustoResponsavel(con, Global.getFuncionarioLogado()))
			CentroCustoServiceDAO.montarCentrosCustoGerente(con, Global.getFuncionarioLogado(), null);
		
		String sql = "SELECT DISTINCT cctr.cdCategoria "
				+ "FROM CadCencusCatrec cctr "
				+ "JOIN TmpCencus ON TmpCencus.cdCentroCusto = cctr.cdCentroCusto";
		
		ResultSet res = stmt.executeQuery(sql);
		int quant = 0;
		while (res.next()) {
			quant++;
		}		
		String[] lista = new String[quant];
		
		res.close();
		res = stmt.executeQuery(sql);
		quant = 0;
		while (res.next()) {
			lista[quant++] = res.getString("cdCategoria");
		}		
		
		return lista;
	}
	
	/* **************************************************************************************************************
	 * Deduções
	 ************************************************************************************************************* */
	public static List<DeducaoReceita> listarDeducoes(Connection con, Receita receita) throws Exception {
		List<DeducaoReceita> lista = new ArrayList<DeducaoReceita>();
		
		String sql = String.format("SELECT * FROM CadDeducaoVenda WHERE cdReceita = %d", receita.getCdReceita());
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			lista.add(carregarDeducaoReceita(con, res));
		}
		
		res.close();
		res = null;
		stmt.close();
		stmt = null;
		return lista;
	}

	public static DeducaoReceita carregarDeducaoReceita(Connection con, ResultSet res) throws Exception {
		DeducaoReceita deducao = new DeducaoReceita();
		
		deducao.setCdReceita(res.getInt("cdReceita"));
		deducao.setConta(ContaContabilServiceDAO.pesquisarIDConta(con, res.getInt("ID_Conta")));
		
		return deducao;
	}

	public static void incluirDeducao(Connection con, DeducaoReceita deducao) throws SQLException, NamingException {
		String sql = "INSERT INTO CadDeducaoVenda VALUES (?, ?)";
		
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setInt(++i, deducao.getCdReceita());
		pstmt.setInt(++i, deducao.getConta().getIdConta());
		
		pstmt.executeUpdate();
		pstmt.close();
		pstmt = null;	
	}
	
	public static boolean existeDeducao(Connection con, DeducaoReceita deducao) throws SQLException, NamingException {
		return ConexaoDB.existeRegistro(con, "CadDeducaoVenda", String.format("cdReceita = %d AND ID_Conta = %d", 
				deducao.getCdReceita(),
				deducao.getConta().getIdConta()));
	}

	public static void excluirDeducao(ConexaoDB con, DeducaoReceita deducao) throws SQLException, NamingException {
		
		String sql = "DELETE FROM CadDeducaoVenda WHERE cdReceita = ? AND ID_Conta = ?";
		PreparedStatement pstmt = con.getConexao().prepareStatement(sql);
				
		pstmt.setInt(1, deducao.getCdReceita());
		pstmt.setInt(2, deducao.getConta().getIdConta());
		pstmt.executeUpdate();				
		
		pstmt.close();
		pstmt = null;
	}
	
	public static DeducaoReceita pesquisarDeducao(Connection con, Receita receita, ContaContabil ctaContabil) throws Exception {
		DeducaoReceita deducao = null;
		
		Statement stmt = con.createStatement();
		
		String sql = String.format("SELECT * FROM CadDeducaoVenda "
				+ "WHERE cdReceita = %d AND ID_Conta = %d", 
				receita.getCdReceita(),
				ctaContabil.getIdConta());
		
		ResultSet res = stmt.executeQuery(sql);
		if (res.next()) {
			deducao = new DeducaoReceita();
			
			deducao.setCdReceita(receita.getCdReceita());
			deducao.setConta(ContaContabilServiceDAO.pesquisarIDConta(con, res.getInt("ID_Conta")));
		}
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		
		return deducao;
	}
	
	
	/*
	 * 
	 */
	public static String montaScriptReceita() {
		return "SELECT r.*, cat.*, ct.*, c.*, g.* "
				+ "FROM CadReceita r "
				+ "JOIN CadCategReceita cat ON r.cdCategoria = cat.cdCategoria "
				+ "JOIN CtbConta ct ON r.ID_ContaReceita = ct.ID_Conta "
				+ "LEFT OUTER JOIN CtbClasseDre c ON ct.cdClasse = c.cdClasse "
				+ "LEFT OUTER JOIN CtbGrupoClasse g ON c.cdGrupoClasse = g.cdGrupoClasse ";
	}

	public static Receita carregarReceita(ResultSet rs) throws SQLException, NamingException {
		Receita receita = new Receita();
    	
    	receita.setCdReceita(rs.getInt("cdReceita"));
    	receita.setSgReceita(rs.getString("sgReceita"));
    	receita.setDsReceita(rs.getString("dsReceita"));
    	receita.setCategoria(CategoriaReceitaServiceDAO.carregarCategoria(rs));
    	receita.setContaReceita(ContaContabilServiceDAO.carregarContaContabil(rs));
    	receita.setIdAtivo(rs.getString("idAtivo").equals("S"));
    	return receita;
	}

	public static Receita pesquisarPorConta(Connection con, String conta, String descr) throws Exception {
		//return executarPesquisa(con, String.format("WHERE ct.cdConta = '%s' AND r.dsReceita = '%s'", 
		//		conta, descr), 
		//		false);
		return executarPesquisa(con, String.format("WHERE ct.cdConta = '%s'", 
				conta), 
				false);
	}

}
