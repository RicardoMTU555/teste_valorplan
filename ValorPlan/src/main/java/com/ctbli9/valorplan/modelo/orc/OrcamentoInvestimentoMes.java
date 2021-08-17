package com.ctbli9.valorplan.modelo.orc;

import java.math.BigDecimal;

import com.ctbli9.valorplan.modelo.CategImobilizado;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

public class OrcamentoInvestimentoMes {
	private MesAnoOrcamento mesRef;
	private CategImobilizado imobilizado;
	private BigDecimal vrInvestimento = BigDecimal.ZERO;
	private BigDecimal vrAcumulado = BigDecimal.ZERO;
	
	public MesAnoOrcamento getMesRef() {
		return mesRef;
	}
	public void setMesRef(MesAnoOrcamento mesRef) {
		this.mesRef = mesRef;
	}

	public CategImobilizado getImobilizado() {
		return imobilizado;
	}
	public void setImobilizado(CategImobilizado imobilizado) {
		this.imobilizado = imobilizado;
	}
	
	public BigDecimal getVrInvestimento() {
		if (vrInvestimento == null)
			return BigDecimal.ZERO;
		else
			return vrInvestimento;
	}
	public void setVrInvestimento(BigDecimal vrInvestimento) {
		this.vrInvestimento = vrInvestimento;
	}
	
	public BigDecimal getVrAcumulado() {
		if (vrAcumulado == null)
			return BigDecimal.ZERO;
		else
			return vrAcumulado;
	}
	public void setVrAcumulado(BigDecimal vrAcumulado) {
		this.vrAcumulado = vrAcumulado;
	}
	
	// arred((vrInvestimento - (vrInvestimento * Residual)) * (depreciação / 12), 2)
	public BigDecimal getVrDepreciacao() {
		double invest = getVrAcumulado().doubleValue();
		double percResidual = imobilizado.getPercentResidual().doubleValue();
		double percDepreciacao = imobilizado.getPercentTaxaDepreciacao().doubleValue();
		
		double valor = (invest - (invest * percResidual)) * (percDepreciacao / 12);
		
		return BigDecimal.valueOf(valor);
	}	
}
