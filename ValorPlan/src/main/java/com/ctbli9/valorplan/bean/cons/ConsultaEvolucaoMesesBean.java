package com.ctbli9.valorplan.bean.cons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.TreeNode;
import org.primefaces.model.charts.bar.BarChartModel;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.enumeradores.TipoSaldo;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.orc.ValorTotalMes;
import com.ctbli9.valorplan.negocio.consulta.ConsultaService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.RegraNegocioException;

@ManagedBean(name = "consultaEvolucaoMesesBean")
@ViewScoped
public class ConsultaEvolucaoMesesBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private ConexaoDB con = null;
	private ConsultaService servico;
	
	private int anoOrcamento;
	private List<ValorTotalMes> totalMesReceitas = new ArrayList<>();
	private BarChartModel receitasChart;
	
	private List<ValorTotalMes> totalMesDeducoes = new ArrayList<>();
	private BarChartModel deducoesChart;
	
	private List<ValorTotalMes> totalMesDespesaGeral = new ArrayList<>();
	private BarChartModel despesaGeralChart;
	
	private List<ValorTotalMes> totalMesDespesaRH = new ArrayList<>();
	private BarChartModel despesaRHChart;
		
	private CentroCusto cenCusto;
	
	private FacesMessages msg = new FacesMessages();

	/*
	 * Contrutor
	 */	
    @PostConstruct
    public void init() {
    	
    	try {
			con = new ConexaoDB();
			if (Global.getFuncionarioLogado() != null) {
				servico = new ConsultaService(con);
			}
			ConexaoDB.gravarTransacao(con);
			
		} catch (RegraNegocioException e) {
			ConexaoDB.desfazerTransacao(con);			
		} catch (Exception e) {
			ConexaoDB.desfazerTransacao(con);
			e.printStackTrace();
		} //finally {
			//ConexaoDB.close(con);
		//}
    }
    
    @PreDestroy
    public void fechar() {
    	servico = null;
    	ConexaoDB.close(con);
    	con = null;
    }

    
	/*
	 * Atributos
	 */
    public int getAnoOrcamento() {
		return anoOrcamento;
	}

	public List<ValorTotalMes> getTotalMesReceitas() {
		return totalMesReceitas;
	}
	public BarChartModel getReceitasChart() {
        return receitasChart;
    }
	
	public List<ValorTotalMes> getTotalMesDeducoes() {
		return totalMesDeducoes;
	}
	public BarChartModel getDeducoesChart() {
		return deducoesChart;
	}
	
	public List<ValorTotalMes> getTotalMesDespesaGeral() {
		return totalMesDespesaGeral;
	}

	public BarChartModel getDespesaGeralChart() {
		return despesaGeralChart;
	}

	public List<ValorTotalMes> getTotalMesDespesaRH() {
		return totalMesDespesaRH;
	}

	public BarChartModel getDespesaRHChart() {
		return despesaRHChart;
	}
	
	/*
	 * Metodos
	 */
	
	

	public void listar(TreeNode elo) {
		this.cenCusto = new CentroCusto();
		this.totalMesReceitas.clear();
		this.receitasChart = null;
		
		this.totalMesDeducoes.clear();
		this.deducoesChart = null;
		
		if(elo != null) {
			if (elo.getData().getClass().getName().indexOf("CentroCusto") > -1) {
				this.cenCusto = (CentroCusto) elo.getData(); 
				listar();
			}
		}
	}

	public void listar() {
		if (this.cenCusto.getCdCentroCusto() > 0) { 
			try {
				this.totalMesReceitas = servico.listarReceitasTotalMes(this.cenCusto, TipoSaldo.REC);
				this.receitasChart = servico.graficoEvolucaoMeses(this.totalMesReceitas,
						"Evolução Mensal de Receitas");

				this.totalMesDeducoes = servico.listarReceitasTotalMes(this.cenCusto, TipoSaldo.DED);
				this.deducoesChart = servico.graficoEvolucaoMeses(this.totalMesDeducoes,
						"Evolução Mensal das Deduções de Vendas");

				this.totalMesDespesaGeral = servico.listarReceitasTotalMes(this.cenCusto, TipoSaldo.DGE);
				this.despesaGeralChart = servico.graficoEvolucaoMeses(this.totalMesDespesaGeral,
						"Evolução Mensal das Despesas Gerais");

				this.totalMesDespesaRH = servico.listarReceitasTotalMes(this.cenCusto, TipoSaldo.DRH);
				this.despesaRHChart = servico.graficoEvolucaoMeses(this.totalMesDespesaRH,
						"Evolução Mensal das Despesas de RH");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
