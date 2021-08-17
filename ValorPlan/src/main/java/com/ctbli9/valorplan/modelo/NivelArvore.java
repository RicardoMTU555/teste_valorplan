package com.ctbli9.valorplan.modelo;

import java.io.Serializable;

public class NivelArvore implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int minimo;
	private int maximo;
	private char gestor;   // D ou C
	
	public NivelArvore() {
		this.gestor = 'N';
	}
	
	public int getMinimo() {
		return minimo;
	}
	public void setMinimo(int minimo) {
		this.minimo = minimo;
	}
	public int getMaximo() {
		return maximo;
	}
	public void setMaximo(int maximo) {
		this.maximo = maximo;
	}
	
	public int getNumNiveis() {
		return ((this.maximo - this.minimo) + 1);
	}
	
	public char getGestor() {
		return gestor;
	}
	public void setGestor(char gestor) {
		this.gestor = gestor;
	}
}
