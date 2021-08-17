package com.ctbli9.valorplan.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.FiltroReceita;
import com.ctbli9.valorplan.modelo.receita.CategoriaReceita;
import com.ctbli9.valorplan.modelo.receita.DeducaoReceita;
import com.ctbli9.valorplan.modelo.receita.Receita;
import com.ctbli9.valorplan.negocio.CategoriaReceitaService;
import com.ctbli9.valorplan.negocio.ContaContabilService;
import com.ctbli9.valorplan.negocio.ReceitaService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.modelo.FiltroContaContabil;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.LibUtilFaces;

@ManagedBean(name="cadastroReceitaBean")
@ViewScoped
public class CadastroReceitaBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<Receita> receitas = new ArrayList<Receita>();
	private Receita receita;
	private boolean novo;
	private List<CategoriaReceita> listaCategoria;
	private List<SelectItem> contasReceita; 
	private FiltroReceita filtro;
	
	private List<ContaContabil> despesasAnaliticas = null;
	private DeducaoReceita despesa = new DeducaoReceita();
	
	private int etapa;
	
	FacesMessages msg;
	
	public CadastroReceitaBean() {
		msg = new FacesMessages();
	}

	public List<Receita> getReceitas() {
		return receitas;
	}
	public void setReceitas(List<Receita> receitas) {
		this.receitas = receitas;
	}

	public Receita getReceita() {
		return receita;
	}
	public void setReceita(Receita receita) {
		this.receita = receita;
	}
	
	public boolean isNovo() {
		return novo;
	}
	public void setNovo(boolean novo) {
		this.novo = novo;
	}
	
	public List<CategoriaReceita> getListaCategoria() {
		return listaCategoria;
	}
	
	public List<SelectItem> getContasReceita() {
		if (this.contasReceita == null)
			this.contasReceita = carregaContas(new String[] {"RE"});  // 1 Receita Bruta
		
		return this.contasReceita;
	}
	
	public FiltroReceita getFiltro() {
		return filtro;
	}
	public void setFiltro(FiltroReceita filtro) {
		this.filtro = filtro;
	}
	
	public List<ContaContabil> getDespesasAnaliticas() {
		return despesasAnaliticas;
	}
		
	public DeducaoReceita getDespesa() {
		return despesa;
	}
	public void setDespesa(DeducaoReceita despesa) {
		this.despesa = despesa;
	}

	
	
	/*
	 * Metodos
	 */	
	private List<SelectItem> carregaContas(String[] tipos) {
		ArrayList<SelectItem> contas = new ArrayList<SelectItem>();
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			FiltroContaContabil filtroConta = new FiltroContaContabil();
			filtroConta.setTipos(tipos);
			List<ContaContabil> listaContas = new ContaContabilService(con).listar(filtroConta);
				
			contas.add(new SelectItem(null, "Selecione"));
				
			// FOR Tipo var : listaTipo
			for (ContaContabil conta : listaContas) {
				contas.add(new SelectItem(conta, (conta.getCdConta() + "-" + conta.getDsConta()) ));
			}
				
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
		
		return contas;
	}

	public void inicializar() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.listaCategoria = new CategoriaReceitaService(con).listarCategoriaReceita();
			
			this.filtro = new FiltroReceita();
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}//end inicializar

		
	public boolean isItemSelecionado() {
		return receita != null && receita.getCdReceita() != 0;
	}
	
	public void listarTudo() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.setReceitas(new ReceitaService(con).listarReceitas(this.filtro));
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		} 
	}//end listar
		
	public void inicializarRegistro() {
		receita = new Receita();
		receita.setCategoria(new CategoriaReceita());
		receita.setIdAtivo(true);
		this.novo = true;
		this.etapa = 1;
	}
	
	public boolean estaNaEtapa(int etapa) {
		return etapa == this.etapa;
	}
	
	public void alterarRegistro() {
		this.novo = false;
		this.etapa = 2;
	}
	
	public void salvar(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			new ReceitaService(con).salvarReceita(receita);
			msg.info("Receita gravada com sucesso!");

			int i = this.receitas.indexOf(this.receita);
			if (i > -1)
				this.receitas.set(i, this.receita);
			else
				this.receitas.add(0, this.receita);
			
			LibUtilFaces.atualizarView("frm:receitaDataTable", "frm:messages");
			
			this.etapa = 2;
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
			else
				LibUtilFaces.atualizarView("frm:messages");
		} finally {
			ConexaoDB.close(con);
		} 
	}//end salvar
	
	
	public void excluir(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			new ReceitaService(con).excluirReceita(receita);
			ConexaoDB.gravarTransacao(con);
			
			msg.info("Receita excluida!");
			
			this.receitas.remove(this.receita);
			this.receita = null;
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			if (e.getClass().getSimpleName().equalsIgnoreCase("RegraNegocioException"))
				FacesContext.getCurrentInstance().validationFailed();
		} finally {
			ConexaoDB.close(con);
		} 
	}//excluir

	
	public void listarDeducoes() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			this.receita.setDeducaoSobreVenda(new ReceitaService(con).listarDespesasSobreVenda(this.receita));
			this.receita.getDeducaoSobreVenda().add(0, new DeducaoReceita());
			
			FiltroContaContabil filtroConta = new FiltroContaContabil();
			filtroConta.setGrupos(new String[] {"R", "D", "C"});
			this.despesasAnaliticas = new ContaContabilService(con).listar(filtroConta);
			
			filtroConta = null;
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
		
	}	
	
	public void incluirDespesaSobreVenda() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			new ReceitaService(con).gravarDeducao(this.receita, this.despesa);
			ConexaoDB.gravarTransacao(con);
		
			this.receita.getDeducaoSobreVenda().add(this.despesa);
			
			this.etapa = 3;
			
			LibUtilFaces.atualizarView("frm:processoPanelGrid");
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
		

		this.despesa = new DeducaoReceita();
	}
	
	public void excluirDespesaSobreVenda() {
		if (this.receita.getDeducaoSobreVenda().indexOf(this.despesa) > -1) {
			ConexaoDB con = null;
			try {
				con = new ConexaoDB();
				new ReceitaService(con).excluirDeducao(this.despesa);
				ConexaoDB.gravarTransacao(con);
				
				this.receita.getDeducaoSobreVenda().remove(this.despesa);
				
				this.etapa = 3;
				
			} catch (Exception e) {
				Global.erro(con, e, msg, null);
			} finally {
				ConexaoDB.close(con);
			}
			
		}
		else
			msg.erro("Conta não relacionada.");
	}

}
