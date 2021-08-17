package com.ctbli9.valorplan.modelo;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

public class Funcionario {
	@Range(min = 1, max = 999999)
	private int cdFuncionario;
	private BigDecimal dcFuncionario;
	@NotEmpty
	private String nmFuncionario;
	@NotEmpty
	private String logUsuario;
	@NotEmpty
	private String txEmailFuncionario;
	@NotNull
	private Cargo cargo = new Cargo();
	private CentroCusto cenCusto = new CentroCusto();
	private boolean funIdAtivo;
	
	public int getCdFuncionario() {
		return cdFuncionario;
	}
	public void setCdFuncionario(int cdFuncionario) {
		this.cdFuncionario = cdFuncionario;
	}
	
	public BigDecimal getDcFuncionario() {
		return dcFuncionario;
	}
	public void setDcFuncionario(BigDecimal dcFuncionario) {
		this.dcFuncionario = dcFuncionario;
	}
	
	public String getNmFuncionario() {
		return nmFuncionario;
	}
	public void setNmFuncionario(String nmFuncionario) {
		this.nmFuncionario = nmFuncionario;
	}
	public Cargo getCargo() {
		return cargo;
	}
	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}
	public String getLogUsuario() {
		return logUsuario;
	}
	public void setLogUsuario(String logUsuario) {
		this.logUsuario = logUsuario;
	}
	
	public CentroCusto getCenCusto() {
		return cenCusto;
	}
	public void setCenCusto(CentroCusto cenCusto) {
		this.cenCusto = cenCusto;
	}
	public String getTxEmailFuncionario() {
		return txEmailFuncionario;
	}
	public void setTxEmailFuncionario(String txEmailFuncionario) {
		this.txEmailFuncionario = txEmailFuncionario;
	}	
	public boolean getFunIdAtivo() {
		return funIdAtivo;
	}
	public void setFunIdAtivo(boolean funIdAtivo) {
		this.funIdAtivo = funIdAtivo;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cdFuncionario;
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
		Funcionario other = (Funcionario) obj;
		if (cdFuncionario != other.cdFuncionario)
			return false;
		return true;
	}

}
