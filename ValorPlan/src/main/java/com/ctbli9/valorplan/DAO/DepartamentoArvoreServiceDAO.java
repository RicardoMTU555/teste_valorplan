package com.ctbli9.valorplan.DAO;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.ctbli9.valorplan.modelo.ArvoreOrganizacional;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Departamento;
import com.ctbli9.valorplan.modelo.NivelArvore;
import com.ctbli9.valorplan.modelo.orc.Recurso;

import ctbli9.recursos.ArquivoTexto;
import ctbli9.recursos.LibUtil;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

public class DepartamentoArvoreServiceDAO {
	
	public static TreeNode criaArvore(Connection con, int nroAno, int codDepartamento) throws Exception {
		Statement stmt = con.createStatement();
		ResultSet res = null;
		String sql = null;
		
		/*
		 * Se veio o codDepartamento, o sistema irá deixar o mesmo expandido e selecionado 
		 */
		String codCompleto = "";
		if (codDepartamento > 0) {
			sql = String.format("select * from sp_BuscaDepartamento(%d)", codDepartamento);
			res = stmt.executeQuery(sql);
			if (res.next())
				codCompleto = "." + res.getString("cod_completo") + ".";
			res.close();
		}
		
		// SELECT 1: Departamento máximo da empresa
		sql = String.format("SELECT d.*, eq.* "
				+ "FROM sp_ListaDepartamento(%d, 0) d "
				+ EquipeServiceDAO.montaScriptRelacaoEquipe("d")
				+ "WHERE d.cdDepartamentoPai IS NULL",
				nroAno);
		
		res = stmt.executeQuery(sql);
		
		TreeNode raiz = null;
		Departamento depto = null;
		
		//1o. Elo Basico: raiz
		depto = new Departamento();
		depto.setSgDepartamento("EMPRESA");
		raiz = new DefaultTreeNode(depto, null);
		raiz.setExpanded((codDepartamento > 0));
	
		if (res.next()) {
			buscaNivel(con, res, raiz, codCompleto, nroAno);
		}
        
		res = null;
		stmt = null;
		
		return raiz;
	}

	
	public static TreeNode criaArvoreSemRaiz(Connection con, int nroAno) throws Exception {
		Statement stmt = con.createStatement();
		ResultSet res = null;
		String sql = null;
		
		TreeNode raiz = null;
		
		// SELECT 1: Departamento máximo da empresa
		sql = String.format("SELECT d.*, eq.* "
				+ "FROM sp_ListaDepartamento(%d, 0) d "
				+ EquipeServiceDAO.montaScriptRelacaoEquipe("d")
				+ "WHERE d.cdDepartamentoPai IS NULL",
				nroAno);
		
		res = stmt.executeQuery(sql);
		
		if (res.next()) {
			//1o. Elo Basico: raiz
			Departamento depto = DepartamentoServiceDAO.carregarDepartamento(con, res);
			raiz = new DefaultTreeNode(depto, null);
			buscaProximoNivel(con, raiz, nroAno);
		}
        
		res = null;
		stmt = null;
		
		return raiz;
	}
	
	
	private static void buscaProximoNivel(Connection con, TreeNode eloAnterior,  
			int nroAno) throws Exception {
		
		Departamento depto = (Departamento)eloAnterior.getData();
		
		TreeNode eloAtual = null;
		
		Statement stmt = con.createStatement();
		// SELECT 2
		String sql = null;
		
		sql = String.format("SELECT d.*, eq.* "
				+ "FROM sp_ListaDepartamento(%d, 0) d "
				+ EquipeServiceDAO.montaScriptRelacaoEquipe("d")
				+ "WHERE d.cdDepartamentoPai = %d", nroAno, depto.getCdDepartamento());
	    
		ResultSet res = stmt.executeQuery(sql);
		
		// Departamento não é pai de nenhum outro subdepartamento
		if (!res.next()) { // Lista centros de custo da organização
			// SELECT 3
			List<CentroCusto> listaCencus = listarCentrosCustoDepto(con, depto.getCdDepartamento(), false, false);
				
			for (CentroCusto cencus : listaCencus) {
				TreeNode eloSetor = new DefaultTreeNode("setor", cencus, eloAnterior);
				eloSetor.setExpanded(true);
			}
			
		} 
		
		res.close();
		res = stmt.executeQuery(sql);
		
		while (res.next()) {
			depto = DepartamentoServiceDAO.carregarDepartamento(con, res);
			eloAtual = new DefaultTreeNode(depto, eloAnterior);
			buscaProximoNivel(con, eloAtual, nroAno);  // RECURSIVIDADE: CHAMA A FUNÇÃO NOVAMENTE para o proximo elo
		}
		
		res = null;
		stmt = null;
		
	}
	
	private static void buscaNivel(Connection con, ResultSet resAnt, TreeNode eloAnterior, String codCompleto, 
			int nroAno) throws Exception {
		
		Departamento depto = DepartamentoServiceDAO.carregarDepartamento(con, resAnt);
		
		TreeNode eloAtual = null;
		
		Statement stmt = con.createStatement();
		// SELECT 2
		String sql = null;
		
		sql = String.format("SELECT d.*, eq.* "
				+ "FROM sp_ListaDepartamento(%d, 0) d "
				+ EquipeServiceDAO.montaScriptRelacaoEquipe("d")
				+ "WHERE d.cdDepartamentoPai = %d", nroAno, depto.getCdDepartamento());
	    
		ResultSet res = stmt.executeQuery(sql);
		
		// Departamento não é pai de nenhum outro subdepartamento
		if (!res.next()) { // Lista centros de custo da organização
			eloAtual = new DefaultTreeNode("ultimoNivel", depto, eloAnterior);
			eloAtual.setSelectable(true);
			eloAtual.setSelected(codCompleto.contains("." + depto.getCdDepartamento() + "."));	
			
		} else {
			eloAtual = new DefaultTreeNode(depto, eloAnterior);
			eloAtual.setExpanded(codCompleto.contains("." + depto.getCdDepartamento() + "."));
			if (!codCompleto.isEmpty()) {
				eloAtual.setSelected(false);
				eloAtual.setSelectable(false);
			}
		}
		
		res.close();
		res = stmt.executeQuery(sql);
		
		while (res.next()) {
			buscaNivel(con, res, eloAtual, codCompleto, nroAno);  // RECURSIVIDADE: CHAMA A FUNÇÃO NOVAMENTE para o proximo elo
		}
		
		res = null;
		stmt = null;
		
	}

	private static List<CentroCusto> listarCentrosCustoDepto(Connection con, long cdDepartamento, 
			boolean soProdutivo, boolean cencustoFunc) throws Exception {
		List<CentroCusto> lista = new ArrayList<CentroCusto>();
		
		Statement stmt = con.createStatement();
		
		String sql = CentroCustoServiceDAO.montaScriptCentroCusto() + 
				(cencustoFunc ? "JOIN TmpCencus ON TmpCencus.cdCentroCusto = cc.cdCentroCusto " : "") +
				String.format("WHERE dep.cdDepartamento = %d ",
						cdDepartamento);
		
		if (soProdutivo)
			sql += "AND cc.cecTpArea IN ('VE', 'SV')"; 
		
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			lista.add(CentroCustoServiceDAO.carregaCentroCusto(con, res, false));
		}
		
		res = null;
		stmt = null;
		
		return lista;
	}

	
	public static TreeNode criaEstrutura(Connection con, boolean mostraRecursos, boolean apenasProdutivo) 
			throws Exception {
		Statement stmt = con.createStatement();
		Statement stmt2 = con.createStatement();
		
		NivelArvore nivel = DepartamentoServiceDAO.getNivelArvore(con);
		
		List<String> listaDepto = DepartamentoServiceDAO.listaDepartamentosDoGestor(con);
		
		// 2o. Departamento máximo da empresa
		String sql = String.format("SELECT d.cdDepartamento "
				+ "FROM sp_ListaDepartamento(%d, 0) d "
				+ "WHERE d.cdDepartamentoPai IS NULL",
				PlanoServiceDAO.getPlanoSelecionado().getNrAno());
		ResultSet res = stmt.executeQuery(sql);
		
		Departamento depto = null;
		depto = new Departamento();
		depto.setSgDepartamento("EMPRESA");
		//1o. Elo Básico: raiz
		TreeNode raiz = new DefaultTreeNode(depto, null);

		while(res.next()) {
			
			if (listaDepto.size() > 0) {
				depto = DepartamentoServiceDAO.pesquisarDepartamento(con, res.getInt("cdDepartamento")); // TODO Mudar para carregar departamento
				
				//2o. Elo: departamentos "pai" ligados a raiz
				TreeNode elo = new DefaultTreeNode(depto, raiz);
				elo.setExpanded(true);
				
				//3o. Elo: departamentos "pais" ligados ao pai mais alto
				buscaNivelValido(con, nivel, res.getInt("cdDepartamento"), elo, listaDepto, mostraRecursos, apenasProdutivo);
			}
			
		}//while...
        
		res.close();
		stmt.close();
		stmt2.close();
		res = null;
		stmt = null;
		stmt2 = null;
		
		return raiz;
	}
	
	private static void buscaNivelValido(Connection con, NivelArvore nivel, int cdDepartamento, TreeNode eloAnterior, 
			List<String> listaDepto, boolean mostraRecursos, boolean apenasProdutivo) throws Exception {
		
		boolean deptoValido = false;
		for (String depAux : listaDepto) {
			if (depAux.contains("." + cdDepartamento + ".")) {
				deptoValido = true;
				break;
			}
		}
		if (!deptoValido)
			return;
		
		
		Statement stmt = con.createStatement();
		// SELECT 2
		String sql = null;
		
		sql = String.format("SELECT d1.cdDepartamento FROM EmpDepartamento d1 WHERE d1.cdDepartamentoPai = %d",
				cdDepartamento);
		
		ResultSet res = stmt.executeQuery(sql);
		
		if (!res.next()) {
			// SELECT 3
			List<CentroCusto> listaCencus = listarCentrosCustoDepto(con, cdDepartamento, apenasProdutivo, true);
			
			for (CentroCusto cencus : listaCencus) {
				TreeNode eloSetor = new DefaultTreeNode("setor", cencus, eloAnterior);
				eloSetor.setExpanded(true);
				
				if (mostraRecursos)
					listarRecursoSetor(con, cencus, eloSetor, apenasProdutivo);
			}
		}
		
		res.close();
		res = stmt.executeQuery(sql);
		
		while (res.next()) {
			int cdDeptoAux = res.getInt("cdDepartamento");
			
			for (String depAux : listaDepto) {
				if (depAux.contains("." + cdDeptoAux + ".")) {
					Departamento depto = DepartamentoServiceDAO.pesquisarDepartamento(con, cdDeptoAux);
					TreeNode elo = new DefaultTreeNode(depto, eloAnterior);
					elo.setExpanded((depto.getNivel() < nivel.getMaximo()));
					
					// RECURSIVIDADE: CHAMA A FUNÇÃO NOVAMENTE para o proximo elo
					buscaNivelValido(con, nivel, res.getInt("cdDepartamento"), elo, listaDepto, mostraRecursos, apenasProdutivo);
					break;
				}
			}
			
		}//while res
		
		res = null;
		stmt = null;
		
	}//buscaNivelValido

	
	private static void listarRecursoSetor(Connection con, CentroCusto cencus, TreeNode eloSetor, boolean apenasProdutivo) throws Exception {
		
		Statement stmt = con.createStatement();
		
		String sql = EquipeServiceDAO.montaScriptEquipe("") +
				String.format("WHERE eq.cdCentroCusto = %d "
				+ (apenasProdutivo ? "AND eq.tpRecurso = 'P'" : "")
				+ " ORDER BY eq.nomeVinculoAtual",
				cencus.getCdCentroCusto());
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			Recurso func = EquipeServiceDAO.carregaRecurso(res, null);
			
			new DefaultTreeNode("vendedor", func, eloSetor).setExpanded(true);
		}
		
		res = null;
		stmt = null;
	}


	/*
	 * Retorna objeto com centro de custo tabulado para agilizar seleção da árvore
	 */
	public static List<ArvoreOrganizacional> listarCentroCustoAgrupado(Connection con) throws SQLException, NamingException {
		NivelArvore nivel = DepartamentoServiceDAO.getNivelArvore(con);
		
		String sql = "SELECT cc.cdCentroCusto, arvore.* "
				+ "FROM EmpCentroCusto cc "
				+ "JOIN EmpCencusDepto cdep ON cc.cdCentroCusto = cdep.cdCentroCusto "
				+ "JOIN (" + DepartamentoServiceDAO.montaScriptArvore(nivel.getMaximo()) + 
				String.format(") arvore ON cdep.cdDepartamento = arvore.niv%02d ", nivel.getMaximo());
						
		List<ArvoreOrganizacional> setores = new ArrayList<ArvoreOrganizacional>();
		
		ArvoreOrganizacional setor = null;
		
		Statement stmt = con.createStatement();
		
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()) {
			setor = new ArvoreOrganizacional();
			setor.setCdCentroCusto(rs.getInt("cdCentroCusto"));
			
			String[] descricoes = new String[nivel.getMaximo()];
			
			for (int i = 0; i < descricoes.length; i++) {
				descricoes[i] = rs.getString(String.format("dep%02d", (i+1)));
			}
			setor.setDsDepto(descricoes);
			setores.add(setor);
		}
		
		rs.close();
		stmt.close();
		
		rs = null;
		stmt = null;
		
		return setores;
	}
/*
	// 2o. Achar o maior departamento do gerente
			sql = String.format("SELECT * FROM sp_ListaDepartamento(%d, 0) spd "
					+ EquipeServiceDAO.montaScriptRelacaoEquipe("spd")
					+ "WHERE spd.ID_Recurso IN (%s) "
					+ "AND nivel = (SELECT MIN(nivel) FROM sp_ListaDepartamento(%d, 0) spd WHERE spd.ID_Recurso IN (%s));", 
					PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
					listaRecurso,
					PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
					listaRecurso);
			res = stmt.executeQuery(sql);
			
			Departamento depto = null;
			depto = new Departamento();
			depto.setSgDepartamento("EMPRESA");
			//1o. Elo Básico: raiz
			TreeNode raiz = new DefaultTreeNode(depto, null);

			while(res.next()) {
				// 2o. Listar os departamentos FILHOS do departamento do gerente que sao do nivel maximo 
				//     (ligados a centros de custo) e que sao de vendas ou servicos
				sql = String.format("SELECT DISTINCT spd.cod_completo, spd.nivel "
					+ "FROM EmpCentroCusto cc "
					+ "JOIN EmpCencusDepto cdep ON cc.cdCentroCusto = cdep.cdCentroCusto "
					+ "JOIN sp_ListaDepartamento(%d, %d) spd ON cdep.cdDepartamento = spd.cdDepartamento "
					+ (produtivo ? "WHERE cc.cecTpArea IN ('VE', 'SV') AND EXISTS " : "WHERE EXISTS ")
					+ "(SELECT DISTINCT 1 FROM OrcEquipe eq WHERE eq.nrAno = %d AND eq.cdCentroCusto = cc.cdCentroCusto);", 
					PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
					res.getInt("cdDepartamento"),
					PlanoServiceDAO.getPlanoSelecionado().getNrAno());
				
				List<String> listaDepto = new ArrayList<String>();
				ResultSet res2 = stmt2.executeQuery(sql);
				while (res2.next()) {
					listaDepto.add("." + res2.getString("cod_completo").trim() + ".");
				}
				res2.close();
				res2 = null;
				
				if (listaDepto.size() > 0) {
					depto = DepartamentoServiceDAO.pesquisarDepartamento(con, res.getInt("cdDepartamento")); // TODO Mudar para carregar departamento
					
					//2o. Elo: departamentos "pai" ligados a raiz
					TreeNode elo = new DefaultTreeNode(depto, raiz);
					elo.setExpanded(true);
					
					//3o. Elo: departamentos "pais" ligados ao pai mais alto
					buscaNivelValido(con, res.getInt("cdDepartamento"), elo, listaDepto, mostraRecursos, produtivo);
				}
				
			}//while...
	        
			res.close();
			stmt.close();
			stmt2.close();
			res = null;
			stmt = null;
			stmt2 = null;
			
			return raiz;*/

	public static void gerarPlanilhaCSV(Connection con, String nomePlanilha) throws SQLException, FileNotFoundException {
		NivelArvore maxNivel = DepartamentoServiceDAO.getNivelArvore(con);
		
		Statement stmt = con.createStatement();
		// TODO colocar os CPFs
		String sql = String.format("SELECT arvore.*, "
				+ "cc.cecCdExterno, cc.cecDsResumida, func.nomeVinculo AS gestor_setor " + //, func.dcFuncionario AS cpf " +
				"FROM EmpCentroCusto cc " + 
				"JOIN EmpCencusDepto cdep ON cc.cdCentroCusto = cdep.cdCentroCusto " + 
				"LEFT OUTER JOIN sp_ListaRecurso(%d) func ON cdep.ID_Recurso = func.ID_Recurso " + 
				"JOIN ( " + DepartamentoServiceDAO.montaScriptArvore(maxNivel.getMaximo()) +
				") arvore ON arvore.niv%02d = cdep.cdDepartamento",
				new MesAnoOrcamento().getAnoMes(),
				maxNivel.getMaximo());
		
		ResultSet res = stmt.executeQuery(sql);
		
		ArquivoTexto arqCSV = new ArquivoTexto(nomePlanilha);
		arqCSV.abreGrava();
		
		boolean inicio = true;
		while (res.next()) {
			if (inicio) {
	    		arqCSV.imprimeLinha(LibUtil.nomeColunaCSV(res));
	    		inicio = false;
	    	}
	    	arqCSV.imprimeLinha(LibUtil.valorColunaCSV(res));
		}
		
		arqCSV.fechaGrava();
		arqCSV = null;
		
		
		res.close();
		stmt.close();
		
		res = null;
		stmt = null;
	}	
	
}
