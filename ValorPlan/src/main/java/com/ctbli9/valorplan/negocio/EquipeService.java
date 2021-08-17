package com.ctbli9.valorplan.negocio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.EquipeServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.enumeradores.TipoRecurso;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

public class EquipeService {
	private Connection con;

	public EquipeService(Connection con) {
		this.con = con;
	}

	public Recurso getRecurso(long idRecurso) throws Exception {
		return EquipeServiceDAO.getRecurso(con, idRecurso);
	}

	public List<Recurso> lisarEquipeSetor(CentroCusto setor) throws SQLException  {
		return EquipeServiceDAO.listarEquipeDoSetor(con, setor);
	}
	
	public List<Recurso> lisarEquipeGeral() throws SQLException  {
		return EquipeServiceDAO.listarEquipeGeral(con);
	}
	
	public List<Recurso> listarRecursos(TipoRecurso tipo) throws SQLException {
		return EquipeServiceDAO.lisarRecursos(con, tipo);
	}

	public List<Recurso> listarRecursosPorGestor(int cdGestor, MesAnoOrcamento mesRef, boolean produtivo) throws Exception {
		return EquipeServiceDAO.listarRecursoPorGestor(con, cdGestor, mesRef, produtivo);
	}

	public void salvar(Recurso recurso) throws SQLException, NamingException {
		if (recurso.getCdRecurso() == 0)
			EquipeServiceDAO.incluir(con, recurso);
		else 
			EquipeServiceDAO.alterar(con, recurso);		
	}

	public void excluir(Recurso recurso) throws SQLException {
		EquipeServiceDAO.excluir(con, recurso);
	}

	public void vincularRecursos(CentroCusto setor, Funcionario[] listaFunc) throws Exception {
		for (int i = 0; i < listaFunc.length; i++) {
			Recurso recurso = new Recurso(new MesAnoOrcamento());
			
			recurso.setCdRecurso(0);
			recurso.setNmRecurso("Recurso " + listaFunc[i].getCargo().getTpCargo().getDescricao());
			recurso.setVinculo(listaFunc[i]);
			recurso.setCargo(recurso.getVinculo().getCargo());
			recurso.setSetor(setor);
			recurso.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
			recurso.setInicioVinculo(0);
			recurso.setFimVinculo(0);
			recurso.setTipo(TipoRecurso.O);
			
			EquipeServiceDAO.incluir(con, recurso);			
		}
	}

}
