package com.ctbli9.valorplan.negocio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.FuncionarioServiceDAO;
import com.ctbli9.valorplan.modelo.FiltroFuncionario;
import com.ctbli9.valorplan.modelo.Funcionario;

import ctbli9.recursos.RegraNegocioException;

public class FuncionarioService {
			
	private Connection con;
	
	public FuncionarioService(Connection con) {
		this.con = con;
	}

	public Funcionario pesquisar(int cdFuncionario) throws Exception {
		return FuncionarioServiceDAO.pesquisar(con, cdFuncionario);
	}
	
	public Funcionario pesquisarPorLogin() throws Exception {
		return FuncionarioServiceDAO.pesquisarPorLogin(con);
	}
	
	public List<Funcionario> listar(FiltroFuncionario filtro) throws Exception {
		return FuncionarioServiceDAO.listar(con, filtro);
	}

	public List<String> retornarNomes(String consulta) throws SQLException, NamingException {
		return FuncionarioServiceDAO.retornarNomes(con, consulta);
	}


	public void salvar(Funcionario funcionario) throws RegraNegocioException, Exception {
		if (!FuncionarioServiceDAO.validaLoginRepetido(con, funcionario))
			throw new RegraNegocioException("LOGIN já existe para outro usuário.");
		
		if (funcionario.getCdFuncionario()==0)
			FuncionarioServiceDAO.incluir(con, funcionario);
		else
			FuncionarioServiceDAO.alterar(con, funcionario);
	}

	public void excluir(Funcionario funcionario) throws RegraNegocioException, SQLException, NamingException  {
		if (!FuncionarioServiceDAO.existe(con, funcionario))
			throw new RegraNegocioException("Código não encontrado ou bloqueado.");
		FuncionarioServiceDAO.excluir(con, funcionario);		
	}
	
}
