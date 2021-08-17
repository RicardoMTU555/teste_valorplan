package com.ctbli9.valorplan.modelo.importa;

import ctbli9.recursos.RegraNegocioException;

public class LayoutArquivo {
		
	private static String msgRetorno = null;
	
	public static int FIL_CODIGO        = -1;
	public static int FIL_CNPJ          = -1;
	public static int FIL_NOME          = -1; 
	public static int FIL_APELIDO       = -1; 
	public static int FIL_TIP_END       = -1; 
	public static int FIL_IDT_END       = -1; 
	public static int FIL_ENDERECO      = -1; 
	public static int FIL_NUM_END       = -1; 
	public static int FIL_COMP_END      = -1; 
	public static int FIL_CEP           = -1; 
	public static int FIL_NOM_BAIRRO    = -1; 
	public static int FIL_NOM_CIDADE    = -1; 
	public static int FIL_COD_IBGE      = -1; 
	public static int FIL_UF_END        = -1; 
	public static int FIL_IDT_PRINC     = -1; 
	public static int FIL_NOM_CONTATO   = -1; 
	public static int FIL_EMAIL_CONTATO = -1; 
	public static int FIL_TELEFONE      = -1; 
	public static int FIL_RAMAL         = -1; 
	public static int FIL_CELULAR       = -1; 
	public static int FIL_FAX           = -1;
	
	/*
	public static final int DEP_FILIAL        = getCadastroDepartamento("DEP_FILIAL");
	public static final int DEP_COD_EXTERNO   = getCadastroDepartamento("DEP_COD_EXTERNO");
	public static final int DEP_NOME          = getCadastroDepartamento("DEP_NOME");
	public static final int DEP_SIGLA         = getCadastroDepartamento("DEP_SIGLA");
	public static final int DEP_CPF_GESTOR    = getCadastroDepartamento("DEP_CPF_GESTOR");
	public static final int DEP_COD_DEPTO_PAI = getCadastroDepartamento("DEP_COD_DEPTO_PAI");
	
	public static final int SET_COD_EXTERNO   = getCadastroSetor("SET_COD_EXTERNO");
	public static final int SET_COD_DEPTO     = getCadastroSetor("SET_COD_DEPTO");
	public static final int SET_DES_RESUMIDA  = getCadastroSetor("SET_DES_RESUMIDA");
	public static final int SET_DES_COMPLETA  = getCadastroSetor("SET_DES_COMPLETA");
	public static final int SET_CPF_GESTOR    = getCadastroSetor("SET_CPF_GESTOR");
	public static final int SET_AREA          = getCadastroSetor("SET_AREA");
	*/
	public static int EST_NIV01        = -1;
	public static int EST_DES01        = -1;
	public static int EST_GER01        = -1;
	public static int EST_SETORCODEXT  = -1;
	public static int EST_FILCOD       = -1;
	public static int EST_DESRESUMIDA  = -1;
	public static int EST_DESCOMPLETA  = -1;
	public static int EST_CPFLIDER     = -1;
	public static int EST_AREASETOR    = -1;
	public static int EST_TIPRECEITA   = -1;

	public static int FUN_CPF         = -1;
	public static int FUN_NOME        = -1;
	public static int FUN_EMAIL       = -1;
	public static int FUN_CARGO       = -1;
	public static int FUN_COD_SETOR   = -1;
	public static int FUN_LOGIN       = -1;
	
	public static int SAL_ID_INT      = -1;
	public static int SAL_CONTA       = -1;
	public static int SAL_MES01       = -1;

	public static int CTB_CONTA       = -1;
	public static int CTB_DESCRICAO   = -1;
	public static int CTB_CONTARED    = -1;
	public static int CTB_NATUREZA    = -1;
	public static int CTB_GRUPO       = -1;
	public static int CTB_TIPOCONTA   = -1;
	
	public static int DRE_PLACON        = -1;
	public static int DRE_CENCUSTO      = -1;
	public static int DRE_CODCLAS       = -1;
	public static int DRE_NOMECLAS01    = -1;
	public static int DRE_GRUPO         = -1;
	public static int DRE_CAMPOBI_ADIC  = -1;
	public static int DRE_CAMPOBI_RV    = -1;
	public static int DRE_CAMPOBI_LB    = -1;
	public static int DRE_CAMPOBI_EBTDA = -1;

	public static int CTA_CATEGORIA   = -1;
	public static int CTA_MEDIDA      = -1;
	public static int CTA_SIGLA       = -1;
	public static int CTA_DESCRICAO   = -1;
	public static int CTA_TIPOCONTA   = -1;
	public static int CTA_PLACON      = -1;
	public static int CTA_PLACOND     = -1;
	public static int CTA_COLABORADOR = -1;
	public static int CTA_CENTROCUSTO = -1;
	public static int CTA_MES01       = -1;
	
	
	public static int MOV_FILIAL       = -1;
	public static int MOV_DTLANCTO     = -1;
	public static int MOV_CONTA        = -1;
	public static int MOV_CENTROCUSTO  = -1;
	public static int MOV_TIPOLANCTO   = -1;
	public static int MOV_VALOR        = -1;
	public static int MOV_DESCRHISTOR  = -1;
	public static int MOV_ID_EXTERNO   = -1;
	public static int MOV_HISTCOMP01   = -1;
	public static int MOV_HISTCOMP02   = -1;
	public static int MOV_HISTCOMP03   = -1;
	
	public static int PRJ_TPCTA       = -1;
	public static int PRJ_SIGLA_CTA   = -1;
	public static int PRJ_DESCR_CTA   = -1;
	public static int PRJ_CENCUS      = -1;
	public static int PRJ_MES01       = -1; 
	
	public static int ORC_ANO          = -1; 
	public static int ORC_CONTA        = -1;
	public static int ORC_CONTARED     = -1;
	public static int ORC_CENTROCUSTO  = -1; 
	public static int ORC_QUANT01      = -1;
	public static int ORC_MES01        = -1; 

	
	
	/*
	public static final int CTB_TIPODESPESA = getCadastroPlaConta("CTB_TIPODESPESA");
	public static final int CTB_CODCLAS         = getCadastroPlaConta("CTB_CODCLAS");
	public static final int CTB_NOMECLAS01      = getCadastroPlaConta("CTB_NOMECLAS01");
	public static final int CTB_NOMECLAS02      = getCadastroPlaConta("CTB_NOMECLAS02");
	public static final int CTB_NOMECLAS03      = getCadastroPlaConta("CTB_NOMECLAS03");
	public static final int CTB_CONTACLAS       = getCadastroPlaConta("CTB_CONTACLAS");
	public static final int CTB_CONTACLASRED    = getCadastroPlaConta("CTB_CONTACLASRED");
	public static final int CTB_SETOR           = getCadastroPlaConta("CTB_SETOR");
	public static final int CTB_SETORDESCR      = getCadastroPlaConta("CTB_SETORDESCR");
	public static final int CTB_ANO            = getCadastroPlaConta("CTB_ANO");
	public static final int CTB_CAMPOBI_ADIC   = getCadastroPlaConta("CTB_CAMPOBI_ADIC");
	public static final int CTB_CAMPOBI_RV     = getCadastroPlaConta("CTB_CAMPOBI_RV");
	public static final int CTB_CAMPOBI_LB     = getCadastroPlaConta("CTB_CAMPOBI_LB");
	public static final int CTB_CAMPOBI_EBTDA  = getCadastroPlaConta("CTB_CAMPOBI_EBTDA");
	public static final int CTB_FILIAL         = getCadastroPlaConta("CTB_FILIAL");
	
	public static final int IBI_CTACONTABIL   = getCadastroBI("IBI_CTACONTABIL");
	public static final int IBI_CENCUSTO      = getCadastroBI("IBI_CENCUSTO");
	public static final int IBI_CAMPOBI_ADIC  = getCadastroBI("IBI_CAMPOBI_ADIC");
	public static final int IBI_CAMPOBI_RV    = getCadastroBI("IBI_CAMPOBI_RV");
	public static final int IBI_CAMPOBI_LB    = getCadastroBI("IBI_CAMPOBI_LB");
	public static final int IBI_CAMPOBI_EBTDA = getCadastroBI("IBI_CAMPOBI_EBTDA");
	*/
	
	private static int getPosicaoCampo(String[]campos, String parametro) {
		int retorno = -1;
		parametro = parametro.trim();
		
		for (int i = 0; i < campos.length; i++) {
			if (campos[i].trim().equals(parametro)) {
				retorno = i;
				break;
			}			
		}
		
		if (retorno == -1)
			msgRetorno += "Não encontrou campo: " + parametro + ".<br/>";
		
		return retorno;
	}


	public static String carregarCamposFilial(String linha, Importador layout) {
		String[] campos = linha.split(layout.getSeparadorCampoStr());

		msgRetorno = "";
		//if (campos.length != 21)
		//	msgRetorno += "Número de colunas incorreto.<br/>";

		layout.setColunas(campos.length);
		
		FIL_CODIGO        = getPosicaoCampo(campos, "FIL_CODIGO");
		FIL_CNPJ          = getPosicaoCampo(campos, "FIL_CNPJ");
		FIL_NOME          = getPosicaoCampo(campos, "FIL_NOME"); 
		FIL_APELIDO       = getPosicaoCampo(campos, "FIL_APELIDO"); 
		/*FIL_TIP_END       = getPosicaoCampo(campos, "FIL_TIP_END"); 
		FIL_IDT_END       = getPosicaoCampo(campos, "FIL_IDT_END"); 
		FIL_ENDERECO      = getPosicaoCampo(campos, "FIL_ENDERECO"); 
		FIL_NUM_END       = getPosicaoCampo(campos, "FIL_NUM_END"); 
		FIL_COMP_END      = getPosicaoCampo(campos, "FIL_COMP_END"); 
		FIL_CEP           = getPosicaoCampo(campos, "FIL_CEP"); 
		FIL_NOM_BAIRRO    = getPosicaoCampo(campos, "FIL_NOM_BAIRRO"); 
		FIL_NOM_CIDADE    = getPosicaoCampo(campos, "FIL_NOM_CIDADE"); 
		FIL_COD_IBGE      = getPosicaoCampo(campos, "FIL_COD_IBGE"); 
		FIL_UF_END        = getPosicaoCampo(campos, "FIL_UF_END"); 
		FIL_IDT_PRINC     = getPosicaoCampo(campos, "FIL_IDT_PRINC"); 
		FIL_NOM_CONTATO   = getPosicaoCampo(campos, "FIL_NOM_CONTATO"); 
		FIL_EMAIL_CONTATO = getPosicaoCampo(campos, "FIL_EMAIL_CONTATO"); 
		FIL_TELEFONE      = getPosicaoCampo(campos, "FIL_TELEFONE"); 
		FIL_RAMAL         = getPosicaoCampo(campos, "FIL_RAMAL"); 
		FIL_CELULAR       = getPosicaoCampo(campos, "FIL_CELULAR"); 
		FIL_FAX           = getPosicaoCampo(campos, "FIL_FAX");*/
		
		if (!msgRetorno.isEmpty())
			msgRetorno += "Verificar layout " + layout.getNomeLayout() + ".";
		
		return msgRetorno;
	}
	
	public static String carregarCamposEstrutura(String linha, Importador layout) {
		String[] campos = linha.split(layout.getSeparadorCampoStr());

		msgRetorno = "";
		
		layout.setColunas(campos.length);

		EST_NIV01        = getPosicaoCampo(campos, "EST_NIV01");
		EST_DES01        = getPosicaoCampo(campos, "EST_DES01");
		EST_GER01        = getPosicaoCampo(campos, "EST_GER01");
		EST_SETORCODEXT  = getPosicaoCampo(campos, "EST_SETORCODEXT");
		EST_FILCOD       = getPosicaoCampo(campos, "EST_FILCOD");
		EST_DESRESUMIDA  = getPosicaoCampo(campos, "EST_DESRESUMIDA");
		EST_DESCOMPLETA  = getPosicaoCampo(campos, "EST_DESCOMPLETA");
		EST_CPFLIDER     = getPosicaoCampo(campos, "EST_CPFLIDER");
		EST_AREASETOR    = getPosicaoCampo(campos, "EST_AREASETOR");
		EST_TIPRECEITA   = getPosicaoCampo(campos, "EST_TIPRECEITA");
		
		// Procura o nível de departamento máximo no arquivo
		int maxNivel = 1;
		for (int i = 0; i < campos.length; i++) {
			if (campos[i].equals(String.format("EST_NIV%02d", maxNivel + 1))) { // Achou departamento filho
				maxNivel++;                                                     // Adiciona nivel do filho

				String campoFilho = String.format("EST_DES%02d", maxNivel);
				if (!campos[i+1].equals(campoFilho))                            // Se o proximo campo nao for a descrição do filho:
					msgRetorno += "Não encontrou campo: " + campoFilho + ".<br/>";
				
				campoFilho = String.format("EST_GER%02d", maxNivel);
				if (!campos[i+2].equals(campoFilho))                            // Se o proximo campo nao for o gerente do filho:
					msgRetorno += "Não encontrou campo: " + campoFilho + ".<br/>";
			}
		}
		layout.setMaxNivel(maxNivel);
		
		
		if (!msgRetorno.isEmpty())
			msgRetorno += "Verificar layout " + layout.getNomeLayout() + ".";
		
		return msgRetorno;
	}
	

	
	public static String carregarCamposFuncionario(String linha, Importador layout) {
		String[] campos = linha.split(layout.getSeparadorCampoStr());

		msgRetorno = "";
		if (campos.length != 7)
			msgRetorno += "Número de colunas incorreto.<br/>";

		layout.setColunas(campos.length);

		FUN_CPF         = getPosicaoCampo(campos, "FUN_CPF");
		FUN_NOME        = getPosicaoCampo(campos, "FUN_NOME");
		FUN_EMAIL       = getPosicaoCampo(campos, "FUN_EMAIL");
		FUN_CARGO       = getPosicaoCampo(campos, "FUN_CARGO");
		FUN_COD_SETOR   = getPosicaoCampo(campos, "FUN_COD_SETOR");
		FUN_LOGIN       = getPosicaoCampo(campos, "FUN_LOGIN");
		
		if (!msgRetorno.isEmpty())
			msgRetorno += "Verificar layout " + layout.getNomeLayout() + ".";
		
		return msgRetorno;
	}
	
	
	public static String carregarCamposPlanoContas(String linha, Importador layout) {
		String[] campos = linha.split(layout.getSeparadorCampoStr());

		msgRetorno = "";
		if (campos.length != 6)
			msgRetorno += "Número de colunas incorreto.<br/>";

		layout.setColunas(campos.length);

		CTB_CONTA     = getPosicaoCampo(campos, "CTB_CONTA");
		CTB_DESCRICAO = getPosicaoCampo(campos, "CTB_DESCRICAO");
		CTB_CONTARED  = getPosicaoCampo(campos, "CTB_CONTARED");
		CTB_NATUREZA  = getPosicaoCampo(campos, "CTB_NATUREZA");
		CTB_GRUPO     = getPosicaoCampo(campos, "CTB_GRUPO");
		CTB_TIPOCONTA = getPosicaoCampo(campos, "CTB_TIPOCONTA");
		
		if (!msgRetorno.isEmpty())
			msgRetorno += "Verificar layout " + layout.getNomeLayout() + ".";
		
		return msgRetorno;
	}
	
	public static String carregarCamposDRE(String linha, Importador layout) {
		String[] campos = linha.split(layout.getSeparadorCampoStr());

		msgRetorno = "";

		layout.setColunas(campos.length);

		DRE_PLACON        = getPosicaoCampo(campos, "DRE_PLACON");
		DRE_CENCUSTO      = getPosicaoCampo(campos, "DRE_CENCUSTO");
		DRE_CODCLAS       = getPosicaoCampo(campos, "DRE_CODCLAS");
		DRE_NOMECLAS01    = getPosicaoCampo(campos, "DRE_NOMECLAS01");
		DRE_GRUPO         = getPosicaoCampo(campos, "DRE_GRUPO");
		
		if (linha.indexOf("DRE_CAMPOBI_") > -1) {
			DRE_CAMPOBI_ADIC  = getPosicaoCampo(campos, "DRE_CAMPOBI_ADIC");
			DRE_CAMPOBI_RV    = getPosicaoCampo(campos, "DRE_CAMPOBI_RV");
			DRE_CAMPOBI_LB    = getPosicaoCampo(campos, "DRE_CAMPOBI_LB");
			DRE_CAMPOBI_EBTDA = getPosicaoCampo(campos, "DRE_CAMPOBI_EBTDA");
		}
		// Configurações iniciais do arquivo de Classificação do DRE
		int maxNivel = 1;
		for (int i = 0; i < campos.length; i++) {
			
			if (campos[i].equals(String.format("DRE_NOMECLAS%02d", maxNivel + 1))) // Achou nivel filho
				maxNivel++;                                                   // Adiciona nivel do filho
		}
		layout.setMaxNivel(maxNivel);

		
		if (!msgRetorno.isEmpty())
			msgRetorno += "Verificar layout " + layout.getNomeLayout() + ".";
		
		return msgRetorno;
	}
	
	public static String carregarCamposContaOperacionais(String linha, Importador layout) {
		String[] campos = linha.split(layout.getSeparadorCampoStr());

		msgRetorno = "";

		layout.setColunas(campos.length);

		CTA_CATEGORIA   = getPosicaoCampo(campos, "CTA_CATEGORIA");
		CTA_SIGLA       = getPosicaoCampo(campos, "CTA_SIGLA");
		CTA_DESCRICAO   = getPosicaoCampo(campos, "CTA_DESCRICAO");
		CTA_TIPOCONTA   = getPosicaoCampo(campos, "CTA_TIPOCONTA");
		CTA_PLACON      = getPosicaoCampo(campos, "CTA_PLACON");
		CTA_MEDIDA      = getPosicaoCampo(campos, "CTA_MEDIDA");
		
		
		// Se veio no arquivo as informações de parametro de vendedor: valida campos de parametros
		if (linha.indexOf("CTA_COLABORADOR") > -1 && linha.indexOf("CTA_CENTROCUSTO") > -1) {
			CTA_COLABORADOR  = getPosicaoCampo(campos, "CTA_COLABORADOR");
			CTA_CENTROCUSTO    = getPosicaoCampo(campos, "CTA_CENTROCUSTO");
			CTA_MES01    = getPosicaoCampo(campos, "CTA_MES01");
			
			int posicao = CTA_MES01;
			for (int i = 2; i <= 12; i++) {
				String mesAtual = String.format("CTA_MES%02d", i);
				if (!campos[++posicao].equals(mesAtual))                            // Se o proximo campo nao for o proximo mes
					msgRetorno += "Não encontrou campo: " + mesAtual + ".<br/>";
			}
		}
		
		
		if (!msgRetorno.isEmpty())
			msgRetorno += "Verificar layout " + layout.getNomeLayout() + ".";
		
		return msgRetorno;
	}
	

	public static String carregarCamposParamDeducao(String linha, Importador layout) {
		String[] campos = linha.split(layout.getSeparadorCampoStr());

		msgRetorno = "";

		layout.setColunas(campos.length);

		CTA_MEDIDA      = getPosicaoCampo(campos, "CTA_MEDIDA");
		CTA_TIPOCONTA   = getPosicaoCampo(campos, "CTA_TIPOCONTA");
		CTA_CATEGORIA   = getPosicaoCampo(campos, "CTA_CATEGORIA");
		CTA_SIGLA       = getPosicaoCampo(campos, "CTA_SIGLA");
		CTA_DESCRICAO   = getPosicaoCampo(campos, "CTA_DESCRICAO");
		CTA_PLACON      = getPosicaoCampo(campos, "CTA_PLACON");
		CTA_PLACOND     = getPosicaoCampo(campos, "CTA_PLACOND");
		CTA_MES01       = getPosicaoCampo(campos, "CTA_MES01");
		
		
		if (!msgRetorno.isEmpty())
			msgRetorno += "Verificar layout " + layout.getNomeLayout() + ".";
		
		return msgRetorno;
	}
	
	public static String carregarCamposMovimento(String linha, Importador layout) {
		String[] campos = linha.split(layout.getSeparadorCampoStr());

		msgRetorno = "";

		layout.setColunas(campos.length);

		MOV_FILIAL       = getPosicaoCampo(campos, "MOV_FILIAL");
		MOV_DTLANCTO     = getPosicaoCampo(campos, "MOV_DTLANCTO");
		MOV_CONTA        = getPosicaoCampo(campos, "MOV_CONTA");
		MOV_CENTROCUSTO  = getPosicaoCampo(campos, "MOV_CENTROCUSTO");
		MOV_TIPOLANCTO   = getPosicaoCampo(campos, "MOV_TIPOLANCTO");
		MOV_VALOR        = getPosicaoCampo(campos, "MOV_VALOR");
		MOV_DESCRHISTOR  = getPosicaoCampo(campos, "MOV_DESCRHISTOR");
		MOV_ID_EXTERNO   = getPosicaoCampo(campos, "MOV_ID_EXTERNO");
		MOV_HISTCOMP01   = getPosicaoCampo(campos, "MOV_HISTCOMP01");
		MOV_HISTCOMP02   = getPosicaoCampo(campos, "MOV_HISTCOMP02");
		MOV_HISTCOMP03   = getPosicaoCampo(campos, "MOV_HISTCOMP03");
		
		if (!msgRetorno.isEmpty())
			msgRetorno += "Verificar layout " + layout.getNomeLayout() + ".";
		
		return msgRetorno;
	}
	

	public static String carregarCamposOrcamentoVendas(String linha, Importador layout) {
		String[] campos = linha.split(layout.getSeparadorCampoStr());

		msgRetorno = "";

		layout.setColunas(campos.length);

		ORC_ANO         = getPosicaoCampo(campos, "ORC_ANO");
		ORC_CONTA       = getPosicaoCampo(campos, "ORC_CONTA");
		ORC_CONTARED    = getPosicaoCampo(campos, "ORC_CONTARED");
		ORC_CENTROCUSTO = getPosicaoCampo(campos, "ORC_CENTROCUSTO");
		ORC_QUANT01     = getPosicaoCampo(campos, "ORC_QUANT01");
		ORC_MES01       = getPosicaoCampo(campos, "ORC_MES01");
		
		
		if (!msgRetorno.isEmpty())
			msgRetorno += "Verificar layout " + layout.getNomeLayout() + ".";
		
		return msgRetorno;
	}
	
	public static String carregarCamposOrcamentoDespesa(String linha, Importador layout) {
		String[] campos = linha.split(layout.getSeparadorCampoStr());

		msgRetorno = "";

		layout.setColunas(campos.length);

		ORC_ANO         = getPosicaoCampo(campos, "ORC_ANO");
		ORC_CONTA       = getPosicaoCampo(campos, "ORC_CONTA");
		ORC_CONTARED    = getPosicaoCampo(campos, "ORC_CONTARED");
		ORC_CENTROCUSTO = getPosicaoCampo(campos, "ORC_CENTROCUSTO");
		ORC_MES01       = getPosicaoCampo(campos, "ORC_MES01");
		
		
		if (!msgRetorno.isEmpty())
			msgRetorno += "Verificar layout " + layout.getNomeLayout() + ".";
		
		return msgRetorno;
	}
	
	
	public static String carregarCamposSalario(String linha, Importador layout) {
		String[] campos = linha.split(layout.getSeparadorCampoStr());

		msgRetorno = "";

		layout.setColunas(campos.length);

		FUN_CPF         = getPosicaoCampo(campos, "FUN_CPF");
		FUN_NOME        = getPosicaoCampo(campos, "FUN_NOME");
		FUN_EMAIL       = getPosicaoCampo(campos, "FUN_EMAIL");
		FUN_CARGO       = getPosicaoCampo(campos, "FUN_CARGO");
		FUN_COD_SETOR   = getPosicaoCampo(campos, "FUN_COD_SETOR");
		FUN_LOGIN       = getPosicaoCampo(campos, "FUN_LOGIN");
		
		SAL_ID_INT      = getPosicaoCampo(campos, "SAL_ID_INT");
		SAL_CONTA       = getPosicaoCampo(campos, "SAL_CONTA");
		SAL_MES01       = getPosicaoCampo(campos, "SAL_MES01");
				
		if (!msgRetorno.isEmpty())
			msgRetorno += "Verificar layout " + layout.getNomeLayout() + ".";
		
		return msgRetorno;
	}
	
	
	
	public static String[] verificaNumeroCampos(Importador layout, String linha) throws RegraNegocioException {
		if (temCaractereEstranho(linha)) {
			throw new RegraNegocioException("Linha com caracteres diferente do charset definido. " + linha);
		}
		
		String[] campos = linha.split(layout.getSeparadorCampoStr());
		
		int difCampo = (layout.getColunas() - campos.length) + 1;
		for (int i = 0; i <= difCampo; i++) {
			linha += " " + layout.getSeparadorCampo();
		}
		return linha.split(layout.getSeparadorCampoStr());
	}
	
	
	private static boolean temCaractereEstranho(String linha) {
		
		for (int i = 0; i < linha.length(); i++) {
			if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz 0123456789,.;|/\\<>:?[]{}-=_+!@#$%&*()ãâàáäêèéëîìíïõôòóöûúùüÃÂÀÁÄÊÈÉËÎÌÍÏÕÔÒÓÖÛÙÚÜçÇñÑªº".indexOf(linha.charAt(i)) == -1) {
				return true;
			}
			
		}
		
		return false;
	}

}
