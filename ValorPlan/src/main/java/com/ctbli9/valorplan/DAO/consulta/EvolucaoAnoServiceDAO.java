package com.ctbli9.valorplan.DAO.consulta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.enumeradores.TipoSaldo;
import com.ctbli9.valorplan.enumeradores.TipoValorAno;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.orc.ValorTotalAno;

import ctbli9.recursos.RegraNegocioException;

public class EvolucaoAnoServiceDAO {
	private boolean conexaoEstabelecida = false;
	Connection con;
	private String sql;
	private Statement stmt;
	private ResultSet res;
	
	public EvolucaoAnoServiceDAO(ConexaoDB con) throws SQLException {
		this.con = con.getConexao();
		stmt = this.con.createStatement();
		
		conexaoEstabelecida = true;
	}
	
	public void close() {
		if (conexaoEstabelecida) {
			try {
				con = null;
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		sql = null;
		res = null;
		stmt = null;
	}

	

	public List<ValorTotalAno> listarContasTotalAno(CentroCusto cenCusto, TipoSaldo tipo) throws RegraNegocioException, SQLException {
		List<ValorTotalAno> lista = new ArrayList<>();
		
		if (tipo.equals(TipoSaldo.REC))
			sql = String.format("SELECT SUBSTRING(nrAnomes FROM 1 FOR 4) AS Ano, SUM(vlOrcado) orcado, SUM(vlRealizado) realizado "
					+ "FROM TmpSaldos WHERE cdCentroCusto = %d AND tipo = '%s' GROUP BY 1 ORDER BY 1 desc;",
					cenCusto.getCdCentroCusto(),
					tipo);
		else
			sql = String.format("SELECT SUBSTRING(nrAnomes FROM 1 FOR 4) AS Ano, SUM(vlOrcado * -1) orcado, SUM(vlRealizado * -1) realizado "
					+ "FROM TmpSaldos WHERE cdCentroCusto = %d AND tipo = '%s' GROUP BY 1 ORDER BY 1 desc;",
					cenCusto.getCdCentroCusto(),
					tipo);
		
		res = stmt.executeQuery(sql);
		
		ValorTotalAno anoPosterior = null;
		while (res.next()) {
			ValorTotalAno conta = new ValorTotalAno();
			
			conta.setAno(res.getInt("ano"));
			
			if (anoPosterior == null) {
				conta.setValor(res.getBigDecimal("Orcado"));
				conta.setOrigemValor(TipoValorAno.ORÇADO);
			}
			else {
				conta.setValor(res.getBigDecimal("Realizado"));
				conta.setOrigemValor(TipoValorAno.REALIZADO);
			}
			conta.setAnoPosterior(anoPosterior);
			
			lista.add(conta);
			
			anoPosterior = conta;
		}
		
		return lista;
	}
	
	
	
}
