package com.ctbli9.valorplan.modelo.orc;

import com.ctbli9.valorplan.enumeradores.TipoRecurso;
import com.ctbli9.valorplan.modelo.Cargo;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Funcionario;

import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

public class Recurso {
	private long cdRecurso;
	private String nmRecurso;
	private String nomeVinculo;
	private CentroCusto setor;
	private int nrAno;
	private Cargo cargo;
	private Funcionario vinculo;
	private int inicioVinculo;
	private Integer fimVinculo;
	private String codEquipeVenda = "";
	private TipoRecurso tipo;
	
	private MesAnoOrcamento mesRef;
	
	
	/*
	 * Constructor 1
	 */
	public Recurso() {
		this.mesRef = new MesAnoOrcamento();
	}
	
	/*
	 * Constructor 2
	 */
	public Recurso(MesAnoOrcamento mesRef) {
		this.mesRef = mesRef;
	}
	
	/*
	 * Constructor 3
	 */
	public Recurso(MesAnoOrcamento mesRef, long cdRecurso) {
		this.mesRef = mesRef;
		this.cdRecurso = cdRecurso;
	}
	
	public long getCdRecurso() {
		return cdRecurso;
	}
	public void setCdRecurso(long cdRecurso) {
		this.cdRecurso = cdRecurso;
	}
	public String getNmRecurso() {
		return nmRecurso;
	}
	public void setNmRecurso(String nmRecurso) {
		this.nmRecurso = nmRecurso;
	}
	
	public String getNomeVinculo() {
		return nomeVinculo;
	}
	public void setNomeVinculo(String nomeVinculo) {
		this.nomeVinculo = nomeVinculo;
	}
	
	public CentroCusto getSetor() {
		return setor;
	}
	public void setSetor(CentroCusto setor) {
		this.setor = setor;
	}

	public int getNrAno() {
		return nrAno;
	}
	public void setNrAno(int nrAno) {
		this.nrAno = nrAno;
	}

	public Cargo getCargo() {
		return cargo;
	}
	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}
	public Funcionario getVinculo() {
		return vinculo;
	}
	public void setVinculo(Funcionario vinculo) {
		this.vinculo = vinculo;
	}
	public int getInicioVinculo() {
		return inicioVinculo;
	}
	public void setInicioVinculo(int inicioVinculo) {
		if (inicioVinculo == 0)
			inicioVinculo = this.mesRef.getAnoMes();
		
		this.inicioVinculo = inicioVinculo;
	}
	public Integer getFimVinculo() {
		return fimVinculo;
	}
	public void setFimVinculo(Integer fimVinculo) {
		if (fimVinculo == 0)
			this.fimVinculo = null;
		else
			this.fimVinculo = fimVinculo;
	}
	
	public String getCodEquipeVenda() {
		if (codEquipeVenda == null || codEquipeVenda.isEmpty())
			return Long.toString(cdRecurso);
		else
			return codEquipeVenda;
	}
	public void setCodEquipeVenda(String codEquipeVenda) {
		this.codEquipeVenda = codEquipeVenda;
	}
	
	public TipoRecurso getTipo() {
		return tipo;
	}
	public void setTipo(TipoRecurso tipo) {
		this.tipo = tipo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cdRecurso ^ (cdRecurso >>> 32));
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
		Recurso other = (Recurso) obj;
		if (cdRecurso != other.cdRecurso)
			return false;
		return true;
	}

	public boolean isNovoRecurso() {
		return this.cdRecurso == 0;
	}
	
	public String getNomeVinculoAtual() {
		if (this.vinculo != null && this.vinculo.getCenCusto() != null && this.vinculo.getCenCusto().getCdCentroCusto() == this.setor.getCdCentroCusto())
			return nomeVinculo;
		else
			return nmRecurso;
	}
	
	public String getLoginVinculoAtual() {
		if (this.vinculo.getCenCusto().getCdCentroCusto() == this.setor.getCdCentroCusto())
			return vinculo.getLogUsuario();
		else
			return nmRecurso;
	}
	
}
