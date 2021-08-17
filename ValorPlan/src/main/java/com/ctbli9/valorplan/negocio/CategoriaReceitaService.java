package com.ctbli9.valorplan.negocio;

import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.CategoriaReceitaServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;

import ctbli9.recursos.RegraNegocioException;


public class CategoriaReceitaService {
	private ConexaoDB con;
	
	public CategoriaReceitaService(ConexaoDB con) {
		this.con = con;
	}

	public List<CategoriaReceita> listarCategoriaReceita() throws SQLException, NamingException {
		return CategoriaReceitaServiceDAO.listarCategoriaReceita(con.getConexao(), "");
	}
	
	public void salvar(List<CategoriaReceita> categorias) throws Exception {
		
		for (CategoriaReceita categoria : categorias) {
			if (categoria.getDsCategoria().trim().isEmpty())
				throw new RegraNegocioException("Descrição da categoria é obrigatória.");
			
			if (categoria.getCdCategoria()==0)
				CategoriaReceitaServiceDAO.incluirCategoriaReceita(con.getConexao(), categoria);
			else
				CategoriaReceitaServiceDAO.alterarCategoriaReceita(con.getConexao(), categoria);		
			
		}		
	}

	public void excluir(CategoriaReceita categ) throws Exception {
		CategoriaReceitaServiceDAO.excluirCategoriaReceita(con.getConexao(), categ);
	}

}
