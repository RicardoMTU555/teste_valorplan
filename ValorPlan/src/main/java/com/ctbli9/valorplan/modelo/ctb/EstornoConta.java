package com.ctbli9.valorplan.modelo.ctb;

import com.ctbli9.valorplan.modelo.CentroCusto;
import ctbli9.modelo.ctb.ContaContabil;

public class EstornoConta {
	private ContaContabil conta;
	private CentroCusto cenCusto;
	
	public ContaContabil getConta() {
		return conta;
	}
	public void setConta(ContaContabil conta) {
		this.conta = conta;
	}
	public CentroCusto getCenCusto() {
		return cenCusto;
	}
	public void setCenCusto(CentroCusto cenCusto) {
		this.cenCusto = cenCusto;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		EstornoConta other = (EstornoConta) obj;
		if (conta == null) {
			if (other.conta != null)
				return false;
		} else if (!conta.equals(other.conta))
			return false;
		return true;
	}
}
