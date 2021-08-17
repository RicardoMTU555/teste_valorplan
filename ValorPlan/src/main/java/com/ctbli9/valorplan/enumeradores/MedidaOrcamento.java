package com.ctbli9.valorplan.enumeradores;

public enum MedidaOrcamento {
	Q("Quantidade"),
	V("Valor");

	private String descricao;
	
	MedidaOrcamento(String descricao) {
		this.descricao = descricao;
	}
	
	public String getDescricao() {
		return descricao;
	}

}
