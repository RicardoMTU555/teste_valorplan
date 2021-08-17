package com.ctbli9.valorplan.DAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.ctb.ClasseDREAgrupada;
import com.ctbli9.valorplan.modelo.ctb.ContaClasseSetor;
import com.ctbli9.valorplan.modelo.ctb.SelecionaCentroCusto;
import com.ctbli9.valorplan.modelo.ctb.SelecionaConta;

import com.ctbli9.valorplan.modelo.CentroCusto;
import ctbli9.modelo.Filial;
import ctbli9.modelo.ctb.ClasseDRE;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.modelo.ctb.GrupoClasseDRE;
import ctbli9.recursos.ArquivoTexto;
import ctbli9.recursos.LibUtil;
import ctbli9.recursos.RegraNegocioException;

public class ClassificacaoCtbServiceDAO {
	public static ClasseDRE pesquisarClassificacaoCtb(Connection con, int cdClasse) throws SQLException, NamingException {
		Statement stmt = con.createStatement();         // declara vari�vel que comunica com a conex�o
		
		ClasseDRE classe = new ClasseDRE();		
		String sql = String.format("SELECT c.*, g.*, "
				+ "COALESCE( (SELECT DISTINCT 'S' FROM CtbConta ctb WHERE ctb.cdClasse = c.cdClasse UNION "
				+ "           SELECT DISTINCT 'S' FROM CtbClaConta ctb2 WHERE ctb2.nrAno = c.nrAno AND ctb2.cdClasse = c.cdClasse), 'N') as TemConta, "
				+ "COALESCE( (select distinct 'N' from CtbClasseDRE ctb2 "
				+ "   where ctb2.nrAno = c.nrAno AND ctb2.cdNivelClasse like (%s) " 
	            + "   and CHAR_LENGTH(TRIM(ctb2.cdNivelClasse)) > CHAR_LENGTH(TRIM(c.cdNivelClasse))), 'S') as UltNivel "
				+ "FROM CtbClasseDre c "
				+ "LEFT OUTER JOIN CtbGrupoClasse g ON c.cdGrupoClasse = g.cdGrupoClasse "
				+ "WHERE cdClasse = %d", 
				"TRIM(c.cdNivelClasse) || '%'",
				cdClasse);
		ResultSet rs = stmt.executeQuery(sql);
	    if(rs.next()) {
			classe = carregaClasseDRE(rs);
	    }
		
	    rs.close();
		stmt.close();
	    rs = null;
		stmt = null;
		
		return classe;
	}//pesquisar
	
	
	public static List<ClasseDRE> listar(Connection con, int nroAno) throws SQLException, NamingException {
		String sql = "SELECT c.*, g.*, "
				+ "COALESCE( (SELECT DISTINCT 'S' FROM CtbConta ctb WHERE ctb.cdClasse = c.cdClasse UNION "
				+ "           SELECT DISTINCT 'S' FROM CtbClaConta ctb2 WHERE ctb2.nrAno = c.nrAno AND ctb2.cdClasse = c.cdClasse), 'N') as TemConta, "
				+ "COALESCE( (select distinct 'N' from CtbClasseDRE ctb2 " + 
				              "where ctb2.nrAno = c.nrAno AND ctb2.cdNivelClasse like TRIM(c.cdNivelClasse) || '%' " + 
				              "and CHAR_LENGTH(TRIM(ctb2.cdNivelClasse)) > CHAR_LENGTH(TRIM(c.cdNivelClasse))), 'S') as UltNivel "
				+ "FROM CtbClasseDRE c "
				+ "LEFT OUTER JOIN CtbGrupoClasse g ON c.cdGrupoClasse = g.cdGrupoClasse "
				+ "WHERE c.nrAno = " + nroAno + " "
				+ "ORDER BY c.cdNivelClasse";
				
		List<ClasseDRE> classes = new ArrayList<ClasseDRE>();
		
		Statement stmt = con.createStatement();
		
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()) {
			classes.add(carregaClasseDRE(rs));
		}
		
		rs.close();
		stmt.close();
		
		rs = null;
		stmt = null;
		
		return classes;
	}


	public static ClasseDRE pesquisarPorDescricao(Connection con, String descrClasse, String cdNivelAnterior, int tamanhoNivel, int nroAno) 
			throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		if (descrClasse.length() > 50)
			descrClasse = descrClasse.substring(0, 50);
		
		ClasseDRE classe = null;		
		String sql = String.format("SELECT c.*, g.*, 'N' as TemConta, 'N' as UltNivel FROM CtbClasseDre c "
				+ "LEFT OUTER JOIN CtbGrupoClasse g ON c.nrAno = g.nrAno AND c.cdGrupoClasse = g.cdGrupoClasse "
				+ "WHERE c.nrAno = %d "
				+ "AND dsClasse = '%s' "
				+ "AND SUBSTRING(cdNivelClasse FROM 1 FOR %d) = '%s' "
				+ "AND   CHAR_LENGTH(cdNivelClasse) = %d", 
				nroAno,
				descrClasse,
				cdNivelAnterior.length(),
				cdNivelAnterior,
				tamanhoNivel);
		ResultSet rs = stmt.executeQuery(sql);
		
	    if(rs.next()) {
	    	classe = carregaClasseDRE(rs);
	    }
		
	    rs.close();
		stmt.close();
	    rs = null;
		stmt = null;
		
		return classe;
	}//pesquisar
	
	
	public static void incluir(Connection con, ClasseDRE classe) throws SQLException, NamingException {
			
		if (classe.getCdClasse() == 0)
			classe.setCdClasse(ConexaoDB.gerarNovoCodigo(con, "CtbClasseDRE", "cdClasse", null));
		
		String sql = "INSERT INTO CtbClasseDRE VALUES (?, ?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setInt(++i, classe.getCdClasse());
		pstmt.setInt(++i, classe.getNrAno());
		pstmt.setString(++i, classe.getCdNivelClasse());
		pstmt.setString(++i, classe.getDsClasse());
		if (classe.getGrupo() == null || classe.getGrupo().getCdGrupoClasse() == 0)
			pstmt.setNull(++i, java.sql.Types.INTEGER);
		else
			pstmt.setInt(++i, classe.getGrupo().getCdGrupoClasse());

		pstmt.executeUpdate();

		pstmt.close();
		pstmt = null;
	}//salvar
	
	public static void alterar(Connection con, ClasseDRE classe) throws SQLException, NamingException {
		String sql = "UPDATE CtbClasseDRE set "
				+ "cdNivelClasse = ?, "
				+ "dsClasse = ?, "
				+ "cdGrupoClasse = ? "
				+ "WHERE cdClasse = ?"; 

		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setString(++i, classe.getCdNivelClasse());
		pstmt.setString(++i, classe.getDsClasse());
		if (classe.getGrupo() == null || classe.getGrupo().getCdGrupoClasse() == 0)
			pstmt.setNull(++i, java.sql.Types.INTEGER);
		else
			pstmt.setInt(++i, classe.getGrupo().getCdGrupoClasse());

		pstmt.setInt(++i, classe.getCdClasse());

		pstmt.executeUpdate();

		pstmt.close();
		pstmt = null;
	}

	public static void excluir(Connection con, ClasseDRE classeConta) throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		String sql = String.format(Locale.US,
				"DELETE FROM CtbClasseDRE WHERE cdClasse = %d", 
				classeConta.getCdClasse()
				);			
		stmt.executeUpdate(sql);

		stmt.close();		
		stmt = null;
	}
	
	public static List<ClasseDRE> listarNivelMaximo(Connection con, int nroAno) throws SQLException, NamingException {
		String sql = String.format("SELECT c.*, g.*, "
				+ "COALESCE( (SELECT DISTINCT 'S' FROM CtbConta ctb WHERE ctb.cdClasse = c.cdClasse UNION "
				+ "           SELECT DISTINCT 'S' FROM CtbClaConta ctb2 WHERE ctb2.nrAno = c.nrAno AND ctb2.cdClasse = c.cdClasse), 'N') as TemConta "
				+ "FROM CtbClasseDRE c "
				+ "LEFT OUTER JOIN CtbGrupoClasse g ON c.nrAno = g.nrAno AND c.cdGrupoClasse = g.cdGrupoClasse "
				+ "WHERE c.nrAno = %d "
				+ "AND char_length(c.cdNivelClasse) = "
				+      "(SELECT MAX(char_length(c2.cdNivelClasse)) FROM CtbClasseDRE c2 "
				+      "WHERE c2.nrAno = %d AND c.cdNivelClasse = SUBSTRING(c2.cdNivelClasse FROM 1 FOR char_length(c.cdNivelClasse))) "
				+ "ORDER BY c.cdNivelClasse",
				nroAno,
				nroAno);
		
		List<ClasseDRE> classes = new ArrayList<ClasseDRE>();
		
		ClasseDRE classe = null;
		
		Statement stmt = con.createStatement();
		
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()) {
			classe = new ClasseDRE();
			classe.setCdClasse(rs.getInt("cdClasse"));
	    	classe.setNrAno(rs.getInt("nrAno"));
			classe.setDsClasse(rs.getString("dsClasse"));
			classe.setGrupo(new GrupoClasseDRE(rs.getByte("cdGrupoClasse"), rs.getInt("nrAno"), rs.getString("dsGrupoClasse")));
			classe.setCdNivelClasse(rs.getString("cdNivelClasse"));
			classe.setTemConta(rs.getString("TemConta").equals("S"));
			classe.setUltimoNivel(true);
			classes.add(classe);
		}
		
		rs.close();
		stmt.close();
		
		rs = null;
		stmt = null;
		
		return classes;
	}

	public static void incluirClasseConta(Connection con, ContaContabil ctaContabil, CentroCusto cencus, ClasseDRE classe) 
			throws SQLException, NamingException {
		
		String sql = "INSERT INTO CtbClaConta (nrAno, ID_Conta, cdCentroCusto, cdClasse) VALUES (?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setInt(++i, classe.getNrAno());
		pstmt.setInt(++i, ctaContabil.getIdConta());
		pstmt.setInt(++i, cencus.getCdCentroCusto());
		pstmt.setInt(++i, classe.getCdClasse());

		pstmt.executeUpdate();

		pstmt.close();
		pstmt = null;
	}

	public static boolean existeClasseConta(Connection con, ContaContabil ctaContabil, CentroCusto cencus, ClasseDRE classe) throws SQLException, NamingException {
		return ConexaoDB.existeRegistro(con, "CtbClaConta", 
				String.format("nrAno = %d AND ID_Conta = %d AND (cdCentroCusto = 0 OR cdCentroCusto = %d)", 
						PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
						ctaContabil.getIdConta(),
						cencus.getCdCentroCusto()));			
	}

	public static boolean existeClasseConta(Connection con, ClasseDRE classe) throws SQLException, NamingException {
		return ConexaoDB.existeRegistro(con, "CtbClaConta", 
				String.format("nrAno = %d AND cdClasse = %d", classe.getNrAno(), classe.getCdClasse()));			
	}

	public static String novoCodigoNivel(Connection con, String cdNivelClasse, int nroAno) throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		String retorno = cdNivelClasse;
		int nivel = cdNivelClasse.trim().length();
		String sql = String.format("SELECT MAX(cdNivelClasse) as nivelClasse FROM CtbClasseDRE "
				+ "WHERE nrAno = %d AND SUBSTRING(cdNivelClasse FROM 1 FOR %d) = '%s' and char_length(cdNivelClasse) = %d;",
				nroAno,
				nivel,
				cdNivelClasse,
				(nivel + 2)
				);			
		ResultSet res = stmt.executeQuery(sql);
		
		if (res.next() && res.getInt("nivelClasse") > 0) {
			retorno = String.format("%0" + (nivel + 2) + "d", res.getInt("nivelClasse") + 1);
		} else {
			retorno = cdNivelClasse + "01";
		}

		res.close();
		res = null;
		stmt.close();		
		stmt = null;
		
		return retorno;
	}
	
	/*
	 * GRUPOS
	 */
	public static List<GrupoClasseDRE> listarGrupos(Connection con, int nrAno) throws SQLException, NamingException {
		String sql = String.format("SELECT * FROM CtbGrupoClasse WHERE nrAno = %d ORDER BY cdGrupoClasse",
				nrAno);
		
		List<GrupoClasseDRE> gruposClasse = new ArrayList<GrupoClasseDRE>();
		
		GrupoClasseDRE grupoClasse = null;
		
		Statement stmt = con.createStatement();
		
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()) {
			grupoClasse = new GrupoClasseDRE();
			grupoClasse.setCdGrupoClasse(rs.getByte("cdGrupoClasse"));
			grupoClasse.setDsGrupoClasse(rs.getString("dsGrupoClasse"));
			grupoClasse.setNrAno(rs.getInt("nrAno"));
			gruposClasse.add(grupoClasse);
		}
		
		rs.close();
		stmt.close();
		
		rs = null;
		stmt = null;
		
		return gruposClasse;
	}


	public static void excluirGrupoClasse(Connection con, GrupoClasseDRE grupo) throws SQLException, NamingException {
		String sql = "DELETE FROM CtbGrupoClasse WHERE cdGrupoClasse = ?";
		PreparedStatement pstmtDel = con.prepareStatement(sql);
		
		int i = 0;
		pstmtDel.setInt(++i, grupo.getCdGrupoClasse());
		pstmtDel.executeUpdate();
		
		pstmtDel.close();
		pstmtDel = null;
	}

	public static GrupoClasseDRE pesquisarGrupoClassePorDescricao(Connection con, String descricao, int ano) 
			throws SQLException, NamingException {
		String sql = String.format("SELECT * FROM CtbGrupoClasse WHERE nrAno = %d AND dsGrupoClasse = '%s'",
				ano,
				descricao);
		
		GrupoClasseDRE grupoClasse = null;
		
		Statement stmt = con.createStatement();
		
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()) {
			grupoClasse = new GrupoClasseDRE();
			grupoClasse.setCdGrupoClasse(rs.getByte("cdGrupoClasse"));
			grupoClasse.setDsGrupoClasse(rs.getString("dsGrupoClasse"));
			grupoClasse.setNrAno(rs.getInt("nrAno"));
		}
		
		rs.close();
		stmt.close();
		
		rs = null;
		stmt = null;
		
		return grupoClasse;

	}

	public static void gravarGrupoClasse(Connection con, GrupoClasseDRE grupo) throws SQLException, NamingException {
		
		String sql = "INSERT INTO CtbGrupoClasse VALUES (?, ?, ?)";
		PreparedStatement pstmtIns = con.prepareStatement(sql);
		
		sql = "UPDATE CtbGrupoClasse SET "
				+ "dsGrupoClasse = ? "
				+ "WHERE cdGrupoClasse = ?";
		PreparedStatement pstmtUpd = con.prepareStatement(sql);
		
		int i;
		
		i = 0;
		if (grupo.getCdGrupoClasse() == 0) {
			grupo.setCdGrupoClasse((byte) ConexaoDB.gerarNovoCodigo(con, "CtbGrupoClasse", "cdGrupoClasse", null));
			i = 0;
			pstmtIns.setInt(++i, grupo.getCdGrupoClasse());
			pstmtIns.setInt(++i, grupo.getNrAno());
			pstmtIns.setString(++i, grupo.getDsGrupoClasse());
			pstmtIns.executeUpdate();	
			
		} else {
			i = 0;
			pstmtUpd.setString(++i, grupo.getDsGrupoClasse());
			pstmtUpd.setInt(++i, grupo.getCdGrupoClasse());
			pstmtUpd.executeUpdate();
					
		}
				
		pstmtUpd.close();
		pstmtIns.close();
		
		pstmtUpd = null;
		pstmtIns = null;
	}

	public static List<ClasseDREAgrupada> listarClasseAgrupada(Connection con, int nroAno) throws SQLException, NamingException {
		int maxNivel = obtemNivelMaximoDRE(con);
		String sql = montaScriptClasseDRE(con, nroAno, maxNivel);
						
		List<ClasseDREAgrupada> classes = new ArrayList<ClasseDREAgrupada>();
		
		ClasseDREAgrupada classe = null;
		
		Statement stmt = con.createStatement();
		
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()) {
			classe = new ClasseDREAgrupada();
			classe.setCdClasse(rs.getInt("cdClasse"));
			classe.setCdNivelClasse(rs.getString(String.format("cdClasse%02d", maxNivel)));
			
			String[] descricoes = new String[maxNivel];
			for (int i = 0; i < descricoes.length; i++) {
				descricoes[i] = rs.getString(String.format("dsClasse%02d",(i+1)));	
			}
			classe.setDsClasse(descricoes);
			classes.add(classe);
		}
		
		rs.close();
		stmt.close();
		
		rs = null;
		stmt = null;
		
		return classes;
	}
	
	
	public static List<SelecionaConta> lisarContasSelecionadas(Connection con, ClasseDRE classeConta) 
			throws SQLException, NamingException {
		List<SelecionaConta> lista = new ArrayList<SelecionaConta>();
		
		String sql = String.format("SELECT ct.* FROM CtbConta ct "
				+ "JOIN CtbClaConta cla ON cla.nrAno = %d AND cla.cdClasse = %d AND cla.ID_Conta = ct.ID_Conta "
				+ "ORDER BY ct.cdConta", 
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				classeConta.getCdClasse());
		
		Statement stmt = con.createStatement();

		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()) {
			SelecionaConta conta = new SelecionaConta();
			conta.setSeleciona(true);
			conta.setCdConta(rs.getString("cdConta"));
			conta.setDsConta(rs.getString("dsConta"));
			
			lista.add(conta);
		}
		
		rs.close();
		stmt.close();
		
		rs = null;
		stmt = null;
		
		return lista;
	}

	public static ContaClasseSetor lisarCentroCustoConta(Connection con, ClasseDRE classeConta, ContaContabil conta) 
			throws SQLException, NamingException {
		
		ContaClasseSetor contaClasseSetor = new ContaClasseSetor();
		
		// Se eu achei SETOR ZERO para essa conta, significa que ela NÃO UTILIZA SETOR
		String sql = String.format(
				"SELECT DISTINCT 'S' AS SetorZero FROM CtbClaConta cla "
				+ "WHERE cla.nrAno = %d "
				+ "AND cla.cdClasse = %d "
				+ "AND cla.ID_Conta = %d "
				+ "AND cla.cdCentroCusto = 0", 
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				classeConta.getCdClasse(),
				conta.getIdConta());
		
		Statement stmt = con.createStatement();
		
		ResultSet rs = stmt.executeQuery(sql);
		contaClasseSetor.setUtilizaSetor(!rs.next());
		rs.close();
		
		// Lista todos os setores, e marca selecionado caso o setor esteja na tabela de relacionamento com a classificação da conta
		sql = String.format("SELECT "
				+ "COALESCE((select DISTINCT 'S' FROM CtbClaConta cla "
				+            "WHERE cla.nrAno = %d "
				+            "AND cla.cdClasse = %d "
				+            "AND cla.ID_Conta = %d "
				+            "AND cla.cdCentroCusto = cc.cdCentroCusto), 'N') AS sel, "
				+ "cc.cdCentroCusto, cc.cecCdExterno, cc.cecDsResumida, "
				+ "cc.cdFilial, f.sgFilial "
				+ "FROM  EmpCentroCusto cc "
				+ "JOIN EmpFilial f ON cc.cdFilial = f.cdFilial "
				+ "ORDER BY 1 DESC, cc.cecCdExterno;", 
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				classeConta.getCdClasse(),
				conta.getIdConta());
		
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			
			SelecionaCentroCusto cenc = new SelecionaCentroCusto();
			cenc.setSeleciona(rs.getString("sel").equals("S"));
			cenc.setCdCentroCusto(rs.getInt("cdCentroCusto"));
			cenc.setCodExterno(rs.getString("cecCdExterno"));
			cenc.setCecDsResumida(rs.getString("cecDsResumida"));
			cenc.setFilial(new Filial());
			cenc.getFilial().setCdFilial(rs.getInt("cdFilial"));
			cenc.getFilial().setSgFilial(rs.getString("sgFilial"));
			
			contaClasseSetor.getListaCencusto().add(cenc);
			
			if (cenc.isSeleciona())
				contaClasseSetor.addSetor();
		}	
				
		rs.close();
		
		stmt.close();
		
		rs = null;
		stmt = null;
		
		return contaClasseSetor;
	}
	
	public static void salvarClaContaSemSetor(Connection con, ClasseDRE classeConta, SelecionaConta conta, SelecionaCentroCusto setorZero) 
			throws SQLException, NamingException {
		String sql = "DELETE FROM CtbClaConta WHERE nrAno = ? AND ID_Conta = ?";
		PreparedStatement pstmtDel = con.prepareStatement(sql);

		int i = 0;
		pstmtDel.setInt(++i, classeConta.getNrAno());
		pstmtDel.setInt(++i, conta.getIdConta());
		pstmtDel.executeUpdate();
		
		pstmtDel.close();
		pstmtDel = null;
		
		if (setorZero.isSeleciona()) {
			conta.setSeleciona(true);
			
			sql = "INSERT INTO CtbClaConta (nrAno, ID_Conta, cdCentroCusto, cdClasse) VALUES (?, ?, 0, ?)";
			PreparedStatement pstmtIns = con.prepareStatement(sql);
			
			i = 0;
			pstmtIns.setInt(++i, classeConta.getNrAno());
			pstmtIns.setInt(++i, conta.getIdConta());
			pstmtIns.setInt(++i, classeConta.getCdClasse());
			pstmtIns.executeUpdate();								
		
			pstmtIns.close();
			pstmtIns = null;
		} else
			conta.setSeleciona(false);
	}
	
	public static void salvarClaContaComSetor(Connection con, ClasseDRE classeConta, SelecionaConta conta,
			List<SelecionaCentroCusto> listaCencusto) throws SQLException, NamingException {

		String sql = "";
		
		sql = "DELETE FROM CtbClaConta WHERE nrAno = ? AND ID_Conta = ? AND cdCentroCusto = 0";
		PreparedStatement pstmtDelSetorZero = con.prepareStatement(sql);

		int i = 0;
		pstmtDelSetorZero.setInt(++i, classeConta.getNrAno());
		pstmtDelSetorZero.setInt(++i, conta.getIdConta());
		pstmtDelSetorZero.executeUpdate();
		
		pstmtDelSetorZero.close();
		pstmtDelSetorZero = null;

		sql = "SELECT * FROM CtbClaConta WHERE nrAno = ? AND ID_Conta = ? AND cdCentroCusto = ? AND cdClasse = ?";
		PreparedStatement pstmtSelEspec = con.prepareStatement(sql);
		
		sql = "SELECT * FROM CtbClaConta WHERE nrAno = ? AND ID_Conta = ? AND cdCentroCusto = ?";
		PreparedStatement pstmtSelGer = con.prepareStatement(sql);
		
		sql = "INSERT INTO CtbClaConta (nrAno, ID_Conta, cdCentroCusto, cdClasse) VALUES (?, ?, ?, ?)";
		PreparedStatement pstmtIns = con.prepareStatement(sql);
		
		sql = "UPDATE CtbClaConta SET cdClasse = ? WHERE nrAno = ? AND ID_Conta = ? AND cdCentroCusto = ?";
		PreparedStatement pstmtUpd = con.prepareStatement(sql);

		sql = "DELETE FROM CtbClaConta WHERE nrAno = ? AND ID_Conta = ? AND cdCentroCusto = ? AND cdClasse = ?";
		PreparedStatement pstmtDel = con.prepareStatement(sql);
		
		conta.setSeleciona(false);
		
		i = 0;
		ResultSet res = null;
		// Processa array enviado
		for (SelecionaCentroCusto cencus : listaCencusto) {
			i = 0;
			pstmtSelEspec.setInt(++i, classeConta.getNrAno());
			pstmtSelEspec.setInt(++i, conta.getIdConta());
			pstmtSelEspec.setInt(++i, cencus.getCdCentroCusto());
			pstmtSelEspec.setInt(++i, classeConta.getCdClasse());
			res = pstmtSelEspec.executeQuery();
			
			if (res.next()) { // Achou a combinação conta+setor na classificação, e é pra desmarcar
				if (!cencus.isSeleciona()) {
					i = 0;
					pstmtDel.setInt(++i, classeConta.getNrAno());
					pstmtDel.setInt(++i, conta.getIdConta());
					pstmtDel.setInt(++i, cencus.getCdCentroCusto());
					pstmtDel.setInt(++i, classeConta.getCdClasse());
					pstmtDel.executeUpdate();	
				}
			} else { // NÃO achou a combinação conta+setor na classificação e é pra marcar
				if (cencus.isSeleciona()) {
					// Verifica se a combinação existe em OUTRA classificação

					res.close();
					i = 0;
					pstmtSelGer.setInt(++i, classeConta.getNrAno());
					pstmtSelGer.setInt(++i, conta.getIdConta());
					pstmtSelGer.setInt(++i, cencus.getCdCentroCusto());
					res = pstmtSelGer.executeQuery();
					if (res.next()) {
						i = 0;
						pstmtUpd.setInt(++i, classeConta.getCdClasse());
						
						pstmtUpd.setInt(++i, classeConta.getNrAno());
						pstmtUpd.setInt(++i, conta.getIdConta());
						pstmtUpd.setInt(++i, cencus.getCdCentroCusto());
						pstmtUpd.executeUpdate();

					} else {
						i = 0;
						pstmtIns.setInt(++i, classeConta.getNrAno());
						pstmtIns.setInt(++i, conta.getIdConta());
						pstmtIns.setInt(++i, cencus.getCdCentroCusto());
						pstmtIns.setInt(++i, classeConta.getCdClasse());
						pstmtIns.executeUpdate();
					}
					conta.setSeleciona(true);
				}
			}
			res.close();
		}
		
		res = null;
		
	}
	
	public static void replicarDadosAno(Connection con, int nroAnoOrigem, int nroAnoDestino) 
			throws SQLException, NamingException, RegraNegocioException {
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<Integer, Integer> mapaGrupo =  new TreeMap();  // Grupo Atual, Novo Grupo
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<Integer, Integer> mapaClasse =  new TreeMap();  // Classe Atual, Nova Classe
		
		
		PreparedStatement pstmt = null; 
		Statement stmt = con.createStatement();

		String sql = String.format("SELECT distinct 1 FROM CtbClasseDRE c WHERE c.nrAno = %d ", nroAnoDestino);
		ResultSet rs = stmt.executeQuery(sql);
		if (rs.next()) {
			throw new RegraNegocioException("Ano DESTINO informado já possui dados na base. Replicação não permitida.");
		}
		rs.close();
		rs = null;
		
		/*
		 * 1. Grupos
		 */
		sql = "INSERT INTO CtbGrupoClasse VALUES (?, ?, ?)";
		pstmt = con.prepareStatement(sql);
		
		
		sql = String.format("SELECT * FROM CtbGrupoClasse c "
				+ "WHERE c.nrAno = %d "
				+ "ORDER BY c.cdGrupoClasse", 
				nroAnoOrigem);
		
		rs = stmt.executeQuery(sql);
		
		while (rs.next()) {
			int novoCodGrupo = ConexaoDB.gerarNovoCodigo(con, "CtbGrupoClasse", "cdGrupoClasse", null);
			
			int i = 0;
			pstmt.setInt(++i, novoCodGrupo);
			pstmt.setInt(++i, nroAnoDestino);
			pstmt.setString(++i, rs.getString("dsGrupoClasse"));
			
			pstmt.executeUpdate();
			
			// Inclui no mapa de correlação
			mapaGrupo.put(rs.getInt("cdGrupoClasse"), novoCodGrupo);
		}

		/*
		 * 2.Classe
		 */
		pstmt.close();
		pstmt = null;
		rs.close();
		rs = null;
		
		sql = "INSERT INTO CtbClasseDRE VALUES (?, ?, ?, ?, ?)";
		pstmt = con.prepareStatement(sql);
		
		
		sql = String.format("SELECT * FROM CtbClasseDRE c "
				+ "WHERE c.nrAno = %d "
				+ "ORDER BY c.cdNivelClasse", 
				nroAnoOrigem);
		
		rs = stmt.executeQuery(sql);
		
		while (rs.next()) {
			
			int novoCodClasse = ConexaoDB.gerarNovoCodigo(con, "CtbClasseDRE", "cdClasse", null);
			
			int i = 0;
			pstmt.setInt(++i, novoCodClasse);
			pstmt.setInt(++i, nroAnoDestino);
			pstmt.setString(++i, rs.getString("cdNivelClasse"));
			pstmt.setString(++i, rs.getString("dsClasse"));
			
			if (rs.getInt("cdGrupoClasse") == 0)
				pstmt.setNull(++i, java.sql.Types.INTEGER);
			else {
				int novoCodGrupo = mapaGrupo.get(rs.getInt("cdGrupoClasse"));
				if (novoCodGrupo > 0)
					pstmt.setInt(++i, novoCodGrupo);
				else
					pstmt.setNull(++i, java.sql.Types.INTEGER);
				
			}
			pstmt.executeUpdate();
			
			// Inclui no mapa de correlação
			mapaClasse.put(rs.getInt("cdClasse"), novoCodClasse);

		}//while

		
		/*
		 * 3.Relacionamento Classe x Conta
		 */
		pstmt.close();
		pstmt = null;
		rs.close();
		rs = null;
		
		sql = "INSERT INTO CtbClaConta (nrAno, ID_Conta, cdCentroCusto, cdClasse) VALUES (?, ?, ?, ?)";
		pstmt = con.prepareStatement(sql);
		
		
		sql = String.format("SELECT * FROM CtbClaConta c WHERE c.nrAno = %d", 
				nroAnoOrigem);
		
		rs = stmt.executeQuery(sql);
		
		while (rs.next()) {
			int novaClasse = mapaClasse.get(rs.getInt("cdClasse"));
			if (novaClasse > 0) {
				int i = 0;
				pstmt.setInt(++i, nroAnoDestino);
				pstmt.setInt(++i, rs.getInt("ID_Conta"));
				pstmt.setInt(++i, rs.getInt("cdCentroCusto"));
				pstmt.setInt(++i, novaClasse);
				pstmt.executeUpdate();
			}
		}
		
		pstmt.close();
		pstmt = null;
		stmt.close();
		stmt = null;
		rs.close();
		rs = null;

	}


	public static void gerarPlanilhaCSV(Connection con, String nomePlanilha) throws IOException, SQLException, NamingException {
		Statement stmt = con.createStatement();
				
		String sql = String.format(
				"SELECT dre.dsGrupoClasse, dre.cdNivelClasse, dre.dsClasse01, dre.dsClasse02, "
				+ "dre.dsClasse03, ct.cdConta, ct.dsConta, ct.ctaIdNatureza, ct.ctaIdGrupo, "
				+ "cc.cdFilial, cc.cecCdExterno, cc.cecDsResumida, "
				+ "c1.dsCampoBI AS Adicional, c2.dsCampoBI AS RV, c3.dsCampoBI AS Lucro_Bruto, c4.dsCampoBI AS Ebitda " + 
				"FROM CtbClaConta cla " + 
				"JOIN CtbConta ct ON cla.ID_Conta = ct.ID_Conta " + 
				"JOIN EmpCentroCusto cc ON cla.cdCentroCusto = cc.cdCentroCusto " + 
				"JOIN " + 
				"(" + montaScriptClasseDRE(con, PlanoServiceDAO.getPlanoSelecionado().getNrAno(), 0) +
				") dre ON dre.cdClasse = cla.cdClasse " +
				"LEFT OUTER JOIN CadRelatorioBI bi ON bi.ID_Conta = cla.ID_Conta AND bi.cdCentroCusto = cla.cdCentroCusto " + 
				"LEFT OUTER JOIN CadCampoBI c1 ON c1.cdCampoBI = bi.cdCampoBI1 AND c1.nrAno = %d " + 
				"LEFT OUTER JOIN CadCampoBI c2 ON c2.cdCampoBI = bi.cdCampoBI2 AND c2.nrAno = %d " + 
				"LEFT OUTER JOIN CadCampoBI c3 ON c3.cdCampoBI = bi.cdCampoBI3 AND c3.nrAno = %d " + 
				"LEFT OUTER JOIN CadCampoBI c4 ON c4.cdCampoBI = bi.cdCampoBI4 AND c4.nrAno = %d " + 
				"" + 
				"WHERE cla.nrAno = %d " + 
				"order by dre.cdNivelClasse, ct.cdConta, cla.cdCentroCusto;", 
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno(),
				PlanoServiceDAO.getPlanoSelecionado().getNrAno());
		
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
	
	
	/*
	 * Monta o DRE de forma TABULADA
	 * REGINALDO: utilizo SUBSTRING do codigo + CHAR_LENGTH quando preciso relacionar PAI com FILHO de forma tabulada
	 */
	public static String montaScriptClasseDRE(Connection con, int nroAno, int nivel) throws SQLException, NamingException {
		if (nivel == 0)
			nivel = obtemNivelMaximoDRE(con);
					
		
		String script = String.format("SELECT c%02d.cdClasse, c%02d.cdNivelClasse ", nivel, nivel); 
		
		for (int i = 1; i <= nivel; i++) {
			script += String.format(", c%02d.cdNivelClasse AS cdClasse%02d, c%02d.dsClasse AS dsClasse%02d ", i, i, i, i);
		}
		script += ", gc.dsGrupoClasse "
				+ "FROM CtbClasseDRE c01 ";

		int pos = 0;
		for (int i = 2; i <= nivel; i++) {
			script += String.format(" JOIN CtbClasseDre c%02d  ON c%02d.nrAno = %d "
					+ "AND c%02d.cdnivelClasse = SUBSTRING(c%02d.cdnivelClasse FROM 1 FOR %d) ", 
					i, i, nroAno, (i-1), i, (2 * ++pos));
			
			for (int j = (i-1); j <= i; j++) {
				script += String.format(" AND CHAR_LENGTH(TRIM(c%02d.cdnivelClasse)) = %d ", 
						j, (j * 2));
			}
			
		}
		
		script += String.format("JOIN CtbGrupoClasse gc ON c%02d.nrAno = gc.nrAno AND c%02d.cdGrupoClasse = gc.cdGrupoClasse "
				+ "WHERE c01.nrAno = %d ORDER BY c01.cdNivelClasse", 
				nivel, nivel, nroAno);
		
		for (int i = 2; i <= nivel; i++) {
			script += String.format(", c%02d.cdNivelClasse ", i);
		}
		
		return script; 
	}

	public static int obtemNivelMaximoDRE(Connection con) throws SQLException, NamingException {
		int maxNivel = 1;
		String sql = "SELECT MAX(char_length(cdNivelClasse)) AS maxNivel FROM CtbClasseDRE";

		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);

		if (res.next()) {
			maxNivel = res.getInt("maxNivel") / 2;			
		}
		
		if (maxNivel == 0)
			maxNivel = 1;
		
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		return maxNivel;
	}


	public static List<String> retornarContas(Connection con, String consulta) throws SQLException {
		List<String> lista = new ArrayList<String>();

		Statement stmt = con.createStatement();
		
		consulta = consulta.trim() + "%";
		String sql = String.format("SELECT FIRST 30 cdConta FROM CtbConta WHERE cdConta LIKE '%s' ORDER BY cdConta", 
				consulta);
		
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			lista.add(res.getString("cdConta"));
		}
		
		stmt.close();
		res.close();
		stmt = null;
		res = null;
		
		return lista;
	}	
	

	public static List<String> buscarClassificacao(Connection con, String codConta) throws SQLException {
		List<String> lista = new ArrayList<String>();

		
		String sql = String.format("SELECT cla.cdNivelClasse FROM CtbClaConta clac " + 
				"JOIN CtbConta ct ON clac.ID_Conta = ct.ID_Conta AND ct.cdConta = '%s' " + 
				"JOIN CtbClasseDRE cla ON cla.cdClasse = clac.cdClasse AND cla.nrAno = clac.nrAno " + 
				"WHERE clac.nrAno = %d;", 
				codConta,
				PlanoServiceDAO.getPlanoSelecionado().getNrAno());
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			lista.add(res.getString("cdNivelClasse"));
		}
		
		stmt.close();
		res.close();
		stmt = null;
		res = null;
		
		return lista;
	}
	
	public static ClasseDRE carregaClasseDRE(ResultSet res) throws SQLException {
		ClasseDRE classe = new ClasseDRE();
		
		classe.setCdClasse(res.getInt("cdClasse"));
		classe.setNrAno(res.getInt("nrAno"));
		classe.setDsClasse(res.getString("dsClasse"));
		classe.setGrupo(new GrupoClasseDRE(res.getByte("cdGrupoClasse"), res.getInt("nrAno"), res.getString("dsGrupoClasse")));
		classe.setCdNivelClasse(res.getString("cdNivelClasse"));
		if (LibUtil.existeColuna(res, "TemConta"))
			classe.setTemConta(res.getString("TemConta").equals("S"));
		if (LibUtil.existeColuna(res, "UltNivel"))
			classe.setUltimoNivel(res.getString("UltNivel").equals("S"));

		return classe;
	}

}
