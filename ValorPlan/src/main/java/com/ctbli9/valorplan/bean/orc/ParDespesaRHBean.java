package com.ctbli9.valorplan.bean.orc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import com.ctbli9.valorplan.DAO.PlanoServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.DespesaRH;
import com.ctbli9.valorplan.negocio.ContaContabilService;
import com.ctbli9.valorplan.negocio.orc.ParamDespesaService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.enumeradores.StatusPlano;
import ctbli9.enumeradores.TipoConta;
import ctbli9.modelo.FiltroContaContabil;
import ctbli9.modelo.ctb.ContaContabil;
import ctbli9.recursos.FacesMessages;
import com.ctbli9.valorplan.recursos.MesAnoOrcamento;

@ManagedBean(name="despesaRHBean")
@ViewScoped
public class ParDespesaRHBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private ConexaoDB con = null;
	
	private ContaContabil contaSalario;
	private List<ContaContabil> contasSalario;
	private List<DespesaRH> listaParDespesas = new ArrayList<DespesaRH>();
	private DespesaRH despesaRH;
	
	private MesAnoOrcamento mesRef = new MesAnoOrcamento();
	private FacesMessages msg;
	
	/*
	 * Atributos
	 */
	
	public ContaContabil getContaSalario() {
		return contaSalario;
	}
	public void setContaSalario(ContaContabil contaSalario) {
		this.contaSalario = contaSalario;
	}
	public List<ContaContabil> getContasSalario() {
		return contasSalario;
	}
	
	public List<DespesaRH> getListaParDespesas() {
		return listaParDespesas;
	}
	public void setListaParDespesas(List<DespesaRH> listaParDespesas) {
		this.listaParDespesas = listaParDespesas;
	}

	public DespesaRH getDespesaRH() {
		return despesaRH;
	}
	public void setDespesaRH(DespesaRH despesaRH) {
		this.despesaRH = despesaRH;
	}
	
	public boolean isItemSelecionado() {
		return true;
	}
	
	public boolean isInativo() {
		return !PlanoServiceDAO.getPlanoSelecionado().getStatus().equals(StatusPlano._0) ||
				this.listaParDespesas.size() == 0;
	}
	
	public int getQuantParametros() {
		int quant = 0;
		if (this.listaParDespesas != null) {
			for (DespesaRH item : this.listaParDespesas) {
				if (item.isTemValor())
					quant++;
			}
		}
		
		return quant;
	}


	/*
	 * Metodos
	 */
	public void inicializar() {
		this.msg = new FacesMessages();
		
		try {
			con = new ConexaoDB();
			FiltroContaContabil filtro = new FiltroContaContabil();
			filtro.setTipos(new String[] {TipoConta.SL.toString()});
			this.contasSalario = new ContaContabilService(con).listar(filtro);
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
			
		} finally {
			ConexaoDB.close(con);			
		} 
		
	}//end inicializar
	
	public void listarDespesas() {
		
		if (this.contaSalario != null) {
			ConexaoDB con = null;
			try {
				con = new ConexaoDB();
				this.listaParDespesas = new ParamDespesaService(con).listarDespesaFolha(this.contaSalario);
				
			} catch (Exception e) {
				Global.erro(con, e, msg, null);
				
			} finally {
				ConexaoDB.close(con);			
			} 
		} else
			this.listaParDespesas =  new ArrayList<DespesaRH>();
	}//listarDespesas
	
	public void salvar(ActionEvent event) {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			new ParamDespesaService(con).salvarDespesaFolha(this.listaParDespesas);
			ConexaoDB.gravarTransacao(con);
			
			msg.info("Despesas gravadas com sucesso!");
			
		}  catch (Exception e) {
			ConexaoDB.desfazerTransacao(con);
			msg.erro(e.getMessage());
			e.printStackTrace();
			
		} finally {
			ConexaoDB.close(con);			
		}
	}//end salvar

	public boolean mesFechado(int mes) {
		mesRef.setMes(mes);
		return !mesRef.isMesAberto();
	}	
}
