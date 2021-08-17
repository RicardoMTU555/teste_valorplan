package com.ctbli9.valorplan.bean;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.naming.NamingException;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.ClassificacaoCtbServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.ctb.ContaClasseSetor;
import com.ctbli9.valorplan.modelo.ctb.SelecionaCentroCusto;
import com.ctbli9.valorplan.modelo.ctb.SelecionaConta;
import com.ctbli9.valorplan.negocio.ClassificacaoCtbService;
import com.ctbli9.valorplan.negocio.ContaContabilService;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.LibUtilFaces;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.modelo.ctb.ClasseDRE;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.modelo.ctb.GrupoClasseDRE;
import ctbli9.recursos.DataUtil;
import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.LibUtil;

@ManagedBean(name="classificacaoCtbBean")
@ViewScoped
public class ClassificacaoCtbBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private TreeNode root1;
	private List<GrupoClasseDRE> listaGrupoClasse = new ArrayList<GrupoClasseDRE>();
	private GrupoClasseDRE grupoClasse;
	private ClasseDRE classeConta;
	private TreeNode selectedNode;
	private MesAnoOrcamento anoReferencia = new MesAnoOrcamento();
	
	private SelecionaConta conta;
	private ContaClasseSetor classeSetor;
	private List<SelecionaCentroCusto> listaCencustoFiltrado = new ArrayList<SelecionaCentroCusto>();
	
    private TreeNode raiz;
    private TreeNode eloSelec;
    
	private int nroAnoOrigem;
	private int nroAnoDestino;
	
	private String codConta;
	
	private StreamedContent file;
	private String nomeXLS;
	
	private FacesMessages msg = new FacesMessages();

	/*
	 * Constructor
	 */
	public ClassificacaoCtbBean() {
		this.anoReferencia.setAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
		this.classeSetor = new ContaClasseSetor();
	}

	/*
	 * Atributos
	 */
    public TreeNode getRoot1() {
        return root1;
    }
    public void setRoot1(TreeNode root1) {
		this.root1 = root1;
	}

	public ClasseDRE getClasseConta() {
		return classeConta;
	}
	public void setClasseConta(ClasseDRE classeConta) {
		this.classeConta = classeConta;
	}

    public TreeNode getSelectedNode() {
        return selectedNode;
    }
    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }
    
	public List<GrupoClasseDRE> getListaGrupoClasse() {
		return listaGrupoClasse;
	}
	public void setListaGrupoClasse(List<GrupoClasseDRE> listaGrupoClasse) {
		this.listaGrupoClasse = listaGrupoClasse;
	}
	
	public GrupoClasseDRE getGrupoClasse() {
		return grupoClasse;
	}
	public void setGrupoClasse(GrupoClasseDRE grupoClasse) {
		this.grupoClasse = grupoClasse;
	}
		
	public boolean isItemSelecionado() {
		return this.classeConta != null && this.classeConta.getCdClasse() != 0;
	}

	public MesAnoOrcamento getAnoReferencia() {
		return anoReferencia;
	}
	public void setAnoReferencia(MesAnoOrcamento anoReferencia) {
		this.anoReferencia = anoReferencia;
	}
	
	public SelecionaConta getConta() {
		return conta;
	}
	public void setConta(SelecionaConta conta) {
		this.conta = conta;
	}
	
	public ContaClasseSetor getClasseSetor() {
		return classeSetor;
	}
	public void setClasseSetor(ContaClasseSetor classeSetor) {
		this.classeSetor = classeSetor;
	}
	
	public List<SelecionaCentroCusto> getListaCencustoFiltrado() {
		return listaCencustoFiltrado;
	}
	public void setListaCencustoFiltrado(List<SelecionaCentroCusto> listaCencustoFiltrado) {
		this.listaCencustoFiltrado = listaCencustoFiltrado;
	}
	
    public TreeNode getRaiz() {
        return raiz;
    }
    
    public TreeNode getEloSelec() {
		return eloSelec;
	}
    public void setEloSelec(TreeNode eloSelec) {
		this.eloSelec = eloSelec;
	}

	public int getNroAnoOrigem() {
		return nroAnoOrigem;
	}
	public void setNroAnoOrigem(int nroAnoOrigem) {
		this.nroAnoOrigem = nroAnoOrigem;
	}
	public int getNroAnoDestino() {
		return nroAnoDestino;
	}
	public void setNroAnoDestino(int nroAnoDestino) {
		this.nroAnoDestino = nroAnoDestino;
	}
	
	public String getCodConta() {
		return codConta;
	}
	public void setCodConta(String codConta) {
		this.codConta = codConta;
	}
	
	/*
	 * Metodos
	 */
	public void inicializarRegistro() {
		classeConta = new ClasseDRE();
		this.selectedNode = root1;
		
		classeConta.setGrupo(new GrupoClasseDRE());
		classeConta.setNrAno(this.anoReferencia.getAno());

		LibUtilFaces.atualizarView("frm:classeContaDialog");
	}
	
	public void inicializarSubItem() {
		classeConta = new ClasseDRE();
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			ClasseDRE classeAux = (ClasseDRE)selectedNode.getData();
			
			this.classeConta = (ClasseDRE) LibUtil.clonaObjeto(classeAux);
			this.classeConta.setCdClasse(0);
			this.classeConta.setNrAno(this.anoReferencia.getAno());
		
			this.classeConta.setCdNivelClasse(ClassificacaoCtbServiceDAO.novoCodigoNivel(con.getConexao(), classeConta.getCdNivelClasse(), 
					anoReferencia.getAno()));
			this.classeConta.setDsClasse("");
			this.classeConta.setTemConta(false);
				
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
			LibUtilFaces.atualizarView("frm:classeContaDialog");
		} 
	}
	
	
	public void alterarRegistro() {
		classeConta = new ClasseDRE();
		if(selectedNode != null)
			classeConta = (ClasseDRE)selectedNode.getData();
		
		LibUtilFaces.atualizarView("frm:classeContaDialog");
	}
	
	public void listarTudo() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			ClassificacaoCtbService servico = new ClassificacaoCtbService(con);
			this.listaGrupoClasse = servico.listarGrupos(anoReferencia.getAno());
			this.root1 = servico.gerarArvoreClassificacaoCtb(servico.listar(anoReferencia.getAno()));			
			servico = null;
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}
	
	public void salvar(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			boolean novo = this.classeConta.getCdClasse() == 0;
			
			new ClassificacaoCtbService(con).salvar(this.classeConta);
			
			if (novo)
				new DefaultTreeNode(this.classeConta, this.selectedNode);

			LibUtilFaces.atualizarView("frm:classeTabView:classesContaTree", "frm:messages");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
			else
				LibUtilFaces.atualizarView("frm:messages");
		} finally {
			ConexaoDB.close(con);
		} 		
	}
	
	public void moverItemParaCima() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
		
			TreeNode pai = this.selectedNode.getParent();
			
			for (int i = 0; i < pai.getChildren().size(); i++) {
				TreeNode filho = pai.getChildren().get(i);
				
				if (filho.equals(this.selectedNode)) { // Achei o nó que vai pra cima
					if (i == 0)
						msg.info("Movimento não é possível para nível anterior.");
					else {
						TreeNode filhoAnt = pai.getChildren().get(i-1);
	
						// 1o. Reposiciona elementos
						pai.getChildren().remove(filho);
						pai.getChildren().remove(filhoAnt);
	
						pai.getChildren().add((i-1), filhoAnt);
						pai.getChildren().add((i-1), filho);
						
						// 2o. Muda código nas classes reposicionadas
						ClasseDRE classeAnt = (ClasseDRE) filhoAnt.getData();
						classeConta = (ClasseDRE) filho.getData();
						
						String nivelAux = classeConta.getCdNivelClasse();
						classeConta.setCdNivelClasse(classeAnt.getCdNivelClasse());
						classeAnt.setCdNivelClasse(nivelAux);
						
						// 3o. Muda codigo nos filhos, e grava as classes
						gravaFilhos(con, filhoAnt, classeAnt);
						gravaFilhos(con, filho, classeConta);
					}
					break;
				}
			}
			
			ConexaoDB.gravarTransacao(con);
			
			LibUtilFaces.atualizarView("frm:classeTabView:classesContaTree", "frm:messages");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			LibUtilFaces.atualizarView("frm:messages");
		
		} finally {
			ConexaoDB.close(con);
		}
			
	}
	
	private void gravaFilhos(ConexaoDB con, TreeNode elo, ClasseDRE classe) throws SQLException, NamingException {
		ClassificacaoCtbServiceDAO.alterar(con.getConexao(), classe);
		
		String cdNivelPai = classe.getCdNivelClasse();
			
		for (TreeNode descend : elo.getChildren()) {
			ClasseDRE aux = (ClasseDRE) descend.getData();
			String novoNivel = cdNivelPai + aux.getCdNivelClasse().substring(cdNivelPai.length());
			aux.setCdNivelClasse(novoNivel);
			ClassificacaoCtbServiceDAO.alterar(con.getConexao(), aux);
			
			if (descend.getChildCount() > 0) { // RECURSIVIDADE
				gravaFilhos(con, descend, aux);
			}
		}
	}

	public void moverItemParaBaixo() {
		TreeNode pai = this.selectedNode.getParent();
		
		for (int i = 0; i < pai.getChildren().size(); i++) {
			TreeNode filho = pai.getChildren().get(i);
			if (filho.equals(this.selectedNode)) {
				if (i == (pai.getChildCount()-1))
					msg.info("Movimento não é possível para nível posterior.");
				
				break;
			}
		}
	}

	public void gravarArvore() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			ClassificacaoCtbService servico = new ClassificacaoCtbService(con);
			
			for (TreeNode elo : this.root1.getChildren()) {
				servico.gravaFilho(elo);
			} 
			this.root1 = servico.gerarArvoreClassificacaoCtb(servico.listar(anoReferencia.getAno()));
			
			servico = null;
			ConexaoDB.gravarTransacao(con);
			
			msg.info("Árvore gravada com sucesso!");
		
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 
	}
	
	public void excluirClasse() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			if(this.selectedNode != null)
				this.classeConta = (ClasseDRE)selectedNode.getData();
			
			new ClassificacaoCtbService(con).excluir(this.classeConta);
			ConexaoDB.gravarTransacao(con);
			
			TreeNode parent = this.selectedNode.getParent();
		    if(parent != null){
		    	parent.getChildren().remove(this.selectedNode);
		    }//listarTudo();
			
			msg.info("Classe excluida!");
			
			LibUtilFaces.atualizarView("frm:classeTabView:classesContaTree", "frm:messages");

		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 
	}//excluir	

	// ************************************************************************************* \\
	public void incluirGrupoClasse() {
		GrupoClasseDRE grupo = new GrupoClasseDRE();
		grupo.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
		this.listaGrupoClasse.add(grupo);
	}

	public void salvarGrupoClasse() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new ClassificacaoCtbService(con).salvarGrupoClasse(this.listaGrupoClasse, anoReferencia.getAno());
			ConexaoDB.gravarTransacao(con);
			
			this.msg.info("Grupos salvos com sucesso.");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 
	}
	
	public void excluirGrupoClasse() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			new ClassificacaoCtbService(con).excluirGrupoClasse(this.grupoClasse);
			ConexaoDB.gravarTransacao(con);
			
			int i = 0;
			for (GrupoClasseDRE grupo : this.listaGrupoClasse) {
				if (grupo.equals(this.grupoClasse)) {
					this.listaGrupoClasse.remove(i);
					break;
				}
				i++;
			}
			
			msg.info("Grupo excluído!");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 
	}//excluir	

	// ************************************************************************************* \\

	public void listarContasSelecionadas() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			if(selectedNode != null) {
				this.classeConta = (ClasseDRE)selectedNode.getData();
				this.classeSetor = new ContaClasseSetor();
				this.raiz = new ContaContabilService(con).criarArvorePlaconta(this.classeConta);
				
				this.eloSelec = achaSelecionado(this.raiz);
				if (this.eloSelec != null) {
					listarCencustoSelecionados();
				}
				
				LibUtilFaces.atualizarView("frm:relacionaDialog");
				
			} else
				msg.erro("Item não encontrado.");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 
	}
	
	private TreeNode achaSelecionado(TreeNode elo) {
		if (elo.isSelectable() && elo.isSelected()) {
			return elo;
		}
		
		for (TreeNode filho : elo.getChildren()) {
			elo = achaSelecionado(filho);
			if (elo != null)
				return elo;
		}
		return null;
	}

	public void listarCencustoSelecionados() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			boolean utilizaSetorAtual = this.classeSetor.isUtilizaSetor();
			
			this.conta = (SelecionaConta) this.eloSelec.getData();
			
			this.classeSetor = new ContaClasseSetor();
			if (conta != null)
				this.classeSetor = new ClassificacaoCtbService(con).lisarCentroCustoConta(this.classeConta, (ContaContabil) conta,
						utilizaSetorAtual);
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		}
	}
	
	public void desmarcarSetores() {
		this.classeSetor.desmarcaSetores();
	}
	
	public void marcaSetor(SelecionaCentroCusto item) {
		if (item.isSeleciona())
			this.classeSetor.addSetor();
		else
			this.classeSetor.subSetor();
	}
	
	public void salvarClaConta() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			TreeNode elo = this.eloSelec;
			while (!elo.getType().equals("analitica")) {
				elo = elo.getChildren().get(0);
			}
			this.conta = (SelecionaConta) elo.getData();
			
			new ClassificacaoCtbService(con).salvarClaConta(this.classeConta, conta, 
					this.classeSetor);
			ConexaoDB.gravarTransacao(con);
			
			if (this.conta.isSeleciona())
				elo.getParent().setExpanded(true);
			
			msg.info("Relacionamentos salvos com sucesso em " +
					this.classeConta.getDsClasse() + " - " + conta.getCdConta());
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			FacesContext.getCurrentInstance().validationFailed();
		} finally {
			ConexaoDB.close(con);		
		} 
	}

	
	public void replicar() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
		
			new ClassificacaoCtbService(con).replicarDadosAno(nroAnoOrigem, nroAnoDestino);
			ConexaoDB.gravarTransacao(con);
			
			msg.info("Replicação efetuada com sucesso!<br/>"
					+ "<b>Ano Origem: </b> " + nroAnoOrigem 
					+ " <b>Ano Destino: </b> " + nroAnoDestino);
			
			LibUtilFaces.atualizarView("frm:messages");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
			else
				LibUtilFaces.atualizarView("frm:messages");
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	
	public void baixarPlanilha() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
			this.nomeXLS = "DRE_" + DataUtil.dataString(new Date(), "yyyy_MM_dd_HH_mm_ss") + ".csv";
			caminho = caminho + "relatorios/" + this.nomeXLS;
			
			new ClassificacaoCtbService(con).gerarPlanilhaCSV(caminho);
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 
	}
	

	public StreamedContent getFile() throws FileNotFoundException {
		String caminhoWebInf = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
		caminhoWebInf = caminhoWebInf + "relatorios/" + this.nomeXLS;
		InputStream stream = new FileInputStream(caminhoWebInf); //Caminho onde esta salvo o arquivo.
        
		file = DefaultStreamedContent.builder()
                .name(this.nomeXLS)
                .contentType("application/csv")
                .stream(() -> stream)
                .build(); 

        return file;  
    }

	public List<String> completarCodigo(String consulta) {
		List<String> listaCodigo = null;
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
	
			listaCodigo = new ClassificacaoCtbService(con).retornarContas(consulta);
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 
		
		return listaCodigo;
	}

	public void localizarConta() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			String retorno = "";
			
			List<String> classes = new ClassificacaoCtbService(con).buscarClassificacao(this.codConta);
			
			for (String classe : classes) {
				if (procuraClasseParaExpandir(this.root1, classe, false)) {
					if (!retorno.contains(classe))
						retorno += classe + ", ";
				}
			}
			
			if (!retorno.isEmpty()) {
				retorno = "Conta localizada no(s) nível(is) " + retorno.substring(0, retorno.length()-2);
				this.msg.info(retorno);
			}
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);		
		} 
	}

	private boolean procuraClasseParaExpandir(TreeNode elo, String nivelClasse, boolean encontrou) {
		if (elo.getChildCount() == 0) {
			classeConta = (ClasseDRE) elo.getData();
			
			if (classeConta.getCdNivelClasse().equals(nivelClasse)) {
				encontrou = true;
				expandeNivel(elo.getParent());
				this.selectedNode = elo;
			}	
			return encontrou;
		}
		
		for (TreeNode filho : elo.getChildren()) {
			encontrou = procuraClasseParaExpandir(filho, nivelClasse, encontrou);
		}
		
		return encontrou;
	}

	private void expandeNivel(TreeNode pai) {
		pai.setExpanded(true);
		if (pai.getParent() == null)
			return;
		
		expandeNivel(pai.getParent());
	}
	
}
