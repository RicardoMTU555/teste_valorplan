package com.ctbli9.valorplan.modelo;

import java.math.BigDecimal;

public class DadosGraficoFilial {
	private String siglaMes;
	private String siglaFilial;
	private BigDecimal receita = BigDecimal.ZERO;
	private BigDecimal despesa = BigDecimal.ZERO;
	
	public String getSiglaMes() {
		return siglaMes;
	}
	public void setSiglaMes(String siglaMes) {
		this.siglaMes = siglaMes;
	}
	public String getSiglaFilial() {
		return siglaFilial;
	}
	public void setSiglaFilial(String siglaFilial) {
		this.siglaFilial = siglaFilial;
	}
	public BigDecimal getReceita() {
		return receita;
	}
	public void setReceita(BigDecimal receita) {
		this.receita = receita;
	}
	public BigDecimal getDespesa() {
		return despesa;
	}
	public void setDespesa(BigDecimal despesa) {
		this.despesa = despesa;
	}
	
	public BigDecimal getPercResultado() {
		if (this.getReceita().compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;
		else
			return this.getReceita().subtract(this.getDespesa()).multiply(BigDecimal.valueOf(100.00))
				.divide(this.getReceita(), BigDecimal.ROUND_UP);
	}
	
}
