package com.ctbli9.valorplan.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import com.ctbli9.valorplan.DAO.orc.OrcReceitaServiceDAO;
import com.ctbli9.valorplan.enumeradores.TipoSaldo;
import com.ctbli9.valorplan.modelo.ArvoreOrganizacional;
import com.ctbli9.valorplan.modelo.NivelArvore;
import com.ctbli9.valorplan.modelo.ctb.ClasseDREAgrupada;
import com.ctbli9.valorplan.modelo.ctb.EstornoConta;
import com.ctbli9.valorplan.recursos.Global;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.enumeradores.TipoConta;
import ctbli9.modelo.Plano;
import ctbli9.recursos.DataUtil;
import ctbli9.recursos.LibUtil;
import ctbli9.recursos.xls.GeraXLS;

public class BalanceteServiceDAO {
	private Connection con;
	private String campoValor = null;
	private String campoValorExt = null;
	private String campoCencus = null;
	private String relacionaEstorno = null;
	
	public BalanceteServiceDAO(Connection con) {
		this.con = con;
	}
	
	public void close() {
		
	}

	/*
	 * REC, DED, DRH, ERH, DGE, RLZ
	 * 
	 */
	public void carregarBalancete(boolean temporario, int[] setoresSelecionados, int nroAnoInicialRealizado, boolean realizado)
			throws Exception {
		Statement stmt = con.createStatement();		
		
		long tempoInicio = System.currentTimeMillis();
		
		
		Plano plano = PlanoServiceDAO.getPlanoSelecionado();
		String sql = null;
		campoValor = null;
		campoValorExt = null;
		campoCencus = null;
		relacionaEstorno = null;
		
		if (setoresSelecionados.length == 0)
			CentroCustoServiceDAO.montarCentrosCustoGerente(con, Global.getFuncionarioLogado(), null);
		else {
			sql = "DELETE FROM TmpCencus";
			stmt.executeUpdate(sql);

			sql = "INSERT INTO TmpCencus VALUES (?)";
			PreparedStatement pstmt = con.prepareStatement(sql);
			for (int i = 0; i < setoresSelecionados.length; i++) {
				pstmt.setLong(1, setoresSelecionados[i]);
				pstmt.executeUpdate();
			}
			pstmt.close();
			pstmt = null;
		}
		
		List<EstornoConta> listaContas = ContaContabilServiceDAO.listarEstornoConta(con);
		MesAnoOrcamento mesRef = new MesAnoOrcamento();
		
		String tabela = null;
		if (temporario) {
			sql = "DELETE FROM TmpSaldos";
			tabela = "TmpSaldos";
		}
		else {
			sql = String.format("UPDATE CtbSaldos SET vlOrcado = 0, vlRealizado = 0 "
					+ "WHERE nrAnoMes BETWEEN %d01 AND %d12 ", 
					mesRef.getAno(), 
					mesRef.getAno());
			tabela = "CtbSaldos";
		}
		stmt.executeUpdate(sql);
		
		// =====> ORÇADO
		// RECEITA
		campoCencus = "eq.cdCentroCusto";
		campoValor = "SUM((o.qtReceita * o.vrUnitario)) as SaldoOrc";
		campoValorExt = "SUM(((o.qtReceita * o.vrUnitario) * -1)) as SaldoOrc";
		relacionaEstorno = "JOIN CtbEstornaConta est ON est.ID_Conta = r.ID_ContaReceita";
		
		sql = String.format("SELECT cc.cdFilial, o.nrAnoMes, r.ID_ContaReceita as ID_Conta, campoCencus, "
				+ "campoValor, 0 as SaldoReal, "
				+ "'%s' "
				+ "FROM OrcReceita o "
				+ "JOIN OrcEquipe eq ON o.ID_Recurso = eq.ID_Recurso "
				+ "JOIN EmpCentroCusto cc ON eq.cdCentroCusto = cc.cdCentroCusto "
				+ "JOIN CadReceita r ON o.cdReceita = r.cdReceita "
				+ "relacionaEstorno "
				+ (temporario ? "JOIN TmpCencus ON TmpCencus.cdCentroCusto = eq.cdCentroCusto " : "")
				+ "WHERE o.cdPlano = %d AND o.nrAnoMes BETWEEN %d01 AND %d12 "
				+ "AND (o.qtReceita * o.vrUnitario) <> 0 "
				+ "GROUP BY 1, 2, 3, 4",
				TipoSaldo.REC,
				plano.getCdPlano(),
				plano.getNrAno(), plano.getNrAno());
		importarOrcado(listaContas, tabela, sql, "ORÇADO RECEITA");
		
		// DEDUÇÕES E CUSTOS DAS VENDAS
		campoCencus = "eq.cdCentroCusto";
		campoValor = "SUM(vo.valorDeducao * -1) as SaldoOrc";
		campoValorExt = "SUM(vo.valorDeducao) as SaldoOrc";
		relacionaEstorno = "JOIN CtbEstornaConta est ON est.ID_Conta = vo.ID_Conta";
		
		sql = "SELECT cc.cdFilial, vo.nrAnoMes, vo.ID_Conta AS ID_Conta, campoCencus, "
				+ "campoValor, 0 as SaldoReal, "
				+ "'" + TipoSaldo.DED + "' "
				+ "FROM (" + 
				OrcReceitaServiceDAO.montaDeducoesReceita(
						String.format("AND orcRec.nrAnoMes BETWEEN %d01 AND %d12 "
								+ " AND orcRec.qtReceita <> 0 AND orcRec.vrUnitario <> 0 ",
								plano.getNrAno(), 
								plano.getNrAno()), temporario) + 
				") vo "
				+ "JOIN OrcEquipe eq ON vo.ID_Recurso = eq.ID_Recurso "
				+ "JOIN EmpCentroCusto cc ON eq.cdCentroCusto = cc.cdCentroCusto "
				+ (temporario ? "JOIN TmpCencus ON TmpCencus.cdCentroCusto = eq.cdCentroCusto " : "")
				+ "relacionaEstorno "
				+ "WHERE vo.ID_Conta IS NOT NULL "
				+ "GROUP BY 1, 2, 3, 4";
		
		importarOrcado(listaContas, tabela, sql, "ORÇADO DEDUÇÕES");
		
		// DESPESAS DE PESSOAL
		campoCencus = "eq.cdCentroCusto";
		campoValor = "SUM(odrh.vrDespesa * -1) as SaldoOrc";
		campoValorExt = "SUM(odrh.vrDespesa) as SaldoOrc";
		relacionaEstorno = "JOIN CtbEstornaConta est ON est.ID_Conta = odrh.ID_Conta";
		
		sql = String.format("SELECT cc.cdFilial, odrh.nrAnoMes, odrh.ID_Conta AS ID_Conta, campoCencus, "
				+ "campoValor, 0 as SaldoReal, "
				+ "'" + TipoSaldo.DRH + "' "
				+ "FROM OrcDespesaRH odrh "
				+ "JOIN OrcEquipe eq ON odrh.ID_Recurso = eq.ID_Recurso "
				+ "JOIN EmpCentroCusto cc ON eq.cdCentroCusto = cc.cdCentroCusto "
				+ (temporario ? "JOIN TmpCencus ON TmpCencus.cdCentroCusto = eq.cdCentroCusto " : "")
				+ "relacionaEstorno "
				+ "WHERE odrh.cdPlano = %d AND odrh.nrAnoMes BETWEEN %d01 AND %d12 "
				+ "AND NOT EXISTS (SELECT DISTINCT 1 FROM OrcParamFolha opf WHERE opf.cdPlano = %d AND opf.ID_ContaEnc = odrh.ID_Conta AND opf.pcDespesa <> 0) "
				+ "AND odrh.vrDespesa <> 0 "
				+ "GROUP BY 1, 2, 3, 4", 
				plano.getCdPlano(),
				plano.getNrAno(), plano.getNrAno(),
				plano.getCdPlano());	
		importarOrcado(listaContas, tabela, sql, "ORÇADO DESPESAS DE PESSOAL");
		
		// ENCARGOS DE FOLHA - ENCARGOS
		campoCencus = "eq.cdCentroCusto";
		campoValor = "SUM(((odrh.vrDespesa * p.pcDespesa) / 100) * -1) as SaldoOrc";
		campoValorExt = "SUM(((odrh.vrDespesa * p.pcDespesa) / 100)) as SaldoOrc";
		relacionaEstorno = "JOIN CtbEstornaConta est ON est.ID_Conta = p.ID_ContaEnc";
		
		sql = String.format("SELECT cc.cdFilial, odrh.nrAnoMes, p.ID_ContaEnc AS ID_Conta, campoCencus, "
				+ "campoValor, 0 as SaldoReal, "
				+ "'%s' "
				+ "FROM OrcDespesaRH odrh "
				+ "JOIN CtbConta ct ON odrh.ID_Conta = ct.ID_Conta AND ct.tpConta = 'SL' "
				+ "JOIN OrcEquipe eq ON odrh.ID_Recurso = eq.ID_Recurso "
				+ "JOIN EmpCentroCusto cc ON eq.cdCentroCusto = cc.cdCentroCusto "
				+ "JOIN OrcParamFolha p ON odrh.cdPlano = p.cdPlano AND p.ID_ContaSal = odrh.ID_Conta AND p.nrAnoMes = odrh.nrAnoMes "
				+  (temporario ? "JOIN TmpCencus ON TmpCencus.cdCentroCusto = eq.cdCentroCusto " : "")
				+ "relacionaEstorno "
				+ "WHERE odrh.cdPlano = %d AND odrh.nrAnoMes BETWEEN %d01 AND %d12 "
				+ "AND ((odrh.vrDespesa * p.pcDespesa) / 100) <> 0 "
				+ "GROUP BY 1, 2, 3, 4",
				TipoSaldo.DRH,
				plano.getCdPlano(),
				plano.getNrAno(), plano.getNrAno());	
		importarOrcado(listaContas, tabela, sql, "ENCARGOS DE FOLHA");
		
		// DESPESAS GERAIS
		campoCencus = "od.cdCentroCusto";
		campoValor = "SUM(CASE WHEN ct.ctaIdNatureza = 'C' THEN od.vrDespesa ELSE od.vrDespesa * -1 END) AS SaldoOrc";
		campoValorExt = "SUM(CASE WHEN ct.ctaIdNatureza = 'C' THEN od.vrDespesa * -1 ELSE od.vrDespesa END) AS SaldoOrc";
		relacionaEstorno = "JOIN CtbEstornaConta est ON est.ID_Conta = od.ID_Conta";
		
		sql = String.format("SELECT cc.cdFilial, od.nrAnoMes, od.ID_Conta, campoCencus, "
				+ "campoValor, 0 AS SaldoReal, "
				+ "'" + TipoSaldo.DGE + "' "
				+ "FROM OrcDespesaGeral od "
				+ "JOIN EmpCentroCusto cc ON od.cdCentroCusto = cc.cdCentroCusto "
				+ "JOIN CtbConta ct ON od.ID_Conta = ct.ID_Conta "
				+ (temporario ? "JOIN TmpCencus ON TmpCencus.cdCentroCusto = od.cdCentroCusto " : "")
				+ "relacionaEstorno "
				+ "WHERE od.cdPlano = %d "
				+ "AND od.nrAnoMes BETWEEN %d01 AND %d12 "
				+ "AND od.vrDespesa <> 0 "
				+ "GROUP BY 1, 2, 3, 4", 
				PlanoServiceDAO.getPlanoSelecionado().getCdPlano(),
				plano.getNrAno(), 
				plano.getNrAno());
		
		importarOrcado(listaContas, tabela, sql, "DESPESAS GERAIS");
		
		if (realizado) {
			// ======> REALIZADO
			importarRealizado(plano.getNrAno());
		}
		long tempoFinal = System.currentTimeMillis();
		double resultado = (tempoFinal - tempoInicio) / 1000.00;
		LibUtil.display("CARRAGAMENTO FINALIZADO EM "  + " " + resultado + " SEGUNDOS.");
		
		stmt.close();
		stmt = null;
	}
	
	private void importarOrcado(List<EstornoConta> contasExtorno, String tabela, String sql, String descrOper) 
			throws Exception {
		long tempoInicio = System.currentTimeMillis();
		long tempoFinal;
		double resultado;
		
		Statement stmt = con.createStatement();
		
		String sqlExec = sql.replaceAll("campoCencus", campoCencus);
		sqlExec = sqlExec.replaceAll("campoValor", campoValor);
		sqlExec = sqlExec.replaceAll("relacionaEstorno", "");
		
		System.out.println("REGINALDO: " + descrOper + " => " + sqlExec);
		
		sqlExec = "INSERT INTO TmpSaldos " + sqlExec;
		stmt.executeUpdate(sqlExec);
		
		if (!relacionaEstorno.isEmpty()) {
			sqlExec = sql.replaceAll("campoCencus", "est.cdCentroCusto");
			sqlExec = sqlExec.replaceAll("campoValor", campoValorExt);
			sqlExec = sqlExec.replaceAll("relacionaEstorno", relacionaEstorno);
			
			sqlExec = "INSERT INTO TmpSaldos " + sqlExec;
			stmt.executeUpdate(sqlExec);
		}
		
		stmt.close();
		stmt = null;
		
		tempoFinal = System.currentTimeMillis();
		resultado = (tempoFinal - tempoInicio) / 1000.00;
		LibUtil.display("TEMPO PROCESSAMENTO " + descrOper + " " + resultado);
	}
	
	public void importarRealizado(int ano) throws Exception {

		TipoConta[] tipos = TipoConta.values();
		
		Date dataI = DataUtil.stringToData(String.format("%04d-01-01",ano), "yyyy-MM-dd");
		Date dataF = DataUtil.stringToData(String.format("%04d-12-31", ano), "yyyy-MM-dd");
		
		String sql = String.format("INSERT INTO TmpSaldos "
				+ "SELECT cdFilial, EXTRACT(YEAR FROM dtLancto) || LPAD(EXTRACT(MONTH FROM dtLancto), 2, '0') as nrAnoMes, "
				+ "CtbLancto.ID_Conta, cdCentroCusto, 0 as SaldoOrc, "
				+ "SUM( CASE WHEN idNatMovto = 'C' THEN vlMovto ELSE vlMovto * -1 END ) AS SaldoReal, "
				+ "? "  //tipoSaldo
				+ "FROM CtbLancto "
				+ "JOIN CtbConta conta ON CtbLancto.ID_Conta = conta.ID_Conta AND conta.tpConta = ? " //tipoConta
				+ "WHERE dtLancto BETWEEN '%s' AND '%s' "
				+ "AND cdCentroCusto IS NOT NULL "
				+ "GROUP BY 1, 2, 3, 4", 
				DataUtil.dataString(dataI, "MM/dd/yyyy"),
				DataUtil.dataString(dataF, "MM/dd/yyyy")
				);
		
		PreparedStatement pstmt = con.prepareStatement(sql);

		System.out.println("REGINALDO: MOVIMENTO REALIZADO " + sql);
		
		for (TipoConta tipoConta : tipos) {
			long tempoInicio = System.currentTimeMillis();
			long tempoFinal;
			double resultado;

			System.out.println("REGINALDO: MOVIMENTO REALIZADO " +
					tipoConta + " => " + obterTipoSaldo(tipoConta));
			
			int i = 0;
			pstmt.setString(++i, obterTipoSaldo(tipoConta));
			pstmt.setString(++i, tipoConta.toString());
			pstmt.executeUpdate();
			
			tempoFinal = System.currentTimeMillis();
			resultado = (tempoFinal - tempoInicio) / 1000.00;
			LibUtil.display("TEMPO PROCESSAMENTO MOVIMENTO REALIZADO " + resultado);
		
		}
		
		pstmt.close();
		pstmt = null;
	}
		
	
	private String obterTipoSaldo(TipoConta tipoConta) {
		if (tipoConta.equals(TipoConta.RE))
			return TipoSaldo.REC.toString();
		else if (tipoConta.equals(TipoConta.DV) || tipoConta.equals(TipoConta.CT))
			return TipoSaldo.DED.toString();
		else if (tipoConta.equals(TipoConta.SL) || tipoConta.equals(TipoConta.DP))
			return TipoSaldo.DRH.toString();
		else if (tipoConta.equals(TipoConta.DO))
			return TipoSaldo.DGE.toString();
		
		return "";
	}

	public void gerarPlanilhaSaldos(String nomePlanilha, ResultSet res) throws Exception {
		MesAnoOrcamento anoRef = new MesAnoOrcamento();
		
		Statement stmt = con.createStatement();
		
		GeraXLS excel = new GeraXLS(PlanoServiceDAO.getPlanoSelecionado().getNmPlano(), nomePlanilha);
		
		NivelArvore nivel = DepartamentoServiceDAO.getNivelArvore(con);
		int maxNivelDRE = ClassificacaoCtbServiceDAO.obtemNivelMaximoDRE(con);
		List<ClasseDREAgrupada> classes = ClassificacaoCtbServiceDAO.listarClasseAgrupada(con, PlanoServiceDAO.getPlanoSelecionado().getNrAno());
		List<ArvoreOrganizacional> deptos = DepartamentoArvoreServiceDAO.listarCentroCustoAgrupado(con);
		
		int numLin = 0;
		int numCol = 0;		
		
		excel.setBorda(1);
		excel.criarEstilo();
		
		excel.setEstilo(GeraXLS.ESTILO_TITULO);
		

		for (int i = 0; i < nivel.getMaximo(); i++) {
			excel.escreveCelula(numLin, numCol++, String.format("Depto %02d|", (i+1)));
			
		}
		excel.escreveCelula(numLin, numCol++, "Centro de Custo");
		excel.escreveCelula(numLin, numCol++, "Descricao");
		
		for (int i = 0; i < maxNivelDRE; i++) {
			excel.escreveCelula(numLin, numCol++, String.format("DRE %02d|", (i+1)));
		}
		excel.escreveCelula(numLin, numCol++, "Conta Contábil");
		excel.escreveCelula(numLin, numCol++, "Descricao");
		
		int auxColuna = numCol;
		for (int i = 1; i <= 12; i++) {
			excel.escreveCelula(numLin, numCol++, String.format("Mês %02d", i), 1);
			numCol++;			
		}
		
		numLin++;
		numCol = auxColuna;
		for (int i = 1; i <= 12; i++) {
			excel.escreveCelula(numLin, numCol++, "Orçado");
			excel.escreveCelula(numLin, numCol++, "Realizado");
		}
		
		numLin++;
		numCol = 0;
		
		excel.setEstilo(GeraXLS.ESTILO_NORMAL);
		
		boolean temRegistro = res.next();
		while (temRegistro) {
			int codCencus = res.getInt("cdCentroCusto");
			String codNivelClasse = res.getString("cdNivelClasse");
			String ctContabil = res.getString("cdConta");
			
			ArvoreOrganizacional arvoreSel = null;
			for (ArvoreOrganizacional arvoreAux : deptos) {
				if (arvoreAux.getCdCentroCusto() == codCencus) {
					arvoreSel = arvoreAux;
					break;
				}
			}	
		
			if (arvoreSel == null) {
				arvoreSel = new ArvoreOrganizacional();

				String[] arrayVazio = new String[nivel.getMaximo()];
				for (int i = 0; i < nivel.getMaximo(); i++) {
					arrayVazio[i] = "";
				}
				arvoreSel.setDsDepto(arrayVazio);
			}
			
			for (int i = 0; i < nivel.getMaximo(); i++) {
				excel.escreveCelula(numLin, numCol++, arvoreSel.getDsDepto()[i]);
			}
			
			excel.escreveCelula(numLin, numCol++, res.getString("cecCdExterno"));
			excel.escreveCelula(numLin, numCol++, res.getString("cecDsResumida"));
			
			ClasseDREAgrupada classSel = null;
			if (!codNivelClasse.isEmpty()) {
				for (ClasseDREAgrupada classAux : classes) {
					if (classAux.getCdNivelClasse().equals(codNivelClasse)) {
						classSel = classAux;
						break;
					}
				}	
			}
			
			if (classSel == null) {
				classSel = new ClasseDREAgrupada();
				
				String[] arrayVazio = new String[maxNivelDRE];
				for (int i = 0; i < maxNivelDRE; i++) {
					arrayVazio[i] = "";
				}
				classSel.setDsClasse(arrayVazio);
			}
			
			for (int i = 0; i < maxNivelDRE; i++) {
				excel.escreveCelula(numLin, numCol++, classSel.getDsClasse()[i]);
			}
			
			excel.escreveCelula(numLin, numCol++, res.getString("cdConta"));
			excel.escreveCelula(numLin, numCol++, res.getString("dsConta"));
			
			int mes = 1;
			try {
				do {
					anoRef.setMes(mes);
					if (temRegistro && res.getInt("nrAnoMes") == anoRef.getAnoMes() &&
							codCencus == res.getInt("cdCentroCusto") && 
							ctContabil.equals(res.getString("cdConta")) && 
							codNivelClasse.equals(res.getString("cdNivelClasse"))
						) {
						excel.escreveCelula(numLin, numCol++, res.getDouble("vlOrcado"));
						excel.escreveCelula(numLin, numCol++, res.getDouble("vlRealizado"));
						
						temRegistro = res.next();
					}
					else {
						excel.escreveCelula(numLin, numCol++, 0.00);
						excel.escreveCelula(numLin, numCol++, 0.00);
					}
					mes++;
					
				} while ((temRegistro && codCencus == res.getInt("cdCentroCusto") && ctContabil.equals(res.getString("cdConta"))
						&& codNivelClasse.equals(res.getString("cdNivelClasse"))) || mes <= 12);
				
				numLin++;
				numCol = 0;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		excel.salvaXLS();
		
		res.close();
		stmt.close();
		
		res = null;
		stmt = null;
	}
}
