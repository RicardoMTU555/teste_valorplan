package com.ctbli9.valorplan.DAO.versao1;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.CargoServiceDAO;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Funcionario;

import ctbli9.enumeradores.AreaSetor;
import ctbli9.modelo.Filial;

public class FuncionarioVersao1DAO {

	public static Funcionario pesquisar(Connection con, int cdFuncionario) throws Exception {
		return executarPesquisa(con, String.format("WHERE func.cdfuncionario = %d", cdFuncionario));
	}

	
	private static Funcionario executarPesquisa(Connection con, String sql) throws Exception {
		Funcionario func = null;
		Statement stmt = con.createStatement();
		
		sql = montaScriptFuncionario() + sql;
		
	    ResultSet rs = stmt.executeQuery(sql);
	    if (rs.next()) {
	    	func = carregarFuncionario(rs);
	    }
		
		stmt.close();
		rs.close();
		rs = null;

		return func;
	}	
	
	protected static String montaScriptFuncionario() {
		return "SELECT func.*, car.*, cc.cdCentroCusto, cc.cecCdExterno, cc.cecDsResumida, cc.cecDsCompleta, cc.cecTpArea, "
				+ "cc.cdFuncLider, cc.cdFilial, e.sgFilial "
				+ "FROM CadFuncionario func "
				+ "JOIN CadCargo car ON func.cdCargo = car.cdCargo "
				+ "LEFT OUTER JOIN EmpCentroCusto cc ON func.cdCentroCusto = cc.cdCentroCusto "
				+ "JOIN EmpFilial e ON cc.cdFilial = e.cdFilial ";
	}
	
	protected static Funcionario carregarFuncionario(ResultSet rs) throws SQLException, NamingException {
		Funcionario func = new Funcionario();
    	func.setCdFuncionario(rs.getInt("cdFuncionario"));
    	func.setDcFuncionario(rs.getBigDecimal("dcFuncionario"));
    	func.setNmFuncionario(rs.getString("nmFuncionario"));
    	func.setLogUsuario(rs.getString("logUsuario"));
    	
    	func.setTxEmailFuncionario(rs.getString("txEmail"));
    	func.setFunIdAtivo(rs.getString("idAtivo").equals("S"));

    	
    	CentroCusto cenc = new CentroCusto();
    	cenc.setCdCentroCusto(rs.getInt("cdCentroCusto"));
    	
    	if (rs.getInt("cdCentroCusto") > 0) {
    		cenc.setCecDsCompleta(rs.getString("cecDsCompleta"));
	    	cenc.setCecDsResumida(rs.getString("cecDsResumida"));
	    	cenc.setCodExterno(rs.getString("cecCdExterno"));
	    	
	    	cenc.setDepartamento(null);
	    	
	    	cenc.setFilial(new Filial());
	    	cenc.getFilial().setCdFilial(rs.getInt("cdFilial"));
	    	cenc.getFilial().setSgFilial(rs.getString("sgFilial"));

	    	cenc.setTipoArea(AreaSetor.valueOf(rs.getString("cecTpArea")));
	    	//cenc.setTipos(TipoReceitaServiceDAO.listarTipoCencusto(rs.getInt("cdCentroCusto")));
	    	
	    	//cenc.setFuncLider(new Funcionario());
	    	//cenc.getFuncLider().setCdFuncionario(rs.getInt("cdFuncLider"));
	    	
	    	func.setCenCusto(cenc);	
    	}
    	func.setCargo(CargoServiceDAO.carregarCargo(rs));
    	
		return func;
	}
}
