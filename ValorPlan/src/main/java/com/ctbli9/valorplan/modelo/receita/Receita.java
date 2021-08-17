package com.ctbli9.valorplan.modelo.receita;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import ctbli9.modelo.ctb.ContaContabil;

public class Receita {
	@Min(1)
	private int cdReceita;
	@NotEmpty
	private String sgReceita;
	@NotEmpty
	private String dsReceita;
	private CategoriaReceita categoria;
	@NotNull
	private ContaContabil contaReceita;
	private boolean idAtivo;
	
	private List<DeducaoReceita> deducaoSobreVenda = new ArrayList<DeducaoReceita>();

	public int getCdReceita() {
		return cdReceita;
	}

	public void setCdReceita(int cdReceita) {
		this.cdReceita = cdReceita;
	}

	public String getSgReceita() {
		if (sgReceita == null)
			sgReceita = "";
		return sgReceita.toUpperCase();
	}

	public void setSgReceita(String sgReceita) {
		if (sgReceita == null)
			sgReceita = "";
		this.sgReceita = sgReceita;
	}

	public String getDsReceita() {
		return dsReceita;
	}

	public void setDsReceita(String dsReceita) {
		this.dsReceita = dsReceita;
	}

	public CategoriaReceita getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaReceita categoria) {
		this.categoria = categoria;
	}

	public ContaContabil getContaReceita() {
		return contaReceita;
	}

	public void setContaReceita(ContaContabil contaReceita) {
		this.contaReceita = contaReceita;
	}

	public boolean isIdAtivo() {
		return idAtivo;
	}

	public void setIdAtivo(boolean idAtivo) {
		this.idAtivo = idAtivo;
	}

	public List<DeducaoReceita> getDeducaoSobreVenda() {
		return deducaoSobreVenda;
	}
	
	public void setDeducaoSobreVenda(List<DeducaoReceita> deducaoSobreVenda) {
		this.deducaoSobreVenda = deducaoSobreVenda;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cdReceita;
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
		Receita other = (Receita) obj;
		if (cdReceita != other.cdReceita)
			return false;
		return true;
	}
}
