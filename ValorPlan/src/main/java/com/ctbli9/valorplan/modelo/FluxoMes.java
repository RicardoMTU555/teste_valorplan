package com.ctbli9.valorplan.modelo;

import java.math.BigDecimal;

public class FluxoMes {
	private String conta;
	private String natureza;
	private BigDecimal[] valor;
	
	public FluxoMes() {
	}
	
	public FluxoMes(String conta, String natureza, int ocorrencias) {
		this.conta = conta;
		this.natureza = natureza;
		
		this.valor = new BigDecimal[ocorrencias];
		for (int i = 0; i < this.valor.length; i++) {
			this.valor[i] = BigDecimal.ZERO;
		}
	}
	
	public String getConta() {
		return conta;
	}
	public void setConta(String conta) {
		this.conta = conta;
	}
	
	public String getNatureza() {
		return natureza;
	}
	public void setNatureza(String natureza) {
		this.natureza = natureza;
	}
	
	public BigDecimal[] getValor() {
		return valor;
	}
	public void setValor(BigDecimal[] valor) {
		this.valor = valor;
	}
}
