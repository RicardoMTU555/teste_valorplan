package com.ctbli9.valorplan.negocio.procavulso;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.EquipeServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.DAO.versao1.FuncionarioVersao1DAO;
import com.ctbli9.valorplan.enumeradores.TipoRecurso;
import com.ctbli9.valorplan.modelo.Cargo;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.negocio.importar.ImportadorService;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.enumeradores.TipoCargo;

public class ProcessamentoService {
	private ConexaoDB conAtual;
	private ConexaoDB conAnterior;
	
	private int nroAno = 2021;
	
	public ProcessamentoService(ConexaoDB con) {
		this.conAtual = con;
	}

	public void processa() throws SQLException {
		// 1.1 foreach
		
		String sql = "SELECT r.cdReceita, ct.cdConta, ct.dsConta "
				+ "FROM cadReceita r "
				+ "JOIN ctbConta ct ON r.id_contaReceita = ct.id_conta;";
		Statement stmt = this.conAtual.getConexao().createStatement();
		Statement stmt2 = this.conAtual.getConexao().createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		int i = 0;
		while (res.next()) {
		
			//1.2 insere tabela 1
			sql = String.format("INSERT INTO cadDeducaoVenda "
					+ "SELECT %d, dv.id_conta " + 
					" FROM cadDeducaoVenda_tmp dv " + 
					" JOIN cadReceita_tmp r ON dv.cdReceita = r.cdReceita " + 
					" JOIN ctbConta ctr ON r.id_contaReceita = ctr.id_conta " + 
					" join ctbConta ctd ON dv.id_conta = ctd.id_conta " + 
					" WHERE ctr.cdConta = '%s';",
					res.getInt("cdReceita"),
					res.getString("cdConta"));
			stmt2.executeUpdate(sql);
			
			//1.3 insere talbela 2
			sql = String.format("INSERT INTO orcParamDeducao "
					+ "SELECT dv.cdplano, %d, " + 
					"       dv.id_recurso, dv.id_conta, dv.nranomes, dv.pcdespesa " + 
					"FROM orcParamDeducao_tmp dv " + 
					"JOIN cadReceita_tmp r ON dv.cdReceita = r.cdReceita " + 
					"JOIN ctbConta ctr ON r.id_contaReceita = ctr.id_conta " + 
					"JOIN ctbConta ctd ON dv.id_conta = ctd.id_conta " + 
					"WHERE ctr.cdConta = '%s';",
					res.getInt("cdReceita"),
					res.getString("cdConta"));
			
			stmt2.executeUpdate(sql);
			
			System.out.println(++i + " ==> " + res.getInt("cdReceita") + " || " + res.getString("cdConta"));
			
		}
	}
	
	
	public void migrarBanco(ConexaoDB con) throws Exception {
		this.conAnterior = con;

		
		migrarTabelaIgual("ORCPLANO", "ORCPLANO"); // ORCPLANO
		ConexaoDB.gravarTransacao(conAtual);
		
		migrarCategReceita(); // CADCATEGRECEITA
		ConexaoDB.gravarTransacao(conAtual);
		
		migrarTabelaIgual("CADTIPORECEITA", "CADTIPORECEITA"); // CADTIPORECEITA
		ConexaoDB.gravarTransacao(conAtual);
				
		migrarTabelaIgual("EMPFILIAL", "EMPFILIAL"); // EMPFILIAL
		ConexaoDB.gravarTransacao(conAtual);
		
		migrarTabelaIgual("CADCARGO", "CADCARGO"); // CADCARGO
		ConexaoDB.gravarTransacao(conAtual);
		
		migrarEmpCentroCusto(); //EMPCENTROCUSTO
		ConexaoDB.gravarTransacao(conAtual);
		
		migrarTabelaIgual("CADFUNCIONARIO", "CADFUNCIONARIO"); // CADFUNCIONARIO
		ConexaoDB.gravarTransacao(conAtual);
		
		migrarOrcEquipe(); //ORCEQUIPE
		ConexaoDB.gravarTransacao(conAtual);
		
		
		
		
		
		
		
		migrarEmpDepartamento(); //EMPDEPARTAMENTO
		ConexaoDB.gravarTransacao(conAtual);
		
		migrarCtbConta(); //CTBCONTA
		ConexaoDB.gravarTransacao(conAtual);

		migrarCadReceita(); //CADRECEITA
		ConexaoDB.gravarTransacao(conAtual);

		migrarCadDeducaoVenda(); //CADDEDUCAOVENDA
		ConexaoDB.gravarTransacao(conAtual);


		migrarTabelaIgual("EMPCENCUSTIPREC", "EMPCENCUSTIPREC"); // EMPCENCUSTIPREC
		ConexaoDB.gravarTransacao(conAtual);
		

		migrarTabelaIgual("CTBGRUPOCLASSE", "CTBGRUPOCLASSE"); // EMPCENCUSTIPREC
		ConexaoDB.gravarTransacao(conAtual);
		
		migrarCtbEstornaConta(); // CTBESTORNACONTA
		ConexaoDB.gravarTransacao(conAtual);
		
		migrarTabelaIgual("CTBCLASSEDRE", "CTBCLASSEDRE"); // CTBCLASSEDRE 
		ConexaoDB.gravarTransacao(conAtual);
		
		migrarCtbClaConta(); // CTBCLACONTA
		ConexaoDB.gravarTransacao(conAtual);

		migrarTabelaIgual("CADCAMPOBI", "CADCAMPOBI"); // CADCAMPOBI
		ConexaoDB.gravarTransacao(conAtual);
		
		migrarCadRelatorioBI(); // CADRELATORIOBI
		ConexaoDB.gravarTransacao(conAtual);

		migrarEmpCencusDepto(); //EMPCENCUSDEPTO 
		ConexaoDB.gravarTransacao(conAtual);
		
		/*
		migrarGestorDepartamento();
		//migrarGestorCentroCusto;
		*/
		
		
		
				

	}
	
	private void migrarCategReceita() throws SQLException {
		if (migrouTabela("CADCATEGRECEITA"))
			return;

		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		PreparedStatement stmtAtuCateg = null;
		
		String sql = "SELECT * FROM CadCategReceita";
		ResultSet resAnt = stmtAnt.executeQuery(sql);

		sql = "INSERT INTO CadCategReceita VALUES (?, ?, null)";
		stmtAtuCateg = this.conAtual.getConexao().prepareStatement(sql);

		while (resAnt.next()) {
			int i = 0;
			stmtAtuCateg.setInt(++i, resAnt.getInt("cdCategoria"));
			stmtAtuCateg.setString(++i, resAnt.getString("dsCategoria"));
			stmtAtuCateg.executeUpdate();
		}
		
		stmtAnt.close();
		sql = "UPDATE CadCategReceita SET tpOrcamento = (SELECT r2.tprTpOrcamento FROM CadTipoReceita r2 WHERE r2.cdTipoReceita = CadCategReceita.cdTipoReceita)";
		stmtAtuCateg = this.conAtual.getConexao().prepareStatement(sql);
		stmtAtuCateg.executeUpdate();
		
		stmtAnt.close();
		stmtAtuCateg.close();
		
		stmtAnt = null;
		stmtAtuCateg = null;
		
		gravarLogTabela("CADCATEGRECEITA");
	}

	private void migrarEmpCentroCusto() throws Exception {
		if (migrouTabela("EMPCENTROCUSTO"))
			return;

		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		PreparedStatement stmtAtuSetor = null;
		
		String sql = "SELECT * FROM EmpCentroCusto";
		ResultSet resAnt = stmtAnt.executeQuery(sql);

		sql = "INSERT INTO EmpCentroCusto VALUES (?, ?, ?, ?, ?, ?)";
		stmtAtuSetor = this.conAtual.getConexao().prepareStatement(sql);

		while (resAnt.next()) {
			int i = 0;
			stmtAtuSetor.setInt(++i, resAnt.getInt("cdCentroCusto"));
			stmtAtuSetor.setString(++i, resAnt.getString("ceccdexterno"));
			stmtAtuSetor.setInt(++i, resAnt.getInt("cdfilial"));
			stmtAtuSetor.setString(++i, resAnt.getString("cecdsresumida"));
			stmtAtuSetor.setString(++i, resAnt.getString("cecdscompleta"));
			stmtAtuSetor.setString(++i, resAnt.getString("cectparea"));
			stmtAtuSetor.executeUpdate();
		}
		
		stmtAnt.close();
		stmtAtuSetor.close();
		
		stmtAnt = null;
		stmtAtuSetor = null;
		
		gravarLogTabela("EMPCENTROCUSTO");
	}
	
	private void migrarOrcEquipe() throws SQLException, NamingException {
		if (migrouTabela("ORCEQUIPE"))
			return;

		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		
		String sql = "SELECT * FROM CadFuncionario func JOIN CadCargo cargo ON func.cdCargo = cargo.cdCargo";
		ResultSet resAnt = stmtAnt.executeQuery(sql);

		
		while (resAnt.next()) {
			Cargo cargo = new Cargo(resAnt.getInt("cdCargo"), resAnt.getString("dsCargo"), TipoCargo.valueOf(resAnt.getString("tpCargo")));
			
			Recurso recurso = new Recurso();
			recurso.setCdRecurso(0);
			recurso.setCargo(cargo);
			
			CentroCusto setor = new CentroCusto();
			setor.setCdCentroCusto(resAnt.getInt("cdCentroCusto"));
			
			Funcionario vinculo = new Funcionario();
			vinculo.setCdFuncionario(resAnt.getInt("cdFuncionario"));
			vinculo.setNmFuncionario(resAnt.getString("nmFuncionario"));
			vinculo.setCargo(cargo);
			vinculo.setCenCusto(setor);
			
			recurso.setFimVinculo(null);
			recurso.setInicioVinculo(Integer.parseInt(this.nroAno + "01"));
			
			recurso.setNmRecurso("Novo " + cargo.getDsCargo().toLowerCase());
			recurso.setNrAno(this.nroAno);
			recurso.setSetor(setor);
			recurso.setTipo(TipoRecurso.O);
			recurso.setVinculo(vinculo);
			
			EquipeServiceDAO.incluir(this.conAtual.getConexao(), recurso);
			
		}
		
		stmtAnt.close();
		stmtAnt = null;
		
		gravarLogTabela("ORCEQUIPE");
	}

	
	private void migrarTabela() throws SQLException {
		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		Statement stmtAtu = this.conAtual.getConexao().createStatement();
		
		
		stmtAnt.close();
		stmtAtu.close();
		
		stmtAnt = null;
		stmtAtu = null;
		
	}

	private void migrarEmpDepartamento() throws Exception {
		if (migrouTabela("EMPDEPARTAMENTO"))
			return;
		
		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		PreparedStatement stmtAtu = null;
		
		String sql = "SELECT * FROM EmpDepartamento";
		ResultSet resAnt = stmtAnt.executeQuery(sql);

		sql = "INSERT INTO EmpDepartamento VALUES (?, ?, ?, ?, ?, null)";
		stmtAtu = this.conAtual.getConexao().prepareStatement(sql);

		
		while (resAnt.next()) {
			int i = 0;
			stmtAtu.setLong(++i, resAnt.getLong("cdDepartamento"));
			stmtAtu.setInt(++i, resAnt.getInt("nrano"));
			stmtAtu.setString(++i, resAnt.getString("sgDepartamento"));
			stmtAtu.setString(++i, resAnt.getString("dsDepartamento"));
			if (resAnt.getLong("cdDepartamentoPai") == 0)
				stmtAtu.setNull(++i, java.sql.Types.BIGINT);
			else
				stmtAtu.setLong(++i, resAnt.getLong("cdDepartamentoPai"));
			
			stmtAtu.executeUpdate();
			    
		}
		
		stmtAnt.close();
		stmtAtu.close();
		
		stmtAnt = null;
		stmtAtu = null;
		
		gravarLogTabela("EMPDEPARTAMENTO");
		
	}
	
	
	private void migrarCtbConta() throws Exception {
		if (migrouTabela("CTBCONTA"))
			return;

		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		PreparedStatement stmtAtu = null;
		
		String sql = "SELECT * FROM CtbConta";
		ResultSet resAnt = stmtAnt.executeQuery(sql);

		sql = "INSERT INTO CtbConta (ID_Conta, cdConta, sgConta, dsConta, cdContaReduzi, ctaIdNatureza, ctaIdGrupo, tpConta, cdClasse) VALUES "
				+    String.format("(      %s,       ?,       ?,       ?,             ?,             ?,          ?,       ?,     null)",
						"(select (COALESCE(max(ID_Conta),0) + 1) from CtbConta)");
		stmtAtu = this.conAtual.getConexao().prepareStatement(sql);

		while (resAnt.next()) {
			String sigla = new ImportadorService().gerarSigla(resAnt.getString("dsConta"));
			int i = 0;
			stmtAtu.setString(++i, resAnt.getString("cdconta"));
			stmtAtu.setString(++i, sigla);
			stmtAtu.setString(++i, resAnt.getString("dsConta"));
			stmtAtu.setString(++i, resAnt.getString("cdContaReduzi"));
			stmtAtu.setString(++i, resAnt.getString("ctaIdNatureza"));
			stmtAtu.setString(++i, resAnt.getString("ctaIdGrupo"));
			stmtAtu.setString(++i, defineTipo(resAnt.getString("cdconta")));
			stmtAtu.executeUpdate();
		}
		
		stmtAnt.close();
		stmtAtu.close();
		
		stmtAnt = null;
		stmtAtu = null;
		
		gravarLogTabela("CTBCONTA");
	}

	private String defineTipo(String codConta) throws SQLException {
		String tipo = "";
		
		String sql = "";
		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		ResultSet res = null;
		
		sql = "select distinct 1 from cadReceita where cdContaReceita = '" + codConta + "'";
		res = stmtAnt.executeQuery(sql);
		if (res.next()) {
			tipo = "RE";  //RE("Receitas")
		} else {
			sql = "select distinct 1 from cadDeducaovenda where cdDespesa in (select cdDespesa from cadDespesa where cdContaDespesa = '" + 
					codConta + "')";
			res = stmtAnt.executeQuery(sql);
			if (res.next()) {
				tipo = "DV";  //DV("Deducao")
				
			} else {
				sql = "select distinct 1 from cadDespesa where tpDespesa = 'S' and cdContaDespesa = '" + codConta + "'";
				res = stmtAnt.executeQuery(sql);
				if (res.next()) {
					tipo = "SL";  //SL("Salario")
					
				} else {
					sql = "select distinct 1 from cadDespesa where tpDespesa = 'P' and cdContaDespesa = '" + codConta + "'";
					res = stmtAnt.executeQuery(sql);
					if (res.next()) {
						tipo = "DP";  //DP("Desp.Pessoal")
						
					} else {
						sql = "select distinct 1 from cadDespesa where tpDespesa = 'O' and cdContaDespesa = '" + codConta + "'";
						res = stmtAnt.executeQuery(sql);
						if (res.next()) {
							tipo = "DO";  //DO("Desp.Geral")
							
						} else {
							if (codConta.startsWith("4"))
								tipo = "CT"; //CT("Custo")
						}
					}
				}
			}
		}
		
		res = null;
		stmtAnt.close();
		stmtAnt = null;
		return tipo;
	}

	public void migrarCadReceita() throws SQLException {
		if (migrouTabela("CADRECEITA"))
			return;

		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		
		String sql = "SELECT * FROM CadReceita";
		ResultSet resAnt = stmtAnt.executeQuery(sql);
		
		sql = "INSERT INTO CadReceita (cdReceita, sgReceita, dsReceita, cdCategoria, ID_Contareceita, idAtivo) VALUES "
				+                    "(        ?,         ?,         ?,           ?,               ?,       ?)";
		
		PreparedStatement stmtAtu = null;
		stmtAtu = this.conAtual.getConexao().prepareStatement(sql);

		while (resAnt.next()) {
			int idConta = procurarNovaConta(resAnt.getString("cdContaReceita"));
			
			int i = 0;
			stmtAtu.setInt(++i, resAnt.getInt("cdReceita"));
			stmtAtu.setString(++i, resAnt.getString("sgReceita"));
			stmtAtu.setString(++i, resAnt.getString("dsReceita"));
			stmtAtu.setInt(++i, resAnt.getInt("cdCategoria"));
			stmtAtu.setInt(++i, idConta);
			stmtAtu.setString(++i, resAnt.getString("idAtivo"));
			stmtAtu.executeUpdate();
		}
		
		stmtAnt.close();
		stmtAtu.close();
		
		
		stmtAnt = null;
		stmtAtu = null;
		
		gravarLogTabela("CADRECEITA");
	}
	
	
	public void migrarCadDeducaoVenda() throws SQLException {
		if (migrouTabela("CADDEDUCAOVENDA"))
			return;

		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		
		String sql = "SELECT dv.*, cad.cdContaDespesa "
				+ "FROM CadDeducaoVenda dv JOIN CadDespesa cad ON dv.cdDespesa = cad.cdDespesa";
		ResultSet resAnt = stmtAnt.executeQuery(sql);
		
		sql = "INSERT INTO CadDeducaoVenda (cdReceita, ID_Conta) VALUES "
				+                         "(        ?,        ?)";
		
		PreparedStatement stmtAtu = null;
		stmtAtu = this.conAtual.getConexao().prepareStatement(sql);

		while (resAnt.next()) {
			int idConta = procurarNovaConta(resAnt.getString("cdContaDespesa"));
						
			int i = 0;
			stmtAtu.setInt(++i, resAnt.getInt("cdReceita"));
			stmtAtu.setInt(++i, idConta);
			stmtAtu.executeUpdate();
		}
		
		stmtAnt.close();
		stmtAtu.close();
		
		stmtAnt = null;
		stmtAtu = null;
		
		gravarLogTabela("CADDEDUCAOVENDA");
	}
	
	private void migrarCtbEstornaConta() throws SQLException {
		if (migrouTabela("CTBESTORNACONTA"))
			return;

		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		PreparedStatement stmtAtuSetDep = null;
		
		String sql = "SELECT * FROM ctbExtornaConta";
		ResultSet resAnt = stmtAnt.executeQuery(sql);

		sql = "INSERT INTO ctbEstornaConta VALUES (?, ?)";
		stmtAtuSetDep = this.conAtual.getConexao().prepareStatement(sql);

		try {
			while (resAnt.next()) {
				int idConta = procurarNovaConta(resAnt.getString("cdConta"));
				
				int i = 0;			
				stmtAtuSetDep.setInt(++i, idConta);
				stmtAtuSetDep.setInt(++i, resAnt.getInt("cdCentroCusto"));
				stmtAtuSetDep.executeUpdate();
				    
			}
		} catch (Exception e) {
			System.out.println("aqui");
			System.out.println(e.getMessage());
		}
		
		stmtAnt.close();
		stmtAtuSetDep.close();
		
		stmtAnt = null;
		stmtAtuSetDep = null;
		
		gravarLogTabela("CTBESTORNACONTA");
		
	}	
	
	private void migrarCtbClaConta() throws SQLException {
		if (migrouTabela("CTBCLACONTA"))
			return;

		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		PreparedStatement stmtAtuSetDep = null;
		
		String sql = "SELECT * FROM CtbClaConta";
		ResultSet resAnt = stmtAnt.executeQuery(sql);

		sql = "INSERT INTO CtbClaConta VALUES (?, ?, ?, ?)";
		stmtAtuSetDep = this.conAtual.getConexao().prepareStatement(sql);

		try {
			int contador = 0;
			while (resAnt.next()) {
				int idConta = procurarNovaConta(resAnt.getString("cdConta"));
				
				int i = 0;			
				stmtAtuSetDep.setInt(++i, resAnt.getInt("nrAno"));
				stmtAtuSetDep.setInt(++i, idConta);
				stmtAtuSetDep.setInt(++i, resAnt.getInt("cdCentroCusto"));
				stmtAtuSetDep.setInt(++i, resAnt.getInt("cdClasse"));
				stmtAtuSetDep.executeUpdate();
				    
				if ((++contador % 5000) == 0)
					ConexaoDB.gravarTransacao(conAtual);
				
				System.out.println(contador);
			}
		} catch (Exception e) {
			System.out.println("aqui");
			System.out.println(e.getMessage());
		}
		
		stmtAnt.close();
		stmtAtuSetDep.close();
		
		stmtAnt = null;
		stmtAtuSetDep = null;
		
		gravarLogTabela("CTBCLACONTA");
		
	}
	
	private void migrarCadRelatorioBI() throws SQLException {
		if (migrouTabela("CADRELATORIOBI"))
			return;

		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		PreparedStatement stmtAtuSetDep = null;
		
		String sql = "SELECT * FROM CadRelatorioBI";
		ResultSet resAnt = stmtAnt.executeQuery(sql);

		sql = "INSERT INTO CadRelatorioBI VALUES (?, ?, ?, ?, ?, ?, ?)";
		stmtAtuSetDep = this.conAtual.getConexao().prepareStatement(sql);

		try {
			int contador = 0;
			while (resAnt.next()) {
				int idConta = procurarNovaConta(resAnt.getString("cdConta"));
				
				int i = 0;			
				stmtAtuSetDep.setInt(++i, resAnt.getInt("nrAno"));
				stmtAtuSetDep.setInt(++i, idConta);
				stmtAtuSetDep.setInt(++i, resAnt.getInt("cdCentroCusto"));
				stmtAtuSetDep.setInt(++i, resAnt.getInt("cdCampoBI1"));
				stmtAtuSetDep.setInt(++i, resAnt.getInt("cdCampoBI2"));
				stmtAtuSetDep.setInt(++i, resAnt.getInt("cdCampoBI3"));
				stmtAtuSetDep.setInt(++i, resAnt.getInt("cdCampoBI4"));
				stmtAtuSetDep.executeUpdate();
				    
				if ((++contador % 5000) == 0)
					ConexaoDB.gravarTransacao(conAtual);
				
				System.out.println(contador);

			}
		} catch (Exception e) {
			System.out.println("aqui");
			System.out.println(e.getMessage());
		}
		
		stmtAnt.close();
		stmtAtuSetDep.close();
		
		stmtAnt = null;
		stmtAtuSetDep = null;
		
		gravarLogTabela("CADRELATORIOBI");
		
	}
	
	
	private int procurarNovaConta(String codConta) throws SQLException {
		int idConta = 0;
		
		String sql = "SELECT ID_Conta FROM CtbConta WHERE cdConta = ?";
		PreparedStatement stmtContaAtu = null;
		stmtContaAtu = this.conAtual.getConexao().prepareStatement(sql);
		ResultSet resAtu = null;
		
		int i = 0;
		stmtContaAtu.setString(++i, codConta);
		resAtu = stmtContaAtu.executeQuery();
		if (resAtu.next())
			idConta = resAtu.getInt("ID_Conta");
		
		stmtContaAtu.close();
		resAtu.close();
		
		stmtContaAtu = null;
		resAtu = null;
		
		return idConta;
	}

	private void migrarEmpCencusDepto() throws Exception {
		if (migrouTabela("EMPCENCUSDEPTO"))
			return;

		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		PreparedStatement stmtAtuSetDep = null;
		
		String sql = "SELECT cdep.*, cc.cdFuncLider FROM EmpCencusDepto cdep "
				+ "JOIN EmpCentroCusto cc ON cdep.cdCentroCusto = cc.cdCentroCusto";
		ResultSet resAnt = stmtAnt.executeQuery(sql);

		sql = "INSERT INTO EmpCencusDepto VALUES (?, ?, ?)";
		stmtAtuSetDep = this.conAtual.getConexao().prepareStatement(sql);

		Funcionario gerente = null;
		try {
			while (resAnt.next()) {
				if (resAnt.getInt("cdfunclider") == 0)
					continue;
				
				gerente = FuncionarioVersao1DAO.pesquisar(this.conAnterior.getConexao(), resAnt.getInt("cdfunclider"));
				
				if (gerente == null)
					System.out.println("erro: " + resAnt.getInt("cdfunclider"));
				
				Recurso recurso = new Recurso(new MesAnoOrcamento());
				
				recurso.setCdRecurso(0);
				recurso.setNmRecurso("Recurso " + gerente.getCargo().getTpCargo().getDescricao());
				recurso.setVinculo(gerente);
				recurso.setCargo(recurso.getVinculo().getCargo());
				recurso.setSetor(gerente.getCenCusto());
				recurso.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
				recurso.setInicioVinculo(0);
				recurso.setFimVinculo(0);
				recurso.setTipo(TipoRecurso.G);
				
				EquipeServiceDAO.incluir(this.conAtual.getConexao(), recurso);
				
				int i = 0;			
				stmtAtuSetDep.setLong(++i, resAnt.getLong("cdDepartamento"));
				stmtAtuSetDep.setInt(++i, resAnt.getInt("cdCentroCusto"));
				stmtAtuSetDep.setLong(++i, recurso.getCdRecurso());
				stmtAtuSetDep.executeUpdate();
				    
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("aqui");
			System.out.println(e.getMessage());
			System.out.println(gerente.getCdFuncionario());
		}
		
		stmtAnt.close();
		stmtAtuSetDep.close();
		
		stmtAnt = null;
		stmtAtuSetDep = null;
		
		gravarLogTabela("EMPCENCUSDEPTO");
	}	
	
	
	private void migrarGestorDepartamento() {
		// TODO
		
		/*Funcionario gerente = FuncionarioVersao1DAO.pesquisar(this.conAnterior.getConexao(), resAnt.getInt("depCdGerente"));
		
		Recurso recurso = new Recurso(new MesAnoOrcamento());
		
		recurso.setCdRecurso(0);
		recurso.setNmRecurso("Recurso " + gerente.getCargo().getTpCargo().getDescricao());
		recurso.setVinculo(gerente);
		recurso.setCargo(recurso.getVinculo().getCargo());
		recurso.setSetor(gerente.getCenCusto());
		recurso.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
		recurso.setInicioVinculo(0);
		recurso.setFimVinculo(0);
		recurso.setTipo(TipoRecurso.G);
		
		EquipeServiceDAO.incluir(this.conAtual.getConexao(), recurso);
		*/
	}
	
	private void migrarTabelaIgual(String tabelaOrigem, String tabelaDestino) throws SQLException {
		if (migrouTabela(tabelaOrigem))
			return;
		
		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		Statement stmtAtu = this.conAtual.getConexao().createStatement();
		
		String sql = "SELECT * FROM " + tabelaOrigem;
		ResultSet resAnt = stmtAnt.executeQuery(sql);
		
		while (resAnt.next()) {
			String sql2 = "INSERT INTO " + tabelaDestino + " VALUES (" + valorColuna(resAnt) + ")";
			stmtAtu.executeUpdate(sql2);
		}
		
		resAnt.close();
		stmtAnt.close();
		stmtAtu.close();
		
		resAnt = null;
		stmtAnt = null;
		stmtAtu = null;
		
		gravarLogTabela(tabelaOrigem);
	}


	private static String valorColuna(ResultSet res) throws SQLException {
		ResultSetMetaData rsmd = res.getMetaData();
		
		int columnCount = res.getMetaData().getColumnCount();

		String valorColunas = "";
		for (int i = 1; i <= columnCount; i++ ) {
			System.out.print(rsmd.getColumnTypeName(i) + " - ");
			System.out.print(rsmd.getColumnDisplaySize(i) + " - ");
			System.out.print(rsmd.getPrecision(i) + " - ");
			System.out.print(rsmd.getScale(i) + " - ");
			System.out.print(rsmd.getCatalogName(i) + " - ");
			System.out.print(rsmd.getSchemaName(i) + " - ");
			System.out.print(rsmd.getTableName(i) + " - ");
			
			System.out.println("");
			
			
			if (rsmd.getColumnTypeName(i).equals("VARCHAR") || rsmd.getColumnTypeName(i).equals("CHAR"))			
				valorColunas += "'" + res.getString(i) + ((i < columnCount) ? "', " : "'");
			else
				valorColunas += res.getString(i) + ((i < columnCount) ? ", " : "");
		}
		return valorColunas;
	}	
	
	private boolean migrouTabela(String tabelaOrigem) throws SQLException {
		boolean retorno = false;
		
		Statement stmtAtu = this.conAtual.getConexao().createStatement();
		
		String sql = String.format("SELECT * FROM TabelaMigrada WHERE tabela = '%s' ", tabelaOrigem);
		ResultSet rs = stmtAtu.executeQuery(sql);
		
		if (rs.next())
			retorno = rs.getString("migrou").equals("S");
		
		rs.close();
		rs = null;
		
		stmtAtu.close();
		stmtAtu = null;
		
		return retorno;
	}

	private void gravarLogTabela(String tabelaOrigem) throws SQLException {
		Statement stmtAtu = this.conAtual.getConexao().createStatement();
		
		String sql = String.format("SELECT * FROM TabelaMigrada WHERE tabela = '%s' ", tabelaOrigem);
		ResultSet rs = stmtAtu.executeQuery(sql);
		
		if (rs.next()) {
			sql = String.format("UPDATE TabelaMigrada SET migrou = 'S' WHERE tabela = '%s' ", tabelaOrigem);
		} else {
			sql = String.format("INSERT INTO TabelaMigrada VALUES ('%s', 'S')", tabelaOrigem);
		}
		stmtAtu.executeUpdate(sql);
		
		rs.close();
		rs = null;
		
		stmtAtu.close();
		stmtAtu = null;
	}
}


/*
 
	private void migrarCtbEstornaConta() throws SQLException {
		if (migrouTabela("CTBESTORNACONTA"))
			return;

		Statement stmtAnt = this.conAnterior.getConexao().createStatement();
		PreparedStatement stmtAtuSetDep = null;
		
		String sql = "SELECT tabela banco antigo";
		ResultSet resAnt = stmtAnt.executeQuery(sql);

		sql = "INSERT INTO tabelabanconovo VALUES (?, ?, ?)";
		stmtAtuSetDep = this.conAtual.getConexao().prepareStatement(sql);

		try {
			while (resAnt.next()) {
				int i = 0;			
				stmtAtuSetDep.setLong(++i, resAnt.getLong("cdDepartamento"));
				stmtAtuSetDep.setInt(++i, resAnt.getInt("cdCentroCusto"));
				stmtAtuSetDep.executeUpdate();
				    
			}
		} catch (Exception e) {
			System.out.println("aqui");
			System.out.println(e.getMessage());
		}
		
		stmtAnt.close();
		stmtAtuSetDep.close();
		
		stmtAnt = null;
		stmtAtuSetDep = null;
		
		gravarLogTabela("CTBESTORNACONTA");
		
	}
 
 
*/