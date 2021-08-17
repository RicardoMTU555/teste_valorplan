package com.ctbli9.valorplan.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.Departamento;
import com.ctbli9.valorplan.modelo.FiltroDepartamento;
import com.ctbli9.valorplan.modelo.NivelArvore;
import com.ctbli9.valorplan.recursos.Global;

import com.ctbli9.valorplan.recursos.MesAnoOrcamento;
import ctbli9.recursos.RegraNegocioException;

public class DepartamentoServiceDAO {
	
	public static Departamento pesquisarDepartamento(Connection con, long cdDepartamento) throws Exception {
		Statement stmt = con.createStatement();

		Departamento depto = null;	
		String sql = String.format("SELECT d.*, eq.* "
				+ "FROM sp_BuscaDepartamento(%d) d ", cdDepartamento);
		sql += EquipeServiceDAO.montaScriptRelacaoEquipe("d");
		
		ResultSet rs = stmt.executeQuery(sql);
	    if(rs.next() && rs.getInt("cdDepartamento") > 0)
	    	depto = carregarDepartamento(con, rs);
		
		stmt.close();
		stmt = null;
		rs.close();
		rs = null;
		
		return depto;
	}//pesquisarDepartamento

	public static boolean existe(Connection con, long cdDepartamento) throws SQLException, NamingException {
		Statement stmt = con.createStatement();

		boolean retorno = false;		
		
		String sql = String.format("select 1 from EmpDepartamento dp where dp.cdDepartamento = %d", 
				cdDepartamento
				);
	    ResultSet rs = stmt.executeQuery(sql);
	    retorno = rs.next();
		
		stmt.close();
		stmt = null;
		rs.close();
		rs = null;
		
		return retorno;
	}

	protected static Departamento carregarDepartamento(Connection con, ResultSet rs) throws Exception {
		Departamento depto = new Departamento();
    	
    	depto.setCdDepartamento(rs.getInt("cdDepartamento"));
    	depto.setNrAno(rs.getInt("nrAno"));
    	depto.setDsDepartamento(rs.getString("dsDepartamento"));

    	depto.setResponsavel(EquipeServiceDAO.carregaRecurso(rs, null));
    	depto.setSgDepartamento(rs.getString("sgdepartamento"));
    	depto.setCodCompleto(rs.getString("cod_completo")); 	
    	
    	depto.setDepartamentoPai(pesquisarDepartamento(con, rs.getInt("cdDepartamentoPai")));
    	
    	depto.setNivel(rs.getInt("nivel"));
    	
		return depto;
	}
	
	// **********************************************************************
	
	public static void incluir(Connection con, Departamento depto) throws SQLException, NamingException {
		depto.setCdDepartamento(ConexaoDB.gerarNovoCodigo(con, "EmpDepartamento", "cdDepartamento", 
				"nrAno = " + depto.getNrAno()));
		
		if (depto.getCdDepartamento() == 1)
			depto.setCdDepartamento(Long.parseLong(String.format("%04d001", depto.getNrAno())));
		
		String sql = "INSERT INTO EmpDepartamento VALUES (?, ?, ?, ?, ?, ?)";
		
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setLong(++i, depto.getCdDepartamento());
		pstmt.setInt(++i, depto.getNrAno());
		pstmt.setString(++i, depto.getSgDepartamento());
		pstmt.setString(++i, depto.getDsDepartamento());
		
		if (depto.getDepartamentoPai() == null || depto.getDepartamentoPai().getCdDepartamento() == 0)
			pstmt.setNull(++i,java.sql.Types.BIGINT);
		else
			pstmt.setLong(++i,depto.getDepartamentoPai().getCdDepartamento());
		
		if (depto.getResponsavel() == null || depto.getResponsavel().getCdRecurso() == 0)
			pstmt.setNull(++i, java.sql.Types.BIGINT);
		else
			pstmt.setLong(++i,depto.getResponsavel().getCdRecurso());

		pstmt.executeUpdate();
		
		pstmt.close();
	}//incluir
	
	public static void alterar(Connection con, Departamento depto) throws SQLException, NamingException {
		String sql = "UPDATE EmpDepartamento SET "
				+ "sgDepartamento = ?, "
				+ "dsDepartamento = ?, "
				+ "cdDepartamentoPai = ?, "
				+ "ID_Recurso = ? "
				+ " WHERE cdDepartamento = ?";
		
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setString(++i, depto.getSgDepartamento());
		pstmt.setString(++i, depto.getDsDepartamento());
		
		if (depto.getDepartamentoPai() == null || depto.getDepartamentoPai().getCdDepartamento() == 0)
			pstmt.setNull(++i,java.sql.Types.INTEGER);
		else
			pstmt.setLong(++i,depto.getDepartamentoPai().getCdDepartamento());
		
		if (depto.getResponsavel() == null || depto.getResponsavel().getCdRecurso() == 0)
			pstmt.setNull(++i, java.sql.Types.BIGINT);
		else
			pstmt.setLong(++i,depto.getResponsavel().getCdRecurso());

		
		pstmt.setLong(++i, depto.getCdDepartamento());
		
		pstmt.executeUpdate();
		
		pstmt.close();
	}//Alterar

	public static void excluir(Connection con, Departamento depto) throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		String sql = String.format(Locale.US,
				"DELETE FROM EmpDepartamento WHERE cdDepartamento = %d", 
				depto.getCdDepartamento()
				);			
		stmt.executeUpdate(sql);
		
		stmt.close();
		stmt = null;
	}//excluir
	
	
	public static List<Departamento> listar(Connection con, FiltroDepartamento filtro) 
			throws Exception {
		List<Departamento> deptos = new ArrayList<Departamento>();
		
		String sql = String.format("SELECT dp.*, eq.* "
				+ "FROM sp_ListaDepartamento(%d, 0) dp ",
				filtro.getNroAno());
		
		sql += EquipeServiceDAO.montaScriptRelacaoEquipe("dp");
		
		if (filtro.getDeptoPai() == 0)
			sql += " WHERE dp.cdDepartamentoPai is null ";
		else
			sql += " WHERE dp.cdDepartamentoPai = " + filtro.getDeptoPai() + " ";
	    
		if (!filtro.getNomeFunc().isEmpty())
			sql += " AND nomeVinc.nmFuncionario = ? ";
		
		sql += " AND dp.dsDepartamento LIKE ? "
				+ " AND dp.sgDepartamento LIKE ? ";

		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		if (!filtro.getNomeFunc().isEmpty())
			pstmt.setString(++i, filtro.getNomeFunc());
		
		pstmt.setString(++i, filtro.getDescricao() + "%");
		pstmt.setString(++i, filtro.getSigla() + "%");
		
		ResultSet rs = pstmt.executeQuery();
		
	    while (rs.next()) {
	    	deptos.add(carregarDepartamento(con, rs));
	    }
		
		pstmt.close();
		pstmt = null;
		rs.close();
		rs = null;
		return deptos;
	}

	
	public static NivelArvore getNivelArvore(Connection con) throws SQLException {
		MesAnoOrcamento referencia = new MesAnoOrcamento();
		
		Statement stmt = con.createStatement();
		NivelArvore retorno = new NivelArvore();
		
		String sql = String.format("SELECT MAX(nivel) AS maxNivel FROM sp_ListaDepartamento(%d, 0)",
				referencia.getAno());
		
	    ResultSet rs = stmt.executeQuery(sql);
	    if(rs.next())
	    	retorno.setMaximo(rs.getInt("maxNivel"));
		
	    rs.close();
	    
		sql = String.format("SELECT min(nivel) as minNivel FROM sp_listaDepartamento(%d,0) " + 
				"WHERE ID_Recurso IN " +
				"( SELECT ID_Recurso " + 
				"    FROM OrcEquipeRecurs " + 
				"   WHERE cdFuncVinculo = %d " + 
				"     AND nrAnoMesInicial <= %d " + 
				"     AND (nrAnoMesFinal IS NULL OR nrAnoMesFinal >= %d)) " + 
				"     AND nrAno = %d;",
				referencia.getAno(),
				Global.getFuncionarioLogado().getCdFuncionario(),
				referencia.getAnoMes(),
				referencia.getAnoMes(),
				referencia.getAno());
		
	    rs = stmt.executeQuery(sql);
	    if(rs.next()) {
	    	retorno.setMinimo(rs.getInt("minNivel"));
	    	retorno.setGestor('D');
	    }

	    if (retorno.getMinimo() == 0) {
	    	sql = String.format("SELECT distinct 1 FROM EmpCencusDepto cdep " +
	    			"JOIN EmpDepartamento d ON cdep.cdDepartamento = d.cdDepartamento AND d.nrAno = %d " +
					"WHERE cdep.ID_Recurso IN " +
					"( SELECT ID_Recurso " + 
					"    FROM OrcEquipeRecurs " + 
					"   WHERE cdFuncVinculo = %d " + 
					"     AND nrAnoMesInicial <= %d " + 
					"     AND (nrAnoMesFinal IS NULL OR nrAnoMesFinal >= %d)) " + 
					"     AND nrAno = %d;",
					referencia.getAno(),
					Global.getFuncionarioLogado().getCdFuncionario(),
					referencia.getAnoMes(),
					referencia.getAnoMes(),
					referencia.getAno());
			
		    rs = stmt.executeQuery(sql);
		    if(rs.next()) {
		    	retorno.setMinimo(retorno.getMaximo());
		    	retorno.setGestor('C');
		    }
	    }
		
	    if (retorno.getMinimo() > 0) {
	    	retorno.setMinimo(1);		// Forçao mínimo ser o departamento raiz da empresa, pra efeito de soma nos relatórios de DRE
	    }
	    
		stmt.close();
		stmt = null;
		rs.close();
		rs = null;

		return retorno;
	}

	public static void replicarDadosAno(Connection con, int nroAnoOrigem, int nroAnoDestino) 
			throws SQLException, NamingException, RegraNegocioException {
		Statement stmt = con.createStatement();

		String sql = String.format("SELECT distinct 1 FROM EmpDepartamento d WHERE d.nrAno = %d ", nroAnoDestino);
		ResultSet rs = stmt.executeQuery(sql);
		if (rs.next()) {
			throw new RegraNegocioException("Ano DESTINO informado já possui dados na base. Replicação não permitida.");
		}
		rs.close();
		rs = null;
		
		
		sql = String.format("INSERT INTO OrcEquipe " + 
				"SELECT "
				+ "  %d || substring(ID_Recurso from 5 for 3), "
				+ "  %d, "
				+ "  cdCentroCusto, nmRecurso, cdCargo, tpRecurso " + 
				"FROM OrcEquipe WHERE nrAno = %d ORDER BY cdCentroCusto, ID_Recurso;", 
				nroAnoDestino,
				nroAnoDestino,
				nroAnoOrigem);
		stmt.executeUpdate(sql);
		
		sql = String.format("INSERT INTO OrcEquipeRecurs " + 
				"SELECT "
				+ "  %d || substring(ID_Recurso from 5 for 3), "
				+ "  %d || substring(nrAnoMesInicial from 5 for 2), "
				+ "  nrAnoMesFinal, cdFuncVinculo " + 
				"FROM OrcEquipeRecurs WHERE ID_Recurso IN (SELECT ID_Recurso FROM OrcEquipe WHERE nrAno = %d)", 
				nroAnoDestino,
				nroAnoDestino,
				nroAnoOrigem);
		stmt.executeUpdate(sql);
		
		sql = "INSERT INTO EmpDepartamento VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		sql = String.format("SELECT d.*, %d || substring(ID_Recurso from 5 for 3) AS novoRecurso "
				+ "FROM EmpDepartamento d "
				+ "WHERE d.nrAno = %d "
				+ "ORDER BY d.cdDepartamento, d.cdDepartamentoPai", 
				nroAnoDestino,
				nroAnoOrigem);
		
		rs = stmt.executeQuery(sql);
		
		while (rs.next()) {
			String novoCodigo = rs.getString("cdDepartamento");
			novoCodigo = nroAnoDestino + novoCodigo.substring(4, 7);
			
			String novoCodigoPai = rs.getString("cdDepartamentoPai");
			if (novoCodigoPai != null)
				novoCodigoPai = nroAnoDestino + novoCodigoPai.substring(4, 7);
			
			
			int i = 0;
			pstmt.setLong(++i, Long.parseLong(novoCodigo));
			pstmt.setInt(++i, nroAnoDestino);
			pstmt.setString(++i, rs.getString("sgDepartamento"));
			pstmt.setString(++i, rs.getString("dsDepartamento"));
			
			if (novoCodigoPai == null)
				pstmt.setNull(++i, java.sql.Types.BIGINT);
			else
				pstmt.setLong(++i, Long.parseLong(novoCodigoPai));
			
			if (rs.getInt("ID_Recurso") == 0)
				pstmt.setNull(++i, java.sql.Types.BIGINT);
			else 
				pstmt.setLong(++i, rs.getLong("novoRecurso"));
			
			pstmt.executeUpdate();
			
		}//while		
		
		pstmt.close();
		pstmt = null;
		rs.close();
		rs = null;
		
		
		sql = "INSERT INTO EmpCencusDepto VALUES (?, ?, ?)";
		pstmt = con.prepareStatement(sql);
		
		sql = String.format("SELECT cdep.*, %d || substring(cdep.ID_Recurso from 5 for 3) AS novoRecurso "
				+ "FROM EmpDepartamento d "
				+ "JOIN EmpCencusDepto cdep ON d.cdDepartamento = cdep.cdDepartamento "
				+ "WHERE d.nrAno = %d "
				+ "ORDER BY cdep.cdCentroCusto, cdep.cdDepartamento", 
				nroAnoDestino,
				nroAnoOrigem);
		
		rs = stmt.executeQuery(sql);
		
		while (rs.next()) {
			String novoCodigo = rs.getString("cdDepartamento");
			novoCodigo = nroAnoDestino + novoCodigo.substring(4, 7);
			
			int i = 0;
			pstmt.setLong(++i, Long.parseLong(novoCodigo));
			pstmt.setInt(++i, rs.getInt("cdCentroCusto"));
			if (rs.getInt("ID_Recurso") == 0)
				pstmt.setNull(++i, java.sql.Types.BIGINT);
			else 
				pstmt.setLong(++i, rs.getLong("novoRecurso"));
				
			pstmt.executeUpdate();
		}
		
		pstmt.close();
		pstmt = null;
		
		stmt.close();
		stmt = null;
		
		rs.close();
		rs = null;
	}
	
	public static List<Departamento> listarAreas(Connection con, int nivelArea) 
			throws SQLException, NamingException, RegraNegocioException {
		List<Departamento> lista = new ArrayList<Departamento>(); 
		
		NivelArvore nivel = getNivelArvore(con);
		
		List<String> listaDepto = DepartamentoServiceDAO.listaDepartamentosDoGestor(con);
		
		String sql = String.format("select distinct niv%02d as codigo, dep%02d as descr from " + 
				"(" + montaScriptArvore(nivel.getMaximo()) +
				") as arvore", 
				(nivelArea == 0 ? nivel.getMaximo() : nivelArea), 
				(nivelArea == 0 ? nivel.getMaximo() : nivelArea)
				);
		
		Statement stmt = con.createStatement();		
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			boolean deptoValido = false;
			for (String depAux : listaDepto) {
				if (depAux.contains("." + res.getLong("codigo") + ".")) {
					deptoValido = true;
					break;
				}
			}
			
			if (deptoValido) {
				Departamento depto = new Departamento();
				depto.setCdDepartamento(res.getLong("codigo"));
				depto.setSgDepartamento(res.getString("descr"));
				
				lista.add(depto);
			}
		}
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		
		return lista;
	}
	
	public static List<String> listaDepartamentosDoGestor(Connection con) 
			throws RegraNegocioException, SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		List<String> listaDepto = new ArrayList<String>();
		
		// Pegar todos os departamentos dos centros de custo do funcionário
		if (!CentroCustoServiceDAO.montarCentrosCustoResponsavel(con, Global.getFuncionarioLogado()))
			CentroCustoServiceDAO.montarCentrosCustoGerente(con, Global.getFuncionarioLogado(), null);
		
		String sql = String.format("SELECT cod_completo FROM sp_ListaDepartamento (%d,0) spd "
				+ "JOIN EmpCencusDepto cdep ON spd.cdDepartamento = cdep.cdDepartamento "
				+ "JOIN TmpCencus ON TmpCencus.cdCentroCusto = cdep.cdCentroCusto;", 
				PlanoServiceDAO.getPlanoSelecionado().getNrAno());
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next())
			listaDepto.add("." + res.getString("cod_completo").trim() + ".");
		
		if (listaDepto.size() == 0)
			throw new RegraNegocioException("Funcionário " + Global.getFuncionarioLogado().getNmFuncionario() + 
					" não está vinculado a nenhum recurso");
		
		return listaDepto;
	}
	
	/*
	 * 
	 */
	public static String montaScriptArvore(int nivel) {
		MesAnoOrcamento mesRef = new MesAnoOrcamento();
		
		String script = 		
				  "SELECT d01.cdDepartamento AS niv01, d01.dsDepartamento AS dep01, "
				  + "d01.ID_Recurso AS ger01"; //, f01.nomeVinculo AS func01, f01.cpfVinculo AS cpf01";
				
				for (int i = 2; i <= nivel; i++) {
					script += ", ";
					// Código
					for (int j = i; j > 1; j--) {
						script += String.format("COALESCE(d%02d.cdDepartamento, ", j);
					}
					script += String.format("d%02d.cdDepartamento", 1);
					for (int j = i; j > 1; j--) {
						script += ")";
					}
					script += String.format(" AS niv%02d, ", i);
					
					// Descrição
					for (int j = i; j > 1; j--) {
						script += String.format("COALESCE(d%02d.dsDepartamento, ", j);
					}
					script += String.format("d%02d.dsDepartamento", 1);
					for (int j = i; j > 1; j--) {
						script += ")";
					}
					script += String.format(" AS dep%02d, ", i);
					
					// Código do funcionário
					for (int j = i; j > 1; j--) {
						script += String.format("COALESCE(d%02d.ID_Recurso, ", j);
					}
					script += String.format("d%02d.ID_Recurso", 1);
					for (int j = i; j > 1; j--) {
						script += ")";
					}
					script += String.format(" AS ger%02d ", i);

					/*// Nome do funcionário
					for (int j = i; j > 1; j--) {
						script += String.format("COALESCE(f%02d.nomeVinculo, ", j);
					}
					script += String.format("f%02d.nomeVinculo", 1);
					for (int j = i; j > 1; j--) {
						script += ")";
					}
					script += String.format(" AS func%02d, ", i);

					// CPF do funcionário
					for (int j = i; j > 1; j--) {
						script += String.format("COALESCE(f%02d.cpfVinculo, ", j);
					}
					script += String.format("f%02d.cpfVinculo", 1);
					for (int j = i; j > 1; j--) {
						script += ")";
					}
					script += String.format(" AS cpf%02d ", i);
					*/
				}
				
				script += " FROM EmpDepartamento d01 ";
						//+ "LEFT OUTER JOIN sp_listarecurso(" + mesRef.getAnoMes() + ") f01 ON d01.ID_Recurso = f01.ID_Recurso ";
				
				for (int i = 2; i <= nivel; i++) {					  
					script += String.format("LEFT OUTER JOIN EmpDepartamento d%02d ON d%02d.cdDepartamento = d%02d.cdDepartamentoPai ", 
							i, i-1, i);
					//script += String.format("LEFT OUTER JOIN sp_listarecurso(%d) f%02d ON d%02d.ID_Recurso = f%02d.ID_Recurso ", 
					//		mesRef.getAnoMes(), i, i, i);
				}
				script += String.format("WHERE d01.nrAno = %04d AND d01.cdDepartamentoPai IS NULL ", 
						mesRef.getAno());
				
				return script;
	}
	
	
	public static void main(String[] args) {
		System.out.println("gerando árvore no nível 4");
		System.out.println(montaScriptArvore(4));
		System.out.println("árvore gerada");
	}
}
