package com.ctbli9.valorplan.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.MovtoContab;

import ctbli9.recursos.DataUtil;

public class MovContabImportarServiceDAO {
	
	public static void incluir(Connection con, MovtoContab movtoContab) throws SQLException, NamingException {
		
	    movtoContab.setSqMovto(ConexaoDB.gerarNovoCodigo(con, "CtbLancto", "sqLancto", 
	    		String.format("CtbLancto.cdfilial = %d AND CtbLancto.dtLancto = '%s'",
	    				movtoContab.getFilial().getCdFilial(),
	        			DataUtil.dataString(movtoContab.getDtMovto(), "MM/dd/yyyy")
	        			)
	    		));
                                           
		String sql = "INSERT INTO CtbLancto VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	    PreparedStatement pstmt = con.prepareStatement(sql);
	    
	    int indice = 0;
		pstmt.setInt(++indice, movtoContab.getFilial().getCdFilial());
		pstmt.setString(++indice, DataUtil.dataString(movtoContab.getDtMovto(), "yyyy-MM-dd"));
		pstmt.setInt(++indice, movtoContab.getSqMovto());
		pstmt.setString(++indice, movtoContab.getIdLegado());
		
		pstmt.setInt(++indice, movtoContab.getConta().getIdConta());
		if (movtoContab.getCencus() == null || movtoContab.getCencus().getCdCentroCusto() == 0)
			pstmt.setNull(++indice, java.sql.Types.INTEGER);
		else
			pstmt.setInt(++indice, movtoContab.getCencus().getCdCentroCusto());
		pstmt.setString(++indice, movtoContab.getIdNatMovto());
		pstmt.setDouble(++indice, movtoContab.getVlMovto());
		pstmt.setString(++indice, movtoContab.getTxHistorico());
		pstmt.setString(++indice, movtoContab.getDsDocumento());
		
		pstmt.executeUpdate();
		
		pstmt.close();
		pstmt = null;
	}//incluir
	
	
	public static boolean pesquisarIdExterno(Connection con, String idExterno) throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		String sql = String.format("SELECT * FROM CtbLancto WHERE lanCdExterno = '%s'",
				idExterno
    			);
		ResultSet rs = stmt.executeQuery(sql);
		boolean retorno = rs.next();
		
		stmt.close();
		rs.close();
		stmt = null;
		rs = null;
				
		return retorno;
	}

}
