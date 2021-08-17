package com.ctbli9.valorplan.negocio;

import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import ctbli9.enumeradores.StatusPlano;
import ctbli9.modelo.Plano;
import ctbli9.recursos.RegraNegocioException;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;

public class PlanoService {
	
	private ConexaoDB con;

	public PlanoService(ConexaoDB con) {
		this.con = con;
	}

	public List<Plano> listar() throws SQLException, NamingException {
		return PlanoServiceDAO.listar(con.getConexao());
	}

	public Plano pesquisar(int codigo) throws SQLException, NamingException {
		return PlanoServiceDAO.pesquisar(con.getConexao(), codigo);
	}

	public void salvar(Plano plano) throws SQLException, NamingException {
		if (plano.getCdPlano() == 0)
			PlanoServiceDAO.incluir(con.getConexao(), plano);
		else
			PlanoServiceDAO.alterar(con.getConexao(), plano);
	}

	public void excluir(Plano plano) throws RegraNegocioException, SQLException, NamingException {
		if (pesquisar(plano.getCdPlano()) == null)
			throw new RegraNegocioException("Plano não localizado.");
		
		PlanoServiceDAO.excluir(con.getConexao(), plano.getCdPlano());
		
	}

	public void validarImportacao(Plano planoOri, Plano planoDest) throws RegraNegocioException, SQLException, NamingException  {
		if (!planoDest.getStatus().equals(StatusPlano._0))
			throw new RegraNegocioException("Status do plano DESTINO inválido para importação: <br/><b>" + 
					planoDest.getStatus().getDescricao() + "</b>");
		
		if (planoOri.equals(planoDest))
			throw new RegraNegocioException("Cenário original e cenário destino são os mesmos.");
		
		PlanoServiceDAO.validarImportacao(con.getConexao(), planoDest);
	}

	public void executarImportacao(Plano planoOri, Plano planoDest) throws SQLException, NamingException  {
		PlanoServiceDAO.executarImportacao(con.getConexao(), planoOri, planoDest);
	}

}
