package com.ctbli9.valorplan.bean.orc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.CategImobilizado;
import com.ctbli9.valorplan.modelo.CentroCusto;
import com.ctbli9.valorplan.modelo.FiltroCentroCusto;
import com.ctbli9.valorplan.modelo.orc.OrcamentoInvestimento;
import com.ctbli9.valorplan.modelo.orc.OrcamentoInvestimentoMes;
import com.ctbli9.valorplan.negocio.CategImobilizadoService;
import com.ctbli9.valorplan.negocio.CentroCustoService;
import com.ctbli9.valorplan.negocio.orc.OrcInvestimentoService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.enumeradores.StatusPlano;
import ctbli9.recursos.FacesMessages;
import ctbli9.recursos.MesAno;

@ManagedBean(name="lancInvestimentoBean")
@ViewScoped
public class LancamentoInvestimentoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private CentroCusto cenCusto;
	private List<CentroCusto> listaCentroCusto;
	private List<CategImobilizado> listaCategoriaImobilizado;
	private CategImobilizado categoriaImobilizado;
	
	private List<OrcamentoInvestimento> listaInvestimento = new ArrayList<OrcamentoInvestimento>();
	private OrcamentoInvestimento investimento;
	private List<OrcamentoInvestimentoMes> valores;

	private FacesMessages messages = new FacesMessages();
	
	public CentroCusto getCenCusto() {
		return cenCusto;
	}
	public void setCenCusto(CentroCusto cenCusto) {
		this.cenCusto = cenCusto;
	}
	
	public List<CentroCusto> getListaCentroCusto() {
		return listaCentroCusto;
	}
	
	public List<CategImobilizado> getListaCategoriaImobilizado() {
		return listaCategoriaImobilizado;
	}
	
	public CategImobilizado getCategoriaImobilizado() {
		return categoriaImobilizado;
	}
	public void setCategoriaImobilizado(CategImobilizado categoriaImobilizado) {
		this.categoriaImobilizado = categoriaImobilizado;
	}
	
	public List<OrcamentoInvestimento> getListaInvestimento() {
		return listaInvestimento;
	}
	public void setListaInvestimento(List<OrcamentoInvestimento> listaInvestimento) {
		this.listaInvestimento = listaInvestimento;
	}
	
	public OrcamentoInvestimento getInvestimento() {
		return investimento;
	}
	public void setInvestimento(OrcamentoInvestimento investimento) {
		this.investimento = investimento;
	}
	
	public List<OrcamentoInvestimentoMes> getValores() {
		return valores;
	}
	public void setValores(List<OrcamentoInvestimentoMes> valores) {
		this.valores = valores;
	}
	
	public boolean isInativo() {
		return this.valores == null || this.valores.isEmpty() ||
				!PlanoServiceDAO.getPlanoSelecionado().getStatus().equals(StatusPlano._0);
	}
	/*
	 * Métodos
	 */
	public void iniciar() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
	    	// TODO
			//this.listaCategoriaImobilizado = new CategImobilizadoService(con.getConexao()).listar();
			FiltroCentroCusto filtro = new FiltroCentroCusto();
    		filtro.setSetorDoGestor(true);
			this.listaCentroCusto = new CentroCustoService(con).listar(filtro);
		
			cenCusto = Global.getFuncionarioLogado().getCenCusto();

			if (cenCusto == null)
				cenCusto = new CentroCusto();
			else
				listarInvestimentoOrcamento();
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void limpaInvestimento() {
		this.investimento = null;
		listarValores();
		listarInvestimentoOrcamento();
	}

	private void listarInvestimentoOrcamento() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
    		this.listaInvestimento = new OrcInvestimentoService(con).listarInvestimentos(this.cenCusto);
    		
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void carregarNovoInvestimento( ) {
		// Item em branco
		OrcamentoInvestimento item = new OrcamentoInvestimento(this.categoriaImobilizado);
		this.listaInvestimento.add(0, item);
		
		if (this.categoriaImobilizado != null) {
			item.setImobilizado(this.categoriaImobilizado);
			
			this.categoriaImobilizado = null;
			this.investimento = item;
			listarValores();
		}
	}
	
	public void listarValores() {
		if (this.investimento == null) {
			this.valores = null;
		} else {
			this.valores = this.investimento.getValores();
		}
	}
	
	
	public void gravarOrcamento(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new OrcInvestimentoService(con).gravarOrcInvestimento(this.cenCusto, this.investimento);
			ConexaoDB.gravarTransacao(con);
			
			listarInvestimentoOrcamento();
			listarValores();
			
			messages.info("Orçaamento gravado com sucesso.");
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public void excluirInvestimento() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new OrcInvestimentoService(con).excluirOrcInvestimento(this.investimento);
			ConexaoDB.gravarTransacao(con);
			
			listarInvestimentoOrcamento();
			listarValores();
			
			messages.info("Orçamento excluído com sucesso.");
			
		} catch (Exception e) {
			Global.erro(con, e, messages, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	public boolean itemEstaSelecionado(OrcamentoInvestimento item) {
		return this.investimento != null && this.investimento.getCdInvestimento() == item.getCdInvestimento();
	}
}
