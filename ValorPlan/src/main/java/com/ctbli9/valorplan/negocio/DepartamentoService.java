package com.ctbli9.valorplan.negocio;

import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.DepartamentoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.Departamento;
import com.ctbli9.valorplan.modelo.FiltroDepartamento;
import com.ctbli9.valorplan.modelo.NivelArvore;

import ctbli9.recursos.RegraNegocioException;

public class DepartamentoService {
	private ConexaoDB con;
	
	public DepartamentoService(ConexaoDB con) {
		this.con = con;
	}

	public ConexaoDB getCon() {
		return con;
	}
	
	public Departamento pesquisarDepartamento(long cdDepartamento) throws Exception {
		return DepartamentoServiceDAO.pesquisarDepartamento(con.getConexao(), cdDepartamento);
	}
	
	public List<Departamento> listar(FiltroDepartamento filtro) throws Exception {
		return DepartamentoServiceDAO.listar(con.getConexao(), filtro);
	}
	
	public boolean existe(int cdDepartamento) throws SQLException, NamingException {
		return DepartamentoServiceDAO.existe(con.getConexao(), cdDepartamento);
	}

	public void incluir(Departamento depto) throws RegraNegocioException, Exception {
		if (depto.getResponsavel().getCdRecurso() == 0)
			throw new RegraNegocioException("Seleção do Responsável pelo departamento é abrigatória.");
		
		if (depto.getDsDepartamento().trim().isEmpty())
			throw new RegraNegocioException("Descrição é obrigatório.");
		
		if (DepartamentoServiceDAO.existe(con.getConexao(), depto.getCdDepartamento()))
			throw new RegraNegocioException("Departamento já existente.");
		
		DepartamentoServiceDAO.incluir(con.getConexao(), depto);
	}

	public void alterar(Departamento depto) throws RegraNegocioException, Exception {
		if (depto.getResponsavel().getCdRecurso() == 0)
			throw new RegraNegocioException("Seleção do Responsável pelo departamento é abrigatória.");

		if (depto.getDsDepartamento().trim().isEmpty())
			throw new RegraNegocioException("Descrição é obrigatório.");
		
		if (!DepartamentoServiceDAO.existe(con.getConexao(), depto.getCdDepartamento()))
			throw new RegraNegocioException("Departamento inexistente.");
		
		DepartamentoServiceDAO.alterar(con.getConexao(), depto);
	}

	public void excluir(Departamento depto) throws RegraNegocioException, Exception {
		if (!DepartamentoServiceDAO.existe(con.getConexao(), depto.getCdDepartamento()))
			throw new RegraNegocioException("Departamento inexistente.");
		
		DepartamentoServiceDAO.excluir(con.getConexao(), depto);
	}

	public int[] listarNiveisDepto(int nroAno) throws SQLException, NamingException, RegraNegocioException {
		NivelArvore nivel = DepartamentoServiceDAO.getNivelArvore(con.getConexao());
		
		if (nivel.getMinimo() == 0)
			throw new RegraNegocioException("Usuário não é gestor de departamento ou setor.");
		
		int[] lista = new int[nivel.getNumNiveis()];
		
		int pos = 0;
		for (int i = nivel.getMinimo(); i <= nivel.getMaximo(); i++) {
			lista[pos++] = i;
		}
		
		return lista;
	}
	
	public List<Departamento> listarAreas(int nivelArea) throws SQLException, NamingException, RegraNegocioException {
		return DepartamentoServiceDAO.listarAreas(con.getConexao(), nivelArea);
	}

	public void replicarDadosAno(int nroAnoOrigem, int nroAnoDestino) throws SQLException, NamingException, RegraNegocioException {
		if (nroAnoOrigem == nroAnoDestino)
			throw new RegraNegocioException("Ano origem e destino não podem ser iguais");
			
		DepartamentoServiceDAO.replicarDadosAno(con.getConexao(), nroAnoOrigem, nroAnoDestino);
			
	}	
}
