package com.ctbli9.valorplan.modelo;

import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

public class FuncValorDespesa {
	private MesAnoOrcamento mesRef;
	private Funcionario funcionario;
	private Double totalDespesaMes;
	
	public MesAnoOrcamento getMesRef() {
		return mesRef;
	}
	public void setMesRef(MesAnoOrcamento mesRef) {
		this.mesRef = mesRef;
	}
	public Funcionario getFuncionario() {
		return funcionario;
	}
	public void setFuncionario(Funcionario funcionario) {
		this.funcionario = funcionario;
	}
	public Double getTotalDespesaMes() {
		return totalDespesaMes;
	}
	public void setTotalDespesaMes(Double totalDespesaMes) {
		this.totalDespesaMes = totalDespesaMes;
	}
}
