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
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.DAO.consulta.ConsultaServiceDAO;
import com.ctbli9.valorplan.enumeradores.TipoSaldo;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.ResumoDRE;
import com.ctbli9.valorplan.modelo.orc.ValorTotalMes;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.recursos.RegraNegocioException;

public class ConsultaService {
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
	
	public ConsultaService(ConexaoDB con) throws Exception {
		this.con = con;
		
		BalanceteServiceDAO dao = new BalanceteServiceDAO(con.getConexao());
		dao.carregarBalancete(true, new int[0], 0, true);
		dao.close();
		dao = null;
	}

	
	public List<ResumoDRE> listarReceitasPorConta(CentroCusto cenCusto, MesAnoOrcamento mesRef) throws SQLException {
		List<ResumoDRE> lista = new ConsultaServiceDAO(this.con).listarReceitasPorConta(cenCusto, mesRef);
		
		return lista;
	}


	public BarChartModel criarGraficoReceita(List<ResumoDRE> contasReceitas) {
		BarChartModel receitasBarModel = new BarChartModel();
        ChartData charData = new ChartData();
    	
        String nomeReceita = null;
        for (int i = 0; i < 4; i++) {
        	
        	switch (i) {
			case 0:
				nomeReceita = "Orçado";
				break;

			case 1:
				nomeReceita = "Realizado";
				break;

			case 2:
				nomeReceita = "Orc.Acumulado";
				break;

			case 3:
				nomeReceita = "Real.Acumulado";
				break;
			}

        	
            BarChartDataSet barDataSet = new BarChartDataSet();
            barDataSet.setLabel(nomeReceita);
            
            // Valores das barras
            List<Number> values = new ArrayList<>();
            List<String> bgColor = new ArrayList<>();
            List<String> borderColor = new ArrayList<>();
            
            for (ResumoDRE graf : contasReceitas) {
            	
            	switch (i) {
    			case 0:
    				values.add(graf.getValorOrcado(1));
    				break;

    			case 1:
    				values.add(graf.getValorRealizado(1));
    				break;

    			case 2:
    				values.add(graf.getValorOrcadoAcum());
    				break;

    			case 3:
    				values.add(graf.getValorRealizadoAcum());
    				break;
    			}
                
                bgColor.add(pegaCorPreenchimento(i));
                borderColor.add(pegaCorBorda(i));
    		}
            barDataSet.setData(values);
            barDataSet.setBackgroundColor(bgColor);
            barDataSet.setBorderColor(borderColor);
            barDataSet.setBorderWidth(1);
            charData.addChartDataSet(barDataSet);
        }
         
        // Meses
        List<String> labels = new ArrayList<>();
        contasReceitas.forEach(g->labels.add(g.getDescricao()));
        
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
        title.setText("Evolução Mensal de Receitas");
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

	
	/*
	 * 
	 */

	public List<ValorTotalMes> listarReceitasTotalMes(CentroCusto cenCusto, TipoSaldo tipo) throws RegraNegocioException, SQLException {
		List<ValorTotalMes> lista = new ConsultaServiceDAO(this.con).listarContasTotalMes(cenCusto, tipo);
		
		return lista;
	}
	
	public BarChartModel graficoEvolucaoMeses(List<ValorTotalMes> contas, String titulo) {
		BarChartModel receitasBarModel = new BarChartModel();
        ChartData charData = new ChartData();
    	
        String nomeReceita = null;
        for (int i = 0; i < 2; i++) {
        	
        	switch (i) {
			case 0:
				nomeReceita = "Orçado";
				break;

			case 1:
				nomeReceita = "Realizado";
				break;
			}

        	
            BarChartDataSet barDataSet = new BarChartDataSet();
            barDataSet.setLabel(nomeReceita);
            
            // Valores das barras
            List<Number> values = new ArrayList<>();
            List<String> bgColor = new ArrayList<>();
            List<String> borderColor = new ArrayList<>();
            
            for (ValorTotalMes graf : contas) {
            	
            	switch (i) {
    			case 0:
    				values.add(graf.getVrOrcado());
    				break;

    			case 1:
    				values.add(graf.getVrRealizado());
    				break;
    			}
                
                bgColor.add(pegaCorPreenchimento(i));
                borderColor.add(pegaCorBorda(i));
    		}
            barDataSet.setData(values);
            barDataSet.setBackgroundColor(bgColor);
            barDataSet.setBorderColor(borderColor);
            barDataSet.setBorderWidth(1);
            charData.addChartDataSet(barDataSet);
        }
         
        // Meses
        List<String> labels = new ArrayList<>();
        contas.forEach(g->labels.add(g.getMesRef().getNomeMes()));
        
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
