package com.ctbli9.valorplan.negocio.importar;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import com.ctbli9.valorplan.DAO.CategoriaReceitaServiceDAO;
import com.ctbli9.valorplan.DAO.CentroCustoServiceDAO;
import com.ctbli9.valorplan.DAO.ClassificacaoCtbServiceDAO;
import com.ctbli9.valorplan.DAO.ContaContabilServiceDAO;
import com.ctbli9.valorplan.DAO.EquipeServiceDAO;
import com.ctbli9.valorplan.DAO.FilialServiceDAO;
import com.ctbli9.valorplan.DAO.MovContabImportarServiceDAO;
import com.ctbli9.valorplan.DAO.ParamDeducaoServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.ReceitaServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.DAO.orc.OrcDespesaGeralServiceDAO;
import com.ctbli9.valorplan.DAO.orc.OrcReceitaServiceDAO;
import com.ctbli9.valorplan.enumeradores.MedidaOrcamento;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.MovtoContab;
import com.ctbli9.valorplan.modelo.importa.LayoutArquivo;
import com.ctbli9.valorplan.modelo.orc.OrcamentoDespesa;
import com.ctbli9.valorplan.modelo.orc.ValorTotalMes;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaAcum;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaMes;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;
import com.ctbli9.valorplan.modelo.receita.DeducaoReceita;
import com.ctbli9.valorplan.modelo.receita.Receita;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.enumeradores.TipoConta;
import ctbli9.modelo.Filial;
import ctbli9.modelo.Plano;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.ArquivoTexto;
import ctbli9.recursos.DataUtil;
import ctbli9.recursos.LibUtil;

public class ImportarOrcamento {
	ImportadorService servico = null;
	private ConexaoDB con;
	
	private ContaContabil contaContabil;
	private CentroCusto cenCusto;
	private Filial filial;
	
	private Receita receita = null;

	public ImportarOrcamento(ConexaoDB con, ImportadorService servico) {
		this.con = con;
		this.servico = servico;
	}
	
	public String importarArquivo(String arquivo) throws Exception {
		
		
		String arqErro = "";

		ArquivoTexto arq = new ArquivoTexto(arquivo, servico.getFormatoDados().getCharCode());
		String linha;
		int linhaAtual = 0;
		int contador = 0;

		try {
			
			servico.limpaListaErro();

			arq.abreLeitura();
			linha = arq.lerLinha(); // ID do Arquivo
			linha = arq.lerLinha(); // Nomes das colunas

			while ((linha = arq.lerLinha()) != null) {
				if (!linha.isEmpty()) {
					linha = linha.replaceAll(";;", "; ;").replaceAll("º", "");

					if (servico.getFormatoDados().getNomeLayout().startsWith("MOVIMENTO_REALIZADO"))
						linhaAtual = importarMovContabil(linha, linhaAtual);

					else if (servico.getFormatoDados().getNomeLayout().startsWith("PARAM_DEDUCAO"))
						linhaAtual = importarParametrosDeducao(linha, linhaAtual);
						
					else if (servico.getFormatoDados().getNomeLayout().startsWith("ORCAMENTO_ANO"))
						linhaAtual = importarOrcamentoAnoAnterior(linha, linhaAtual);
						
					if (++contador > 5000) {
						ConexaoDB.gravarTransacao(con);
						contador = 0;
					}
				}
			} // while
			

			if (contador > 0)
				ConexaoDB.gravarTransacao(con);

			if (servico.temErro()) {
				arqErro = servico.gerarArquivo();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ConexaoDB.desfazerTransacao(con);

			throw new Exception("ERRO ao processar a linha : " + linhaAtual + "." + e.getMessage(), e);

		} catch (Exception e) {
			e.printStackTrace();
			ConexaoDB.desfazerTransacao(con);

			throw new Exception("ERRO ao processar a linha : " + linhaAtual + "." + e.getMessage(), e);

		} finally {
			try {
				arq.fechaLeitura();
			} catch (Exception e) {
			}
		}

		return arqErro;
	}
	

	/*
	 * Usado para importar os parâmetros de dedução da COLORADO EQUIPAMENTOS
	 */
	private int importarParametrosDeducao(String linha, int linhaAtual) throws Exception {
		linha = LibUtil.substituiAcentuado(linha);
		String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);

		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);
		
		try {
				
			if (campos[LayoutArquivo.CTA_TIPOCONTA].trim().toUpperCase().equals("RECEITA")) { // Receita
				ContaContabil ctaContabil = ContaContabilServiceDAO.pesquisarContaReduzida(con.getConexao(), 
						campos[LayoutArquivo.CTA_PLACON].trim());
						
				if (ctaContabil == null) {
					servico.addErro(linhaAtual + " ==> " + linha + " ==> \nConta não cadastrada");
					return linhaAtual;
				}
				
				String sigla = campos[LayoutArquivo.CTA_SIGLA].trim().toUpperCase();
				if (sigla.isEmpty())
					sigla = servico.gerarSigla(campos[LayoutArquivo.CTA_DESCRICAO].trim());

				CategoriaReceita categ = CategoriaReceitaServiceDAO
						.pesquisarCategoriaPorDescricao(con.getConexao(), campos[LayoutArquivo.CTA_CATEGORIA].trim());

				if (categ == null) {
					categ = new CategoriaReceita();
					categ.setCdCategoria(0);
					categ.setDsCategoria(campos[LayoutArquivo.CTA_CATEGORIA].trim());
					categ.setMedida(MedidaOrcamento.valueOf(campos[LayoutArquivo.CTA_MEDIDA].trim()));
					CategoriaReceitaServiceDAO.incluirCategoriaReceita(con.getConexao(), categ);
				}

				receita = null;
				receita = ReceitaServiceDAO.pesquisarPorDescricao(con.getConexao(),	
						campos[LayoutArquivo.CTA_DESCRICAO].trim());

				if (receita == null) {
					receita = new Receita();
					receita.setCdReceita(0);
					receita.setSgReceita(sigla);
					receita.setContaReceita(ctaContabil);
					receita.setCategoria(categ);
					receita.setDsReceita(campos[LayoutArquivo.CTA_DESCRICAO].trim());
					receita.setIdAtivo(true);

					receita.setDeducaoSobreVenda(null);

					ReceitaServiceDAO.incluirReceita(con.getConexao(), receita);
				
				} else {
					
					receita.setIdAtivo(true);
					ReceitaServiceDAO.alterarReceita(con.getConexao(), receita);
				}
				
			} else { // Conta de dedução
				
				if (!campos[LayoutArquivo.CTA_PLACON].trim().isEmpty()) {
					ContaContabil ctaContabil = ContaContabilServiceDAO.pesquisarContaReduzida(con.getConexao(), 
							campos[LayoutArquivo.CTA_PLACON].trim());
							
					if (ctaContabil == null) {
						servico.addErro(linhaAtual + " ==> " + linha + " ==> \nConta não cadastrada");
						return linhaAtual;
					}	
					
					DeducaoReceita deducao = new DeducaoReceita();
					deducao.setCdReceita(receita.getCdReceita());
					deducao.setConta(ctaContabil);
					
					ReceitaServiceDAO.incluirDeducao(con.getConexao(), deducao);
						
				
					String percentStr = campos[LayoutArquivo.CTA_MES01];
					
					if (percentStr.endsWith("%")) {
						percentStr = percentStr.replace(",", ".").replace("%", "");
						BigDecimal percentual = BigDecimal.ZERO;
						if (percentStr != null && !percentStr.trim().isEmpty())
							percentual = new BigDecimal(percentStr);
						
						if (percentual.compareTo(BigDecimal.ZERO) > 0) {
							List<Recurso> listaRecurso = EquipeServiceDAO.listaRecursoPorCategoriaReceita(con.getConexao(),
									receita.getCategoria().getCdCategoria());
							
							if (listaRecurso.size() == 0) {
								servico.addErro(linhaAtual + " ==> " + linha + " ==> \nReceita sem recurso no Setor");
							
								return linhaAtual;
							}
							
							Plano plano = PlanoServiceDAO.getPlanoSelecionado();
							
							PreparedStatement pstmtSel = ParamDeducaoServiceDAO.inicializaSel(con.getConexao()); 
							PreparedStatement pstmtIns = ParamDeducaoServiceDAO.inicializaIns(con.getConexao()); 
							PreparedStatement pstmtUpd = ParamDeducaoServiceDAO.inicializaUpd(con.getConexao()); 
							PreparedStatement pstmtDel = ParamDeducaoServiceDAO.inicializaDel(con.getConexao()); 
							
							for (Recurso recurso : listaRecurso) {
								BigDecimal percAux = percentual;
								
								for (int j = 1; j <= 12; j++) {
									ParamDeducaoServiceDAO.atualizaParametros(pstmtSel, pstmtIns, pstmtUpd, pstmtDel, 
											plano.getCdPlano(),
											Integer.parseInt(String.format("%04d%02d", plano.getNrAno(), j)),
											receita.getCdReceita(),
											deducao.getConta().getIdConta(),
											recurso.getCdRecurso(),
											percAux
											);
								}
							}
							
							pstmtSel.close();
							pstmtIns.close();
							pstmtUpd.close();
							pstmtDel.close();
							
							pstmtSel = null;
							pstmtIns = null;
							pstmtUpd = null;
							pstmtDel = null;
						}
					
					}//percentStr.endsWith("%")
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			servico.addErro(linhaAtual + " ==> " + linha + " ==> \n" + e.getMessage());
		}
		return linhaAtual;
	}
	
	private int importarOrcamentoAnoAnterior(String linha, int linhaAtual) {

		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);
		
		try {
			String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);

			Plano plano = null;
			
			List<Plano> listaPlanos = PlanoServiceDAO.listar(con.getConexao());
			for (Plano plan : listaPlanos) {
				if (plan.getNrAno() == Integer.parseInt(campos[LayoutArquivo.ORC_ANO]) /*&&
						plan.getStatus().equals(StatusPlano._0)*/) {
					plano = plan;
					break;
				}
			}
			
			ContaContabil conta = ContaContabilServiceDAO.pesquisarConta(con.getConexao(), campos[LayoutArquivo.ORC_CONTA].trim());
			
			CentroCusto cencus = CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(), 
					campos[LayoutArquivo.ORC_CENTROCUSTO].trim());
			
			if (cencus != null && conta != null) {
				
				if (conta.getTipo().equals(TipoConta.RE)) {
					receita = ReceitaServiceDAO.pesquisarPorConta(con.getConexao(),
							conta.getCdConta(), conta.getDsConta());
					
					if (receita != null) {
						
						PreparedStatement pstmtSel = OrcReceitaServiceDAO.inicializaSel(con.getConexao());
						PreparedStatement pstmtIns = OrcReceitaServiceDAO.inicializaIns(con.getConexao());
						PreparedStatement pstmtUpd = OrcReceitaServiceDAO.inicializaUpd(con.getConexao());
						PreparedStatement pstmtDel = OrcReceitaServiceDAO.inicializaDel(con.getConexao());
						
						List<Recurso> equipe = EquipeServiceDAO.listarEquipeDoSetor(con.getConexao(), cencus);
						
						MesAnoOrcamento mesRef = new MesAnoOrcamento();
						mesRef.setAno(plano.getNrAno());
						for (int i = 0; i < 12; i++) {
							
							int mes = LayoutArquivo.ORC_MES01 + i;
							
							mesRef.setMes(i + 1);
							
							String valorStr = campos[mes];
							if (valorStr.trim().isEmpty())
								continue;
							
							BigDecimal valor = new BigDecimal(valorStr.trim().replace(",", ".").replace("-", ""));
								
							OrcamentoReceitaAcum orc = new OrcamentoReceitaAcum();
							orc.setReceita(receita);
							
							OrcamentoReceitaMes recMes = new OrcamentoReceitaMes();
							recMes.setMesRef(mesRef);
							recMes.setQuantidade(1);
							recMes.setValorUnitario(valor);
							
							OrcReceitaServiceDAO.gravarOrcamento(equipe.get(0), orc, recMes, plano,
									pstmtSel, pstmtIns, pstmtUpd, pstmtDel);
						}
						
						pstmtSel.close();
						pstmtIns.close();
						pstmtUpd.close();
						pstmtDel.close();
						
						pstmtSel = null;
						pstmtIns = null;
						pstmtUpd = null;
						pstmtDel = null;
					} 
	
				} else if (conta.getTipo().equals(TipoConta.DO)) {
					OrcamentoDespesa despesa = new OrcamentoDespesa();
					despesa.setConta(conta);
					
					List<ValorTotalMes> valores = new ArrayList<ValorTotalMes>();
					
					for (int i = 0; i < 12; i++) {
						
						int mes = LayoutArquivo.ORC_MES01 + i;
						
						BigDecimal valorDesp = BigDecimal.ZERO;
						String valorStr = campos[mes];
						if (!valorStr.trim().isEmpty())
							valorDesp = new BigDecimal(valorStr.trim().replace(",", ".").replace("-", ""));
						
						ValorTotalMes valor = new ValorTotalMes();
						valor.setMesRef(new MesAnoOrcamento((i + 1), plano.getNrAno()));
						valor.setVrOrcado(valorDesp);
						
						valores.add(valor);
					}
					
					OrcDespesaGeralServiceDAO.gravarOrcDespesaGeral(con.getConexao(), plano,
							cencus, despesa, valores);
				}
				
			} else {
				
				if (cencus == null)
					servico.addErro("Setor não encontrado: " + campos[LayoutArquivo.ORC_CENTROCUSTO].trim());
				if (conta == null) 
					servico.addErro("Conta de receita não encontrada: " + campos[LayoutArquivo.ORC_CONTA].trim());
				
			}
				
				
			
			
			
			
			
			


		} catch (Exception e) {
			e.printStackTrace();
			servico.addErro(linhaAtual + " ==> " + linha + " ==> \n" + e.getMessage());
		}
		return linhaAtual;
	}
	
	
	/*
	 * 
	 */
	private int importarMovContabil(String linha, int linhaAtual) throws Exception {
		String[] campos = linha.split(servico.getFormatoDados().getSeparadorCampoStr()); // ("\\|");
		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);

		try {
			MovtoContab mov = new MovtoContab();

			if (campos[LayoutArquivo.MOV_CENTROCUSTO].trim().isEmpty()) // Apenas movimentações com centro de custo
				return linhaAtual;

			if (!MovContabImportarServiceDAO.pesquisarIdExterno(con.getConexao(), campos[LayoutArquivo.MOV_ID_EXTERNO].trim())) {
				mov.setIdLegado(campos[LayoutArquivo.MOV_ID_EXTERNO]);

				if (contaContabil == null || !contaContabil.getCdConta().trim().equals(campos[LayoutArquivo.MOV_CONTA].trim()))
					contaContabil = ContaContabilServiceDAO.pesquisarConta(con.getConexao(), 
							campos[LayoutArquivo.MOV_CONTA].trim());

				String codExterno = campos[LayoutArquivo.MOV_CENTROCUSTO].trim().replace(".", "");
				codExterno = Integer.toString(Integer.parseInt(codExterno));
				
				if (cenCusto == null || !cenCusto.getCodExterno().trim().equals(codExterno.trim()))
					cenCusto = CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(), codExterno);

				
				if (filial == null || !filial.getCodExterno().trim().equals(campos[LayoutArquivo.MOV_FILIAL].trim()))
					filial = FilialServiceDAO.pesquisarFilial(con.getConexao(), campos[LayoutArquivo.MOV_FILIAL].trim());

				if (contaContabil != null && cenCusto != null && filial != null && filial.getCdFilial() > 0) {

					mov.setConta(contaContabil);
					mov.setCencus(cenCusto);
					mov.setFilial(filial);
					mov.setDtMovto(DataUtil.stringToData(campos[LayoutArquivo.MOV_DTLANCTO].trim(),
							this.servico.getFormatoDados().getFormatoData()));
					mov.setSqMovto(0);

					mov.setIdNatMovto(campos[LayoutArquivo.MOV_TIPOLANCTO].trim());
					if (servico.getFormatoDados().getCharDecimal() == ',')
						mov.setVlMovto(Double.parseDouble(campos[LayoutArquivo.MOV_VALOR].trim().replace(",", ".")));

					String histor = campos[LayoutArquivo.MOV_DESCRHISTOR].trim() + " "
							+ campos[LayoutArquivo.MOV_HISTCOMP01].trim() + " "
							+ campos[LayoutArquivo.MOV_HISTCOMP02].trim() + " "
							+ campos[LayoutArquivo.MOV_HISTCOMP03].trim();

					mov.setTxHistorico(histor);

					mov.setDsDocumento(null);

					if (!ClassificacaoCtbServiceDAO.existeClasseConta(con.getConexao(), contaContabil, cenCusto, null))
						servico.addErro("RELACIONAMENTO NAO ENCONTRADO PARA A COMBINACAO CONTA CONTABIL: "
								+ contaContabil.getCdConta() + " -- CENTRO DE CUSTO: "
								+ cenCusto.getCodExterno());

					MovContabImportarServiceDAO.incluir(con.getConexao(), mov);

				} else { // Nao Existe conta ou centro de custo ou filial
					if (contaContabil == null)
						servico.addErro("Conta contabil nao encontrada: " + campos[LayoutArquivo.MOV_CONTA]);

					if (cenCusto == null)
						servico.addErro("Centro de Custo nao encontrado: " + campos[LayoutArquivo.MOV_CENTROCUSTO]);

					if (filial == null || filial.getCdFilial() == 0)
						servico.addErro("Filal nao encontrada: " + campos[LayoutArquivo.MOV_FILIAL]);

				}

			} // pesquisarIdExterno

		} catch (Exception e) {
			e.printStackTrace();
			servico.addErro(linhaAtual + " ==> " + linha + "\nErro: " + e.getMessage());
		}
		return linhaAtual;
	}
	
	
/* TEMPLATE
	private int importarOrcamentoAnoAnterior(String linha, int linhaAtual) {
		String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);

		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);
		
		try {

		} catch (Exception e) {
			e.printStackTrace();
			servico.addErro(linhaAtual + " ==> " + linha + " ==> \n" + e.getMessage());
		}
		return linhaAtual;
	}
		
	*/
}
