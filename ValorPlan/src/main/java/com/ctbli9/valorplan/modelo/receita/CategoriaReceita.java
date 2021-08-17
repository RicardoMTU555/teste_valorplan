package com.ctbli9.valorplan.modelo.receita;

import org.hibernate.validator.constraints.NotEmpty;

import com.ctbli9.valorplan.enumeradores.MedidaOrcamento;


public class CategoriaReceita {
	private int cdCategoria;
	@NotEmpty
	private String dsCategoria;
	private MedidaOrcamento medida;
	
	public int getCdCategoria() {
		return cdCategoria;
	}
	public void setCdCategoria(int cdCategoria) {
		this.cdCategoria = cdCategoria;
	}
	public String getDsCategoria() {
		return dsCategoria;
	}
	public void setDsCategoria(String dsCategoria) {
		this.dsCategoria = dsCategoria;
	}
	public MedidaOrcamento getMedida() {
		return medida;
	}
	public void setMedida(MedidaOrcamento medida) {
		this.medida = medida;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cdCategoria;
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
		CategoriaReceita other = (CategoriaReceita) obj;
		if (cdCategoria != other.cdCategoria)
			return false;
		return true;
	}	
}
