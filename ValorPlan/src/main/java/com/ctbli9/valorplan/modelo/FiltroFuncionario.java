package com.ctbli9.valorplan.modelo;

import com.ctbli9.valorplan.modelo.CentroCusto;

public class FiltroFuncionario {
	private String nmFuncionario;
	private String[] cargosSelecionados = new String[0];
	private CentroCusto centroCusto = new CentroCusto();
	private String[] areasSelecionadas = new String[0];
	private boolean ativo;
	private boolean inativo;
	
	public String getNmFuncionario() {
		if (nmFuncionario == null)
			nmFuncionario = "";
		return nmFuncionario;
	}
	public void setNmFuncionario(String nmFuncionario) {
		this.nmFuncionario = nmFuncionario;
	}
	public String[] getCargosSelecionados() {
		return cargosSelecionados;
	}
	public void setCargosSelecionados(String[] cargosSelecionados) {
		this.cargosSelecionados = cargosSelecionados;
	}
	public CentroCusto getCentroCusto() {
		return centroCusto;
	}
	public void setCentroCusto(CentroCusto centroCusto) {
		this.centroCusto = centroCusto;
	}
	public String[] getAreasSelecionadas() {
		return areasSelecionadas;
	}
	public void setAreasSelecionadas(String[] areasSelecionadas) {
		this.areasSelecionadas = areasSelecionadas;
	}
	public boolean isAtivo() {
		return ativo;
	}
	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
	public boolean isInativo() {
		return inativo;
	}
	public void setInativo(boolean inativo) {
		this.inativo = inativo;
	}
}
