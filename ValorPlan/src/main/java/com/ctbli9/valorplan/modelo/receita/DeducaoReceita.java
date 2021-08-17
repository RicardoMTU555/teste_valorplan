package com.ctbli9.valorplan.modelo.receita;

import java.io.Serializable;

import ctbli9.modelo.ctb.ContaContabil;

public class DeducaoReceita implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int cdReceita;
	private ContaContabil conta = new ContaContabil();
	
	public int getCdReceita() {
		return cdReceita;
	}
	public void setCdReceita(int cdReceita) {
		this.cdReceita = cdReceita;
	}
	
	public ContaContabil getConta() {
		return conta;
	}
	public void setConta(ContaContabil conta) {
		this.conta = conta;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cdReceita;
		result = prime * result + ((conta == null) ? 0 : conta.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeducaoReceita other = (DeducaoReceita) obj;
		if (cdReceita != other.cdReceita)
			return false;
		if (conta == null) {
			if (other.conta != null)
				return false;
		} else if (!conta.equals(other.conta))
			return false;
		return true;
	}
	
	
}
