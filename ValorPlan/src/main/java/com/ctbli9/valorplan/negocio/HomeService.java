package com.ctbli9.valorplan.negocio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
//import org.primefaces.model.charts.optionconfig.animation.Animation;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.legend.LegendLabel;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.pie.PieChartOptions;

import com.ctbli9.valorplan.DAO.BalanceteServiceDAO;
import com.ctbli9.valorplan.DAO.DepartamentoServiceDAO;
import com.ctbli9.valorplan.DAO.FilialServiceDAO;
import com.ctbli9.valorplan.DAO.GraficoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.DadosGrafico;
import com.ctbli9.valorplan.modelo.DadosGraficoFilial;
import com.ctbli9.valorplan.modelo.NivelArvore;
import com.ctbli9.valorplan.modelo.ParametroFilial;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

public class HomeService {
    private String[] coresPreenchimento = 
    		new String[] {"rgba(7, 12, 99, 5.2)",
					    "rgba(63, 88, 232, 5.2)",
					    "rgba(116, 157, 227, 5.2)",
					    "rgba(116, 227, 207, 5.2)",
					    "rgba(38, 240, 69, 5.2)",
					    "rgba(223, 240, 38, 5.2)",
					    "rgba(242, 174, 48, 5.2)",
					    "rgba(235, 39, 242, 5.2)",
					    "rgba(242, 17, 17, 5.2)"
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
    
    private String[] recDed = 
    		new String[] {"Receitas", "Deduções", "Líquido"};
	
    private List<String> listaAreaReceita;
    private List<String> listaAreaGeral;
    private List<DadosGrafico> dadosBaseReceita;
    private List<DadosGraficoFilial> dadosBaseResultado;
    private NivelArvore nivel;
    private ParametroFilial parametro;
    
	private ConexaoDB con;
	
    public HomeService(ConexaoDB con) throws Exception {
		this.con = con;
		
		BalanceteServiceDAO dao = new BalanceteServiceDAO(con.getConexao());
		dao.carregarBalancete(true, new int[0], 0, false);
		dao.close();
		dao = null;
		
		nivel = DepartamentoServiceDAO.getNivelArvore(con.getConexao());
		parametro = FilialServiceDAO.obterParametros(con.getConexao());
		listaAreaReceita = GraficoServiceDAO.listarAreas(con.getConexao(), nivel, parametro.getNivelGrafico(), true);
		listaAreaGeral = GraficoServiceDAO.listarAreas(con.getConexao(), nivel, parametro.getNivelGrafico(), false);
		
		// Cria o mapa
		dadosBaseReceita = GraficoServiceDAO.gerarValoresRecDed(con.getConexao(), 
				listaAreaReceita, nivel, parametro.getNivelGrafico());
		
		dadosBaseResultado = GraficoServiceDAO.gerarGraficoResultadoFilial(con.getConexao());
	}

	public BarChartModel criarGraficoRecDed() throws Exception {
		
		// Carrega o grafico
		
		BarChartModel receitasBarModel = new BarChartModel();
        ChartData charData = new ChartData();
    	
        for (int i = 0; i < listaAreaReceita.size(); i++) {
        	String nomeReceita = listaAreaReceita.get(i);
        	
            BarChartDataSet barDataSet = new BarChartDataSet();
            barDataSet.setLabel(nomeReceita);
            
            // Valores das barras
            List<Number> values = new ArrayList<>();
            List<String> bgColor = new ArrayList<>();
            List<String> borderColor = new ArrayList<>();
            
            for (DadosGrafico graf : dadosBaseReceita) {
            	int indice = graf.getTipoReceita().indexOf(nomeReceita);   // Acha a receita
				
                values.add(graf.getReceita().get(indice));
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
        for (DadosGrafico graf : dadosBaseReceita) {
        	labels.add(graf.getSiglaMes());
		}
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
        
        // disable animation
        /*Animation animation = new Animation();
        animation.setDuration(0);
        options.setAnimation(animation);*/
        
        //Axis yAxis = receitasBarModel.getAxis(AxisType.Y);
        //yAxis.setTickFormat("%'.2f");
        receitasBarModel.setExtender("ajustarGrafico");		
        
        receitasBarModel.setOptions(options);
        
        return receitasBarModel;		
	}

	/*
	 BarChartModel <- ChartData <- BarChartDataSet
	 */
	public BarChartModel criarGraficoBarraDeducoes(MesAnoOrcamento mesRef) {
		BarChartModel receitasBarModel = new BarChartModel();
        ChartData charData = new ChartData();
    	
        for (DadosGrafico graf : dadosBaseReceita) {
        	if (graf.getSiglaMes().equals(mesRef.getNomeMes().substring(0, 3))) {
        		
        		for (int j = 0; j < this.recDed.length; j++) {
        			BarChartDataSet barDataSet = new BarChartDataSet(); // Cria uma série
        			
        	        // Valores das barras
        	        List<Number> values = new ArrayList<>();
        	        List<String> bgColor = new ArrayList<>();
        	        List<String> borderColor = new ArrayList<>();
        			
        			for (int i = 0; i < graf.getTipoReceita().size(); i++) {
        				BigDecimal valor = null;
        				
        				if (j == 0)
        					valor = graf.getReceita().get(i);
        				else if (j == 1)
        					valor = graf.getDeducao().get(i);
        				else if (j == 2)
        					valor = graf.getReceita().get(i).subtract(graf.getDeducao().get(i));
        				
        		        values.add(valor);
        		        bgColor.add(pegaCorPreenchimento(j));
        		        borderColor.add(pegaCorBorda(j));
            			
    				}
        			
        	        barDataSet.setData(values);
        	        barDataSet.setBackgroundColor(bgColor);
        	        barDataSet.setBorderColor(borderColor);
        	        barDataSet.setBorderWidth(1);
        	        barDataSet.setLabel(this.recDed[j]);
        	        
        	        charData.addChartDataSet(barDataSet);
        			
				}
        		
        		List<String> labels = new ArrayList<>();
        		for (int i = 0; i < graf.getTipoReceita().size(); i++) {
                	labels.add(graf.getTipoReceita().get(i)); // Rótulo horizontal
                }        		
        		charData.setLabels(labels);
        		break;
        	}
        }
        
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
        title.setText("Receitas e Deduções");
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
	
	public PieChartModel criarGraficoPizzaAcumReceita() {
		
		PieChartModel pieModel = new PieChartModel();
        ChartData charData = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();
        
        List<Number> values = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();
        
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < listaAreaReceita.size(); i++) {
        	String nomeReceita = listaAreaReceita.get(i);
        	labels.add(nomeReceita);
            
        	BigDecimal total = BigDecimal.ZERO;
			for (DadosGrafico graf : dadosBaseReceita) {
				int indice = graf.getTipoReceita().indexOf(nomeReceita);   // Acha a receita
				total = total.add(graf.getReceita().get(indice));
			}
			values.add(total);
			bgColors.add(pegaCorPreenchimento(i));
		    
		}
		
		dataSet.setData(values);
        dataSet.setBackgroundColor(bgColors);
        
        charData.addChartDataSet(dataSet);
        charData.setLabels(labels);
        pieModel.setData(charData);
        
		return pieModel;
	}

	
	public LineChartModel criarGraficoDespesaGeral() throws Exception {
		// Cria o mapa
		List<DadosGrafico> orcadoDespesa = GraficoServiceDAO.gerarGraficoDespesaGeral(con.getConexao(),
			listaAreaGeral, nivel, parametro.getNivelGrafico());
		
		return criarGraficoDespesas(orcadoDespesa, "Evolução Mensal de Despesas Gerais");
	}
		
	public LineChartModel criarGraficoDespesaRH() throws Exception {
		// Cria o mapa
		List<DadosGrafico> orcadoDespesa = GraficoServiceDAO.gerarGraficoDespesaRH(con.getConexao(),
			listaAreaGeral, nivel, parametro.getNivelGrafico());
		
		return criarGraficoDespesas(orcadoDespesa, "Evolução Mensal de Despesas de RH");
	}
		
	private LineChartModel criarGraficoDespesas(List<DadosGrafico> orcadoDespesa, String titulo) {
		LineChartModel lineModel = new LineChartModel();
        ChartData chartData = new ChartData();
        
        for (int i = 0; i < listaAreaGeral.size(); i++) {
        	String nomeDespesa = listaAreaGeral.get(i);
        
        	LineChartDataSet lineDataSet = new LineChartDataSet();
        	lineDataSet.setLabel(nomeDespesa);
            
            // Valores das barras
            List<Object> values = new ArrayList<>();
            for (DadosGrafico graf : orcadoDespesa) {
            	// Acha a receita
            	int indice = graf.getTipoReceita().indexOf(nomeDespesa);
				
                values.add(graf.getDeducao().get(indice));
    		}
            lineDataSet.setData(values);
            lineDataSet.setFill(false);
            lineDataSet.setLabel(nomeDespesa);
            lineDataSet.setBorderColor(pegaCorBorda(i));
            lineDataSet.setLineTension(0.1);
            chartData.addChartDataSet(lineDataSet);

        }
         
        // Meses
        List<String> labels = new ArrayList<>();
        for (DadosGrafico graf : orcadoDespesa) {
        	labels.add(graf.getSiglaMes());
		}
        chartData.setLabels(labels);
        
        //Options
        LineChartOptions options = new LineChartOptions();        
        Title title = new Title();
        title.setDisplay(true);
        title.setText(titulo);
        options.setTitle(title);
        
        lineModel.setOptions(options);
        lineModel.setData(chartData);
				
		return lineModel;
	}

	
	public BarChartModel criarGraficoBarraResultado(MesAnoOrcamento mesRef) {
		BarChartModel resultadoBarModel = new BarChartModel();
        
		ChartData charData = new ChartData();

        BarChartDataSet barDataSetReceita = new BarChartDataSet();
        barDataSetReceita.setLabel("Receita");
		
        BarChartDataSet barDataSetDespesa = new BarChartDataSet();
        barDataSetDespesa.setLabel("Despesa");
		
        List<String> labels = new ArrayList<>();
        
        // Valores das barras
        List<Number> valuesRec = new ArrayList<>();
        List<Number> valuesDes = new ArrayList<>();
        
        List<String> bgColorRec = new ArrayList<>();
        List<String> borderColorRec = new ArrayList<>();
        
        List<String> bgColorDes = new ArrayList<>();
        List<String> borderColorDes = new ArrayList<>();
        
                
        for (DadosGraficoFilial graf : this.dadosBaseResultado) {
			
			if (graf.getSiglaMes().equals(mesRef.getNomeMes().substring(0, 3))) {
				
				valuesRec.add(graf.getReceita());
				valuesDes.add(graf.getDespesa());
	            
		        bgColorRec.add(pegaCorPreenchimento(0));
		        bgColorDes.add(pegaCorPreenchimento(8));

		        borderColorRec.add(pegaCorBorda(0));
		        borderColorDes.add(pegaCorBorda(8));

		        labels.add(graf.getSiglaFilial());	
			}
			
		}
        
        barDataSetReceita.setData(valuesRec);
        barDataSetReceita.setBackgroundColor(bgColorRec);
        barDataSetReceita.setBorderColor(borderColorRec);
        barDataSetReceita.setBorderWidth(1);
        charData.addChartDataSet(barDataSetReceita);
                
        barDataSetDespesa.setData(valuesDes);
        barDataSetDespesa.setBackgroundColor(bgColorDes);
        barDataSetDespesa.setBorderColor(borderColorDes);
        barDataSetDespesa.setBorderWidth(1);
        charData.addChartDataSet(barDataSetDespesa);
        
        charData.setLabels(labels);


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
        title.setText("Evolução Mensal de Receitas e Despesas");
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
                
		resultadoBarModel.setData(charData);
        resultadoBarModel.setOptions(options);
        
        return resultadoBarModel;		

	}

	public PieChartModel criarGraficoPizzaResultado(MesAnoOrcamento mesRef) {
		PieChartModel pieModel = new PieChartModel();
        ChartData charData = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();
        
        List<Number> values = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();
        
        List<String> labels = new ArrayList<>();
        
        int i = 0;
        for (DadosGraficoFilial graf : this.dadosBaseResultado) {
			
			if (graf.getSiglaMes().equals(mesRef.getNomeMes().substring(0, 3))) {
				
				values.add(graf.getPercResultado());
	            
				labels.add(graf.getSiglaFilial());	
				
				bgColors.add(pegaCorPreenchimento(i++));
			}
			
		}
		
		dataSet.setData(values);
        dataSet.setBackgroundColor(bgColors);
        
        charData.addChartDataSet(dataSet);
        charData.setLabels(labels);
        pieModel.setData(charData);
        
        
        PieChartOptions options = new PieChartOptions();

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Resultado Percentual por Filial");
        options.setTitle(title);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        LegendLabel legendLabels = new LegendLabel();
        legendLabels.setFontStyle("bold");
        legendLabels.setFontColor("#2980B9");
        legendLabels.setFontSize(9);
        legend.setLabels(legendLabels);
        options.setLegend(legend);
        
        pieModel.setOptions(options);
        
		return pieModel;
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
