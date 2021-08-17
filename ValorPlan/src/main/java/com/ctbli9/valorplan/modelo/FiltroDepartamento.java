package com.ctbli9.valorplan.modelo;

public class FiltroDepartamento {
	private long deptoPai;
	private String descricao; 
	private String sigla;
	private String nomeFunc;
	private int nroAno;
	
	public long getDeptoPai() {
		return deptoPai;
	}
	public void setDeptoPai(long deptoPai) {
		this.deptoPai = deptoPai;
	}
	public String getDescricao() {
		if (descricao == null)
			descricao = "";
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public String getSigla() {
		if (sigla == null)
			sigla = "";
		return sigla;
	}
	public void setSigla(String sigla) {
		this.sigla = sigla;
	}
	public String getNomeFunc() {
		if (nomeFunc == null)
			nomeFunc = "";
		return nomeFunc;
	}
	public void setNomeFunc(String nomeFunc) {
		this.nomeFunc = nomeFunc;
	}
	public int getNroAno() {
		return nroAno;
	}
	public void setNroAno(int nroAno) {
		this.nroAno = nroAno;
	}
}
