package com.ctbli9.valorplan.modelo.orc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

public class OrcamentoReceitaMes {
	private MesAnoOrcamento mesRef;
	private int quantidade;
	private BigDecimal valorUnitario = BigDecimal.ZERO;
	private BigDecimal percDespesa = BigDecimal.ZERO;
	private BigDecimal valorDespesa = BigDecimal.ZERO;
	private List<OrcamentoDespesaVenda> listaDespesas = new ArrayList<OrcamentoDespesaVenda>();
	
	public MesAnoOrcamento getMesRef() {
		return mesRef;
	}
	public void setMesRef(MesAnoOrcamento mesRef) {
		this.mesRef = mesRef;
	}
	public int getQuantidade() {
		return quantidade;
	}
	public void setQuantidade(int quantidade) {
		this.quantidade = quantidade;
	}
	public BigDecimal getValorUnitario() {
		if (valorUnitario == null)
			return BigDecimal.ZERO;
		else
			return valorUnitario;
	}
	public void setValorUnitario(BigDecimal valorUnitario) {
		if (valorUnitario == null)
			this.valorUnitario = BigDecimal.ZERO;
		else
			this.valorUnitario = valorUnitario;
	}
	public BigDecimal getPercDespesa() {
		return percDespesa;
	}
	public void setPercDespesa(BigDecimal percDespesa) {
		if (percDespesa == null) {
			if (this.getValorUnitario().compareTo(BigDecimal.ZERO) > 0)
				this.percDespesa = this.getValorDespesa().multiply(BigDecimal.valueOf(100.00)).divide(this.getValorUnitario(), BigDecimal.ROUND_UP);
	    	else
	    		this.percDespesa = BigDecimal.ZERO;
		}
		else	
			this.percDespesa = percDespesa;
	}
	public BigDecimal getValorDespesa() {
		if (valorDespesa == null)
			return BigDecimal.ZERO;
		else
			return valorDespesa;
	}
	public void setValorDespesa(BigDecimal valorDespesa) {
		if (valorDespesa == null)
			this.valorDespesa = BigDecimal.ZERO;
		else	
			this.valorDespesa = valorDespesa;
	}
	
	public List<OrcamentoDespesaVenda> getListaDespesas() {
		return listaDespesas;
	}
	public void setListaDespesas(List<OrcamentoDespesaVenda> listaDespesas) {
		this.listaDespesas = listaDespesas;
	}
	
	
	public BigDecimal getValorTotal() {
		return this.valorUnitario.multiply(new BigDecimal(this.quantidade));
	}
	
	public BigDecimal getLucroBruto() {
		if (this.valorDespesa != null)
			return getValorTotal().subtract(this.valorDespesa);
		else
			return BigDecimal.ZERO;
	}

	public BigDecimal getLucroBrutoUnitario() {
		if (this.valorDespesa != null)
			return getValorUnitario().subtract(this.valorDespesa);
		else
			return BigDecimal.ZERO;
	}

	public void addQuant(int quantidade) {
		this.quantidade = this.quantidade + quantidade; 
	}
	public void addValorUnitario(BigDecimal valorUnitario) {
		if (valorUnitario == null)
			valorUnitario = BigDecimal.ZERO;
		setValorUnitario(getValorUnitario().add(valorUnitario));
	}
	public void addValorDespesa(BigDecimal valorDespesa) {
		if (valorDespesa == null)
			valorDespesa = BigDecimal.ZERO;
		setValorDespesa(getValorDespesa().add(valorDespesa));
	}
}
