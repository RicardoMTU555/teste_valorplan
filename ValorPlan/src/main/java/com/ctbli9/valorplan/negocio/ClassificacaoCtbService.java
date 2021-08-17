package com.ctbli9.valorplan.negocio;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.ClassificacaoCtbServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.ctb.ContaClasseSetor;
import com.ctbli9.valorplan.modelo.ctb.SelecionaConta;

import ctbli9.modelo.ctb.ClasseDRE;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.modelo.ctb.GrupoClasseDRE;
import ctbli9.recursos.RegraNegocioException;

public class ClassificacaoCtbService {
	private ConexaoDB con;
	
	public ClassificacaoCtbService(ConexaoDB con) {
		this.con = con;
	}

	public ClasseDRE pesquisarClassificacaoCtb(int cdClasse) throws SQLException, NamingException {
		return ClassificacaoCtbServiceDAO.pesquisarClassificacaoCtb(con.getConexao(), cdClasse);
	}
	
	public List<ClasseDRE> listar(int nroAno) throws SQLException, NamingException {
		return ClassificacaoCtbServiceDAO.listar(con.getConexao(), nroAno);
	}
	
	public List<ClasseDRE> listarNivelMaximo(int nroAno) throws SQLException, NamingException {
		return ClassificacaoCtbServiceDAO.listarNivelMaximo(con.getConexao(), nroAno);
	}
	

	public void salvar(ClasseDRE classeConta) throws RegraNegocioException, Exception {
		if (ClassificacaoCtbServiceDAO.pesquisarClassificacaoCtb(con.getConexao(), classeConta.getCdClasse()).getCdClasse() == 0 )
			ClassificacaoCtbServiceDAO.incluir(con.getConexao(), classeConta);
		else
			ClassificacaoCtbServiceDAO.alterar(con.getConexao(), classeConta);
	}

	public void excluir(ClasseDRE classeConta) throws RegraNegocioException, Exception {
		if (!classeConta.isUltimoNivel())
			throw new RegraNegocioException("Exclua antes níveis FILHOS deste nível.");

		if (classeConta.isTemConta())
			throw new RegraNegocioException("Exclua antes os relacionamentos de conta e centro de custo deste nível.");
		
		ClassificacaoCtbServiceDAO.excluir(con.getConexao(), classeConta);
	}
	
	public TreeNode gerarArvoreClassificacaoCtb(List<ClasseDRE> classes) {
		TreeNode raiz = new DefaultTreeNode(new ClasseDRE(), null);
		
		int maxNivelClasse = 1;
		for (ClasseDRE classe : classes) {
			if (classe.getNivel() > maxNivelClasse)
				maxNivelClasse = classe.getNivel(); 
		}
		TreeNode[] eloClasse = new TreeNode[maxNivelClasse];
		
		
		for (ClasseDRE classe : classes) {
			
			if (classe.getNivel() == 1)
				eloClasse[classe.getNivel()-1] = new DefaultTreeNode(classe, raiz);
			else
				eloClasse[classe.getNivel()-1] = new DefaultTreeNode(classe, eloClasse[classe.getNivel()-2]);
	
		}

		return raiz;
	}

	public void gravaFilho(TreeNode elo) throws RegraNegocioException, Exception {
		ClasseDRE classeConta = (ClasseDRE) elo.getData();
		this.salvar(classeConta);
		
		for (TreeNode filho : elo.getChildren()) {
			ClasseDRE classeContaFilho = (ClasseDRE) filho.getData(); // Instancia a classe do elo filho
			classeContaFilho.setGrupo(classeConta.getGrupo());        // Classe do elo filho herda o mesmo grupo do pai
			gravaFilho(filho);                                        // Chama recursivamente para gravar o filho e ver se tem netos
		}		
	}
	
	// *************************************************************************************** \\
	
	public List<GrupoClasseDRE> listarGrupos(int nroAno) throws SQLException, NamingException {
		return ClassificacaoCtbServiceDAO.listarGrupos(con.getConexao(), nroAno);
	}

	public void excluirGrupoClasse(GrupoClasseDRE grupoClasse) throws SQLException, NamingException {
		ClassificacaoCtbServiceDAO.excluirGrupoClasse(con.getConexao(), grupoClasse);
	}

	public void salvarGrupoClasse(List<GrupoClasseDRE> listaGrupoClasse, int nroAno) throws Exception {
			
		// 1o. Remove excluidos
		List<GrupoClasseDRE> listaAux = ClassificacaoCtbServiceDAO.listarGrupos(con.getConexao(), nroAno); // Grupos existentes no banco
		for (GrupoClasseDRE aux : listaAux) {
			boolean achou = false;
			for (GrupoClasseDRE aux1 : listaGrupoClasse) {
				if (aux1.equals(aux)) {
					achou = true;
					break;
				}
			}
			if (!achou)
				ClassificacaoCtbServiceDAO.excluirGrupoClasse(con.getConexao(), aux);
		}
		
		// 2o. Grava adicionados e alterados
		for (GrupoClasseDRE aux : listaGrupoClasse) {
			ClassificacaoCtbServiceDAO.gravarGrupoClasse(con.getConexao(), aux);
		}
		
	}


	public ContaClasseSetor lisarCentroCustoConta(ClasseDRE classeConta, ContaContabil conta, boolean utilizaSetorDefault) 
			throws SQLException, NamingException {
		ContaClasseSetor classeSetor = ClassificacaoCtbServiceDAO.lisarCentroCustoConta(con.getConexao(), classeConta, conta);
		
		// Se não tiver NENHUM setor selecionado e estiver informando que UTILIZA setor (não encontrou nada relacionado), inclui o default
		if (classeSetor.getQuantSetoresSelecionados() == 0 && classeSetor.isUtilizaSetor()) {
			classeSetor.setUtilizaSetor(utilizaSetorDefault);
			if (classeSetor.getQuantSetoresSelecionados() == 1) {
				classeSetor.getSetores().get(0).setSeleciona(false);
				classeSetor.subSetor();
			}
		}
			
		return classeSetor;
		
	}

	public void salvarClaConta(ClasseDRE classeConta, SelecionaConta conta,
			ContaClasseSetor classeSetor) throws SQLException, NamingException {
		
		if (classeSetor.isUtilizaSetor())
			ClassificacaoCtbServiceDAO.salvarClaContaComSetor(con.getConexao(), classeConta, conta, classeSetor.getListaCencusto());
		else
			ClassificacaoCtbServiceDAO.salvarClaContaSemSetor(con.getConexao(), classeConta, conta, classeSetor.getSetorZero());
	}
	
	
	public void replicarDadosAno(int nroAnoOrigem, int nroAnoDestino) throws SQLException, NamingException, RegraNegocioException {
		if (nroAnoOrigem == nroAnoDestino)
			throw new RegraNegocioException("Ano origem e destino não podem ser iguais");
			
		ClassificacaoCtbServiceDAO.replicarDadosAno(con.getConexao(), nroAnoOrigem, nroAnoDestino);
			
	}

	public void gerarPlanilhaCSV(String caminho) throws IOException, SQLException, NamingException {
		ClassificacaoCtbServiceDAO.gerarPlanilhaCSV(con.getConexao(), caminho);
	}

	public List<String> retornarContas(String consulta) throws SQLException {
		return ClassificacaoCtbServiceDAO.retornarContas(con.getConexao(), consulta);
	}

	public List<String> buscarClassificacao(String codConta) throws SQLException {
		return ClassificacaoCtbServiceDAO.buscarClassificacao(con.getConexao(), codConta);
	}
}
