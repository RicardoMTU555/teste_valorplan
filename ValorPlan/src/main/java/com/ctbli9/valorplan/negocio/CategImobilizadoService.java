package com.ctbli9.valorplan.negocio;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.CategImobilizadoServiceDAO;
import com.ctbli9.valorplan.modelo.CategImobilizado;

public class CategImobilizadoService {
	protected Connection con;
	
	public CategImobilizadoService(Connection con) {
		this.con = con;
	}

	public void salvar(CategImobilizado categ) throws SQLException, NamingException {
		if (categ.getCdCategImobili() == 0)
			CategImobilizadoServiceDAO.incluir(con, categ);
		else
			CategImobilizadoServiceDAO.alterar(con, categ);
	}

	public void excluir(CategImobilizado categ) throws SQLException, NamingException {
		if (CategImobilizadoServiceDAO.existe(con, categ.getCdCategImobili()))
			CategImobilizadoServiceDAO.excluir(con, categ);
		
	}
	
	public CategImobilizado pesquisarCategImobilizado(int codigo) throws Exception {
		return CategImobilizadoServiceDAO.pesquisarCategImobilizado(con, codigo);
	}
	
	// TODO
	/*public List<CategImobilizado> listar() throws Exception {
		return CategImobilizadoServiceDAO.listar(con);
	}
	
	public List<OrcamentoInvestimento> listarOrcamentoCategoria(int codCategoria, int codCencus) throws Exception {
		return CategImobilizadoServiceDAO.listarOrcamentoCategoria(con, codCategoria, codCencus);
	}*/

}
