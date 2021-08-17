package com.ctbli9.valorplan.modelo.orc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.ctbli9.valorplan.modelo.CategImobilizado;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

public class OrcamentoInvestimento {
	private long cdInvestimento;
	private CentroCusto cencusto;
	private CategImobilizado imobilizado;
	private int sqInvestimento;
	private String dsInvestimento;
	private List<OrcamentoInvestimentoMes> valores;
	
	public OrcamentoInvestimento(CategImobilizado imobilizado) {
		this.imobilizado = imobilizado;
		
		this.valores = new ArrayList<OrcamentoInvestimentoMes>();
		for (int i = 1; i <= 12; i++) {
			OrcamentoInvestimentoMes valorMes = new OrcamentoInvestimentoMes();
			valorMes.setImobilizado(imobilizado);
			valorMes.setMesRef(new MesAnoOrcamento());
			valorMes.getMesRef().setMes(i);
			this.valores.add(valorMes);
		}
		
	}
	
	public long getCdInvestimento() {
		return cdInvestimento;
	}
	public void setCdInvestimento(long cdInvestimento) {
		this.cdInvestimento = cdInvestimento;
	}
	public CentroCusto getCencusto() {
		return cencusto;
	}
	public void setCencusto(CentroCusto cencusto) {
		this.cencusto = cencusto;
	}
	public CategImobilizado getImobilizado() {
		return imobilizado;
	}
	public void setImobilizado(CategImobilizado imobilizado) {
		this.imobilizado = imobilizado;
	}
	public int getSqInvestimento() {
		return sqInvestimento;
	}
	public void setSqInvestimento(int sqInvestimento) {
		this.sqInvestimento = sqInvestimento;
	}
	public String getDsInvestimento() {
		return dsInvestimento;
	}
	public void setDsInvestimento(String dsInvestimento) {
		this.dsInvestimento = dsInvestimento;
	}	
	public List<OrcamentoInvestimentoMes> getValores() {
		return valores;
	}
	public void setValores(List<OrcamentoInvestimentoMes> valores) {
		this.valores = valores;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cdInvestimento ^ (cdInvestimento >>> 32));
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
		OrcamentoInvestimento other = (OrcamentoInvestimento) obj;
		if (cdInvestimento != other.cdInvestimento)
			return false;
		return true;
	}
	
	public BigDecimal getValorAcum() {
		BigDecimal valor = BigDecimal.ZERO;
		
		for (OrcamentoInvestimentoMes valMes : valores) {
			valor = valor.add(valMes.getVrInvestimento());
		}
		
		return valor;
	}

	public void setValorMes(int anoMes, BigDecimal valInvestimento) {
		for (OrcamentoInvestimentoMes valor : valores) {
			if (valor.getMesRef().getAnoMes() == anoMes) {
				valor.setVrInvestimento(valInvestimento);
				valor.setVrAcumulado(valor.getVrAcumulado().add(valInvestimento));
			} else if (valor.getMesRef().getAnoMes() > anoMes) {
				valor.setVrAcumulado(valor.getVrAcumulado().add(valInvestimento));
			}
			
		}
		
	}
}
