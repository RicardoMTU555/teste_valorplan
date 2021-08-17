package com.ctbli9.valorplan.enumeradores;

public enum TipoRecurso {
	O("Operacional"),
	P("Produtivo"),
	G("Gerencial");

	/*
	 * Atributos do enum (separados por virgula dentro dos parentesis)
	 */
	private String descricao;
	
	/*
	 * Construtor do enum (carregando cada atributo criado)
	 */
	TipoRecurso(String descricao) {
		this.descricao = descricao;
	}
	
	/*
	 * getters dos atributos
	 */
	public String getDescricao() {
		return descricao;
	}
}
