/*
 * C:\RMC\Desenv\Contabili9\Web\Valor\.metadata\.plugins\org.eclipse.wst.server.core\tmp0/temp/
 */
package com.ctbli9.valorplan.negocio.importar;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;

import com.ctbli9.valorplan.modelo.importa.Importador;
import com.ctbli9.valorplan.modelo.importa.LayoutArquivo;

import ctbli9.recursos.ArquivoTexto;
import ctbli9.recursos.LibUtil;

public class ImportadorService {
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, String> mapaSemantica = new TreeMap();

	private List<String> listaErros = new ArrayList<String>();
	private Importador formatoDados;



	public ImportadorService() {
		montaMapaSemantica();
	}


	public void setListaErros(List<String> listaErros) {
		this.listaErros = listaErros;
	}
	
	public Importador getFormatoDados() {
		return formatoDados;
	}
	public void setFormatoDados(Importador formatoDados) {
		this.formatoDados = formatoDados;
	}

	public String verificaTipoArquivo(String arquivo, Importador layout) throws Exception {
		String retorno = "";

		ArquivoTexto arq = new ArquivoTexto(LibUtil.localArquivoIntegra() + arquivo, layout.getCharCode());
		try {
			arq.abreLeitura();
			String linha = arq.lerLinha();
			String[] campos = linha.split(layout.getSeparadorCampoStr());
			layout.setNomeLayout(campos[0]);

			linha = arq.lerLinha();
			if (layout.getNomeLayout().startsWith("FILIAL")) {
				retorno = LayoutArquivo.carregarCamposFilial(linha, layout);

			} else if (layout.getNomeLayout().equals("ESTRUTURA")) {
				retorno = LayoutArquivo.carregarCamposEstrutura(linha, layout);

			} else if (layout.getNomeLayout().equals("FUNCIONARIO")) {
				retorno = LayoutArquivo.carregarCamposFuncionario(linha, layout);

			} else if (layout.getNomeLayout().equals("PLANO_DE_CONTAS")) {
				retorno = LayoutArquivo.carregarCamposPlanoContas(linha, layout);

			} else if (layout.getNomeLayout().equals("CLASSE_DRE")) {
				retorno = LayoutArquivo.carregarCamposDRE(linha, layout);

			} else if (layout.getNomeLayout().equals("CONTAS_OPERACIONAIS")) {
				retorno = LayoutArquivo.carregarCamposContaOperacionais(linha, layout);

			} else if (layout.getNomeLayout().equals("PARAM_DEDUCAO")) {
				retorno = LayoutArquivo.carregarCamposParamDeducao(linha, layout);

			} else if (layout.getNomeLayout().equals("MOVIMENTO_REALIZADO")) {
				retorno = LayoutArquivo.carregarCamposMovimento(linha, layout);
				
			} else if (layout.getNomeLayout().equals("ORCAMENTO_VENDAS")) {
				retorno = LayoutArquivo.carregarCamposOrcamentoVendas(linha, layout);

			} else if (layout.getNomeLayout().equals("ORCAMENTO_DESPESAS")) {
				retorno = LayoutArquivo.carregarCamposOrcamentoDespesa(linha, layout);
				
			} else if (layout.getNomeLayout().equals("SALARIO_FUNCIONARIO")) {
				retorno = LayoutArquivo.carregarCamposSalario(linha, layout);

			} else {
				retorno += "Layout nao existente.";
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage(), e);

		} finally {
			try {
				arq.fechaLeitura();
			} catch (Exception e) {
			}
		}

		return retorno;
	}

	
	public void addErro(String msgErro) {
		boolean achou = false;
		for (String erro : listaErros) {
			if (erro.equals(msgErro)) {
				achou = true;
				break;
			}
		}
		if (!achou)
			listaErros.add(msgErro);
	}
	
	public void limpaListaErro() {
		listaErros.clear();
	}
	
	public boolean temErro() {
		return !listaErros.isEmpty();
	}

	
	public String gerarArquivo() {
		String caminhoRelatorio = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
		caminhoRelatorio += "relatorios/";

		String nomeRelat = "erros_" + this.formatoDados.getNomeLayout() + ".html";
		caminhoRelatorio += nomeRelat;

		ArquivoTexto arq = new ArquivoTexto(caminhoRelatorio);
		arq.apagaArquivo();
		try {
			arq.abreGrava();

			arq.imprimeLinha("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
			arq.imprimeLinha(
					"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" >");
			arq.imprimeLinha("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			arq.imprimeLinha("<head>");
			arq.imprimeLinha("	<title>Erros</title>");
			arq.imprimeLinha("</head>");

			arq.imprimeLinha("<body>");
			for (String erro : listaErros) {
				arq.imprimeLinha("  <span style=\"font-size: 11pt; font-family: Calibri\">" + erro + "</span>");
				arq.imprimeLinha("  <br/>");
			}
			arq.imprimeLinha("</body>");

			arq.imprimeLinha("</html>");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			arq.fechaGrava();
		}

		return nomeRelat;
	}
	
	
	public String gerarSigla(String texto) {
		String retorno = "";
		String partes[] = LibUtil.substituiAcentuado(texto.toUpperCase()).split(" ");
		
		for (int i = 0; i < partes.length; i++) {
			String parteValida = defineSemantica(partes[i]);

			if (parteValida == null) {
				parteValida = "";
				for (int j = 0; j < partes[i].length(); j++) {
					if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ".contains(partes[i].substring(j, j + 1)))
						parteValida += partes[i].substring(j, j + 1);
				}
			}
			
			if (parteValida.length() > 1) 
				retorno += LibUtil.truncaTexto(parteValida, 3).trim();
			
		}

		retorno = LibUtil.truncaTexto(retorno, 10).trim();

		return retorno;
	}
	
	public String defineSemantica(String chave) {
		chave = chave.trim();
		
		String valor = mapaSemantica.get(chave);
		
		return valor;
	}
	

	private void montaMapaSemantica() {
		// Chave, Valor
		mapaSemantica.put("ICMS", "ICMS");		
		mapaSemantica.put("PIS", "PIS");		
		mapaSemantica.put("CSLL", "CSLL");		
		mapaSemantica.put("RECEITA", "");		
		mapaSemantica.put("RECEITAS", "");		
		mapaSemantica.put("SOBRE", "S/");		
		mapaSemantica.put("DE", "");		
		mapaSemantica.put("COM", "");		
		mapaSemantica.put("JOHN", "JD");		
		mapaSemantica.put("DEERE", "");		
		mapaSemantica.put("AMS/AUTEQ", "AMS");		
		mapaSemantica.put("C/", "");		
		mapaSemantica.put("PARA", "");		
		mapaSemantica.put("IPVA", "IPVA");		
		mapaSemantica.put("IRPJ", "IRPJ");		
		mapaSemantica.put("ATIVO", "ATIVO");		
		mapaSemantica.put("PASSIVO", "PASSIVO");		
		/*mapaSemantica.put("", "");		
		mapaSemantica.put("", "");		
		mapaSemantica.put("", "");		
		mapaSemantica.put("", "");		
		mapaSemantica.put("", "");		
		*/
	}


}
