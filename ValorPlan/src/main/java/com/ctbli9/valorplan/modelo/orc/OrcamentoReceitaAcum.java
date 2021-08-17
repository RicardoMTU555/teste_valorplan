package com.ctbli9.valorplan.modelo.orc;

import java.math.BigDecimal;

import com.ctbli9.valorplan.modelo.receita.Receita;

public class OrcamentoReceitaAcum {
	private Receita receita;
	private BigDecimal valorAcumulado = BigDecimal.ZERO;
	
	public Receita getReceita() {
		return receita;
	}
	public void setReceita(Receita receita) {
		this.receita = receita;
	}
	public BigDecimal getValorAcumulado() {
		if (valorAcumulado == null)
			return BigDecimal.ZERO;
		else
			return valorAcumulado;
	}
	public void setValorAcumulado(BigDecimal valorAcumulado) {
		if (valorAcumulado == null)
			this.valorAcumulado = BigDecimal.ZERO;
		else
			this.valorAcumulado = valorAcumulado;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((receita == null) ? 0 : receita.hashCode());
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
		OrcamentoReceitaAcum other = (OrcamentoReceitaAcum) obj;
		if (receita == null) {
			if (other.receita != null)
				return false;
		} else if (!receita.equals(other.receita))
			return false;
		return true;
	}
	
	public void addValorAcumulado(BigDecimal valorAcumulado) {
		if (valorAcumulado == null)
			valorAcumulado = BigDecimal.ZERO;
		setValorAcumulado(getValorAcumulado().add(valorAcumulado));
	}
}
