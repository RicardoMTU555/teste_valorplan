package com.ctbli9.valorplan.modelo;

public class FiltroCentroCusto {
	String descrResumida;
	int cdFilial;
	int cdDepartamento;
	private String[] areas = new String[0];
	private int[] departamentos = new int[0];
	private boolean setorDoGestor;
	
	public String getDescrResumida() {
		if (descrResumida == null)
			descrResumida = "";
		return descrResumida;
	}
	public void setDescrResumida(String descrResumida) {
		this.descrResumida = descrResumida;
	}
	public int getCdFilial() {
		return cdFilial;
	}
	public void setCdFilial(int cdFilial) {
		this.cdFilial = cdFilial;
	}
	public int getCdDepartamento() {
		return cdDepartamento;
	}
	public void setCdDepartamento(int cdDepartamento) {
		this.cdDepartamento = cdDepartamento;
	}
	public String[] getAreas() {
		return areas;
	}
	public void setAreas(String[] areas) {
		this.areas = areas;
	}
	public int[] getDepartamentos() {
		return departamentos;
	}
	public void setDepartamentos(int[] departamentos) {
		this.departamentos = departamentos;
	}
	public boolean isSetorDoGestor() {
		return setorDoGestor;
	}
	public void setSetorDoGestor(boolean setorDoGestor) {
		this.setorDoGestor = setorDoGestor;
	}
}
