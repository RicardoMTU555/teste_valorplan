package com.ctbli9.valorplan.modelo;

import ctbli9.modelo.ctb.ContaContabil;

public class FiltroReceita {
	private String sgReceita;
	private String dsCompleta;
	private String[] cdCategorias = new String[0];
	private ContaContabil contaContabil;
	private boolean contaAtiva;
	private boolean contaInativa;
	
	public String getSgReceita() {
		if (sgReceita == null)
			sgReceita = "";
		return sgReceita;
	}
	public void setSgReceita(String sgReceita) {
		if (sgReceita != null)
			sgReceita = sgReceita.toUpperCase();
		this.sgReceita = sgReceita;
	}
	
	public String getDsCompleta() {
		if (dsCompleta == null)
			dsCompleta = "";
		return dsCompleta;
	}
	public void setDsCompleta(String dsCompleta) {
		this.dsCompleta = dsCompleta;
	}
	
	public String[] getCdCategorias() {
		return cdCategorias;
	}
	public void setCdCategorias(String[] cdCategorias) {
		this.cdCategorias = cdCategorias;
	}
	
	public ContaContabil getContaContabil() {
		return contaContabil;
	}
	public void setContaContabil(ContaContabil contaContabil) {
		this.contaContabil = contaContabil;
	}
	
	public boolean isContaAtiva() {
		return contaAtiva;
	}
	public void setContaAtiva(boolean contaAtiva) {
		this.contaAtiva = contaAtiva;
	}
	public boolean isContaInativa() {
		return contaInativa;
	}
	public void setContaInativa(boolean contaInativa) {
		this.contaInativa = contaInativa;
	}
}
