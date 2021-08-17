package com.ctbli9.valorplan.bean;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import org.primefaces.model.chart.PieChartModel;
 
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.HorizontalBarChartModel;
import org.primefaces.model.chart.ChartSeries;
 
@ManagedBean
public class GraficoBean implements Serializable {
 
    private static final long serialVersionUID = 1L;
	
	private PieChartModel pieModel1;
    private PieChartModel pieModel2;
    
    private BarChartModel barModel;
    private HorizontalBarChartModel horizontalBarModel;
    
    private double valorMaximo;
 
    @PostConstruct
    public void init() {
        createPieModels();
        createBarModels();
        this.valorMaximo = 0;
    }
 
    public double getValorMaximo() {
		return valorMaximo;
	}
    public void setValorMaximo(double valorMaximo) {
    	if (valorMaximo > this.valorMaximo)
    		this.valorMaximo = valorMaximo;
	}
    
    /*
     * Pizza
     */
    public PieChartModel getPieModel1() {
        return pieModel1;
    }
     
    public PieChartModel getPieModel2() {
        return pieModel2;
    }
     
    private void createPieModels() {
        createPieModel1();
        createPieModel2();
    }
 
    private void createPieModel1() {
        pieModel1 = new PieChartModel();
         
        pieModel1.set("Brand 1", 540);
        pieModel1.set("Brand 2", 325);
        pieModel1.set("Brand 3", 702);
        pieModel1.set("Brand 4", 421);
         
        pieModel1.setTitle("Simple Pie");
        pieModel1.setLegendPosition("w");
        pieModel1.setShadow(false);
    }
     
    private void createPieModel2() {
        pieModel2 = new PieChartModel();
         
        /*pieModel2.set("Brand 1", 540);
        pieModel2.set("Brand 2", 325);
        pieModel2.set("Brand 3", 702);
        pieModel2.set("Brand 4", 421);
         */
        pieModel2.set("Receita 1", 5);
        pieModel2.set("Receita 2", 5);
        pieModel2.set("Receita 3", 30);
        pieModel2.set("Receita 4", 10);
        pieModel2.set("Receita 5", 40);
         
        pieModel2.setTitle("Evolução Mensal");
        pieModel2.setLegendPosition("e");
        pieModel2.setFill(true);
        pieModel2.setShowDataLabels(true);
        pieModel2.setDiameter(150);
        pieModel2.setShadow(true);
    }
 
    /*
     * Barra
     */
    
    public BarChartModel getBarModel() {
        return barModel;
    }
     
    public HorizontalBarChartModel getHorizontalBarModel() {
        return horizontalBarModel;
    }
 
    /*
     * Reginaldo: primeiro passo
     */
    private BarChartModel initBarModel() {
        BarChartModel model = new BarChartModel();
 
        /*ChartSeries boys = new ChartSeries();
        boys.setLabel("Orçado");
        boys.set("2018-01", 1000000);
        boys.set("2018-02", 875000);
        boys.set("2018-03", 750000);
        boys.set("2018-04", 1000450);
        boys.set("2018-05", 1200740);
 
        ChartSeries girls = new ChartSeries();
        girls.setLabel("Realizado");
        girls.set("2018-01", 1200000);
        girls.set("2018-02", 850000);
        girls.set("2018-03", 700000);
        girls.set("2018-04", 1000800);
        girls.set("2018-05", 1700000);*/
        
        ChartSeries boys = new ChartSeries();
        ChartSeries girls = new ChartSeries();

        boys.setLabel("Orçado");
        girls.setLabel("Realizado");

        for (int i = 0; i < 12; i++) {
        	String mes = String.format("%04d-%02d", 20418, (i + 1));
        	boys.set(mes, 1000 + (i * i));
        	girls.set(mes, 900 + (i));
        	
        	setValorMaximo(1000 + (i * i));	
		}
 
        model.addSeries(boys);
        model.addSeries(girls);
         
        return model;
    }
     
    private void createBarModels() {
        createBarModel();
        createHorizontalBarModel();
    }
     
    /*
     * Reginaldo: segundo passo
     */
    private void createBarModel() {
        barModel = initBarModel();
         
        barModel.setTitle("Evolução mensal comparativa");
        barModel.setLegendPosition("ne");
         
        Axis xAxis = barModel.getAxis(AxisType.X);
        xAxis.setLabel("Meses");
         
        Axis yAxis = barModel.getAxis(AxisType.Y);
        yAxis.setLabel("Valores");
        yAxis.setMin(0);
        yAxis.setMax(this.valorMaximo);
    }
     
    private void createHorizontalBarModel() {
        horizontalBarModel = new HorizontalBarChartModel();
 
        ChartSeries boys = new ChartSeries();
        boys.setLabel("Boys");
        boys.set("2004", 50);
        boys.set("2005", 96);
        boys.set("2006", 44);
        boys.set("2007", 55);
        boys.set("2008", 25);
 
        ChartSeries girls = new ChartSeries();
        girls.setLabel("Girls");
        girls.set("2004", 52);
        girls.set("2005", 60);
        girls.set("2006", 82);
        girls.set("2007", 35);
        girls.set("2008", 120);
 
        horizontalBarModel.addSeries(boys);
        horizontalBarModel.addSeries(girls);
         
        horizontalBarModel.setTitle("Horizontal and Stacked");
        horizontalBarModel.setLegendPosition("e");
        horizontalBarModel.setStacked(true);
         
        Axis xAxis = horizontalBarModel.getAxis(AxisType.X);
        xAxis.setLabel("Births");
        xAxis.setMin(0);
        xAxis.setMax(200);
         
        Axis yAxis = horizontalBarModel.getAxis(AxisType.Y);
        yAxis.setLabel("Gender");        
    }
    
}