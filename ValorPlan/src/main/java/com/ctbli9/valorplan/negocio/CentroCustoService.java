package com.ctbli9.valorplan.negocio;

import java.util.List;

import com.ctbli9.valorplan.DAO.CentroCustoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.FiltroCentroCusto;

import com.ctbli9.valorplan.modelo.CentroCusto;
import ctbli9.recursos.RegraNegocioException;

public class CentroCustoService {
	private ConexaoDB con;
	
	public CentroCustoService(ConexaoDB con) {
		this.con = con;
	}

	public CentroCusto pesquisarCentroCusto(int cdCencus) throws Exception {
		return CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(), cdCencus);
	}
	
	public CentroCusto pesquisarCentroCusto(String codExterno) throws Exception {
		return CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(), codExterno);
	}
	
	public List<CentroCusto> listar(FiltroCentroCusto filtro) throws Exception {
		return CentroCustoServiceDAO.listar(con.getConexao(), filtro);
	}

	public void salvar(CentroCusto centroCusto) throws RegraNegocioException, Exception {
		if (centroCusto.getCodExterno() == null || centroCusto.getCodExterno().isEmpty())
			throw new RegraNegocioException("Preenchimento obrigatório do código de referência externa.");

		if (centroCusto.getCdCentroCusto() == 0) { // Inclusao
			if (pesquisarCentroCusto(centroCusto.getCodExterno()) != null) // Se achou o codigo externo em outro registro
				throw new RegraNegocioException("Código de referência externa já existe em outro Centro de Custo.");
		}
		
		CentroCustoServiceDAO.salvar(con.getConexao(), centroCusto);
	}

	public void excluir(CentroCusto centroCusto) throws RegraNegocioException, Exception {
		if (pesquisarCentroCusto(centroCusto.getCdCentroCusto()).getCdCentroCusto() > 0) 
			CentroCustoServiceDAO.excluir(con.getConexao(), centroCusto);
		else
			throw new RegraNegocioException("Centro de custo nao encontrado: " + centroCusto.getCodExterno());
	}
	
}
