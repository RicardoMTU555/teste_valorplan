package com.ctbli9.valorplan.recursos;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import ctbli9.modelo.Plano;
import ctbli9.recursos.DataUtil;
import ctbli9.recursos.LibUtil;
import ctbli9.recursos.RegraNegocioException;

public class MesAnoOrcamento {
	private int mes;
	private int ano;
	private String nomeMes;
	private List<SelectItem> meses;
	
	private char[] mesAberto = null;


	/**
	 * Construtor sem parâmetro: seta mês e ano atuais
	 */
	public MesAnoOrcamento() {
		Plano plano = null;
		try {
			plano = (Plano)LibUtil.getParametroSessao("PLANO_ORCAMENTO");
			mesAberto = plano.getMesAberto();
		} catch (Exception e) {}
		
		if (plano == null) {
			plano = new Plano();
			plano.setNrAno(DataUtil.getAno());
			mesAberto = new char[] {'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S'};
		}
		
		this.ano = plano.getNrAno();
		if (this.ano > DataUtil.getAno())
			this.mes = 01;
		else
			this.mes = DataUtil.getMes();
		plano = null;
	}
	
	/**
	 * Construtor com parâmetros: seta mês e ano informados
	 * @param mes
	 * @param ano
	 */
	public MesAnoOrcamento(int mes, int ano) {
		this.mes = mes;
		this.ano = ano;
	}
	
	/*
	 * Atributos
	 */
	public int getAno() {
		return ano;
	}
	public void setAno(int ano) {
		this.ano = ano;
	}
	public int getMes() {
		return mes;
	}
	public void setMes(int mes) {
		this.mes = mes;
	}

	/**
	 * 
	 * @return mês/ano formatado mm-aaaa
	 */
	public String getMesAno() {
		return String.format("%02d-%04d", mes, ano);
	}
	
	/**
	 * 
	 * @param mesAnoStr Recebe mês/ano formatado como MMAAAA
	 * @throws RegraNegocioException valida o formato
	 */
	public void setMesAno(String mesAnoStr) throws RegraNegocioException {
		if (mesAnoStr.length() < 6)
			throw new RegraNegocioException("Mês/Ano em formato inválido.");
			
		if (mesAnoStr.length() > 6) {
			this.mes = Integer.parseInt(mesAnoStr.substring(0, 2));
			this.ano = Integer.parseInt(mesAnoStr.substring(3, 7));
		} else {
			this.mes = Integer.parseInt(mesAnoStr.substring(0, 2));
			this.ano = Integer.parseInt(mesAnoStr.substring(2, 6));
		}
	}

	/**
	 * 
	 * @return ano/mês formatado: aaaamm
	 */
	public int getAnoMes() {
		return Integer.parseInt(String.format("%04d%02d", ano, mes));
	}
	
	/**
	 * 
	 * @param anoMes ano/mês formatado como AAAAMM
	 * @throws RegraNegocioException
	 */
	public void setAnoMes(int anoMes) throws RegraNegocioException {
		String anoMesStr = String.format("%06d", anoMes);
		this.ano = Integer.parseInt(anoMesStr.substring(0, 4));
		this.mes = Integer.parseInt(anoMesStr.substring(4, 6));
	}
	
	/**
	 * 
	 * @return primeiro mes do ano formatado como AAAAMM
	 */
	public int getMes01() {
		return Integer.parseInt(String.format("%04d%02d", this.ano, 01));
	}

	/**
	 * 
	 * @return mês anterior formatado como AAAAMM
	 */
	public int getMesAnterior() {
		if ((this.mes-1) < 1)
			return Integer.parseInt(String.format("%04d%02d", (this.ano-1), 12 ));
		else
			return Integer.parseInt(String.format("%04d%02d", this.ano, (this.mes-1) ));
	}

	/**
	 * 
	 * @return mês Posterior formatado como AAAAMM
	 */
	public int getMesPosterior() {
		if ((this.mes+1) > 12)
			return Integer.parseInt(String.format("%04d%02d", (this.ano+1), 01 ));
		else
			return Integer.parseInt(String.format("%04d%02d", this.ano, (this.mes+1) ));
	}

	/**
	 * Valida se o mês/ano é maior que o atual
	 * @return true ou false
	 */
	public boolean validaDataMaiorQueAtual() {
		if (this.ano > DataUtil.getAno() || (this.ano == DataUtil.getAno() && this.mes > DataUtil.getMes() )) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Adiciona 1 ao mês
	 */
	public void add() {
		if (++this.mes > 12) {
			this.mes = 01;
			this.ano++;
		}
	}
	
	/**
	 * Subtrai 1 ao mês
	 */
	public void sub() {
		if (--this.mes < 1) {
			this.mes = 12;
			this.ano--;
		}
	}

	/**
	 * 
	 * @return nome do mês carregado
	 */
	public String getNomeMes() {
		switch (this.mes) {
		case 1:
			this.nomeMes = "Janeiro";
			break;
		case 2:
			this.nomeMes = "Fevereiro";
			break;
		case 3:
			this.nomeMes = "Março";
			break;
		case 4:
			this.nomeMes = "Abril";
			break;
		case 5:
			this.nomeMes = "Maio";
			break;
		case 6:
			this.nomeMes = "Junho";
			break;
		case 7:
			this.nomeMes = "Julho";
			break;
		case 8:
			this.nomeMes = "Agosto";
			break;
		case 9:
			this.nomeMes = "Setembro";
			break;
		case 10:
			this.nomeMes = "Outubro";
			break;
		case 11:
			this.nomeMes = "Novembro";
			break;
		case 12:
			this.nomeMes = "Dezembro";
			break;

		default:
			this.nomeMes = " ";
			break;
		}
		return nomeMes;
	}

	/**
	 * 
	 * @return Arraylist de SelectItem com número do mês - Nome do mês
	 */
	public List<SelectItem> getMeses() {
		this.meses = new ArrayList<SelectItem>();
		
		String[] meses = new String[] {"Janeiro",
				"Fevereiro", 
				"Março", 
				"Abril", 
				"Maio", 
				"Junho", 
				"Julho", 
				"Agosto", 
				"Setembro", 
				"Outubro", 
				"Novembro", 
				"Dezembro"
				};
		
		
		for (int i = 0; i < mesAberto.length; i++) {
			this.meses.add(new SelectItem((i+1), meses[i]));
		}
		
		return this.meses;
	}
	
	public boolean isMesAberto() {
		return ((mesAberto[mes - 1] == 'S')); // && getAnoMes() >= new MesAnoOrcamento().getAnoMes());
	}

	@Override
	public String toString() {
		return "MesAno [mes=" + mes + ", ano=" + ano + "]";
	}
	
	
}
