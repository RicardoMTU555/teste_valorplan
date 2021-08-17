package com.ctbli9.valorplan.negocio;

import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import ctbli9.modelo.Filial;
import ctbli9.modelo.FilialEndereco;
import ctbli9.recursos.RegraNegocioException;
import com.ctbli9.valorplan.DAO.FilialServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;

public class FilialService {
	private ConexaoDB con;
	
	public FilialService(ConexaoDB con) {
		this.con = con;
	}
	
	public Filial pesquisar(int cdFilial) throws Exception {
		return FilialServiceDAO.pesquisarFilial(con.getConexao(), cdFilial);
	}
	
	public void incluir(Filial filial) throws RegraNegocioException, Exception {
		if (filial.getDsFilial().trim().isEmpty())
			throw new RegraNegocioException("Descrição da Filial é obrigatório.");
		
		if (FilialServiceDAO.existe(con.getConexao(), filial.getCdFilial()))	
			throw new RegraNegocioException("Filial já existente.");
			
		FilialServiceDAO.incluir(con.getConexao(), filial);
	}

	public void alterar(Filial filial) throws RegraNegocioException, Exception {
		if (filial.getDsFilial().trim().isEmpty())
			throw new RegraNegocioException("Descrição da Filial é obrigatório.");
		
		if (!FilialServiceDAO.existe(con.getConexao(), filial.getCdFilial()))	
			throw new RegraNegocioException("Filial não existe.");

		FilialServiceDAO.alterar(con.getConexao(), filial);	
	}

	public void excluir(Filial filial) throws RegraNegocioException, Exception {
		if (!FilialServiceDAO.existe(con.getConexao(), filial.getCdFilial()))	
			throw new RegraNegocioException("Filial não existe.");
		
		FilialServiceDAO.excluir(con.getConexao(), filial);
	}
	
	public List<Filial> listar() throws SQLException, NamingException {
		return FilialServiceDAO.listar(con.getConexao());
	}
	public List<Filial> listar(String termoPesquisa) throws SQLException, NamingException {
		return FilialServiceDAO.listar(con.getConexao(), termoPesquisa);
	}
	
	public boolean existe(int cdFilial) throws SQLException, NamingException {
		return FilialServiceDAO.existe(con.getConexao(), cdFilial);
	}

	/*
	 * ParFilial
	 */
	public void gravarParametro(Filial filial) throws SQLException, NamingException {
		FilialServiceDAO.gravarParametro(con.getConexao(), filial);
	}

	/*
	 * EndereFilial
	 */
	public void salvarEndereco(Filial filial, FilialEndereco endereco) throws SQLException, NamingException {
		if (endereco.getSqEndFilial() == 0)
			FilialServiceDAO.incluirEndereco(con.getConexao(), filial, endereco);
		else
			FilialServiceDAO.alterarEndereco(con.getConexao(), filial, endereco);
		
	}		
}
