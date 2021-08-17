package com.ctbli9.valorplan.bean.adm;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.negocio.procavulso.ProcessamentoService;
import com.ctbli9.valorplan.recursos.Global;

import ctbli9.recursos.FacesMessages;

@ManagedBean(name="procAvulsoBean")
@ViewScoped
public class ProcessamentoAvulsoBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private FacesMessages msg = new FacesMessages();
	
	public void carregarReceitas() {
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			new ProcessamentoService(con).processa();
			
		} catch (Exception e) {
			Global.erro(con, e, msg, null);
		} finally {
			ConexaoDB.close(con);
		}
	}
	
	
	public void migrarBanco() {
		ConexaoDB conAtual = null;
		ConexaoDB conAnterior = null;
		try {
			conAtual = new ConexaoDB("valori9clm");
			conAnterior = new ConexaoDB("controller01");
			
			new ProcessamentoService(conAtual).migrarBanco(conAnterior);
			
		} catch (Exception e) {
			ConexaoDB.desfazerTransacao(conAnterior);
			ConexaoDB.desfazerTransacao(conAtual);
			Global.erro(conAtual, e, msg, null);
			
		} finally {
			
			ConexaoDB.close(conAtual);
			ConexaoDB.close(conAnterior);
		}
	}

}
