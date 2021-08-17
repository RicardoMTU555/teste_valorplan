package com.ctbli9.valorplan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.FiltroCentroCusto;
import com.ctbli9.valorplan.modelo.ctb.EstornoConta;
import com.ctbli9.valorplan.negocio.CentroCustoService;
import com.ctbli9.valorplan.negocio.ContaContabilService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.modelo.FiltroContaContabil;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.FacesMessages;

@ManagedBean(name="cadEstornoContaBean")
@ViewScoped
public class CadEstornoContaBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private List<EstornoConta> listaContas = new ArrayList<EstornoConta>();
	private EstornoConta conta;
	private List<ContaContabil> listaPlacon;
	private List<CentroCusto> listaCentroCusto;
	
	private FacesMessages msg;
	
	public List<EstornoConta> getListaContas() {
		return listaContas;
	}
	public void setListaContas(List<EstornoConta> listaContas) {
		this.listaContas = listaContas;
	}

	public EstornoConta getConta() {
		return conta;
	}
	public void setConta(EstornoConta conta) {
		this.conta = conta;
	}
	
	public List<ContaContabil> getListaPlacon() {
		return listaPlacon;
	}
	public List<CentroCusto> getListaCentroCusto() {
		return listaCentroCusto;
	}

	/*
	 * 
	 */
	public void iniciar() {
		this.msg = new FacesMessages();
		
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.listaContas = new ContaContabilService(con).listarEstornoConta();
			
			FiltroContaContabil filtroConta = new FiltroContaContabil();
			listaPlacon = new ContaContabilService(con).listar(filtroConta);

    		FiltroCentroCusto filtroCencus = new FiltroCentroCusto();
    		this.listaCentroCusto = new CentroCustoService(con).listar(filtroCencus);

			
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void incluir() {
		this.listaContas.add(new EstornoConta());
	}

	public void excluir() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new ContaContabilService(con).excluirEstornoConta(this.conta);
			ConexaoDB.gravarTransacao(con);
			
			int i = 0;
			for (EstornoConta contaAux : this.listaContas) {
				if (contaAux.equals(this.conta)) {
					this.listaContas.remove(i);
					break;
				}
				i++;
			}
			
			this.msg.info("Conta removida.");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void salvar() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new ContaContabilService(con).gravarEstornoConta(this.listaContas);
			ConexaoDB.gravarTransacao(con);
			
			this.msg.info("Contas salvas com sucesso.");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	
}
