package com.ctbli9.valorplan.modelo.ctb;

import ctbli9.modelo.ctb.ContaContabil;

/*
 * POLIMORFISMO
 */
public class SelecionaConta extends ContaContabil {
	private boolean seleciona;

	public boolean isSeleciona() {
		return seleciona;
	}

	public void setSeleciona(boolean seleciona) {
		this.seleciona = seleciona;
	}
}
