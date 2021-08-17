package com.ctbli9.valorplan.modelo.orc;

import java.math.BigDecimal;

import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

public class ValorTotalMes {
	private MesAnoOrcamento mesRef;
	private BigDecimal vrOrcado = BigDecimal.ZERO;
	private BigDecimal vrRealizado = BigDecimal.ZERO;
	
	public MesAnoOrcamento getMesRef() {
		return mesRef;
	}
	public void setMesRef(MesAnoOrcamento mesRef) {
		this.mesRef = mesRef;
	}
	
	public BigDecimal getVrOrcado() {
		if (vrOrcado == null)
			return BigDecimal.ZERO;
		else
			return vrOrcado;
	}
	public void setVrOrcado(BigDecimal vrOrcado) {
		this.vrOrcado = vrOrcado;
	}
	
	public BigDecimal getVrRealizado() {
		if (vrRealizado == null)
			return BigDecimal.ZERO;
		else
			return vrRealizado;
	}
	public void setVrRealizado(BigDecimal vrRealizado) {
		this.vrRealizado = vrRealizado;
	}
	
	public BigDecimal getVariacao() {
		if ((getVrOrcado().compareTo(BigDecimal.ZERO) == 0))
			return BigDecimal.ZERO;
		else
			return getVrRealizado().multiply(new BigDecimal("100.00")).divide(getVrOrcado(), BigDecimal.ROUND_HALF_UP);
	}
}
