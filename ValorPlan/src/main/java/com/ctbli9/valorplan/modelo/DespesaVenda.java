package com.ctbli9.valorplan.modelo;

import java.math.BigDecimal;

import com.ctbli9.valorplan.modelo.receita.DeducaoReceita;

public class DespesaVenda {
	private int cdReceita; 
	private int cdCentroCusto; 
	private long cdFuncionario; 
	private DeducaoReceita deducao;
	private BigDecimal[] percDespesa;
	
	public int getCdReceita() {
		return cdReceita;
	}
	public void setCdReceita(int cdReceita) {
		this.cdReceita = cdReceita;
	}
	public int getCdCentroCusto() {
		return cdCentroCusto;
	}
	public void setCdCentroCusto(int cdCentroCusto) {
		this.cdCentroCusto = cdCentroCusto;
	}
	public long getCdFuncionario() {
		return cdFuncionario;
	}
	public void setCdFuncionario(long cdFuncionario) {
		this.cdFuncionario = cdFuncionario;
	}	
	public DeducaoReceita getDeducao() {
		return deducao;
	}
	public void setDeducao(DeducaoReceita deducao) {
		this.deducao = deducao;
	}
	public BigDecimal[] getPercDespesa() {
		return percDespesa;
	}
	public void setPercDespesa(BigDecimal[] percDespesa) {
		this.percDespesa = percDespesa;
	}
}
