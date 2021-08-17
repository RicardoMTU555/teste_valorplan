package com.ctbli9.valorplan.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.ParametroFilial;

import ctbli9.modelo.Filial;
import ctbli9.modelo.FilialEndereco;
import ctbli9.modelo.FilialParametro;

public class FilialServiceDAO extends ctbli9.DAO.FilialServiceDAO {
	
	public static void incluir(Connection con, Filial filial) throws SQLException, NamingException {
		filial.setCdFilial(ConexaoDB.gerarNovoCodigo(con, "EmpFilial", "cdFilial", null));
		
		String sql = "INSERT INTO EmpFilial VALUES (?, ?, ?, ?, ?)";

		PreparedStatement pstmt = con.prepareStatement(sql);
		int i = 0;
		pstmt.setInt(++i, filial.getCdFilial());
		pstmt.setString(++i, filial.getCodExterno());
		pstmt.setBigDecimal(++i, filial.getDcFilial());
		pstmt.setString(++i, filial.getDsFilial());
		pstmt.setString(++i, filial.getSgFilial());

		pstmt.executeUpdate();
		
		pstmt.close();
		pstmt = null;
	}//incluir
	
	public static void alterar(Connection con, Filial filial) throws SQLException, NamingException {
		String sql = "UPDATE EmpFilial SET "
				+ "filcdexterno = ?, "
				+ "dcfilial = ?, "
				+ "dsFilial = ?, "
				+ "sgfilial = ? "
				+ "WHERE cdFilial = ?";		
				
		PreparedStatement pstmt = con.prepareStatement(sql);
		int i = 0;
		pstmt.setString(++i, filial.getCodExterno());
		pstmt.setBigDecimal(++i, filial.getDcFilial());
		pstmt.setString(++i, filial.getDsFilial());
		pstmt.setString(++i, filial.getSgFilial());

		pstmt.setInt(++i, filial.getCdFilial());
		pstmt.executeUpdate();
		
		pstmt.close();
		pstmt = null;

	}//alterar

	public static void excluir(Connection con, Filial filial) throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		String sql = String.format("DELETE FROM ParFilial WHERE cdFilial = %d", 
				filial.getCdFilial()
				);			
		stmt.executeUpdate(sql);
		
		sql = String.format("DELETE FROM EmpEndereFilial WHERE cdFilial = %d", 
				filial.getCdFilial()
				);			
		stmt.executeUpdate(sql);
		
		sql = String.format("DELETE FROM EmpFilial WHERE cdFilial = %d", 
				filial.getCdFilial()
				);			
		stmt.executeUpdate(sql);
		
		stmt.close();
		stmt = null;
	}//excluir
	
	public static void incluirEndereco(Connection con, Filial filial, FilialEndereco endereco) throws SQLException, NamingException {
		endereco.setSqEndFilial(ConexaoDB.gerarNovoCodigo(con, "EmpEndereFilial", "sqEndFilial", 
				String.format("cdFilial = %d", filial.getCdFilial())));
		
		String sql = "INSERT INTO EmpEndereFilial VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setInt(++i, filial.getCdFilial());
		pstmt.setInt(++i, endereco.getSqEndFilial());
		pstmt.setString(++i, endereco.getTipo().toString());
		pstmt.setString(++i, endereco.getTpLogradouro());
		pstmt.setString(++i, endereco.getNmLogradouro());
		pstmt.setInt(++i, endereco.getNrLogradouro());
		pstmt.setString(++i, endereco.getDsComplemento());
		pstmt.setInt(++i, endereco.getNrCEP());
		pstmt.setInt(++i, endereco.getBairro().getCdBairro());
		pstmt.setString(++i, (endereco.isIdPrincipal() ? "S" : "N"));
		pstmt.setString(++i, (endereco.isIdAtivo() ? "S" : "N"));
		pstmt.setString(++i, endereco.getNmContato());
		pstmt.setString(++i, endereco.getTxEmailContato());
		pstmt.setString(++i, endereco.getTlFixo());
		pstmt.setInt(++i, endereco.getNrRamal());
		pstmt.setString(++i, endereco.getTlCelular());
		pstmt.setString(++i, endereco.getTlFax());
		pstmt.executeUpdate();
		
		pstmt.close();
		pstmt = null;
	}

	public static void alterarEndereco(Connection con, Filial filial, FilialEndereco endereco) throws SQLException, NamingException {
		String sql = "UPDATE EmpEndereFilial SET "
				+ "tpEndFilial = ?, "
				+ "tpLogradouro = ?, "
				+ "nmLogradouro = ?, "
				+ "nrLogradouro = ?, "
				+ "dsComplemento = ?, "
				+ "nrCep = ?, "
				+ "cdBairro = ?, "
				+ "idPrincipal = ?, "
				+ "idAtivo = ?, "
				+ "nmContato = ?, "
				+ "txEmailContato = ?, "
				+ "tlFixo = ?, "
				+ "nrRamal = ?, "
				+ "tlCelular = ?, "
				+ "tlFax = ? "
				+ "WHERE cdFilial = ? "
				+ "AND sqEndFilial = ? ";
			
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setString(++i, endereco.getTipo().toString());
		pstmt.setString(++i, endereco.getTpLogradouro());
		pstmt.setString(++i, endereco.getNmLogradouro());
		pstmt.setInt(++i, endereco.getNrLogradouro());
		pstmt.setString(++i, endereco.getDsComplemento());
		pstmt.setInt(++i, endereco.getNrCEP());
		pstmt.setInt(++i, endereco.getBairro().getCdBairro());
		pstmt.setString(++i, (endereco.isIdPrincipal() ? "S" : "N"));
		pstmt.setString(++i, (endereco.isIdAtivo() ? "S" : "N"));
		pstmt.setString(++i, endereco.getNmContato());
		pstmt.setString(++i, endereco.getTxEmailContato());
		pstmt.setString(++i, endereco.getTlFixo());
		pstmt.setInt(++i, endereco.getNrRamal());
		pstmt.setString(++i, endereco.getTlCelular());
		pstmt.setString(++i, endereco.getTlFax());
		
		pstmt.setInt(++i, filial.getCdFilial());
		pstmt.setInt(++i, endereco.getSqEndFilial());
		
		pstmt.executeUpdate();
		
		pstmt.close();
		pstmt = null;
		
	}

	public static Filial pesquisarPorSigla(Connection con, String siglaFilial) throws SQLException, NamingException {
		Statement stmt = con.createStatement();

		Filial filial = new Filial();
		
		String sql = String.format("SELECT * FROM EmpFilial "
				+ "LEFT OUTER JOIN ParFilial ON EmpFilial.cdFilial = ParFilial.cdFilial "
				+ "WHERE EmpFilial.sgFilial = '%s'", siglaFilial);
	    ResultSet rs = stmt.executeQuery(sql);
	    if(rs.next()) {
			filial.setCdFilial(rs.getInt("cdFilial"));
			filial.setCodExterno(rs.getString("filCdExterno"));
			filial.setDcFilial(rs.getBigDecimal("dcFilial"));
			filial.setDsFilial(rs.getString("dsFilial"));
			filial.setSgFilial(rs.getString("sgFilial"));
			
			FilialParametro param = new FilialParametro();
			param.setTxObsOrdemCompra(rs.getString("txObsOrdemCompra"));
			filial.setParametro(param);
			
			// TODO Reginaldo: abrir o escopo deste metodo
			//filial.setEnderecos(getEnderecos(rs.getInt("cdFilial")));
	    }
		
		stmt.close();
		rs.close();
		stmt = null;
		rs = null;
		
		return filial;
	}

	public static ParametroFilial obterParametros(Connection con) throws Exception {
		ParametroFilial parametro = new ParametroFilial();
		
		Statement stmt = con.createStatement();

		Filial filial = new Filial();
		
		String sql = String.format("SELECT FIRST 1 * FROM ParFilial");
	    ResultSet rs = stmt.executeQuery(sql);
	    if(rs.next()) {
	    	parametro.setFilial(pesquisarFilial(con, rs.getInt("cdFilial")));
	    	parametro.setNivelGrafico(rs.getInt("nivelGrafico"));
	    }
		
		stmt.close();
		rs.close();
		stmt = null;
		rs = null;
		
		return parametro;
	}
}
