package com.ctbli9.valorplan.bean;


import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.pie.PieChartModel;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.negocio.HomeService;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.recursos.RegraNegocioException;

@ManagedBean(name = "homeBean")
@ViewScoped
public class HomeBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private HomeService servico;
	
	private MesAnoOrcamento mesRef = new MesAnoOrcamento();
	
	private BarChartModel receitasChart;
	private BarChartModel recDedChart;
	private PieChartModel acumReceitaPie;
	private LineChartModel despesaGeralLine;
	private LineChartModel despesaRHLine;
	private BarChartModel resultadoChart;
	private PieChartModel percResultadoPie;
	
	public MesAnoOrcamento getMesRef() {
		return mesRef;
	}
	public void setMesRef(MesAnoOrcamento mesRef) {
		this.mesRef = mesRef;
	}
	public LineChartModel getDespesaGeralLine() {
		return despesaGeralLine;
	}
	public LineChartModel getDespesaRHLine() {
		return despesaRHLine;
	}
	public BarChartModel getReceitasChart() {
        return receitasChart;
    }
	public BarChartModel getRecDedChart() {
		return recDedChart;
	}
	public PieChartModel getAcumReceitaPie() {
		return acumReceitaPie;
	}
	public BarChartModel getResultadoChart() {
		return resultadoChart;
	}
	public PieChartModel getPercResultadoPie() {
		return percResultadoPie;
	}
	
	
    @PostConstruct
    public void init() {
    	ConexaoDB con = null;
    	try {
			con = new ConexaoDB();
			if (Global.getFuncionarioLogado() != null) {
				servico = new HomeService(con);
				receitasChart = servico.criarGraficoRecDed();
				acumReceitaPie = servico.criarGraficoPizzaAcumReceita();
				despesaGeralLine = servico.criarGraficoDespesaGeral();
				despesaRHLine =  servico.criarGraficoDespesaRH();
				montarGraficosMes();
				
			}
			ConexaoDB.gravarTransacao(con);
			
		} catch (RegraNegocioException e) {
			ConexaoDB.desfazerTransacao(con);
			
		} catch (Exception e) {
			ConexaoDB.desfazerTransacao(con);
			e.printStackTrace();
		} finally {
			ConexaoDB.close(con);
		}
    }
    
    
    public void montarGraficosMes() {
    	recDedChart = servico.criarGraficoBarraDeducoes(this.mesRef);
    	
    	resultadoChart = servico.criarGraficoBarraResultado(this.mesRef);
    	percResultadoPie = servico.criarGraficoPizzaResultado(this.mesRef);
	}
    
	public void mesAnterior() {
		int anoAux = this.mesRef.getAno();
		this.mesRef.sub();
		if (this.mesRef.getAno() == anoAux)
			montarGraficosMes();
		else
			this.mesRef.add();
	}

	public void mesPosterior() {
		int anoAux = this.mesRef.getAno();
		this.mesRef.add();
		if (this.mesRef.getAno() == anoAux)
			montarGraficosMes();
		else
			this.mesRef.sub();
	}

}
