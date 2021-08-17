package com.ctbli9.valorplan.modelo.orc;

import java.math.BigDecimal;

import com.ctbli9.valorplan.modelo.CentroCusto;
import ctbli9.modelo.ctb.ContaContabil;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

public class OrcamentoDespesa {
	//private MesAnoOrcamento mesRef;
	private ContaContabil conta = new ContaContabil();
	private CentroCusto cenCusto = new CentroCusto();
	private BigDecimal vrConta = BigDecimal.ZERO;
	private BigDecimal vrContaAcum = BigDecimal.ZERO;
	private String classifDRE;
	
	/*public MesAnoOrcamento getMesRef() {
		return mesRef;
	}
	public void setMesRef(MesAnoOrcamento mesRef) {
		this.mesRef = mesRef;
	}*/
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
	public BigDecimal getVrConta() {
		if (vrConta == null)
			return BigDecimal.ZERO;
		else
			return vrConta;
	}
	public void setVrConta(BigDecimal vrConta) {
		this.vrConta = vrConta;
	}
	
	public BigDecimal getVrContaAcum() {
		if (vrContaAcum == null)
			return BigDecimal.ZERO;
		else
			return vrContaAcum;
	}
	public void setVrContaAcum(BigDecimal vrContaAcum) {
		this.vrContaAcum = vrContaAcum;
	}

	public String getClassifDRE() {
		return classifDRE;
	}
	public void setClassifDRE(String classifDRE) {
		this.classifDRE = classifDRE;
	}
	
	
	
	public void addVrDespesa(BigDecimal vrDespesa) {
		if (vrDespesa == null)
			vrDespesa = BigDecimal.ZERO;
		setVrConta(getVrConta().add(vrDespesa));
	}
	
	public void addVrDespesaAcum(BigDecimal vrDespesa) {
		if (vrDespesa == null)
			vrDespesa = BigDecimal.ZERO;
		setVrContaAcum(getVrContaAcum().add(vrDespesa));
	}
}
