package com.ctbli9.valorplan.modelo.orc;

import java.io.Serializable;
import java.math.BigDecimal;

import com.ctbli9.valorplan.modelo.CentroCusto;

import ctbli9.modelo.ctb.ContaContabil;

public class ComparativoAno implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private ContaContabil conta;
	private CentroCusto setor;
	private BigDecimal valorRealizadoAnterior = BigDecimal.ZERO;
	private BigDecimal valorOrcadoAtual = BigDecimal.ZERO;
	
	public ContaContabil getConta() {
		return conta;
	}
	public void setConta(ContaContabil conta) {
		this.conta = conta;
	}
	public CentroCusto getSetor() {
		return setor;
	}
	public void setSetor(CentroCusto setor) {
		this.setor = setor;
	}
	public BigDecimal getValorRealizadoAnterior() {
		return valorRealizadoAnterior;
	}
	public void setValorRealizadoAnterior(BigDecimal valorRealizadoAnterior) {
		this.valorRealizadoAnterior = valorRealizadoAnterior;
	}
	public BigDecimal getValorOrcadoAtual() {
		return valorOrcadoAtual;
	}
	public void setValorOrcadoAtual(BigDecimal valorOrcadoAtual) {
		this.valorOrcadoAtual = valorOrcadoAtual;
	}

}
