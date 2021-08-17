package com.ctbli9.valorplan.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;

import ctbli9.enumeradores.AreaSetor;
import ctbli9.modelo.Filial;

public class CentroCusto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Range(min = 1)
	private int cdCentroCusto;  
	@NotEmpty
	private String codExterno;
	private Filial filial;
	@NotEmpty
	private String cecDsResumida;  
	@NotEmpty
	private String cecDsCompleta;  
	private Recurso responsavel;    
	private AreaSetor tipoArea;
	
	private Departamento departamento = new Departamento(); 
	private List<CategoriaReceita> tipos = new ArrayList<>();
	
	
	
	public CentroCusto() {
	}
	
	public CentroCusto(int cdCentroCusto) {
		this.cdCentroCusto = cdCentroCusto;
	}

	public CentroCusto(int cdCentroCusto, String codExterno, String cecDsResumida) {
		this.cdCentroCusto = cdCentroCusto;
		this.codExterno = codExterno;
		this.cecDsResumida = cecDsResumida;
	}


	public int getCdCentroCusto() {
		return cdCentroCusto;
	}
	public void setCdCentroCusto(int cdCentroCusto) {
		this.cdCentroCusto = cdCentroCusto;
	}
	public String getCodExterno() {
		return codExterno;
	}
	public void setCodExterno(String codExterno) {
		this.codExterno = codExterno;
	}
	public Filial getFilial() {
		return filial;
	}
	public void setFilial(Filial filial) {
		this.filial = filial;
	}
	public String getCecDsResumida() {
		return cecDsResumida;
	}
	public void setCecDsResumida(String cecDsResumida) {
		this.cecDsResumida = cecDsResumida;
	}
	public String getCecDsCompleta() {
		return cecDsCompleta;
	}
	public void setCecDsCompleta(String cecDsCompleta) {
		this.cecDsCompleta = cecDsCompleta;
	}
	public Recurso getResponsavel() {
		return responsavel;
	}
	public void setResponsavel(Recurso responsavel) {
		this.responsavel = responsavel;
	}
	public AreaSetor getTipoArea() {
		return tipoArea;
	}
	public void setTipoArea(AreaSetor tipoArea) {
		this.tipoArea = tipoArea;
	}
	public Departamento getDepartamento() {
		return departamento;
	}
	public void setDepartamento(Departamento departamento) {
		this.departamento = departamento;
	}
	public List<CategoriaReceita> getTipos() {
		return tipos;
	}
	public void setTipos(List<CategoriaReceita> tipos) {
		this.tipos = tipos;
	}
		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cdCentroCusto;
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
		CentroCusto other = (CentroCusto) obj;
		if (cdCentroCusto != other.cdCentroCusto)
			return false;
		return true;
	}

}
