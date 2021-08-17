package com.ctbli9.valorplan.negocio.importar;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ctbli9.valorplan.DAO.CargoServiceDAO;
import com.ctbli9.valorplan.DAO.CategoriaReceitaServiceDAO;
import com.ctbli9.valorplan.DAO.CentroCustoServiceDAO;
import com.ctbli9.valorplan.DAO.ClassificacaoCtbServiceDAO;
import com.ctbli9.valorplan.DAO.ContaContabilServiceDAO;
import com.ctbli9.valorplan.DAO.DepartamentoServiceDAO;
import com.ctbli9.valorplan.DAO.EquipeServiceDAO;
import com.ctbli9.valorplan.DAO.FilialServiceDAO;
import com.ctbli9.valorplan.DAO.FuncionarioServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.ReceitaServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.enumeradores.MedidaOrcamento;
import com.ctbli9.valorplan.enumeradores.TipoRecurso;
import com.ctbli9.valorplan.modelo.Cargo;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Departamento;
import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.modelo.importa.LayoutArquivo;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;
import com.ctbli9.valorplan.modelo.receita.DeducaoReceita;
import com.ctbli9.valorplan.modelo.receita.Receita;
import com.ctbli9.valorplan.negocio.CargoService;
import com.ctbli9.valorplan.negocio.RelatorioBIService;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.enumeradores.AreaSetor;
import ctbli9.enumeradores.GrupoContaContabil;
import ctbli9.enumeradores.NatContaContabil;
import ctbli9.enumeradores.TipoConta;
import ctbli9.modelo.Filial;
import ctbli9.modelo.ctb.ClasseDRE;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.modelo.ctb.GrupoClasseDRE;
import ctbli9.recursos.ArquivoTexto;
import ctbli9.recursos.LibUtil;

public class ImportarCadastro {
	private ConexaoDB con;
	
	ImportadorService servico = null;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, String> mapaDepto = new TreeMap();
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, BigDecimal> mapaRecurso = new TreeMap();


	// CLASSE_DRE
	private String cdNivelClasse;
	private Receita receita = null;
	
	public ImportarCadastro(ConexaoDB con, ImportadorService servico) {
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
			this.servico.limpaListaErro();

			arq.abreLeitura();
			linha = arq.lerLinha(); // ID do Arquivo
			linha = arq.lerLinha(); // Nomes das colunas

			while ((linha = arq.lerLinha()) != null) {
				if (!linha.isEmpty()) {
					// linha = LibUtil.substituiAcentuado(linha);
					 linha = linha.replaceAll(";;", "; ;").replaceAll("º", "");

					if (servico.getFormatoDados().getNomeLayout().equals("FILIAL"))
						linhaAtual = importarFilial(linha, linhaAtual);
					
					else if (servico.getFormatoDados().getNomeLayout().startsWith("FUNCIONARIO"))
						linhaAtual = importarFuncionario(linha, linhaAtual);
					
					else if (servico.getFormatoDados().getNomeLayout().startsWith("ESTRUTURA"))
						linhaAtual = importarDeptoSetor(linha, linhaAtual);
					
					else if (servico.getFormatoDados().getNomeLayout().startsWith("PLANO_DE_CONTAS"))
						linhaAtual = importarPlanoConta(linha, linhaAtual);
					
					else if (servico.getFormatoDados().getNomeLayout().startsWith("CLASSE_DRE"))
						linhaAtual = importarClasseDRE(linha, linhaAtual);
					
					else if (servico.getFormatoDados().getNomeLayout().startsWith("CONTAS_OPERACIONAIS"))
						linhaAtual = importarContas(linha, linhaAtual);


					if (++contador > 5000) {
						ConexaoDB.gravarTransacao(con);
						contador = 0;
					}
				}
			} // while
			
			if (servico.getFormatoDados().getNomeLayout().startsWith("ESTRUTURA"))
				criarRecurso(con);

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
	 * 1o. Importar FILIAIS
	 */
	public int importarFilial(String linha, int linhaAtual) throws Exception {
		String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);
		linhaAtual++;

		try {
			Filial filial = FilialServiceDAO.pesquisarFilial(con.getConexao(), campos[LayoutArquivo.FIL_CODIGO]);

			if (filial == null || filial.getCdFilial() == 0) {
				filial = new Filial();

				filial.setCdFilial(0);
				filial.setCodExterno(campos[LayoutArquivo.FIL_CODIGO].trim());
				filial.setSgFilial(campos[LayoutArquivo.FIL_APELIDO]);
				
				filial.setDcFilial(new BigDecimal(campos[LayoutArquivo.FIL_CNPJ].trim().
						replace(".", "").replace("-", "").replace("/", "").replace("\\", "")));
				
				filial.setDsFilial(campos[LayoutArquivo.FIL_NOME]);

				FilialServiceDAO.incluir(con.getConexao(), filial);

				// TODO colocar o endereço como opcional ou então separar as importações
				/*FilialEndereco end = new FilialEndereco();

				end.setSqEndFilial(0);
				end.setTipo(TipoEndereco.valueOf(campos[LayoutArquivo.FIL_TIP_END]));
				end.setTpLogradouro(campos[LayoutArquivo.FIL_IDT_END]);
				end.setNmLogradouro(campos[LayoutArquivo.FIL_ENDERECO]);

				try {
					end.setNrLogradouro(Integer.parseInt(campos[LayoutArquivo.FIL_NUM_END].trim()));
				} catch (Exception e) {
					end.setNrLogradouro(0);
					end.setNmLogradouro(end.getNmLogradouro().trim() + " " + campos[LayoutArquivo.FIL_NUM_END]);
				}

				end.setDsComplemento(campos[LayoutArquivo.FIL_COMP_END]);

				try {
					end.setNrCEP(Integer.parseInt(campos[LayoutArquivo.FIL_CEP].replace("-", "")));
				} catch (Exception e) {
					end.setNrCEP(0);
				}

				Cidade cidade = new Cidade();
				cidade.setCdCidade(0);
				cidade.setNmCidade(campos[LayoutArquivo.FIL_NOM_CIDADE]);
				try {
					cidade.setCdIBGE(Integer.parseInt(campos[LayoutArquivo.FIL_COD_IBGE]));
				} catch (Exception e) {
					cidade.setCdIBGE(0);
				}

				try {
					cidade.setEstado(UF.valueOf(campos[LayoutArquivo.FIL_UF_END]));
				} catch (Exception e) {
					cidade.setEstado(UF.ID);
				}

				cidade = CidadeServiceDAO.buscarCidadePorNome(con.getConexao(), cidade);
				if (cidade.getCdCidade() == 0)
					CidadeServiceDAO.incluir(con.getConexao(), cidade);

				Bairro bairro = new Bairro();
				bairro.setCdBairro(0);
				bairro.setNmBairro(campos[LayoutArquivo.FIL_NOM_BAIRRO]);
				bairro.setCidade(cidade);

				bairro = CidadeServiceDAO.buscarBairroPorNome(con.getConexao(), bairro);
				if (bairro.getCdBairro() == 0)
					CidadeServiceDAO.incluirBairro(con.getConexao(), bairro);

				end.setBairro(bairro);
				end.setIdPrincipal(campos[LayoutArquivo.FIL_IDT_PRINC].equals("S"));

				end.setNmContato(campos[LayoutArquivo.FIL_NOM_CONTATO]);
				try {
					end.setNrRamal(Integer.parseInt(campos[LayoutArquivo.FIL_RAMAL]));
				} catch (Exception e) {
					end.setNrRamal(0);
				}
				end.setTlCelular(campos[LayoutArquivo.FIL_CELULAR]);
				end.setTlFax(campos[LayoutArquivo.FIL_FAX]);
				end.setTlFixo(campos[LayoutArquivo.FIL_TELEFONE]);
				end.setTxEmailContato(campos[LayoutArquivo.FIL_EMAIL_CONTATO]);
				end.setIdAtivo(true);

				FilialServiceDAO.incluirEndereco(con.getConexao(),filial, end);
				*/
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.servico.addErro("erro: " + e.getMessage() + "\n" + linhaAtual + " ==> " + linha);
		}
		return linhaAtual;

	}

	/*
	 * 2o. Importar a árvore estrutural do cliente (departamentos e setores)
	 */
	private int importarDeptoSetor(String linha, int linhaAtual) {
		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);

		try {
			String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);
			
			String chave = "";

			Departamento pai = null;
			Departamento depto = null;
			int posicaoDepto = LayoutArquivo.EST_NIV01;
			for (int i = 0; i < servico.getFormatoDados().getMaxNivel(); i++) {

				chave += campos[posicaoDepto];
				depto = gravarDepartamento(chave, campos, posicaoDepto, pai);

				pai = (Departamento) LibUtil.clonaObjeto(depto);

				posicaoDepto += 3;
			}

			CentroCusto cenc = CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(),
					campos[LayoutArquivo.EST_SETORCODEXT]);
			if (cenc == null) {
				cenc = new CentroCusto();
				
				cenc.setCdCentroCusto(0);
				cenc.setCodExterno(campos[LayoutArquivo.EST_SETORCODEXT]);
				cenc.setFilial(FilialServiceDAO.pesquisarFilial(con.getConexao(), campos[LayoutArquivo.EST_FILCOD].trim()));

				cenc.setCecDsResumida(campos[LayoutArquivo.EST_DESRESUMIDA].trim());
				cenc.setCecDsCompleta(campos[LayoutArquivo.EST_DESCOMPLETA].trim());
				cenc.setDepartamento(depto);
				
				cenc.setTipoArea(AreaSetor.valueOf(campos[LayoutArquivo.EST_AREASETOR]));

				String[] tiposRec = campos[LayoutArquivo.EST_TIPRECEITA].split(":");

				List<CategoriaReceita> tipos = new ArrayList<>();
				for (String tip : tiposRec) {
					if (tip != null && !tip.trim().isEmpty()) {
						CategoriaReceita tipoReceita = new CategoriaReceita();
						tipoReceita.setCdCategoria(Integer.parseInt(tip.trim()));
						tipos.add(tipoReceita);
					}
				}
				cenc.setTipos(tipos);

				CentroCustoServiceDAO.salvar(con.getConexao(), cenc);
				
				mapaRecurso.put("C" + cenc.getCdCentroCusto(), new BigDecimal(campos[LayoutArquivo.EST_CPFLIDER].trim().
						replace(".", "").replace("-", "").replace("/", "").replace("\\", "")));

				
				// Vincular os centros de custo aos funcionários importados
				FuncionarioServiceDAO.atualizaRelacaoFuncionarioSetor(con.getConexao(), cenc.getCodExterno());
			}


		} catch (Exception e) {
			e.printStackTrace();
			this.servico.addErro(linhaAtual + " ==> " + linha + "\n" + e.getMessage());
		}
		return linhaAtual;

	}
	
	private Departamento gravarDepartamento(String chave, String[] campos, int posicaoDepto, Departamento pai)
			throws Exception {

		int posicaoDescri = posicaoDepto + 1;
		int posicaoCPF = posicaoDescri + 1;

		int cdDepartamento = 0;
		for (String descrDepto : mapaDepto.keySet()) { // Seta o valor da chave
			if (descrDepto.equals(chave)) {
				cdDepartamento = Integer.parseInt(mapaDepto.get(descrDepto)); // Pega o valor da chave
				break;
			}
		}

		Departamento depto = null;
		if (cdDepartamento > 0)
			depto = DepartamentoServiceDAO.pesquisarDepartamento(con.getConexao(), cdDepartamento);

		if (cdDepartamento == 0) {
			BigDecimal cpf = new BigDecimal(campos[posicaoCPF].trim().
					replace(".", "").replace("-", "").replace("/", "").replace("\\", ""));

			depto = new Departamento();

			depto.setCdDepartamento(0);
			depto.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());

			if (campos[posicaoDepto].length() > 10)
				depto.setSgDepartamento(campos[posicaoDepto].substring(0, 10));
			else
				depto.setSgDepartamento(campos[posicaoDepto]);

			depto.setDsDepartamento(campos[posicaoDescri]);
			depto.setDepartamentoPai(pai);
			depto.setResponsavel(null);
			DepartamentoServiceDAO.incluir(con.getConexao(), depto);
			
			mapaRecurso.put("D" + depto.getCdDepartamento(), cpf);
			
			mapaDepto.put(chave, Long.toString(depto.getCdDepartamento()));
		}

		return depto;
	}

	private void criarRecurso(ConexaoDB con) throws Exception {
		System.out.println("Criar os recursos vinculados aos gestores");
		
		for (String codigo : mapaRecurso.keySet()) { // Seta o valor da chave
			char tipo = codigo.charAt(0);
			long id = Long.parseLong(codigo.substring(1));
			
			Funcionario funcionario = FuncionarioServiceDAO.pesquisarPorCPF(con.getConexao(), mapaRecurso.get(codigo));
					
			if (tipo == 'C') {
				criarRecursoCC(con, id, funcionario);
			} else if (tipo == 'D') {
				criarRecursoDEP(con, id, funcionario);
			}  
		}

	}
	private void criarRecursoCC(ConexaoDB con, long id, Funcionario funcionario) throws Exception {
		
		CentroCusto cenc = CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(), (int) id);
		Recurso responsavel = EquipeServiceDAO.getRecursoVinculado(con.getConexao(), funcionario.getCdFuncionario());
		
		if (responsavel == null) {
			responsavel = new Recurso(new MesAnoOrcamento());
			responsavel.setCdRecurso(0);
			responsavel.setNmRecurso("GESTOR SETOR " + cenc.getCecDsResumida());
			responsavel.setVinculo(funcionario);
			responsavel.setCargo(responsavel.getVinculo().getCargo());
			responsavel.setSetor(cenc);
			responsavel.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
			responsavel.setInicioVinculo(0);
			responsavel.setFimVinculo(0);
			responsavel.setTipo(TipoRecurso.G);
			
			EquipeServiceDAO.incluir(con.getConexao(), responsavel);
		}
		cenc.setResponsavel(responsavel);
		
		CentroCustoServiceDAO.salvar(con.getConexao(), cenc);
	}
	
	private void criarRecursoDEP(ConexaoDB con, long id, Funcionario funcionario) throws Exception {
		
		Departamento depto = DepartamentoServiceDAO.pesquisarDepartamento(con.getConexao(), id);
		Recurso responsavel = EquipeServiceDAO.getRecursoVinculado(con.getConexao(), funcionario.getCdFuncionario());
		
		if (responsavel == null) {
			responsavel = new Recurso(new MesAnoOrcamento());
			responsavel.setCdRecurso(0);
			responsavel.setNmRecurso("GESTOR DEPARTAMENTO " + depto.getSgDepartamento());
			responsavel.setVinculo(funcionario);
			responsavel.setCargo(responsavel.getVinculo().getCargo());
			responsavel.setSetor(responsavel.getVinculo().getCenCusto());
			responsavel.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
			responsavel.setInicioVinculo(0);
			responsavel.setFimVinculo(0);
			responsavel.setTipo(TipoRecurso.G);
			
			EquipeServiceDAO.incluir(con.getConexao(), responsavel);
		}
		depto.setResponsavel(responsavel);
		
		DepartamentoServiceDAO.alterar(con.getConexao(), depto);
				 
	}
	/*
	 * 
	 */
	private int importarPlanoConta(String linha, int linhaAtual) throws Exception {
		String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);
		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);
		try {
			ContaContabil conta = new ContaContabil();
			conta.setCdConta(campos[LayoutArquivo.CTB_CONTA]);

			if (!ContaContabilServiceDAO.existe(con.getConexao(), conta.getCdConta())) {

				conta.setCdContaReduzi(campos[LayoutArquivo.CTB_CONTARED]);
				conta.setDsConta(campos[LayoutArquivo.CTB_DESCRICAO]);
				conta.setSgConta(servico.gerarSigla(conta.getDsConta()));
				conta.setGrupo(GrupoContaContabil.valueOf(campos[LayoutArquivo.CTB_GRUPO].trim().substring(0, 1)));
				conta.setNatureza(NatContaContabil.valueOf(campos[LayoutArquivo.CTB_NATUREZA].trim().substring(0, 1)));
				if (!campos[LayoutArquivo.CTB_TIPOCONTA].trim().isEmpty())
					conta.setTipo(TipoConta.valueOf(campos[LayoutArquivo.CTB_TIPOCONTA].trim().substring(0, 2)));

				ContaContabilServiceDAO.incluir(con.getConexao(), conta);
			}

		} catch (Exception e) {
			e.printStackTrace();
			this.servico.addErro(linhaAtual + " ==> " + linha + "\n" + e.getMessage());
		}
		return linhaAtual;
	}

	// CLASSE_DRE
	private int importarClasseDRE(String linha, int linhaAtual) {

		linha = LibUtil.substituiAcentuado(linha);
		linha = linha.replaceAll(";;", "; ;").replaceAll("º", "");

		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);
				
		try {
			String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);
			GrupoClasseDRE grupo = null;

			grupo = ClassificacaoCtbServiceDAO.pesquisarGrupoClassePorDescricao(con.getConexao(), 
					campos[LayoutArquivo.DRE_GRUPO].trim(),
					PlanoServiceDAO.getPlanoSelecionado().getNrAno());

			if (grupo == null) {
				grupo = new GrupoClasseDRE();
				grupo.setCdGrupoClasse((byte) 0);
				grupo.setDsGrupoClasse(campos[LayoutArquivo.DRE_GRUPO].trim());
				grupo.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
				ClassificacaoCtbServiceDAO.gravarGrupoClasse(con.getConexao(), grupo);
			}

			ClasseDRE classe = null;

			cdNivelClasse = campos[LayoutArquivo.DRE_CODCLAS].trim().replace(".", "");
			if (cdNivelClasse.length() == 1)
				cdNivelClasse = "0" + cdNivelClasse;

			int posicaoDRE = LayoutArquivo.DRE_NOMECLAS01;
			for (int i = 0; i < servico.getFormatoDados().getMaxNivel(); i++) {
				classe = ClassificacaoCtbServiceDAO.pesquisarPorDescricao(con.getConexao(), campos[posicaoDRE], cdNivelClasse,
						((i + 1) * 2), PlanoServiceDAO.getPlanoSelecionado().getNrAno());

				if (classe == null) {
					classe = new ClasseDRE();

					classe.setCdClasse(0);
					classe.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());

					if (i == 0)
						classe.setCdNivelClasse(cdNivelClasse);
					else
						classe.setCdNivelClasse(ClassificacaoCtbServiceDAO.novoCodigoNivel(con.getConexao(), cdNivelClasse,
								PlanoServiceDAO.getPlanoSelecionado().getNrAno()));

					classe.setDsClasse(campos[posicaoDRE]);
					classe.setGrupo(grupo);

					ClassificacaoCtbServiceDAO.incluir(con.getConexao(), classe);
				}
				cdNivelClasse = classe.getCdNivelClasse();
				posicaoDRE++;
			}

			//ContaContabil ctaContabil = ContaContabilServiceDAO.pesquisarContaReduzida(con.getConexao(), campos[LayoutArquivo.DRE_PLACON].trim());
			ContaContabil ctaContabil = ContaContabilServiceDAO.pesquisarConta(con.getConexao(), campos[LayoutArquivo.DRE_PLACON].trim());
			if (ctaContabil == null)
				this.servico.addErro("Conta nao localizada: " + campos[LayoutArquivo.DRE_PLACON].trim());

			CentroCusto cencus = null;
			if (campos[LayoutArquivo.DRE_CENCUSTO] == null || campos[LayoutArquivo.DRE_CENCUSTO].trim().isEmpty())
				cencus = new CentroCusto();
			else
				cencus = CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(),
						campos[LayoutArquivo.DRE_CENCUSTO].trim());

			if (cencus == null)
				this.servico.addErro("Centro de Custo nao localizado: " + campos[LayoutArquivo.DRE_CENCUSTO].trim());

			if (ctaContabil != null && cencus != null) {
				if (!ClassificacaoCtbServiceDAO.existeClasseConta(con.getConexao(), ctaContabil, cencus, classe))
					ClassificacaoCtbServiceDAO.incluirClasseConta(con.getConexao(), ctaContabil, cencus, classe);

				if (LayoutArquivo.DRE_CAMPOBI_ADIC >= 0 && LayoutArquivo.DRE_CAMPOBI_RV >= 0
						&& LayoutArquivo.DRE_CAMPOBI_LB >= 0 && LayoutArquivo.DRE_CAMPOBI_EBTDA >= 0)
					new RelatorioBIService().importaCamposBI(con.getConexao(), 
							linha, ctaContabil, cencus, LayoutArquivo.DRE_CAMPOBI_ADIC,
							LayoutArquivo.DRE_CAMPOBI_RV, LayoutArquivo.DRE_CAMPOBI_LB,
							LayoutArquivo.DRE_CAMPOBI_EBTDA);
			}

		} catch (Exception e) {
			e.printStackTrace();
			this.servico.addErro(linhaAtual + " ==> " + linha + "\n" + e.getMessage());
		}

		return linhaAtual;
	}

	public int importarFuncionario(String linha, int linhaAtual) throws Exception {
		String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);

		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);

		try {
			
			BigDecimal cpfAux = new BigDecimal(campos[LayoutArquivo.FUN_CPF].trim().
					replace(".", "").replace("-", "").replace("/", "").replace("\\", ""));
			
			Funcionario funcionario = FuncionarioServiceDAO.pesquisarPorCPF(con.getConexao(),
					cpfAux);
			if (funcionario == null || funcionario.getCdFuncionario() == 0) {
				
				Cargo cargo = CargoServiceDAO.pesquisarDescricao(con.getConexao(), campos[LayoutArquivo.FUN_CARGO].trim());
				if (cargo == null) {
					cargo = new Cargo();

					cargo.setCdCargo(0);
					cargo.setDsCargo(campos[LayoutArquivo.FUN_CARGO].trim());

					cargo.setTpCargo(new CargoService(con).defineTipoCargo(cargo.getDsCargo()));

					CargoServiceDAO.incluir(con.getConexao(), cargo);
				}

				funcionario = new Funcionario();
				funcionario.setCdFuncionario(0);
				funcionario.setCargo(cargo);

				String login = campos[LayoutArquivo.FUN_LOGIN].toLowerCase();
				if (login.trim().isEmpty()) {
					login = gerarLogin(campos[LayoutArquivo.FUN_NOME]);
				}
				
				CentroCusto cenCusto = CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(), campos[LayoutArquivo.FUN_COD_SETOR].trim());
				
				funcionario.setCenCusto(cenCusto);
				
				if (cenCusto == null)
					FuncionarioServiceDAO.guardarRelacaoFuncionarioSetor(con.getConexao(), 
							cpfAux, campos[LayoutArquivo.FUN_COD_SETOR].trim());
				
				funcionario.setLogUsuario(login);
				funcionario.setTxEmailFuncionario(campos[LayoutArquivo.FUN_EMAIL]);
				funcionario.setNmFuncionario(campos[LayoutArquivo.FUN_NOME].trim());
				funcionario.setDcFuncionario(cpfAux);
				funcionario.setFunIdAtivo(true);

				FuncionarioServiceDAO.incluir(con.getConexao(), funcionario);
				
			} else {
				CentroCusto cenCusto = CentroCustoServiceDAO.pesquisarCentroCusto(con.getConexao(), campos[LayoutArquivo.FUN_COD_SETOR].trim());
				funcionario.setCenCusto(cenCusto);
				FuncionarioServiceDAO.alterar(con.getConexao(), funcionario);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
			this.servico.addErro(linhaAtual + " ==> " + linha + "\n" + e.getMessage());
		}
		return linhaAtual;
	}

	private String gerarLogin(String nome) {
		String partes[] = nome.trim().split(" ");
		
		if (partes[0].trim().isEmpty())
			partes[0] = partes[1];
		
		String login = partes[0].charAt(0) + partes[partes.length - 1];
		login = login.toLowerCase();
		
		return login;
	}


	/*
	 * 
	 */
	private int importarContas(String linha, int linhaAtual) throws Exception {
		String[] campos = LayoutArquivo.verificaNumeroCampos(servico.getFormatoDados(), linha);

		linhaAtual++;
		System.out.println("linha : " + linhaAtual + " => " + linha);

		try {
			ContaContabil ctaContabil = ContaContabilServiceDAO.pesquisarContaReduzida(con.getConexao(), 
					campos[LayoutArquivo.CTA_PLACON].trim());
					
			if (ctaContabil != null) {

				String sigla = campos[LayoutArquivo.CTA_SIGLA].trim().toUpperCase();
				if (sigla.isEmpty())
					sigla = servico.gerarSigla(campos[LayoutArquivo.CTA_DESCRICAO].trim());

				if (campos[LayoutArquivo.CTA_TIPOCONTA].trim().toUpperCase().charAt(0) == 'R') {
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
					}

				} else { // Tipo <> RECEITA
					if (receita != null) {
						DeducaoReceita deducao = new DeducaoReceita();
						deducao.setCdReceita(receita.getCdReceita());
						deducao.setConta(ctaContabil);
						
						ReceitaServiceDAO.incluirDeducao(con.getConexao(), deducao);
					}
				}

			} // Tem conta contabil existente

		} catch (Exception e) {
			e.printStackTrace();
			this.servico.addErro(linhaAtual + " ==> " + linha + " ==> \n" + e.getMessage());
		}
		return linhaAtual;
	}// importarContas

}
