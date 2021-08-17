package com.ctbli9.valorplan.negocio;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;

import com.ctbli9.valorplan.modelo.CentroCusto;
import ctbli9.modelo.ctb.ContaContabil;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.RelatorioBIServiceDAO;
import com.ctbli9.valorplan.modelo.RelatorioBI;

public class RelatorioBIService {

	public boolean importaCamposBI(Connection con, String linha, ContaContabil ctaContabil, CentroCusto cencus, 
			int campoAdic, int campoRv, int campoLb, int campoEbtda) throws SQLException, NamingException {
		
		boolean retorno = false;
		
		String[] campos = linha.split(";");
		
		
		RelatorioBI campo1 = new RelatorioBI();
		campo1.setDsCampoBI(campos[campoAdic]);
		campo1.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
		campo1 = RelatorioBIServiceDAO.pesquisaDescricao(con, campo1);
		if (campo1.getCdCampoBI() == 0) {
			RelatorioBIServiceDAO.incluir(con, campo1);
		}
		
		RelatorioBI campo2 = new RelatorioBI();
		campo2.setDsCampoBI(campos[campoRv]);
		campo2.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
		campo2 = RelatorioBIServiceDAO.pesquisaDescricao(con, campo2);
		if (campo2.getCdCampoBI() == 0) {
			RelatorioBIServiceDAO.incluir(con, campo2);
		}
		
		RelatorioBI campo3 = new RelatorioBI();
		campo3.setDsCampoBI(campos[campoLb]);
		campo3.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
		campo3 = RelatorioBIServiceDAO.pesquisaDescricao(con, campo3);
		if (campo3.getCdCampoBI() == 0) {
			RelatorioBIServiceDAO.incluir(con, campo3);
		}
		
		RelatorioBI campo4 = new RelatorioBI();
		campo4.setDsCampoBI(campos[campoEbtda]);
		campo4.setNrAno(PlanoServiceDAO.getPlanoSelecionado().getNrAno());
		campo4 = RelatorioBIServiceDAO.pesquisaDescricao(con, campo4);
		if (campo4.getCdCampoBI() == 0) {
			RelatorioBIServiceDAO.incluir(con, campo4);
		}
		
		if (!RelatorioBIServiceDAO.existeRelatorioBI(con, ctaContabil, cencus)) {
			RelatorioBIServiceDAO.incluirRelatorioBI(con, ctaContabil, cencus, campo1, campo2, campo3, campo4);
			retorno = true;

		}

		return retorno;
	}

}
