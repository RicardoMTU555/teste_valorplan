package com.ctbli9.valorplan.modelo;

import java.math.BigDecimal;

import ctbli9.modelo.ctb.ContaContabil;


public class DespesaRH {
	private ContaContabil salario;
	private ContaContabil encargo;
	private BigDecimal[] percDespesa;
	
	public ContaContabil getSalario() {
		return salario;
	}
	public void setSalario(ContaContabil salario) {
		this.salario = salario;
	}
	public ContaContabil getEncargo() {
		return encargo;
	}
	public void setEncargo(ContaContabil encargo) {
		this.encargo = encargo;
	}
	public BigDecimal[] getPercDespesa() {
		return percDespesa;
	}
	public void setPercDespesa(BigDecimal[] pcDespesa) {
		this.percDespesa = pcDespesa;
	}
	
	public boolean isTemValor() {
		boolean retorno = false;
		
		for (int i = 0; i < percDespesa.length; i++) {
			if (percDespesa[i] != null && percDespesa[i].compareTo(BigDecimal.ZERO) > 0) {
				retorno = true;
				break;
			}
		}
		
		return retorno;
	}
}
