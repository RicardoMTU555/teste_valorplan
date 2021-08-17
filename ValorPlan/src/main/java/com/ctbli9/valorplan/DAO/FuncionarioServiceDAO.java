package com.ctbli9.valorplan.DAO;

import java.math.BigDecimal;
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
import com.ctbli9.valorplan.modelo.FiltroFuncionario;
import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.adm.modelo.CoUsuario;
import ctbli9.enumeradores.AreaSetor;
import ctbli9.modelo.Filial;
import ctbli9.recursos.LibUtil;
import ctbli9.recursos.RegraNegocioException;

public class FuncionarioServiceDAO {
	public static List<Funcionario> listar(Connection con, FiltroFuncionario filtro) 
			throws Exception {
		
		filtro.setNmFuncionario("%" + filtro.getNmFuncionario().toUpperCase() + "%");

		String sql = "WHERE UPPER(func.nmFuncionario) LIKE ? ";
		
		String listaCargos = LibUtil.arrayParaString(filtro.getCargosSelecionados());
		if (listaCargos.length() > 0)
			sql += " AND car.cdCargo in (" + listaCargos.trim() + ")";

		if (filtro.getCentroCusto() != null && filtro.getCentroCusto().getCdCentroCusto() > 0)
			sql += String.format(" AND func.cdCentroCusto = %d ", filtro.getCentroCusto().getCdCentroCusto());
		
		String ativo = "T";
		if (filtro.isAtivo())
			ativo = filtro.isInativo() ? "T" : "S";
		if (filtro.isInativo())
			ativo = filtro.isAtivo() ? "T" : "N";
		
		if (!ativo.equals("T"))
			sql += String.format(" AND func.idAtivo = '%s'", ativo);

		String listaAreas = LibUtil.arrayParaString(filtro.getAreasSelecionadas());
		if (listaAreas.length() > 0)
			sql += " AND cc.cecTpArea in (" + listaAreas.trim() + ")";

		
		sql += "ORDER BY func.nmFuncionario";

		return executaListar(con, sql, filtro.getNmFuncionario());
	}
	
	private static List<Funcionario> executaListar(Connection con, String sqlLista, String nmFuncionario) throws Exception {
		List<Funcionario> funcionarios = new ArrayList<Funcionario>();
		
		sqlLista = montaScriptFuncionario() + sqlLista;
		PreparedStatement pstmt = con.prepareStatement(sqlLista);
		
		if (nmFuncionario != null)
			pstmt.setString(1, nmFuncionario);
		
		ResultSet rs = pstmt.executeQuery();
	    while (rs.next()) {
	    	funcionarios.add(carregarFuncionario(con, rs));
	    }
		
		pstmt.close();
		pstmt = null;
		rs.close();
		rs = null;
		
		return funcionarios;
	}//executaListar
	
	
	public static Funcionario pesquisar(Connection con, int cdFuncionario) throws Exception {
		return executarPesquisa(con, String.format("WHERE func.cdfuncionario = %d", cdFuncionario));
	}
	
	public static Funcionario pesquisarNome(Connection con, String nmFuncionario) throws Exception {
		// TAMANHO DO CAMPO = 60
		nmFuncionario = nmFuncionario.length() > 60 ? nmFuncionario.substring(0, 60) : nmFuncionario;
		return executarPesquisa(con, String.format("WHERE func.nmfuncionario = '%s'", nmFuncionario.trim()));
	}	

	public static Funcionario pesquisarPorLogin(Connection con) throws Exception {
		CoUsuario usuario = LibUtil.getUsuarioSessao();
		Funcionario funcionario = null;
		String sql = null;
		
		if (usuario.isIdtAdmin()) {
			// Usuário administrador tem acesso a todos os departamentos (nível de acesso igual ao do presidente)
			MesAnoOrcamento mesRef = new MesAnoOrcamento();
			sql = String.format("SELECT e.codFuncVinculo FROM EmpDepartamento d "
					+ "JOIN sp_listaRecurso(%d) e ON d.ID_Recurso = e.ID_Recurso "
					+ "WHERE d.nrAno = %d AND d.cdDepartamentoPai IS NULL;", 
					mesRef.getAnoMes(),
					mesRef.getAno());
			Statement stmt = con.createStatement();
			ResultSet res = stmt.executeQuery(sql);
			if (res.next()) {
				funcionario = pesquisar(con, res.getInt("codFuncVinculo"));
			}
			
			res.close();
			res = null;
			stmt.close();
			stmt = null;
		} else {
			
			String loginUsuario = null;
			// TODO Mudar esse campo criar o novo campo
			int posicao = usuario.getLogUsuario().indexOf("." + usuario.getContrato().getNmContrato());
			if (posicao == 0) 
				posicao = usuario.getLogUsuario().length();
				
			loginUsuario = usuario.getLogUsuario().substring(0, posicao);
				
			sql = String.format("WHERE func.logUsuario = '%s'", loginUsuario);
			
			funcionario = executarPesquisa(con, sql);
			
			if (funcionario == null)
				throw new RegraNegocioException("Usuario " + loginUsuario + " nao encontrato no cadastro de Funcionarios.");
		}
		
		usuario = null;
		
		return funcionario;
		
	}//pesquisarPorLogin
	
	public static Funcionario pesquisarPorCPF(Connection con, BigDecimal cpf) throws Exception {
		String sql = String.format("WHERE func.dcFuncionario = %14.0f", cpf);
		
		return executarPesquisa(con, sql);
		
	}//pesquisarPorCPF
	
	private static Funcionario executarPesquisa(Connection con, String sql) throws Exception {
		Funcionario func = null;
		Statement stmt = con.createStatement();
		
		sql = montaScriptFuncionario() + sql;
		
	    ResultSet rs = stmt.executeQuery(sql);
	    if (rs.next()) {
	    	func = carregarFuncionario(con, rs);
	    }
		
		stmt.close();
		rs.close();
		rs = null;

		return func;
	}	
	
	

	/*
	 * Retorna lista de string com os 10 primeiros nomes encontrados com a substring enviada.
	 * Carrega componente autocomplete
	 */
	// TODO REGINALDO: FUNÇÃO DESCONTINUADA
	public static List<String> retornarNomes(Connection con, String consulta) throws SQLException, NamingException {
		List<String> lista = new ArrayList<String>();

		Statement stmt = con.createStatement();
		
		consulta = consulta.trim() + "%";
		String sql = String.format("SELECT FIRST 10 nmFuncionario FROM CadFuncionario WHERE nmFuncionario LIKE '%s'", 
				consulta);
		
		ResultSet res = stmt.executeQuery(sql);
		
		while (res.next()) {
			lista.add(res.getString("nmFuncionario"));
		}
		
		stmt.close();
		res.close();
		stmt = null;
		res = null;
		
		return lista;
	}

	protected static String montaScriptFuncionario() {
		return "SELECT func.*, car.*, cc.cdCentroCusto, cc.cecCdExterno, cc.cecDsResumida, cc.cecDsCompleta, cc.cecTpArea, "
				+ "cdep.cdDepartamento, cdep.sgDepartamento, cdep.dsDepartamento, cdep.ID_Recurso, cc.cdFilial, e.sgFilial "
				+ "FROM CadFuncionario func "
				+ "JOIN CadCargo car ON func.cdCargo = car.cdCargo "
				+ "LEFT OUTER JOIN EmpCentroCusto cc ON func.cdCentroCusto = cc.cdCentroCusto "
				+ "LEFT OUTER JOIN EmpFilial e ON cc.cdFilial = e.cdFilial "
				+ "LEFT OUTER JOIN "
				+ "( "
				+ "  SELECT cdep.cdCentroCusto, dep.cdDepartamento, dep.ID_Recurso, dep.sgDepartamento, dep.dsDepartamento FROM EmpDepartamento dep "
				+ "  JOIN EmpCencusDepto cdep ON cdep.cdDepartamento = dep.cdDepartamento AND dep.nrAno = " + new MesAnoOrcamento().getAno()
				+ ") cdep ON cc.cdCentroCusto = cdep.cdCentroCusto ";
	}
	
	protected static Funcionario carregarFuncionario(Connection con, ResultSet rs) throws SQLException, NamingException {
		Funcionario func = new Funcionario();
    	func.setCdFuncionario(rs.getInt("cdFuncionario"));
    	func.setDcFuncionario(rs.getBigDecimal("dcFuncionario"));
    	func.setNmFuncionario(rs.getString("nmFuncionario"));
    	func.setLogUsuario(rs.getString("logUsuario"));
    	
    	func.setTxEmailFuncionario(rs.getString("txEmail"));
    	func.setFunIdAtivo(rs.getString("idAtivo").equals("S"));
    	
    	CentroCusto cenc = new CentroCusto();
    	cenc.setCdCentroCusto(rs.getInt("cdCentroCusto"));
    	
    	if (rs.getInt("cdCentroCusto") > 0) {
    		cenc.setCecDsCompleta(rs.getString("cecDsCompleta"));
	    	cenc.setCecDsResumida(rs.getString("cecDsResumida"));
	    	cenc.setCodExterno(rs.getString("cecCdExterno"));
	    	
	    	cenc.setDepartamento(new Departamento());
	    	cenc.getDepartamento().setCdDepartamento(rs.getInt("cdDepartamento"));
	    	cenc.getDepartamento().setSgDepartamento(rs.getString("sgDepartamento"));
	    	cenc.getDepartamento().setDsDepartamento(rs.getString("DsDepartamento"));
	    	
	    	cenc.setFilial(new Filial());
	    	cenc.getFilial().setCdFilial(rs.getInt("cdFilial"));
	    	cenc.getFilial().setSgFilial(rs.getString("sgFilial"));

	    	cenc.setTipoArea(AreaSetor.valueOf(rs.getString("cecTpArea")));
	    	cenc.setTipos(CategoriaReceitaServiceDAO.listarCategCencusto(con, rs.getInt("cdCentroCusto")));
	    	
	    	cenc.setResponsavel(new Recurso(new MesAnoOrcamento(), rs.getInt("ID_Recurso")));
	    	
	    	func.setCenCusto(cenc);	
    	}
    	func.setCargo(CargoServiceDAO.carregarCargo(rs));
    	
		return func;
	}

	
	
	public static boolean existe(Connection con, Funcionario funcionario) throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		ResultSet rs = null;
		
		String sql = String.format("SELECT 1 FROM cadFuncionario fc WHERE fc.cdfuncionario = %d",
				funcionario.getCdFuncionario());
	    rs = stmt.executeQuery(sql);

	    boolean retorno = rs.next();
	    
		rs.close();
		rs = null;
		stmt.close();
		stmt = null;
	
		return retorno;
	}//existe

	public static boolean validaLoginRepetido(Connection con, Funcionario funcionario) throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		ResultSet rs = null;
		
		String sql = String.format("SELECT * FROM cadFuncionario fc WHERE fc.logUsuario = '%s' AND fc.cdfuncionario <> %d",
				funcionario.getLogUsuario(),
				funcionario.getCdFuncionario());
	    rs = stmt.executeQuery(sql);

	    boolean retorno = !rs.next(); // Nao encontrou: retorna TRUE
	    
		rs.close();
		rs = null;
		stmt.close();
		stmt = null;
	
		return retorno;
	}//validaLoginRepetido
	
	public static void incluir(Connection con, Funcionario funcionario) throws SQLException, NamingException {
		
		funcionario.setCdFuncionario(ConexaoDB.gerarNovoCodigo(con, "CadFuncionario", "cdFuncionario", null));
		
		String sql = "insert into cadFuncionario values (?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);

		int i = 0;
		pstmt.setInt(++i, funcionario.getCdFuncionario());
		pstmt.setBigDecimal(++i, funcionario.getDcFuncionario());
		pstmt.setString(++i, funcionario.getNmFuncionario());
		pstmt.setString(++i, funcionario.getLogUsuario());
		pstmt.setString(++i, funcionario.getTxEmailFuncionario());
		pstmt.setInt(++i, funcionario.getCargo().getCdCargo());
		if (funcionario.getCenCusto() == null || funcionario.getCenCusto().getCdCentroCusto() == 0)
			pstmt.setNull(++i, java.sql.Types.INTEGER);
		else	
			pstmt.setInt(++i, funcionario.getCenCusto().getCdCentroCusto());
		pstmt.setString(++i, funcionario.getFunIdAtivo() ? "S" : "N"); // OPERADOR TERNARIO

		pstmt.executeUpdate();
		
		pstmt = null;
		
	}//incluir

	
	public static void alterar(Connection con, Funcionario funcionario) throws SQLException, NamingException {
		String sql = "UPDATE cadFuncionario SET "
				+ " cadFuncionario.dcFuncionario = ?, "
    			+ " cadFuncionario.nmfuncionario = ?, "
    			+ " cadFuncionario.logUsuario = ?, "
    			+ " cadFuncionario.txEmail = ?, "
    			+ " cadFuncionario.cdcargo = ?, "
    			+ " cadFuncionario.cdCentroCusto = ?, "
    			+ " cadFuncionario.idAtivo = ? "
    			+ "WHERE cadFuncionario.cdfuncionario = ? ";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		int i = 0;
		pstmt.setBigDecimal(++i, funcionario.getDcFuncionario());
		pstmt.setString(++i, funcionario.getNmFuncionario());
		pstmt.setString(++i, funcionario.getLogUsuario());
		pstmt.setString(++i, funcionario.getTxEmailFuncionario());
		pstmt.setInt(++i, funcionario.getCargo().getCdCargo());
		if (funcionario.getCenCusto() == null || funcionario.getCenCusto().getCdCentroCusto() == 0)
			pstmt.setNull(++i, java.sql.Types.INTEGER);
		else	
			pstmt.setInt(++i, funcionario.getCenCusto().getCdCentroCusto());
		pstmt.setString(++i, funcionario.getFunIdAtivo() ? "S" : "N"); // OPERADOR TERNARIO

		pstmt.setInt(++i, funcionario.getCdFuncionario());
		
		pstmt.executeUpdate();
		
		pstmt = null;
		
	}//alterar

	public static void excluir(Connection con, Funcionario funcionario) throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		String sql = String.format("DELETE from cadFuncionario where cadFuncionario.cdfuncionario = %d",
	    		funcionario.getCdFuncionario());
	    stmt.executeUpdate(sql);
	    
	    stmt.close();
	    stmt = null;
	}//excluir

	
	
	public static void guardarRelacaoFuncionarioSetor(Connection con, BigDecimal cpfFun, String codExterno) throws SQLException, NamingException {
		Statement stmt = con.createStatement();
		
		String sql = String.format("INSERT INTO tmp_FuncionarioSetor VALUES (%14.0f, '%s')",
	    		cpfFun,
	    		codExterno);
	    stmt.executeUpdate(sql);
	    
	    stmt.close();
	    stmt = null;		
	}	
	
	public static void atualizaRelacaoFuncionarioSetor(Connection con, String codExterno) throws Exception {
		Statement stmt = con.createStatement();
		
		String sql = "DELETE FROM tmp_FuncionarioSetor WHERE cpf = ? AND codExterno = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		
		sql = String.format("SELECT * FROM tmp_FuncionarioSetor WHERE codExterno = '%s'", codExterno);
		
	    ResultSet res = stmt.executeQuery(sql);
	    
	    while (res.next()) {
	    	Funcionario func = FuncionarioServiceDAO.pesquisarPorCPF(con, res.getBigDecimal("cpf"));
	    	if (func != null) {
		    	func.setCenCusto(CentroCustoServiceDAO.pesquisarCentroCusto(con, res.getString("codExterno")));
	    		FuncionarioServiceDAO.alterar(con, func);
	    		
		    	pstmt.setBigDecimal(1, func.getDcFuncionario());
		    	pstmt.setString(2, res.getString("codExterno"));
		    	pstmt.executeUpdate();
	    	}
	    }
	    
	    pstmt.close();
	    pstmt = null;
	    
	    res.close();
	    res = null;
	    stmt.close();
	    stmt = null;				
	}

}
