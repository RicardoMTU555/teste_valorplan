package com.ctbli9.valorplan.DAO;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;

import ctbli9.enumeradores.StatusPlano;
import ctbli9.modelo.Plano;
import ctbli9.recursos.ArquivoTexto;
import ctbli9.recursos.DataUtil;
import ctbli9.recursos.LibUtil;
import ctbli9.recursos.RegraNegocioException;

public class PlanoServiceDAO {

	public static Plano getPlanoSelecionado() {
		return (Plano)LibUtil.getParametroSessao("PLANO_ORCAMENTO");
	}
	
	public static List<Plano> listar(Connection con) throws SQLException, NamingException {
		List<Plano> lista = new ArrayList<Plano>();
		
		String sql = "SELECT * FROM OrcPlano ORDER BY OrcPlano.cdPlano desc";
		Statement stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery(sql);
	    while (rs.next()) {
	    	Plano plano = new Plano();

			char[] mesAberto = new char[12];
			for (int i = 0; i < mesAberto.length; i++) {
				mesAberto[i] = rs.getString("txMesAberto").charAt(i);
			}
			
			plano.setCdPlano(rs.getInt("cdPlano"));
			plano.setNmPlano(rs.getString("nmPlano"));
			plano.setNrAno(rs.getInt("nrAno"));
			plano.setStatus(StatusPlano.getStatus(rs.getString("stPlano")));
			plano.setMesAberto(mesAberto);
	    	
	    	lista.add(plano);
	    }//end while
		
		
		stmt.close();
		rs.close();
		stmt = null;
		rs = null;
	
		return lista;
	}

	public static Plano pesquisar(Connection con, int codigo) throws SQLException, NamingException {
		Plano plano = null;

		String sql = String.format("SELECT * FROM OrcPlano WHERE OrcPlano.cdPlano = %d",
				codigo);
		Statement stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery(sql);
	    while (rs.next()) {
	    	plano = new Plano();

	    	char[] mesAberto = new char[12];
			for (int i = 0; i < mesAberto.length; i++) {
				mesAberto[i] = rs.getString("txMesAberto").charAt(i);
			}

			plano.setCdPlano(rs.getInt("cdPlano"));
			plano.setNmPlano(rs.getString("nmPlano"));
			plano.setNrAno(rs.getInt("nrAno"));
			plano.setStatus(StatusPlano.getStatus(rs.getString("stPlano")));
			plano.setMesAberto(mesAberto);
	    }	
		
		stmt.close();
		rs.close();
		stmt = null;
		rs = null;
	
		return plano;
	}

	public static void incluir(Connection con, Plano plano) throws SQLException, NamingException {
			
		plano.setCdPlano(ConexaoDB.gerarNovoCodigo(con, "OrcPlano", "cdPlano", null));
		
		String sql = "INSERT INTO OrcPlano values (?, ?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		String txMesAberto = "";
		for (char aberto : plano.getMesAberto()) {
			txMesAberto += aberto;
		}
		
		int i = 0;
		pstmt.setInt(++i, plano.getCdPlano());
		pstmt.setString(++i, plano.getNmPlano());
		pstmt.setInt(++i, plano.getNrAno());
		pstmt.setString(++i, plano.getStatus().getValor());
		pstmt.setString(++i, txMesAberto);
			
		pstmt.executeUpdate();
		
		pstmt.close();
		pstmt = null;

	}

	public static void alterar(Connection con, Plano plano) throws SQLException, NamingException {
		String sql = "UPDATE OrcPlano SET "
    			+ " nmPlano = ?, "
    			+ " nrAno = ?, "
    			+ " stPlano = ?, "
    			+ " txMesAberto = ? "
    			+ "WHERE cdPlano = ?";

		String txMesAberto = "";
		for (char aberto : plano.getMesAberto()) {
			txMesAberto += aberto;
		}
		
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setString(++i, plano.getNmPlano());
		pstmt.setInt(++i, plano.getNrAno());
		pstmt.setString(++i, plano.getStatus().getValor());
		pstmt.setString(++i, txMesAberto);
		
		pstmt.setInt(++i, plano.getCdPlano());
			
		pstmt.executeUpdate();
		
		pstmt.close();
		pstmt = null;
	}
	
	public static void excluir(Connection con, int cdPlano) throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		String sql = String.format("DELETE FROM OrcPlano WHERE cdPlano = %d",
				cdPlano);
	    	stmt.executeUpdate(sql);


	    stmt.close();
	    stmt = null;
	}
	
	
	public static void validarImportacao(Connection con, Plano planoDest) throws SQLException, NamingException, RegraNegocioException {
		Statement stmt = con.createStatement();

		String sql = String.format(
				"SELECT '1' AS tabela, COUNT(*) AS Quant FROM OrcParamDeducao WHERE cdPlano = %d HAVING COUNT(*) > 0 " + 
				"UNION " + 
				"SELECT '2' AS tabela, COUNT(*) AS Quant FROM OrcParamFolha WHERE cdPlano = %d HAVING COUNT(*) > 0 " + 
				"UNION " + 
				"SELECT '3' AS tabela, COUNT(*) AS Quant FROM OrcReceita WHERE cdPlano = %d HAVING COUNT(*) > 0 " + 
				"UNION " + 
				"SELECT '4' AS tabela, COUNT(*) AS Quant FROM OrcDespesaGeral WHERE cdPlano = %d HAVING COUNT(*) > 0 " + 
				"UNION " + 
				"SELECT '5' AS tabela, COUNT(*) AS Quant FROM OrcDespesaRH WHERE cdPlano = %d HAVING COUNT(*) > 0; ",
				planoDest.getCdPlano(),
				planoDest.getCdPlano(),
				planoDest.getCdPlano(),
				planoDest.getCdPlano(),
				planoDest.getCdPlano()
				);
		
		ResultSet res = stmt.executeQuery(sql);
		boolean temReg = res.next();

		res.close();
		res = null;
	    stmt.close();
	    stmt = null;
	    
	    if (temReg) {
	    	String msg = "O cenário <b>" + planoDest.getNmPlano() + "</b> Já possui dados carregados.<br/>"
					+ "Importação não permitida.";
	    	
	    	throw new RegraNegocioException(msg);
	    }

	}
	
	public static void executarImportacao(Connection con, Plano planoOri, Plano planoDest) throws SQLException, NamingException {
		Statement stmt = con.createStatement();

		// 1o. Parametros de dedução
		LibUtil.display("Passo 1");
		String sql = String.format("DELETE FROM OrcParamDeducao WHERE cdPlano = %d;",
				planoDest.getCdPlano());
		stmt.executeUpdate(sql);

		sql = String.format("INSERT INTO OrcParamDeducao " + 
				"SELECT %d, cdReceita, ID_Conta, ID_Recurso, nrAnoMes, pcDespesa " + 
				"FROM OrcParamDeducao " + 
				"WHERE cdPlano = %d;",
				planoDest.getCdPlano(),
				planoOri.getCdPlano());
		stmt.executeUpdate(sql);
		
		// 2o. Parametros de folha
		LibUtil.display("Passo 2");
		sql = String.format("DELETE FROM OrcParamFolha where cdPlano = %d;",
				planoDest.getCdPlano());
		stmt.executeUpdate(sql);
		
		sql = String.format("INSERT INTO OrcParamFolha " + 
				"SELECT %d, ID_ContaSal, ID_ContaEnc, nrAnoMes, pcDespesa " + 
				"FROM OrcParamFolha " + 
				"WHERE cdPlano = %d;",
				planoDest.getCdPlano(),
				planoOri.getCdPlano());
		stmt.executeUpdate(sql);
		
		// 3o. Receitas
		LibUtil.display("Passo 3");
		sql = String.format("DELETE FROM OrcReceita WHERE cdPlano = %d;",
				planoDest.getCdPlano());
		stmt.executeUpdate(sql);
		
		sql = String.format("INSERT INTO OrcReceita " + 
				"SELECT %d,nrAnoMes, ID_Recurso, cdReceita, qtReceita, vrUnitario " + 
				"FROM OrcReceita " + 
				"WHERE cdPlano = %d;",
				planoDest.getCdPlano(),
				planoOri.getCdPlano());
		stmt.executeUpdate(sql);
		
		// 4o. Despesa Geral
		LibUtil.display("Passo 4");
		sql = String.format("DELETE FROM OrcDespesaGeral WHERE cdPlano = %d;",
				planoDest.getCdPlano());
		stmt.executeUpdate(sql);
		
		sql = String.format("INSERT INTO OrcDespesaGeral " + 
				"SELECT %d, nrAnoMes, cdCentroCusto, ID_Conta, vrDespesa " + 
				"FROM OrcDespesaGeral " + 
				"WHERE cdPlano = %d;",
				planoDest.getCdPlano(),
				planoOri.getCdPlano());
		stmt.executeUpdate(sql);
		
		// 5o. Despesa RH
		LibUtil.display("Passo 5");
		sql = String.format("DELETE FROM OrcDespesaRH WHERE cdPlano = %d;",
				planoDest.getCdPlano());
		stmt.executeUpdate(sql);
		
		sql = String.format("INSERT INTO OrcDespesaRH " + 
				"SELECT %d,nrAnoMes, ID_Recurso, ID_Conta, vrDespesa " + 
				"FROM OrcDespesaRH " + 
				"WHERE cdPlano = %d;",
				planoDest.getCdPlano(),
				planoOri.getCdPlano());
		stmt.executeUpdate(sql);
		
		stmt.close();
		stmt = null;
		LibUtil.display("Fim da replicação");

	}
	
	
	public static void log(String prefixo, String texto) {
		
		ServletContext sc = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		String caminho = sc.getRealPath("/");

		File dir = new File(caminho);
		caminho = dir.getParent();
		dir = new File(caminho);
		caminho = dir.getParent();
		
		String nmArq = caminho + "/logs/" + prefixo.trim() + "_" + LibUtil.getUsuarioSessao().getLogUsuario().trim() + "_" + 
				DataUtil.dataString(new Date(), "yyyy_MM_dd") + ".log";
		
		//System.out.println(nmArq);
		ArquivoTexto arqLog = new ArquivoTexto(nmArq);
		try {
			arqLog.abreGrava();
			arqLog.imprimeLinha(DataUtil.dataString(new Date(), "dd/MM/yyyy HH:mm:ss") + " - " + 
					LibUtil.getUsuarioSessao().getLogUsuario() + " - " +
					texto);
			arqLog.fechaGrava();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
	}
	
}
