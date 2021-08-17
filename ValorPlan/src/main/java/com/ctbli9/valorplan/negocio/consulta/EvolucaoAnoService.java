package com.ctbli9.valorplan.negocio.consulta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.legend.LegendLabel;
import org.primefaces.model.charts.optionconfig.title.Title;

import com.ctbli9.valorplan.DAO.BalanceteServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.DAO.consulta.EvolucaoAnoServiceDAO;
import com.ctbli9.valorplan.enumeradores.TipoSaldo;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.orc.ValorTotalAno;

import ctbli9.recursos.RegraNegocioException;

public class EvolucaoAnoService {
    private String[] coresPreenchimento = 
    		new String[] {"rgba(7, 12, 99, 5.2)", // Azul escuro
					    "rgba(63, 88, 232, 5.2)", // Azul claro
					    "rgba(116, 157, 227, 5.2)", // Azul mais claro
					    "rgba(116, 227, 207, 5.2)", // Verde Água
					    "rgba(38, 240, 69, 5.2)", // Verde
					    "rgba(223, 240, 38, 5.2)", // Amarelo
					    "rgba(242, 174, 48, 5.2)", // Laranja
					    "rgba(235, 39, 242, 5.2)", // Rosa
					    "rgba(242, 17, 17, 5.2)" // Vermelho
					    };
	
    private String[] coresBorda = 
    		new String[] {"rgb(7, 12, 99)",
					    "rgb(63, 88, 232)",
					    "rgb(116, 157, 227)",
					    "rgb(116, 227, 207)",
					    "rgb(38, 240, 69)",
					    "rgb(223, 240, 38)",
					    "rgb(242, 174, 48)",
					    "rgb(235, 39, 242)",
					    "rgb(242, 17, 17)"
					    };

    private ConexaoDB con;
	
	public EvolucaoAnoService(ConexaoDB con) throws Exception {
		this.con = con;
		
		BalanceteServiceDAO dao = new BalanceteServiceDAO(con.getConexao());
		dao.carregarBalancete(true, new int[0], 0, true);
		dao.importarRealizado(PlanoServiceDAO.getPlanoSelecionado().getNrAno() - 1);
		dao.close();
		dao = null;
	}

	
	/*
	 * 
	 */

	public List<ValorTotalAno> listarReceitasTotalAno(CentroCusto cenCusto, TipoSaldo tipo) throws RegraNegocioException, SQLException {
		List<ValorTotalAno> lista = new EvolucaoAnoServiceDAO(this.con).listarContasTotalAno(cenCusto, tipo);
		
		return lista;
	}
	
	public BarChartModel graficoEvolucaoAnos(List<ValorTotalAno> contas, String titulo) {
		BarChartModel receitasBarModel = new BarChartModel();
        ChartData charData = new ChartData();
    	
        	
        
        	
        BarChartDataSet barDataSet = new BarChartDataSet();
        barDataSet.setLabel("");
        
        // Valores das barras
        List<Number> values = new ArrayList<>();
        List<String> bgColor = new ArrayList<>();
        List<String> borderColor = new ArrayList<>();
        
        for (ValorTotalAno graf : contas) {
        	
        	values.add(graf.getValor());
			                
            bgColor.add(pegaCorPreenchimento(1));
            borderColor.add(pegaCorBorda(1));
		}
        barDataSet.setData(values);
        barDataSet.setBackgroundColor(bgColor);
        barDataSet.setBorderColor(borderColor);
        barDataSet.setBorderWidth(1);
        charData.addChartDataSet(barDataSet);
        
            
            
         
        // Meses
        List<String> labels = new ArrayList<>();
        contas.forEach(g->labels.add(Integer.toString(g.getAno())));
        
        charData.setLabels(labels);
        receitasBarModel.setData(charData);
        
        //Options
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setOffset(true);
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        ticks.setBeginAtZero(true);
        linearAxes.setTicks(ticks);
        cScales.addYAxesData(linearAxes);
        
        options.setScales(cScales);
        
        Title title = new Title();
        title.setDisplay(true);
        title.setText(titulo);
        options.setTitle(title);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        LegendLabel legendLabels = new LegendLabel();
        legendLabels.setFontStyle("bold");
        legendLabels.setFontColor("#2980B9");
        legendLabels.setFontSize(15);
        legend.setLabels(legendLabels);
        options.setLegend(legend);
        
        receitasBarModel.setOptions(options);
        
        return receitasBarModel;
	}
	
	// TODO refatorar isso
	private String pegaCorPreenchimento(int numeroCor) {
		if (numeroCor >= this.coresPreenchimento.length) {
			int posicao = numeroCor / this.coresPreenchimento.length;
			int base = this.coresPreenchimento.length * posicao;
			numeroCor -= base;
		}
		
		return this.coresPreenchimento[numeroCor];
	}
	private String pegaCorBorda(int numeroCor) {
		if (numeroCor >= this.coresBorda.length) {
			int posicao = numeroCor / this.coresBorda.length;
			int base = this.coresBorda.length * posicao;
			numeroCor -= base;
		}
		
		return this.coresBorda[numeroCor];
	}

}
