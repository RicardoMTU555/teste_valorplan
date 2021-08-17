package com.ctbli9.valorplan.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ctbli9.valorplan.modelo.Cargo;

import ctbli9.DAO.LibDAO;
import ctbli9.enumeradores.TipoCargo;


public class CargoServiceDAO {
	public static Cargo pesquisarCargo(Connection con, int cdCargo) throws Exception {
		return pesquisarCargo(con, String.format("select * from cadcargo where cadcargo.cdCargo = %d",
					cdCargo));
	}
	
	public static Cargo pesquisarDescricao(Connection con, String dsCargo) throws Exception {
		// TAMANHO DO CAMPO = 40
		dsCargo = dsCargo.length() > 40 ? dsCargo.substring(0, 40) : dsCargo;
		
		return pesquisarCargo(con, String.format("select * from cadcargo where UPPER(cadcargo.dsCargo) = '%s'",
				dsCargo.trim().toUpperCase()));
	}
	
	private static Cargo pesquisarCargo(Connection con, String sql) throws Exception {
		Cargo cargo = null;
		
		try {
			Statement stmt = con.createStatement();
			
		    ResultSet rs = stmt.executeQuery(sql);
		    if(rs.next()) {
		    	cargo = carregarCargo(rs);
		    }
			
			stmt.close();
			rs = null;
		} catch (Exception e) {
			if (e.getMessage() != null && !e.getMessage().isEmpty()) {
				String msg = "Classe: CargoServiceDAO. Método: pesquisarCargo. " + e.getMessage();
				throw new Exception(msg);	
			} else {
				throw e;
			}
		}

		return cargo;
	}//pesquisarCargo

	public static Cargo carregarCargo(ResultSet rs) throws SQLException {
		Cargo cargo = new Cargo();
		cargo.setCdCargo(rs.getInt("cdCargo"));
    	cargo.setDsCargo(rs.getString("dsCargo"));
    	cargo.setTpCargo(TipoCargo.valueOf(converterCargo(rs.getString("tpCargo"))));
    	return cargo;
	}

	private static String converterCargo(String tipoAntigo) {
		String tipoNovo = tipoAntigo;
		
		if (tipoAntigo.equals("AN") || tipoAntigo.equals("AX"))
			tipoNovo = "OP";
		
		if (tipoAntigo.equals("CS"))
			tipoNovo = "VE";
			
		return tipoNovo;
	}

	public static void incluir(Connection con, Cargo cargo) throws Exception {
		try {
			
			cargo.setCdCargo(LibDAO.gerarNovoCodigo(con, "CadCargo", "cdCargo", null));
			
			String sql = "INSERT INTO CadCargo values (?, ?, ?)";
			PreparedStatement pstmt = con.prepareStatement(sql);
			
			int i = 0;
			pstmt.setInt(++i, cargo.getCdCargo());
			pstmt.setString(++i, cargo.getDsCargo());
			pstmt.setString(++i, cargo.getTpCargo().toString());
				
			pstmt.executeUpdate();
			
			pstmt.close();
			pstmt = null;

		} catch (Exception e) {
			if (e.getMessage() != null && !e.getMessage().isEmpty()) {
				String msg = "Classe: CargoServiceDAO. Método: incluir. " + e.getMessage();
				throw new Exception(msg);	
			} else {
				throw e;
			}

		}
	}//incluir

	public static void alterar(Connection con, Cargo cargo) throws Exception {
		try {
						
			String sql = "UPDATE CadCargo "
		    			+ "SET dsCargo = ?, "
		    			+ " tpcargo = ? "
		    			+ "WHERE cdCargo = ?";

			PreparedStatement pstmt = con.prepareStatement(sql);
			
			int i = 0;
			pstmt.setString(++i, cargo.getDsCargo());
			pstmt.setString(++i, cargo.getTpCargo().toString());
			pstmt.setInt(++i, cargo.getCdCargo());
				
			pstmt.executeUpdate();
			
			pstmt.close();
			pstmt = null;

		} catch (Exception e) {
			if (e.getMessage() != null && !e.getMessage().isEmpty()) {
				String msg = "Classe: CargoServiceDAO. Método: alterar. " + e.getMessage();
				throw new Exception(msg);	
			} else {
				throw e;
			}

		}

	}//alterar

	public static void excluir(Connection con, Cargo cargo) throws Exception {
		try {
			Statement stmt = con.createStatement();
			
			ResultSet rs = null;
			
			String sql = String.format("select 1 from cadcargo where cdCargo = %d",
					cargo.getCdCargo());
		    rs = stmt.executeQuery(sql);
		    if(rs.next()) {
		    	sql = String.format("DELETE from cadcargo where cdCargo = %d",
		    			cargo.getCdCargo());
		    	stmt.executeUpdate(sql);
		    }
			rs.close();
			rs = null;
			stmt.close();

		} catch (Exception e) {
			if (e.getMessage() != null && !e.getMessage().isEmpty()) {
				String msg = "Classe: CargoServiceDAO. Método: excluir. " + e.getMessage();
				throw new Exception(msg);	
			} else {
				throw e;
			}
		}

	}//excluir

	private static List<Cargo> ExecutaListar(Connection con, String sql) throws Exception {
		List<Cargo> cargos = new ArrayList<Cargo>();
		try {
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery(sql);
		    while (rs.next()) {
		    	Cargo cargo = new Cargo(rs.getInt("cdCargo"), 
		    			rs.getString("dsCargo"), 
		    			TipoCargo.valueOf(rs.getString("tpCargo"))
		    			);
		    	cargos.add(cargo);
		    }//end while
			
			stmt.close();
			
		} catch (Exception e) {
			if (e.getMessage() != null && !e.getMessage().isEmpty()) {
				String msg = "Classe: CargoServiceDAO. Metodo: ExecutaLista. " + e.getMessage();
				throw new Exception(msg);	
			} else {
				throw e;
			}
		}
		
	    return cargos;
	}//listar

	
	public static List<Cargo> listar(Connection con) throws Exception {
		return ExecutaListar(con, "select * from cadcargo order by cadcargo.cdCargo");
	}
	
	public static List<Cargo> listar(Connection con, String termoPesquisa) throws Exception {
		termoPesquisa = "%" + termoPesquisa.trim() + "%";
		return ExecutaListar(con, String.format("select * from cadcargo where dsCargo like '%S' order by cadcargo.cdCargo", 
				termoPesquisa));
	}

}
