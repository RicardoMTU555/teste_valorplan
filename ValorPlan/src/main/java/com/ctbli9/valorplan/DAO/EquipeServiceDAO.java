package com.ctbli9.valorplan.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.enumeradores.TipoRecurso;
import com.ctbli9.valorplan.modelo.Cargo;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.modelo.orc.Recurso;

import ctbli9.enumeradores.AreaSetor;
import ctbli9.enumeradores.TipoCargo;
import ctbli9.modelo.Filial;
import ctbli9.recursos.LibUtil;

import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

public class EquipeServiceDAO {

	public static void incluir(Connection con, Recurso recurso) throws SQLException, NamingException {
		recurso.setCdRecurso(ConexaoDB.gerarNovoCodigo(con, "OrcEquipe", "ID_Recurso", 
				"nrAno = " + recurso.getNrAno()));
		
		if (recurso.getCdRecurso() == 1)
			recurso.setCdRecurso(Long.parseLong(String.format("%04d001", recurso.getNrAno())));
		
		String sql = "INSERT INTO OrcEquipe VALUES (?, ?, ?, ?, ?, ?)";
		
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setLong(++i, recurso.getCdRecurso());
		pstmt.setInt(++i, recurso.getNrAno());
		pstmt.setInt(++i, recurso.getSetor().getCdCentroCusto());
		pstmt.setString(++i, recurso.getNmRecurso());
		pstmt.setInt(++i, recurso.getCargo().getCdCargo());
		pstmt.setString(++i, recurso.getTipo().toString());
		
		pstmt.executeUpdate();
		
		pstmt.close();
		
		vincularRecurso(con, recurso);
	}
	
	public static void alterar(Connection con, Recurso recurso) throws SQLException, NamingException {
		
		// TODO Verificar se o recurso já está vinculado ou se está sendo utilizado no orçamento
		String sql = "UPDATE OrcEquipe SET "
				+ "nmRecurso = ?, "
				+ "cdCargo = ?, "
				+ "tpRecurso = ? "
				+ "WHERE ID_Recurso = ?";
		
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setString(++i, recurso.getNmRecurso());
		pstmt.setInt(++i, recurso.getCargo().getCdCargo());
		pstmt.setString(++i, recurso.getTipo().toString());
		
		pstmt.setLong(++i, recurso.getCdRecurso());
		
		pstmt.executeUpdate();
		
		pstmt.close();
		
		vincularRecurso(con, recurso);
	}
	
	public static void excluir(Connection con, Recurso recurso) throws SQLException {
		String sql = String.format("DELETE FROM OrcEquipeRecurs "
				+ "WHERE ID_Recurso = %d",
				recurso.getCdRecurso());
			
		Statement stmt = con.createStatement();
		stmt.executeUpdate(sql);
		
		stmt.close();
		stmt = null;
		
		sql = String.format("DELETE FROM OrcEquipe "
				+ "WHERE ID_Recurso = %d",
				recurso.getCdRecurso());
			
		stmt = con.createStatement();
		stmt.executeUpdate(sql);
		
		stmt.close();
		stmt = null;
		
	}
	
	private static void vincularRecurso(Connection con, Recurso recurso) throws SQLException {
		String sql = null;
		Statement stmt = con.createStatement();
		ResultSet res = null;
		MesAnoOrcamento referencia = new MesAnoOrcamento();
					
		recurso.setInicioVinculo(referencia.getAnoMes());
		
		sql = String.format("SELECT nrAnoMesInicial FROM OrcEquipeRecurs "
				+ "WHERE ID_Recurso = %d AND nrAnoMesInicial < %d AND nrAnoMesFinal IS NULL",
				recurso.getCdRecurso(),
				recurso.getInicioVinculo());
		res = stmt.executeQuery(sql);
		
		// Se achou recurso em mes inicial menor que inicio (JÁ TEM VINCULO COM OUTRO FUNCIONARIO)
		if (res.next()) {
			// Fecha o recurso encontrado
			recurso.setInicioVinculo(res.getInt("nrAnoMesInicial"));
			recurso.setFimVinculo(referencia.getMesAnterior());
			fecharVinculo(con, recurso);
			
			if (recurso.getVinculo() != null && recurso.getVinculo().getCdFuncionario() > 0) {
				// Inclui o novo recurso
				recurso.setInicioVinculo(referencia.getAnoMes());
				recurso.setFimVinculo(0);
				incluirVinculo(con, recurso);
			}
			
		} else {
			sql = String.format("SELECT nrAnoMesInicial FROM OrcEquipeRecurs "
					+ "WHERE ID_Recurso = %d AND nrAnoMesInicial = %d AND nrAnoMesFinal IS NULL",
					recurso.getCdRecurso(),
					recurso.getInicioVinculo());
			res = stmt.executeQuery(sql);
			// Se achou recurso em mes inicial igual ao inicio (FOI VINCULADO NO MÊS CORRENTE)
			if (res.next()) {
				if (recurso.getVinculo() != null && recurso.getVinculo().getCdFuncionario() > 0)
					alterarVinculo(con, recurso);
				else
					excluirVinculo(con, recurso);	
			} else {
				if (recurso.getVinculo() != null && recurso.getVinculo().getCdFuncionario() > 0)
					incluirVinculo(con, recurso);
			}
		}
		
		res.close();
		res = null;
		stmt.close();
		stmt = null;
	}

	private static void incluirVinculo(Connection con, Recurso recurso) throws SQLException {
		String sql = "INSERT INTO OrcEquipeRecurs VALUES (?, ?, ?, ?)";
			
		PreparedStatement pstmt = con.prepareStatement(sql);
			
		int i = 0;
		pstmt.setLong(++i, recurso.getCdRecurso());
		pstmt.setInt(++i, recurso.getInicioVinculo());
		if (recurso.getFimVinculo() == null)
			pstmt.setNull(++i,java.sql.Types.INTEGER);
		else
			pstmt.setInt(++i, recurso.getFimVinculo());
		pstmt.setInt(++i, recurso.getVinculo().getCdFuncionario());

		pstmt.executeUpdate();
		
		pstmt.close();
		
		pstmt = null;
	}

	private static void alterarVinculo(Connection con, Recurso recurso) throws SQLException {
		
		String sql = "UPDATE OrcEquipeRecurs SET "
				+ "nrAnoMesFinal = ?, "
				+ "cdFuncVinculo = ? "
				+ "WHERE ID_Recurso = ? AND nrAnoMesInicial = ?";
			
		PreparedStatement pstmt = con.prepareStatement(sql);
			
		int i = 0;
		if (recurso.getFimVinculo() == null)
			pstmt.setNull(++i,java.sql.Types.INTEGER);
		else
			pstmt.setInt(++i, recurso.getFimVinculo());
		pstmt.setInt(++i, recurso.getVinculo().getCdFuncionario());

		pstmt.setLong(++i, recurso.getCdRecurso());
		pstmt.setInt(++i, recurso.getInicioVinculo());

		pstmt.executeUpdate();
		
		pstmt.close();
		
		pstmt = null;
	}
	
private static void fecharVinculo(Connection con, Recurso recurso) throws SQLException {
		
		String sql = "UPDATE OrcEquipeRecurs SET "
				+ "nrAnoMesFinal = ? "
				+ "WHERE ID_Recurso = ? AND nrAnoMesInicial = ?";
			
		PreparedStatement pstmt = con.prepareStatement(sql);
			
		int i = 0;
		pstmt.setInt(++i, recurso.getFimVinculo());
		
		pstmt.setLong(++i, recurso.getCdRecurso());
		pstmt.setInt(++i, recurso.getInicioVinculo());

		pstmt.executeUpdate();
		
		pstmt.close();
		
		pstmt = null;
	}

	private static void excluirVinculo(Connection con, Recurso recurso) throws SQLException {
		
		String sql = "DELETE FROM OrcEquipeRecurs "
				+ "WHERE ID_Recurso = ? AND nrAnoMesInicial = ?";
			
		PreparedStatement pstmt = con.prepareStatement(sql);
			
		int i = 0;
		pstmt.setLong(++i, recurso.getCdRecurso());
		pstmt.setInt(++i, recurso.getInicioVinculo());
	
		pstmt.executeUpdate();
		
		pstmt.close();
		
		pstmt = null;
	}

	public static Recurso getRecurso(Connection con, long idRecurso) throws Exception {
		String sql = String.format("SELECT * FROM sp_BuscaRecurso(%d, %d) eq ", new MesAnoOrcamento().getAnoMes(), idRecurso);
		return executarGetRecurso(con, sql);
	}
	
	public static Recurso getRecursoVinculado(Connection con, int cdFuncionario) throws Exception {
		
		String sql = montaScriptEquipe("") +
				String.format(" WHERE eq.codFuncVinculo = %d", cdFuncionario);
		return executarGetRecurso(con, sql);
	}
	

	public static Recurso pesquisarPorDescricao(ConexaoDB con, String novoRecurso, Cargo cargo, CentroCusto cenCusto) 
			throws SQLException {
		novoRecurso = LibUtil.truncaTexto(novoRecurso, 60);
		String sql = montaScriptEquipe("") +
				String.format(" WHERE eq.nmRecurso = '%s' AND eq.cdCargo = %d AND eq.cdCentroCusto = %d",
						novoRecurso, cargo.getCdCargo(), cenCusto.getCdCentroCusto());
		return executarGetRecurso(con.getConexao(), sql);
	}
	
	private static Recurso executarGetRecurso(Connection con, String sql) throws SQLException {
		
		Recurso recurso = null;
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		if (res.next()) {
			recurso = carregaRecurso(res, null);
		}
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		
		return recurso;
	}

	// TODO Verificar desativação 
	/*
	public static String contatenaRecursoVinculado(Connection con, int cdFuncionario) throws SQLException {
		String listaIDConcatenado = "";
		
		String sql = montaScriptEquipe("") +
				String.format(" WHERE er.cdFuncVinculo = %d", cdFuncionario);
		
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			listaIDConcatenado += res.getLong("ID_Recurso") + ",";
		}
		
		if (!listaIDConcatenado.isEmpty())
			listaIDConcatenado = listaIDConcatenado.substring(0, listaIDConcatenado.length()-1);
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		
		return listaIDConcatenado;
	}
	*/

	public static List<Recurso> listarEquipeDoSetor(Connection con, CentroCusto setor) throws SQLException {
		String sql = montaScriptEquipe("") +
				String.format(" WHERE eq.cdCentroCusto = %d", setor.getCdCentroCusto());
		
		List<Recurso> lista = new ArrayList<Recurso>();
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			lista.add(carregaRecurso(res, null));
		}
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		
		return lista;
	}
	
	public static List<Recurso> listarEquipeGeral(Connection con) throws SQLException {
		String sql = montaScriptEquipe("");
		
		List<Recurso> lista = new ArrayList<Recurso>();
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			lista.add(carregaRecurso(res, null));
		}
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		
		return lista;
	}

	public static List<Recurso> lisarRecursos(Connection con, TipoRecurso tipo) throws SQLException {
		String sql = montaScriptEquipe("") +
				String.format(" WHERE eq.tpRecurso = '%s'", tipo.toString());
		
		List<Recurso> lista = new ArrayList<Recurso>();
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			lista.add(carregaRecurso(res, null));
		}
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		
		return lista;
	}

	
	public static List<Recurso> listarRecursoPorGestor(Connection con, int cdGestor, MesAnoOrcamento mesRef, boolean produtivo) 
			throws Exception {
		
		List<Recurso> lista = new ArrayList<Recurso>();
		
		Funcionario gestor = new Funcionario();
		gestor.setCdFuncionario(cdGestor);
		
		if (!CentroCustoServiceDAO.montarCentrosCustoResponsavel(con, gestor))
			CentroCustoServiceDAO.montarCentrosCustoGerente(con, gestor, mesRef.getAnoMes());
		
		// Procura o funcionário como gerente de algum departamento
		String sql = montaScriptEquipe("JOIN TmpCencus ON TmpCencus.cdCentroCusto = eq.cdCentroCusto ");
		
		if (produtivo)
			sql += "WHERE eq.tpRecurso = 'P' AND eq.cecTpArea IN ('VE', 'SV') ";
		else
			sql += " WHERE eq.tpRecurso IN ('O', 'P') ";
		
		sql += "ORDER BY eq.cecCdExterno, eq.nomeVinculo;";
				
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			
			Recurso recurso = carregaRecurso(res, mesRef);
			recurso.getSetor().setTipos(CategoriaReceitaServiceDAO.listarCategCencusto(con, res.getInt("cdCentroCusto")));
			
			lista.add(recurso);
		}
		
		if (lista.size() == 0) { // Nao achou o funcionario como coordenador de setor
			Recurso func = getRecursoVinculado(con, cdGestor);
			if (func != null && 
					(func.getSetor().getTipoArea().equals(AreaSetor.VE) || func.getSetor().getTipoArea().equals(AreaSetor.SV)))
				lista.add(func);
			func = null;
		}
		
		stmt.close();
		res.close();
		
		stmt = null;
		res = null;
		return lista;
	}
	
	
	public static List<Recurso> listaRecursoPorCategoriaReceita(Connection con, int codCateg) throws SQLException {
		
		List<Recurso> lista = new ArrayList<Recurso>();
		
		// Procura o funcionário como gerente de algum departamento
		String sql = montaScriptEquipe(String.format("JOIN CadCencusCatrec cctr ON cctr.cdCentroCusto = eq.cdCentroCusto "
				+ " WHERE cctr.cdCategoria = %d", codCateg)) 
				+ " AND eq.tpRecurso = 'O'";
		sql += "ORDER BY eq.ID_Recurso;";
				
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			lista.add(carregaRecurso(res, null));
		}
		
		stmt.close();
		res.close();
		
		stmt = null;
		res = null;
		return lista;
		
	}
	
	
	
	public static Recurso carregaRecurso(ResultSet res, MesAnoOrcamento mesRef) throws SQLException {
		if (mesRef == null)
			mesRef = new MesAnoOrcamento();
		
		Recurso recurso = new Recurso(mesRef);
		
		recurso.setCdRecurso(res.getLong("ID_Recurso"));
		recurso.setNrAno(mesRef.getAno());
		
		CentroCusto setor = new CentroCusto();
		setor.setCdCentroCusto(res.getInt("cdCentroCusto"));
		if (setor.getCdCentroCusto() > 0) {
			setor.setCodExterno(res.getString("cecCdExterno"));
			setor.setCecDsResumida(res.getString("cecDsResumida"));
			setor.setTipoArea(AreaSetor.valueOf(res.getString("cecTpArea")));
			
			setor.setFilial(new Filial());
			setor.getFilial().setCdFilial(res.getInt("cdFilial"));
			setor.getFilial().setSgFilial(res.getString("sgFilial"));
		}
		recurso.setSetor(setor);
		
		recurso.setNmRecurso(res.getString("nmRecurso"));
		recurso.setNomeVinculo(res.getString("nomeVinculo"));
		if (res.getLong("ID_Recurso") > 0) {
			TipoCargo tipo = TipoCargo.valueOf(res.getString("tpCargo"));	
			
			recurso.setCargo(new Cargo(res.getInt("cdCargo"), res.getString("dsCargo"), tipo));
			recurso.setTipo(TipoRecurso.valueOf(res.getString("tpRecurso")));
			
			Funcionario vinculo = new Funcionario();
			vinculo.setCdFuncionario(res.getInt("codFuncVinculo"));
			vinculo.setLogUsuario(res.getString("loginVinculo"));
			vinculo.setNmFuncionario(res.getString("nmFuncionario"));
			vinculo.setCenCusto(new CentroCusto(res.getInt("setorVinculo")));
			recurso.setVinculo(vinculo);
			
			recurso.setInicioVinculo(0);  // TODO (res.getInt("nrAnoMesInicial"));
			recurso.setFimVinculo(0);  // TODO (res.getInt("nrAnoMesFinal"));
		}
		return recurso;
	}
	
	/**
	 * Utilizado quando quero criar uma relação para CARREGAR o objeto de recurso juntamente com outros objetos
	 * @return
	 */
	public static String montaScriptRelacaoEquipe(String tabelaFilha) {
		/*
		   MesAno referencia = new MesAnoOrcamento();
		
		   String script = String.format("LEFT OUTER JOIN OrcEquipe equipe ON equipe.ID_Recurso = %s.ID_Recurso "
				+ "LEFT OUTER JOIN CadCargo cargo ON equipe.cdCargo = cargo.cdCargo "
				+ "LEFT OUTER JOIN EmpCentroCusto setorEq ON equipe.cdCentroCusto = setorEq.cdCentroCusto "
				+ "LEFT OUTER JOIN EmpFilial filialEq ON setorEq.cdFilial = filialEq.cdFilial "
				+ "LEFT OUTER JOIN OrcEquipeRecurs vinculo ON equipe.ID_Recurso = vinculo.ID_Recurso "
				+      "AND vinculo.nrAnoMesInicial <= %d AND (vinculo.nrAnoMesFinal IS NULL OR vinculo.nrAnoMesFinal >= %d) "
				+ "LEFT OUTER JOIN CadFuncionario nomeVinc ON vinculo.cdFuncVinculo = nomeVinc.cdFuncionario ",
				tabelaFilha,
				referencia.getAnoMes(),
				referencia.getAnoMes());
		*/
		
		String script = String.format("LEFT OUTER JOIN sp_ListaRecurso(%d) eq ON eq.ID_Recurso = %s.ID_Recurso ",
				new MesAnoOrcamento().getAnoMes(),
				tabelaFilha);
		
		return script;
	}
	
	public static String montaScriptEquipe(String relacao) {
		if (relacao == null)
			relacao = "";
		
		String script = String.format("SELECT * FROM sp_ListaRecurso(%d) eq ", new MesAnoOrcamento().getAnoMes());
		script += relacao;
				
		return script;
	}

	
}
