package com.ctbli9.valorplan.modelo;

import java.io.Serializable;

import ctbli9.modelo.Filial;

public class ParametroFilial implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Filial filial;
	private int nivelGrafico;
	
	public Filial getFilial() {
		return filial;
	}
	public void setFilial(Filial filial) {
		this.filial = filial;
	}
	public int getNivelGrafico() {
		return nivelGrafico;
	}
	public void setNivelGrafico(int nivelGrafico) {
		this.nivelGrafico = nivelGrafico;
	}
	
	
}
