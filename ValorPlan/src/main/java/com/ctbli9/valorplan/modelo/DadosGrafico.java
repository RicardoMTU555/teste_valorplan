package com.ctbli9.valorplan.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DadosGrafico {
	private String siglaMes;
	private List<String> tipoReceita = new ArrayList<String>();
	private List<BigDecimal> receita = new ArrayList<BigDecimal>();
	private List<BigDecimal> deducao = new ArrayList<BigDecimal>();
	
	public String getSiglaMes() {
		return siglaMes;
	}
	public void setSiglaMes(String siglaMes) {
		this.siglaMes = siglaMes;
	}
	public List<String> getTipoReceita() {
		return tipoReceita;
	}
	public void setTipoReceita(List<String> tipoReceita) {
		this.tipoReceita = tipoReceita;
	}
	public List<BigDecimal> getReceita() {
		return receita;
	}
	public void setReceita(List<BigDecimal> receita) {
		this.receita = receita;
	}
	public List<BigDecimal> getDeducao() {
		return deducao;
	}
	public void setDeducao(List<BigDecimal> deducao) {
		this.deducao = deducao;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((siglaMes == null) ? 0 : siglaMes.hashCode());
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
		DadosGrafico other = (DadosGrafico) obj;
		if (siglaMes == null) {
			if (other.siglaMes != null)
				return false;
		} else if (!siglaMes.equals(other.siglaMes))
			return false;
		return true;
	}
}
