package com.ctbli9.valorplan.negocio.consulta;

import java.util.List;

import com.ctbli9.valorplan.DAO.BalanceteServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.DAO.consulta.ComparativoAnoServiceDAO;
import com.ctbli9.valorplan.modelo.orc.ComparativoAno;

public class ComparativoAnoService {
    private ConexaoDB con;
	
	public ComparativoAnoService(ConexaoDB con) {
		this.con = con;
	}
	
	public List<ComparativoAno> listarContasTotalAno() throws Exception {
		
		int anoAnterior = PlanoServiceDAO.getPlanoSelecionado().getNrAno() - 1;
		
		// 1. Carregar Orçado do ano atual (cenário atual
		BalanceteServiceDAO dao = new BalanceteServiceDAO(con.getConexao());
		dao.carregarBalancete(true, new int[0], 0, true);

	 	// 2. Carregar Realizado do ano anterior
		dao.importarRealizado(anoAnterior);
		dao.close();
		dao = null;
				
		// 3. Select distinct conta+setor from tmpSaldos: tabela basica
		List<ComparativoAno> lista = new ComparativoAnoServiceDAO(this.con).listarContasTotalAno(anoAnterior, anoAnterior + 1);
		
		return lista;
	}
	

}
