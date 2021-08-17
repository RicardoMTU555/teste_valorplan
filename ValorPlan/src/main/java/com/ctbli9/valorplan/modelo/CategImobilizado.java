package com.ctbli9.valorplan.modelo;

import java.math.BigDecimal;

import ctbli9.modelo.ctb.ContaContabil;

public class CategImobilizado {
	private int cdCategImobili;	
	private String dsCategImobili;
	private BigDecimal pcTaxaDepreciacao;
	private BigDecimal pcResidual;
	private ContaContabil contaAtivo;
	private ContaContabil contaDepreciacao;
	
	public int getCdCategImobili() {
		return cdCategImobili;
	}
	public void setCdCategImobili(int cdCategImobili) {
		this.cdCategImobili = cdCategImobili;
	}
	public String getDsCategImobili() {
		return dsCategImobili;
	}
	public void setDsCategImobili(String dsCategImobili) {
		this.dsCategImobili = dsCategImobili;
	}
	public BigDecimal getPcTaxaDepreciacao() {
		if (pcTaxaDepreciacao == null)
			return BigDecimal.ZERO;
		else
			return pcTaxaDepreciacao;
	}
	public void setPcTaxaDepreciacao(BigDecimal pcTaxaDepreciacao) {
		this.pcTaxaDepreciacao = pcTaxaDepreciacao;
	}
	public BigDecimal getPcResidual() {
		if (pcResidual == null)
			return BigDecimal.ZERO;
		else
			return pcResidual;
	}
	public void setPcResidual(BigDecimal pcResidual) {
		this.pcResidual = pcResidual;
	}
	
	public ContaContabil getContaAtivo() {
		return contaAtivo;
	}
	public void setContaAtivo(ContaContabil contaAtivo) {
		this.contaAtivo = contaAtivo;
	}
	
	public ContaContabil getContaDepreciacao() {
		return contaDepreciacao;
	}
	public void setContaDepreciacao(ContaContabil contaDepreciacao) {
		this.contaDepreciacao = contaDepreciacao;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cdCategImobili;
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
		CategImobilizado other = (CategImobilizado) obj;
		if (cdCategImobili != other.cdCategImobili)
			return false;
		return true;
	}
	
	
	public BigDecimal getPercentTaxaDepreciacao() {
		return getPcTaxaDepreciacao().divide(BigDecimal.valueOf(100.00));
	}
	public BigDecimal getPercentResidual() {
		return getPcResidual().divide(BigDecimal.valueOf(100.00));		
	}
	
}
