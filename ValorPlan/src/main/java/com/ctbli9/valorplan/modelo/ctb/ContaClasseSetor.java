package com.ctbli9.valorplan.modelo.ctb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ctbli9.modelo.Filial;

public class ContaClasseSetor {

	private boolean utilizaSetor;
	private List<SelecionaCentroCusto> listaCencusto = new ArrayList<SelecionaCentroCusto>();
	private SelecionaCentroCusto setorZero;
	private int quantSetoresSelecionados;
	private List<SelecionaCentroCusto> setores = new ArrayList<SelecionaCentroCusto>();
	
	public ContaClasseSetor() {
		this.setorZero = new SelecionaCentroCusto();
		
		this.setorZero.setSeleciona(true);
		this.setorZero.setCdCentroCusto(0);
		this.setorZero.setCodExterno("TODOS");
		this.setorZero.setCecDsResumida("Não Utiliza Setor");
		this.setorZero.setFilial(new Filial());
		this.setorZero.getFilial().setCdFilial(0);
		this.setorZero.getFilial().setSgFilial("");
	}
	
	public boolean isUtilizaSetor() {
		return utilizaSetor;
	}
	public void setUtilizaSetor(boolean utilizaSetor) {
		if (!utilizaSetor)
			this.quantSetoresSelecionados = 1;
		this.utilizaSetor = utilizaSetor;
	}
	
	public List<SelecionaCentroCusto> getListaCencusto() {
		return listaCencusto;
	}
	public void setListaCencusto(List<SelecionaCentroCusto> listaCencusto) {
		this.listaCencusto = listaCencusto;
	}
	
	public SelecionaCentroCusto getSetorZero() {
		return setorZero;
	}
	public void setSetorZero(SelecionaCentroCusto setorZero) {
		this.setorZero = setorZero;
	}
	
	public int getQuantSetoresSelecionados() {
		return quantSetoresSelecionados;
	}
	
	public List<SelecionaCentroCusto> getSetores() {
		if (utilizaSetor) {
			this.setores = this.listaCencusto;
		} else {
			this.setores = Arrays.asList(this.setorZero);
		}
		return setores;
	}
	public void setSetores(List<SelecionaCentroCusto> setores) {
		if (utilizaSetor) {
			this.listaCencusto = setores;
		} else {
			this.setorZero = setores.get(0);
		}
		
	}
	
	/* *********************************************************************** */
	
	public void desmarcaSetores() {
		for (SelecionaCentroCusto setor : getListaCencusto()) {
			setor.setSeleciona(false);
		}
		this.quantSetoresSelecionados = 0;
	
	}
	public void addSetor() {
		this.quantSetoresSelecionados++;
	}
	public void subSetor() {
		this.quantSetoresSelecionados--;
	}	
}
