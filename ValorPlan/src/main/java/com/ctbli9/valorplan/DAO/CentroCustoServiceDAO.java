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
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Departamento;
import com.ctbli9.valorplan.modelo.FiltroCentroCusto;
import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.enumeradores.AreaSetor;
import ctbli9.modelo.Filial;
import ctbli9.recursos.LibUtil;


public class CentroCustoServiceDAO {
	
	private static void incluir(Connection con, CentroCusto cenc) throws SQLException, NamingException {
		PreparedStatement pstmt = null;
		
		int cdCentroCusto = ConexaoDB.gerarNovoCodigo(con, "EmpCentroCusto", "cdCentroCusto", null);
		
		cenc.setCdCentroCusto(cdCentroCusto);
		
		String sql = "INSERT INTO EmpCentroCusto values (?, ?, ?, ?, ?, ?)";
		pstmt = con.prepareStatement(sql);
		
		int i = 1;
		pstmt.setInt(i++, cenc.getCdCentroCusto());
		pstmt.setString(i++, cenc.getCodExterno());
		pstmt.setInt(i++, cenc.getFilial().getCdFilial());
		pstmt.setString(i++, LibUtil.truncaTexto(cenc.getCecDsResumida(), 30));
		pstmt.setString(i++, LibUtil.truncaTexto(cenc.getCecDsCompleta(), 100));
		
		pstmt.setString(i++, cenc.getTipoArea().toString());
		
		pstmt.executeUpdate();

		
		pstmt.close();
		gravarCencusDepto(con, cenc);
		gravarCencusCatrec(con, cenc.getCdCentroCusto(), cenc.getTipos());
	}

	private static void gravarCencusDepto(Connection con, CentroCusto cenc) 
			throws SQLException, NamingException {
		String sql = null;
		
		// Verifica se o centro de custo está em um departamento diferente do enviado
		sql = String.format("SELECT cdDepartamento FROM EmpCencusDepto WHERE cdCentroCusto = %d "
				+ "AND cdDepartamento <> %d "
				+ "AND cdDepartamento IN "
				+ "(SELECT cdDepartamento FROM EmpDepartamento WHERE nrAno = %d)", 
				cenc.getCdCentroCusto(),
				cenc.getDepartamento().getCdDepartamento(),
				cenc.getDepartamento().getNrAno());
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		if (res.next()) {
			sql = "DELETE FROM EmpCencusDepto WHERE cdDepartamento = ? AND cdCentroCusto = ?";
			PreparedStatement pstmtDel = con.prepareStatement(sql);
			
			int i = 1;
			pstmtDel.setLong(i++, res.getLong("cdDepartamento"));
			pstmtDel.setInt(i++, cenc.getCdCentroCusto());
			pstmtDel.executeUpdate();
			pstmtDel.close();
			pstmtDel = null;

		} 
		
		
		sql = String.format("SELECT distinct 1 FROM EmpCencusDepto WHERE cdDepartamento = %d AND cdCentroCusto = %d ", 
				cenc.getDepartamento().getCdDepartamento(),
				cenc.getCdCentroCusto());
		
		Statement stmt2 = con.createStatement();
		ResultSet res2 = stmt2.executeQuery(sql);
		
		if (!res2.next()) {
			sql = "INSERT INTO EmpCencusDepto VALUES (?, ?, ?)";
			PreparedStatement pstmtIns = con.prepareStatement(sql);
			
			int i = 1;
			pstmtIns.setLong(i++, cenc.getDepartamento().getCdDepartamento());
			pstmtIns.setInt(i++, cenc.getCdCentroCusto());
			
			if (cenc.getResponsavel() == null || cenc.getResponsavel().getCdRecurso() == 0)
				pstmtIns.setNull(i++, java.sql.Types.BIGINT);
			else
				pstmtIns.setLong(i++, cenc.getResponsavel().getCdRecurso());
						
			pstmtIns.executeUpdate();
			pstmtIns.close();
			pstmtIns = null;
			
		} else {
			sql = "UPDATE EmpCencusDepto SET ID_Recurso = ? "
					+ "WHERE cdDepartamento = ? AND cdCentroCusto = ?";
			PreparedStatement pstmtUpd = con.prepareStatement(sql);
			
			int i = 1;
			if (cenc.getResponsavel() == null || cenc.getResponsavel().getCdRecurso() == 0)
				pstmtUpd.setNull(i++, java.sql.Types.BIGINT);
			else
				pstmtUpd.setLong(i++, cenc.getResponsavel().getCdRecurso());
			
			pstmtUpd.setLong(i++, cenc.getDepartamento().getCdDepartamento());
			pstmtUpd.setInt(i++, cenc.getCdCentroCusto());
			pstmtUpd.executeUpdate();
			pstmtUpd = null;
		}
		

		res.close();
		res = null;
		
		stmt.close();
		stmt = null;

		res2.close();
		res2 = null;
		
		stmt2.close();
		stmt2 = null;

	}

	private static void gravarCencusCatrec(Connection con, int cdCentroCusto, List<CategoriaReceita> tipos) 
			throws SQLException, NamingException {
		PreparedStatement pstmt = null;
		
		
		String sql = "DELETE FROM CadCencusCatrec WHERE cdCentroCusto = ?";
		
		pstmt = con.prepareStatement(sql);
		int i = 1;
		pstmt.setInt(i++, cdCentroCusto);
		pstmt.executeUpdate();
		pstmt.close();
		
		sql = "INSERT INTO CadCencusCatrec VALUES (?, ?)";
		
		pstmt = con.prepareStatement(sql);
		
		for (CategoriaReceita tipoReceita : tipos) {
			i = 1;
			pstmt.setInt(i++, cdCentroCusto);
			pstmt.setInt(i++, tipoReceita.getCdCategoria());
			pstmt.executeUpdate();
		}
		pstmt.close();
		
		pstmt = null;
	}

	private static void alterar(Connection con, CentroCusto cenc) throws SQLException, NamingException {
		PreparedStatement pstmt = null;
				
		String sql = "UPDATE EmpCentroCusto SET "
				+ "cecCdExterno = ?, "
				+ "cdFilial = ?, "
				+ "cecDsResumida = ?, "
				+ "cecDsCompleta = ?, "
				+ "cecTpArea = ? "
				+ "WHERE cdCentroCusto = ? ";
		pstmt = con.prepareStatement(sql);
		
		int i = 1;
		pstmt.setString(i++, cenc.getCodExterno());
		pstmt.setInt(i++, cenc.getFilial().getCdFilial());
		pstmt.setString(i++, cenc.getCecDsResumida());
		pstmt.setString(i++, cenc.getCecDsCompleta());
		
		pstmt.setString(i++, cenc.getTipoArea().toString());
		pstmt.setInt(i++, cenc.getCdCentroCusto());

		pstmt.executeUpdate();
		
		pstmt.close();
		
		gravarCencusDepto(con, cenc);
		gravarCencusCatrec(con, cenc.getCdCentroCusto(), cenc.getTipos());
	}

	public static void excluir(Connection con, CentroCusto centroCusto) throws SQLException, NamingException {
		Statement stmt = con.createStatement();

		String sql = String.format("DELETE FROM CadCencusCatrec WHERE cdCentroCusto = %d", centroCusto.getCdCentroCusto());
		stmt.executeUpdate(sql);
		
			sql = String.format("DELETE FROM EmpCencusDepto WHERE cdCentroCusto = %d", centroCusto.getCdCentroCusto());
		stmt.executeUpdate(sql);
		
		sql = String.format("DELETE FROM EmpCentroCusto WHERE cdCentroCusto = %d", centroCusto.getCdCentroCusto());
		stmt.executeUpdate(sql);
	}

	public static void salvar(Connection con, CentroCusto centroCusto) throws SQLException, NamingException {
		if (centroCusto.getCdCentroCusto() == 0)
			incluir(con, centroCusto);
		else
			alterar(con, centroCusto);
		
	}

	

	
	
	public static CentroCusto pesquisarCentroCusto(Connection con, int cdCenCusto) throws Exception {
		String sql = String.format("WHERE cc.cdCentroCusto = %d", cdCenCusto);
		return executaPesquisa(con, sql);
	}
	
	public static CentroCusto pesquisarCentroCusto(Connection con, String codExterno) throws Exception {
		String sql = String.format("WHERE cc.cecCdExterno = '%s'", codExterno.trim());
		return executaPesquisa(con, sql);
	}
	
	private static CentroCusto executaPesquisa(Connection con, String sql) throws Exception {
		Statement stmt = con.createStatement();
		CentroCusto cenc = null;
		
		sql = montaScriptCentroCusto() + sql;
		
		ResultSet rs = stmt.executeQuery(sql);
	    if(rs.next()) {
	    	cenc = carregaCentroCusto(con, rs);
	    }
		
	    rs.close();
		stmt.close();
	    rs = null;
		stmt = null;
		
		return cenc;
	}//pesquisarCentroPorSetor

	public static CentroCusto carregaCentroCusto(Connection con, ResultSet rs) throws Exception {
		return carregaCentroCusto(con, rs, true);
	}
	
	public static List<CentroCusto> listar(Connection con, FiltroCentroCusto filtro) throws Exception {
		List<CentroCusto> centrosCusto = new ArrayList<CentroCusto>();
		
		String sql = montaScriptCentroCusto();
		
		if (filtro.isSetorDoGestor()) {
			CentroCustoServiceDAO.montarCentrosCustoGerente(con, Global.getFuncionarioLogado(), new MesAnoOrcamento().getAnoMes());
			sql += "JOIN TmpCencus ON TmpCencus.cdCentroCusto = cc.cdCentroCusto ";
		}
				
		filtro.setDescrResumida(filtro.getDescrResumida() + "%");
		sql += " WHERE cc.cecDsResumida LIKE ? ";
		
		
		if (filtro.getCdFilial() > 0)
			sql += String.format(" AND cc.cdFilial = %d ", filtro.getCdFilial());
		
		if (filtro.getCdDepartamento() > 0)
			sql += String.format(" AND cdep.cdDepartamento = %d", filtro.getCdDepartamento());
		
		String areas = LibUtil.arrayParaString(filtro.getAreas());
		if (!areas.isEmpty())
			sql += String.format(" AND cc.cecTpArea IN (%s)", areas);
		
		String departamentos = LibUtil.arrayParaString(filtro.getDepartamentos());
		if (!departamentos.isEmpty())
			sql += String.format(" AND dep.cdDepartamento IN (%s)", departamentos);
		
		sql += "ORDER BY cc.cecCdExterno";
		System.out.println(sql);
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setString(++i, filtro.getDescrResumida());
		
		ResultSet rs = pstmt.executeQuery();
	    while (rs.next()) {
	    	centrosCusto.add(carregaCentroCusto(con, rs));
	    }//end while
		
	    pstmt.close();
	    pstmt = null;
		rs.close();
		rs = null;
		
		return centrosCusto;
	}
	
	
	public static String montaScriptCentroCusto() {
		// TODO Testar isso
		return String.format("SELECT cc.*, eq.*, "
				+ "cdep.cdDepartamento, dep.sgDepartamento, dep.dsDepartamento, cc.cdFilial AS codFilialSetor, e.sgFilial AS sigFilialSetor, "
				+ "cdep.ID_Recurso "
				+ "FROM EmpCentroCusto cc "
				+ "JOIN EmpFilial e ON cc.cdFilial = e.cdFilial "
				+ "LEFT OUTER JOIN EmpCencusDepto cdep ON cc.cdCentroCusto = cdep.cdCentroCusto AND cdep.cdDepartamento IN "
				+ "(select cdDepartamento from EmpDepartamento where nrAno = " + PlanoServiceDAO.getPlanoSelecionado().getNrAno() + ") "
				+ EquipeServiceDAO.montaScriptRelacaoEquipe("cdep")
				+ "LEFT OUTER JOIN EmpDepartamento dep ON cdep.cdDepartamento = dep.cdDepartamento AND dep.nrAno = %d"
				+ " ",
				PlanoServiceDAO.getPlanoSelecionado().getNrAno());
	}
	
	public static CentroCusto carregaCentroCusto(Connection con, ResultSet rs, boolean carregaFilhos) throws Exception {
		CentroCusto cenc = new CentroCusto();
    	cenc.setCdCentroCusto(rs.getInt("cdCentroCusto"));
    	cenc.setCecDsCompleta(rs.getString("cecDsCompleta"));
    	cenc.setCecDsResumida(rs.getString("cecDsResumida"));
    	cenc.setCodExterno(rs.getString("cecCdExterno"));
    	
    	cenc.setDepartamento(new Departamento());
    	cenc.getDepartamento().setCdDepartamento(rs.getInt("cdDepartamento"));
    	cenc.getDepartamento().setSgDepartamento(rs.getString("sgDepartamento"));
    	cenc.getDepartamento().setDsDepartamento(rs.getString("DsDepartamento"));
    	
    	cenc.setFilial(new Filial());
    	cenc.getFilial().setCdFilial(rs.getInt("codFilialSetor"));
    	cenc.getFilial().setSgFilial(rs.getString("sigFilialSetor"));
    	
    	cenc.setResponsavel(EquipeServiceDAO.carregaRecurso(rs, null));
    	
    	cenc.setTipoArea(AreaSetor.valueOf(rs.getString("cecTpArea")));
    	
    	if (carregaFilhos) {
    		cenc.setTipos(CategoriaReceitaServiceDAO.listarCategCencusto(con, rs.getInt("cdCentroCusto")));
    	} else {
    		cenc.setTipos(new ArrayList<>());
    	}
    	return cenc;
	}

	
	public static void montarCentrosCustoGerente(Connection con, Funcionario func, Integer anoMesRef) 
			throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		if (anoMesRef == null)
			anoMesRef = new MesAnoOrcamento().getAnoMes();

		String sql = "DELETE FROM TmpCencus";
		stmt.executeUpdate(sql);

		sql = String.format("INSERT INTO TmpCencus SELECT DISTINCT cdCentroCusto FROM sp_Cencusto_Gerente(%d, %d)", 
				func.getCdFuncionario(),
				anoMesRef);
		System.out.println(sql);
		stmt.executeUpdate(sql);
		
		stmt.close();
		stmt = null;
	}

	/*
	 colocar o tratamento para pegar os centros de custo de ORCCENCUSRESPONS do funcionário logado
	 se não existir, então pega do "montarCentrosCustoGerente"	
	*/
	public static boolean montarCentrosCustoResponsavel(Connection con, Funcionario func) 
			throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		MesAnoOrcamento mesRef = new MesAnoOrcamento();
		
		String sql = "DELETE FROM TmpCencus";
		stmt.executeUpdate(sql);

		sql = String.format("INSERT INTO TmpCencus "
				+ "SELECT DISTINCT cdCentroCusto FROM OrcCencusRespon WHERE nrAno = %d AND ID_Recurso IN "
				+ "(select ID_Recurso from sp_listaRecurso(%d) where codFuncVinculo = %d)", 
				mesRef.getAno(),
				mesRef.getAnoMes(),
				func.getCdFuncionario());
		System.out.println(sql);
		stmt.executeUpdate(sql);
		
		sql = "select count(*) as quant from TmpCencus";
		ResultSet res = stmt.executeQuery(sql);
		boolean retorno = false;
		
		if (res.next())
			retorno = (res.getInt("quant") > 0);
			
		mesRef = null;
					
		res.close();
		res = null;
		
		stmt.close();
		stmt = null;
		
		return retorno;
	}
	
	public static void montarCentrosDoDepartamento(Connection con, long codDepto) 
			throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		String sql = "DELETE FROM TmpCencus";
		stmt.executeUpdate(sql);

		sql = String.format("INSERT INTO TmpCencus SELECT distinct cdCentroCusto FROM spi_procura_filho(%d)", 
				codDepto);
		stmt.executeUpdate(sql);
		System.out.println(sql);
		stmt.close();
		stmt = null;
	}

}
