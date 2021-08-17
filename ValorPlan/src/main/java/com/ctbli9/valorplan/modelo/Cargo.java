package com.ctbli9.valorplan.modelo;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import ctbli9.enumeradores.TipoCargo;

public class Cargo {
	@Range(min = 1, max = 9999)
	private int cdCargo;
	@NotEmpty
	private String dsCargo;
	@NotNull
	private TipoCargo tpCargo;
	
	/*
	 * Construtores
	 */
	public Cargo() {
	}
	
	public Cargo(int cdCargo, String dsCargo, TipoCargo tpCargo) {
		this.cdCargo = cdCargo;
		this.dsCargo = dsCargo;
		setTpCargo(tpCargo);
	}
	
	/*
	 * get e set dos atributos
	 */
	public int getCdCargo() {
		return cdCargo;
	}
	public void setCdCargo(int cdCargo) {
		this.cdCargo = cdCargo;
	}
	public String getDsCargo() {
		return dsCargo;
	}
	public void setDsCargo(String dsCargo) {
		this.dsCargo = dsCargo;
	}
	public TipoCargo getTpCargo() {
		return tpCargo;
	}
	public void setTpCargo(TipoCargo tpCargo) {
		this.tpCargo = tpCargo;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cdCargo;
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
		Cargo other = (Cargo) obj;
		if (cdCargo != other.cdCargo)
			return false;
		return true;
	}
}
