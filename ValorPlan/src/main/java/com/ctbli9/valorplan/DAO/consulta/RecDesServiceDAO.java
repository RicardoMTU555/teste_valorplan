package com.ctbli9.valorplan.DAO.consulta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.ctbli9.valorplan.DAO.CentroCustoServiceDAO;
import com.ctbli9.valorplan.DAO.EquipeServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.modelo.Plano;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;
import ctbli9.recursos.xls.GeraXLS;

public class RecDesServiceDAO {

	public static void gerarPlanilhaRecDes(Connection con, String nomePlanilha) throws Exception {
		Plano plano = PlanoServiceDAO.getPlanoSelecionado();
		
		CentroCustoServiceDAO.montarCentrosCustoGerente(con, Global.getFuncionarioLogado(), null);
				
		GeraXLS excel = new GeraXLS(PlanoServiceDAO.getPlanoSelecionado().getNmPlano(), nomePlanilha);
		
		String sql = String.format(
				"SELECT r.nrAnoMes, eq.nomeVinculo AS nmFuncionario, eq.cecCdExterno, " +
				"lpad(r.cdReceita, 10, '0') || '0000000000' || lpad(r.ID_Recurso, 10, '0') AS Chave, " +
				"       cr.sgReceita AS Conta, cr.dsReceita AS DescrConta, ct.cdConta AS ContaContabil, " + 
				"       cat.cdTipoReceita, r.ID_Recurso, eq.cecDsResumida, " + 
				"       CASE WHEN cat.cdTipoReceita = 'MAQ' THEN r.qtReceita ELSE 1 END AS Quantidade, " + 
				"       (r.qtReceita * r.vrUnitario) AS Valor  " + 
				"FROM OrcReceita r " + 
				"JOIN CadReceita cr ON r.cdReceita = cr.cdReceita " +
				"JOIN CtbConta ct ON cr.ID_ContaReceita = ct.ID_Conta " +
				"JOIN CadCategReceita cat ON cr.cdCategoria = cat.cdCategoria " +
				EquipeServiceDAO.montaScriptRelacaoEquipe("r") +
				"JOIN TmpCencus ON TmpCencus.cdCentroCusto = eq.cdCentroCusto " + 
				"WHERE r.cdPlano = %d  " + 
				"AND r.qtReceita <> 0 AND r.vrUnitario <> 0 " + 
				"AND r.nrAnoMes BETWEEN %04d01 AND %04d12 " + 
				
				"UNION " + 
				
				"SELECT r.nrAnoMes, eq.NomeVinculo AS nmFuncionario, eq.cecCdExterno, " +
				"lpad(r.cdReceita, 10, '0') || lpad(pdr.ID_Conta, 10, '0') || lpad(r.ID_Recurso, 10, '0') AS Chave, " +
				"  ct.sgConta AS Conta, ct.dsConta AS DescrConta, ct.cdConta AS ContaContabil, " + 
				"  ' ' AS cdTipoReceita, r.ID_Recurso, eq.cecDsResumida, " + 
				"	0 AS Quantidade, " + 
				"	CAST(((COALESCE(pdr.pcDespesa,0) / 100.0000)*(r.qtReceita*r.vrUnitario)) AS DECIMAL(13,2)) AS Valor  " + 
				"FROM OrcReceita r  " + 
				EquipeServiceDAO.montaScriptRelacaoEquipe("r") +
				"JOIN TmpCencus ON TmpCencus.cdCentroCusto = eq.cdCentroCusto " +
				"JOIN CadDeducaoVenda dv ON dv.cdReceita = r.cdReceita " + 
				"JOIN OrcParamDeducao pdr " + 
				"     ON pdr.cdPlano = r.cdPlano AND pdr.cdReceita = dv.cdReceita " + 
				"     AND pdr.ID_Conta = dv.ID_Conta AND pdr.ID_Recurso = r.ID_Recurso AND pdr.nrAnoMes = r.nrAnoMes " + 
				"JOIN CtbConta ct ON pdr.ID_Conta = ct.ID_Conta " + 
				"WHERE r.cdPlano = %d " + 
				"AND r.qtReceita <> 0 AND r.vrUnitario <> 0 " + 
				"AND r.nrAnoMes BETWEEN %04d01 AND %04d12 " + 
				"ORDER BY 2, 3, 4, 1;",
				plano.getCdPlano(),
				plano.getNrAno(),
				plano.getNrAno(),
				
				plano.getCdPlano(),
				plano.getNrAno(),
				plano.getNrAno());
		
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		MesAnoOrcamento anoRef = new MesAnoOrcamento();
		
		int numLin = 0;
		int numCol = 0;
		
		excel.setBorda(1);
		excel.criarEstilo();
		
		excel.setEstilo(GeraXLS.ESTILO_TITULO);
		
		excel.escreveCelula(numLin, numCol++, "Colaborador");
		excel.escreveCelula(numLin, numCol++, "Centro de Custo");
		excel.escreveCelula(numLin, numCol++, "Descrição");
		excel.escreveCelula(numLin, numCol++, "Código");
		excel.escreveCelula(numLin, numCol++, "Conta");
		excel.escreveCelula(numLin, numCol++, "Descrição");
		
		for (int i = 1; i <= 12; i++) {
			excel.escreveCelula(numLin, numCol++, String.format("Mês %02d", i), 1);
			numCol++;			
		}
		
		numLin++;
		numCol = 6;
		for (int i = 1; i <= 12; i++) {
			excel.escreveCelula(numLin, numCol++, "Quantidade");
			excel.escreveCelula(numLin, numCol++, "Valor");
		}
		
		//String linha = " | | | | |Quantidade|Valor|Quantidade|Valor|Quantidade|Valor|Quantidade|Valor|Quantidade|Valor|Quantidade|Valor|Quantidade|Valor|Quantidade|Valor|Quantidade|Valor|Quantidade|Valor|Quantidade|Valor|Quantidade|Valor";
		//excel.linhaListradaTitulo(linha);
		
		numLin++;
		numCol = 0;
		
		excel.setFonte("Arial", 10, "preto", false, false);
		excel.setCorFundo("branco");
		excel.setBorda(1);
		excel.criarEstilo();
		
		boolean temRegistro = res.next();
		while (temRegistro) {
			// 1. Pego a chave
			String chaveMonitor = res.getString("Chave");
			String tipoReceita = res.getString("cdTipoReceita");
			
			if (!res.getString("cdTipoReceita").trim().isEmpty()) {
				excel.setEstilo(GeraXLS.ESTILO_SUB_TITULO);
			} else {
				excel.setEstilo(GeraXLS.ESTILO_NORMAL);
			}

			excel.escreveCelula(numLin, numCol++, res.getString("nmFuncionario"));
			excel.escreveCelula(numLin, numCol++, res.getString("cecCdExterno"));
			excel.escreveCelula(numLin, numCol++, res.getString("cecDsResumida"));
			
			excel.escreveCelula(numLin, numCol++, res.getString("Conta"));
			excel.escreveCelula(numLin, numCol++, res.getString("ContaContabil"));
			excel.escreveCelula(numLin, numCol++, res.getString("DescrConta"));
			
			// 2. Inicializa mes
			int mes = 1;
			do {
				anoRef.setMes(mes);
				// 3. Verifica se esta em registro valido
				if (temRegistro && res.getInt("nrAnoMes") == anoRef.getAnoMes() &&
						chaveMonitor.equals(res.getString("Chave"))) {
					
					if (!tipoReceita.isEmpty())
						excel.escreveCelula(numLin, numCol++, res.getDouble("Quantidade"));
					else
						excel.escreveCelula(numLin, numCol++, "");
					
					excel.escreveCelula(numLin, numCol++, res.getDouble("Valor"));
					
					temRegistro = res.next();
				} else {
					if (!tipoReceita.trim().isEmpty())
						excel.escreveCelula(numLin, numCol++, 0.00);
					else
						excel.escreveCelula(numLin, numCol++, "");
					
					excel.escreveCelula(numLin, numCol++, 0.00);
				}
				++mes;
			} while ((temRegistro && chaveMonitor.equals(res.getString("Chave"))) || mes <= 12);
			numLin++;
			numCol = 0;
		}
		
		excel.salvaXLS();
		
		res.close();
		stmt.close();
		
		res = null;
		stmt = null;
	}

}
