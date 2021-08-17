package com.ctbli9.valorplan.modelo.importa;

import ctbli9.recursos.ArquivoTexto;

public class Importador {
	private String nomeLayout;
	private int colunas;
	private int codCharsert;
	private char separadorCampo;
	private char charDecimal;
	private String formatoData;
	private int maxNivel;
	
	public Importador() {
		this.codCharsert = 0;
		this.separadorCampo = ';';
		this.charDecimal = ',';
		this.formatoData = "dd/MM/yyyy";
	}
	
	public String getNomeLayout() {
		return nomeLayout;
	}
	public void setNomeLayout(String nomeLayout) {
		this.nomeLayout = nomeLayout.trim();
	}
	public int getColunas() {
		return colunas;
	}
	public void setColunas(int colunas) {
		this.colunas = colunas;
	}
	public TipoLayout getTipo() {
		
		if (";FILIAL;ESTRUTURA;FUNCIONARIO;PLANO_DE_CONTAS;CLASSE_DRE;CONTAS_OPERACIONAIS;".contains(nomeLayout))
			return TipoLayout.CADASTRO;
		else
			return TipoLayout.ORCADO;
	}
	
	public int getCodCharsert() {
		return codCharsert;
	}
	public void setCodCharsert(int codCharsert) {
		this.codCharsert = codCharsert;
	}
	public char getSeparadorCampo() {
		return separadorCampo;
	}
	public void setSeparadorCampo(char separadorCampo) {
		this.separadorCampo = separadorCampo;
	}
	public char getCharDecimal() {
		return charDecimal;
	}
	public void setCharDecimal(char charDecimal) {
		this.charDecimal = charDecimal;
	}
	public String getFormatoData() {
		return formatoData;
	}
	public void setFormatoData(String formatoData) {
		this.formatoData = formatoData;
	}
	public int getMaxNivel() {
		return maxNivel;
	}
	public void setMaxNivel(int maxNivel) {
		this.maxNivel = maxNivel;
	}
	
	
	
	public String getCharCode() {
		String charCode = ArquivoTexto.CHARSET_UTF8;
		
		if (this.getCodCharsert() == 0)
			charCode = ArquivoTexto.CHARSET_UTF8;
		else
			charCode = ArquivoTexto.CHARSET_ISO;
		
		return charCode;
	}
	
	public String getSeparadorCampoStr() {
		if (separadorCampo == '|')
			return "\\|";
		else
			return Character.toString(separadorCampo);
	}
	
}
