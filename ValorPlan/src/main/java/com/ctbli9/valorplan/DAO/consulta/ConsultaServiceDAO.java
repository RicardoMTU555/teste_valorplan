package com.ctbli9.valorplan.DAO.consulta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.enumeradores.TipoSaldo;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.ResumoDRE;
import com.ctbli9.valorplan.modelo.orc.ValorTotalMes;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.recursos.RegraNegocioException;

public class ConsultaServiceDAO {
	private boolean conexaoEstabelecida = false;
	Connection con;
	private String sql;
	private Statement stmt;
	private ResultSet res;
	
	public ConsultaServiceDAO(ConexaoDB con) throws SQLException {
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

	public List<ResumoDRE> listarReceitasPorConta(CentroCusto cenCusto, MesAnoOrcamento mesRef) throws SQLException {
		List<ResumoDRE> lista = new ArrayList<>();
		
		sql = String.format("SELECT TRIM(ctb.cdConta) || ' - ' || ctb.dsConta AS ID, saldo.nrAnoMes, saldo.ID_Conta, " +
				"SUM(vlOrcado) AS orcado, SUM(vlRealizado) realizado  " +
				"FROM TmpSaldos saldo " + 
				"JOIN CtbConta ctb ON saldo.ID_Conta = ctb.ID_Conta " +
				"WHERE saldo.tipo IN ('%s') " +
				"AND saldo.cdCentroCusto = %d AND saldo.nrAnoMes BETWEEN %04d01 AND %d " +
				"GROUP BY TRIM(ctb.cdConta) || ' - ' || ctb.dsConta, saldo.nrAnoMes, saldo.ID_Conta",
				TipoSaldo.REC,
				cenCusto.getCdCentroCusto(),
				mesRef.getAno(),
				mesRef.getAnoMes());
		
		res = stmt.executeQuery(sql);
		
		boolean temRegistro = res.next();
		while (temRegistro) {
			
			ResumoDRE resumo = new ResumoDRE(1);
			
			resumo.setDescricao(res.getString("ID"));
			
			while (temRegistro &&
					resumo.getDescricao().equals(res.getString("ID"))) {
			
				if (mesRef.getAnoMes() == res.getInt("nrAnoMes")) {
					resumo.setValorOrcado(1, res.getDouble("Orcado"));
					resumo.setValorRealizado(1, res.getDouble("Realizado"));
				}
				
				resumo.addValorOrcadoAcum(res.getDouble("Orcado"));
				resumo.addValorRealizadoAcum(res.getDouble("Realizado"));
				
				temRegistro = res.next();
			}
			
			lista.add(resumo);
		}
		
		return lista;
	}

	public List<ValorTotalMes> listarContasTotalMes(CentroCusto cenCusto, TipoSaldo tipo) throws RegraNegocioException, SQLException {
		List<ValorTotalMes> lista = new ArrayList<>();
		
		if (tipo.equals(TipoSaldo.REC))
			sql = String.format("SELECT nrAnomes, SUM(vlOrcado) orcado, SUM(vlRealizado) realizado "
					+ "FROM TmpSaldos WHERE cdCentroCusto = %d AND tipo = '%s' GROUP BY 1;",
					cenCusto.getCdCentroCusto(),
					tipo);
		else
			sql = String.format("SELECT nrAnomes, SUM(vlOrcado * -1) orcado, SUM(vlRealizado * -1) realizado "
					+ "FROM TmpSaldos WHERE cdCentroCusto = %d AND tipo = '%s' GROUP BY 1;",
					cenCusto.getCdCentroCusto(),
					tipo);
		
		res = stmt.executeQuery(sql);
		
		while (res.next()) {
			ValorTotalMes conta = new ValorTotalMes();
			
			conta.setMesRef(new MesAnoOrcamento());
			conta.getMesRef().setAnoMes(res.getInt("nrAnoMes"));
			
			conta.setVrOrcado(res.getBigDecimal("Orcado"));
			conta.setVrRealizado(res.getBigDecimal("Realizado"));
			
			lista.add(conta);
		}
		
		return lista;
	}
	
	
	
}
