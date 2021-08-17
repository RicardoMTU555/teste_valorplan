package com.ctbli9.valorplan.negocio.importar;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import com.ctbli9.valorplan.DAO.CargoServiceDAO;
import com.ctbli9.valorplan.DAO.CategoriaReceitaServiceDAO;
import com.ctbli9.valorplan.DAO.CentroCustoServiceDAO;
import com.ctbli9.valorplan.DAO.ClassificacaoCtbServiceDAO;
import com.ctbli9.valorplan.DAO.ContaContabilServiceDAO;
import com.ctbli9.valorplan.DAO.EquipeServiceDAO;
import com.ctbli9.valorplan.DAO.FilialServiceDAO;
import com.ctbli9.valorplan.DAO.FuncionarioServiceDAO;
import com.ctbli9.valorplan.DAO.MovContabImportarServiceDAO;
import com.ctbli9.valorplan.DAO.ParamDeducaoServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.ReceitaServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.DAO.orc.OrcDespesaGeralServiceDAO;
import com.ctbli9.valorplan.DAO.orc.OrcDespesaRHServiceDAO;
import com.ctbli9.valorplan.DAO.orc.OrcReceitaServiceDAO;
import com.ctbli9.valorplan.enumeradores.MedidaOrcamento;
import com.ctbli9.valorplan.enumeradores.TipoRecurso;
import com.ctbli9.valorplan.modelo.Cargo;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.modelo.MovtoContab;
import com.ctbli9.valorplan.modelo.importa.LayoutArquivo;
import com.ctbli9.valorplan.modelo.orc.OrcamentoDespesa;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaAcum;
import com.ctbli9.valorplan.modelo.orc.OrcamentoReceitaMes;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.modelo.orc.ValorTotalMes;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;
import com.ctbli9.valorplan.modelo.receita.DeducaoReceita;
import com.ctbli9.valorplan.modelo.receita.Receita;
import com.ctbli9.valorplan.negocio.CargoService;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.enumeradores.TipoCargo;
import ctbli9.enumeradores.TipoConta;
import ctbli9.modelo.Filial;
import ctbli9.modelo.Plano;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.ArquivoTexto;
import ctbli9.recursos.DataUtil;

public class ImportarOrcamentoAgr {
	private ImportarCadastro importarCadastro = null;
	private ImportadorService servico = null;
	private ConexaoDB con;
	
	private ContaContabil contaContabil;
	private CentroCusto cenCusto;
	private Filial filial;
	
	private Receita receita = null;
	private OrcamentoReceitaMes[] orcReceita = new OrcamentoReceitaMes[12];
	String[] setoresParametroDeducao;

	public ImportarOrcamentoAgr(ConexaoDB con, ImportadorService servico) {
		this.con = con;
		this.servico = servico;
		this.importarCadastro = new ImportarCadastro(con, servico);
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
					// linha = LibUtil.substituiAcentuado(linha);
					linha = linha.replaceAll(";;", "; ;").replaceAll("º", "");
					
					if (servico.getFormatoDados().getNomeLayout().startsWith("MOVIMENTO_REALIZADO"))
						linhaAtual = importarMovContabil(linha, linhaAtual);

					else if (servico.getFormatoDados().getNomeLayout().startsWith("PARAM_DEDUCAO"))
						linhaAtual = importarParametrosDeducao(linha, linhaAtual);
					
					else if (servico.getFormatoDados().getNomeLayout().startsWith("SALARIO_FUNCIONARIO"))
						linhaAtual = importarSalarioFuncionario(linha, linhaAtual);
					
					else if (servico.getFormatoDados().getNomeLayout().startsWith("ORCAMENTO_VENDAS"))
						linhaAtual = importarOrcamentoVendas(linha, linhaAtual);
						
					else if (servico.getFormatoDados().getNomeLayout().startsWith("ORCAMENTO_DESPESAS"))
						linhaAtual = importarOrcamentoDespesa(linha, linhaAtual);
						
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
	 * Usado para importar os parâmetros de dedução da AGRINORTE
	 */
	private int importarParametrosDeducao(String linha, int linhaAtual) throws Exception {
		String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);

		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);
		
		if (campos[LayoutArquivo.CTA_SIGLA].trim().equals("COD_SETORES")) {
			this.setoresParametroDeducao = campos[LayoutArquivo.CTA_SIGLA + 1].split(":");
			return linhaAtual;
		}
		
		
		try {
			ContaContabil ctaContabil = ContaContabilServiceDAO.pesquisarConta(con.getConexao(), 
					campos[LayoutArquivo.CTA_PLACON].trim());
					
			// Receita
			if (ctaContabil != null) {

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
				receita = ReceitaServiceDAO.pesquisarPorSigla(con.getConexao(),	sigla);

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


			} 
			
				
			ctaContabil = ContaContabilServiceDAO.pesquisarConta(con.getConexao(), 
					campos[LayoutArquivo.CTA_PLACOND].trim());
			
			if (ctaContabil != null && receita != null) {
				
				DeducaoReceita deducao = ReceitaServiceDAO.pesquisarDeducao(con.getConexao(), receita, 
						ctaContabil);
				if (deducao == null) {
					deducao = new DeducaoReceita();
					deducao.setCdReceita(receita.getCdReceita());
					deducao.setConta(ctaContabil);
					
					ReceitaServiceDAO.incluirDeducao(con.getConexao(), deducao);
				}
				String percentStr = campos[LayoutArquivo.CTA_MES01].replace(",", ".").replace("%", "");
				BigDecimal percentual = BigDecimal.ZERO;
				if (percentStr != null && !percentStr.trim().isEmpty())
					percentual = new BigDecimal(percentStr);
				
				if (percentual.compareTo(BigDecimal.ZERO) > 0) {
				
					Plano plano = PlanoServiceDAO.getPlanoSelecionado();
					
					PreparedStatement pstmtSel = ParamDeducaoServiceDAO.inicializaSel(con.getConexao()); 
					PreparedStatement pstmtIns = ParamDeducaoServiceDAO.inicializaIns(con.getConexao()); 
					PreparedStatement pstmtUpd = ParamDeducaoServiceDAO.inicializaUpd(con.getConexao()); 
					PreparedStatement pstmtDel = ParamDeducaoServiceDAO.inicializaDel(con.getConexao()); 
				
					for (int set = 0; set < this.setoresParametroDeducao.length; set++) {
					
						CentroCusto cencus = CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(), 
								this.setoresParametroDeducao[set].trim());

						List<Recurso> listaRecurso = EquipeServiceDAO.listarEquipeDoSetor(con.getConexao(), cencus);
						
						for (Recurso recurso : listaRecurso) {
							BigDecimal percAux = percentual;
						
							if (ctaContabil.getDsConta().startsWith("(-) ISS")) {
								if (recurso.getSetor().getFilial().getCdFilial() == 2)
									percAux = BigDecimal.valueOf(4.00);
							}
							
						
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
			}
		
		

		} catch (Exception e) {
			e.printStackTrace();
			servico.addErro(linhaAtual + " ==> " + linha + " ==> \n" + e.getMessage());
		}
		return linhaAtual;
	}
	
	
	private int importarSalarioFuncionario(String linha, int linhaAtual) throws Exception {
		
		String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);
		
		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);

		if (campos[LayoutArquivo.SAL_CONTA].charAt(0) == '1' ||
				campos[LayoutArquivo.SAL_CONTA].charAt(0) == '2' ||
				campos[LayoutArquivo.SAL_CONTA].charAt(0) == '3')
			return linhaAtual;
		
		try {
			Recurso recurso = null;
			
			if (campos[LayoutArquivo.FUN_CPF].trim().isEmpty()) {
				if (!campos[LayoutArquivo.SAL_ID_INT].isEmpty())
					recurso = EquipeServiceDAO.getRecurso(con.getConexao(), Long.parseLong(campos[LayoutArquivo.SAL_ID_INT]));
				else {
					recurso = obterOuCriarRecurso(con.getConexao(), campos);
				}

			} else {
				Funcionario funcionario = FuncionarioServiceDAO.pesquisarPorCPF(con.getConexao(), 
						new BigDecimal(campos[LayoutArquivo.FUN_CPF].trim()));
		
				if (funcionario == null) {
					importarCadastro.importarFuncionario(linha, linhaAtual);
				
					funcionario = FuncionarioServiceDAO.pesquisarPorCPF(con.getConexao(), 
							new BigDecimal(campos[LayoutArquivo.FUN_CPF].trim()));
				}
				
				if (funcionario == null) {
					servico.addErro("CPF do funcionário não cadastrado: " + campos[LayoutArquivo.FUN_CPF].trim());
					return linhaAtual;
				}
				
				recurso = EquipeServiceDAO.getRecursoVinculado(con.getConexao(), funcionario.getCdFuncionario());
				
				if (recurso == null) {
					recurso = new Recurso(new MesAnoOrcamento());
					recurso.setCdRecurso(0);
					recurso.setNmRecurso("NOVO " + funcionario.getCargo().getDsCargo().toUpperCase());
					recurso.setVinculo(funcionario);
					recurso.setCargo(funcionario.getCargo());
					recurso.setSetor(funcionario.getCenCusto());
					recurso.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
					recurso.setInicioVinculo(new MesAnoOrcamento().getAnoMes());
					recurso.setFimVinculo(0);
					
					if (funcionario.getCargo().getTpCargo().equals(TipoCargo.TC) ||
						funcionario.getCargo().getTpCargo().equals(TipoCargo.VE))
						recurso.setTipo(TipoRecurso.P);
					else
						recurso.setTipo(TipoRecurso.O);
					
					EquipeServiceDAO.incluir(con.getConexao(), recurso);
	
				}
			} 
			
			if (recurso != null) {
				OrcamentoDespesa despesa = new OrcamentoDespesa();
				despesa.setConta(ContaContabilServiceDAO.pesquisarContaReduzida(con.getConexao(), campos[LayoutArquivo.SAL_CONTA]));
				
				if (despesa.getConta() == null) {
					servico.addErro("Conta contábil não cadastrada: " + campos[LayoutArquivo.SAL_CONTA]);
					return linhaAtual;
				}
				
				// Monta valores da despesa mês a mês				
				List<ValorTotalMes> valores = OrcDespesaRHServiceDAO.listarOrcDespesaRH(con.getConexao(), recurso, despesa.getConta());
				for (int i = 0; i < 12; i++) {
					MesAnoOrcamento mesRef = new MesAnoOrcamento();
					int mes = LayoutArquivo.SAL_MES01 + i;
				
					mesRef.setMes(i + 1);
					
					String valorStr = campos[mes].trim().replace(".", "").replace(",", ".").replace("-", "").replace("R$", "");
					if (valorStr.trim().isEmpty())
						continue;
					
					BigDecimal valor = new BigDecimal(valorStr);
	
					for (ValorTotalMes orcMes : valores) {
						if (orcMes.getMesRef().getAnoMes() == mesRef.getAnoMes()) {
							orcMes.setVrOrcado(orcMes.getVrOrcado().add(valor));
							break;
						}
					}
				}
				
				OrcDespesaRHServiceDAO.gravarOrcDespesaRH(con.getConexao(), recurso, despesa, valores);
			}
	
		} catch (Exception e) {
			servico.addErro(linhaAtual + " ==> " + linha + " ==> \nErro: " + e.getMessage());
			e.printStackTrace();
		}
		return linhaAtual;
	}
	
	private Recurso obterOuCriarRecurso(Connection connection, String[] campos) throws Exception {
		
		Cargo cargo = CargoServiceDAO.pesquisarDescricao(con.getConexao(), campos[LayoutArquivo.FUN_CARGO].trim());
		if (cargo == null) {
			cargo = new Cargo();

			cargo.setCdCargo(0);
			cargo.setDsCargo(campos[LayoutArquivo.FUN_CARGO].trim());

			cargo.setTpCargo(new CargoService(con).defineTipoCargo(cargo.getDsCargo()));

			CargoServiceDAO.incluir(con.getConexao(), cargo);
		}
		
		CentroCusto cenCusto = CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(), 
				campos[LayoutArquivo.FUN_COD_SETOR].trim());
		
		String novoRecurso = campos[LayoutArquivo.FUN_NOME].trim(); //"VAGA " + cargo.getDsCargo().toUpperCase() + " " + cenCusto.getCecDsResumida().toUpperCase();
		Recurso recurso = EquipeServiceDAO.pesquisarPorDescricao(con, novoRecurso, cargo, cenCusto);
		
		if (recurso == null) {
			recurso = new Recurso(new MesAnoOrcamento());
			recurso.setCdRecurso(0);
			recurso.setNmRecurso(novoRecurso);
			recurso.setVinculo(null);
			recurso.setCargo(cargo);
			recurso.setSetor(cenCusto);
			recurso.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
			recurso.setInicioVinculo(new MesAnoOrcamento().getAnoMes());
			recurso.setFimVinculo(0);
			
			recurso.setTipo(TipoRecurso.O);
			
			EquipeServiceDAO.incluir(con.getConexao(), recurso);

		}
		
		return recurso;
	}
	
	private int importarOrcamentoDespesa(String linha, int linhaAtual) {

		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);
		
		try {
			String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);
			Plano plano = null;
			
			if (Integer.parseInt(campos[LayoutArquivo.ORC_ANO]) == PlanoServiceDAO.getPlanoSelecionado().getNrAno()) {
				plano = PlanoServiceDAO.getPlanoSelecionado();
				
			} else {
			
				List<Plano> listaPlanos = PlanoServiceDAO.listar(con.getConexao());
				for (Plano plan : listaPlanos) {
					if (plan.getNrAno() == Integer.parseInt(campos[LayoutArquivo.ORC_ANO]) /*&&
							plan.getStatus().equals(StatusPlano._0)*/) {
						plano = plan;
						break;
					}
				}
			}
			
			ContaContabil conta = null;
			if (campos[LayoutArquivo.ORC_CONTA].trim().isEmpty())
				conta = ContaContabilServiceDAO.pesquisarContaReduzida(con.getConexao(), campos[LayoutArquivo.ORC_CONTARED].trim());
			else
				conta = ContaContabilServiceDAO.pesquisarConta(con.getConexao(), campos[LayoutArquivo.ORC_CONTA].trim());
			
			CentroCusto cencus = CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(), 
					campos[LayoutArquivo.ORC_CENTROCUSTO].trim());
			
			if (cencus != null && conta != null) {
				
				if (conta.getTipo() == null || !conta.getTipo().equals(TipoConta.DO)) {
					servico.addErro("Conta com tipo errado: " + conta.getCdConta() + " tipo => " + conta.getTipo());
					return linhaAtual;
				}
				
				OrcamentoDespesa despesa = new OrcamentoDespesa();
				despesa.setConta(conta);
				
				List<ValorTotalMes> valores = new ArrayList<ValorTotalMes>();
				
				for (int i = 0; i < 12; i++) {
					
					int mes = LayoutArquivo.ORC_MES01 + i;
					
					BigDecimal valorDesp = BigDecimal.ZERO;
					String valorStr = campos[mes].replace(".", "").replace(",", ".").replace("-", "").replace("R$", "").trim();
					if (!valorStr.trim().isEmpty())
						valorDesp = new BigDecimal(valorStr);
					
					ValorTotalMes valor = new ValorTotalMes();
					valor.setMesRef(new MesAnoOrcamento((i + 1), plano.getNrAno()));
					valor.setVrOrcado(valorDesp);
					
					valores.add(valor);
				}
				
				OrcDespesaGeralServiceDAO.gravarOrcDespesaGeral(con.getConexao(), plano,
						cencus, despesa, valores);
			
				
			} else {
				
				if (cencus == null)
					servico.addErro("Setor não encontrado: " + campos[LayoutArquivo.ORC_CENTROCUSTO].trim());
				if (conta == null) 
					servico.addErro("Conta contabil não encontrada: " + campos[LayoutArquivo.ORC_CONTA].trim());
				
			}
	
		} catch (Exception e) {
			e.printStackTrace();
			servico.addErro(linhaAtual + " ==> " + linha + " ==> \n" + e.getMessage());
		}
		return linhaAtual;
	}

	private int importarOrcamentoVendas(String linha, int linhaAtual) {

		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);
		
		try {
			String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);
			Plano plano = null;
			
			if (Integer.parseInt(campos[LayoutArquivo.ORC_ANO]) == PlanoServiceDAO.getPlanoSelecionado().getNrAno()) {
				plano = PlanoServiceDAO.getPlanoSelecionado();
				
			} else {
			
				List<Plano> listaPlanos = PlanoServiceDAO.listar(con.getConexao());
				for (Plano plan : listaPlanos) {
					if (plan.getNrAno() == Integer.parseInt(campos[LayoutArquivo.ORC_ANO]) /*&&
							plan.getStatus().equals(StatusPlano._0)*/) {
						plano = plan;
						break;
					}
				}
			}
			
			ContaContabil conta = null;
			if (campos[LayoutArquivo.ORC_CONTA].trim().isEmpty())
				conta = ContaContabilServiceDAO.pesquisarContaReduzida(con.getConexao(), campos[LayoutArquivo.ORC_CONTARED].trim());
			else
				conta = ContaContabilServiceDAO.pesquisarConta(con.getConexao(), campos[LayoutArquivo.ORC_CONTA].trim());
			
			CentroCusto cencus = CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(), 
					campos[LayoutArquivo.ORC_CENTROCUSTO].trim());
			
			if (cencus != null && conta != null) {
				
				if (conta.getTipo() == null || !conta.getTipo().equals(TipoConta.RE)) {
					servico.addErro("Conta com tipo errado: " + conta.getCdConta() + " tipo => " + conta.getTipo());
					return linhaAtual;
				}
				
				
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
					int valorMes = LayoutArquivo.ORC_MES01;
					int quantMes = LayoutArquivo.ORC_QUANT01;
					
					for (int i = 0; i < 12; i++) {
						mesRef.setMes(i + 1);
						
						String valorStr = campos[valorMes].replace(".", "").replace(",", ".").replace("-", "").replace("R$", "").trim();
						if (valorStr.trim().isEmpty())
							continue;
						
						BigDecimal valor = new BigDecimal(valorStr);
						
						OrcamentoReceitaAcum orc = new OrcamentoReceitaAcum();
						orc.setReceita(receita);
						
						OrcamentoReceitaMes recMes = new OrcamentoReceitaMes();
						recMes.setMesRef(mesRef);
						recMes.setQuantidade(Integer.parseInt(campos[quantMes]));
						recMes.setValorUnitario(valor);
						
						OrcReceitaServiceDAO.gravarOrcamento(equipe.get(0), orc, recMes, plano,
								pstmtSel, pstmtIns, pstmtUpd, pstmtDel);
						
						valorMes +=2;
						quantMes +=2;
					}
					
					pstmtSel.close();
					pstmtIns.close();
					pstmtUpd.close();
					pstmtDel.close();
					
					pstmtSel = null;
					pstmtIns = null;
					pstmtUpd = null;
					pstmtDel = null;
				} else {
					servico.addErro("Conta de receita não encontrada: " + campos[LayoutArquivo.ORC_CONTA].trim());
				}
	
				
			} else {
				
				if (cencus == null)
					servico.addErro("Setor não encontrado: " + campos[LayoutArquivo.ORC_CENTROCUSTO].trim());
				if (conta == null) 
					servico.addErro("Conta contabil não encontrada: " + campos[LayoutArquivo.ORC_CONTA].trim());
				
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
					String valorStr = campos[LayoutArquivo.MOV_VALOR].replace(".", "").replace("-", "").replace(",", ".").trim();
					if (!valorStr.isEmpty())
						mov.setVlMovto(Double.parseDouble(valorStr));
					else
						mov.setVlMovto(0);

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

					if (mov.getVlMovto() != 0)
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
