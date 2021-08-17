package com.ctbli9.valorplan.modelo.orc;

import java.math.BigDecimal;

import com.ctbli9.valorplan.enumeradores.TipoValorAno;

public class ValorTotalAno {
	private int ano;
	private BigDecimal valor = BigDecimal.ZERO;
	private ValorTotalAno anoPosterior;
	private TipoValorAno origemValor;
	
	public int getAno() {
		return ano;
	}
	public void setAno(int mesRef) {
		this.ano = mesRef;
	}
	
	public BigDecimal getValor() {
		if (valor == null)
			return BigDecimal.ZERO;
		else
			return valor;
	}
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
	
	public ValorTotalAno getAnoPosterior() {
		if (anoPosterior == null)
			return new ValorTotalAno();
		else
			return anoPosterior;
	}
	public void setAnoPosterior(ValorTotalAno anoPosterior) {
		if (anoPosterior == null)
			this.anoPosterior = new ValorTotalAno();
		else
			this.anoPosterior = anoPosterior;
	}
	
	public TipoValorAno getOrigemValor() {
		return origemValor;
	}
	public void setOrigemValor(TipoValorAno origemValor) {
		this.origemValor = origemValor;
	}
	
	public BigDecimal getVariacao() {
		if ((getValor().compareTo(BigDecimal.ZERO) == 0))
			return BigDecimal.ZERO;
		else
			return getAnoPosterior().getValor().multiply(new BigDecimal("100.00")).divide(getValor(), BigDecimal.ROUND_HALF_UP);
	}
}
