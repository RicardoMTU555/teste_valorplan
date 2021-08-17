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
import com.ctbli9.valorplan.modelo.orc.ValorTotalAno;
import com.ctbli9.valorplan.negocio.consulta.ConsultaService;
import com.ctbli9.valorplan.negocio.consulta.EvolucaoAnoService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.RegraNegocioException;

@ManagedBean(name = "consultaEvolucaoAnosBean")
@ViewScoped
public class ConsultaEvolucaoAnosBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private ConexaoDB con = null;
	private EvolucaoAnoService servico;
	
	private int anoOrcamento;
	private List<ValorTotalAno> totalAnoReceitas = new ArrayList<>();
	private BarChartModel receitasChart;
	
	private List<ValorTotalAno> totalAnoDeducoes = new ArrayList<>();
	private BarChartModel deducoesChart;
	
	private List<ValorTotalAno> totalAnoDespGerais = new ArrayList<>();
	private BarChartModel despGeraisChart;
	
	private List<ValorTotalAno> totalAnoDespRH = new ArrayList<>();
	private BarChartModel despRHChart;
	
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
				servico = new EvolucaoAnoService(con);
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

	public List<ValorTotalAno> getTotalAnoReceitas() {
		return totalAnoReceitas;
	}
	
	public BarChartModel getReceitasChart() {
        return receitasChart;
    }
	
	public List<ValorTotalAno> getTotalAnoDeducoes() {
		return totalAnoDeducoes;
	}
	
	public BarChartModel getDeducoesChart() {
		return deducoesChart;
	}
	
	public List<ValorTotalAno> getTotalAnoDespGerais() {
		return totalAnoDespGerais;
	}

	public BarChartModel getDespGeraisChart() {
		return despGeraisChart;
	}

	public List<ValorTotalAno> getTotalAnoDespRH() {
		return totalAnoDespRH;
	}

	public BarChartModel getDespRHChart() {
		return despRHChart;
	}

	/*
	 * Metodos
	 */
	
	public void listar(TreeNode elo) {
		this.cenCusto = new CentroCusto();
		this.totalAnoReceitas.clear();
		this.receitasChart = null;
		
		this.totalAnoDeducoes.clear();
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
				this.totalAnoReceitas = servico.listarReceitasTotalAno(this.cenCusto, TipoSaldo.REC);
				this.receitasChart = servico.graficoEvolucaoAnos(this.totalAnoReceitas,
						"Evolução Anual de Receitas");

				this.totalAnoDeducoes = servico.listarReceitasTotalAno(this.cenCusto, TipoSaldo.DED);
				this.deducoesChart = servico.graficoEvolucaoAnos(this.totalAnoDeducoes,
						"Evolução Anual de Deduções");

				this.totalAnoDespGerais = servico.listarReceitasTotalAno(this.cenCusto, TipoSaldo.DGE);
				this.despGeraisChart = servico.graficoEvolucaoAnos(this.totalAnoDespGerais,
						"Evolução Anual de Despesas Gerais");
				
				this.totalAnoDespRH = servico.listarReceitasTotalAno(this.cenCusto, TipoSaldo.DRH);
				this.despRHChart = servico.graficoEvolucaoAnos(this.totalAnoDespRH,
						"Evolução Anual de Despesas RH/Pessoal");
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
