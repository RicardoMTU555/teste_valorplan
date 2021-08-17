package com.ctbli9.valorplan.modelo;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import com.ctbli9.valorplan.modelo.orc.Recurso;

import com.ctbli9.valorplan.recursos.MesAnoOrcamento;


public class Departamento {
	@Range(min = 1)
	private long cdDepartamento;
	private int nrAno;
	@NotEmpty
	private String sgDepartamento;   
	@NotEmpty
	private String dsDepartamento;
	private Departamento departamentoPai;
	private Recurso responsavel = new Recurso(new MesAnoOrcamento());    
	
	private String codCompleto;
	private int nivel;
	
	public long getCdDepartamento() {
		return cdDepartamento;
	}
	public void setCdDepartamento(long cdDepartamento) {
		this.cdDepartamento = cdDepartamento;
	}
	public int getNrAno() {
		return nrAno;
	}
	public void setNrAno(int nrAno) {
		this.nrAno = nrAno;
	}
	public String getSgDepartamento() {
		return sgDepartamento;
	}
	public void setSgDepartamento(String sgDepartamento) {
		this.sgDepartamento = sgDepartamento;
	}
	public String getDsDepartamento() {
		return dsDepartamento;
	}
	public void setDsDepartamento(String dsDepartamento) {
		this.dsDepartamento = dsDepartamento;
	}
	public Departamento getDepartamentoPai() {
		return departamentoPai;
	}
	public void setDepartamentoPai(Departamento departamentoPai) {
		this.departamentoPai = departamentoPai;
	}
	public Recurso getResponsavel() {
		return responsavel;
	}
	public void setResponsavel(Recurso responsavel) {
		this.responsavel = responsavel;
	}
	public String getCodCompleto() {
		return codCompleto;
	}
	public void setCodCompleto(String codCompleto) {
		this.codCompleto = codCompleto;
	}
	public int getNivel() {
		return nivel;
	}
	public void setNivel(int nivel) {
		this.nivel = nivel;
	}
		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cdDepartamento ^ (cdDepartamento >>> 32));
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
		Departamento other = (Departamento) obj;
		if (cdDepartamento != other.cdDepartamento)
			return false;
		return true;
	}

}
