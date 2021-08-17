package com.ctbli9.valorplan.negocio.orc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import com.ctbli9.valorplan.DAO.ParamDeducaoServiceDAO;
import com.ctbli9.valorplan.DAO.ParamDespesaRHServiceDAO;
import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.DespesaRH;
import com.ctbli9.valorplan.modelo.DespesaVenda;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.modelo.receita.Receita;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

import ctbli9.modelo.Plano;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.RegraNegocioException;

public class ParamDespesaService {
	private ConexaoDB con;
	
	public ParamDespesaService(ConexaoDB con) {
		this.con = con;
	}

	public void salvarDespesaVenda(Receita receita, Recurso funcionario, List<DespesaVenda> listaParDespesa) 
		throws RegraNegocioException, SQLException, NamingException {
		
		Plano plano = PlanoServiceDAO.getPlanoSelecionado();
		
		PreparedStatement pstmtSel = ParamDeducaoServiceDAO.inicializaSel(con.getConexao()); 
		PreparedStatement pstmtIns = ParamDeducaoServiceDAO.inicializaIns(con.getConexao()); 
		PreparedStatement pstmtUpd = ParamDeducaoServiceDAO.inicializaUpd(con.getConexao()); 
		PreparedStatement pstmtDel = ParamDeducaoServiceDAO.inicializaDel(con.getConexao()); 
		
		for (DespesaVenda pd : listaParDespesa) {
			pd.setCdReceita(receita.getCdReceita());
			pd.setCdCentroCusto(funcionario.getSetor().getCdCentroCusto());
			pd.setCdFuncionario(funcionario.getCdRecurso());
			
			for (int i = 0; i < pd.getPercDespesa().length; i++) {
				ParamDeducaoServiceDAO.atualizaParametros(pstmtSel, pstmtIns, pstmtUpd, pstmtDel, 
						plano.getCdPlano(),
						Integer.parseInt(String.format("%04d%02d", plano.getNrAno(), i+1)),
						receita.getCdReceita(),
						pd.getDeducao().getConta().getIdConta(),
						pd.getCdFuncionario(),
						pd.getPercDespesa()[i]
						);
			}	
			
		}
		
		pstmtSel.close();
		pstmtIns.close();
		pstmtUpd.close();
		pstmtDel.close();
		
		pstmtSel = null;
		pstmtIns = null;
		pstmtUpd = null;
		pstmtDel = null;
					
	}

	public List<DespesaVenda> listarDespesaVenda(Receita receita, Recurso funcionario) 
			throws Exception {
		return ParamDeducaoServiceDAO.listarDespesaVenda(con.getConexao(), receita, funcionario);
	}

	public List<DespesaRH> listarDespesaFolha(ContaContabil contaSalario) throws Exception {
		return ParamDespesaRHServiceDAO.listarDespesaFolha(con.getConexao(), contaSalario);
	}

	public void salvarDespesaFolha(List<DespesaRH> listaParDespesa) throws Exception {
			
		PreparedStatement pstmtSel = ParamDespesaRHServiceDAO.inicializaSel(con.getConexao()); 
		PreparedStatement pstmtIns = ParamDespesaRHServiceDAO.inicializaIns(con.getConexao()); 
		PreparedStatement pstmtUpd = ParamDespesaRHServiceDAO.inicializaUpd(con.getConexao()); 
		PreparedStatement pstmtDel = ParamDespesaRHServiceDAO.inicializaDel(con.getConexao()); 
		
		Plano plano = PlanoServiceDAO.getPlanoSelecionado();
		MesAnoOrcamento mesRef = new MesAnoOrcamento();
		for (DespesaRH pd : listaParDespesa) {
			for (int i = 0; i < pd.getPercDespesa().length; i++) {
				mesRef.setMes(i+1);
				ParamDespesaRHServiceDAO.atualizaParametros(pstmtSel, pstmtIns, pstmtUpd, pstmtDel, plano.getCdPlano(),
						mesRef.getAnoMes(), pd.getSalario().getIdConta(), pd.getEncargo().getIdConta(),
						pd.getPercDespesa()[i]);
			}
			
		}
		
	}

	
	public void gerarPlanilha(String caminho) throws Exception {
		ParamDeducaoServiceDAO.gerarPlanilha(con.getConexao(), caminho);
	}

	public void duplicarDadosCusvoVendaVendedor(CentroCusto setor, List<Recurso> funcionarios, Receita receita,
			Recurso origem, List<DespesaVenda> listaParDespesas) throws NumberFormatException, SQLException, NamingException {
		
		for (Recurso destino : funcionarios) {
			if (!origem.equals(destino)) {
				if (setor == null || setor.equals(destino.getSetor())) { // Se enviou setor, e ele for o mesmo setor do funcionário destino
					if (destino.getSetor().getTipos().indexOf(receita.getCategoria()) > -1) // Se o setor do funcionario destino orçar o tipo da receita selecionada
						ParamDeducaoServiceDAO.duplicarDadosCusvoVendaVendedor(con.getConexao(), receita, destino, listaParDespesas);
				}
			}
		}
	}

}
