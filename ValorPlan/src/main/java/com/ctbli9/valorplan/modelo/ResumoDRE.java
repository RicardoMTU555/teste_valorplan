package com.ctbli9.valorplan.modelo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.primefaces.model.TreeNode;

public class ResumoDRE {
	private String descricao;
	private String tipo;
	private int chaveClasse;
	private TreeNode eloLigacao;
	
	private double[] valorOrcado;
	private double[] valorRealizado;
	
	private double valorOrcadoAcum;
	private double valorRealizadoAcum;
	
	
	/*
	 * Construtor
	 */
	public ResumoDRE(int periodo) {
		this.valorOrcado = new double[periodo];
		this.valorRealizado = new double[periodo];
		
		this.valorOrcadoAcum = 0;
		this.valorRealizadoAcum = 0;
	}
	
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	
	public String getTipo() {
		if (tipo == null)
			return "";
		else
			return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public int getChaveClasse() {
		return chaveClasse;
	}
	public void setChaveClasse(int chaveClasse) {
		this.chaveClasse = chaveClasse;
	}
	
	public TreeNode getEloLigacao() {
		return eloLigacao;
	}
	public void setEloLigacao(TreeNode eloLigacao) {
		this.eloLigacao = eloLigacao;
	}
	
	public double[] getValorOrcado() {
		return valorOrcado;
	}
	public double getValorOrcado(int periodo) {
		return valorOrcado[--periodo];
	}
	public void setValorOrcado(double[] valorOrcado) {
		this.valorOrcado = valorOrcado;
	}
	public void setValorOrcado(int periodo, double valorOrcado) {
		this.valorOrcado[--periodo] = valorOrcado;
	}
	public void addValorOrcado(int periodo, double valorOrcado) {
		this.valorOrcado[--periodo] += valorOrcado;
	}
	
	public String obtemOrcadoStr(int indice) {
		String valorStr = "";
		DecimalFormat df = null;
		
		DecimalFormatSymbols formato = DecimalFormatSymbols.getInstance(Locale.UK);
		formato.setDecimalSeparator(',');
		formato.setGroupingSeparator('.'); 

		if (this.getTipo().equals("PER"))
			df = new DecimalFormat("###0.00", formato);
		else
			df = new DecimalFormat("#,##0", formato);
		
		valorStr = df.format(this.valorOrcado[indice]);
		
		if (this.getTipo().equals("PER"))
			valorStr += "%";
		
		return valorStr;
	}

	public double[] getValorRealizado() {
		return valorRealizado;
	}
	public double getValorRealizado(int periodo) {
		return valorRealizado[--periodo];
	}
	public void setValorRealizado(double[] valorRealizado) {
		this.valorRealizado = valorRealizado;
	}
	public void setValorRealizado(int periodo, double valorRealizado) {
		this.valorRealizado[--periodo] = valorRealizado;
	}
	public void addValorRealizado(int periodo, double valorRealizado) {
		this.valorRealizado[--periodo] += valorRealizado;
	}
	
	public String obtemRealizadoStr(int indice) {
		String valorStr = "";
		DecimalFormat df = null;
		
		DecimalFormatSymbols formato = DecimalFormatSymbols.getInstance(Locale.UK);
		formato.setDecimalSeparator(',');
		formato.setGroupingSeparator('.'); 

		if (this.getTipo().equals("PER"))
			df = new DecimalFormat("###0.00", formato);
		else
			df = new DecimalFormat("#,##0", formato);
		
		valorStr = df.format(this.valorRealizado[indice]);
		
		if (this.getTipo().equals("PER"))
			valorStr += "%";
		
		return valorStr;
	}
	
	public double obtemPercent(int indice) {	
		if (this.valorOrcado[indice] != 0)
			return (this.valorRealizado[indice] / this.valorOrcado[indice]) * 100;
		else
			return 0;
	}
	
	public double getValorOrcadoAcum() {
		return valorOrcadoAcum;
	}
	public void setValorOrcadoAcum(double valorOrcadoAcum) {
		this.valorOrcadoAcum = valorOrcadoAcum;
	}
	public void addValorOrcadoAcum(double valorOrcadoAcum) {
		this.valorOrcadoAcum += valorOrcadoAcum;
	}
	
	public double getValorRealizadoAcum() {
		return valorRealizadoAcum;
	}
	public void setValorRealizadoAcum(double valorRealizadoAcum) {
		this.valorRealizadoAcum = valorRealizadoAcum;
	}
	public void addValorRealizadoAcum(double valorRealizadoAcum) {
		this.valorRealizadoAcum += valorRealizadoAcum;
	}
	
	public double getPercentAcum() {

		if (this.getValorOrcadoAcum() != 0)
			//return ((orca - real) / orca) * 100;
			return (this.getValorRealizadoAcum() / this.getValorOrcadoAcum()) * 100;
		else
			return 0;
	}
}
