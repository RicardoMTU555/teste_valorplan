package com.ctbli9.valorplan.modelo.ctb;

import com.ctbli9.valorplan.modelo.CentroCusto;

public class SelecionaCentroCusto extends CentroCusto {
	
	private static final long serialVersionUID = 1L;
	
	private boolean seleciona;

	public boolean isSeleciona() {
		return seleciona;
	}

	public void setSeleciona(boolean seleciona) {
		this.seleciona = seleciona;
	}
}
