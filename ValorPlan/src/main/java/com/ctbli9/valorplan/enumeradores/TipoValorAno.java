package com.ctbli9.valorplan.enumeradores;

public enum TipoValorAno {
	ORÇADO("Orçado"),
	REALIZADO("Realizado");
	
	private String descricao;
	
	TipoValorAno(String descricao) {
		this.descricao = descricao;
	}
	
	public String getDescricao() {
		return descricao;
	}
}
