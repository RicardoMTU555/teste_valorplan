package com.ctbli9.valorplan.negocio;

import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.TreeNode;

import ctbli9.modelo.FiltroContaContabil;
import ctbli9.modelo.ctb.ClasseDRE;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.LibUtil;
import ctbli9.recursos.RegraNegocioException;
import com.ctbli9.valorplan.DAO.ClassificacaoCtbServiceDAO;
import com.ctbli9.valorplan.DAO.ContaContabilServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.ctb.EstornoConta;
import com.ctbli9.valorplan.modelo.ctb.SelecionaConta;

public class ContaContabilService {
	private ConexaoDB con;
	
	public ContaContabilService(ConexaoDB con) {
		this.con = con;
	}

	public ContaContabil perquisar(String cdConta) throws Exception {
		return ContaContabilServiceDAO.pesquisarConta(con.getConexao(), cdConta);
	}
	
	public List<ContaContabil> listar(FiltroContaContabil filtro) throws Exception {
		return ContaContabilServiceDAO.listar(con.getConexao(), filtro);
	}
	
	public boolean existe(String cdConta) throws Exception {
		return ContaContabilServiceDAO.existe(con.getConexao(), cdConta);
	}
	
	public void salvar(ContaContabil conta) throws RegraNegocioException, Exception {
		if (conta.getDsConta().trim().isEmpty())
			throw new RegraNegocioException("Descrição da conta é obrigatória.");
		
		if (ContaContabilServiceDAO.existe(con.getConexao(), conta.getCdConta()))
			ContaContabilServiceDAO.alterar(con.getConexao(), conta);
		else
			ContaContabilServiceDAO.incluir(con.getConexao(), conta);
	}
	
	public void incluir(ContaContabil conta) throws Exception {
		ContaContabilServiceDAO.incluir(con.getConexao(), conta);
	}
	
	public void excluir(ContaContabil conta) throws Exception {
		if (ContaContabilServiceDAO.existe(con.getConexao(), conta.getCdConta()))
			ContaContabilServiceDAO.excluir(con.getConexao(), conta);
	}
	
	//---------------------------------------------------------------------------------------\\
	public List<EstornoConta> listarEstornoConta() throws Exception {
		return ContaContabilServiceDAO.listarEstornoConta(con.getConexao());
	}
	
	public void excluirEstornoConta(EstornoConta conta) throws SQLException, NamingException {
		ContaContabilServiceDAO.excluirEstornoConta(con.getConexao(), conta);
	}
	
	public void gravarEstornoConta(List<EstornoConta> listaContas) throws Exception {
			
		// 1o. Remove excluidos
		List<EstornoConta> listaAux = ContaContabilServiceDAO.listarEstornoConta(con.getConexao()); // Lista registros do banco
		
		for (EstornoConta contaAux : listaAux) {
			boolean achou = false;
			for (EstornoConta aux : listaContas) {
				if (contaAux.equals(aux)) {
					achou = true;
					break;
				}
			}
			if (!achou)
				ContaContabilServiceDAO.excluirEstornoConta(con.getConexao(), contaAux);
		}
		
		// 2o. Grava adicionados e alterados
		for (EstornoConta aux : listaContas) {
			ContaContabilServiceDAO.gravarEstornoConta(con.getConexao(), aux);
		}
	}
	
	
	public TreeNode criarArvorePlaconta(ClasseDRE classeConta) throws Exception {
		List<ContaContabil> lista = ContaContabilServiceDAO.listar(con.getConexao(), new FiltroContaContabil());
		
		List<SelecionaConta> selecionadas = ClassificacaoCtbServiceDAO
				.lisarContasSelecionadas(con.getConexao(), classeConta);
		
		SelecionaConta contaRaiz = new SelecionaConta();
		TreeNode raiz = new CheckboxTreeNode(contaRaiz, null);
        raiz.setExpanded(true);
        
        SelecionaConta conta1 = new SelecionaConta();
        conta1.setCdConta("Plano de Contas");
        TreeNode plaConta = new CheckboxTreeNode(conta1, raiz);
        plaConta.setExpanded(true);
        
        
        // Acha o maior nível para instanciar o array
        int maxNivel = 0;
        for (ContaContabil conta : lista) {
			maxNivel = (conta.getNivel() > maxNivel) ? conta.getNivel() : maxNivel;
		}
        
        // Instancia o array
        TreeNode[] menu = new TreeNode[maxNivel];
        
        boolean primeiroSelecionado = false;
        // Popula o array
        for (ContaContabil conta : lista) {
        	 SelecionaConta novaConta = (SelecionaConta) LibUtil.clonaObjeto(conta, SelecionaConta.class);

        	int numNivel = conta.getNivel();
        	if (numNivel < maxNivel) {
        		if (numNivel == 1)
            		menu[numNivel-1] = new CheckboxTreeNode(novaConta, plaConta);
            	else
            		menu[numNivel-1] = new CheckboxTreeNode(novaConta, menu[numNivel-2]);
        		
        		menu[numNivel-1].setExpanded(false);
        		menu[numNivel-1].setSelectable(false);
        	} else {
        		TreeNode elo = new CheckboxTreeNode("analitica", novaConta, menu[numNivel-2]);
        		elo.setSelectable(true);
        		if (selecionadas.indexOf(novaConta) > -1) {
        			novaConta.setSeleciona(true);
            		
        			if (!primeiroSelecionado) {
            			elo.setSelected(true);
            			primeiroSelecionado = true;
            		}
        			
        			expande(elo);
        			
        		} else {
        			novaConta.setSeleciona(false);
        			elo.setExpanded(false);
        		}
        	}	
        	
        }
        lista = null;
        
        return raiz;
    }

	private void expande(TreeNode elo) {
		if (elo == null)
			return;
		
		elo.setExpanded(true);
		expande(elo.getParent());
	}
	
}
