package com.ctbli9.valorplan.negocio;

import java.util.List;

import com.ctbli9.valorplan.DAO.CargoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.Cargo;

import ctbli9.enumeradores.TipoCargo;
import ctbli9.recursos.RegraNegocioException;

public class CargoService {
	private ConexaoDB con;
	
	public CargoService(ConexaoDB con) {
		this.con = con;
	}

	public Cargo pesquisarCargo(int cdCargo) throws Exception {
		return CargoServiceDAO.pesquisarCargo(con.getConexao(), cdCargo);
	}
	
	public void incluir(Cargo cargo) throws Exception {
		CargoServiceDAO.incluir(con.getConexao(), cargo);
	}

	public void alterar(Cargo cargo) throws Exception {
		CargoServiceDAO.alterar(con.getConexao(), cargo);
	}

	public List<Cargo> listar() throws Exception {
		return CargoServiceDAO.listar(con.getConexao());
	}

	public void salvar(Cargo cargo) throws Exception {
		if (cargo.getDsCargo().trim().isEmpty())
			throw new RegraNegocioException("Descrição do cargo é obrigatório.");
		
		if (cargo.getCdCargo()==0)
			CargoServiceDAO.incluir(con.getConexao(), cargo);
		else
			CargoServiceDAO.alterar(con.getConexao(), cargo);		
	}

	public void excluir(Cargo cargo) throws Exception {
		CargoServiceDAO.excluir(con.getConexao(), cargo);
	}

	public List<Cargo> pesquisar(String termoPesquisa) throws Exception {
		return CargoServiceDAO.listar(con.getConexao(), termoPesquisa);
	}

	public TipoCargo defineTipoCargo(String dsCargo) {
		
		TipoCargo tipo = TipoCargo.OP;
		
		if (dsCargo.toUpperCase().contains("DIRETOR"))
			tipo = TipoCargo.DI;
		else if (dsCargo.toUpperCase().contains("GERENTE") || ("COORD;SUPER".contains(dsCargo.toUpperCase())) )
			tipo = TipoCargo.GD;
		else if (dsCargo.toUpperCase().contains("TECNI") || dsCargo.toUpperCase().contains("CONSULT"))
			tipo = TipoCargo.TC;
		else if (dsCargo.toUpperCase().contains("VEND"))
			tipo = TipoCargo.VE;
		else if (dsCargo.toUpperCase().contains("COMPRA"))
			tipo = TipoCargo.CO;
		
		
		
		return tipo;
	}

}
