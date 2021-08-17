package com.ctbli9.valorplan.DAO.consulta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.ctbli9.valorplan.DAO.CentroCustoServiceDAO;
import com.ctbli9.valorplan.DAO.DepartamentoServiceDAO;
import com.ctbli9.valorplan.DAO.EquipeServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.modelo.NivelArvore;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.modelo.Plano;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;
import ctbli9.recursos.xls.GeraXLS;

public class QuantRecServiceDAO {

	public static void gerarPlanilhaQuantidade(Connection con, String nomePlanilha, int numNiveis, CategoriaReceita categoria) 
			throws Exception {
		Plano plano = PlanoServiceDAO.getPlanoSelecionado();
		
		CentroCustoServiceDAO.montarCentrosCustoGerente(con, Global.getFuncionarioLogado(), null);
		
		GeraXLS excel = new GeraXLS(PlanoServiceDAO.getPlanoSelecionado().getNmPlano(), nomePlanilha);
		
		String sql = "SELECT rec.nranomes, ";
		for (int i = 1; i <= numNiveis; i++) { // Colunas dos niveis de departamento
			sql += String.format("arvore.niv%02d, arvore.dep%02d, ", i, i);
		}
		
		NivelArvore nivel = DepartamentoServiceDAO.getNivelArvore(con);
		sql += String.format("eq.cecCdExterno, eq.cecDsResumida, " + 
				"eq.nomeVinculo AS nmFuncionario, " +
				"cr.sgReceita, cr.dsReceita, catr.dsCategoria, " +
				"rec.cdReceita || rec.ID_Recurso AS Chave, " +
				"SUM(rec.qtReceita) AS Quant " + 
				"FROM OrcReceita rec " + 
				EquipeServiceDAO.montaScriptRelacaoEquipe("rec") +
				"JOIN CadReceita cr ON rec.cdReceita = cr.cdReceita AND cr.idAtivo = 'S' " + 
				"JOIN CadCategReceita catr ON cr.cdCategoria = catr.cdCategoria AND catr.tpOrcamento = 'Q' " +
				"JOIN EmpCencusDepto ccd ON eq.cdCentroCusto = ccd.cdCentroCusto " + 
				"JOIN TmpCencus ON TmpCencus.cdCentroCusto = eq.cdCentroCusto " + 
				"JOIN (" + DepartamentoServiceDAO.montaScriptArvore(nivel.getMaximo()) + ") arvore ON ccd.cdDepartamento = arvore.niv%02d " + 
				"WHERE rec.cdPlano = %d " + 
				"AND rec.nrAnoMes BETWEEN %04d01 AND %04d12 " + 
				"AND rec.qtReceita <> 0 AND rec.vrUnitario <> 0 " +
				(categoria.getCdCategoria() == 0 ? "" : "AND catr.cdCategoria = " + categoria.getCdCategoria()) + 
				"GROUP BY ",
				nivel.getMaximo(),
				plano.getCdPlano(),
				plano.getNrAno(),
				plano.getNrAno());
		
		for (int i = 1; i <= numNiveis; i++) {
			sql += String.format("arvore.niv%02d, arvore.dep%02d, ", i, i);
		}
		sql += " eq.cecCdExterno, eq.cecDsResumida, eq.nomeVinculo, "
				+ "cr.sgReceita, cr.dsReceita, catr.dsCategoria, "
				+ "rec.cdReceita || rec.ID_Recurso, rec.nranomes";
		
		System.out.println(sql);
		Statement stmt = con.createStatement();
		ResultSet res = stmt.executeQuery(sql);
		
		MesAnoOrcamento anoRef = new MesAnoOrcamento();
		
		int numLin = 0;
		int numCol = 0;
		
		excel.setBorda(1);
		excel.setFonte("Arial", 10, "preto", false, false);
		excel.setCorFundo("branco");
		excel.criarEstilo();
		
		excel.setEstilo(GeraXLS.ESTILO_TITULO);
		for (int i = 1; i <= numNiveis; i++) {
			excel.escreveCelula(numLin, numCol++, String.format("DEPTO %02d", i));
		}
		
		excel.escreveCelula(numLin, numCol++, "Centro de Custo");
		excel.escreveCelula(numLin, numCol++, "Descrição");
		excel.escreveCelula(numLin, numCol++, "CEN");
		excel.escreveCelula(numLin, numCol++, "Receita");
		excel.escreveCelula(numLin, numCol++, "Descrição");
		excel.escreveCelula(numLin, numCol++, "Categoria");
		
		for (int i = 1; i <= 12; i++) {
			excel.escreveCelula(numLin, numCol++, String.format("Mês %02d", i));
		}
		
		excel.setEstilo(GeraXLS.ESTILO_NORMAL);
		numLin++;
		numCol = 0;
		
		boolean temRegistro = res.next();
		while (temRegistro) {
			// 1. Pego a chave
			long chaveMonitor = res.getLong("Chave");
			
			for (int i = 1; i <= numNiveis; i++) {
				excel.escreveCelula(numLin, numCol++, res.getString(String.format("dep%02d", i)));
			}
			
			excel.escreveCelula(numLin, numCol++, res.getString("cecCdExterno"));
			excel.escreveCelula(numLin, numCol++, res.getString("cecDsResumida"));
			excel.escreveCelula(numLin, numCol++, res.getString("nmFuncionario"));
			
			excel.escreveCelula(numLin, numCol++, res.getString("sgReceita"));
			excel.escreveCelula(numLin, numCol++, res.getString("dsReceita"));
			excel.escreveCelula(numLin, numCol++, res.getString("dsCategoria"));
			
			// 2. Inicializa mes
			int mes = 1;
			do {
				anoRef.setMes(mes);
				// 3. Verifica se esta em registro valido
				if (temRegistro && res.getInt("nrAnoMes") == anoRef.getAnoMes() &&
						chaveMonitor == res.getLong("Chave")) {
					excel.escreveCelula(numLin, numCol++, res.getInt("Quant"));
					temRegistro = res.next();
				} else {
					excel.escreveCelula(numLin, numCol++, 0);
				}
				++mes;
			} while ((temRegistro && chaveMonitor == res.getLong("Chave")) || mes <= 12);
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
