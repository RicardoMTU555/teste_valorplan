package com.ctbli9.valorplan.modelo;

import java.util.Date;

import com.ctbli9.valorplan.modelo.CentroCusto;
import ctbli9.modelo.Filial;
import ctbli9.modelo.ctb.ContaContabil;

public class MovtoContab {
	private Filial filial;
	private Date dtMovto;
	private int sqMovto;
	   
	private ContaContabil conta = new ContaContabil();
	private CentroCusto cencus = new CentroCusto();

	private String idNatMovto;
	private double vlMovto;
	private String txHistorico;
	private String dsDocumento;
	private String idLegado;
	
	public Filial getFilial() {
		return filial;
	}
	public void setFilial(Filial filial) {
		this.filial = filial;
	}
	public Date getDtMovto() {
		return dtMovto;
	}
	public void setDtMovto(Date dtMovto) {
		this.dtMovto = dtMovto;
	}
	public int getSqMovto() {
		return sqMovto;
	}
	public void setSqMovto(int sqMovto) {
		this.sqMovto = sqMovto;
	}
	public ContaContabil getConta() {
		return conta;
	}
	public void setConta(ContaContabil conta) {
		this.conta = conta;
	}
	public CentroCusto getCencus() {
		return cencus;
	}
	public void setCencus(CentroCusto cencus) {
		this.cencus = cencus;
	}
	public String getIdNatMovto() {
		return idNatMovto;
	}
	public void setIdNatMovto(String idNatMovto) {
		this.idNatMovto = idNatMovto;
	}
	public double getVlMovto() {
		return vlMovto;
	}
	public void setVlMovto(double vlMovto) {
		this.vlMovto = vlMovto;
	}
	public String getTxHistorico() {
		return txHistorico;
	}
	public void setTxHistorico(String txHistorico) {
		this.txHistorico = txHistorico;
	}
	public String getDsDocumento() {
		return dsDocumento;
	}
	public void setDsDocumento(String dsDocumento) {
		this.dsDocumento = dsDocumento;
	}
	public String getIdLegado() {
		return idLegado;
	}
	public void setIdLegado(String idLegado) {
		this.idLegado = idLegado;
	}
	
	@Override
	public String toString() {
		return "MovtoContab [filial=" + filial + ", dtMovto=" + dtMovto + ", sqMovto=" + sqMovto + ", conta=" + conta
				+ ", cencus=" + cencus + ", idNatMovto=" + idNatMovto + ", vlMovto=" + vlMovto + ", txHistorico="
				+ txHistorico + ", dsDocumento=" + dsDocumento + ", idLegado=" + idLegado + "]";
	}
}
