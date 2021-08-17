package com.ctbli9.valorplan.enumeradores;

public enum TipoSaldo {
	REC("Receita"),
	DED("Dedução"),
	DGE("Desp.Geral"),
	DRH("Desp.RH"); //,
	//ERH("Enc.RH");

	/*
	 * Atributos do enum (separados por virgula dentro dos parentesis)
	 */
	private String descricao;
	
	/*
	 * Construtor do enum (carregando cada atributo criado)
	 */
	TipoSaldo(String descricao) {
		this.descricao = descricao;
	}
	
	/*
	 * getters dos atributos
	 */
	public String getDescricao() {
		return descricao;
	}
}
