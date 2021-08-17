package com.ctbli9.valorplan.negocio.orc;

import java.util.List;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.DAO.orc.OrcInvestimentoServiceDAO;
import com.ctbli9.valorplan.modelo.orc.OrcamentoInvestimento;

import com.ctbli9.valorplan.modelo.CentroCusto;

public class OrcInvestimentoService {
	private ConexaoDB con;

	public OrcInvestimentoService(ConexaoDB con) {
		this.con = con;
	}

	public List<OrcamentoInvestimento> listarInvestimentos(CentroCusto cenCusto) throws Exception {
		return OrcInvestimentoServiceDAO.listarInvestimentos(con.getConexao(), cenCusto);
	}

	public void gravarOrcInvestimento(CentroCusto cenCusto, OrcamentoInvestimento investimento) throws Exception {
		investimento.setCencusto(cenCusto);
		OrcInvestimentoServiceDAO.gravarOrcInvestimento(con.getConexao(), investimento);
	}

	public void excluirOrcInvestimento(OrcamentoInvestimento investimento) throws Exception {
		OrcInvestimentoServiceDAO.excluirOrcInvestimento(con.getConexao(), investimento);
	}
}
