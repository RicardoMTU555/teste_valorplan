package com.ctbli9.valorplan.modelo.orc;

import java.math.BigDecimal;

import ctbli9.modelo.ctb.ContaContabil;

public class OrcamentoDespesaVenda {
	private ContaContabil conta;
	private BigDecimal valorReceita;
	private BigDecimal percDespesa;
	private BigDecimal valorDespesa;
	
	public ContaContabil getConta() {
		return conta;
	}
	public void setConta(ContaContabil conta) {
		this.conta = conta;
	}
	public BigDecimal getPercDespesa() {
		if (percDespesa == null) {
			if (this.getValorReceita().compareTo(BigDecimal.ZERO) > 0)
				this.percDespesa = this.getValorDespesa().multiply(BigDecimal.valueOf(100.00)).divide(this.getValorReceita(), BigDecimal.ROUND_UP);
	    	else
	    		this.percDespesa = BigDecimal.ZERO;
		}
		return percDespesa;
	}
	public void setPercDespesa(BigDecimal percDespesa) {
		this.percDespesa = percDespesa; 
	}
	public BigDecimal getValorDespesa() {
		return valorDespesa;
	}
	public void setValorDespesa(BigDecimal valorDespesa) {
		this.valorDespesa = valorDespesa;
	}
	
	public BigDecimal getValorReceita() {
		return valorReceita;
	}
	public void setValorReceita(BigDecimal valorReceita) {
		this.valorReceita = valorReceita;
	}
	
}
