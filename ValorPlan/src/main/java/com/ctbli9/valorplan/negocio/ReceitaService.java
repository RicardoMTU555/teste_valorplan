package com.ctbli9.valorplan.negocio;

import java.util.List;

import com.ctbli9.valorplan.DAO.CategoriaReceitaServiceDAO;
import com.ctbli9.valorplan.DAO.ReceitaServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.FiltroReceita;
import com.ctbli9.valorplan.modelo.receita.DeducaoReceita;
import com.ctbli9.valorplan.modelo.receita.Receita;

import ctbli9.recursos.RegraNegocioException;

public class ReceitaService {
	private ConexaoDB con;
	
	public ReceitaService(ConexaoDB con) {
		this.con = con;
	}

	public Receita buscarReceita(int cdReceita) throws Exception {
		return ReceitaServiceDAO.buscarReceita(this.con.getConexao(), cdReceita, true);
	}

	public Receita buscarReceita(int cdReceita, boolean listaDespesas) throws Exception {
		return ReceitaServiceDAO.buscarReceita(this.con.getConexao(), cdReceita, listaDespesas);
	}

	public List<Receita> listarReceitas(FiltroReceita filtro) throws Exception {
		return ReceitaServiceDAO.listarReceitas(this.con, filtro);
	}

	public void salvarReceita(Receita receita) throws RegraNegocioException, Exception {
		receita.setCategoria(CategoriaReceitaServiceDAO.pesquisarCategoria(con.getConexao(), receita.getCategoria().getCdCategoria()));
		if (receita.getCdReceita()==0)
			ReceitaServiceDAO.incluirReceita(this.con.getConexao(), receita);
		else
			ReceitaServiceDAO.alterarReceita(this.con.getConexao(), receita);
	}

	public void excluirReceita(Receita receita) throws RegraNegocioException, Exception {
		if (ReceitaServiceDAO.existeReceita(this.con, receita.getCdReceita()))
			ReceitaServiceDAO.excluirReceita(this.con, receita);
	}

	public List<DeducaoReceita> listarDespesasSobreVenda(Receita receita) throws Exception {
		return ReceitaServiceDAO.listarDeducoes(this.con.getConexao(), receita);
	}

	public void gravarDeducao(Receita receita, DeducaoReceita despesa) throws Exception {
		despesa.setCdReceita(receita.getCdReceita());
		ReceitaServiceDAO.incluirDeducao(this.con.getConexao(), despesa);
	}
	
	public void excluirDeducao(DeducaoReceita despesa) throws Exception {
		ReceitaServiceDAO.excluirDeducao(this.con, despesa);
	}

	public String[] listarTiposReceita() throws Exception {
		return ReceitaServiceDAO.listarTiposReceita(this.con.getConexao());
	}

}
