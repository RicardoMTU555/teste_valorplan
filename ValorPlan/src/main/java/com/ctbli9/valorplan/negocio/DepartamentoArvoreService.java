package com.ctbli9.valorplan.negocio;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.DAO.DepartamentoArvoreServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;

// REGINALDO: OUTRA FORMA DE INSTANCIAR CLASSES DE SERVIÇO
@ManagedBean(name = "documentService")
@ApplicationScoped
public class DepartamentoArvoreService {
    private ConexaoDB con;
    
    public DepartamentoArvoreService(ConexaoDB con) {
		this.con = con;
	}

	public TreeNode montaArvoreEmpresa(int nroAno, int codDepartamento) throws Exception {
		return DepartamentoArvoreServiceDAO.criaArvore(con.getConexao(), nroAno, codDepartamento);
	}

	public TreeNode montaArvoreEmpresaSemRaiz(int nroAno) throws Exception {
		return DepartamentoArvoreServiceDAO.criaArvoreSemRaiz(con.getConexao(), nroAno);
	}

	public TreeNode arvoreSetoresDoGestor(boolean mostraRecursos, boolean ApenasProdutivo) 
			throws Exception {
        return DepartamentoArvoreServiceDAO.criaEstrutura(con.getConexao(), mostraRecursos, ApenasProdutivo);
	}

	public void gerarPlanilhaCSV(String caminho) throws FileNotFoundException, SQLException  {
		DepartamentoArvoreServiceDAO.gerarPlanilhaCSV(con.getConexao(), caminho);
	}
    
}